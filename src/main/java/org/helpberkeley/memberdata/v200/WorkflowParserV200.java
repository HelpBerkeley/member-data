/*
 * Copyright (c) 2020-2024 helpberkeley.org
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

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import org.helpberkeley.memberdata.*;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;

public class WorkflowParserV200 extends WorkflowParser {

    public WorkflowParserV200(final String csvData) throws IOException, CsvException {
        super(csvData);
    }

    @Override
    protected List<WorkflowBean> parse(String csvData) {
        try {
            return new CsvToBeanBuilder<WorkflowBean>(
                    new StringReader(csvData)).withType(WorkflowBeanV200.class).build().parse();
        } catch (RuntimeException ex) {
            throw new MemberDataException(ex);
        }
    }

    /**
     * Check for missing columns.
     * @param csvData Normalized workflow spreadsheet data
     * @throws MemberDataException If there are any missing columns.
     */
    @Override
    protected void auditColumnNames(final String csvData) throws IOException, CsvException {
        List<String> columnNames = List.of(
                Constants.WORKFLOW_ADDRESS_COLUMN,
                Constants.WORKFLOW_ALT_PHONE_COLUMN,
                Constants.WORKFLOW_CITY_COLUMN,
                Constants.WORKFLOW_CONDO_COLUMN,
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_NAME_COLUMN,
                Constants.WORKFLOW_NEIGHBORHOOD_COLUMN,
                Constants.WORKFLOW_NORMAL_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_PHONE_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_USER_NAME_COLUMN,
                Constants.WORKFLOW_VEGGIE_COLUMN);

        CSVListReader csvReader = new CSVListReader(new StringReader(csvData));
        List<String> headerColumns = csvReader.readNextToList();
        csvReader.close();

        Set<String> set = new HashSet<>(headerColumns);

        int numErrors = 0;
        StringBuilder errors = new StringBuilder();
        for (String columnName : columnNames) {
            if (! set.contains(columnName)) {
                errors.append(MessageFormat.format(ERROR_MISSING_HEADER_COLUMN, columnName)).append("\n");
                numErrors++;
            }
        }

        if (errors.length() > 0) {
            throw new MemberDataException(errors.toString());
        }

    }

    @Override
    public Delivery processDelivery(WorkflowBean bean) {

        String errors = "";

        String consumerName = bean.getName();
        if (consumerName.isEmpty()) {
            errors += "missing consumer name\n";
        }
        String userName = bean.getUserName();
        if (userName.isEmpty()) {
            errors += "missing user name\n";
        }
        String phone = bean.getPhone();
        if (phone.isEmpty()) {
            errors += "missing phone\n";
        }
        String altPhone = bean.getAltPhone();
        String neighborhood = bean.getNeighborhood();
        String city = bean.getCity();
        if (city.isEmpty()) {
            errors += "missing city\n";
        }
        String address = bean.getAddress();
        if (address.isEmpty()) {
            errors += "missing address\n";
        }
        boolean isCondo = Boolean.parseBoolean(bean.getCondo());
        String details = bean.getDetails();
        String restaurantName = bean.getRestaurant();
        if (restaurantName.isEmpty()) {
            errors += "missing restaurant name\n";
        }
        String normalRations = bean.getNormal();
        String veggieRations = bean.getVeggie();

        if (normalRations.isEmpty() || veggieRations.isEmpty()) {
            errors += "normal and/or veggie rations column is empty. "
                    + "Please insert the the correct number(s) (e.g. 0). ";
        }

        if (! errors.isEmpty()) {
            throw new MemberDataException("line " + lineNumber + " " + errors);
        }

        DeliveryV200 delivery = new DeliveryV200(consumerName, lineNumber);
        delivery.setUserName(userName);
        delivery.setPhone(phone);
        delivery.setAltPhone(altPhone);
        delivery.setNeighborhood(neighborhood);
        delivery.setCity(city);
        delivery.setAddress(address);
        delivery.setIsCondo(isCondo);
        delivery.setDetails(details);
        delivery.setRestaurant(restaurantName);
        delivery.setNormalRations(normalRations.isEmpty() ? "0" : normalRations);
        delivery.setVeggieRations(veggieRations.isEmpty() ? "0" : veggieRations);

        return delivery;
    }

    @Override
    protected void auditPickupDeliveryMismatch(Driver driver) {

        // First build of map of deliveries (orders) per restaurant
        Map<String, Long> deliveryOrders = new HashMap<>();
        for (Delivery delivery : driver.getDeliveries()) {
            DeliveryV200 deliveryV200 = (DeliveryV200) delivery;
            String restaurantName = deliveryV200.getRestaurant();

            // Support for 0 order delivery (e.g. donation drop-off)
            if (deliveryV200.getNormalRations().equals("0") &&
                    deliveryV200.getVeggieRations().equals("0")) {
                continue;
            }

            Long orders = deliveryOrders.getOrDefault(restaurantName, 0L);
            orders++;
            deliveryOrders.put(restaurantName, orders);
        }

        // Now build a map of orders to pickup per restaurant
        Map<String, Long> pickupOrders = new HashMap<>();
        for (Restaurant restaurant : driver.getPickups()) {
            String restaurantName = restaurant.getName();

            // FIX THIS, DS: too late to audit this here?
            if (pickupOrders.containsKey(restaurantName)) {
                throw new MemberDataException("Restaurant " + restaurantName
                        + " appears more than once for driver " + driver.getUserName());
            }

            pickupOrders.put(restaurantName, ((RestaurantV200)restaurant).getOrders());
        }

        StringBuilder errors = new StringBuilder();

        // Now check that the pickups match the deliveries
        for (String restaurant : pickupOrders.keySet()) {

            if (pickupOrders.get(restaurant) == 0L) {
                if (deliveryOrders.containsKey(restaurant)){
                    errors.append("deliveries for ").append(restaurant).append(", but 0 orders\n");
                }
            } else if (! deliveryOrders.containsKey(restaurant)) {
                errors.append("orders for ").append(restaurant).append(" but no deliveries\n");
            } else if (! deliveryOrders.get(restaurant).equals(pickupOrders.get(restaurant))) {
                errors.append(pickupOrders.get(restaurant)).append(" orders for ").append(restaurant).append(" but ").append(deliveryOrders.get(restaurant)).append(" deliveries\n");
            }
        }

        // And that each delivery order has a pickup
        for (String restaurant : deliveryOrders.keySet()) {
            if (! pickupOrders.containsKey(restaurant)) {
                errors.append(deliveryOrders.get(restaurant)).append(" deliveries for ").append(restaurant).append(" but no orders\n");
            }
        }

        if (errors.length() > 0) {
            throw new MemberDataException("Driver " + driver.getUserName() + ": " + errors);
        }
    }

    @Override
    protected void auditDeliveryBeforePickup(Driver driver) {
        StringBuilder errors = new StringBuilder();

        for (DeliveryV200 delivery : (List<DeliveryV200>)(List<? extends Delivery>)driver.getDeliveries()) {

            if ((delivery.getNormalRations().isEmpty() || delivery.getNormalRations().equals("0"))
                && (delivery.getVeggieRations().isEmpty() || delivery.getVeggieRations().equals("0"))) {
                continue;
            }

            String restaurantName = delivery.getRestaurant();
            Restaurant restaurant = driver.getPickup(restaurantName);
            if (delivery.getLineNumber() < restaurant.getLineNumber()) {
                errors.append(MessageFormat.format(ERROR_DELIVERY_BEFORE_PICKUP, driver.getUserName(),
                        delivery.getName(), delivery.getLineNumber(),
                        restaurantName, restaurant.getLineNumber()));
            }
        }

        if (errors.length() > 0) {
            throw new MemberDataException(errors.toString());
        }
    }

    @Override
    protected void versionSpecificAudit(Driver driver) {
    }
}
