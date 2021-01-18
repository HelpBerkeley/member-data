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
import java.util.List;
import java.util.Map;

public class DriverExporter extends Exporter {

    public static final String IN_COLUMN = "In";

    private final Tables tables;
    private final Map<String, String> driverDetails;

    public DriverExporter(List<User> users, Map<String, String> driverDetails) {
        this.tables = new Tables(users);
        this.driverDetails = driverDetails;
    }

    String drivers() {
        StringBuilder rows = new StringBuilder();
        rows.append(driverHeaders());

        for (User user : tables.drivers()) {

            String details = driverDetails.getOrDefault(user.getUserName(), "");

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
}
