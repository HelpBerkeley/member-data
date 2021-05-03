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

    private final String opsManagerKey = Constants.CONTROL_BLOCK_OPS_MANAGER;
    private final String opsManagerValue = "JVol|123-456-7890";
    private final String foodSourcesKey = Constants.CONTROL_BLOCK_FOOD_SOURCES;
    private final String foodSourcesValue = "WeBeSammiges|Bagzzz";
    private final String startTimesKey = Constants.CONTROL_BLOCK_START_TIMES;
    private final String startTimesValue = "\"1:00, 1:01, 1:59\"";
    private final String altMealOptionsKey = Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS;
    private final String altMealOptionsValue = "\"only-purple, no-emu\"";
    private final String altGroceryOptionsKey = Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS;
    private final String altGroceryOptionsValue = "\"Mac'n'Cheese only, piscivorous\"";
    private final String pickupManagersKey = Constants.CONTROL_BLOCK_PICKUP_MANAGER;
    private final String pickupManagersValue = "ZZZ";

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

    @Override
    String addVersionSpecificRequiredVariables() {
        return getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + getKeyValueRow(pickupManagersKey, pickupManagersValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsValue);

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
        assertThat(controlBlock.getAltMealOptions()).containsExactly("none", "veggie", "noRed", "noPork");
    }

    @Test
    public void altGroceryOptionsTest() {
        String key = Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS;
        String value = "\"none, veg, custom pick\"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + getKeyValueRow(pickupManagersKey, pickupManagersValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getAltGroceryOptions()).containsExactly("none", "veg", "custom pick");
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
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsKey)
                + getKeyValueRow(pickupManagersKey, pickupManagersValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getMealSource()).isEqualTo(mealSource);
        assertThat(controlBlock.getGrocerySource()).isEqualTo(grocerySource);
    }

    @Test
    public void startTimesTest() {
        String key = Constants.CONTROL_BLOCK_START_TIMES;
        String value = "\"3:00, 3:10, 3:15\"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsValue)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsValue)
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + getKeyValueRow(pickupManagersKey, pickupManagersValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getStartTimes()).containsExactly("3:00", "3:10", "3:15");
    }

    @Test
    public void pickupManagersTest() {
        String key = Constants.CONTROL_BLOCK_PICKUP_MANAGER;
        String value1 = "ZZZ";
        String value2 = "ThirdPerson";
        String value3 = "JVol";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value1)
                + getKeyValueRow(key, value2)
                + getKeyValueRow(key, value3)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsValue)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getPickupManagers()).containsExactly("ZZZ", "ThirdPerson", "JVol");
    }

    @Test
    public void startTimesAuditWarningsTest() {
        String key = Constants.CONTROL_BLOCK_START_TIMES;
        String value = "\"3:00, 3:10, 3:15\"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesKey)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsValue)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsValue)
                + getKeyValueRow(pickupManagersKey, pickupManagersValue)
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
                    + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                    + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                    + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsValue)
                    + getKeyValueRow(pickupManagersKey, pickupManagersValue)
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
        String key = Constants.CONTROL_BLOCK_START_TIMES;
        String[] values = {
                "3:59", "03:11", "4:19 PM", "21:42"
        };

        for (String value : values) {
            String workFlowData = HEADER
                    + CONTROL_BLOCK_BEGIN_ROW
                    + CONTROL_BLOCK_VERSION_ROW
                    + getKeyValueRow(key, value)
                    + getKeyValueRow(altMealOptionsKey, altMealOptionsValue)
                    + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsValue)
                    + getKeyValueRow(opsManagerKey, opsManagerValue)
                    + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                    + getKeyValueRow(pickupManagersKey, pickupManagersValue)
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

    @Test
    public void tooManyStartTimesTest() {
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
                    + getKeyValueRow(key, value)
                    + CONTROL_BLOCK_END_ROW;

            WorkflowParser workflowParser = WorkflowParser.create(
                    WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
            Throwable thrown = catchThrowable(() -> workflowParser.controlBlock());
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessage(ControlBlockV300.TOO_MANY_START_TIMES_VARIABLES);
        }
    }

    @Test
    public void missingStartTimesTest() {

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsValue)
                + getKeyValueRow(pickupManagersKey, pickupManagersValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.MISSING_START_TIMES_VARIABLE);
    }

    @Test
    public void tooManyFoodSourcesTest() {
        String key = Constants.CONTROL_BLOCK_FOOD_SOURCES;
        String mealSource = "Meals'R'Us";
        String grocerySource = "Groceryland";
        String value = mealSource + '|' + grocerySource;

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        Throwable thrown = catchThrowable(() -> workflowParser.controlBlock());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.TOO_MANY_FOOD_SOURCES_VARIABLES);
    }

    @Test
    public void missingFoodSourcesTest() {

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsValue)
                + getKeyValueRow(pickupManagersKey, pickupManagersValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.MISSING_FOOD_SOURCES_VARIABLE);
    }

    @Test
    public void missingAltMealOptionsTest() {

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(pickupManagersKey, pickupManagersValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.MISSING_ALT_MEAL_OPTIONS_VARIABLE);
    }

    @Test
    public void tooManyAltMealOptionsTest() {

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsKey)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        Throwable thrown = catchThrowable(() -> workflowParser.controlBlock());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.TOO_MANY_ALT_MEAL_OPTIONS_VARIABLES);
    }

    @Test
    public void missingAltGroceryOptionsTest() {

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                + getKeyValueRow(pickupManagersKey, pickupManagersValue)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.MISSING_ALT_GROCERY_OPTIONS_VARIABLE);
    }

    @Test
    public void tooManyAltGroceryOptionsTest() {

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsKey)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsKey)
                + CONTROL_BLOCK_END_ROW;
        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        Throwable thrown = catchThrowable(() -> workflowParser.controlBlock());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.TOO_MANY_ALT_GROCERY_OPTIONS_VARIABLES);
    }

    @Test
    public void missingPickupManagersTest() {

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(opsManagerKey, opsManagerValue)
                + getKeyValueRow(startTimesKey, startTimesValue)
                + getKeyValueRow(foodSourcesKey, foodSourcesValue)
                + getKeyValueRow(altMealOptionsKey, altMealOptionsKey)
                + getKeyValueRow(altGroceryOptionsKey, altGroceryOptionsKey)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.MISSING_PICKUP_MANAGERS_VARIABLE);
    }

}
