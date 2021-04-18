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

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import java.util.*;

public abstract class WorkflowParser {

    public enum Mode {
        /** The CSV data is for passing to the routing software */
        DRIVER_ROUTE_REQUEST,
        /** The CSV data is for driver message generation */
        DRIVER_MESSAGE_REQUEST,
    }

    protected final Mode mode;
    protected final ControlBlock controlBlock;
    protected long lineNumber = 1;
    protected final PeekingIterator<WorkflowBean> iterator;
    protected Map<String, Restaurant> globalRestaurants;
    protected final String normalizedCSVData;

    protected WorkflowParser(Mode mode, final String csvData) {
        this.mode = mode;
        // Normalize EOL
        normalizedCSVData = csvData.replaceAll("\\r\\n?", "\n");
        controlBlock = ControlBlock.create(normalizedCSVData);
        iterator = initializeIterator(csvData);
    }

    public static WorkflowParser create(
            Mode mode, Map<String, Restaurant> globalRestaurants, String csvData) {

        ControlBlock controlBlock = ControlBlock.create(csvData);
        WorkflowParser workflowParser;

        switch (controlBlock.getVersion()) {
            case Constants.CONTROL_BLOCK_VERSION_UNKNOWN:
                throw new MemberDataException("Control block not found");
            case Constants.CONTROL_BLOCK_VERSION_1:
                throw new MemberDataException(
                        "Control block version " + controlBlock.getVersion() + " is not supported.\n");
            case Constants.CONTROL_BLOCK_VERSION_200:
                workflowParser = new WorkflowParserV200(mode, csvData);
                break;
            case Constants.CONTROL_BLOCK_VERSION_300:
                workflowParser = new WorkflowParserV300(mode, csvData);
                break;
            default:
                throw new MemberDataException(
                        "Control block version " + controlBlock.getVersion() + " is not supported.\n");
        }

        workflowParser.globalRestaurants = globalRestaurants;
        return workflowParser;
    }

    abstract List<WorkflowBean> parse(String csvData);
    /**
     * Check for missing columns.
     * @param csvData Normalized workflow spreadsheet data
     * @throws MemberDataException If there are any missing columns.
     */
    abstract void auditColumnNames(final String csvData);

    abstract List<Delivery> processDeliveries();

    abstract void auditPickupDeliveryMismatch(Driver driver);

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
        assert ! csvData.isEmpty() : "empty workflow";
        auditColumnNames(csvData);

        List<WorkflowBean> workflowBeans = parse(csvData);
        return Iterators.peekingIterator(workflowBeans.iterator());
    }

    /**
     * Return the bean representation of the next row.
     * Increments current line number
     * @return Next bean, or null if at end.
     */
    protected final WorkflowBean nextRow() {
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
    protected final WorkflowBean peekNextRow() {
        if (! iterator.hasNext()) {
            return null;
        }

        return iterator.peek();
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

            driver = Driver.createDriver(
                    driverBean, restaurants, deliveries, gmapURL, controlBlock.lateArrivalAuditDisabled());
        } else {
            assert mode == Mode.DRIVER_ROUTE_REQUEST;

            // This can be either an empty row, marking boundary between this driver and the next,
            // Or the end of file.

            if ((bean != null) && (! emptyRow(bean))) {
                throw new MemberDataException("Line " + lineNumber + " is not empty");
            }

            driver = Driver.createDriver(driverBean, restaurants, deliveries);
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

            Restaurant restaurant = Restaurant.createRestaurant(controlBlock, restaurantName);
            errors += restaurant.setVersionSpecificFields(bean);

            if (! errors.isEmpty()) {
                throw new MemberDataException("line " + lineNumber + " " + errors);
            }

            restaurant.setAddress(address);
            restaurant.setDetails(details);

            // FIX THIS, DS: refactor to a single map of restaurants
            if (mode == Mode.DRIVER_MESSAGE_REQUEST) {
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

    private boolean emptyRow(WorkflowBean bean) {
        return bean.isEmpty();
    }
}
