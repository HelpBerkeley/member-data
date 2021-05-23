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

import org.helpberkeley.memberdata.*;
import org.junit.Ignore;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class WorkflowHBParserTest extends WorkflowHBParserBaseTest {

    private final String controlBlockData = ControlBlockTest.HEADER
            + ControlBlockTest.CONTROL_BLOCK_BEGIN_ROW
            + ControlBlockTest.CONTROL_BLOCK_VERSION_ROW
            + ControlBlockTest.CONTROL_BLOCK_END_ROW
            + ControlBlockTest.EMPTY_ROW;

    private final ControlBlock controlBlock = ControlBlock.create(controlBlockData);

    private final Map<String, Restaurant> restaurants =
            Map.of("RevFoodTruck", Restaurant.createRestaurant(controlBlock, "RevFoodTruck"),
                    "BFN", Restaurant.createRestaurant(controlBlock, "BFN"));

    public WorkflowHBParserTest() {
        super();
    }

    @Override
    public List<String> getColumnNames() {
        return List.of(
                Constants.WORKFLOW_ADDRESS_COLUMN,
                Constants.WORKFLOW_ALT_GROCERY_COLUMN,
                Constants.WORKFLOW_ALT_MEALS_COLUMN,
                Constants.WORKFLOW_ALT_PHONE_COLUMN,
                Constants.WORKFLOW_CITY_COLUMN,
                Constants.WORKFLOW_CONDO_COLUMN,
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_NAME_COLUMN,
                Constants.WORKFLOW_PHONE_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_STD_GROCERY_COLUMN,
                Constants.WORKFLOW_STD_MEALS_COLUMN,
                Constants.WORKFLOW_TYPE_MEAL_COLUMN,
                Constants.WORKFLOW_USER_NAME_COLUMN,
                Constants.WORKFLOW_TYPE_GROCERY_COLUMN);
    }

    @Override
    public String getMinimumControlBlock() {
        return controlBlockData;
    }

    @Test
    public void badValueStandardMealsTest() {
        String badCount = "NaN";

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withStdMeals(badCount);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.INVALID_COUNT_VALUE, 15, badCount, Constants.WORKFLOW_STD_MEALS_COLUMN));
    }

    @Test
    public void badValueAltMealsTest() {
        String badCount = "mauvaise valeur";

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltMeals(badCount);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.INVALID_COUNT_VALUE, 15, badCount, Constants.WORKFLOW_ALT_MEALS_COLUMN));
    }

    @Test
    public void missingMealTypeTest() {
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltMeals("1");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_ALT_TYPE, 15, Constants.WORKFLOW_TYPE_MEAL_COLUMN));
    }

    @Test
    public void mismatchMealTypeTest() {
        String unknownMealType = "ginger sling with a pineapple heart";
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltMeals("1").withTypeMeal(unknownMealType);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.ALT_MEAL_MISMATCH,
                17, unknownMealType, ControlBlockBuilder.DEFAULT_ALT_MEAL_OPTIONS));
    }

    @Test
    public void emptyAltMealTest() {
        String mealType = "coffee only";
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltMeals("1").withTypeMeal(mealType);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withAltMealOptions(""));

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.EMPTY_ALT_MEAL, 17, mealType));
    }

    @Test
    public void emptyAltGroceryTest() {
        String groceryType = "kosher";
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltGrocery("1").withTypeGrocery(groceryType);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder().withName("BFN"));
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withAltGroceryOptions(""));

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.EMPTY_ALT_GROCERY, 17, groceryType));
    }


    @Test
    public void badValueStandardGroceryTest() {
        String badCount = "Also NaN";

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withStdGrocery(badCount);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.INVALID_COUNT_VALUE, 15, badCount, Constants.WORKFLOW_STD_GROCERY_COLUMN));
    }

    @Test
    public void badValueAltGroceryTest() {
        String badCount = "encore une mauvaise valeur";

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltGrocery(badCount);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.INVALID_COUNT_VALUE, 15, badCount, Constants.WORKFLOW_ALT_GROCERY_COLUMN));
    }

    @Test
    public void missingGroceryTypeTest() {

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltGrocery("1");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_ALT_TYPE, 15, Constants.WORKFLOW_TYPE_GROCERY_COLUMN));
    }

    @Test
    public void mismatchGroceryTypeTest() {
        String unknownGroceryType = "blue foods";
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltGrocery("1").withTypeGrocery(unknownGroceryType);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder().withName("BFN"));
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.ALT_GROCERY_MISMATCH,
                17, unknownGroceryType, ControlBlockBuilder.DEFAULT_ALT_GROCERY_OPTIONS));
    }

    @Test
    public void missingConsumerNameTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withConsumerName(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.MISSING_CONSUMER_NAME, 15));
    }

    @Test
    public void missingConsumerUserNameTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withConsumerUserName(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_CONSUMER_USER_NAME, 15));
    }

    @Test
    public void missingCityTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withCity(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_CITY, 15));
    }

    @Test
    public void missingAddressTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withAddress(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_ADDRESS, 15));
    }

    @Test
    public void missingPhoneTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withPhone(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_PHONE, 15));
    }

    @Test
    public void emptyGrocerySourceTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withPhone(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withFoodSources("RevFoodTruck|"));

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        assertThat(parser.controlBlock().getWarnings()).contains(ControlBlockV300.EMPTY_GROCERY_SOURCE);
    }

    @Test
    public void emptyMealSourceTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withStdGrocery("1"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withFoodSources("|BFN"));

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        assertThat(parser.controlBlock().getWarnings()).contains(ControlBlockV300.EMPTY_MEAL_SOURCE);
    }

    @Test
    public void unknownPickupManagerTest() {
        String notAMember = "Fred";
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withPickupManager(notAMember));

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        Throwable thrown = catchThrowable(() -> auditControlBlock(parser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.UNKNOWN_PICKUP_MANAGER, notAMember));
    }

    @Test
    public void duplicatePickupRestaurantTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(() -> parser.drivers());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.DUPLICATE_PICKUP,
                WorkflowBuilder.DEFAULT_RESTAURANT_NAME, WorkflowBuilder.DEFAULT_DRIVER_USER_NAME));
    }

    @Test
    public void emptyDeliveryTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withStdMeals("0"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        List<Driver> drivers = parser.drivers();
        assertThat(drivers).hasSize(1);
        assertThat(drivers.get(0).getWarningMessages()).containsExactly(MessageFormat.format(
                WorkflowParserV300.EMPTY_DELIVERY, 15,
                WorkflowBuilder.DEFAULT_DRIVER_NAME, DeliveryBuilder.DEFAULT_CONSUMER_NAME));
    }

    private void auditControlBlock(ControlBlock controlBlock) {
        ((ControlBlockV300) controlBlock).audit(users, List.of());
    }
}