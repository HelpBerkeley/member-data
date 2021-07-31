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
package org.helpberkeley.memberdata.v200;

import org.helpberkeley.memberdata.*;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.helpberkeley.memberdata.v200.BuilderConstants.*;

public class WorkflowHBParserTest extends WorkflowHBParserBaseTest {

    private final String controlBlockData = HEADER
            + CONTROL_BLOCK_BEGIN_ROW
            + CONTROL_BLOCK_VERSION_ROW
            + CONTROL_BLOCK_END_ROW
            + EMPTY_ROW;

    private final ControlBlock controlBlock = ControlBlock.create(controlBlockData);

    private final Map<String, Restaurant> restaurants =
            Map.of("Cafe Raj", Restaurant.createRestaurant(controlBlock, "Cafe Raj", 1),
                    "Bopshop", Restaurant.createRestaurant(controlBlock, "Bopshop", 2),
                    "Kim's Cafe", Restaurant.createRestaurant(controlBlock, "Kim's Cafe", 3));

    public WorkflowHBParserTest() {
        for (Restaurant restaurant : restaurants.values()) {
            restaurant.setStartTime("5:00");
            restaurant.setClosingTime("7:00");
        }
    }

    @Override
    public List<String> getColumnNames() {
        return List.of(
                Constants.WORKFLOW_ADDRESS_COLUMN,
                Constants.WORKFLOW_ALT_PHONE_COLUMN,
                Constants.WORKFLOW_CITY_COLUMN,
                Constants.WORKFLOW_CONDO_COLUMN,
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_NAME_COLUMN,
                Constants.WORKFLOW_NORMAL_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_PHONE_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_USER_NAME_COLUMN,
                Constants.WORKFLOW_VEGGIE_COLUMN);
    }

    @Override
    public String getMinimumControlBlock() {
        return "FALSE,FALSE," + Constants.CONTROL_BLOCK_BEGIN + ",,,,,,,,,,,,\n" +
                "FALSE,FALSE,,Version,,,,2-0-0,,,,,,,\n" +
                "FALSE,FALSE," + Constants.CONTROL_BLOCK_END + ",,,,,,,,,,,,\n";
    }

    @Test
    public void splitRestaurantsTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-with-split-restaurant.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);

        List<Driver> drivers = driverPostFormat.getDrivers();

        Map<String, Restaurant> restaurants = driverPostFormat.getRestaurants();
        assertThat(restaurants).hasSize(11);
        assertThat(restaurants.get("Cafe Raj").getDrivers()).hasSize(2);
        assertThat(restaurants.get("Cafe Raj").getDrivers()).containsKey("jcDriver");
        assertThat(restaurants.get("Cafe Raj").getDrivers()).containsKey("jsDriver");

        assertThat(drivers).hasSize(4);
        Driver driver = drivers.get(0);
        assertThat(driver.getUserName()).isEqualTo("jbDriver");

        driver = drivers.get(1);
        assertThat(driver.getUserName()).isEqualTo("jsDriver");

        driver = drivers.get(2);
        assertThat(driver.getUserName()).isEqualTo("jcDriver");

        driver = drivers.get(3);
        assertThat(driver.getUserName()).isEqualTo("jdDriver");

        List<String> posts = driverPostFormat.generateDriverPosts();
        String groupPost = driverPostFormat.generateGroupInstructionsPost();

        // FIX THIS, DS: add validation
    }

    @Test
    public void deliveryErrorsTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-delivery-errors.csv");
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("missing consumer name",
                "missing user name", "missing phone", "missing city", "missing address",
                "missing restaurant name", "normal and/or veggie rations column is empty");
    }

    @Test
    public void restaurantErrorsTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-restaurant-errors.csv");
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("missing restaurant name",
                "missing address", "missing orders");
    }

    @Test
    public void driverErrorsTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-driver-errors.csv");
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("missing driver user name",
                "missing driver phone number");
    }

    @Test
    public void missingGMapURLTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-missing-gmap-url.csv");
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("missing gmap URL");
    }

    @Test
    public void emptyGMapURLTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-empty-gmap-url.csv");
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("empty gmap URL");
    }

