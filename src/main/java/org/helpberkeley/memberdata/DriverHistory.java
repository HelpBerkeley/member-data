/*
 * Copyright (c) 2021. helpberkeley.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package org.helpberkeley.memberdata;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

public class DriverHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverHistory.class);

    static final int WEEKS_OF_HISTORY = 7;
    private static final int DAYS_IN_A_WEEK = DayOfWeek.values().length;
    private static final String DRIVER_HISTORY_HEADER =
        Constants.COLUMN_USERNAME + Constants.CSV_SEPARATOR + Constants.COLUMN_DELIVERY_DATE + '\n';

    private final LocalDate today;
    private final Week[] weeks = new Week[WEEKS_OF_HISTORY];

    private final String userName;
    Set<String> runDates = new HashSet<>();

    public DriverHistory(String userName) {
        this.userName = userName;
        this.today = LocalDate.now(Constants.TIMEZONE);
        calculateWeekBoundaries();
    }

    /** Test CTOR */
    DriverHistory(String userName, String todaysDate) {
        this.userName = userName;
        this.today = LocalDate.parse(todaysDate.replaceAll("/", "-"));
        calculateWeekBoundaries();
    }

    public List<Integer> getWeeklyRunTotals() {
        List<Integer> weeklyRunTotals = new ArrayList<>();
        for (Week week : weeks) {
            weeklyRunTotals.add(week.getNumberOfRuns());
        }

        return weeklyRunTotals;
    }

    public long totalRuns() {
        return runDates.size();
    }

    public void addRun(String runDate) {
        String date = runDate.replaceAll("/", "-");
        if (runDates.add(date)) {
            LocalDate localDate = LocalDate.parse(date);
            for (Week week : weeks) {
                if (week.incrementIfMatch(localDate)) {
                    break;
                }
            }
        }
    }

    /**
     * Generate a table of all driver history:
     *     DriverName, DeliveryDate
     *
     * @param apiClient
     * @return CSV table
     */
    public static String generateDriverHistory(ApiClient apiClient) throws IOException, CsvException {
        // Get the order history data posts
        OrderHistoryDataPosts orderHistoryDataPosts = new OrderHistoryDataPosts(apiClient);
        Collection<OrderHistoryData> postsToProcess = orderHistoryDataPosts.getAllPosts().values();

        // If a reset of the order history has been done, we are going to download
        // all of the delivery files.  Avoid getting rate limited by Discourse, which
        // occurs when there are more than 60 requests per minute on a connection.
        long napTime = (postsToProcess.size() > 10) ? TimeUnit.SECONDS.toMillis(1) : 0;

        StringBuilder output = new StringBuilder();
        output.append(DRIVER_HISTORY_HEADER);

        for (OrderHistoryData orderHistoryData : postsToProcess) {
            // Download the delivery file
            UploadFile uploadFile = orderHistoryData.getUploadFile();
            String deliveries = apiClient.downloadFile(uploadFile.getFileName());

            if (isBlacklisted(uploadFile.getOriginalFileName())) {
                LOGGER.info("Skipping blacklisted {}", uploadFile.getOriginalFileName());
            } else {
                LOGGER.info("Processing drivers from " + orderHistoryData.getDate());

                Collection<String> drivers =
                        HBParser.parseDeliveryDrivers(uploadFile.getOriginalFileName(), deliveries);

                final String deliveryDate = orderHistoryData.getDate();
                for (String driverName : drivers) {
                    output.append(driverName).append(Constants.CSV_SEPARATOR).append(deliveryDate).append('\n');
                }
            }

            try {
                Thread.sleep(napTime);
            } catch (InterruptedException ignored) { }
        }

        return output.toString();
    }

    private static final List<String> BLACKLIST = List.of(
            "HelpBerkeleyDeliveries - 12_20.csv",
            "HelpBerkeleyDeliveries - 3_30.csv",
            "HelpBerkeleyDeliveries - 3_31.csv",
            "HelpBerkeleyDeliveries - 4_10.csv",
            "HelpBerkeleyDeliveries - 4_17.csv",
            "HelpBerkeleyDeliveries - 4_18.csv",
            "HelpBerkeleyDeliveries - 4_28.csv",
            "HelpBerkeleyDeliveries - 4_4.csv",
            "HelpBerkeleyDeliveries - 4_7.csv",
            "HelpBerkeleyDeliveries - 5_20.csv",
            "HelpBerkeleyDeliveries - 5_23.csv",
            "HelpBerkeleyDeliveries - 5_28.csv",
            "HelpBerkeleyDeliveries - 5_7.csv",
            "HelpBerkeleyDeliveries - 5_8.csv",
            "HelpBerkeleyDeliveries - 6_04.csv");

    private static boolean isBlacklisted(String fileName) {
        return BLACKLIST.contains(fileName);
    }

    public static Map<String, DriverHistory> getDriverHistory(ApiClient apiClient) throws IOException, CsvException {
        String driverRuns = getDriverRuns(apiClient);

        Map<String, DriverHistory> driverHistory = new HashMap<>();

        CSVReader cvsReader = new CSVReader(new StringReader(driverRuns));
        List<String[]> lines = cvsReader.readAll();
        assert ! lines.isEmpty();
        String[] headers = lines.get(0);
        assert headers.length == 2 : headers.length;
        assert headers[0].equals(Constants.COLUMN_USERNAME) : headers[0];
        assert headers[1].equals(Constants.COLUMN_DELIVERY_DATE) : headers[1];

        lines.remove(0);
        for (String[] columns : lines) {
            assert columns.length == 2 : columns.length;

            DriverHistory driver = driverHistory.computeIfAbsent(columns[0], k -> new DriverHistory(k));
            driver.addRun(columns[1]);
        }

        return driverHistory;
    }

    private static String getDriverRuns(ApiClient apiClient) {
        // Get Driver history post
        String driverHistoryPost = apiClient.getPost(Main.DRIVER_HISTORY_POST_ID);
        // Get file link to download
        UploadFile driverHistoryFile = HBParser.parseFileFromPost(driverHistoryPost);
        // Download it.
        return apiClient.downloadFile(driverHistoryFile.getFileName());

    }

    // Work out the date boundaries for this week, and each of the previous weeks
    // Weeks run Monday through Sunday
    private void calculateWeekBoundaries() {

        // Find Monday of this week
        LocalDate monday = today.with(previousOrSame(MONDAY));
        LocalDate sunday = today.with(nextOrSame(SUNDAY));

        for (int week = 0; week < WEEKS_OF_HISTORY; week++) {
            weeks[week] = new Week(monday.minusDays(DAYS_IN_A_WEEK * week), sunday.minusDays(DAYS_IN_A_WEEK * week));
        }
    }

    private static class Week {
        private final LocalDate monday;
        private final LocalDate sunday;
        private int numberOfRuns;

        Week(LocalDate monday, LocalDate sunday) {
            this.monday = monday;
            this.sunday = sunday;
        }

        boolean incrementIfMatch(LocalDate runDate) {
            if ((runDate.compareTo(monday) >= 0) &&
                    runDate.compareTo(sunday) <= 0) {
                numberOfRuns++;
                return true;
            }

            return false;
        }

        int getNumberOfRuns() {
            return numberOfRuns;
        }
    }
}