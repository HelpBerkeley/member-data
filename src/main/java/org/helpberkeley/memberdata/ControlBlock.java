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

import java.util.ArrayList;
import java.util.List;

class ControlBlock {

    static final String INTRA_FIELD_SEPARATOR = "|";

    private List<OpsManager> opsManagers = new ArrayList<>();
    private List<SplitRestaurant> splitRestaurants = new ArrayList<>();
    private List<String> backupDrivers = new ArrayList<>();

    private StringBuilder warnings = new StringBuilder();

    ControlBlock() {

    }

    List<OpsManager> getOpsManagers() {
        return opsManagers;
    }

    List<SplitRestaurant> getSplitRestaurants() {
        return splitRestaurants;
    }

    List<String> getBackupDrivers() {
        return backupDrivers;
    }

    void clear()
    {
        opsManagers.clear();
        splitRestaurants.clear();
        backupDrivers.clear();
    }

    void processRow(WorkflowBean bean, long lineNumber) {

        String variable = bean.getControlBlockKey().replaceAll("\\s*\\|\\s*", "|");
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

        if (phone.isEmpty()) {
            errors.append("Empty OpsManager phone number at line ").append(lineNumber).append(".\n");
        }

        // FIX THIS, DS: add auditing and transformation for phone number

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }

        opsManagers.add(new OpsManager(userName, phone));
    }

    //
    // An SplitRestaurant data field should look like "restaurant name | cleanup driver username"
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

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }

        splitRestaurants.add(new SplitRestaurant(restaurantName, cleanupDriver));
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
}
