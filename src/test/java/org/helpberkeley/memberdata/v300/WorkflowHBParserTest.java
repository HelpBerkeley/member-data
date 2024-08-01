/*
 * Copyright (c) 2021-2024. helpberkeley.org
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

import com.opencsv.exceptions.CsvException;
import org.helpberkeley.memberdata.*;
import org.junit.Test;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.helpberkeley.memberdata.v300.BuilderConstants.*;

public class WorkflowHBParserTest extends WorkflowHBParserBaseTest {

    private final String controlBlockData = ControlBlockTest.HEADER
            + ControlBlockTest.CONTROL_BLOCK_BEGIN_ROW
            + ControlBlockTest.CONTROL_BLOCK_VERSION_ROW
            + ControlBlockTest.CONTROL_BLOCK_END_ROW
            + ControlBlockTest.EMPTY_ROW;

    private final ControlBlock controlBlock = ControlBlock.create(controlBlockData);

    private final Map<String, Restaurant> restaurants =
            Map.of("RevFoodTruck", Restaurant.createRestaurant(controlBlock, "RevFoodTruck", 1),
                    "BFN", Restaurant.createRestaurant(controlBlock, "BFN", 2));

    public WorkflowHBParserTest() throws IOException, CsvException {
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

    @Override
    public String getHeader() throws IOException, CsvException {
        return new ControlBlockTest().getHeader();
    }

    @Test
    public void badValueStandardMealsTest() throws IOException, CsvException {
        String badCount = "NaN";

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withStdMeals(badCount);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.INVALID_COUNT_VALUE, 15, badCount, Constants.WORKFLOW_STD_MEALS_COLUMN));
    }

    @Test
    public void badValueAltMealsTest() throws IOException, CsvException {
        String badCount = "mauvaise valeur";

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltMeals(badCount);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.INVALID_COUNT_VALUE, 15, badCount, Constants.WORKFLOW_ALT_MEALS_COLUMN));
    }

    @Test
    public void missingMealTypeTest() throws IOException, CsvException {
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltMeals("1");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_ALT_TYPE, 15, Constants.WORKFLOW_TYPE_MEAL_COLUMN));
    }

    @Test
    public void mismatchMealTypeTest() throws IOException, CsvException {
        String unknownMealType = "ginger sling with a pineapple heart";
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltMeals("1").withTypeMeal(unknownMealType);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.ALT_MEAL_MISMATCH,
                17, unknownMealType, ControlBlockBuilder.DEFAULT_ALT_MEAL_OPTIONS));
    }

    @Test
    public void emptyAltMealTest() throws IOException, CsvException {
        String mealType = "coffee only";
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltMeals("1").withTypeMeal(mealType);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withAltMealOptions(""));

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.EMPTY_ALT_MEAL, 17, mealType));
    }

    @Test
    public void emptyAltGroceryTest() throws IOException, CsvException {
        String groceryType = "kosher";
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltGrocery("1").withTypeGrocery(groceryType);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder().withName("BFN"));
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withAltGroceryOptions(""));

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.EMPTY_ALT_GROCERY, 17, groceryType));
    }


    @Test
    public void badValueStandardGroceryTest() throws IOException, CsvException {
        String badCount = "Also NaN";

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withStdGrocery(badCount);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.INVALID_COUNT_VALUE, 15, badCount, Constants.WORKFLOW_STD_GROCERY_COLUMN));
    }

    @Test
    public void badValueAltGroceryTest() throws IOException, CsvException {
        String badCount = "encore une mauvaise valeur";

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltGrocery(badCount);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.INVALID_COUNT_VALUE, 15, badCount, Constants.WORKFLOW_ALT_GROCERY_COLUMN));
    }

    @Test
    public void missingGroceryTypeTest() throws IOException, CsvException {

        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltGrocery("1");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_ALT_TYPE, 15, Constants.WORKFLOW_TYPE_GROCERY_COLUMN));
    }

    @Test
    public void mismatchGroceryTypeTest() throws IOException, CsvException {
        String unknownGroceryType = "blue foods";
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withAltGrocery("1").withTypeGrocery(unknownGroceryType);
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder().withName("BFN"));
        driverBlock.withDelivery(delivery);
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.ALT_GROCERY_MISMATCH,
                17, unknownGroceryType, ControlBlockBuilder.DEFAULT_ALT_GROCERY_OPTIONS));
    }

    @Test
    public void missingConsumerNameTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withConsumerName(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.MISSING_CONSUMER_NAME, 15));
    }

    @Test
    public void missingConsumerUserNameTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withConsumerUserName(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_CONSUMER_USER_NAME, 15));
    }

    @Test
    public void missingCityTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withCity(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_CITY, 15));
    }

    @Test
    public void missingAddressTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withAddress(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_ADDRESS, 15));
    }

    @Test
    public void missingPhoneTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withPhone(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());

        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                WorkflowParserV300.MISSING_PHONE, 15));
    }

    @Test
    public void emptyGrocerySourceTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withPhone(""));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withFoodSources("RevFoodTruck|"));

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        assertThat(parser.controlBlock().getWarnings()).contains(ControlBlockV300.EMPTY_GROCERY_SOURCE);
    }

    @Test
    public void emptyMealSourceTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withStdGrocery("1"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withFoodSources("|BFN"));

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        assertThat(parser.controlBlock().getWarnings()).contains(ControlBlockV300.EMPTY_MEAL_SOURCE);
    }

    @Test
    public void unknownPickupManagerTest() throws IOException, CsvException {
        String notAMember = "Fred";
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withPickupManager(notAMember));

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        Throwable thrown = catchThrowable(() -> auditControlBlock(parser.controlBlock()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.UNKNOWN_PICKUP_MANAGER, notAMember));
    }

    @Test
    public void duplicatePickupRestaurantTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.DUPLICATE_PICKUP,
                DEFAULT_RESTAURANT_NAME, DEFAULT_DRIVER_USER_NAME));
    }

    @Test
    public void emptyDeliveryTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withStdMeals("0"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        List<Driver> drivers = parser.drivers();
        assertThat(drivers).hasSize(1);
        assertThat(drivers.get(0).getWarningMessages()).containsExactly(MessageFormat.format(
                WorkflowParserV300.EMPTY_DELIVERY, 15,
                DEFAULT_DRIVER_NAME, DEFAULT_CONSUMER_NAME));
    }

    @Test
    public void mixedPickupsAndDeliveriesTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withStdMeals("1"));
        driverBlock.withRestaurant(new RestaurantBuilder().withName("BFN"));
        driverBlock.withDelivery(new DeliveryBuilder().withStdGrocery("1"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        List<Driver> drivers = parser.drivers();
        assertThat(drivers.get(0).getWarningMessages()).isEmpty();
        assertThat(drivers).hasSize(1);
        Driver driver = drivers.get(0);

        List<ItineraryStop> itinerary = driver.getItinerary();
        assertThat(itinerary).hasSize(4);

        assertThat(itinerary.get(0)).isInstanceOf(RestaurantV300.class);
        RestaurantV300 restaurant = (RestaurantV300) itinerary.get(0);
        assertThat(restaurant.getName()).isEqualTo(DEFAULT_RESTAURANT_NAME);

        assertThat(itinerary.get(1)).isInstanceOf(DeliveryV300.class);
        DeliveryV300 delivery = (DeliveryV300)itinerary.get(1);
        assertThat(delivery.getName()).isEqualTo(DEFAULT_CONSUMER_NAME);
        assertThat(delivery.getUserName()).isEqualTo(DEFAULT_CONSUMER_USER_NAME);

        assertThat(itinerary.get(2)).isInstanceOf(RestaurantV300.class);
        restaurant = (RestaurantV300) itinerary.get(2);
        assertThat(restaurant.getName()).isEqualTo("BFN");

        assertThat(itinerary.get(3)).isInstanceOf(DeliveryV300.class);
        delivery = (DeliveryV300)itinerary.get(3);
        assertThat(delivery.getName()).isEqualTo(DEFAULT_CONSUMER_NAME);
        assertThat(delivery.getUserName()).isEqualTo(DEFAULT_CONSUMER_USER_NAME);
    }

    @Test
    public void deliveryBeforePickupTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withDelivery(new DeliveryBuilder().withStdMeals("1"));
        driverBlock.withRestaurant(new RestaurantBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParser.ERROR_DELIVERY_BEFORE_PICKUP,
                DEFAULT_DRIVER_USER_NAME, DEFAULT_CONSUMER_NAME, 14, DEFAULT_RESTAURANT_NAME, 15));
    }

    @Test
    public void emptyPickupsNoDeliveriesTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withRestaurant(new RestaurantBuilder().withName("BFN"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        List<Driver> drivers = parser.drivers();
        assertThat(drivers).hasSize(1);
        Driver driver = drivers.get(0);
        assertThat(driver.getWarningMessages()).isEmpty();

        List<ItineraryStop> itinerary = driver.getItinerary();
        assertThat(itinerary).hasSize(2);

        assertThat(itinerary.get(0).getType()).isEqualTo(ItineraryStopType.PICKUP);
        assertThat(itinerary.get(0)).isInstanceOf(RestaurantV300.class);
        RestaurantV300 restaurant = (RestaurantV300)itinerary.get(0);
        assertThat(restaurant.getName()).isEqualTo(DEFAULT_RESTAURANT_NAME);

        assertThat(itinerary.get(1).getType()).isEqualTo(ItineraryStopType.PICKUP);
        assertThat(itinerary.get(1)).isInstanceOf(RestaurantV300.class);
        restaurant = (RestaurantV300)itinerary.get(1);
        assertThat(restaurant.getName()).isEqualTo("BFN");
    }

    @Test
    public void missingMealSourceTest() throws IOException, CsvException {
        ControlBlockBuilder controlBlock = new ControlBlockBuilder()
                .withFoodSources("|BFN");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withDelivery(new DeliveryBuilder().withStdMeals("1"));
        driverBlock.withRestaurant(new RestaurantBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder()
                .withControlBlock(controlBlock)
                .withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.MISSING_MEAL_SOURCE,
                14, DEFAULT_DRIVER_NAME, DEFAULT_CONSUMER_NAME));
    }

    @Test
    public void missingGrocerySourceTest() throws IOException, CsvException {
        ControlBlockBuilder controlBlock = new ControlBlockBuilder()
                .withFoodSources("RevFoodTruck|");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withDelivery(new DeliveryBuilder().withStdGrocery("1"));
        driverBlock.withRestaurant(new RestaurantBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder()
                .withControlBlock(controlBlock)
                .withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParserV300.MISSING_GROCERY_SOURCE,
                14, DEFAULT_DRIVER_NAME, DEFAULT_CONSUMER_NAME));
    }

    @Test
    public void stdMealCountOnPickupRowTest() throws IOException, CsvException {
        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        RestaurantBuilder restaurantBuilder = new RestaurantBuilder()
                .withStdMealCount("1");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder()
                .withRestaurant(restaurantBuilder)
                .withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder()
                .withControlBlock(controlBlock)
                .withDriverBlock(driverBlock);
        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_STD_MEALS_COLUMN));
    }

    @Test
    public void altMealCountOnPickupRowTest() throws IOException, CsvException {
        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        RestaurantBuilder restaurantBuilder = new RestaurantBuilder()
                .withAltMealCount("1");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder()
                .withRestaurant(restaurantBuilder)
                .withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder()
                .withControlBlock(controlBlock)
                .withDriverBlock(driverBlock);
        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_ALT_MEALS_COLUMN));
    }

    @Test
    public void altMealTypeOnPickupRowTest() throws IOException, CsvException {
        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        RestaurantBuilder restaurantBuilder = new RestaurantBuilder()
                .withAltMealType("a ginger sling with a pineapple heart");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder()
                .withRestaurant(restaurantBuilder)
                .withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder()
                .withControlBlock(controlBlock)
                .withDriverBlock(driverBlock);
        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_TYPE_MEAL_COLUMN));
    }

    @Test
    public void stdGroceryCountOnPickupRowTest() throws IOException, CsvException {
        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        RestaurantBuilder restaurantBuilder = new RestaurantBuilder()
                .withStdGroceryCount("1");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder()
                .withRestaurant(restaurantBuilder)
                .withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder()
                .withControlBlock(controlBlock)
                .withDriverBlock(driverBlock);
        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_STD_GROCERY_COLUMN));
    }

    @Test
    public void altGroceryCountOnPickupRowTest() throws IOException, CsvException {
        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        RestaurantBuilder restaurantBuilder = new RestaurantBuilder()
                .withAltGroceryCount("1");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder()
                .withRestaurant(restaurantBuilder)
                .withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder()
                .withControlBlock(controlBlock)
                .withDriverBlock(driverBlock);
        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_ALT_GROCERY_COLUMN));
    }

    @Test
    public void altGroceryTypeOnPickupRowTest() throws IOException, CsvException {
        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        RestaurantBuilder restaurantBuilder = new RestaurantBuilder()
                .withAltGroceryType("inorganic");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder()
                .withRestaurant(restaurantBuilder)
                .withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder()
                .withControlBlock(controlBlock)
                .withDriverBlock(driverBlock);
        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_TYPE_GROCERY_COLUMN));
    }

    @Test
    public void allTypeAndCountErrorsOnPickupRowTest() throws IOException, CsvException {
        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        RestaurantBuilder restaurantBuilder = new RestaurantBuilder()
                .withStdMealCount("1")
                .withAltMealCount("1")
                .withAltMealType("invisi-food")
                .withStdGroceryCount("1")
                .withAltGroceryCount("1")
                .withAltGroceryType("blue foods only");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder()
                .withRestaurant(restaurantBuilder)
                .withDelivery(new DeliveryBuilder());
        WorkflowBuilder workflowBuilder = new WorkflowBuilder()
                .withControlBlock(controlBlock)
                .withDriverBlock(driverBlock);
        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_STD_MEALS_COLUMN));
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_ALT_MEALS_COLUMN));
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_TYPE_MEAL_COLUMN));
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_STD_GROCERY_COLUMN));
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_ALT_GROCERY_COLUMN));
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                RestaurantV300.ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_TYPE_GROCERY_COLUMN));

    }

    @Test
    public void driverBooleanWhitespaceTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withStdMeals("1"));
        driverBlock.withRestaurant(new RestaurantBuilder().withName("BFN"));
        driverBlock.withDelivery(new DeliveryBuilder().withStdGrocery("1"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        List<Driver> drivers = parser.drivers();
        assertThat(drivers.get(0).getWarningMessages()).isEmpty();
        assertThat(drivers).hasSize(1);
        Driver driver = drivers.get(0);

        List<ItineraryStop> itinerary = driver.getItinerary();
        assertThat(itinerary).hasSize(4);

        assertThat(itinerary.get(0)).isInstanceOf(RestaurantV300.class);
        RestaurantV300 restaurant = (RestaurantV300) itinerary.get(0);
        assertThat(restaurant.getName()).isEqualTo(DEFAULT_RESTAURANT_NAME);

        assertThat(itinerary.get(1)).isInstanceOf(DeliveryV300.class);
        DeliveryV300 delivery = (DeliveryV300)itinerary.get(1);
        assertThat(delivery.getName()).isEqualTo(DEFAULT_CONSUMER_NAME);
        assertThat(delivery.getUserName()).isEqualTo(DEFAULT_CONSUMER_USER_NAME);

        assertThat(itinerary.get(2)).isInstanceOf(RestaurantV300.class);
        restaurant = (RestaurantV300) itinerary.get(2);
        assertThat(restaurant.getName()).isEqualTo("BFN");

        assertThat(itinerary.get(3)).isInstanceOf(DeliveryV300.class);
        delivery = (DeliveryV300)itinerary.get(3);
        assertThat(delivery.getName()).isEqualTo(DEFAULT_CONSUMER_NAME);
        assertThat(delivery.getUserName()).isEqualTo(DEFAULT_CONSUMER_USER_NAME);
    }

    @Test
    public void consumerBooleanWhitespaceTest() throws IOException, CsvException {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withStdMeals("1"));
        driverBlock.withRestaurant(new RestaurantBuilder().withName("BFN"));
        driverBlock.withDelivery(new DeliveryBuilder().withIsConsumer("TRUE ").withStdGrocery("1"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        List<Driver> drivers = parser.drivers();
        assertThat(drivers.get(0).getWarningMessages()).isEmpty();
        assertThat(drivers).hasSize(1);
        Driver driver = drivers.get(0);

        List<ItineraryStop> itinerary = driver.getItinerary();
        assertThat(itinerary).hasSize(4);

        assertThat(itinerary.get(0)).isInstanceOf(RestaurantV300.class);
        RestaurantV300 restaurant = (RestaurantV300) itinerary.get(0);
        assertThat(restaurant.getName()).isEqualTo(DEFAULT_RESTAURANT_NAME);

        assertThat(itinerary.get(1)).isInstanceOf(DeliveryV300.class);
        DeliveryV300 delivery = (DeliveryV300)itinerary.get(1);
        assertThat(delivery.getName()).isEqualTo(DEFAULT_CONSUMER_NAME);
        assertThat(delivery.getUserName()).isEqualTo(DEFAULT_CONSUMER_USER_NAME);

        assertThat(itinerary.get(2)).isInstanceOf(RestaurantV300.class);
        restaurant = (RestaurantV300) itinerary.get(2);
        assertThat(restaurant.getName()).isEqualTo("BFN");

        assertThat(itinerary.get(3)).isInstanceOf(DeliveryV300.class);
        delivery = (DeliveryV300)itinerary.get(3);
        assertThat(delivery.getName()).isEqualTo(DEFAULT_CONSUMER_NAME);
        assertThat(delivery.getUserName()).isEqualTo(DEFAULT_CONSUMER_USER_NAME);
    }

    private void auditControlBlock(ControlBlock controlBlock) {
        ((ControlBlockV300) controlBlock).audit(users, List.of());
    }
}
