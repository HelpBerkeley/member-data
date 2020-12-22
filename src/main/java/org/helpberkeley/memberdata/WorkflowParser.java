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

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.StringReader;
import java.util.*;

public class WorkflowParser {

    public enum Mode {
        /** The CSV data is for passing to the routing software */
        DRIVER_ROUTE_REQUEST,
        /** The CSV data is for driver message generation */
        DRIVER_MESSAGE_REQUEST,
    }

    private final Mode mode;
    private final ControlBlock controlBlock = new ControlBlock();
    private long lineNumber = 1;
    private final PeekingIterator<WorkflowBean> iterator;
    private final Map<String, Restaurant> globalRestaurants;
    private String normalizedCSVData;

    WorkflowParser(Mode mode, Map<String, Restaurant> globalRestaurants, final String csvData) {
        this.mode = mode;
        assert mode == Mode.DRIVER_MESSAGE_REQUEST : mode;
        this.globalRestaurants = globalRestaurants;
        iterator = initializeIterator(csvData);
    }

    public WorkflowParser(Mode mode, final String csvData) {
        this.mode = mode;
        assert mode == Mode.DRIVER_ROUTE_REQUEST : mode;
        this.globalRestaurants = null;
        iterator = initializeIterator(csvData);
    }

    public String rawControlBlock() {

        StringBuilder lines = new StringBuilder();

        // Copy all lines up to the first driver block
        for (String line : normalizedCSVData.split("\n")) {

            if (line.startsWith("FALSE,TRUE,")) {
                break;
            }

            lines.append(line).append('\n');
        }

        return lines.toString();
    }

    /**
     * Based upon the header row, return an empty row.
     * @return Empty row
     */
    public String emptyRow() {

        int endOfFirstLine = normalizedCSVData.indexOf('\n');
        String row = normalizedCSVData.substring(0, endOfFirstLine);

        return row.replaceAll("[^,]", "") + "\n";
    }

    private PeekingIterator<WorkflowBean> initializeIterator(final String csvData) {
        // Normalize EOL
        normalizedCSVData = csvData.replaceAll("\\r\\n?", "\n");
        assert ! normalizedCSVData.isEmpty() : "empty workflow";
        auditColumnNames(normalizedCSVData);

        List<WorkflowBean> workflowBeans = new CsvToBeanBuilder<WorkflowBean>(new StringReader(normalizedCSVData))
                .withType(WorkflowBean.class).build().parse();

        return Iterators.peekingIterator(workflowBeans.iterator());
    }

    /**
     * Return the bean representation of the next row.
     * Increments current line number
     * @return Next bean, or null if at end.
     */
    private WorkflowBean nextRow() {
        if (iterator.hasNext()) {
            lineNumber++;
            return iterator.next();
        }
        return null;
    }

    /**
     * Return a peek the bean representation of the next row.
     * Does not increment current line number
     * @return Next bean, or null if at end.
     */
    private WorkflowBean peekNextRow() {
        if (! iterator.hasNext()) {
            return null;
        }

        return iterator.peek();
    }

    /**
     * Check for missing columns.
     * @param csvData Normalized workflow spreadsheet data
     * @throws MemberDataException If there are any missing columns.
     */
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
        Set<String> set = new HashSet<>(Arrays.asList(columns));

        int numErrors = 0;
        StringBuilder errors = new StringBuilder();
        for (String columnName : columnNames) {
            if (! set.contains(columnName)) {
                errors.append("Missing column header: ").append(columnName).append("\n");
                numErrors++;
            }
        }

