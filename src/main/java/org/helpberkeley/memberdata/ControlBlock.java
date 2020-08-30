/*
 * Copyright (c) 2020. helpberkeley.org
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
import java.util.*;

class ControlBlock {

    static final String INTRA_FIELD_SEPARATOR = "|";
    static final String ERROR_MISSING_OPS_MANAGER =
            "Control block missing a " + Constants.CONTROL_BLOCK_OPS_MANAGER + " entry.\n";

    static final String UNKNOWN_BACKUP_DRIVER =
            Constants.CONTROL_BLOCK_BACKUP_DRIVER + " {0} is not a member. Misspelling?\n";
    static final String BACKUP_IS_NOT_A_DRIVER =
            Constants.CONTROL_BLOCK_BACKUP_DRIVER + " {0} is not a driver.\n";
    static final String UNKNOWN_OPS_MANAGER =
            Constants.CONTROL_BLOCK_OPS_MANAGER + " {0} is not a member. Misspelling?\n";
    static final String OPS_MANAGER_PHONE_MISMATCH =
            Constants.CONTROL_BLOCK_OPS_MANAGER + " {0} phone {1} does not match the member data";

    private int version = Constants.CONTROL_BLOCK_VERSION_UNKNOWN;
    private final List<OpsManager> opsManagers = new ArrayList<>();
    // FIX THIS, DS: remove and just use map
    private final List<SplitRestaurant> splitRestaurants = new ArrayList<>();
    private final Map<String, SplitRestaurant> splitRestaurantMap = new HashMap<>();
    private final List<String> backupDrivers = new ArrayList<>();

    private final StringBuilder warnings = new StringBuilder();

    ControlBlock() {

    }

    void audit(Map<String, User> users, List<String> splitRestaurants) {
        StringBuilder errors = new StringBuilder();

        auditVersion(errors);
        auditOpsManager(errors, users);
        auditSplitRestaurants(splitRestaurants, errors);
        auditBackupDrivers(errors, users);

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }
    }

    private void auditVersion(StringBuilder errors) {
        if (version > Constants.CONTROL_BLOCK_CURRENT_VERSION) {
            errors.append("Control block version " + version + " is not supported.\n");
        }
    }

    private void auditOpsManager(StringBuilder errors, Map<String, User> users) {
        if (opsManagers.isEmpty()) {
            errors.append(ERROR_MISSING_OPS_MANAGER);
        }

        for (OpsManager opsManager : opsManagers) {
            User user = users.get(opsManager.userName);

            if (user == null) {
                errors.append(MessageFormat.format(UNKNOWN_OPS_MANAGER, opsManager.userName));
            } else {
                String phone = opsManager.phone.replaceAll("\\D", "");
                boolean match = phone.equals(user.getPhoneNumber().replace("\\D", ""));

                if (! match) {
                    match = phone.equals(user.getAltPhoneNumber().replace("\\D", ""));
                }

                if (! match) {
                    warnings.append(MessageFormat.format(OPS_MANAGER_PHONE_MISMATCH,
                            opsManager.userName, opsManager.phone));
                }
            }
        }
    }

    private void auditSplitRestaurants(List<String> splitRestaurants, StringBuilder errors) {

        for (String restaurant : splitRestaurants) {
            if (!splitRestaurantMap.containsKey(restaurant)) {
                errors.append("Control block does not contain a ")
                        .append(Constants.CONTROL_BLOCK_SPLIT_RESTAURANT)
                        .append(" entry for ")
                        .append(restaurant)
                        .append("\n");
            }
        }
    }

    private void auditBackupDrivers(StringBuilder errors, Map<String, User> users) {
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

    int getVersion() {
        return version;
    }

    List<OpsManager> getOpsManagers() {
        return opsManagers;
    }

    OpsManager getFirstOpsManager() {
        if (opsManagers.size() == 0) {
            throw new MemberDataException("No OpsManager found");
        }

        return opsManagers.get(0);
    }

    List<SplitRestaurant> getSplitRestaurants() {
        return splitRestaurants;
    }

    SplitRestaurant getSplitRestaurant(String restaurantName) {

        SplitRestaurant splitRestaurant = splitRestaurantMap.get(restaurantName);

        if (splitRestaurant == null) {
            throw new MemberDataException
                    ("Split restaurant \"" + restaurantName + "\" not found in the control block");
        }

        return splitRestaurant;
    }

    List<String> getBackupDrivers() {
        return backupDrivers;
    }

    void clear()
    {
        version = Constants.CONTROL_BLOCK_VERSION_UNKNOWN;
        opsManagers.clear();
        splitRestaurants.clear();
        splitRestaurantMap.clear();
        backupDrivers.clear();
    }

    void processRow(WorkflowBean bean, long lineNumber) {

        String variable = bean.getControlBlockKey().replaceAll(" ", "");
        String value = bean.getControlBlockValue().replaceAll("\\s*\\|\\s*", "|");

        switch (variable) {
            case Constants.CONTROL_BLOCK_VERSION:
                processVersion(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_OPS_MANAGER:
                processOpsManager(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_SPLIT_RESTAURANT:
                processSplitRestaurant(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_BACKUP_DRIVER:
                processBackupDriver(value, lineNumber);
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
                processVersion(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_OPS_MANAGER:
            case Constants.CONTROL_BLOCK_SPLIT_RESTAURANT:
            case Constants.CONTROL_BLOCK_BACKUP_DRIVER:
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

    String getWarnings() {
        return warnings.toString();
    }

    private void processVersion(String value, long lineNumber) {
        try {
            version = Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new MemberDataException("Version \"" + value + "\" at line " + lineNumber
                    + " is not valid version number.\n");
        }
    }

    //
    // An OpManager data field should look like "userName | phone number"
    //
    private void processOpsManager(final String value, long lineNumber) {

        String[] fields = value.split("\\" + INTRA_FIELD_SEPARATOR, -42);

        if (fields.length != 2) {
            throw new MemberDataException("OpsManager value \"" + value
                    + "\" at line " + lineNumber + " does not match \"username | phone\".\n");
        }

        String userName = fields[0].trim();
        String phone = fields[1].trim();

        StringBuilder errors = new StringBuilder();

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

        if (phone.isEmpty()) {
            errors.append("Empty OpsManager phone number at line ").append(lineNumber).append(".\n");
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

        splitRestaurants.add(new SplitRestaurant(restaurantName, cleanupDriver));
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

    static class SplitRestaurant {
        private final String name;
        private final String cleanupDriverUserName;

        SplitRestaurant(String name, String cleanupDriverUserName) {
            this.name = name;
            this.cleanupDriverUserName = cleanupDriverUserName;
        }

        public String getName() {
            return name;
        }

        public String getCleanupDriverUserName() {
            return cleanupDriverUserName;
        }

        @Override
        public boolean equals(Object obj) {
            return ((obj != null)
                    && (obj instanceof SplitRestaurant)
                    && (name.equals(((SplitRestaurant)obj).name))
                    && (cleanupDriverUserName.equals(((SplitRestaurant)obj).cleanupDriverUserName)));
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
        private final String phone;

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

            return ((obj != null)
                    && (obj instanceof OpsManager)
                    && (userName.equals(((OpsManager)obj).userName)))
                    && (phone.equals(((OpsManager)obj).phone));
        }

        @Override
        public int hashCode() {
            return Objects.hash(userName, phone);
        }
    }
}
