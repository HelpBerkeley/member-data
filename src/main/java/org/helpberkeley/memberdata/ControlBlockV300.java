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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ControlBlockV300 extends ControlBlock {

    public static final String MORE_START_TIMES_THAN_DRIVERS =
            "More start times defined in control block than they are drivers.\n";
    public static final String MORE_DRIVERS_THAN_START_TIMES =
            "There are more drivers {0} than start times {1}.\n";
    public static final String INVALID_START_TIME =
            "\"{0}\" is not a valid start time. Must be of the form H:MM, H:MM PM (or AM), or HH:MM\n";

    private final List<String> altMealOptions = new ArrayList<>();
    private final List<String> altGroceryOptions = new ArrayList<>();
    private final List<String> startTimes = new ArrayList<>();

     ControlBlockV300(String header) {
         super(header);
    }

    void audit(Map<String, User> users, List<Driver> drivers) {
        StringBuilder errors = new StringBuilder();

        auditOpsManager(errors, users);
        auditBackupDrivers(errors, users);
        auditStartTimes(errors, drivers);

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }
    }

    @Override
    public String getVersion() {
        return Constants.CONTROL_BLOCK_VERSION_300;
    }

    @Override
    void processAltMealOptions(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
         altMealOptions.addAll(processList(value, lineNumber));
    }

    @Override
    void processAltGroceryOptions(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
        altGroceryOptions.addAll(processList(value, lineNumber));
    }

    @Override
    void processStartTimes(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
        startTimes.addAll(processList(value, lineNumber));
    }

    @Override
    List<String> processPickupManagers(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
        return processList(value, lineNumber);
    }

    public List<String> getAltMealOptions() {
         return altMealOptions;
    }

    public List<String> getAltGroceryOptions() {
         return altGroceryOptions;
    }

    public List<String> getStartTimes() {
         return startTimes;
    }

    private List<String> processList(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
         return Arrays.asList(value.split("\\s*,\\s*"));
    }

    private void auditStartTimes(StringBuilder errors, List<Driver> drivers) {

        // Error if there are more drivers than start times
        if (drivers.size() > startTimes.size()) {
            errors.append(MessageFormat.format(MORE_DRIVERS_THAN_START_TIMES,
                    drivers.size(), startTimes.size()));
        }

        // Warning if there are more start times than drivers
        if (startTimes.size() > drivers.size()) {
            warnings.append(MessageFormat.format(MORE_START_TIMES_THAN_DRIVERS,
                    startTimes.size(), drivers.size()));
        }

        // Error if start times are malformed

        String regex = "[0-2]?[0-9]:[0-5][0-9]\\s*([AP]M)?";
        Pattern pattern = Pattern.compile(regex);

        for (String startTime : startTimes) {
            Matcher matcher = pattern.matcher(startTime);
            if (! matcher.find()) {
                errors.append(MessageFormat.format(INVALID_START_TIME, startTime));
            }
        }
    }
}