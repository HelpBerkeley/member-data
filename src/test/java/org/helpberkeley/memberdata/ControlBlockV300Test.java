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

import org.junit.Ignore;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ControlBlockV300Test extends ControlBlockTestBase{

    private final String controlBlockData;
    private final Map<String, User> users;
    private final Map<String, Restaurant> allRestaurants;

    private static final String EMPTY_ROW = ",,,,,,,,,,,,,,,,,,\n";

    private static final String HEADER = "Consumer,Driver,Name,User Name,Phone #,Phone2 #,Neighborhood,City,Address,"
            + "Condo,Details,Restaurants,std meals,alt meals,type meal,std grocery,alt grocery,type grocery,#orders\n";
    private final String  CONTROL_BLOCK_BEGIN_ROW = "FALSE,FALSE,ControlBegin,,,,,,,,,,,,,,,,\n";
    private final String  CONTROL_BLOCK_END_ROW = "FALSE,FALSE,ControlEnd,,,,,,,,,,,,,,,,\n";
    private final String  CONTROL_BLOCK_VERSION_ROW = "FALSE,FALSE,,Version ,,,,3-0-0,,,,,,,,,,,\n";

    public ControlBlockV300Test() {
        controlBlockData = readResourceFile("control-block-v300.csv");
        List<User> userList = new Loader(createApiSimulator()).load();
        users = new Tables(userList).mapByUserName();

        RestaurantTemplateParser parser =
                RestaurantTemplateParser.create(readResourceFile("restaurant-template-v300.csv"));
        allRestaurants = parser.restaurants();
    }

    @Override
    String getHeader() {
        return HEADER;
    }
    @Override
    String getBeginRow() {
        return CONTROL_BLOCK_BEGIN_ROW;
    }

    @Override
    String getEndRow() {
        return CONTROL_BLOCK_END_ROW;
    }

    @Override
    String getVersionRow() {
        return CONTROL_BLOCK_VERSION_ROW;
    }

    @Override
    String getEmptyRow() {
        return EMPTY_ROW;
    }

    @Override
    String getDirectiveRow(String directive) {
        return EMPTY_ROW.replaceFirst(",,,", "FALSE,FALSE," + directive + ",");
    }

    @Override
    String getKeyValueRow(String key, String value) {
        return EMPTY_ROW.replaceFirst(",,,,,,,", "FALSE,FALSE,," + key + ",,,," + value);
    }

    @Override
    protected String getControlBlockData() {
        return controlBlockData;
    }

    @Override
    protected Map<String, Restaurant> getAllRestaurants() {
        return allRestaurants;
    }

    @Override
    void audit(ControlBlock controlBlock) {
        assertThat(controlBlock).isInstanceOf(ControlBlockV300.class);
        ((ControlBlockV300)controlBlock).audit(users, List.of());
    }

    @Test
    public void altMealOptionsTest() {
        String key = Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS;
        String value = "\"none, veggie , noRed,noPork \"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).isEmpty();
        assertThat(controlBlock.getAltMealOptions()).containsExactly("none", "veggie", "noRed", "noPork");
        assertThat(controlBlock.getAltGroceryOptions()).isEmpty();
        assertThat(controlBlock.getStartTimes()).isEmpty();
        assertThat(controlBlock.getPickupManagers()).isEmpty();
    }

    @Test
    public void altGroceryOptionsTest() {
        String key = Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS;
        String value = "\"none, veg, custom pick\"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).isEmpty();
        assertThat(controlBlock.getAltGroceryOptions()).containsExactly("none", "veg", "custom pick");
        assertThat(controlBlock.getAltMealOptions()).isEmpty();
        assertThat(controlBlock.getStartTimes()).isEmpty();
        assertThat(controlBlock.getPickupManagers()).isEmpty();
    }

    @Test
    public void foodSourcesTest() {
        String key = Constants.CONTROL_BLOCK_FOOD_SOURCES;
        String mealSource = "Meals'R'Us";
        String grocerySource = "Groceryland";
        String value = mealSource + '|' + grocerySource;

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).isEmpty();
        assertThat(controlBlock.getMealSource()).isEqualTo(mealSource);
        assertThat(controlBlock.getGrocerySource()).isEqualTo(grocerySource);
        assertThat(controlBlock.getAltGroceryOptions()).isEmpty();
        assertThat(controlBlock.getAltMealOptions()).isEmpty();
        assertThat(controlBlock.getStartTimes()).isEmpty();
        assertThat(controlBlock.getPickupManagers()).isEmpty();
    }

    @Test
    public void startTimesTest() {
        String key = Constants.CONTROL_BLOCK_START_TIMES;
        String value = "\"3:00, 3:10, 3:15\"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).isEmpty();
        assertThat(controlBlock.getStartTimes()).containsExactly("3:00", "3:10", "3:15");
        assertThat(controlBlock.getAltMealOptions()).isEmpty();
        assertThat(controlBlock.getAltGroceryOptions()).isEmpty();
        assertThat(controlBlock.getPickupManagers()).isEmpty();
    }

    @Test
    public void pickupManagersTest() {
        String key = Constants.CONTROL_BLOCK_PICKUP_MANAGERS;
        String value = "\"John, Jacob, Jingleheimer\"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).isEmpty();
        assertThat(controlBlock.getPickupManagers()).containsExactly("John", "Jacob", "Jingleheimer");
        assertThat(controlBlock.getStartTimes()).isEmpty();
        assertThat(controlBlock.getAltMealOptions()).isEmpty();
        assertThat(controlBlock.getAltGroceryOptions()).isEmpty();
    }

    @Test
    public void startTimesAuditWarningsTest() {
        String key = Constants.CONTROL_BLOCK_START_TIMES;
        String value = "\"3:00, 3:10, 3:15\"";
        String opsManagerKey = "OpsManager (UserName|Phone)";
        String opsManagerValue = "JVol|222-222-2222";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).isEmpty();
        controlBlock.audit(users, List.of());
        assertThat(controlBlock.getWarnings()).contains(ControlBlockV300.MORE_START_TIMES_THAN_DRIVERS);
    }

    @Test
    public void invalidStartTimesTest() {
        String opsManagerKey = "OpsManager (UserName|Phone)";
        String opsManagerValue = "JVol|222-222-2222";
        String key = Constants.CONTROL_BLOCK_START_TIMES;
        String[] values = {
                "", "Seven", "1130", "9:O3"
        };

        for (String value : values) {
            String workFlowData = HEADER
                    + CONTROL_BLOCK_BEGIN_ROW
                    + CONTROL_BLOCK_VERSION_ROW
                    + getKeyValueRow(key, value)
                    + getKeyValueRow(opsManagerKey, opsManagerValue)
                    + CONTROL_BLOCK_END_ROW;

            WorkflowParser workflowParser = WorkflowParser.create(
                    WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
            ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();

            Throwable thrown = catchThrowable(() -> controlBlock.audit(users, List.of()));
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessage(MessageFormat.format(ControlBlockV300.INVALID_START_TIME, value));
        }
    }

    @Test
    public void validStartTimesTest() {
        String opsManagerKey = "OpsManager (UserName|Phone)";
        String opsManagerValue = "JVol|222-222-2222";
        String key = Constants.CONTROL_BLOCK_START_TIMES;
        String[] values = {
                "3:59", "03:11", "4:19 PM", "21:42"
        };

        for (String value : values) {
            String workFlowData = HEADER
                    + CONTROL_BLOCK_BEGIN_ROW
                    + CONTROL_BLOCK_VERSION_ROW
                    + getKeyValueRow(key, value)
                    + getKeyValueRow(opsManagerKey, opsManagerValue)
                    + CONTROL_BLOCK_END_ROW;

            WorkflowParser workflowParser = WorkflowParser.create(
                    WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
            ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
            assertThat(controlBlock.getStartTimes()).containsExactly(value);
            controlBlock.audit(users, List.of());
        }
    }

    // FIX THIS, DS: need to create a route workflow sheet for this test
    @Ignore
    @Test
    public void notEnoughStartTimesTest() {
        String opsManagerKey = "OpsManager (UserName|Phone)";
        String opsManagerValue = "JVol|222-222-2222";
        String key = Constants.CONTROL_BLOCK_START_TIMES;
        String value = "3:00";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();

        Throwable thrown = catchThrowable(() -> controlBlock.audit(users, List.of()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
    }
}
