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
import org.junit.Test;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class RoutedDeliveriesTest extends TestBase {

    private final Map<String, User> users;

    public RoutedDeliveriesTest() {
        Loader loader = new Loader(createApiSimulator());
        users = new Tables(loader.load()).mapByUserName();
    }

    @Test
    public void singleDriverTest() {
        String csvData = readResourceFile("routed-deliveries-v300.csv");

        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, csvData);
        List<Driver> drivers = driverPostFormat.getDrivers();

        assertThat(drivers).hasSize(1);
        Driver driver = drivers.get(0);
        assertThat(driver.getUserName()).isEqualTo("jbDriver");
        assertThat(driver.hasCondo()).isTrue();
        assertThat(driver.getgMapURL()).isEqualTo("https://www.google.com/maps/dir/something+something+else");

        List<Restaurant> pickups = driver.getPickups();
        assertThat(pickups).hasSize(1);

        Restaurant restaurant = pickups.get(0);
        assertThat(restaurant.getName()).isEqualTo("Bauman Meals/Groceries");
        assertThat(restaurant.getAddress()).isEqualTo("1955 Ninth St., Berkeley, CA");
        assertThat(restaurant.getDetails()).isEqualTo(
                "Come from Hearst, park alongside E side of street past loading dock");

        List<Delivery> deliveries = driver.getDeliveries();
        assertThat(deliveries).hasSize(7);

        Delivery delivery = deliveries.get(0);
        assertThat(delivery.getName()).isEqualTo("Cust Name 1");
        assertThat(delivery.getUserName()).isEqualTo("Cust1");
        assertThat(delivery.getPhone()).isEqualTo("555-555-1112");
        assertThat(delivery.getAltPhone()).isEqualTo("111-222-3333");
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("123 456th Ave");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        DeliveryV300 deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("noRed");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(1);
        assertThat(delivery.getName()).isEqualTo("Cust Name 2");
        assertThat(delivery.getUserName()).isEqualTo("Cust2");
        assertThat(delivery.getPhone()).isEqualTo("555-555-2222");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("77 77th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(2);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(0);
        assertThat(deliveryV300.getTypeMeal()).isEmpty();
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(2);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(2);
        assertThat(delivery.getName()).isEqualTo("Cust Name 3");
        assertThat(delivery.getUserName()).isEqualTo("Cust3");
        assertThat(delivery.getPhone()).isEqualTo("555-555-3333");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("11 11th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(0);
        assertThat(deliveryV300.getTypeMeal()).isEmpty();
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(2);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(3);
        assertThat(delivery.getName()).isEqualTo("Cust Name 4");
        assertThat(delivery.getUserName()).isEqualTo("Cust4");
        assertThat(delivery.getPhone()).isEqualTo("555-555-4444");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("44 44th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("veggie");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(1);
        assertThat(deliveryV300.getTypeGrocery()).isEqualTo("veg");

        delivery = deliveries.get(4);
        assertThat(delivery.getName()).isEqualTo("Cust Name 5");
        assertThat(delivery.getUserName()).isEqualTo("Cust5");
        assertThat(delivery.getPhone()).isEqualTo("555-555-5555");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("55 55th St");
        assertThat(delivery.isCondo()).isTrue();
        assertThat(delivery.getDetails()).isEqualTo("listed as a condo but");
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("noPork");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(5);
        assertThat(delivery.getName()).isEqualTo("Cust Name 6");
        assertThat(delivery.getUserName()).isEqualTo("Cust6");
        assertThat(delivery.getPhone()).isEqualTo("555-555-6666");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("66 66th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(0);
        assertThat(deliveryV300.getTypeMeal()).isEmpty();
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(1);
        assertThat(deliveryV300.getTypeGrocery()).isEqualTo("custom pick");

        delivery = deliveries.get(6);
        assertThat(delivery.getName()).isEqualTo("Cust Name 7");
        assertThat(delivery.getUserName()).isEqualTo("Cust7");
        assertThat(delivery.getPhone()).isEqualTo("555-555-7777");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("77 77th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("veggie");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(1);
        assertThat(deliveryV300.getTypeGrocery()).isEqualTo("custom pick");
    }

    @Test
    public void multiDriverTest() {
        String csvData = readResourceFile("routed-deliveries-multi-driver-v300.csv");

        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, csvData);

        List<Driver> drivers = driverPostFormat.getDrivers();

        assertThat(drivers).hasSize(2);
        Driver driver = drivers.get(0);
        assertThat(driver.getUserName()).isEqualTo("jbDriver");
        assertThat(driver.hasCondo()).isFalse();
        assertThat(driver.getgMapURL()).isEqualTo("https://www.google.com/maps/dir/something+something+else");

        List<Restaurant> pickups = driver.getPickups();
        assertThat(pickups).hasSize(1);

        Restaurant restaurant = pickups.get(0);
        assertThat(restaurant.getName()).isEqualTo("Bauman Meals/Groceries");
        assertThat(restaurant.getAddress()).isEqualTo("1955 Ninth St., Berkeley, CA");
        assertThat(restaurant.getDetails()).isEqualTo(
                "Come from Hearst, park alongside E side of street past loading dock");

        List<Delivery> deliveries = driver.getDeliveries();
        assertThat(deliveries).hasSize(4);

        Delivery delivery = deliveries.get(0);
        assertThat(delivery.getName()).isEqualTo("Cust Name 1");
        assertThat(delivery.getUserName()).isEqualTo("Cust1");
        assertThat(delivery.getPhone()).isEqualTo("555-555-1112");
        assertThat(delivery.getAltPhone()).isEqualTo("111-222-3333");
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("123 456th Ave");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        DeliveryV300 deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("noRed");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(1);
        assertThat(delivery.getName()).isEqualTo("Cust Name 2");
        assertThat(delivery.getUserName()).isEqualTo("Cust2");
        assertThat(delivery.getPhone()).isEqualTo("555-555-2222");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("77 77th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(2);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(0);
        assertThat(deliveryV300.getTypeMeal()).isEmpty();
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(2);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(2);
        assertThat(delivery.getName()).isEqualTo("Cust Name 3");
        assertThat(delivery.getUserName()).isEqualTo("Cust3");
        assertThat(delivery.getPhone()).isEqualTo("555-555-3333");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("11 11th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(0);
        assertThat(deliveryV300.getTypeMeal()).isEmpty();
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(2);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(3);
        assertThat(delivery.getName()).isEqualTo("Cust Name 4");
        assertThat(delivery.getUserName()).isEqualTo("Cust4");
        assertThat(delivery.getPhone()).isEqualTo("555-555-4444");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("44 44th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("veggie");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(1);
        assertThat(deliveryV300.getTypeGrocery()).isEqualTo("veg");

        driver = drivers.get(1);
        assertThat(driver.getUserName()).isEqualTo("jsDriver");
        assertThat(driver.hasCondo()).isTrue();
        assertThat(driver.getgMapURL()).isEqualTo("https://www.google.com/maps/dir/x+y+z");

        pickups = driver.getPickups();
        assertThat(pickups).hasSize(1);

        restaurant = pickups.get(0);
        assertThat(restaurant.getName()).isEqualTo("Bauman Meals/Groceries");
        assertThat(restaurant.getAddress()).isEqualTo("1955 Ninth St., Berkeley, CA");
        assertThat(restaurant.getDetails()).isEqualTo(
                "Come from Hearst, park alongside E side of street past loading dock");

        deliveries = driver.getDeliveries();
        assertThat(deliveries).hasSize(3);

        delivery = deliveries.get(0);
        assertThat(delivery.getName()).isEqualTo("Cust Name 5");
        assertThat(delivery.getUserName()).isEqualTo("Cust5");
        assertThat(delivery.getPhone()).isEqualTo("555-555-5555");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("55 55th St");
        assertThat(delivery.isCondo()).isTrue();
        assertThat(delivery.getDetails()).isEqualTo("listed as a condo but");
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("noPork");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(1);
        assertThat(delivery.getName()).isEqualTo("Cust Name 6");
        assertThat(delivery.getUserName()).isEqualTo("Cust6");
        assertThat(delivery.getPhone()).isEqualTo("555-555-6666");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("66 66th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(0);
        assertThat(deliveryV300.getTypeMeal()).isEmpty();
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(1);
        assertThat(deliveryV300.getTypeGrocery()).isEqualTo("custom pick");

        delivery = deliveries.get(2);
        assertThat(delivery.getName()).isEqualTo("Cust Name 7");
        assertThat(delivery.getUserName()).isEqualTo("Cust7");
        assertThat(delivery.getPhone()).isEqualTo("555-555-7777");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("77 77th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("veggie");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(1);
        assertThat(deliveryV300.getTypeGrocery()).isEqualTo("custom pick");
    }

    @Test
    public void missingPickupTest() {
        String csvData = readResourceFile("routed-deliveries-missing-pickup-v300.csv");

        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, csvData));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                WorkflowParserV300.MISSING_MEAL_PICKUP, 24, "jsDriver", "Bauman Meals/Groceries"));
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                WorkflowParserV300.MISSING_GROCERY_PICKUP, 25, "jsDriver", "Bauman Meals/Groceries"));
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                WorkflowParserV300.MISSING_GROCERY_PICKUP, 26, "jsDriver", "Bauman Meals/Groceries"));
        assertThat(thrown).hasMessageContaining(MessageFormat.format(
                WorkflowParserV300.MISSING_MEAL_PICKUP, 26, "jsDriver", "Bauman Meals/Groceries"));
    }

    @Test
    public void singleDriverMultiPickupTest() {
        String csvData = readResourceFile("routed-deliveries-multi-pickup-v300.csv");

        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, csvData);
        List<Driver> drivers = driverPostFormat.getDrivers();

        assertThat(drivers).hasSize(1);
        Driver driver = drivers.get(0);
        assertThat(driver.getUserName()).isEqualTo("jbDriver");
        assertThat(driver.hasCondo()).isTrue();
        assertThat(driver.getgMapURL()).isEqualTo("https://www.google.com/maps/dir/something+something+else");

        List<Restaurant> pickups = driver.getPickups();
        assertThat(pickups).hasSize(2);

        Restaurant restaurant = pickups.get(0);
        assertThat(restaurant.getName()).isEqualTo("RevFoodTruck");
        assertThat(restaurant.getAddress()).isEqualTo("9999 999 St., Berkeley, CA");
        assertThat(restaurant.getDetails()).isEmpty();

        restaurant = pickups.get(1);
        assertThat(restaurant.getName()).isEqualTo("BFN");
        assertThat(restaurant.getAddress()).isEqualTo("1955 Ninth St., Berkeley, CA");
        assertThat(restaurant.getDetails()).isEqualTo(
                "Come from Hearst, park alongside E side of street past loading dock");

        List<Delivery> deliveries = driver.getDeliveries();
        assertThat(deliveries).hasSize(7);

        Delivery delivery = deliveries.get(0);
        assertThat(delivery.getName()).isEqualTo("Cust Name 1");
        assertThat(delivery.getUserName()).isEqualTo("Cust1");
        assertThat(delivery.getPhone()).isEqualTo("555-555-1112");
        assertThat(delivery.getAltPhone()).isEqualTo("111-222-3333");
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("123 456th Ave");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        DeliveryV300 deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("noRed");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(1);
        assertThat(delivery.getName()).isEqualTo("Cust Name 2");
        assertThat(delivery.getUserName()).isEqualTo("Cust2");
        assertThat(delivery.getPhone()).isEqualTo("555-555-2222");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("77 77th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(2);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(0);
        assertThat(deliveryV300.getTypeMeal()).isEmpty();
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(2);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(2);
        assertThat(delivery.getName()).isEqualTo("Cust Name 3");
        assertThat(delivery.getUserName()).isEqualTo("Cust3");
        assertThat(delivery.getPhone()).isEqualTo("555-555-3333");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("11 11th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(0);
        assertThat(deliveryV300.getTypeMeal()).isEmpty();
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(2);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(3);
        assertThat(delivery.getName()).isEqualTo("Cust Name 4");
        assertThat(delivery.getUserName()).isEqualTo("Cust4");
        assertThat(delivery.getPhone()).isEqualTo("555-555-4444");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("44 44th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("veggie");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(1);
        assertThat(deliveryV300.getTypeGrocery()).isEqualTo("veg");

        delivery = deliveries.get(4);
        assertThat(delivery.getName()).isEqualTo("Cust Name 5");
        assertThat(delivery.getUserName()).isEqualTo("Cust5");
        assertThat(delivery.getPhone()).isEqualTo("555-555-5555");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("55 55th St");
        assertThat(delivery.isCondo()).isTrue();
        assertThat(delivery.getDetails()).isEqualTo("listed as a condo but");
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("noPork");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getTypeGrocery()).isEmpty();

        delivery = deliveries.get(5);
        assertThat(delivery.getName()).isEqualTo("Cust Name 6");
        assertThat(delivery.getUserName()).isEqualTo("Cust6");
        assertThat(delivery.getPhone()).isEqualTo("555-555-6666");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("66 66th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(0);
        assertThat(deliveryV300.getTypeMeal()).isEmpty();
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(1);
        assertThat(deliveryV300.getTypeGrocery()).isEqualTo("custom pick");

        delivery = deliveries.get(6);
        assertThat(delivery.getName()).isEqualTo("Cust Name 7");
        assertThat(delivery.getUserName()).isEqualTo("Cust7");
        assertThat(delivery.getPhone()).isEqualTo("555-555-7777");
        assertThat(delivery.getAltPhone()).isEqualTo(Constants.ALT_TYPE_NONE);
        assertThat(delivery.getCity()).isEqualTo("Berkeley");
        assertThat(delivery.getAddress()).isEqualTo("77 77th St");
        assertThat(delivery.isCondo()).isFalse();
        assertThat(delivery.getDetails()).isEmpty();
        assertThat(delivery).isInstanceOf(DeliveryV300.class);
        deliveryV300 = (DeliveryV300) delivery;
        assertThat(deliveryV300.getStdMeals()).isEqualTo(0);
        assertThat(deliveryV300.getAltMeals()).isEqualTo(1);
        assertThat(deliveryV300.getTypeMeal()).isEqualTo("veggie");
        assertThat(deliveryV300.getStdGrocery()).isEqualTo(0);
        assertThat(deliveryV300.getAltGrocery()).isEqualTo(1);
        assertThat(deliveryV300.getTypeGrocery()).isEqualTo("custom pick");
    }

    @Test
    public void invalidStandardMealsTest() {

    }
}