//    @Test
//    public void missingHeaderRowTest() {
//        String routedDeliveries = readResourceFile("routed-deliveries-missing-header.csv");
//        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessage(ControlBlock.BAD_HEADER_ROW);
//    }

    @Test
    public void pickupsAndDeliveriesMismatchTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-order-mismatch.csv");
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "orders for Talavera but no deliveries");
        assertThat(thrown).hasMessageContaining(
                "1 orders for Sweet Basil but 2 deliveries");
        assertThat(thrown).hasMessageContaining(
                "2 orders for Bopshop but 1 deliveries");
        assertThat(thrown).hasMessageContaining(
                "1 deliveries for Kim's Cafe but no orders");
    }

    @Test
    public void pickupsAndDeliveriesEmptyDeliveryMismatchTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-order-mismatch-empty-delivery.csv");
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "orders for Talavera but no deliveries");
        assertThat(thrown).hasMessageContaining(
                "1 orders for Sweet Basil but 2 deliveries");
        assertThat(thrown).hasMessageContaining(
                "2 orders for Bopshop but 1 deliveries");
        assertThat(thrown).hasMessageContaining(
                "1 deliveries for Kim's Cafe but no orders");
    }

    @Test
    public void duplicateDriverDuplicateRestaurantsTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-duplicate-driver.csv");
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Duplicate driver \"jbDriver\" at line 34");
    }

    @Test
    public void singleDriverSingleDeliveryTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder().withOrders("1"));
        driverBlock.withDelivery(new DeliveryBuilder().withNormalMeals("1"));
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
        assertThat(itinerary.get(0)).isInstanceOf(RestaurantV200.class);
        RestaurantV200 restaurant = (RestaurantV200)itinerary.get(0);
        assertThat(restaurant.getName()).isEqualTo(DEFAULT_RESTAURANT_NAME);
        assertThat(restaurant.getOrders()).isEqualTo(1);

        assertThat(itinerary.get(1).getType()).isEqualTo(ItineraryStopType.DELIVERY);
        assertThat(itinerary.get(1)).isInstanceOf(DeliveryV200.class);
        DeliveryV200 delivery = (DeliveryV200) itinerary.get(1);
        assertThat(delivery.getName()).isEqualTo(DEFAULT_CONSUMER_NAME);
        assertThat(delivery.getUserName()).isEqualTo(DEFAULT_CONSUMER_USER_NAME);
        assertThat(delivery.getNormalRations()).isEqualTo("1");
        assertThat(delivery.getVeggieRations()).isEqualTo("0");
    }

    @Test
    public void deliveryBeforePickupTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withDelivery(new DeliveryBuilder().withNormalMeals("1"));
        driverBlock.withRestaurant(new RestaurantBuilder().withOrders("1"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        Throwable thrown = catchThrowable(parser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(WorkflowParser.ERROR_DELIVERY_BEFORE_PICKUP,
                DEFAULT_DRIVER_USER_NAME, DEFAULT_CONSUMER_NAME, 8, DEFAULT_RESTAURANT_NAME, 9));
    }

    @Test
    public void emptyPickupsNoDeliveriesTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder().withOrders("0"));
        driverBlock.withRestaurant(new RestaurantBuilder().withName("Bopshop").withOrders("0"));
        driverBlock.withRestaurant(new RestaurantBuilder().withName("Kim's Cafe").withOrders("0"));
        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        WorkflowParser parser = WorkflowParser.create(restaurants, workflowBuilder.build());
        auditControlBlock(parser.controlBlock());
        List<Driver> drivers = parser.drivers();
        assertThat(drivers).hasSize(1);
        Driver driver = drivers.get(0);
        assertThat(driver.getWarningMessages()).isEmpty();

        List<ItineraryStop> itinerary = driver.getItinerary();
        assertThat(itinerary).hasSize(3);

        assertThat(itinerary.get(0).getType()).isEqualTo(ItineraryStopType.PICKUP);
        assertThat(itinerary.get(0)).isInstanceOf(RestaurantV200.class);
        RestaurantV200 restaurant = (RestaurantV200)itinerary.get(0);
        assertThat(restaurant.getName()).isEqualTo(DEFAULT_RESTAURANT_NAME);
        assertThat(restaurant.getOrders()).isEqualTo(0);

        assertThat(itinerary.get(1).getType()).isEqualTo(ItineraryStopType.PICKUP);
        assertThat(itinerary.get(1)).isInstanceOf(RestaurantV200.class);
        restaurant = (RestaurantV200)itinerary.get(1);
        assertThat(restaurant.getName()).isEqualTo("Bopshop");
        assertThat(restaurant.getOrders()).isEqualTo(0);

        assertThat(itinerary.get(2).getType()).isEqualTo(ItineraryStopType.PICKUP);
        assertThat(itinerary.get(2)).isInstanceOf(RestaurantV200.class);
        restaurant = (RestaurantV200)itinerary.get(2);
        assertThat(restaurant.getName()).isEqualTo("Kim's Cafe");
        assertThat(restaurant.getOrders()).isEqualTo(0);
    }

    private void auditControlBlock(ControlBlock controlBlock) {
        ((ControlBlockV200) controlBlock).audit(users, restaurants, List.of());
    }
}