        if (numErrors == columnNames.size()) {
            throw new MemberDataException("All column names missing. Line 1 does not look like a header row");
        }
        if (errors.length() > 0) {
            throw new MemberDataException(errors.toString());
        }
    }

    public List<Driver> drivers() {

        LinkedHashMap<String, Driver> driverMap = new LinkedHashMap<>();
        WorkflowBean bean;

        while ((bean = nextRow()) != null) {

            if (isControlBlockBeginRow(bean)) {
                processControlBlock();
                continue;
            }

            if (bean.isEmpty()) {
                 continue;
            }

            if (! isDriverRow(bean)) {
                throw new MemberDataException("line " + lineNumber + " is not a driver row. "
                    + "Is this a driver who is also a consumer? If so, the consumer column must be set to false.");
            }

            if (driverMap.containsKey(bean.getUserName())) {
                throw new MemberDataException("Duplicate driver \"" + bean.getUserName() + "\" at line " + lineNumber);
            }

            Driver driver = processDriver(bean);
            auditPickupDeliveryMismatch(driver);
            driverMap.put(driver.getUserName(), driver);
        }

        return new ArrayList<>(driverMap.values());
    }

    ControlBlock getControlBlock() {
        return controlBlock;
    }

    ControlBlock controlBlock() {

        controlBlock.clear();

        WorkflowBean bean;

        bean = nextRow();
        if ((bean != null) && isControlBlockBeginRow(bean)) {
            processControlBlock();
        }

        return controlBlock;
    }

    /**
     * The first row of a control block looks like:
     *     FALSE,FALSE,ControlBegin,,,,,,,,,,,,,
     * @param bean WorkflowBean representation of row
     * @return Whether or not the row is the beginning of a control block.
     */
    private boolean isControlBlockBeginRow(WorkflowBean bean) {

        String consumerValue = bean.getConsumer();
        String driverValue = bean.getDriver();
        String directive = bean.getControlBlockDirective();

        return (! Boolean.parseBoolean(consumerValue))
            && (! Boolean.parseBoolean(driverValue))
            && (directive.equals(Constants.CONTROL_BLOCK_BEGIN));
    }

    /**
     * The final row of a control block looks like:
     *     FALSE,FALSE,ControlEnd,,,,,,,,,,,,,
     * @param bean WorkflowBean representation of row
     * @return Whether or not the row is the end of a control block.
     */
    private boolean isControlBlockEndRow(WorkflowBean bean) {

        String consumerValue = bean.getConsumer();
        String driverValue = bean.getDriver();
        String directive = bean.getControlBlockDirective();

        return (! Boolean.parseBoolean(consumerValue))
                && (! Boolean.parseBoolean(driverValue))
                && (directive.equals(Constants.CONTROL_BLOCK_END));
    }

    /**
     * A driver row looks like
     *     FALSE,TRUE,...
     * @param bean WorkflowBean representation of row
     * @return Whether or not the row is a driver row
     */
    private boolean isDriverRow(WorkflowBean bean) {
        String consumerValue = bean.getConsumer();
        String driverValue = bean.getDriver();

        return Boolean.parseBoolean(driverValue) && (! Boolean.parseBoolean(consumerValue));
    }

    private void processControlBlock() {
        WorkflowBean bean;

        while ((bean = nextRow()) != null) {

            auditControlBlockRow(bean);

            if (isControlBlockEndRow(bean)) {
                break;
            } else if (ignoreControlBlockRow(bean)) {
                continue;
            }

            if (mode != Mode.DRIVER_ROUTE_REQUEST) {
                assert mode == Mode.DRIVER_MESSAGE_REQUEST : mode;
                controlBlock.processRow(bean, lineNumber);
            }
        }
    }

    private void auditControlBlockRow(WorkflowBean bean) {
        String errors = "";

        if (! bean.getConsumer().equalsIgnoreCase("false")) {
            errors += "Control block " + Constants.WORKFLOW_CONSUMER_COLUMN
                    + " column does not contain FALSE, at line " + lineNumber + ".\n";
        }
        if (! bean.getDriver().equalsIgnoreCase("false")) {
            errors += "Control block " + Constants.WORKFLOW_DRIVER_COLUMN
                    + " column does not contain FALSE, at line " + lineNumber + ".\n";
        }

        String directive = bean.getControlBlockDirective();

        switch (directive) {
            case "":
            case Constants.CONTROL_BLOCK_COMMENT:
            case Constants.CONTROL_BLOCK_END:
                break;
            default:
                errors += "Unexpected control block directive \"" + directive
                    + "\" in " + Constants.WORKFLOW_NAME_COLUMN + " column at line " + lineNumber
                    + ".\n";
        }

        if (! errors.isEmpty()) {
            throw new MemberDataException(errors);
        }
    }

    // FIX THIS, DS: move to ControlBlock.  Call by processRow
    private boolean ignoreControlBlockRow(WorkflowBean bean) {

        String directive = bean.getControlBlockDirective();

        if (directive.equals(Constants.CONTROL_BLOCK_COMMENT)) {
            return true;
        }

        return directive.isEmpty()
                && bean.getControlBlockKey().isEmpty()
                && bean.getControlBlockValue().isEmpty();
    }

    private Driver processDriver(WorkflowBean driverBean) {

        String errors = "";

        String driverUserName = driverBean.getUserName();
        if (driverUserName.isEmpty()) {
            errors += "missing driver user name\n";
        }
        String driverPhone = driverBean.getPhone();
        if (driverPhone.isEmpty()) {
            errors += "missing driver phone number\n";
        }
        String driverAddress = driverBean.getAddress();
        if (driverAddress.isEmpty()) {
            errors += "missing driver address\n";
        }
        String driverCity = driverBean.getCity();
        if (driverCity.isEmpty()) {
            errors += "missing driver city\n";
        }

        if (! errors.isEmpty()) {
            throw new MemberDataException("line " + lineNumber + " " + errors);
        }

        // Read 1 or more restaurant rows. Example:
        // FALSE,,,,,,,,"1561 Solano Ave, Berkeley",FALSE,,Talavera,,,0
        //
        List<Restaurant> restaurants = processRestaurants();
        List<Delivery> deliveries = processDeliveries();

        WorkflowBean bean = nextRow();

        if (bean == null) {
            throw new MemberDataException(
                    "Line " + lineNumber + " driver block for " + driverUserName + " missing closing driver row");
        }

        if (! isDriverRow(bean)) {
            throw new MemberDataException("line " + lineNumber + " is not a driver row. "
                    + "Is this a driver who is also a consumer? If so, the consumer column must be set to false.");
        }
        if (! driverUserName.equals(bean.getUserName())) {
            throw new MemberDataException(driverUserName + ", line " + lineNumber + ", mismatch driver end name");
        }

        Driver driver;

        bean = nextRow();

        if (mode == Mode.DRIVER_MESSAGE_REQUEST) {
            if (bean == null) {
                throw new MemberDataException("Driver " + driverUserName
                        + " missing gmap URL after line " + lineNumber);
            }

            String gmapURL = bean.getGMapURL();
            if (gmapURL.isEmpty()) {
                throw new MemberDataException("Line " + lineNumber + ", driver " + driverUserName + " empty gmap URL");
            }
            if (!gmapURL.contains("https://")) {
                throw new MemberDataException("Driver " + driverUserName + " unrecognizable gmap URL");
            }

            driver = new Driver(driverBean, restaurants, deliveries, gmapURL, controlBlock.lateArrivalAuditDisabled());
        } else {
            assert mode == Mode.DRIVER_ROUTE_REQUEST;

            // This can be either an empty row, marking boundary between this driver and the next,
            // Or the end of file.

            if ((bean != null) && (! emptyRow(bean))) {
                throw new MemberDataException("Line " + lineNumber + " is not empty");
            }

            driver = new Driver(driverBean, restaurants, deliveries);
        }

        return driver;
    }

    private List<Restaurant> processRestaurants() {

        List<Restaurant> restaurants = new ArrayList<>();
        WorkflowBean bean;

        while ((bean = peekNextRow()) != null) {

            if (! (bean.getConsumer().equalsIgnoreCase("FALSE") && bean.getDriver().isEmpty())) {
                break;
            }

            isDriverRow(bean);

            bean = nextRow();
            String errors = "";

            String restaurantName = Objects.requireNonNull(bean).getRestaurant();
            if (restaurantName.isEmpty()) {
                errors += "missing restaurant name\n";
            }
            String address = bean.getAddress();
            if (address.isEmpty()) {
                errors += "missing address\n";
            }
            String details = bean.getDetails();
            String orders = bean.getOrders();
            if (orders.isEmpty()) {
                errors += "missing orders";
            }

            if (! errors.isEmpty()) {
                throw new MemberDataException("line " + lineNumber + " " + errors);
            }

            Restaurant restaurant = new Restaurant(restaurantName);
            restaurant.setAddress(address);
            restaurant.setDetails(details);
            restaurant.setOrders(orders);

            // FIX THIS, DS: refactor to a single map of restaurants
            if (globalRestaurants != null) {
                Restaurant globalRestaurant = globalRestaurants.get(restaurantName);
                if (globalRestaurant == null) {
                    throw new MemberDataException("Restaurant " + restaurantName + ", line number " + lineNumber
                        + ", not found in restaurant template");
                }
                restaurant.mergeGlobal(globalRestaurant);
            }

            restaurants.add(restaurant);
        }

        return restaurants;
    }

    private List<Delivery> processDeliveries() {
        List<Delivery> deliveries = new ArrayList<>();
        WorkflowBean bean;

        while ((bean = peekNextRow()) != null) {
            if (! bean.getConsumer().equalsIgnoreCase("TRUE")) {
                break;
            }

            bean = nextRow();
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
                errors += "missing restaurant name\n";
            }
            String normalRations = bean.getNormal();
            String veggieRations = bean.getVeggie();
            if (normalRations.isEmpty() && veggieRations.isEmpty()) {
                errors += "no rations detected\n";
            }

            if (! errors.isEmpty()) {
                throw new MemberDataException("line " + lineNumber + " " + errors);
            }

            Delivery delivery = new Delivery(consumerName);
            delivery.setUserName(userName);
            delivery.setPhone(phone);
            delivery.setAltPhone(altPhone);
            delivery.setNeighborhood(neighborhood);
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

            // Support for 0 order delivery (e.g. donation drop-off)
            if (delivery.getNormalRations().equals("0") &&
                    delivery.getVeggieRations().equals("0")) {
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

            pickupOrders.put(restaurantName, restaurant.getOrders());
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

    private boolean emptyRow(WorkflowBean bean) {
        return bean.isEmpty();
    }
}