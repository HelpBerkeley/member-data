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
    public static final String TOO_MANY_START_TIMES_VARIABLES = Constants.CONTROL_BLOCK_START_TIMES
            + " is defined more than once in the control block.\n";
    public static final String TOO_MANY_FOOD_SOURCES_VARIABLES = Constants.CONTROL_BLOCK_FOOD_SOURCES
            + " is defined more than once in the control block.\n";
    public static final String TOO_MANY_ALT_MEAL_OPTIONS_VARIABLES = Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS
            + " is defined more than once in the control block.\n";
    public static final String TOO_MANY_ALT_GROCERY_OPTIONS_VARIABLES = Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS
            + " is defined more than once in the control block.\n";
    public static final String TOO_MANY_PICKUP_MANAGER_VARIABLES = Constants.CONTROL_BLOCK_PICKUP_MANAGERS
            + " is defined more than once in the control block.\n";
    public static final String MISSING_FOOD_SOURCES_VARIABLE =
            "Required " + Constants.CONTROL_BLOCK_FOOD_SOURCES + " control block variable is missing.\n";
    public static final String MISSING_START_TIMES_VARIABLE =
            "Required " + Constants.CONTROL_BLOCK_START_TIMES + " control block variable is missing.\n";
    public static final String MISSING_ALT_MEAL_OPTIONS_VARIABLE =
            "Required " + Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS + " control block variable is missing.\n";
    public static final String MISSING_ALT_GROCERY_OPTIONS_VARIABLE =
            "Required " + Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS + " control block variable is missing.\n";
    public static final String MISSING_PICKUP_MANAGERS_VARIABLE =
            "Required " + Constants.CONTROL_BLOCK_PICKUP_MANAGERS + " control block variable is missing.\n";

    private List<String> pickupManagers = null;
    private List<String> altMealOptions = null;
    private List<String> altGroceryOptions = null;
    private final List<String> startTimes = new ArrayList<>();
    private String mealSource = "";
    private String grocerySource = "";

    ControlBlockV300(String header) {
         super(header);
    }

    void audit(Map<String, User> users, List<Driver> drivers) {
        StringBuilder errors = new StringBuilder();

        auditOpsManager(errors, users);
        auditBackupDrivers(errors, users);
        auditStartTimes(errors, drivers);
        auditFoodSources(errors);
        auditAltMealOptions(errors);
        auditAltGroceryOptions(errors);
        auditPickupManagers(errors);

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

        if (altMealOptions != null) {
            throw new MemberDataException(TOO_MANY_ALT_MEAL_OPTIONS_VARIABLES);
        }

        altMealOptions = new ArrayList<>();
        altMealOptions.addAll(processList(value));
    }

    @Override
    void processAltGroceryOptions(String value, long lineNumber) {
        if (altGroceryOptions != null) {
            throw new MemberDataException(TOO_MANY_ALT_GROCERY_OPTIONS_VARIABLES);
        }
        altGroceryOptions = new ArrayList<>();
        altGroceryOptions.addAll(processList(value));
    }

    @Override
    void processStartTimes(String value, long lineNumber) {
        if (! startTimes.isEmpty()) {
            throw new MemberDataException(TOO_MANY_START_TIMES_VARIABLES);
        }

        startTimes.addAll(processList(value));
    }

    @Override
    void processPickupManagers(String value, long lineNumber) {
        if (pickupManagers != null) {
            throw new MemberDataException(TOO_MANY_PICKUP_MANAGER_VARIABLES);
        }
        pickupManagers = new ArrayList<>();
        pickupManagers.addAll(processList(value));
    }


    //
    // A FoodSources data field should look like "meal source  | grocery source"
    //
    @Override
    void processFoodSources(String value, long lineNumber) {

         if (! (mealSource.isEmpty() && grocerySource.isEmpty())) {
             throw new MemberDataException(TOO_MANY_FOOD_SOURCES_VARIABLES);

         }

        String[] fields = value.split("\\" + INTRA_FIELD_SEPARATOR, -42);

        if (fields.length != 2) {
            throw new MemberDataException("FoodSources value \"" + value
                    + "\" at line " + lineNumber + " does not match \"Meal source | Grocery source\".\n");
        }

        mealSource = fields[0].trim();
        grocerySource = fields[1].trim();

        if (mealSource.isEmpty()) {
            warnings.append("Line ").append(lineNumber).append(", No meal source specified.\n");
        }

        if (grocerySource.isEmpty()) {
            warnings.append("Line ").append(lineNumber).append(", No grocery source specified.\n");
        }
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

    public String getMealSource() {
         return mealSource;
    }

    public String getGrocerySource() {
         return grocerySource;
    }

    List<String> getPickupManagers() {
        return pickupManagers;
    }

    private List<String> processList(String value) {
        // FIX THIS, DS: is there auditing to do here?
         return Arrays.asList(value.split("\\s*,\\s*"));
    }

    private void auditStartTimes(StringBuilder errors, List<Driver> drivers) {

        if (startTimes.isEmpty()) {
            errors.append(MISSING_START_TIMES_VARIABLE);
            return;
        }

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

    private void auditFoodSources(StringBuilder errors) {
        if (mealSource.isEmpty() && grocerySource.isEmpty()) {
            errors.append(MISSING_FOOD_SOURCES_VARIABLE);
        }
    }

    private void auditAltMealOptions(StringBuilder errors) {
        if (altMealOptions == null) {
            errors.append(MISSING_ALT_MEAL_OPTIONS_VARIABLE);
        }
    }

    private void auditAltGroceryOptions(StringBuilder errors) {
        if (altGroceryOptions == null) {
            errors.append(MISSING_ALT_GROCERY_OPTIONS_VARIABLE);
        }
    }

    private void auditPickupManagers(StringBuilder errors) {
        if (pickupManagers == null) {
            errors.append(MISSING_PICKUP_MANAGERS_VARIABLE);
        }
    }
}