/*
 * Copyright (c) 2021-2024. helpberkeley.org
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

package org.helpberkeley.memberdata.v300;

import org.helpberkeley.memberdata.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControlBlockV300 extends ControlBlock {

    public static final String MORE_START_TIMES_THAN_DRIVERS =
            "More start times defined in control block than there are drivers.\n";
    public static final String MORE_DRIVERS_THAN_START_TIMES =
            "There are more drivers ({0}) than start times ({1}) defined in the "
                    + Constants.CONTROL_BLOCK_START_TIMES + ".\n";
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
    public static final String TOO_MANY_MESSAGE_FORMAT_VARIABLES = Constants.CONTROL_BLOCK_MESSAGE_FORMAT
            + " is defined more than once in the control block.\n";

    public static final String MISSING_REQUIRED_VARIABLE = "Required {0} control block variable is missing.\n";
    public static final String UNKNOWN_PICKUP_MANAGER =
            Constants.CONTROL_BLOCK_PICKUP_MANAGER + " {0} is not a member. Misspelling?\n";
    public static final String EMPTY_GROCERY_SOURCE =
            "No grocery source specified in " + Constants.CONTROL_BLOCK_FOOD_SOURCES + ".\n";
    public static final String EMPTY_MEAL_SOURCE =
            "No meal source specified in " + Constants.CONTROL_BLOCK_FOOD_SOURCES + ".\n";
    public static final String FOOD_SOURCES_BAD_VALUE = Constants.CONTROL_BLOCK_FOOD_SOURCES
            + " value \"{0}\" at line {1} does not match \"Meal source | Grocery source\".\n";
    public static final String INVALID_NONE_ALT = "\"{0}\" is not valid for control block {1}. "
            + "Leave the value column empty if there are no alternate choices.\n";
    public static final String INVALID_MESSAGE_FORMAT = "\"{0}\" is not valid for "
            + Constants.CONTROL_BLOCK_MESSAGE_FORMAT + "\n";
    public static final String INVALID_FILE_PREFIX = "\"{0}\" is not a supported file prefix";

    private List<String> pickupManagers = null;
    private List<String> altMealOptions = null;
    private List<String> altGroceryOptions = null;
    private final List<String> startTimes = new ArrayList<>();
    private String mealSource = "";
    private String grocerySource = "";
    private String messageFormat = null;

    public ControlBlockV300(List<String> header) {
         super(header);
    }

    public void audit(Map<String, User> users, List<Driver> drivers) {
        StringBuilder errors = new StringBuilder();

        auditMessageFormat(errors);
        auditOpsManager(errors, users);
        auditBackupDrivers(errors, users);
        auditStartTimes(errors, drivers);
        auditFoodSources(errors);
        auditAltMealOptions(errors);
        auditAltGroceryOptions(errors);
        auditPickupManagers(errors, users);

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }
    }

    @Override
    public String getVersion() {
        return Constants.CONTROL_BLOCK_VERSION_300;
    }
    @Override
    public boolean versionIsCompatible(String version) {
        return (version.equals(Constants.CONTROL_BLOCK_VERSION_300)
                || version.equals(Constants.CONTROL_BLOCK_VERSION_301)
                || version.equals(Constants.CONTROL_BLOCK_VERSION_302));
    }

    @Override
    public void processAltMealOptions(String value, long lineNumber) {

        if (altMealOptions != null) {
            throw new MemberDataException(TOO_MANY_ALT_MEAL_OPTIONS_VARIABLES);
        }

        altMealOptions = new ArrayList<>();

        if (! value.isEmpty()) {
            altMealOptions.addAll(processList(value));
        }
    }

    @Override
    public void processAltGroceryOptions(String value, long lineNumber) {
        if (altGroceryOptions != null) {
            throw new MemberDataException(TOO_MANY_ALT_GROCERY_OPTIONS_VARIABLES);
        }
        altGroceryOptions = new ArrayList<>();
        if (! value.isEmpty()) {
            altGroceryOptions.addAll(processList(value));
        }
    }

    @Override
    public void processStartTimes(String value, long lineNumber) {
        if (! startTimes.isEmpty()) {
            throw new MemberDataException(TOO_MANY_START_TIMES_VARIABLES);
        }

        startTimes.addAll(processList(value));
    }

    @Override
    public void processPickupManager(String value, long lineNumber) {
        if (pickupManagers == null) {
            pickupManagers = new ArrayList<>();
        }
        pickupManagers.add(value.trim());
    }

    @Override
    public void processMessageFormat(String value, long lineNumber) {
        if (messageFormat != null) {
            throw new MemberDataException(TOO_MANY_MESSAGE_FORMAT_VARIABLES);
        }

        messageFormat = value;
    }

    //
    // A FoodSources data field should look like "meal source  | grocery source"
    //
    @Override
    public void processFoodSources(String value, long lineNumber) {

         if (! (mealSource.isEmpty() && grocerySource.isEmpty())) {
             throw new MemberDataException(TOO_MANY_FOOD_SOURCES_VARIABLES);

         }

        String[] fields = value.split("\\" + INTRA_FIELD_SEPARATOR, -42);

        if (fields.length != 2) {
            throw new MemberDataException(MessageFormat.format(FOOD_SOURCES_BAD_VALUE, value, lineNumber));
        }

        mealSource = fields[0].trim();
        grocerySource = fields[1].trim();

        if (mealSource.isEmpty()) {
            warnings.append(EMPTY_MEAL_SOURCE);
        }

        if (grocerySource.isEmpty()) {
            warnings.append(EMPTY_GROCERY_SOURCE);
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

    public List<String> getPickupManagers() {
        return pickupManagers;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    private List<String> processList(String value) {
        // FIX THIS, DS: is there auditing to do here?
         return Arrays.asList(value.split("\\s*,\\s*"));
    }

    private void auditStartTimes(StringBuilder errors, List<Driver> drivers) {

        if (startTimes.isEmpty()) {
            errors.append(MessageFormat.format(MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_START_TIMES));
            return;
        }

        // Error if there are more drivers than start times
        if (drivers.size() > startTimes.size()) {
            errors.append(MessageFormat.format(MORE_DRIVERS_THAN_START_TIMES,
                    drivers.size(), startTimes.size()));
        }

        // Warning if there are more start times than drivers
        if (startTimes.size() > drivers.size()) {
            warnings.append(MORE_START_TIMES_THAN_DRIVERS);
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
            errors.append(MessageFormat.format(MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_FOOD_SOURCES));
        }
    }

    private void auditAltMealOptions(StringBuilder errors) {
        if (altMealOptions == null) {
            errors.append(MessageFormat.format(MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS));
        } else if ((altMealOptions.size() == 1) &&
                altMealOptions.get(0).equalsIgnoreCase(Constants.ALT_TYPE_NONE)) {
            errors.append(MessageFormat.format(INVALID_NONE_ALT,
                    Constants.ALT_TYPE_NONE, Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS));
        }
    }

    private void auditAltGroceryOptions(StringBuilder errors) {
        if (altGroceryOptions == null) {
            errors.append(MessageFormat.format(MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS));
        } else if ((altGroceryOptions.size() == 1) &&
            altGroceryOptions.get(0).equalsIgnoreCase(Constants.ALT_TYPE_NONE)) {
            errors.append(MessageFormat.format(INVALID_NONE_ALT,
                    Constants.ALT_TYPE_NONE, Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS));
        }
    }

    private void auditPickupManagers(StringBuilder errors, Map<String, User> users) {
        if (pickupManagers == null) {
            errors.append(MessageFormat.format(MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_PICKUP_MANAGER));
            return;
        }

        for (String pickupManager : pickupManagers) {

            User user = users.get(pickupManager);

            if (user == null) {
                errors.append(MessageFormat.format(UNKNOWN_PICKUP_MANAGER, pickupManager));
            }
            // FIX THIS, DS: add group audit warning?
        }
    }

    private void auditMessageFormat(StringBuilder errors) {
        if (messageFormat == null) {
            errors.append(MessageFormat.format(
                    MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_MESSAGE_FORMAT));
        } else if (! MessageSpecFormat.validFormat(messageFormat)) {
            errors.append(MessageFormat.format(INVALID_MESSAGE_FORMAT, messageFormat));
        }
    }
}
