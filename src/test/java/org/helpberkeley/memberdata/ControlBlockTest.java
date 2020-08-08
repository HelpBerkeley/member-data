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

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

@SuppressWarnings("unchecked")
public class ControlBlockTest extends TestBase {

    private final String controlBlockData;

    private static final String HEADER =
        "Consumer,Driver,Name,User Name,Phone #,Phone2 #,Neighborhood,City,"
        + "Address,Condo,Details,Restaurants,normal,veggie,#orders\n";
    private static final String  CONTROL_BLOCK_BEGIN_ROW =
            "FALSE,FALSE," + Constants.CONTROL_BLOCK_BEGIN + ",,,,,,,,,,,,\n";
    private static final String  CONTROL_BLOCK_END_ROW =
            "FALSE,FALSE," + Constants.CONTROL_BLOCK_END + ",,,,,,,,,,,,\n";

    public ControlBlockTest() {
        controlBlockData = readResourceFile("control-block.csv");
    }

    @Test
    public void controlBlockTest() {
        WorkflowParser workflowParser =
                new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, controlBlockData);

        ControlBlock controlBlock = workflowParser.controlBlock();
        assertThat(controlBlock.getOpsManagers()).containsExactly(
                new ControlBlock.OpsManager("zzz", "123-456-7890"));
        assertThat(controlBlock.getSplitRestaurants()).containsExactly(
                new ControlBlock.SplitRestaurant("Jot Mahal", "joebdriver"));
        assertThat(controlBlock.getBackupDrivers()).containsExactly("josephinedriver");
    }

    /** Test that true in a consumer column throws an exception */
    @Test
    public void consumerTrueTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "TRUE,FALSE,,,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Control block Consumer column does not contain FALSE, at line 3.\n");
    }

    /** Test that true in a driver column throws an exception */
    @Test
    public void driverTrueTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "FALSE,TRUE,,,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Control block Driver column does not contain FALSE, at line 3.\n");
    }

    @Test
    public void unknownDirectiveTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "FALSE,FALSE,ControlBlockGarbage,,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Unexpected control block directive");
        assertThat(thrown).hasMessageContaining("ControlBlockGarbage");
    }

    @Test
    public void unknownVariableTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "FALSE,FALSE,,BadVariableName,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).contains(
                "Unknown key \"BadVariableName\" in the User Name column at line 3.");
    }

    @Test
    public void missingOpsManagerValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,fred 123-456-7890,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"username | phone\"");
        assertThat(thrown).hasMessageContaining("fred 123-456-7890");
    }

    @Test
    public void tooManyOpsManagerValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,|fred|123-456-7890|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"username | phone\"");
        assertThat(thrown).hasMessageContaining("|fred|123-456-7890|");
    }

    @Test
    public void opsManagerEmptyUserNameTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,|123-456-7890,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty OpsManager user name at line 3.");
    }

    @Test
    public void opsManagerUserNameWithAtSignTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,@fred|123-456-7890,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("OpsManager user name \"@fred\" at line 3 cannot start with @");
    }

    @Test
    public void opsManagerUserNameWithWithSpacesTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,fred e mercury|123-456-7890,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "OpsManager user name \"fred e mercury\" at line 3 cannot contain spaces.");
    }

    @Test
    public void opsManagerEmptyPhoneTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName|Phone),,,,biff| ,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty OpsManager phone number at line 3.");
    }

    @Test
    public void missingSplitRestaurantValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,MickeyDs bobbyjo,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"restaurant name | cleanup driver user name\"");
        assertThat(thrown).hasMessageContaining("\"MickeyDs bobbyjo\"");
    }

    @Test
    public void tooManySplitRestaurantValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,|Max's|buzz|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"restaurant name | cleanup driver user name\"");
        assertThat(thrown).hasMessageContaining("|Max's|buzz|");
    }

    @Test
    public void splitRestaurantRestaurantNameTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,|buzz,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty SplitRestaurant restaurant name at line 3.");
    }

    @Test
    public void splitRestaurantCleanupDriverUserNameWithAtSignTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,Bopshop|@fred,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "SplitRestaurant cleanup driver user name \"@fred\" at line 3 cannot start with @");
    }

    @Test
    public void splitRestaurantCleanupDriverUserNameWithWithSpacesTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,Bobshop|fred e mercury,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "SplitRestaurant cleanup driver user name \"fred e mercury\" at line 3 cannot contain spaces.");
    }

    @Test
    public void splitRestaurantEmptyCleanupDriverUserNameTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,SplitRestaurant (Name|CleanupDriverUserName),,,,Bobshop|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty SplitRestaurant cleanup driver user name at line 3.");
    }

    @Test
    public void backupDriverUserNameWithSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,BackupDriverUserName,,,,bligzhfzzt|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "BackupDriverUserName value \"bligzhfzzt|\" at line 3 does not match \"backupDriverUserName\"");
    }

    @Test
    public void emptyBackupDriverUserNameTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,BackupDriverUserName,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty BackupDriver user name at line 3");
    }

    @Test
    public void backupDriverUserNameWithAtSignTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,BackupDriverUserName,,,,@roygbv,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("BackupDriver user name \"@roygbv\" at line 3 cannot start with a @");
    }

    @Test
    public void backupDriverUserNameWithSpacesTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,BackupDriverUserName,,,,billy joe bob boy,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "BackupDriver user name \"billy joe bob boy\" at line 3 cannot contain spaces");
    }

    @Test
    public void missingRestaurantValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,Restaurant (Name|Emoji),,,,Bopshop :splatt:,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"name | emoji\"");
        assertThat(thrown).hasMessageContaining("Bopshop :splatt:");
    }

    @Test
    public void tooManyRestaurantValueSeparatorTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,Restaurant (Name|Emoji),,,,|x|:splatt:|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("does not match \"name | emoji\"");
        assertThat(thrown).hasMessageContaining("|x|:splatt:|");
    }

    @Test
    public void restaurantEmptyNameTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,Restaurant (Name|Emoji),,,,|:splatt:,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty Restaurant name at line 3.");
    }

    @Test
    public void restaurantEmptyEmojiTest() {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,Restaurant (Name|Emoji),,,,V&A Cafe|,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Empty Restaurant emoji at line 3.");
    }

    @Test
    public void auditNoOpsManagerTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(Collections.EMPTY_LIST, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(ControlBlock.ERROR_MISSING_OPS_MANAGER);
    }

    @Test
    public void auditNoSplitRestaurantsNotFound() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + CONTROL_BLOCK_END_ROW;

        List<String> splitRestaurantNames = List.of("Bob's Big Boy", "Daimo");

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(()
                -> workflowParser.controlBlock().audit(Collections.EMPTY_LIST, splitRestaurantNames));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(ControlBlock.ERROR_MISSING_OPS_MANAGER);

        for (String name : splitRestaurantNames) {
            assertThat(thrown).hasMessageContaining(
                    "Control block does not contain a SplitRestaurant(Name|CleanupDriverUserName) entry for "
                    + name);
        }
    }

    @Test
    public void opsManagerUserNameDefaultValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
            + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
            + "ReplaceThisByUserName | 510-555-1212,,,,,,,\n"
            + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(Collections.EMPTY_LIST, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(
                "Set OpsManager user name \"ReplaceThisByUserName\" at line 3 to a valid OpsManager user name.\n");
    }

    @Test
    public void opsManagerPhoneDefaultValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | ReplaceThisByPhone#In510-555-1212Format,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(Collections.EMPTY_LIST, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Set OpsManager phone "
                + "\"ReplaceThisByPhone#In510-555-1212Format\" at line 3 to a valid phone number.\n");
    }

    @Test
    public void multipleOpsManagersTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | 510-555-1212,,,,,,,\n"
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "Fredrica | 510-555-1213,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(Collections.EMPTY_LIST, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Line 4, multiple OpsManager entries not yet supported.\n");
    }

    @Test
    public void splitRestaurantNameDefaultValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | 510-555-1212,,,,,,,\n"
                + "FALSE,FALSE,,SplitRestaurant (Name | CleanupDriverUserName)"
                + ",,,,ReplaceThisBySplitRestaurantName | RalphKramden,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(Collections.EMPTY_LIST, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Set SplitRestaurant name "
                + "\"ReplaceThisBySplitRestaurantName\" at line 4 to a valid restaurant name.\n");
    }

    @Test
    public void splitRestaurantCleanupDefaultValueTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | 510-555-1212,,,,,,,\n"
                + "FALSE,FALSE,,SplitRestaurant (Name | CleanupDriverUserName)"
                + ",,,,White Castle| ReplaceThisByCleanupDriverUserName,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(() ->
                workflowParser.controlBlock().audit(Collections.EMPTY_LIST, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Set SplitRestaurant cleanup driver user name "
                + "\"White Castle\" at line 4 to a valid user name.\n");
    }

    @Test
    public void missingEmojiTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | 510-555-1212,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;

        List<String> restaurants = List.of("Bopshop", "Cafe Raj", "Jot Mahal");

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        controlBlock.audit(restaurants, Collections.EMPTY_LIST);
        for (String restaurant : restaurants) {
            assertThat(controlBlock.getWarnings()).contains("Restaurant "
                    + restaurant + " does not have a Restaurant(Name|Emoji) entry in the control block.\n");
        }
    }

    @Test
    public void noBackupDriversTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,OpsManager (UserName | Phone),,,,"
                + "FredZ | 510-555-1212,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        controlBlock.audit(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        assertThat(controlBlock.getWarnings()).contains("No BackupDriverUserName set in the control block.\n");
    }

    @Test
    public void versionNotANumberTest() {
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,Version,,,,ThisIsNotAVersionNumber,,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);
        Throwable thrown = catchThrowable(workflowParser::controlBlock);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Version \"ThisIsNotAVersionNumber\" at line 3 is not valid version number.\n");
    }

    @Test
    public void unsupportedVersionTest() {
        String unsupportedVersion = Integer.toString(Constants.CONTROL_BLOCK_CURRENT_VERSION + 1);
        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW
                + "FALSE,FALSE,,Version,,,,"
                + unsupportedVersion
                + ",,,,,,,\n"
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> controlBlock.audit(Collections.EMPTY_LIST, Collections.EMPTY_LIST));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Control block version " + unsupportedVersion + " is not supported.\n");
    }
}