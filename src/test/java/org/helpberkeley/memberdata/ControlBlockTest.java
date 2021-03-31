/*
 * Copyright (c) 2020-2021 helpberkeley.org
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

@SuppressWarnings("unchecked")
public class ControlBlockTest extends TestBase {

    private final String controlBlockData;
    private final Map<String, User> users;
    private final Map<String, Restaurant> allRestaurants;

    private static final String HEADER =
        "Consumer,Driver,Name,User Name,Phone #,Phone2 #,Neighborhood,City,"
        + "Address,Condo,Details,Restaurants,normal,veggie,#orders\n";

    public ControlBlockTest() {
        controlBlockData = readResourceFile("control-block.csv");
        List<User> userList = new Loader(createApiSimulator()).load();
        users = new Tables(userList).mapByUserName();

        RestaurantTemplateParser parser =
                new RestaurantTemplateParser(readResourceFile("restaurant-template-v2-0-0.csv"));
        allRestaurants = parser.restaurants();
    }

    @Test
    public void controlBlockTest() {
        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, controlBlockData);

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

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "TRUE,FALSE,,,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Control block Consumer column does not contain FALSE, at line 3.\n");
    }

    /** Test that true in a driver column throws an exception */
    @Test
    public void driverTrueTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "FALSE,TRUE,,,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Control block Driver column does not contain FALSE, at line 3.\n");
    }

    @Test
    public void unknownDirectiveTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "FALSE,FALSE,ControlBlockGarbage,,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Unexpected control block directive");
        assertThat(thrown).hasMessageContaining("ControlBlockGarbage");
    }

    @Test
    public void unknownVariableTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "FALSE,FALSE,,BadVariableName,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).contains(
                "Unknown key \"BadVariableName\" in the User Name column at line 3.");
    }

    @Test
    public void missingOpsManagerValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,fred 123-456-7890,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"username | phone\"");
        assertThat(thrown).hasMessageContaining("fred 123-456-7890");
    }

    @Test
    public void tooManyOpsManagerValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,|fred|123-456-7890|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"username | phone\"");
        assertThat(thrown).hasMessageContaining("|fred|123-456-7890|");
    }

    @Test
    public void opsManagerEmptyUserNameTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,|123-456-7890,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty OpsManager user name at line 4.");
    }

    @Test
    public void opsManagerUserNameWithAtSignTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,@fred|123-456-7890,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("OpsManager user name \"@fred\" at line 4 cannot start with @");
    }

    @Test
    public void opsManagerUserNameWithWithSpacesTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,fred e mercury|123-456-7890,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "OpsManager user name \"fred e mercury\" at line 4 cannot contain spaces.");
    }

    /** Test the audit for an ops manager that is not a known user */
    @Test
    public void opsManagerUnknownUserTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,UnknownDudette|123-456-7890,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> controlBlock.audit(users, allRestaurants, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.UNKNOWN_OPS_MANAGER, "UnknownDudette"));
    }

    @Test
    public void opsManagerEmptyPhoneTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,biff| ,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty OpsManager phone number at line 4.");
    }

    @Test
    public void opsManagerMismatchedPhoneTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,JVol|222-222-2222,,,,,,,\n";
        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        controlBlock.audit(users, allRestaurants, Collections.EMPTY_LIST);
        assertThat(controlBlock.getWarnings()).contains(
                MessageFormat.format(ControlBlock.OPS_MANAGER_PHONE_MISMATCH, "JVol", "222-222-2222"));
    }

    @Test
    public void missingSplitRestaurantValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,MickeyDs bobbyjo,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"restaurant name | cleanup driver user name\"");
        assertThat(thrown).hasMessageContaining("\"MickeyDs bobbyjo\"");
    }

    @Test
    public void tooManySplitRestaurantValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,|Max's|buzz|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"restaurant name | cleanup driver user name\"");
        assertThat(thrown).hasMessageContaining("|Max's|buzz|");
    }

    @Test
    public void splitRestaurantRestaurantNameTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,|buzz,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty SplitRestaurant restaurant name at line 3.");
    }

    @Test
    public void splitRestaurantCleanupDriverUserNameWithAtSignTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,Bopshop|@fred,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "SplitRestaurant cleanup driver user name \"@fred\" at line 3 cannot start with @");
    }

    @Test
    public void splitRestaurantCleanupDriverUserNameWithWithSpacesTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,Bobshop|fred e mercury,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "SplitRestaurant cleanup driver user name \"fred e mercury\" at line 4 cannot contain spaces.");
    }

    @Test
    public void splitRestaurantEmptyCleanupDriverUserNameTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,Bobshop|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty SplitRestaurant cleanup driver user name at line 3.");
    }

    @Test
    public void backupDriverUserNameWithSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,BackupDriverUserName,,,,bligzhfzzt|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "BackupDriverUserName value \"bligzhfzzt|\" at line 4 does not match \"backupDriverUserName\"");
    }

    @Test
    public void emptyBackupDriverUserNameTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,BackupDriverUserName,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty BackupDriver user name at line 3");
    }

    @Test
    public void backupDriverUserNameWithAtSignTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,BackupDriverUserName,,,,@roygbv,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("BackupDriver user name \"@roygbv\" at line 4 cannot start with a @");
    }

    @Test
    public void backupDriverUserNameWithSpacesTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,BackupDriverUserName,,,,billy joe bob boy,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "BackupDriver user name \"billy joe bob boy\" at line 3 cannot contain spaces");
    }

    @Test
    public void auditNoOpsManagerTest() {
        String workFlowData =
                HEADER + CONTROL_BLOCK_BEGIN_ROW + CONTROL_BLOCK_VERSION_2_0_0_ROW + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(users, allRestaurants, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(ControlBlock.ERROR_MISSING_OPS_MANAGER);
    }

    /** Verify audit of a split restaurant not having a cleanup driver in the control block */
    @Test
    public void auditSplitRestaurantNoCleanupTest() {

        Throwable thrown = catchThrowable(() -> new DriverPostFormat(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-missing-cleanup.csv")));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.MISSING_SPLIT_RESTAURANT, "Cafe Raj"));
    }

    /** Verify disabled audit of a split restaurant not having a cleanup driver in the control block */
    @Test
    public void disabledAuditSplitRestaurantNoCleanupTest() {
        new DriverPostFormat(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-missing-cleanup-audit-disabled.csv"));
    }

    /** Verify audit of a split restaurant with an unknown restaurant name */
    @Test
    public void auditSplitRestaurantBadNameTest() {

        Throwable thrown = catchThrowable(() -> new DriverPostFormat(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-restaurant-unknown.csv")));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.MISSING_SPLIT_RESTAURANT, "Cafe Raj"));
    }

    /**
     *  Verify audit of a split restaurant specifying a cleanup driver
     * that isn't a driver for that restaurant.
     */
    @Test
    public void auditSplitRestaurantWrongDriverTest() {

        Throwable thrown = catchThrowable(() -> new DriverPostFormat(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-wrong-cleanup.csv")));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.WRONG_CLEANUP_DRIVER, "JVol", "Cafe Raj"));
    }

    /**
     *  Verify audit of a split restaurant specifying a cleanup driver
     * that isn't a driver for that restaurant.
     */
    @Test
    public void auditSplitRestaurantUnknownDriverTest() {

        Throwable thrown = catchThrowable(() -> new DriverPostFormat(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-unknown-cleanup.csv")));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.UNKNOWN_CLEANUP_DRIVER, "Sparkles", "Cafe Raj"));
    }

    @Test
    public void opsManagerUserNameDefaultValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
            + CONTROL_BLOCK_VERSION_2_0_0_ROW
            + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
            + "ReplaceThisByUserName | 510-555-1212,,,,,,,\n"
            + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(users, allRestaurants, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(
                "Set OpsManager user name \"ReplaceThisByUserName\" at line 4 to a valid OpsManager user name.\n");
    }

    @Test
    public void opsManagerPhoneDefaultValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | ReplaceThisByPhone#In510-555-1212Format,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(users, allRestaurants, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Set OpsManager phone "
                + "\"ReplaceThisByPhone#In510-555-1212Format\" at line 4 to a valid phone number.\n");
    }

    @Test
    public void multipleOpsManagersTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | 510-555-1212,,,,,,,\n"
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "Fredrica | 510-555-1213,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(users, allRestaurants, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Line 5, multiple OpsManager entries not yet supported.\n");
    }

    @Test
    public void splitRestaurantNameDefaultValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | 510-555-1212,,,,,,,\n"
                + "FALSE,FALSE,,SplitRestaurant (Name | CleanupDriverUserName)"
                + ",,,,ReplaceThisBySplitRestaurantName | RalphKramden,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(users, allRestaurants, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Set SplitRestaurant name "
                + "\"ReplaceThisBySplitRestaurantName\" at line 5 to a valid restaurant name.\n");
    }

    @Test
    public void splitRestaurantCleanupDefaultValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | 510-555-1212,,,,,,,\n"
                + "FALSE,FALSE,,SplitRestaurant (Name | CleanupDriverUserName)"
                + ",,,,White Castle| ReplaceThisByCleanupDriverUserName,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(users, allRestaurants, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Set SplitRestaurant cleanup driver user name "
                + "\"White Castle\" at line 5 to a valid user name.\n");
    }

    @Test
    public void noBackupDriversTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "JVol | 123-456-7890,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        controlBlock.audit(users, allRestaurants, Collections.EMPTY_LIST);
        assertThat(controlBlock.getWarnings()).contains("No BackupDriverUserName set in the control block.\n");
    }

    @Test
    public void unsupportedVersionTest() {
        String unsupportedVersion = "42";
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,Version,,,,"
                + unsupportedVersion
                + ",,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;

        Throwable thrown = catchThrowable(() -> new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Control block version " + unsupportedVersion + " is not supported.\n");
    }

    /** Verify audit failure for unknown backup driver */
    @Test
    public void unknownBackupDriverTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | 510-555-1212,,,,,,,\n"
                + "FALSE,FALSE,,BackupDriverUserName,,,,"
                + "NotAMemberDude,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> controlBlock.audit(users, allRestaurants, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.UNKNOWN_BACKUP_DRIVER, "NotAMemberDude"));
    }

    /** Verify audit warning for backup not being a driver */
    @Test
    public void backupNotADriverTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "JVol | 123-456-7890,,,,,,,\n"
                + "FALSE,FALSE,,BackupDriverUserName,,,,"
                + "ZZZ,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        controlBlock.audit(users, allRestaurants, Collections.EMPTY_LIST);
        assertThat(controlBlock.getWarnings()).contains(
                MessageFormat.format(ControlBlock.BACKUP_IS_NOT_A_DRIVER, "ZZZ"));
    }

    @Test
    public void lateStartAuditBadValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT + ",,,,TRUE,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Invalid setting");
        assertThat(thrown).hasMessageContaining(Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT);
    }

    @Test
    public void unvisitedResutaurantsAuditBadValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT + ",,,,TRUE,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Invalid setting");
        assertThat(thrown).hasMessageContaining(Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT);
    }

    @Test
    public void splitRestaurantsAuditBadValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS + ",,,,TRUE,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Invalid setting");
        assertThat(thrown).hasMessageContaining(Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS);
    }

    @Test
    public void lateStartAuditDisableTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT + ",,,,disable,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isFalse();
    }

    @Test
    public void splitRestaurantAuditsDisableTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS + ",,,,disable,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isFalse();
    }

    @Test
    public void unvisitedRestaurantsAuditDisableTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT + ",,,,disable,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isFalse();
    }

    @Test
    public void auditDefaultsTest() {

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, HEADER);

        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isFalse();
    }

    @Test
    public void lateStartAuditEnableTest() {
        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT + ",,,,enable,,,,,,,\n"
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS + ",,,,disable,,,,,,,\n"
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT + ",,,,disable,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isTrue();
    }

    @Test
    public void splitRestaurantAuditsEnableTest() {
        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS + ",,,,enable,,,,,,,\n"
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT + ",,,,disable,,,,,,,\n"
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT + ",,,,disable,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isTrue();
    }

    @Test
    public void unvisitedRestaurantsAuditEnableTest() {
        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_2_0_0_ROW
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT + ",,,,enable,,,,,,,\n"
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS + ",,,,disable,,,,,,,\n"
                + "FALSE,FALSE,," + Constants.CONTROL_BLOCK_LATE_ARRIVAL_AUDIT + ",,,,disable,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, allRestaurants, workFlowData);

        assertThat(workflowParser.controlBlock().restaurantsAuditDisabled()).isFalse();
        assertThat(workflowParser.controlBlock().splitRestaurantAuditsDisabled()).isTrue();
        assertThat(workflowParser.controlBlock().lateArrivalAuditDisabled()).isTrue();
    }
}
