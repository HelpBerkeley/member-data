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

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class WorkflowParser {

    enum Mode {
        /** The CSV data is for passing to the routing software */
        DRIVER_ROUTE_REQUEST,
        /** The CSV data is for driver message generation */
        DRIVER_MESSAGE_REQUEST,
    }


    private final Mode mode;
    private final CSVReaderHeaderAware csvReader;
    private final List<Driver> drivers = new ArrayList<>();

    WorkflowParser(Mode mode, final String csvData) throws IOException {
        this.mode = mode;
        // Normalize EOL
        String normalizedData = csvData.replaceAll("\\r\\n?", "\n");
        assert ! csvData.isEmpty() : "empty workflow";
        csvReader = new CSVReaderHeaderAware(new StringReader(normalizedData));
        auditColumnNames(normalizedData);
    }

    private void auditColumnNames(final String csvData) {
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

        // get the header line
        String header = csvData.substring(0, csvData.indexOf('\n'));

        String[] columns = header.split(",");
        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList(columns));

        String errors = "";
        for (String columnName : columnNames) {
            if (! set.contains(columnName)) {
                errors += "Missing column header: " + columnName + "\n";
            }
        }

        if (! errors.isEmpty()) {
            throw new MemberDataException(errors);
        }
    }

    List<Driver> drivers() throws IOException, CsvValidationException {

        List<Driver> drivers = new ArrayList<>();
        Map<String, String> rowMap;

        while ((rowMap = csvReader.readMap()) != null) {

            if (isControlBlockBeginRow(rowMap)) {
                processControlBlock();
                continue;
            }

            if (! isDriverRow(rowMap)) {
                throw new MemberDataException("line " + csvReader.getLinesRead() + " is not a driver row. "
                    + "Is this a driver who is also a consumer? If so, the consumer column must be set to false.");
            }

            Driver driver = processDriver(rowMap);
            auditPickupDeliveryMismatch(driver);
            drivers.add(driver);
        }

        return drivers;
    }

    /**
     * The first row of a control block looks like:
     *     FALSE,FALSE,ControlBegin,,,,,,,,,,,,,
     * @return Whether or not the row is the beginning of a control block.
     */
    private boolean isControlBlockBeginRow(final Map<String, String> rowMap) {

        String consumerValue = rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN);
        assert consumerValue != null;

        String driverValue = rowMap.get(Constants.WORKFLOW_DRIVER_COLUMN);
        assert driverValue != null;

        String directive = rowMap.get(Constants.CONTROL_BLOCK_DIRECTIVE_COLUMN);
        assert directive != null;

        return (! Boolean.parseBoolean(consumerValue.trim()))
            && (! Boolean.parseBoolean(driverValue.trim()))
            && (directive.trim().equals(Constants.CONTROL_BLOCK_BEGIN));
    }

    /**
     * The final row of a control block looks like:
     *     FALSE,FALSE,ControlEnd,,,,,,,,,,,,,
     * @return Whether or not the row is the end of a control block.
     */
    private boolean isControlBlockEndRow(final Map<String, String> rowMap) {

        String consumerValue = rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN);
        assert consumerValue != null;

        String driverValue = rowMap.get(Constants.WORKFLOW_DRIVER_COLUMN);
        assert driverValue != null;

        String directive = rowMap.get(Constants.CONTROL_BLOCK_DIRECTIVE_COLUMN);
        assert directive != null;

        return (! Boolean.parseBoolean(consumerValue.trim()))
                && (! Boolean.parseBoolean(driverValue.trim()))
                && (directive.trim().equals(Constants.CONTROL_BLOCK_END));
    }

    private void processControlBlock() throws IOException, CsvValidationException {
        Map<String, String> rowMap;

        ControlBlock controlBlock = new ControlBlock();

        while ((rowMap = csvReader.readMap()) != null) {

            auditControlBlockRow(rowMap);

            if (isControlBlockEndRow(rowMap)) {
                break;
            } else if (ignoreControlBlockRow(rowMap)) {
                continue;
            }

            String key = rowMap.get(Constants.CONTROL_BLOCK_KEY_COLUMN);
            String value = rowMap.get(Constants.CONTROL_BLOCK_VALUE_COLUMN);

            controlBlock.processRow(key, value, csvReader.getLinesRead());
        }
    }

    private void auditControlBlockRow(Map<String, String> rowMap) {
        String errors = "";

        for (String columnName : List.of(Constants.WORKFLOW_CONSUMER_COLUMN, Constants.WORKFLOW_DRIVER_COLUMN)) {

            String value = rowMap.get(columnName);

            if (value == null) {
                errors += "Missing value control block " + columnName
                        + " column at line " + csvReader.getLinesRead() + ".\n";
            } else if (! value.toLowerCase().equals("false")) {
                errors += "Control block " + columnName
                        + " column does not contain FALSE, at line " + csvReader.getLinesRead() + ".\n";
            }
        }

        String directive = rowMap.get(Constants.WORKFLOW_NAME_COLUMN).trim();

        switch (directive) {
            case "":
            case Constants.CONTROL_BLOCK_COMMENT:
            case Constants.CONTROL_BLOCK_END:
                break;
            default:
                errors += "Unexpected control block directive \"" + rowMap.get(Constants.WORKFLOW_NAME_COLUMN)
                    + "\" in " + Constants.WORKFLOW_NAME_COLUMN + " column at line " + csvReader.getLinesRead()
                    + ".\n";
        }

        if (! errors.isEmpty()) {
            throw new MemberDataException(errors);
        }
    }

    private boolean ignoreControlBlockRow(Map<String, String> rowMap) {

        String directive = rowMap.get(Constants.WORKFLOW_NAME_COLUMN).trim();

        if (directive.equals(Constants.CONTROL_BLOCK_COMMENT)) {
            return true;
        }

        return (directive.isEmpty()
            && rowMap.get(Constants.WORKFLOW_USER_NAME_COLUMN).isEmpty()
            && rowMap.get(Constants.WORKFLOW_NEIGHBORHOOD_COLUMN).trim().isEmpty());
    }

    /**
     * Is the passed in row of a restaurant route entry?
     * @param rowMap Map of column names and values.
     * @return Whether or not the row is a restaurant route entry.
     */
    private boolean isDriverRow(final Map<String, String> rowMap) {
        //
        // The columns we look at in a driver row look like:
        //    FALSE,TRUE,,jbDriver,,,,,,,,,,,
        //
        return (! Boolean.parseBoolean(rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN)))
                && Boolean.parseBoolean(rowMap.get(Constants.WORKFLOW_DRIVER_COLUMN));
    }
    
    Driver processDriver(Map<String, String> rowMap) throws IOException, CsvValidationException {

        String errors = "";

        String driverUserName = rowMap.get(Constants.WORKFLOW_USER_NAME_COLUMN);
        if (driverUserName.isEmpty()) {
            errors += "missing driver user name\n";
        }
        String driverPhone = rowMap.get(Constants.WORKFLOW_PHONE_COLUMN);
        if (driverPhone.isEmpty()) {
            errors += "missing driver phone number\n";
        }

        if (! errors.isEmpty()) {
            throw new MemberDataException("line " + csvReader.getLinesRead() + " " + errors);
        }

        // Read 1 or more restaurant rows. Example:
        // FALSE,,,,,,,,"1561 Solano Ave, Berkeley",FALSE,,Talavera,,,0
        //
        List<Restaurant> restaurants = processRestaurants();
        List<Delivery> deliveries = processDeliveries();

        rowMap = csvReader.readMap();
        if (! isDriverRow(rowMap)) {
            throw new MemberDataException("line " + csvReader.getLinesRead() + " is not a driver row. "
                    + "Is this a driver who is also a consumer? If so, the consumer column must be set to false.");
        }
        if (! driverUserName.equals(rowMap.get(Constants.WORKFLOW_USER_NAME_COLUMN))) {
            throw new MemberDataException(driverUserName + ", line "
                    + csvReader.getLinesRead() + ", mismatch driver end name");
        }

        Driver driver;

        rowMap = csvReader.readMap();

        if (mode == Mode.DRIVER_MESSAGE_REQUEST) {
            if (rowMap == null) {
                throw new MemberDataException("Driver " + driverUserName
                        + " missing gmap URL after line " + csvReader.getLinesRead());
            }

            String gmapURL = rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN);
            if (gmapURL.isEmpty()) {
                throw new MemberDataException("Line " + csvReader.getLinesRead()
                        + ", driver " + driverUserName + " empty gmap URL");
            }
            if (!gmapURL.contains("https://")) {
                throw new MemberDataException("Driver " + driverUserName + " unrecognizable gmap URL");
            }

            driver = new Driver(driverUserName, driverPhone,
                    restaurants, deliveries, gmapURL);
        } else {
            assert mode == Mode.DRIVER_ROUTE_REQUEST;

            // This can be either an empty row, marking boundary between this driver and the next,
            // Or the end of file.

            if ((rowMap != null) && (! emptyRow(rowMap))) {
                throw new MemberDataException("Line " + csvReader.getLinesRead() + " is not empty");
            }

            driver = new Driver(driverUserName, driverPhone,
                    restaurants, deliveries);
        }

        return driver;
    }

    List<Restaurant> processRestaurants() throws IOException, CsvValidationException {

        List<Restaurant> restaurants = new ArrayList<>();

        String[] columns;
        while ((columns = csvReader.peek()) != null) {
            if (! columns[0].toUpperCase().equals("FALSE")) {
                break;
            }

            Map<String, String> rowMap = csvReader.readMap();
            String errors = "";

            String restaurantName = rowMap.get(Constants.WORKFLOW_RESTAURANTS_COLUMN);
            if (restaurantName.isEmpty()) {
                errors += "missing restaurant name\n";
            }
            String address = rowMap.get(Constants.WORKFLOW_ADDRESS_COLUMN);
            if (address.isEmpty()) {
                errors += "missing address\n";
            }
            String details = rowMap.get(Constants.WORKFLOW_DETAILS_COLUMN);
            String orders = rowMap.get(Constants.WORKFLOW_ORDERS_COLUMN);
            if (orders.isEmpty()) {
                errors += "missing orders";
            }

            if (! errors.isEmpty()) {
                throw new MemberDataException("line " + csvReader.getLinesRead() + " " + errors);
            }

            Restaurant restaurant = new Restaurant(restaurantName);
            restaurant.setAddress(address);
            restaurant.setDetails(details);
            restaurant.setOrders(orders);

            restaurants.add(restaurant);
        }

        return restaurants;
    }

    List<Delivery> processDeliveries() throws IOException, CsvValidationException {
        List<Delivery> deliveries = new ArrayList<>();

        String[] columns;
        while ((columns = csvReader.peek()) != null) {
            if (! columns[0].toUpperCase().equals("TRUE")) {
                break;
            }

            Map<String, String> rowMap = csvReader.readMap();
            String errors = "";

            String consumerName = rowMap.get(Constants.WORKFLOW_NAME_COLUMN);
            if (consumerName.isEmpty()) {
                errors += "missing consumer name\n";
            }
            String userName = rowMap.get(Constants.WORKFLOW_USER_NAME_COLUMN);
            if (userName.isEmpty()) {
                errors += "missing user name\n";
            }
            String phone = rowMap.get(Constants.WORKFLOW_PHONE_COLUMN);
            if (phone.isEmpty()) {
                errors += "missing phone\n";
            }
            String altPhone = rowMap.get(Constants.WORKFLOW_ALT_PHONE_COLUMN);
            String city = rowMap.get(Constants.WORKFLOW_CITY_COLUMN);
            if (city.isEmpty()) {
                errors += "missing city\n";
            }
            String address = rowMap.get(Constants.WORKFLOW_ADDRESS_COLUMN);
            if (address.isEmpty()) {
                errors += "missing address\n";
            }
            boolean isCondo = Boolean.parseBoolean(rowMap.get(Constants.WORKFLOW_CONDO_COLUMN));
            String details = rowMap.get(Constants.WORKFLOW_DETAILS_COLUMN);
            String restaurantName = rowMap.get(Constants.WORKFLOW_RESTAURANTS_COLUMN);
            if (restaurantName.isEmpty()) {
                errors += "missing restaurant name\n";
            }
            String normalRations = rowMap.get(Constants.WORKFLOW_NORMAL_COLUMN);
            String veggieRations = rowMap.get(Constants.WORKFLOW_VEGGIE_COLUMN);
            if (normalRations.isEmpty() && veggieRations.isEmpty()) {
                errors += "no rations detected\n";
            }

            if (! errors.isEmpty()) {
                throw new MemberDataException("line " + csvReader.getLinesRead() + " " + errors);
            }

            Delivery delivery = new Delivery(consumerName);
            delivery.setUserName(userName);
            delivery.setPhone(phone);
            delivery.setAltPhone(altPhone);
            delivery.setCity(city);
            delivery.setAddress(address);
            delivery.setIsCondo(isCondo);
            delivery.setDetails(details);
            delivery.setRestaurant(restaurantName);
            delivery.setNormalRations(normalRations);
            delivery.setVeggieRations(veggieRations);

            deliveries.add(delivery);
        }

        return deliveries;
    }

    private void auditPickupDeliveryMismatch(Driver driver) {

        // First build of map of deliveries (orders) per restaurant
        Map<String, Long> deliveryOrders = new HashMap<>();
        for (Delivery delivery : driver.getDeliveries()) {
            String restaurantName = delivery.getRestaurant();

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

            pickupOrders.put(restaurantName, restaurant.getOrders());
        }

        String errors = "";

        // Now check that the pickups match the deliveries
        for (String restaurant : pickupOrders.keySet()) {

            if (pickupOrders.get(restaurant) == 0L) {
                if (deliveryOrders.containsKey(restaurant)){
                    errors += "deliveries for " + restaurant + ", but 0 orders\n";
                }
            } else if (! deliveryOrders.containsKey(restaurant)) {
                errors += "orders for " + restaurant + " but no deliveries\n";
            } else if (! deliveryOrders.get(restaurant).equals(pickupOrders.get(restaurant))) {
                errors += pickupOrders.get(restaurant) + " orders for " + restaurant
                        + " but " + deliveryOrders.get(restaurant) + " deliveries\n";
            }
        }

        // And that each delivery order has a pickup
        for (String restaurant : deliveryOrders.keySet()) {
            if (! pickupOrders.containsKey(restaurant)) {
                errors += deliveryOrders.get(restaurant) + " deliveries for " + restaurant + " but no orders\n";
            }
        }

        if (! errors.isEmpty()) {
            throw new MemberDataException("Driver " + driver.getUserName() + ": " + errors);
        }
    }

    private boolean emptyRow(Map<String, String> rowMap) {
        for (String value : rowMap.values()) {
            if (! value.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
