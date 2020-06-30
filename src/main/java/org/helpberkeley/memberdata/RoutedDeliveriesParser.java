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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoutedDeliveriesParser {


    private final CSVReaderHeaderAware csvReader;
    private final List<Driver> drivers = new ArrayList<>();

    RoutedDeliveriesParser(final String csvData) throws IOException {
        // Normalize EOL
        String normalizedData = csvData.replaceAll("\\r\\n?", "\n");
        assert ! csvData.isEmpty() : "empty restaurant template";
        csvReader = new CSVReaderHeaderAware(new StringReader(normalizedData));
        auditColumnNames();
    }

    private void auditColumnNames() {
        // FIX THIS, DS: implement
    }

    List<Driver> drivers() throws IOException, CsvValidationException {

        List<Driver> drivers = new ArrayList<>();
        Map<String, String> rowMap;

        while ((rowMap = csvReader.readMap()) != null) {

            if (isControlBlockRow(rowMap)) {
                processControlBlock();
            }

            if (! isDriverRow(rowMap)) {
                throw new MemberDataException("line " + csvReader.getLinesRead() + " is not a driver row");
            }
            drivers.add(processDriver(rowMap));
        }

        return drivers;
    }

    private boolean isControlBlockRow(final Map<String, String> rowMap) {

        return (! Boolean.parseBoolean(rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN)))
            && (! Boolean.parseBoolean(rowMap.get(Constants.WORKFLOW_DRIVER_COLUMN)))
            && (rowMap.get(Constants.WORKFLOW_NAME_COLUMN).equals("CONTROL"));
    }

    private void processControlBlock() throws IOException, CsvValidationException {
        Map<String, String> rowMap;

        while ((rowMap = csvReader.readMap()) != null) {
            if (! isControlBlockRow(rowMap)) {
                continue;
            }
        }
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
            throw new MemberDataException("line " + csvReader.getLinesRead() + " is not a driver row");
        }
        if (! driverUserName.equals(rowMap.get(Constants.WORKFLOW_USER_NAME_COLUMN))) {
            throw new MemberDataException(driverUserName + ", line "
                    + csvReader.getLinesRead() + ", mismatch driver end name");
        }

        rowMap = csvReader.readMap();
        if (rowMap == null) {
            throw new MemberDataException("Driver " + driverUserName
                    + " missing gmap URL after line " + csvReader.getLinesRead());
        }

        String gmapURL = rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN);
        if (gmapURL.isEmpty()) {
            throw new MemberDataException("Line " + csvReader.getLinesRead()
                    + ", driver " + driverUserName + " empty gmap URL");
        }
        if (! gmapURL.contains("https://")) {
            throw new MemberDataException("Driver " + driverUserName + " unrecognizable gmap URL");
        }

        return new Driver(driverUserName, driverPhone, restaurants, deliveries, gmapURL);
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
}
