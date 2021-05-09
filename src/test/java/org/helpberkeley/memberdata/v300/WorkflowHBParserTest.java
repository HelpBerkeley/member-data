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

import org.assertj.core.api.Assertions;
import org.checkerframework.checker.units.qual.C;
import org.helpberkeley.memberdata.*;
import org.junit.Ignore;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class WorkflowHBParserTest extends WorkflowHBParserBaseTest {

    private final String controlBlockData = ControlBlockTest.HEADER
            + ControlBlockTest.CONTROL_BLOCK_BEGIN_ROW
            + ControlBlockTest.CONTROL_BLOCK_VERSION_ROW
            + ControlBlockTest.CONTROL_BLOCK_END_ROW
            + ControlBlockTest.EMPTY_ROW;

    private final String driverRow =
        "FALSE,TRUE,Joe B. Driver,jbDriver,777-777-7777,none,Hills,Berkeley,77 77th Place,,,,,,,,,\n";
    private final String restaurantRow =
            "FALSE,,,,,,,,\"9999 999 St., Berkeley, CA\",FALSE,,RevFoodTruck,,,,,,\n";
    private final String routeURL =
        "\"https://www.google.com/maps/dir/something+something+else\",,,,,,,,,,,,,,,,,\n";

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
                WorkflowParserV300.INVALID_COUNT_VALUE, "14", badCount, Constants.WORKFLOW_STD_MEALS_COLUMN));
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
                WorkflowParserV300.INVALID_COUNT_VALUE, 14, badCount, Constants.WORKFLOW_ALT_MEALS_COLUMN));
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
                WorkflowParserV300.MISSING_ALT_TYPE, 14, Constants.WORKFLOW_TYPE_MEAL_COLUMN));
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
                "16", unknownMealType, ControlBlockBuilder.DEFAULT_ALT_MEAL_OPTIONS));
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
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.EMPTY_ALT_MEAL, "16", mealType));
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
                WorkflowParserV300.EMPTY_ALT_GROCERY, "16", groceryType));
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
                WorkflowParserV300.INVALID_COUNT_VALUE, "14", badCount, Constants.WORKFLOW_STD_GROCERY_COLUMN));
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
                WorkflowParserV300.INVALID_COUNT_VALUE, 14, badCount, Constants.WORKFLOW_ALT_GROCERY_COLUMN));
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
                WorkflowParserV300.MISSING_ALT_TYPE, 14, Constants.WORKFLOW_TYPE_GROCERY_COLUMN));
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
                "16", unknownGroceryType, ControlBlockBuilder.DEFAULT_ALT_GROCERY_OPTIONS));
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
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.MISSING_CONSUMER_NAME, "14"));
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
                WorkflowParserV300.MISSING_CONSUMER_USER_NAME, "14"));
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
                WorkflowParserV300.MISSING_CITY, "14"));
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
                WorkflowParserV300.MISSING_ADDRESS, "14"));
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
                WorkflowParserV300.MISSING_PHONE, "14"));
    }

    private void auditControlBlock(ControlBlock controlBlock) {
        ((ControlBlockV300) controlBlock).audit(users, List.of());
    }
}