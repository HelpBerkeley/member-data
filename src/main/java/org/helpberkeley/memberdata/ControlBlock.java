/*
 * Copyright (c) 2020-2024. helpberkeley.org
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

import com.opencsv.exceptions.CsvException;
import org.helpberkeley.memberdata.v200.ControlBlockV200;
import org.helpberkeley.memberdata.v200.ControlBlockV202;
import org.helpberkeley.memberdata.v300.ControlBlockV300;
import org.helpberkeley.memberdata.v300.ControlBlockV301;
import org.helpberkeley.memberdata.v300.ControlBlockV302;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;

public abstract class ControlBlock {

    public static final String INTRA_FIELD_SEPARATOR = "|";
    static final String ERROR_MISSING_OPS_MANAGER =
            "Control block missing a " + Constants.CONTROL_BLOCK_OPS_MANAGER_USERNAME_ONLY + " entry.\n";

    public static final String UNSUPPORTED_VERSION_GENERIC = "Control block version {0} is not supported.\n";
    public static final String UNSUPPORTED_VERSION_FOR = "Control block version {0} is not supported for {1}.\n";
    static final String BAD_HEADER_ROW = "Line 1, column names missing.\n";
    static final String MISSING_OR_INVALID_HEADER_ROW
            = "Line 1, header row missing or has a duplicate column name ({0})\n";
    static final String ERROR_WRONG_NUMBER_OF_VERSION_KEYS =
            "Control block Version key must appear once and only once";
    static final String ERROR_WRONG_NUMBER_OF_VERSION_VALUES =
            "Too many non-empty columns. Control block Version value must appear once and only once";
    static final String ERROR_UNKNOWN_DIRECTIVE = "Unexpected control block directive \"{0}\" in "
            + Constants.WORKFLOW_NAME_COLUMN + " column at line {1}.\n";

    static final String UNKNOWN_BACKUP_DRIVER =
            Constants.CONTROL_BLOCK_BACKUP_DRIVER + " {0} is not a member. Misspelling?\n";
    static final String BACKUP_IS_NOT_A_DRIVER =
            Constants.CONTROL_BLOCK_BACKUP_DRIVER + " {0} is not a driver.\n";
    static final String UNKNOWN_OPS_MANAGER =
            Constants.CONTROL_BLOCK_OPS_MANAGER_USERNAME_ONLY + " {0} is not a member. Misspelling?\n";
    static final String OPS_MANAGER_PHONE_MISMATCH =
            Constants.CONTROL_BLOCK_OPS_MANAGER_USERNAME_AND_PHONE + " {0} phone {1} does not match the member data\n";
    static final String OPS_MANAGER_WRONG_FORMAT =
            " OpsManager value {0} at line {1} does not match OpsManager(UserName) or "
            + Constants.CONTROL_BLOCK_OPS_MANAGER_USERNAME_AND_PHONE + ".\n";
    static final String UNKNOWN_SPLIT_RESTAURANT =
            Constants.CONTROL_BLOCK_SPLIT_RESTAURANT + " contains unknown restaurant {0}. Misspelling?\n";
    public static final String UNKNOWN_CLEANUP_DRIVER =
            Constants.CONTROL_BLOCK_SPLIT_RESTAURANT + " {0} for {1} is not a member. Misspelling?\n";
    public static final String WRONG_CLEANUP_DRIVER =
            Constants.CONTROL_BLOCK_SPLIT_RESTAURANT + " {0} is not going to {1}.\n";
    public static final String MISSING_SPLIT_RESTAURANT =
            "Control block does not contain a "
            + Constants.CONTROL_BLOCK_SPLIT_RESTAURANT + " entry for {0}\n";

    public boolean isDisableLateArrivalAudit() {
        return disableLateArrivalAudit;
    }

    public static final String UNSUPPORTED =
            "{0} (line {1}) is not supported in control block version {2}\n";

    private final Set<String> columnNames;
    private final List<OpsManager> opsManagers = new ArrayList<>();
    private final Map<String, SplitRestaurant> splitRestaurantMap = new HashMap<>();
    private final List<String> backupDrivers = new ArrayList<>();
    private boolean disableLateArrivalAudit = false;
    private boolean disableSplitRestaurantAudits = false;
    private boolean disableRestaurantsAudit = false;

    protected final StringBuilder warnings = new StringBuilder();

    protected ControlBlock(List<String> header) {

        columnNames = new HashSet<>(header);
        if (columnNames.size() != header.size()) {
            throw new MemberDataException(MessageFormat.format(MISSING_OR_INVALID_HEADER_ROW, header.toString()));
        }
        auditColumnNames();
    }

    protected void unsupported(long lineNumber, String feature) {
        warnings.append(MessageFormat.format(UNSUPPORTED, feature, lineNumber, getVersion()));
    }

    private void auditColumnNames() {
        if ((! columnNames.contains(Constants.WORKFLOW_CONSUMER_COLUMN))
                && (! columnNames.contains(Constants.WORKFLOW_DRIVER_COLUMN))
                && (! columnNames.contains(Constants.WORKFLOW_USER_NAME_COLUMN))
                && (! columnNames.contains(Constants.WORKFLOW_NAME_COLUMN))
                && (! columnNames.contains(Constants.WORKFLOW_CITY_COLUMN))) {
            throw new MemberDataException(BAD_HEADER_ROW);
        }
    }

    public static ControlBlock create(String csvData) throws IOException, CsvException {

//        // Normalize lines
//        String normalized = csvData.replaceAll("\\r\\n?", "\n");
//        // Break into lines
//        String[] lines = normalized.split("\n");
//
//        String header = lines[0];

        CSVListReader csvReader = new CSVListReader(new StringReader(csvData));
        List<List<String>> lines = csvReader.readAllToList();
        List<String> header = lines.get(0);
        csvReader.close();


//        if (! header.contains(Constants.CSV_SEPARATOR)) {
//            throw new MemberDataException(BAD_HEADER_ROW);
//        }

        String version = new VersionParser(lines).version();

        switch (version) {
            case Constants.CONTROL_BLOCK_VERSION_UNKNOWN:
                return new ControlBlockV0(header);
            case Constants.CONTROL_BLOCK_VERSION_1:
                return new ControlBlockV1(header);
            case Constants.CONTROL_BLOCK_VERSION_200:
                return new ControlBlockV200(header);
            case Constants.CONTROL_BLOCK_VERSION_202:
                return new ControlBlockV202(header);
            case Constants.CONTROL_BLOCK_VERSION_300:
                return new ControlBlockV300(header);
            case Constants.CONTROL_BLOCK_VERSION_301:
                return new ControlBlockV301(header);
            case Constants.CONTROL_BLOCK_VERSION_302:
                return new ControlBlockV302(header);
            default:
                throw new MemberDataException(MessageFormat.format(UNSUPPORTED_VERSION_GENERIC, version));
        }
    }

    boolean lateArrivalAuditDisabled() {
        return disableLateArrivalAudit;
    }

    public boolean splitRestaurantAuditsDisabled() {
        return disableSplitRestaurantAudits;
    }

    public boolean restaurantsAuditDisabled() {
        return disableRestaurantsAudit;
    }

    protected void auditOpsManager(StringBuilder errors, Map<String, User> users) {
        if (opsManagers.isEmpty()) {
            errors.append(ERROR_MISSING_OPS_MANAGER);
        }

        for (OpsManager opsManager : opsManagers) {
            User user = users.get(opsManager.userName);

            if (user == null) {
                errors.append(MessageFormat.format(UNKNOWN_OPS_MANAGER, opsManager.userName));
            } else if (opsManager.phone.isEmpty()) {
                opsManager.phone = user.getPhoneNumber();
            } else {
                String phone = opsManager.phone.replaceAll("\\D", "");

                boolean match = phone.equals(user.getPhoneNumber().replaceAll("\\D", ""));

                if (! match) {
                    match = phone.equals(user.getAltPhoneNumber().replaceAll("\\D", ""));
                }

                if (! match) {
                    warnings.append(MessageFormat.format(OPS_MANAGER_PHONE_MISMATCH,
                            opsManager.userName, opsManager.phone));
                }
            }
        }
    }

    //
    // Split Restaurant audits:
    //  - valid restaurant name
    //  - all split restaurants have control block entries
    //  - cleanup driver is a valid user name
    //  - cleanup driver is picking up at that restaurant
    //
    protected void auditSplitRestaurants(StringBuilder errors, Map<String, User> users,
                   Map<String, Restaurant> allRestaurants, List<Restaurant> splitRestaurants) {

        if (disableSplitRestaurantAudits) {
            return;
        }

        for (SplitRestaurant splitRestaurant : splitRestaurantMap.values()) {
            if (!allRestaurants.containsKey(splitRestaurant.name)) {
                errors.append(MessageFormat.format(UNKNOWN_SPLIT_RESTAURANT, splitRestaurant.name));
            }
        }

        for (Restaurant restaurant : splitRestaurants) {
            String name = restaurant.getName();

            SplitRestaurant splitRestaurant = splitRestaurantMap.get(name);
            if (splitRestaurant == null) {
                errors.append(MessageFormat.format(MISSING_SPLIT_RESTAURANT, name));
                continue;
            }

            if (! users.containsKey(splitRestaurant.cleanupDriverUserName)) {
                errors.append(MessageFormat.format(UNKNOWN_CLEANUP_DRIVER,
                        splitRestaurant.cleanupDriverUserName, name));
                continue;
            }

            Map<String, Driver> drivers = restaurant.getDrivers();
            assert drivers != null : name;
            if (! drivers.containsKey(splitRestaurant.cleanupDriverUserName)) {
                errors.append(MessageFormat.format(
                        WRONG_CLEANUP_DRIVER, splitRestaurant.cleanupDriverUserName, name));
            }
        }
    }

    protected void auditBackupDrivers(StringBuilder errors, Map<String, User> users) {
        if (backupDrivers.isEmpty()) {
            warnings.append("No " + Constants.CONTROL_BLOCK_BACKUP_DRIVER + " set in the control block.\n");
        }

        for (String backupDriver : backupDrivers) {
            User user = users.get(backupDriver);

            if (user == null) {
                errors.append(MessageFormat.format(UNKNOWN_BACKUP_DRIVER, backupDriver));
            } else if (! user.isDriver()) {
                warnings.append(MessageFormat.format(BACKUP_IS_NOT_A_DRIVER, backupDriver));
            }
        }
    }

    public abstract String getVersion();

    public abstract boolean versionIsCompatible(String version);

    List<OpsManager> getOpsManagers() {
        return opsManagers;
    }

    public OpsManager getFirstOpsManager() {
        if (opsManagers.isEmpty()) {
            throw new MemberDataException("No OpsManager found");
        }

        return opsManagers.get(0);
    }

    List<SplitRestaurant> getSplitRestaurants() {
        return new ArrayList<>(splitRestaurantMap.values());
    }

    public SplitRestaurant getSplitRestaurant(String restaurantName) {

        SplitRestaurant splitRestaurant = splitRestaurantMap.get(restaurantName);

        if (splitRestaurant == null) {
            throw new MemberDataException
                    ("Split restaurant \"" + restaurantName + "\" not found in the control block");
        }

        return splitRestaurant;
    }

    public List<String> getBackupDrivers() {
        return backupDrivers;
    }

    void processRow(WorkflowBean bean, long lineNumber) {

        String variable = bean.getControlBlockKey().replaceAll(" ", "");
        String value = bean.getControlBlockValue().replaceAll("\\s*\\|\\s*", "|");

        switch (variable) {
            case Constants.CONTROL_BLOCK_VERSION:
                processVersion(value);
                break;
            case Constants.CONTROL_BLOCK_OPS_MANAGER_USERNAME_ONLY:
            case Constants.CONTROL_BLOCK_OPS_MANAGER_USERNAME_AND_PHONE:
                processOpsManager(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_SPLIT_RESTAURANT:
                processSplitRestaurant(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_BACKUP_DRIVER:
                processBackupDriver(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT:
            case Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT:
            case Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS:
                processAuditControl(variable, value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS:
                processAltMealOptions(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS:
                processAltGroceryOptions(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_START_TIMES:
                processStartTimes(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_PICKUP_MANAGER:
                processPickupManager(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_FOOD_SOURCES:
                processFoodSources(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_MESSAGE_FORMAT:
                processMessageFormat(value, lineNumber);
                break;
            default:
                warnings.append("Unknown key \"")
                        .append(variable)
                        .append("\" in the ")
                        .append(Constants.WORKFLOW_USER_NAME_COLUMN)
                        .append(" column at line ")
                        .append(lineNumber)
                        .append(".\n");
        }
    }

    // FIX THIS, DS: refactor the two beans
    void processRow(RestaurantBean bean, long lineNumber) {
        String variable = bean.getControlBlockKey().replaceAll(" ", "");
        String value = bean.getControlBlockValue().replaceAll("\\s*\\|\\s*", "|");

        switch (variable) {
            case Constants.CONTROL_BLOCK_VERSION:
                processVersion(value);
                break;
            case Constants.CONTROL_BLOCK_OPS_MANAGER_USERNAME_AND_PHONE:
            case Constants.CONTROL_BLOCK_OPS_MANAGER_USERNAME_ONLY:
            case Constants.CONTROL_BLOCK_SPLIT_RESTAURANT:
            case Constants.CONTROL_BLOCK_BACKUP_DRIVER:
            case Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT:
                // Skip.  Not relevant to restaurant template
                break;
            default:
                warnings.append("Unknown key \"")
                        .append(variable)
                        .append("\" in the ")
                        .append(Constants.WORKFLOW_USER_NAME_COLUMN)
                        .append(" column at line ")
                        .append(lineNumber)
                        .append(".\n");
        }
    }

    public String getWarnings() {
        return warnings.toString();
    }

    private void processVersion(String value) {
        if (! versionIsCompatible(value)) {
            throw new MemberDataException("Control block version mismatch: " + value + " and " + getVersion());
        }
    }

    private void processAuditControl(String variable, String value, long lineNumber) {

        boolean disableAudit;

        if (value.equalsIgnoreCase("enable")) {
            disableAudit = false;
        } else if (value.equalsIgnoreCase("disable")) {
            disableAudit = true;
        } else {
            throw new MemberDataException("Invalid setting \"" + value + "\" for " + variable
                    + " at line " + lineNumber + ". Must be Enable or Disable.\n");
        }

        switch (variable) {
            case Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT:
                disableLateArrivalAudit = disableAudit;
                break;
            case Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS:
                disableSplitRestaurantAudits = disableAudit;
                break;
            default:
                assert variable.equals(Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT) : variable;
                disableRestaurantsAudit = disableAudit;
                break;
        }
    }

    //
    // An OpManager data field should look like "username" or "userName | phone number"
    //
    private void processOpsManager(final String value, long lineNumber) {

        String[] fields = value.split("\\" + INTRA_FIELD_SEPARATOR, -42);

        String userName = fields[0].trim();
        String phone = "";

        StringBuilder errors = new StringBuilder();

        if (fields.length == 2) {
            phone = fields[1].trim();
            if (phone.isEmpty()) {
                errors.append(MessageFormat.format(OPS_MANAGER_WRONG_FORMAT, value, lineNumber));
            }
        } else if (fields.length > 2) {
            errors.append(MessageFormat.format(OPS_MANAGER_WRONG_FORMAT, value, lineNumber));
        }

        if (! opsManagers.isEmpty()) {
            errors.append("Line ").append(lineNumber).append(", multiple OpsManager entries not yet supported.\n");
        }

        if (userName.isEmpty()) {
            errors.append("Empty OpsManager user name at line ").append(lineNumber).append(".\n");
        }
        if (userName.startsWith("@")) {
            errors.append("OpsManager user name \"")
                    .append(userName).append("\" at line ").append(lineNumber)
                    .append(" cannot start with @\n");
        }
        if (userName.indexOf(' ') != -1) {
            errors.append("OpsManager user name \"")
                    .append(userName).append("\" at line ").append(lineNumber)
                    .append(" cannot contain spaces.\n");
        }

        if (userName.contains(Constants.CONTROL_BLOCK_VALUE_DEFAULT_PREFIX)) {
            errors.append("Set OpsManager user name \"")
                    .append(userName).append("\" at line ").append(lineNumber)
                    .append(" to a valid OpsManager user name.\n");
        }

        if (phone.contains(Constants.CONTROL_BLOCK_VALUE_DEFAULT_PREFIX)) {
            errors.append("Set OpsManager phone \"")
                    .append(phone).append("\" at line ").append(lineNumber)
                    .append(" to a valid phone number.\n");
        }

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }

        opsManagers.add(new OpsManager(userName, phone));
    }

    //
    // A SplitRestaurant data field should look like "restaurant name | cleanup driver username"
    //
    private void processSplitRestaurant(final String value, long lineNumber) {
        String[] fields = value.split("\\" + INTRA_FIELD_SEPARATOR, -42);

        if (fields.length != 2) {
            throw new MemberDataException(Constants.CONTROL_BLOCK_SPLIT_RESTAURANT + " value \"" + value
                    + "\" at line " + lineNumber
                    + " does not match \"restaurant name | cleanup driver user name\"");
        }

        String restaurantName = fields[0].trim();
        String cleanupDriver = fields[1].trim();

        StringBuilder errors = new StringBuilder();

        if (restaurantName.isEmpty()) {
            errors.append("Empty SplitRestaurant restaurant name at line ").append(lineNumber).append(".\n");
        }

        if (restaurantName.contains(Constants.CONTROL_BLOCK_VALUE_DEFAULT_PREFIX)) {
            errors.append("Set SplitRestaurant name \"")
                    .append(restaurantName).append("\" at line ").append(lineNumber)
                    .append(" to a valid restaurant name.\n");
        }

        if (cleanupDriver.isEmpty()) {
            errors.append("Empty SplitRestaurant cleanup driver user name at line ").append(lineNumber).append(".\n");
        }

        if (cleanupDriver.startsWith("@")) {
            errors.append("SplitRestaurant cleanup driver user name \"")
                    .append(cleanupDriver).append("\" at line ").append(lineNumber)
                    .append(" cannot start with @\n");
        }

        if (cleanupDriver.indexOf(' ') != -1) {
            errors.append("SplitRestaurant cleanup driver user name \"")
                    .append(cleanupDriver).append("\" at line ").append(lineNumber)
                    .append(" cannot contain spaces.\n");
        }

        if (cleanupDriver.contains(Constants.CONTROL_BLOCK_VALUE_DEFAULT_PREFIX)) {
            errors.append("Set SplitRestaurant cleanup driver user name \"")
                    .append(restaurantName).append("\" at line ").append(lineNumber)
                    .append(" to a valid user name.\n");
        }

        if (splitRestaurantMap.containsKey(restaurantName)) {
            errors.append("Control block contains SplitRestaurant ")
                    .append(restaurantName)
                    .append(" more than once\n");
        }

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }

        splitRestaurantMap.put(restaurantName, new SplitRestaurant(restaurantName, cleanupDriver));
    }

    private void processBackupDriver(final String backupDriver, long lineNumber) {

        if (backupDriver.contains(INTRA_FIELD_SEPARATOR)) {
            throw new MemberDataException(Constants.CONTROL_BLOCK_BACKUP_DRIVER + " value \"" + backupDriver
                    + "\" at line " + lineNumber
                    + " does not match \"backupDriverUserName\"");
        }

        StringBuilder errors = new StringBuilder();

        if (backupDriver.isEmpty()) {
            errors.append("Empty BackupDriver user name at line ").append(lineNumber).append(".\n");
        }

        if (backupDriver.startsWith(Constants.CONTROL_BLOCK_VALUE_DEFAULT_PREFIX)) {
            errors.append("Set BackupDriverUserName \"")
                    .append(backupDriver).append("\" at line ").append(lineNumber)
                    .append(" to a valid user name.\n");
        }

        if (backupDriver.startsWith("@")) {
            errors.append("BackupDriver user name \"")
                    .append(backupDriver).append("\" at line ").append(lineNumber)
                    .append(" cannot start with a @\n");
        }

        if (backupDriver.indexOf(' ') != -1) {
            errors.append("BackupDriver user name \"")
                    .append(backupDriver).append("\" at line ").append(lineNumber)
                    .append(" cannot contain spaces.\n");
        }

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }

        backupDrivers.add(backupDriver);
    }

    public void processAltMealOptions(String value, long lineNumber) {
        unsupported(lineNumber, Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS);
    }

    public void processAltGroceryOptions(String value, long lineNumber) {
        unsupported(lineNumber, Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS);
    }

    public void processStartTimes(String value, long lineNumber) {
        unsupported(lineNumber, Constants.CONTROL_BLOCK_START_TIMES);
    }

    public void processFoodSources(String value, long lineNumber) {
        unsupported(lineNumber, Constants.CONTROL_BLOCK_FOOD_SOURCES);
    }

    public void processPickupManager(String value, long lineNumber) {
        unsupported(lineNumber, Constants.CONTROL_BLOCK_PICKUP_MANAGER);
    }

    public void processMessageFormat(String value, long lineNumber) {
        unsupported(lineNumber, Constants.CONTROL_BLOCK_MESSAGE_FORMAT);
    }

    public static class SplitRestaurant {
        private final String name;
        private final String cleanupDriverUserName;

        SplitRestaurant(String name, String cleanupDriverUserName) {
            this.name = name;
            this.cleanupDriverUserName = cleanupDriverUserName;
        }

        public String getCleanupDriverUserName() {
            return cleanupDriverUserName;
        }

        @Override
        public boolean equals(Object obj) {
            return ((obj instanceof SplitRestaurant)
                    && (name.equals(((SplitRestaurant) obj).name))
                    && (cleanupDriverUserName.equals(((SplitRestaurant) obj).cleanupDriverUserName)));
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, cleanupDriverUserName);
        }

        @Override
        public String toString() {
            return (name + ", " + cleanupDriverUserName);
        }
    }

    public static class OpsManager {
        private final String userName;
        private String phone;

        OpsManager(String userName, String phone) {
            this.userName = userName;
            this.phone = phone;
        }

        public String getUserName() {
            return userName;
        }

        // FIX THIS, DS: add transformation for the phone number
        public String getPhone() {
            return phone;
        }

        @Override
        public String toString() {
            return userName + ", " + phone;
        }

        @Override
        public boolean equals(Object obj) {

            return ((obj instanceof OpsManager)
                    && (userName.equals(((OpsManager) obj).userName)))
                    && (phone.equals(((OpsManager)obj).phone));
        }

        @Override
        public int hashCode() {
            return Objects.hash(userName, phone);
        }
    }

    private static class VersionParser {

        final List<List<String>> lines;

        VersionParser(List<List<String>> lines) {
            this.lines = lines;
        }

        String version() {

            boolean lookingForControlBlock = true;

            int lineNumber = 0;
            for (List<String> line : lines) {

                lineNumber++;

//                if (! line.startsWith("FALSE,FALSE,")) {
//                    continue;
//                }
                if (! (line.get(0).equals("False") && line.get(1).equals("False"))) {
                    continue;
                }

                if (lookingForControlBlock && line.contains(Constants.CONTROL_BLOCK_BEGIN)) {
                    lookingForControlBlock = false;
                    continue;
                }

                if (line.contains(Constants.CONTROL_BLOCK_COMMENT) || line.contains(Constants.CONTROL_BLOCK_FORMULA)) {
                    continue;
                }

                if (line.contains(Constants.CONTROL_BLOCK_END)) {
                    break;
                }

                if (line.contains(Constants.CONTROL_BLOCK_VERSION)) {
                    return parseVersion(line, lineNumber);
                }
            }

            return Constants.CONTROL_BLOCK_VERSION_UNKNOWN;
        }

        private String parseVersion(List<String> line, int lineNumber) {
            // remove whitespace and split into columns
//            String[] columns = line.replaceAll(" ", "").split(",");
            String value = null;
            int versionTags = 0;
            int values = 0;

            assert line.size() > 1 : lineNumber + ": " + line;
            assert line.get(0).equalsIgnoreCase("FALSE") : lineNumber + ": " + line;
            assert line.get(1).equalsIgnoreCase("FALSE") : lineNumber + ": " + line;

            int columnNumber = 0;
            for (String column : line) {

                columnNumber++;

                if ((columnNumber == 1) || (columnNumber == 2) || column.isEmpty()) {
                    continue;
                }

                if (column.equals(Constants.CONTROL_BLOCK_VERSION)) {
                    versionTags++;
                } else {
                    values++;
                    value = column;
                }
            }

            if (versionTags != 1) {
                raiseException(ERROR_WRONG_NUMBER_OF_VERSION_KEYS, line, lineNumber);
            } else if (values != 1) {
                raiseException(ERROR_WRONG_NUMBER_OF_VERSION_VALUES, line, lineNumber);
            }

            return value;
        }

        private void raiseException(String error, List<String> line, int lineNumber) {
            throw new MemberDataException("Line " + lineNumber + ": "
                    + error + ":\n" + line.toString() + "\n");
        }
    }
}