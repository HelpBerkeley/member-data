/*
 * Copyright (c) 2021-2022. helpberkeley.org
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

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DriverExporter extends Exporter {

    public static final String IN_COLUMN = "In";
    public static final String TOTAL_RUNS_COLUMNS = "Runs";
    public static final String THIS_WEEK_COLUMN = "0w";
    public static final String ONE_WEEK_AGO_COLUMN = "1w";
    public static final String TWO_WEEKS_AGO_COLUMN = "2w";
    public static final String THREE_WEEKS_AGO_COLUMN = "3w";
    public static final String FOUR_WEEKS_AGO_COLUMN = "4w";
    public static final String FIVE_WEEKS_AGO_COLUMN = "5w";
    public static final String SIX_WEEKS_AGO_COLUMN = "6w";

    private final Tables tables;
    private final Map<String, DetailsPost> driverDetails;
    private final Map<String, DriverHistory> driverHistory;

    public DriverExporter(List<User> users, Map<String,
            DriverHistory> driverHistory, Map<String, DetailsPost> driverDetails) {
        this.tables = new Tables(users);
        this.driverDetails = driverDetails;
        // Driver user name key is lower case in this map. See DriverHistory.doGetDriverHistory()
        this.driverHistory = driverHistory;
    }

    String drivers() throws IOException {
        StringWriter writer = new StringWriter();
        CSVListWriter csvWriter = new CSVListWriter(writer);
        List<List<String>> dataToEncode = new ArrayList<>();
        dataToEncode.add(driverHeaders());

        for (User user : tables.drivers()) {

            DetailsPost detailsPost = driverDetails.get(user.getUserName());
            String details = (detailsPost == null) ? "" : detailsPost.getDetails();
            DriverHistory history = driverHistory.get(user.getUserName().toLowerCase());
            List<Integer> weeklyHistory =
                    (history != null) ? history.getWeeklyRunTotals() : List.of(0, 0, 0, 0, 0, 0, 0);

            List<String> row = new ArrayList<>(Arrays.asList(user.getCreateDate(), user.getName(), user.getUserName(),
                    shortBoolean(user.isAvailableDriver()), shortBoolean(user.isTrainedDriver()), shortBoolean(user.isBiker()),
                    shortBoolean(user.isLimitedRuns()), history != null ? String.valueOf(history.totalRuns()) : "0",
                    weeklyHistory.get(6) != 0 ? "🟢" : "", weeklyHistory.get(5) != 0 ? "🟢" : "", weeklyHistory.get(4) != 0 ? "🟢" : "",
                    weeklyHistory.get(3) != 0 ? "🟢" : "", weeklyHistory.get(2) != 0 ? "🟢" : "", weeklyHistory.get(1) != 0 ? "🟢" : "",
                    weeklyHistory.get(0) != 0 ? "🟢" : "", user.getPhoneNumber(), user.getAltPhoneNumber(), user.getCity(),
                    user.getAddress(), shortBoolean(user.isAtRisk()), shortBoolean(user.isGone()), shortBoolean(user.isOut()),
                    shortBoolean(user.isOtherDrivers()), shortBoolean(user.isEventDriver()), shortBoolean(user.isTrainedEventDriver()),
                    details));
            dataToEncode.add(row);
        }

        csvWriter.writeAllToList(dataToEncode);
        csvWriter.close();
        return writer.toString();
    }


    List<String> driverHeaders() {
       return new ArrayList<>(Arrays.asList(User.CREATED_AT_COLUMN, User.NAME_COLUMN, User.USERNAME_COLUMN, IN_COLUMN,
               User.SHORT_TRAINED_DRIVER_COLUMN, User.SHORT_BIKERS_COLUMN, User.SHORT_LIMITED_RUNS_COLUMN, TOTAL_RUNS_COLUMNS,
               SIX_WEEKS_AGO_COLUMN, FIVE_WEEKS_AGO_COLUMN, FOUR_WEEKS_AGO_COLUMN, THREE_WEEKS_AGO_COLUMN, TWO_WEEKS_AGO_COLUMN,
               ONE_WEEK_AGO_COLUMN, THIS_WEEK_COLUMN, User.PHONE_NUMBER_COLUMN, User.ALT_PHONE_NUMBER_COLUMN, User.CITY_COLUMN,
               User.ADDRESS_COLUMN, User.SHORT_AT_RISK_COLUMN, User.SHORT_GONE_COLUMN, User.SHORT_OUT_COLUMN, User.SHORT_OTHER_DRIVERS_COLUMN,
               User.SHORT_EVENTS_DRIVER_COLUMN, User.SHORT_TRAINED_EVENT_DRIVER_COLUMN, User.SHORT_DRIVER_DETAILS_COLUMN));
    }

    String driversToFile() throws IOException {

        String outputFileName = generateFileName(Constants.DRIVERS_FILE, "csv");
        writeFile(outputFileName, drivers());
        return outputFileName;
    }

    List<DetailedDriver> availableDriversWithDetailsPosts() {

        // Create a list of available DetailedDrivers with details posts
        List<DetailedDriver> detailedDrivers = new ArrayList<>();
        for (User driver : tables.availableDrivers()) {

            DetailsPost detailsPost = driverDetails.get(driver.getUserName());

            if (detailsPost != null) {
                detailedDrivers.add(new DetailedDriver(driver, detailsPost));
            }
        }

        // Sort by those with details, then alphabetically by user name
        detailedDrivers.sort(
                Comparator.comparing(DetailedDriver::getLatestDetailsPostNumber, Comparator.reverseOrder())
                        .thenComparing(DetailedDriver::getUserName));

        return detailedDrivers;
    }

    List<DetailedDriver> availableDetailedDriversByReverseCreationDate() {

        // Create a list of available DetailedDrivers
        List<DetailedDriver> detailedDrivers = new ArrayList<>();
        for (User driver : tables.availableDrivers()) {
            detailedDrivers.add(new DetailedDriver(driver, driverDetails.get(driver.getUserName())));

        }

        // Sort by those with details, then alphabetically by user name
        detailedDrivers.sort(Comparator.comparing(DetailedDriver::getCreateDate, Comparator.reverseOrder()));

        return detailedDrivers;
    }

    List<DetailedDriver> availableEventDriversWithDetailsPosts() {

        // Create a list of available DetailedDrivers with details posts
        List<DetailedDriver> detailedDrivers = new ArrayList<>();
        for (User driver : tables.availableEventDrivers()) {

            DetailsPost detailsPost = driverDetails.get(driver.getUserName());

            if (detailsPost != null) {
                detailedDrivers.add(new DetailedDriver(driver, detailsPost));
            }
        }

        // Sort by those with details, then alphabetically by user name
        detailedDrivers.sort(
                Comparator.comparing(DetailedDriver::getLatestDetailsPostNumber, Comparator.reverseOrder())
                        .thenComparing(DetailedDriver::getUserName));

        return detailedDrivers;
    }

    List<DetailedDriver> availableDetailedEventDriversByReverseCreationDate() {

        // Create a list of available DetailedDrivers
        List<DetailedDriver> detailedDrivers = new ArrayList<>();
        for (User driver : tables.availableEventDrivers()) {
            detailedDrivers.add(new DetailedDriver(driver, driverDetails.get(driver.getUserName())));

        }

        // Sort by those with details, then alphabetically by user name
        detailedDrivers.sort(Comparator.comparing(DetailedDriver::getCreateDate, Comparator.reverseOrder()));

        return detailedDrivers;
    }

    List<DetailedDriver> outDrivers() {

        List<DetailedDriver> outDrivers = new ArrayList<>();

        for (User driver : tables.outDrivers()) {
            outDrivers.add(new DetailedDriver(driver, driverDetails.get(driver.getUserName())));
        }

        // Sort by user name
        outDrivers.sort(Comparator.comparing(DetailedDriver::getUserName, String.CASE_INSENSITIVE_ORDER));

        return outDrivers;
    }

    String shortPost() {

        // get available  DetailedDrivers
        List<DetailedDriver> detailedDrivers = availableDetailedDriversByReverseCreationDate();

        StringBuilder output = new StringBuilder();

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));
        output.append("Updated: ").append(timeStamp).append("\n");

        output.append("|UserName|Full Name|phn .#|lt|ar|bk|rn|3. 2. 1. 0|\n");
        output.append("|---|---|---|---|---|---|---|---|---|---|---|---|---|\n");

        for (DetailedDriver detailedDriver : detailedDrivers) {

            DriverHistory history = driverHistory.get(detailedDriver.getUserName().toLowerCase());

            String recentRuns;
            String totalRuns = "0";

            if (history != null) {
                List<Integer> weeklyRuns = history.getWeeklyRunTotals();

                recentRuns = (weeklyRuns.get(3) > 0 ? ":green_circle:" : ":red_circle:")
                    + (weeklyRuns.get(2) > 0 ? ":green_circle:" : ":red_circle:")
                    + (weeklyRuns.get(1) > 0 ? ":green_circle:" : ":red_circle:")
                    + (weeklyRuns.get(0) > 0 ? ":green_circle:" : ":red_circle:");
                totalRuns = Long.toString(history.totalRuns());
            } else {
                recentRuns = ":red_circle::red_circle::red_circle::red_circle:";
            }

            // Skip drivers inactive in the past 4 weeks
            if (recentRuns.equals(":red_circle::red_circle::red_circle::red_circle:")) {
                continue;
            }

            String name = detailedDriver.getName();
            if (name.length() > 19) {
                name = name.substring(0, 18);
                name += '.';
            }

            output.append(detailedDriver.getUserName());
            output.append('|');
            output.append(name);
            output.append('|');
            output.append(detailedDriver.getPhoneNumber());
            output.append('|');
            output.append(detailedDriver.isLimitedRuns() ? Constants.LIMITED_RUNS_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isAtRisk() ? Constants.AT_RISK_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isBiker() ? Constants.BIKE_EMOJI : "");
            output.append('|');
            output.append(totalRuns);
            output.append('|');
            output.append(recentRuns);
            output.append("|\n");
        }

        return output.toString();
    }

    String longPost() {

        // get available  DetailedDrivers
        List<DetailedDriver> detailedDrivers = availableDriversWithDetailsPosts();

        StringBuilder output = new StringBuilder();

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));
        output.append("Updated: ").append(timeStamp).append("\n");

        output.append("|UserName|phn .#|l i m i t e d|a t - r i s k|b i k e|r u n s|3 2|1 0|details|\n");
        output.append("|---|---|---|---|---|---|---|---|---|\n");

        for (DetailedDriver detailedDriver : detailedDrivers) {

            DriverHistory history = driverHistory.get(detailedDriver.getUserName().toLowerCase());

            String recentRuns32;
            String recentRuns10;
            String totalRuns = "0";

            if (history != null) {
                List<Integer> weeklyRuns = history.getWeeklyRunTotals();

                recentRuns32 = (weeklyRuns.get(3) > 0 ? ":green_circle:" : ":red_circle:")
                        + (weeklyRuns.get(2) > 0 ? ":green_circle:" : ":red_circle:");
                recentRuns10 = (weeklyRuns.get(1) > 0 ? ":green_circle:" : ":red_circle:")
                        + (weeklyRuns.get(0) > 0 ? ":green_circle:" : ":red_circle:");

                totalRuns = Long.toString(history.totalRuns());
            } else {
                recentRuns32 = ":red_circle::red_circle:";
                recentRuns10 = ":red_circle::red_circle:";
            }

            output.append(detailedDriver.getUserName());
            output.append('|');
            output.append(detailedDriver.getPhoneNumber());
            output.append('|');
            output.append(detailedDriver.isLimitedRuns() ? Constants.LIMITED_RUNS_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isAtRisk() ? Constants.AT_RISK_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isBiker() ? Constants.BIKE_EMOJI : "");
            output.append('|');
            output.append(totalRuns);
            output.append('|');
            output.append(recentRuns32);
            output.append('|');
            output.append(recentRuns10);
            output.append('|');
            output.append(detailedDriver.getDetails());
            output.append("|\n");
        }

        return output.toString();
    }

    String eventDriversShortPost() {

        // get available  DetailedDrivers
        List<DetailedDriver> detailedDrivers = availableDetailedEventDriversByReverseCreationDate();

        StringBuilder output = new StringBuilder();

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));
        output.append("Updated: ").append(timeStamp).append("\n");

        output.append("|UserName|Full Name|phn .#|lt|ar|bk|rn|3. 2. 1. 0|\n");
        output.append("|---|---|---|---|---|---|---|---|\n");

        for (DetailedDriver detailedDriver : detailedDrivers) {

            DriverHistory history = driverHistory.get(detailedDriver.getUserName().toLowerCase());

            String recentRuns;
            String totalRuns = "0";

            if (history != null) {
                List<Integer> weeklyRuns = history.getWeeklyRunTotals();

                recentRuns = (weeklyRuns.get(3) > 0 ? ":green_circle:" : ":red_circle:")
                        + (weeklyRuns.get(2) > 0 ? ":green_circle:" : ":red_circle:")
                        + (weeklyRuns.get(1) > 0 ? ":green_circle:" : ":red_circle:")
                        + (weeklyRuns.get(0) > 0 ? ":green_circle:" : ":red_circle:");
                totalRuns = Long.toString(history.totalRuns());
            } else {
                recentRuns = ":red_circle::red_circle::red_circle::red_circle:";
            }

            // Skip drivers inactive in the past 4 weeks
            if (recentRuns.equals(":red_circle::red_circle::red_circle::red_circle:")) {
                continue;
            }

            String name = detailedDriver.getName();
            if (name.length() > 19) {
                name = name.substring(0, 18);
                name += '.';
            }

            output.append(detailedDriver.getUserName());
            output.append('|');
            output.append(name);
            output.append('|');
            output.append(detailedDriver.getPhoneNumber());
            output.append('|');
            output.append(detailedDriver.isLimitedRuns() ? Constants.LIMITED_RUNS_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isAtRisk() ? Constants.AT_RISK_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isBiker() ? Constants.BIKE_EMOJI : "");
            output.append('|');
            output.append(totalRuns);
            output.append('|');
            output.append(recentRuns);
            output.append("|\n");
        }

        return output.toString();
    }

    String eventDriversLongPost() {

        // get available  DetailedDrivers
        List<DetailedDriver> detailedDrivers = availableEventDriversWithDetailsPosts();

        StringBuilder output = new StringBuilder();

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));
        output.append("Updated: ").append(timeStamp).append("\n");

        output.append("|UserName|phn .#|l i m i t e d|a t - r i s k|b i k e|r u n s|3 2|1 0|details|\n");
        output.append("|---|---|---|---|---|---|---|---|---|\n");

        for (DetailedDriver detailedDriver : detailedDrivers) {

            DriverHistory history = driverHistory.get(detailedDriver.getUserName().toLowerCase());

            String recentRuns32;
            String recentRuns10;
            String totalRuns = "0";

            if (history != null) {
                List<Integer> weeklyRuns = history.getWeeklyRunTotals();

                recentRuns32 = (weeklyRuns.get(3) > 0 ? ":green_circle:" : ":red_circle:")
                        + (weeklyRuns.get(2) > 0 ? ":green_circle:" : ":red_circle:");
                recentRuns10 = (weeklyRuns.get(1) > 0 ? ":green_circle:" : ":red_circle:")
                        + (weeklyRuns.get(0) > 0 ? ":green_circle:" : ":red_circle:");

                totalRuns = Long.toString(history.totalRuns());
            } else {
                recentRuns32 = ":red_circle::red_circle:";
                recentRuns10 = ":red_circle::red_circle:";
            }

            output.append(detailedDriver.getUserName());
            output.append('|');
            output.append(detailedDriver.getPhoneNumber());
            output.append('|');
            output.append(detailedDriver.isLimitedRuns() ? Constants.LIMITED_RUNS_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isAtRisk() ? Constants.AT_RISK_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isBiker() ? Constants.BIKE_EMOJI : "");
            output.append('|');
            output.append(totalRuns);
            output.append('|');
            output.append(recentRuns32);
            output.append('|');
            output.append(recentRuns10);
            output.append('|');
            output.append(detailedDriver.getDetails());
            output.append("|\n");
        }

        return output.toString();
    }

    List<DetailedDriver> needsTrainingDetailedDrivers() {

        // Create a list of available DetailedDrivers
        List<DetailedDriver> detailedDrivers = new ArrayList<>();
        for (User driver : tables.drivers()) {

            if (driver.isGone() || driver.isOut() || driver.isOtherDrivers()) {
                continue;
            }

            boolean trainedDriver = driver.isTrainedDriver();
            boolean trainedEventDriver = driver.isTrainedEventDriver();
            boolean eventDriver = driver.isEventDriver();

            if (eventDriver) {
                if (! trainedEventDriver) {
                    detailedDrivers.add(new DetailedDriver(driver, driverDetails.get(driver.getUserName())));
                }
            } else if (! trainedDriver) {
                detailedDrivers.add(new DetailedDriver(driver, driverDetails.get(driver.getUserName())));
            }
        }

        // Sort by creation data, oldest to newest.
        detailedDrivers.sort(Comparator.comparing(DetailedDriver::getCreateDate));

        return detailedDrivers;
    }

    Optional<Post> needsTraining() {

        List<DetailedDriver> detailedDrivers = needsTrainingDetailedDrivers();

        if (detailedDrivers.isEmpty()) {
            return Optional.empty();
        }

        StringBuilder output = new StringBuilder();
        output.append("|User Name|Name|Phone #|Wait|Event|Details|\n");
        output.append("|---|---|---|---|---|---|\n");

        ZonedDateTime now = ZonedDateTime.now();

        for (DetailedDriver detailedDriver : detailedDrivers) {

            ZonedDateTime createTime = ZonedDateTime.parse(detailedDriver.getCreateTime());

            output.append('@');
            output.append(detailedDriver.getUserName());
            output.append('|');
            output.append(detailedDriver.getName());
            output.append('|');
            output.append(detailedDriver.getPhoneNumber());
            output.append('|');
            output.append(Math.abs(ChronoUnit.DAYS.between(now, createTime)));
            output.append('|');
            output.append(detailedDriver.isEventDriver() ? Constants.GIFT_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.getDetails());
            output.append("|\n");
        }

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

        Post post = new Post();
        post.title = "Driver Post [long]";
        post.topic_id = Constants.TOPIC_DRIVER_TRAINING_TABLE.getId();
        post.raw = output.toString();
        post.createdAt = timeStamp;

        return Optional.of(post);
    }

    String outPost() {

        List<DetailedDriver> outDrivers = outDrivers();

        if (outDrivers.isEmpty()) {
            return "";
        }

        StringBuilder output = new StringBuilder();

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));
        output.append("Updated: ").append(timeStamp).append("\n");

        output.append("|User Name|Name|Phone #|Event|Details|\n");
        output.append("|---|---|---|---|---|\n");

        for (DetailedDriver detailedDriver : outDrivers) {

            output.append('@');
            output.append(detailedDriver.getUserName());
            output.append('|');
            output.append(detailedDriver.getName());
            output.append('|');
            output.append(detailedDriver.getPhoneNumber());
            output.append('|');
            output.append(detailedDriver.isEventDriver() ? Constants.GIFT_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.getDetails());
            output.append("|\n");
        }

        return output.toString();
    }
}
