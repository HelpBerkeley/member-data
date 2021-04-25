/*
 * Copyright (c) 2020-2021 helpberkeley.org
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

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;

public class WorkflowParserV300 extends WorkflowParser {

    public static final String EMPTY_DELIVERY = "Line {0}, Driver {1} delivering nothing to {2}.\n";
    public static final String MISSING_MEAL_PICKUP = "Line {0}, driver {1}, meal delivery from {2} but no pickup.\n";
    public static final String MISSING_GROCERY_PICKUP = "Line {0}, driver {1}, grocery delivery from {2} but no pickup.\n";

    public WorkflowParserV300(Mode mode, final String csvData) {
        super(mode, csvData);
    }

    @Override
    protected List<WorkflowBean> parse(String csvData) {
        return new CsvToBeanBuilder<WorkflowBean>(
                new StringReader(csvData)).withType(WorkflowBeanV300.class).build().parse();
    }

    /**
     * Check for missing columns.
     * @param csvData Normalized workflow spreadsheet data
     * @throws MemberDataException If there are any missing columns.
     */
    @Override
    protected void auditColumnNames(final String csvData) {
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
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_PHONE_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_USER_NAME_COLUMN,
                Constants.WORKFLOW_STD_MEALS_COLUMN,
                Constants.WORKFLOW_ALT_MEALS_COLUMN,
                Constants.WORKFLOW_TYPE_MEAL_COLUMN,
                Constants.WORKFLOW_STD_GROCERY_COLUMN,
                Constants.WORKFLOW_ALT_GROCERY_COLUMN,
                Constants.WORKFLOW_TYPE_GROCERY_COLUMN);

        // get the header line
        String header = csvData.substring(0, csvData.indexOf('\n'));

        String[] columns = header.split(",");
        Set<String> set = new HashSet<>(Arrays.asList(columns));

        int numErrors = 0;
        StringBuilder errors = new StringBuilder();
        for (String columnName : columnNames) {
            if (! set.contains(columnName)) {
                errors.append("Missing column header: ").append(columnName).append("\n");
                numErrors++;
            }
        }

        if (errors.length() > 0) {
            throw new MemberDataException(errors.toString());
        }
    }

    @Override
    List<Delivery> processDeliveries() {
        List<Delivery> deliveries = new ArrayList<>();
        WorkflowBeanV300 bean;

        while ((bean = (WorkflowBeanV300)peekNextRow()) != null) {
            if (!bean.getConsumer().equalsIgnoreCase("TRUE")) {
                break;
            }

            bean = (WorkflowBeanV300) nextRow();
            assert bean != null;
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
                // FIX THIS, DS: resolve multi-pickup restaurant names
                restaurantName = "";
//                errors += "missing restaurant name\n";
            }
            String stdMeals = bean.getStdMeals().trim();
            String altMeals = bean.getAltMeals().trim();
            String typeMeal = bean.getTypeMeal().trim();
            String stdGrocery = bean.getStdGrocery().trim();
            String altGrocery = bean.getAltGrocery().trim();
            String typeGrocery = bean.getTypeGrocery().trim();

            if (stdMeals.isEmpty()) {
                errors += Constants.WORKFLOW_STD_MEALS_COLUMN + " column is empty. ";
                errors += "Please insert the the correct number(s) (e.g. 0).\n";
            }
            if (altMeals.isEmpty()) {
                errors += Constants.WORKFLOW_ALT_MEALS_COLUMN + " column is empty. ";
                errors += "Please insert the the correct number(s) (e.g. 0).\n";
            } else if (! altMeals.equals("0")) {
                if (typeMeal.isEmpty()) {
                    errors += Constants.WORKFLOW_TYPE_MEAL_COLUMN + " column is empty. ";
                    errors += "Please insert the the correct alternate grocery type (e.g. veg).\n";
                } else if (typeMeal.equals(Constants.ALT_TYPE_NONE)) {
                    errors += Constants.ALT_TYPE_NONE + " is invalid for the ";
                    errors += Constants.WORKFLOW_TYPE_MEAL_COLUMN + " column when ";
                    errors += Constants.WORKFLOW_ALT_MEALS_COLUMN + " is not 0. ";
                    errors += "Please insert a valid alt meal type.\n";
                }
            }
            if (stdGrocery.isEmpty()) {
                errors += Constants.WORKFLOW_STD_GROCERY_COLUMN + " column is empty. ";
                errors += "Please insert the the correct number(s) (e.g. 0).\n";
            }
            if (altGrocery.isEmpty()) {
                errors += Constants.WORKFLOW_ALT_GROCERY_COLUMN + " column is empty. ";
                errors += "Please insert the the correct number(s) (e.g. 0).\n";
            } else if (! altGrocery.equals("0")) {
                if (typeGrocery.isEmpty()) {
                    errors += Constants.WORKFLOW_TYPE_GROCERY_COLUMN + " column is empty. ";
                    errors += "Please insert the the correct alternate grocery type (e.g. veg).\n";
                } else if (typeGrocery.equals(Constants.ALT_TYPE_NONE)) {
                    errors += Constants.ALT_TYPE_NONE + " is invalid for the ";
                    errors += Constants.WORKFLOW_TYPE_GROCERY_COLUMN + " column when ";
                    errors += Constants.WORKFLOW_ALT_GROCERY_COLUMN + " is not 0. ";
                    errors += "Please insert a valid alt grocery type.\n";
                }
            }

            if (! errors.isEmpty()) {
                throw new MemberDataException("line " + lineNumber + " " + errors);
            }

            DeliveryV300 delivery = new DeliveryV300(consumerName, lineNumber);
            delivery.setUserName(userName);
            delivery.setPhone(phone);
            delivery.setAltPhone(altPhone);
            delivery.setNeighborhood(neighborhood);
            delivery.setCity(city);
            delivery.setAddress(address);
            delivery.setIsCondo(isCondo);
            delivery.setDetails(details);
            delivery.setRestaurant(restaurantName);
            delivery.setStdMeals(stdMeals.isEmpty() ? "0" : stdMeals);
            delivery.setAltMeals(altMeals.isEmpty() ? "0" : altMeals);
            delivery.setTypeMeal(typeMeal.isEmpty() ? "none" : typeMeal);
            delivery.setStdGrocery(stdGrocery.isEmpty() ? "0" : stdGrocery);
            delivery.setAltGrocery(altGrocery.isEmpty() ? "0" : altGrocery);
            delivery.setTypeGrocery(typeGrocery.isEmpty() ? "none" : typeGrocery);

            deliveries.add(delivery);
        }

        return deliveries;
    }

    @Override
    void auditPickupDeliveryMismatch(Driver driver) {

        // Build a Set of pickup restaurants

        Set<String> pickups = new HashSet<>();
        for (Restaurant restaurant : driver.getPickups()) {
            String restaurantName = restaurant.getName();

            // FIX THIS, DS: too late to audit this here?
            if (pickups.contains(restaurantName)) {
                throw new MemberDataException("Restaurant " + restaurantName
                        + " appears more than once for driver " + driver.getUserName());
            }

            pickups.add(restaurantName);
        }

        StringBuilder errors = new StringBuilder();

        assert controlBlock instanceof ControlBlockV300;
        ControlBlockV300 controlBlockV300 = (ControlBlockV300) controlBlock;

        // Audit deliveries
        for (Delivery delivery : driver.getDeliveries()) {
            DeliveryV300 deliveryV300 = (DeliveryV300) delivery;

            // Delivery without any items?
            if (deliveryV300.getStdMeals().equals("0")
                    && deliveryV300.getAltMeals().equals("0")
                    && deliveryV300.getStdGrocery().equals("0")
                    && deliveryV300.getAltGrocery().equals("0")) {

                driver.addWarning(MessageFormat.format(EMPTY_DELIVERY,
                        delivery.getLineNumber(), driver.getName(), delivery.getName()));
            }

            // Check that if we have meals, that the meal source location is in the pickups
            if (! (deliveryV300.getStdMeals().equals("0") && deliveryV300.getAltMeals().equals("0"))) {
                if (! pickups.contains(controlBlockV300.getMealSource())) {
                    errors.append(MessageFormat.format(MISSING_MEAL_PICKUP,
                            delivery.getLineNumber(), driver, controlBlockV300.getMealSource()));
                }
            }

            // Check that if we have meals, that the meal source location is in the pickups
            if (! (deliveryV300.getStdGrocery().equals("0") && deliveryV300.getAltGrocery().equals("0"))) {
                if (!pickups.contains(controlBlockV300.getGrocerySource())) {
                    errors.append(MessageFormat.format(MISSING_GROCERY_PICKUP,
                            delivery.getLineNumber(), driver, controlBlockV300.getMealSource()));
                }
            }
        }

        if (errors.length() > 0) {
            throw new MemberDataException(errors.toString());
        }
    }
}
