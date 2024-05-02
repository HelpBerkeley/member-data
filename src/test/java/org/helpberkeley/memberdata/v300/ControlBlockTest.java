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
package org.helpberkeley.memberdata.v300;

import org.assertj.core.api.ThrowableAssert;
import org.helpberkeley.memberdata.*;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ControlBlockTest extends ControlBlockTestBase {

    private final Map<String, User> users;
    private final Map<String, Restaurant> allRestaurants;

    public static final String EMPTY_ROW = ",,,,,,,,,,,,,,,,,\n";

    public static final String HEADER = "Consumer,Driver,Name,User Name,Phone #,Phone2 #,Neighborhood,City,Address,"
            + "Condo,Details,Restaurants,std meals,alt meals,type meal,std grocery,alt grocery,type grocery\n";
    public static final String  CONTROL_BLOCK_BEGIN_ROW = "FALSE,FALSE,ControlBegin,,,,,,,,,,,,,,,\n";
    public static final String  CONTROL_BLOCK_END_ROW =   "FALSE,FALSE,ControlEnd  ,,,,,,,,,,,,,,,\n";
    public static final String  CONTROL_BLOCK_VERSION_ROW = "FALSE,FALSE,,Version ,,,,3-0-0,,,,,,,,,,\n";

    public ControlBlockTest() {
        List<User> userList = new Loader(createApiSimulator()).load();
        users = new Tables(userList).mapByUserName();

        RestaurantTemplateParser parser =
                RestaurantTemplateParser.create(readResourceFile("restaurant-template-v300.csv"));
        allRestaurants = parser.restaurants();
    }

    @Override
    public String getHeader() {
        return HEADER;
    }
    @Override
    public String getBeginRow() {
        return CONTROL_BLOCK_BEGIN_ROW;
    }

    @Override
    public String getEndRow() {
        return CONTROL_BLOCK_END_ROW;
    }

    @Override
    public String getVersionRow() {
        return CONTROL_BLOCK_VERSION_ROW;
    }

    @Override
    public String getEmptyRow() {
        return EMPTY_ROW;
    }

    @Override
    public String getDirectiveRow(String directive) {
        return EMPTY_ROW.replaceFirst(",,,", "FALSE,FALSE," + directive + ",");
    }

    @Override
    public String getKeyValueRow(String key, String value) {
        return EMPTY_ROW.replaceFirst(",,,,,,,", "FALSE,FALSE,," + key + ",,,," + value);
    }

    @Override
    public Map<String, Restaurant> getAllRestaurants() {
        return allRestaurants;
    }

    @Override
    public void audit(ControlBlock controlBlock) {
        assertThat(controlBlock).isInstanceOf(ControlBlockV300.class);
        ((ControlBlockV300)controlBlock).audit(users, List.of());
    }

    @Override
    public String addVersionSpecificRequiredVariables() {
        return getKeyValueRow(Constants.CONTROL_BLOCK_FOOD_SOURCES,
                    quote(ControlBlockBuilder.DEFAULT_FOOD_SOURCES))
                + getKeyValueRow(Constants.CONTROL_BLOCK_PICKUP_MANAGER, ControlBlockBuilder.DEFAULT_PICKUP_MANAGER)
                + getKeyValueRow(Constants.CONTROL_BLOCK_START_TIMES,
                    quote(ControlBlockBuilder.DEFAULT_START_TIMES))
                + getKeyValueRow(Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS,
                    quote(ControlBlockBuilder.DEFAULT_ALT_MEAL_OPTIONS))
                + getKeyValueRow(Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS,
                    quote((ControlBlockBuilder.DEFAULT_ALT_GROCERY_OPTIONS)))
                + getKeyValueRow(Constants.CONTROL_BLOCK_MESSAGE_FORMAT, ControlBlockBuilder.DEFAULT_MESSAGE_FORMAT);
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

        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        assertThat(controlBlock.getAltMealOptions()).containsExactly("none", "veggie", "noRed", "noPork");
    }

    @Test
    public void altMealOptionsNoneTest() {

        String none = "none";
        ControlBlockBuilder builder = new ControlBlockBuilder();
        builder.withAltMealOptions(none);

        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), builder.build());

        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.INVALID_NONE_ALT, none, Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS));
    }

    @Test
    public void altGroceryOptionsTest() {
        String value = "none, veg, custom pick";
        String workFlowData = new ControlBlockBuilder().withAltGroceryOptions(value).build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getAltGroceryOptions()).containsExactly("none", "veg", "custom pick");
    }

    @Test
    public void altGroceryOptionsNoneTest() {

        String none = "none";
        ControlBlockBuilder builder = new ControlBlockBuilder();
        builder.withAltGroceryOptions(none);

        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), builder.build());

        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.INVALID_NONE_ALT, none, Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS));
    }

    @Test
    public void foodSourcesTest() {
        String mealSource = "Meals'R'Us";
        String grocerySource = "Groceryland";
        String value = mealSource + '|' + grocerySource;
        String workFlowData = new ControlBlockBuilder().withFoodSources(value).build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getMealSource()).isEqualTo(mealSource);
        assertThat(controlBlock.getGrocerySource()).isEqualTo(grocerySource);
    }

    @Test
    public void foodSourcesBadValueTest() {
        String mealSource = "Meals'R'Us";
        String grocerySource = "Groceryland";
        String value = mealSource + ':' + grocerySource;
        String workFlowData = new ControlBlockBuilder().withFoodSources(value).build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        Throwable thrown = catchThrowable(workflowParser::controlBlock);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(ControlBlockV300.FOOD_SOURCES_BAD_VALUE, value, 8));
    }

    @Test
    public void startTimesTest() {
        String value = "3:00, 3:10, 3:15";
        String workFlowData = new ControlBlockBuilder().withStartTimes(value).build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getStartTimes()).containsExactly("3:00", "3:10", "3:15");
    }

    @Test
    public void pickupManagersTest() {
        String value1 = "ZZZ";
        String value2 = "ThirdPerson";
        String value3 = "JVol";

        String workFlowData = new ControlBlockBuilder()
                .withPickupManager(value1)
                .withPickupManager(value2)
                .withPickupManager(value3).build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getPickupManagers()).containsExactly("ZZZ", "ThirdPerson", "JVol");
    }

    @Test
    public void startTimesAuditWarningsTest() {
        String value = "3:00, 3:10, 3:15";
        String workFlowData = new ControlBlockBuilder().withStartTimes(value).build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
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
            String workFlowData = new ControlBlockBuilder().withStartTimes(value).build();
            WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
            ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
            Throwable thrown = catchThrowable(() -> controlBlock.audit(users, List.of()));
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessage(MessageFormat.format(ControlBlockV300.INVALID_START_TIME, value));
        }
    }

    @Test
    public void validStartTimesTest() {
        String[] values = {
                "3:59", "03:11", "4:19 PM", "21:42"
        };

        for (String value : values) {
            String workFlowData = new ControlBlockBuilder().withStartTimes(value).build();
            WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
            ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
            assertThat(controlBlock.getStartTimes()).containsExactly(value);
            controlBlock.audit(users, List.of());
        }
    }

    @Test
    public void tooManyStartTimesTest() {
        String startTimes = "3:59";

        String workFlowData = new ControlBlockBuilder()
                .withStartTimes(startTimes)
                .withStartTimes(startTimes)
                .build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        Throwable thrown = catchThrowable(workflowParser::controlBlock);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.TOO_MANY_START_TIMES_VARIABLES);
    }

    @Test
    public void missingStartTimesTest() {

        String workFlowData = new ControlBlockBuilder().withoutStartTimes().build();

        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_START_TIMES));
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

        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        Throwable thrown = catchThrowable(workflowParser::controlBlock);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.TOO_MANY_FOOD_SOURCES_VARIABLES);
    }

    @Test
    public void missingFoodSourcesTest() {

        String workFlowData = new ControlBlockBuilder().withoutFoodSources().build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_FOOD_SOURCES));
    }

    @Test
    public void missingAltMealOptionsTest() {

        String workFlowData = new ControlBlockBuilder().withoutAltMealOptions().build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS));
    }

    @Test
    public void tooManyAltMealOptionsTest() {

        String altMealOptions = ControlBlockBuilder.DEFAULT_ALT_MEAL_OPTIONS;

        String workFlowData = new ControlBlockBuilder()
                .withAltMealOptions(altMealOptions)
                .withAltMealOptions(altMealOptions)
                .build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        Throwable thrown = catchThrowable(workflowParser::controlBlock);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.TOO_MANY_ALT_MEAL_OPTIONS_VARIABLES);
    }

    @Test
    public void missingAltGroceryOptionsTest() {

        String workFlowData = new ControlBlockBuilder().withoutAltGroceryOptions().build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS));
    }

    @Test
    public void tooManyAltGroceryOptionsTest() {

        String altGroceryOptions = "x, y, z";
        String workFlowData = new ControlBlockBuilder()
                .withAltGroceryOptions(altGroceryOptions)
                .withAltGroceryOptions(altGroceryOptions)
                .build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        Throwable thrown = catchThrowable(workflowParser::controlBlock);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.TOO_MANY_ALT_GROCERY_OPTIONS_VARIABLES);
    }

    @Test
    public void missingPickupManagersTest() {

        String workFlowData = new ControlBlockBuilder().withoutPickupManager().build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_PICKUP_MANAGER));
    }

    @Test
    public void missingMessageFormatTest() {

        String workFlowData = new ControlBlockBuilder().withoutMessageFormat().build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.MISSING_REQUIRED_VARIABLE, Constants.CONTROL_BLOCK_MESSAGE_FORMAT));
    }

    @Test
    public void invalidMessageFormatTest() {
        String badFormat = "TuesdayWeld";
        String workFlowData = new ControlBlockBuilder().withMessageFormat(badFormat).build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        Throwable thrown = catchThrowable(() -> audit(controlBlock));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(ControlBlockV300.INVALID_MESSAGE_FORMAT, badFormat));
    }

    @Test
    public void tooManyMessageFormatTest() {
        String format1 = MessageSpecFormat.MONDAY.getFormat();
        String format2 = MessageSpecFormat.MONDAY.getFormat();
        String workFlowData = new ControlBlockBuilder()
                .withMessageFormat(format1)
                .withMessageFormat(format2)
                .build();
        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), workFlowData);
        Throwable thrown = catchThrowable(workflowParser::controlBlock);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(ControlBlockV300.TOO_MANY_MESSAGE_FORMAT_VARIABLES);
    }

    @Test
    public void mealsOnlyFoodSourceTest() {

        String mealSource = "Chez McDo";
        ControlBlockBuilder builder = new ControlBlockBuilder();
        builder.withFoodSources(mealSource + "|");

        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), builder.build());

        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getMealSource()).isEqualTo(mealSource);
        assertThat(controlBlock.getGrocerySource()).isEmpty();
    }

    @Test
    public void groceriesOnlyFoodSourceTest() {

        String grocerySource = "Big Red Barn Productions";
        ControlBlockBuilder builder = new ControlBlockBuilder();
        builder.withFoodSources("|" + grocerySource);

        WorkflowParser workflowParser = WorkflowParser.create(Map.of(), builder.build());

        ControlBlockV300 controlBlock = (ControlBlockV300) workflowParser.controlBlock();
        audit(controlBlock);
        assertThat(controlBlock.getGrocerySource()).isEqualTo(grocerySource);
        assertThat(controlBlock.getMealSource()).isEmpty();
    }

    @Test
    public void retrieveOpsManagerPhoneTest() {
        // JVol | 123-456-7890
        ControlBlockBuilder builder = new ControlBlockBuilder();
        String value = "JVol";
        builder.withOpsManager(value);

        String csvData = builder.build();

        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, csvData);
        assertThat(driverPostFormat.getControlBlock().getFirstOpsManager().getPhone()).isEqualTo("123-456-7890");
    }
}