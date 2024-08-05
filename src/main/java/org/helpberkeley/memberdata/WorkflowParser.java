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
package org.helpberkeley.memberdata;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.helpberkeley.memberdata.v200.WorkflowParserV200;
import org.helpberkeley.memberdata.v300.WorkflowParserV300;

import java.text.MessageFormat;
import java.util.*;

public abstract class WorkflowParser {

    public static final String ERROR_DELIVERY_BEFORE_PICKUP =
            "Driver {0}, delivery for {1} at line {2} occurs before pickup from {3} at line {4}.\n";
    public static final String ERROR_MISSING_HEADER_COLUMN = "Missing header column: {0}";

    protected final ControlBlock controlBlock;
    // Keep the line numbers in sync with the workflow sheet.
    // CsvToBeanBuilder swallows the header row and nextRow() pre-increments line number
    protected int lineNumber = 1;
    protected final PeekingIterator<WorkflowBean> iterator;
    protected Map<String, Restaurant> globalRestaurants;
    protected final String csvData;

    protected WorkflowParser(final String csvData) {
        this.csvData = csvData;
        controlBlock = ControlBlock.create(csvData);
        iterator = initializeIterator();
    }

    public static WorkflowParser create(Map<String, Restaurant> globalRestaurants, String csvData) {

        ControlBlock controlBlock = ControlBlock.create(csvData);
        String version = controlBlock.getVersion();
        WorkflowParser workflowParser;

        if (version.equals(Constants.CONTROL_BLOCK_VERSION_UNKNOWN)) {
            throw new MemberDataException("Control block not found");
        } else if (controlBlock.versionIsCompatible(Constants.CONTROL_BLOCK_VERSION_200)) {
            workflowParser = new WorkflowParserV200(csvData);
        } else if (controlBlock.versionIsCompatible(Constants.CONTROL_BLOCK_VERSION_300)) {
            workflowParser = new WorkflowParserV300(csvData);
        } else {
            throw new MemberDataException(MessageFormat.format(
                    ControlBlock.UNSUPPORTED_VERSION_GENERIC, version));
        }

        workflowParser.globalRestaurants = globalRestaurants;
        return workflowParser;
    }

    protected abstract List<WorkflowBean> parse();
    /**
     * Check for missing columns.
     * @throws MemberDataException If there are any missing columns.
     */
    protected abstract void auditColumnNames();

    protected abstract Delivery processDelivery(WorkflowBean bean);
    protected abstract void versionSpecificAudit(Driver driver);
    protected abstract void auditDeliveryBeforePickup(Driver driver);
    protected abstract void auditPickupDeliveryMismatch(Driver driver);

    private PeekingIterator<WorkflowBean> initializeIterator() {
        assert ! csvData.isEmpty() : "empty workflow";
        auditColumnNames();

        List<WorkflowBean> workflowBeans = parse();
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

    protected final String getIntegerValue(String value) {

        String newValue = value.trim();
        return newValue.isEmpty() ? "0" : newValue;
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
            auditDeliveryBeforePickup(driver);
            versionSpecificAudit(driver);
            driverMap.put(driver.getUserName(), driver);
        }

        return new ArrayList<>(driverMap.values());
    }

    public ControlBlock getControlBlock() {
        return controlBlock;
    }

    public ControlBlock controlBlock() {

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

    public boolean isMemberRow(WorkflowBean bean) {
        String consumerValue = bean.getConsumer();
        String driverValue = bean.getDriver();

        return Boolean.parseBoolean(driverValue) != Boolean.parseBoolean(consumerValue);
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

            controlBlock.processRow(bean, lineNumber);
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
            case Constants.CONTROL_BLOCK_FORMULA:
            case "":
            case Constants.CONTROL_BLOCK_COMMENT:
            case Constants.CONTROL_BLOCK_END:
                break;
            default:
                errors += MessageFormat.format(ControlBlock.ERROR_UNKNOWN_DIRECTIVE, directive, lineNumber);
        }

        if (! errors.isEmpty()) {
            throw new MemberDataException(errors);
        }
    }

    // FIX THIS, DS: move to ControlBlock.  Call by processRow
    private boolean ignoreControlBlockRow(WorkflowBean bean) {

        String directive = bean.getControlBlockDirective();

        if (directive.equals(Constants.CONTROL_BLOCK_COMMENT) || directive.equals(Constants.CONTROL_BLOCK_FORMULA)) {
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

        Driver driver = Driver.createDriver(driverBean);

        processItinerary(driver);
        driver.initialize();

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

        bean = nextRow();

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

        driver.setGMapURL(gmapURL);
        driver.setDisableLateArrivalAudit(controlBlock.lateArrivalAuditDisabled());

        return driver;
    }

    private void processItinerary(Driver driver) {

        WorkflowBean bean;
        while ((bean = peekNextRow()) != null) {

            if (isPickupRow(bean)) {
                driver.addRestaurant(processRestaurant(nextRow()));
            } else if (isDeliveryRow(bean)) {
                driver.addDelivery(processDelivery(nextRow()));
            } else {
                // FIX THIS, DS: sufficient?  Check for end of driver block?
                return;
            }
        }
    }

    private boolean isDeliveryRow(WorkflowBean bean) {
        return bean.getConsumer().equalsIgnoreCase("TRUE");
    }

    private boolean isPickupRow(WorkflowBean bean) {
        return (bean.getConsumer().equalsIgnoreCase("FALSE") && bean.getDriver().isEmpty());
    }

    private Restaurant processRestaurant(WorkflowBean bean) {
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

        Restaurant restaurant = Restaurant.createRestaurant(controlBlock, restaurantName, lineNumber);
        errors += restaurant.setVersionSpecificFields(bean);

        if (! errors.isEmpty()) {
            throw new MemberDataException("line " + lineNumber + ", " + errors);
        }

        restaurant.setAddress(address);
        restaurant.setDetails(details);

        // FIX THIS, DS: refactor to a single map of restaurants
        Restaurant globalRestaurant = globalRestaurants.get(restaurantName);
        if (globalRestaurant == null) {
            throw new MemberDataException("Restaurant " + restaurantName + ", line number " + lineNumber
                    + ", not found in restaurant template");
        }
        restaurant.mergeGlobal(globalRestaurant);

        return restaurant;
    }
}