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
    public static final String MISSING_GROCERY_PICKUP =
            "Line {0}, driver {1}, grocery delivery from {2} but no pickup.\n";

    public static final String INVALID_COUNT_VALUE =
            "Line {0}, \"{1}\" is not a valid value for the \"{2}\" column.\n";

    public static final String MISSING_ALT_TYPE =
            "Line {0}, the \"{1}\" column is empty. "
            + "Please insert the correct alternate type (e.g. veg).\n";

    public static final String ALT_MEAL_MISMATCH =
            "Line {0}, delivery alt meal type \"{1}\" does not match any of the options defined "
            + "in the control block " + Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS + ": \"{2}\".\n";

    public static final String EMPTY_ALT_MEAL =
            "Line {0}, delivery has alt meal type \"{1}\" but control block "
            + Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS + " is empty.\n";

    public static final String ALT_GROCERY_MISMATCH =
            "Line {0}, delivery alt grocery type \"{1}\" does not match any of the options defined "
                    + "in the " + Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS + ": \"{2}\".\n";

    public static final String EMPTY_ALT_GROCERY =
            "Line {0}, delivery has alt grocery type \"{1}\" but control block "
                    + Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS + " is empty.\n";

    public static final String MISSING_CONSUMER_NAME = "Line {0}, missing consumer name.\n";
    public static final String MISSING_CONSUMER_USER_NAME = "Line {0}, missing consumer user name.\n";
    public static final String MISSING_PHONE = "Line {0}, missing primary phone number.\n";
    public static final String MISSING_CITY = "Line {0}, missing city.\n";
    public static final String MISSING_ADDRESS = "Line {0}, missing address.\n";

    public static final String DUPLICATE_PICKUP =
            "Restaurant {0} appears more than once for driver {1}.\n";

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
            StringBuilder errors = new StringBuilder();

            String consumerName = bean.getName();
            if (consumerName.isEmpty()) {
                errors.append(MessageFormat.format(MISSING_CONSUMER_NAME, lineNumber));
            }
            String userName = bean.getUserName();
            if (userName.isEmpty()) {
                errors.append(MessageFormat.format(MISSING_CONSUMER_USER_NAME, lineNumber));
            }
            String phone = bean.getPhone();
            if (phone.isEmpty()) {
                errors.append(MessageFormat.format(MISSING_PHONE, lineNumber));
            }
            String altPhone = bean.getAltPhone();
            String neighborhood = bean.getNeighborhood();
            String city = bean.getCity();
            if (city.isEmpty()) {
                errors.append(MessageFormat.format(MISSING_CITY, lineNumber));
            }
            String address = bean.getAddress();
            if (address.isEmpty()) {
                errors.append(MessageFormat.format(MISSING_ADDRESS, lineNumber));
            }
            boolean isCondo = Boolean.parseBoolean(bean.getCondo());
            String details = bean.getDetails();

            String stdMeals = getIntegerValue(bean.getStdMeals());
            String altMeals = getIntegerValue(bean.getAltMeals());
            String typeMeal = bean.getTypeMeal().trim();
            String stdGrocery = getIntegerValue(bean.getStdGrocery());
            String altGrocery = getIntegerValue(bean.getAltGrocery());
            String typeGrocery = bean.getTypeGrocery().trim();

            validIntegerValue(stdMeals, Constants.WORKFLOW_STD_MEALS_COLUMN, errors);
            parseAuditAltMeals(altMeals, typeMeal, errors);
            validIntegerValue(stdGrocery, Constants.WORKFLOW_STD_GROCERY_COLUMN, errors);
            parseAuditAltGrocery(altGrocery, typeGrocery, errors);

            if (! errors.toString().isEmpty()) {
                throw new MemberDataException(errors.toString());
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
            delivery.setStdMeals(stdMeals);
            delivery.setAltMeals(altMeals);
            delivery.setTypeMeal(typeMeal.isEmpty() ? "" : typeMeal);
            delivery.setStdGrocery(stdGrocery);
            delivery.setAltGrocery(altGrocery);
            delivery.setTypeGrocery(typeGrocery.isEmpty() ? "" : typeGrocery);

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

            if (pickups.contains(restaurantName)) {
                throw new MemberDataException(MessageFormat.format(
                        DUPLICATE_PICKUP, restaurantName, driver.getUserName()));
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

    @Override
    void versionSpecificAudit(Driver driver) {

        ControlBlockV300 controlBlockV300 = (ControlBlockV300) controlBlock;

        List<String> altMealTypes = controlBlockV300.getAltMealOptions();
        List<String> altGroceryTypes = controlBlockV300.getAltGroceryOptions();
        String errors = "";

        for (DeliveryV300 delivery : ((DriverV300)driver).getDeliveriesV300()) {
            if (! delivery.getAltMeals().equals("0")) {

                if (altMealTypes == null) {
//                    errors += MessageFormat.format(ALT_MEAL_OPTIONS_NOT_DEFINED,
                    //                           lineNumber, delivery.getTypeMeal(), String.join(", ", altMealTypes));
                } else if (altMealTypes.isEmpty()) {
                    errors += MessageFormat.format(EMPTY_ALT_MEAL, lineNumber, delivery.getTypeMeal());
                } else if (! altMealTypes.contains(delivery.getTypeMeal())) {
                    errors += MessageFormat.format(ALT_MEAL_MISMATCH,
                            lineNumber, delivery.getTypeMeal(), String.join(", ", altMealTypes));
                }
            }
            if (! delivery.getAltGrocery().equals("0")) {

                if (altGroceryTypes == null) {

                } else if (altGroceryTypes.isEmpty()) {
                    errors += MessageFormat.format(EMPTY_ALT_GROCERY, lineNumber, delivery.getTypeGrocery());
                } else if (! altGroceryTypes.contains(delivery.getTypeGrocery())) {
                    errors += MessageFormat.format(ALT_GROCERY_MISMATCH,
                            lineNumber, delivery.getTypeGrocery(), String.join(", ", altGroceryTypes));
                }
            }
        }

        if (! errors.isEmpty()) {
            throw new MemberDataException(errors);
        }
    }

    private boolean validIntegerValue(String value, String columnName, StringBuilder errors) {

        assert ! value.isEmpty();

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            errors.append(MessageFormat.format(INVALID_COUNT_VALUE, lineNumber, value, columnName));
            return false;
        }

        return true;
    }

    private void parseAuditAltMeals(String altMeals, String typeMeal, StringBuilder errors) {

        assert ! altMeals.isEmpty();

        if (altMeals.equals("0")) {
            return;
        }

        if (! validIntegerValue(altMeals, Constants.WORKFLOW_ALT_MEALS_COLUMN, errors)) {
            return;
        }

        if (typeMeal.isEmpty()) {
            errors.append(MessageFormat.format(
                    MISSING_ALT_TYPE, lineNumber, Constants.WORKFLOW_TYPE_MEAL_COLUMN));
        }
    }

    private void parseAuditAltGrocery(String altGrocery, String typeGrocery, StringBuilder errors) {

        assert ! altGrocery.isEmpty();

        if (altGrocery.equals("0")) {
            return;
        }

        if (! validIntegerValue(altGrocery, Constants.WORKFLOW_ALT_GROCERY_COLUMN, errors)) {
            return;
        }

        if (typeGrocery.isEmpty()) {
            errors.append(MessageFormat.format(
                    MISSING_ALT_TYPE, lineNumber, Constants.WORKFLOW_TYPE_GROCERY_COLUMN));
        }
    }
}
