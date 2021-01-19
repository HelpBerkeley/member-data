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

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DriverExporter extends Exporter {

    public static final String IN_COLUMN = "In";

    private final Tables tables;
    private final Map<String, DetailsPost> driverDetails;

    public DriverExporter(List<User> users, Map<String, DetailsPost> driverDetails) {
        this.tables = new Tables(users);
        this.driverDetails = driverDetails;
    }

    String drivers() {
        StringBuilder rows = new StringBuilder();
        rows.append(driverHeaders());

        for (User user : tables.drivers()) {

            DetailsPost detailsPost = driverDetails.get(user.getUserName());
            String details = (detailsPost == null) ? "" : detailsPost.getDetails();

            rows.append(user.getCreateDate());
            rows.append(separator);
            rows.append(escapeCommas(user.getName()));
            rows.append(separator);
            rows.append(user.getUserName());
            rows.append(separator);
            rows.append(shortBoolean(user.isAvailableDriver()));
            rows.append(separator);
            rows.append(shortBoolean(user.isTrainedDriver()));
            rows.append(separator);
            rows.append(shortBoolean(user.isBiker()));
            rows.append(separator);
            rows.append(shortBoolean(user.isLimitedRuns()));
            rows.append(separator);
            rows.append(user.getPhoneNumber());
            rows.append(separator);
            rows.append(user.getAltPhoneNumber());
            rows.append(separator);
            rows.append(escapeCommas(user.getCity()));
            rows.append(separator);
            rows.append(escapeCommas(user.getAddress()));
            rows.append(separator);
            rows.append(shortBoolean(user.isAtRisk()));
            rows.append(separator);
            rows.append(shortBoolean(user.isGone()));
            rows.append(separator);
            rows.append(shortBoolean(user.isOut()));
            rows.append(separator);
            rows.append(shortBoolean(user.isOtherDrivers()));
            rows.append(separator);
            rows.append(shortBoolean(user.isEventDriver()));
            rows.append(separator);
            rows.append(shortBoolean(user.isTrainedEventDriver()));
            rows.append(separator);
            rows.append(escapeCommas(details));
            rows.append('\n');
        }

        return rows.toString();
    }

    String driverHeaders() {
        return User.CREATED_AT_COLUMN
                + separator
                + User.NAME_COLUMN
                + separator
                + User.USERNAME_COLUMN
                + separator
                + IN_COLUMN
                + separator
                + User.SHORT_TRAINED_DRIVER_COLUMN
                + separator
                + User.SHORT_BIKERS_COLUMN
                + separator
                + User.SHORT_LIMITED_RUNS_COLUMN
                + separator
                + User.PHONE_NUMBER_COLUMN
                + separator
                + User.ALT_PHONE_NUMBER_COLUMN
                + separator
                + User.CITY_COLUMN
                + separator
                + User.ADDRESS_COLUMN
                + separator
                + User.SHORT_AT_RISK_COLUMN
                + separator
                + User.SHORT_GONE_COLUMN
                + separator
                + User.SHORT_OUT_COLUMN
                + separator
                + User.SHORT_OTHER_DRIVERS_COLUMN
                + separator
                + User.SHORT_EVENTS_DRIVER_COLUMN
                + separator
                + User.SHORT_TRAINED_EVENT_DRIVER_COLUMN
                + separator
                + User.SHORT_DRIVER_DETAILS_COLUMN
                + '\n';
    }

    String driversToFile() throws IOException {

        String outputFileName = generateFileName(Constants.DRIVERS_FILE, "csv");
        writeFile(outputFileName, drivers());
        return outputFileName;
    }

    List<DetailedDriver> availableDetailedDrivers() {

        // Create a list of available DetailedDrivers
        List<DetailedDriver> detailedDrivers = new ArrayList<>();
        for (User driver : tables.availableDrivers()) {
            detailedDrivers.add(new DetailedDriver(driver, driverDetails.get(driver.getUserName())));
        }

        // Sort by those with details, then alphabetically by user name
        detailedDrivers.sort(
                Comparator.comparing(DetailedDriver::getLatestDetailsPostNumber, Comparator.reverseOrder())
                        .thenComparing(DetailedDriver::getUserName));

        return detailedDrivers;
    }

    Post shortPost() {

        // get available  DetailedDrivers
        List<DetailedDriver> detailedDrivers = availableDetailedDrivers();


        //|MichelThouati|262-434-0554|:no_entry_sign:|:warning:|:bike:|26|:green_circle::red_circle::red_circle::green_circle:|
        //|MPFarrell|262-303-6233||:warning:||17|:red_circle::red_circle::red_circle::green_circle:|
        //|Crestonia|510-555-1212||:warning:||9|:red_circle::green_circle::red_circle::green_circle:|

        StringBuilder output = new StringBuilder();
        output.append("|UserName|phn .#|lt|ar|bk|rn|3. 2. 1. 0|\n");
        output.append("|---|---|---|---|---|---|---|---|---|---|---|---|\n");

        for (DetailedDriver detailedDriver : detailedDrivers) {
            output.append(detailedDriver.getUserName());
            output.append('|');
            output.append(detailedDriver.getPhoneNumber());
            output.append('|');
            output.append(detailedDriver.isLimitedRuns() ? Constants.LIMITED_RUNS_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isAtRisk() ? Constants.AT_RISK_EMOJI : "");
            output.append('|');
            output.append(detailedDriver.isBiker() ? Constants.BIKE_EMOJI : "");
            output.append("|\n");
        }

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

        Post post = new Post();
        post.title = "Driver Post [short]";
        post.topic_id = Constants.TOPIC_DRIVER_TABLE_SHORT.getId();
        post.raw = output.toString();
        post.createdAt = timeStamp;

        return post;
    }

    Post longPost() {

        // get available  DetailedDrivers
        List<DetailedDriver> detailedDrivers = availableDetailedDrivers();

        StringBuilder output = new StringBuilder();
        output.append("|UserName|phn .#|l i m i t e d|a t - r i s k|b i k e|r u n s|3 2|1 0|details|\n");
        output.append("|---|---|---|---|---|---|---|---|---|\n");

        for (DetailedDriver detailedDriver : detailedDrivers) {
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
            output.append('|');
            output.append('|');
            output.append('|');
            output.append(detailedDriver.getDetails());
            output.append("|\n");
        }

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

        Post post = new Post();
        post.title = "Driver Post [long]";
        post.topic_id = Constants.TOPIC_DRIVER_TABLE_LONG.getId();
        post.raw = output.toString();
        post.createdAt = timeStamp;

        return post;
    }
}