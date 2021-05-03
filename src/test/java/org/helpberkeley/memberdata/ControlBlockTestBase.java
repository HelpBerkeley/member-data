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

import org.junit.Test;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public abstract class ControlBlockTestBase extends TestBase {

    protected final Map<String, User> users;

    abstract String getControlBlockData();
    abstract Map<String, Restaurant> getAllRestaurants();
    abstract String getHeader();
    abstract String getBeginRow();
    abstract String getEndRow();
    abstract String getVersionRow();
    abstract String getEmptyRow();
    abstract String getDirectiveRow(String directive);
    abstract String getKeyValueRow(String key, String value);
    abstract void audit(ControlBlock controlBlock);
    abstract String addVersionSpecificRequiredVariables();

    public ControlBlockTestBase() {
        List<User> userList = new Loader(createApiSimulator()).load();
        users = new Tables(userList).mapByUserName();
    }

    @Test
    public void controlBlockTest() {
        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), getControlBlockData());

        ControlBlock controlBlock = workflowParser.controlBlock();
        assertThat(controlBlock.getOpsManagers()).containsExactly(
                new ControlBlock.OpsManager("JVol", "123-456-7890"));
        assertThat(controlBlock.getSplitRestaurants()).containsExactly(
                new ControlBlock.SplitRestaurant("Jot Mahal", "joebdriver"));
        assertThat(controlBlock.getBackupDrivers()).containsExactly("JVol");
    }

    /** Test that true in a consumer column throws an exception */
    @Test
    public void consumerTrueTest() {

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getEmptyRow().replaceFirst(",,", "TRUE,FALSE,")
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Control block Consumer column does not contain FALSE, at line 4.\n");
    }

    /** Test that true in a driver column throws an exception */
    @Test
    public void driverTrueTest() {

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getEmptyRow().replaceFirst(",,", "FALSE,TRUE,")
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Control block Driver column does not contain FALSE, at line 4.\n");
    }


    @Test
    public void unknownDirectiveTest() {

        String badDirective = "notADirective";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getDirectiveRow(badDirective)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Unexpected control block directive");
        assertThat(thrown).hasMessageContaining(badDirective);
    }

    @Test
    public void unknownVariableTest() {

        String badVariableName = "thisIsABadVariable";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(badVariableName, "")
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).contains(
                "Unknown key \"" + badVariableName + "\" in the User Name column at line 4.");
    }

    @Test
    public void missingOpsManagerValueSeparatorTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "fred 123-456-7890";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("OpsManager value \"" + value + "\" at line 4 does not match");
    }

    @Test
    public void tooManyOpsManagerValueSeparatorTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "|fred|123-456-7890|";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"username | phone\"");
        assertThat(thrown).hasMessageContaining("|fred|123-456-7890|");
    }

    @Test
    public void opsManagerEmptyUserNameTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "|123-456-7890";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty OpsManager user name at line 4.");
    }

    @Test
    public void opsManagerUserNameWithAtSignTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "@fred|123-456-7890";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("OpsManager user name \"@fred\" at line 4 cannot start with @");
    }

    @Test
    public void opsManagerUserNameWithWithSpacesTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "fred e mercury|123-456-7890";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "OpsManager user name \"fred e mercury\" at line 4 cannot contain spaces.");
    }

    /** Test the audit for an ops manager that is not a known user */
    @Test
    public void opsManagerUnknownUserTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "UnknownDudette|123-456-7890";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.UNKNOWN_OPS_MANAGER, "UnknownDudette"));
    }

    @Test
    public void opsManagerEmptyPhoneTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "biff|";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty OpsManager phone number at line 4.");
    }

    @Test
    public void opsManagerMismatchedPhoneTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "JVol|222-222-2222";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + addVersionSpecificRequiredVariables()
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getWarnings()).contains(
                MessageFormat.format(ControlBlock.OPS_MANAGER_PHONE_MISMATCH, "JVol", "222-222-2222"));
    }

    @Test
    public void missingSplitRestaurantValueSeparatorTest() {

        String key = "SplitRestaurant (Name|CleanupDriverUserName)";
        String value = "MickeyDs bobbyjo";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"restaurant name | cleanup driver user name\"");
        assertThat(thrown).hasMessageContaining("\"MickeyDs bobbyjo\"");
    }

    @Test
    public void tooManySplitRestaurantValueSeparatorTest() {

        String key = "SplitRestaurant (Name|CleanupDriverUserName)";
        String value = "|Max's|buzz|";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"restaurant name | cleanup driver user name\"");
        assertThat(thrown).hasMessageContaining("|Max's|buzz|");
    }

    @Test
    public void splitRestaurantRestaurantNameTest() {

        String key = "SplitRestaurant (Name|CleanupDriverUserName)";
        String value = "|buzz";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty SplitRestaurant restaurant name at line 4.");
    }

    @Test
    public void splitRestaurantCleanupDriverUserNameWithAtSignTest() {

        String key = "SplitRestaurant (Name|CleanupDriverUserName)";
        String value = "Bopshop|@fred";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "SplitRestaurant cleanup driver user name \"@fred\" at line 4 cannot start with @");
    }

    @Test
    public void splitRestaurantCleanupDriverUserNameWithWithSpacesTest() {

        String key = "SplitRestaurant (Name|CleanupDriverUserName)";
        String value = "Bopshop|fred e mercury";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "SplitRestaurant cleanup driver user name \"fred e mercury\" at line 4 cannot contain spaces.");
    }

    @Test
    public void splitRestaurantEmptyCleanupDriverUserNameTest() {

        String key = "SplitRestaurant (Name|CleanupDriverUserName)";
        String value = "Bopshop|";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty SplitRestaurant cleanup driver user name at line 4.");
    }

    @Test
    public void backupDriverUserNameWithSeparatorTest() {

        String key = "BackupDriverUserName";
        String value = "bligzhfzzt|";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "BackupDriverUserName value \"bligzhfzzt|\" at line 4 does not match \"backupDriverUserName\"");
    }

    @Test
    public void emptyBackupDriverUserNameTest() {

        String key = "BackupDriverUserName";
        String value = "";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty BackupDriver user name at line 4");
    }

    @Test
    public void backupDriverUserNameWithAtSignTest() {

        String key = "BackupDriverUserName";
        String value = "@roygbv";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("BackupDriver user name \"@roygbv\" at line 4 cannot start with a @");
    }

    @Test
    public void backupDriverUserNameWithSpacesTest() {

        String key = "BackupDriverUserName";
        String value = "billy joe bob boy";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "BackupDriver user name \"billy joe bob boy\" at line 4 cannot contain spaces");
    }

    @Test
    public void auditNoOpsManagerTest() {

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(() -> audit(workflowParser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(ControlBlock.ERROR_MISSING_OPS_MANAGER);
    }


    @Test
    public void opsManagerUserNameDefaultValueTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "ReplaceThisByUserName | 510-555-1212";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(() -> audit(workflowParser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(
                "Set OpsManager user name \"ReplaceThisByUserName\" at line 4 to a valid OpsManager user name.\n");
    }

    @Test
    public void opsManagerPhoneDefaultValueTest() {

        String key = "OpsManager (UserName|Phone)";
        String value = "FredZ | ReplaceThisByPhone#In510-555-1212Format";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(() -> audit(workflowParser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Set OpsManager phone "
                + "\"ReplaceThisByPhone#In510-555-1212Format\" at line 4 to a valid phone number.\n");
    }

    @Test
    public void multipleOpsManagersTest() {

        String key = "OpsManager (UserName|Phone)";
        String value1 = "FredZ | 510-555-1212";
        String value2 = "Fredrika | 123-456-7890";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value1)
                + getKeyValueRow(key, value2)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(() -> audit(workflowParser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Line 5, multiple OpsManager entries not yet supported.\n");
    }

    @Test
    public void splitRestaurantNameDefaultValueTest() {

        String opsKey = "OpsManager (UserName|Phone)";
        String opsValue = "FredZ | 510-555-1212";
        String splitKey = "SplitRestaurant (Name | CleanupDriverUserName)";
        String splitValue = "ReplaceThisBySplitRestaurantName | RalphKramden";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(opsKey, opsValue)
                + getKeyValueRow(splitKey, splitValue)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(() -> audit(workflowParser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Set SplitRestaurant name "
                + "\"ReplaceThisBySplitRestaurantName\" at line 5 to a valid restaurant name.\n");
    }

    @Test
    public void splitRestaurantCleanupDefaultValueTest() {

        String opsKey = "OpsManager (UserName|Phone)";
        String opsValue = "FredZ | 510-555-1212";
        String splitKey = "SplitRestaurant (Name | CleanupDriverUserName)";
        String splitValue = "White Castle| ReplaceThisByCleanupDriverUserName";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(opsKey, opsValue)
                + getKeyValueRow(splitKey, splitValue)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(() -> audit(workflowParser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Set SplitRestaurant cleanup driver user name "
                + "\"White Castle\" at line 5 to a valid user name.\n");
    }

    @Test
    public void noBackupDriversTest() {

        String opsKey = "OpsManager (UserName|Phone)";
        String opsValue = "JVol | 123-456-7890";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(opsKey, opsValue)
                + addVersionSpecificRequiredVariables()
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        ControlBlock controlBlock = workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getWarnings()).contains("No BackupDriverUserName set in the control block.\n");
    }

    // FIX THIS, DS: move to generic testing
//    @Test
//    public void unsupportedVersionTest() {
//        String unsupportedVersion = "42";
//        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
//                + "FALSE,FALSE,,Version,,,,"
//                + unsupportedVersion
//                + ",,,,,,,\n"
//                + CONTROL_BLOCK_END_ROW;
//
//        Throwable thrown = catchThrowable(() -> WorkflowParser.create(
//                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContaining("Control block version " + unsupportedVersion + " is not supported.\n");
//    }
//
    /** Verify audit failure for unknown backup driver */
    @Test
    public void unknownBackupDriverTest() {

        String opsKey = "OpsManager (UserName|Phone)";
        String opsValue = "FredZ | 510-555-1212";
        String backupDriverKey = "BackupDriverUserName";
        String backupDriverValue = "NotAMemberDude";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(opsKey, opsValue)
                + getKeyValueRow(backupDriverKey, backupDriverValue)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(() -> audit(workflowParser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.UNKNOWN_BACKUP_DRIVER, "NotAMemberDude"));
    }

    /** Verify audit warning for backup not being a driver */
    @Test
    public void backupNotADriverTest() {

        String opsKey = "OpsManager (UserName|Phone)";
        String opsValue = "JVol | 123-456-7890";
        String backupDriverKey = "BackupDriverUserName";
        String backupDriverValue = "ZZZ";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(opsKey, opsValue)
                + getKeyValueRow(backupDriverKey, backupDriverValue)
                + addVersionSpecificRequiredVariables()
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        ControlBlock controlBlock = workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getWarnings()).contains(
                MessageFormat.format(ControlBlock.BACKUP_IS_NOT_A_DRIVER, "ZZZ"));
    }

    @Test
    public void lateStartAuditBadValueTest() {

        String key = Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT;
        String value = "TRUE";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);

        Throwable thrown = catchThrowable(() -> audit(workflowParser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Invalid setting");
        assertThat(thrown).hasMessageContaining(Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT);
    }

    @Test
    public void unvisitedResutaurantsAuditBadValueTest() {

        String key = Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT;
        String value = "TRUE";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);

        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Invalid setting");
        assertThat(thrown).hasMessageContaining(Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT);
    }

    @Test
    public void splitRestaurantsAuditBadValueTest() {

        String key = Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS;
        String value = "TRUE";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Invalid setting");
        assertThat(thrown).hasMessageContaining(Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS);
    }

    @Test
    public void lateStartAuditDisableTest() {

        String key = Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT;
        String value = "disable";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), workFlowData);

        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isFalse();
    }

    @Test
    public void splitRestaurantAuditsDisableTest() {

        String key = Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS;
        String value = "disable";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), workFlowData);

        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isFalse();
    }

    @Test
    public void unvisitedRestaurantsAuditDisableTest() {

        String key = Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT;
        String value = "disable";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key, value)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), workFlowData);

        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isFalse();
    }

    @Test
    public void auditDefaultsTest() {

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), workFlowData);

        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isFalse();
    }

    @Test
    public void lateStartAuditEnableTest() {

        String key1 = Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT;
        String value1 = "enable";
        String key2 = Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS;
        String value2 = "disable";
        String key3 = Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT;
        String value3 = "disable";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key1, value1)
                + getKeyValueRow(key2, value2)
                + getKeyValueRow(key3, value3)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), workFlowData);

        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isTrue();
    }

    @Test
    public void splitRestaurantAuditsEnableTest() {

        String key1 = Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT;
        String value1 = "disable";
        String key2 = Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS;
        String value2 = "enable";
        String key3 = Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT;
        String value3 = "disable";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key1, value1)
                + getKeyValueRow(key2, value2)
                + getKeyValueRow(key3, value3)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), workFlowData);

        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isTrue();
    }

    @Test
    public void unvisitedRestaurantsAuditEnableTest() {

        String key1 = Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT;
        String value1 = "enable";
        String key2 = Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS;
        String value2 = "disable";
        String key3 = Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT;
        String value3 = "disable";

        String workFlowData = getHeader()
                + getBeginRow()
                + getVersionRow()
                + getKeyValueRow(key1, value1)
                + getKeyValueRow(key2, value2)
                + getKeyValueRow(key3, value3)
                + getEndRow();

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, getAllRestaurants(), workFlowData);

        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isTrue();
    }
}
