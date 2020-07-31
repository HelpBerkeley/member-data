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

import java.util.*;

class ControlBlock {

    static final String INTRA_FIELD_SEPARATOR = "|";
    static final String ERROR_MISSING_OPS_MANAGER =
            "Control block missing a " + Constants.CONTROL_BLOCK_OPS_MANAGER + " entry.\n";

    private final List<OpsManager> opsManagers = new ArrayList<>();
    // FIX THIS, DS: remove and just use map
    private final List<SplitRestaurant> splitRestaurants = new ArrayList<>();
    private final Map<String, SplitRestaurant> splitRestaurantMap = new HashMap<>();
    private final List<String> backupDrivers = new ArrayList<>();
    private final Map<String, ControlBlockRestaurant> controlBlockRestaurants = new HashMap<>();

    private final StringBuilder warnings = new StringBuilder();

    ControlBlock() {

    }

    void audit(List<String> allRestaurants, List<String> splitRestaurants) {
        StringBuilder errors = new StringBuilder();

        auditOpsManager(errors);
        auditEmojis(allRestaurants);
        auditSplitRestaurants(splitRestaurants, errors);
        auditBackupDrivers();

        // FIX THIS, DS: add warning for no backup driver

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }
    }

    private void auditOpsManager(StringBuilder errors) {

        if (opsManagers.isEmpty()) {
            errors.append(ERROR_MISSING_OPS_MANAGER);
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

    private void auditEmojis(List<String> splitRestaurants) {
        for (String restaurant : splitRestaurants) {
            if (! controlBlockRestaurants.containsKey(restaurant)) {
                warnings.append("Restaurant ").append(restaurant).append(" does not have a ")
                        .append(Constants.CONTROL_BLOCK_RESTAURANT).append(" entry in the control block.\n");
            }
        }
    }

    private void auditBackupDrivers() {
        if (backupDrivers.isEmpty()) {
            warnings.append("No " + Constants.CONTROL_BLOCK_BACKUP_DRIVER + " set in the control block.\n");
        }
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

    String getEmoji(String restaurantName) {

        ControlBlockRestaurant controlBlockRestaurant = controlBlockRestaurants.get(restaurantName);
        String value;

        if (controlBlockRestaurant != null) {
            value = controlBlockRestaurant.getEmoji();
        } else {
            warnings.append("Restaurant ").append(restaurantName).append(" does not have a ")
                    .append(Constants.CONTROL_BLOCK_RESTAURANT).append(" entry in the control block");
            value = "";
        }

        return value;
    }

    void clear()
    {
        opsManagers.clear();
        splitRestaurants.clear();
        splitRestaurantMap.clear();
        backupDrivers.clear();
    }

    void processRow(WorkflowBean bean, long lineNumber) {

        String variable = bean.getControlBlockKey().replaceAll(" ", "");
        String value = bean.getControlBlockValue().replaceAll("\\s*\\|\\s*", "|");

        switch (variable) {
            case Constants.CONTROL_BLOCK_OPS_MANAGER:
                processOpsManager(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_SPLIT_RESTAURANT:
                processSplitRestaurant(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_BACKUP_DRIVER:
                processBackupDriver(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_RESTAURANT:
                processRestaurant(value, lineNumber);
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

    //
    // An OpManager data field should look like "userName | phone number"
    //
    private void processOpsManager(final String value, long lineNumber) {

        String[] fields = value.split("\\" + INTRA_FIELD_SEPARATOR, -42);

        if (fields.length != 2) {
            throw new MemberDataException("OpManager value \"" + value
                    + "\" at line " + lineNumber + " does not match \"username | phone\"");
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

    //
    // A ControlBlockRestaurant data field should look like "restaurant name | emoji"
    //
    private void processRestaurant(final String value, long lineNumber) {
        String[] fields = value.split("\\" + INTRA_FIELD_SEPARATOR, -42);

        if (fields.length != 2) {
            throw new MemberDataException(Constants.CONTROL_BLOCK_RESTAURANT + " value \"" + value
                    + "\" at line " + lineNumber
                    + " does not match \"name | emoji\"");
        }

        String restaurantName = fields[0].trim();
        String emoji = fields[1].trim();

        StringBuilder errors = new StringBuilder();

        if (restaurantName.isEmpty()) {
            errors.append("Empty Restaurant name at line ").append(lineNumber).append(".\n");
        }

        if (emoji.isEmpty()) {
            errors.append("Empty Restaurant emoji at line ").append(lineNumber).append(".\n");
        }

        // FIX THIS, DS: emoji audit?

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }

        controlBlockRestaurants.put(restaurantName, new ControlBlockRestaurant(restaurantName, emoji));
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

    static class ControlBlockRestaurant {
        private final String name;
        private final String emoji;

        ControlBlockRestaurant(String name, String emoji) {
            this.name = name;
            this.emoji = emoji;
        }

        public String getName() {
            return name;
        }

        public String getEmoji() {
            return emoji;
        }

        @Override
        public boolean equals(Object obj) {
            return ((obj != null)
                    && (obj instanceof ControlBlockRestaurant)
                    && (name.equals(((ControlBlockRestaurant)obj).name))
                    && (emoji.equals(((ControlBlockRestaurant)obj).emoji)));
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, emoji);
        }

        @Override
        public String toString() {
            return (name + ", " + emoji);
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
