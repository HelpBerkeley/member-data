/*
 * Copyright (c) 2020-2021. helpberkeley.org
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class DriverPostFormat {

    public static final String ERROR_CONTINUE_WITHOUT_LOOP = "CONTINUE without an enclosing loop";

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverPostFormat.class);
    protected ApiClient apiClient;

    // FIX THIS, DS: fix lifecycle.  Currently initialized in an abstract
    protected Map<String, Restaurant> restaurants;
    // FIX THIS, DS: fix lifecycle.  Currently initialized in an abstract
    protected List<Driver> drivers;

    protected Map<String, User> users;
    protected final List<MessageBlock> driverPostMessageBlocks = new ArrayList<>();
    protected final List<MessageBlock> groupInstructionMessageBlocks = new ArrayList<>();
    protected final List<MessageBlock> backupDriverMessageBlocks = new ArrayList<>();

    private int driverTemplateQuery = 0;
    private int groupTemplateQuery = 0;
    private int restaurantTemplateQuery = 0;

    public static DriverPostFormat create(
            ApiClient apiClient, Map<String, User> users, String routedDeliveries) {

        // Normalize lines
        String normalized = routedDeliveries.replaceAll("\\r\\n?", "\n");

        ControlBlock controlBlock = ControlBlock.create(normalized);

        // FIX THIS, DS: check warnings here?

        DriverPostFormat driverPostFormat;

        switch (controlBlock.getVersion()) {
            case Constants.CONTROL_BLOCK_VERSION_UNKNOWN:
                throw new MemberDataException("Control block not found");
            case Constants.CONTROL_BLOCK_VERSION_1:
                throw new MemberDataException(
                        "Control block version " + controlBlock.getVersion() + " is not supported.\n");
            case Constants.CONTROL_BLOCK_VERSION_200:
                driverPostFormat = new DriverPostFormatV200();
                break;
            case Constants.CONTROL_BLOCK_VERSION_300:
                driverPostFormat = new DriverPostFormatV300();
                break;
            default:
                throw new MemberDataException(
                        "Control block version " + controlBlock.getVersion() + " is not supported.\n");
        }

        driverPostFormat.apiClient = apiClient;
        driverPostFormat.users = users;
        driverPostFormat.initialize(routedDeliveries);
        return driverPostFormat;
    }

    // FIX THIS, DS: cleanup duplicated code in ctor
    public static DriverPostFormat create(ApiClient apiClient, Map<String, User> users,
            String routedDeliveries, int restaurantTemplateQuery,
            int driverTemplateQuery, int groupTemplateQuery) {

        // Normalize lines
        String normalized = routedDeliveries.replaceAll("\\r\\n?", "\n");

        ControlBlock controlBlock = ControlBlock.create(normalized);

        // FIX THIS, DS: check warnings here?

        DriverPostFormat driverPostFormat;

        switch (controlBlock.getVersion()) {
            case Constants.CONTROL_BLOCK_VERSION_UNKNOWN:
                throw new MemberDataException("Control block not found");
            case Constants.CONTROL_BLOCK_VERSION_1:
                throw new MemberDataException(
                        "Control block version " + controlBlock.getVersion() + " is not supported.\n");
            case Constants.CONTROL_BLOCK_VERSION_200:
                driverPostFormat = new DriverPostFormatV200();
                break;
            case Constants.CONTROL_BLOCK_VERSION_300:
                driverPostFormat = new DriverPostFormatV300();
                break;
            default:
                throw new MemberDataException(
                        "Control block version " + controlBlock.getVersion() + " is not supported.\n");
        }

        driverPostFormat.apiClient = apiClient;
        driverPostFormat.users = users;
        driverPostFormat.restaurantTemplateQuery = restaurantTemplateQuery;
        driverPostFormat.driverTemplateQuery = driverTemplateQuery;
        driverPostFormat.groupTemplateQuery = groupTemplateQuery;

        driverPostFormat.initialize(routedDeliveries);
        return driverPostFormat;
    }

    protected DriverPostFormat() {
    }

    abstract ControlBlock getControlBlock();
    abstract void initialize(String routedDeliveries);
    abstract List<Driver> getDrivers();
    abstract String statusMessages();
    abstract String generateSummary();
    abstract Map<String, Restaurant> getRestaurants();
    abstract ProcessingReturnValue processStructRef(MessageBlockStructRef structRef, MessageBlockContext context);
    abstract ProcessingReturnValue processDeliveriesListRef(MessageBlockListRef listRef, MessageBlockContext context);
    abstract ProcessingReturnValue processThisRestaurantPickupListRef(
            MessageBlockListRef listRef, MessageBlockContext context);
    abstract boolean processBooleanSimpleRef(MessageBlockSimpleRef element, MessageBlockContext context);
    abstract boolean processBooleanListRef(MessageBlockListRef listRef, MessageBlockContext context);
    abstract ProcessingReturnValue processListRef(MessageBlockListRef listRef, MessageBlockContext context);
    abstract ProcessingReturnValue processLoopListRef(
            MessageBlockLoop loop, MessageBlockListRef listRef, MessageBlockContext context);
    abstract String versionSpecificSimpleRef(MessageBlockContext context, String varName);

    public final List<String> generateDriverPosts() {

        List<String> driverPosts = new ArrayList<>();
        for (Driver driver : drivers) {
            StringBuilder post = new StringBuilder();

            MessageBlockContext context = new MessageBlockContext("Base", null);
            context.setDriver(driver);

            for (MessageBlock messageBlock : driverPostMessageBlocks) {

                context.setMessageBlockContext(messageBlock.getPostNumber(), messageBlock.getName());

                if (messageBlock.getName().equalsIgnoreCase("comment")) {
                    continue;
                }

                post.append(processMessageBlock(messageBlock, context));
            }

            driverPosts.add(post.toString());
        }

        return driverPosts;
    }

    public final String generateGroupInstructionsPost() {

        StringBuilder post = new StringBuilder();

        MessageBlockContext context = new MessageBlockContext("Base", null);

        for (MessageBlock messageBlock : groupInstructionMessageBlocks) {

            context.setMessageBlockContext(messageBlock.getPostNumber(), messageBlock.getName());

            if (messageBlock.name.equalsIgnoreCase("comment")) {
                continue;
            }

            post.append(processMessageBlock(messageBlock, context));
        }

        return post.toString();
    }

    public final String generateBackupDriverPost() {

        StringBuilder post = new StringBuilder();

        MessageBlockContext context = new MessageBlockContext("Base", null);

        for (MessageBlock messageBlock : backupDriverMessageBlocks) {

            context.setMessageBlockContext(messageBlock.getPostNumber(), messageBlock.getName());

            if (messageBlock.name.equalsIgnoreCase("comment")) {
                continue;
            }

            post.append(processMessageBlock(messageBlock, context));
        }

        return post.toString();
    }


    protected final void loadLastRestaurantTemplate() {
        String  json = apiClient.runQuery(restaurantTemplateQuery);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        assert apiQueryResult.rows.length == 1;

        Object[] columns = (Object[])apiQueryResult.rows[0];
        assert columns.length == 3 : columns.length;
        String rawPost = (String)columns[2];
        RestaurantTemplatePost restaurantTemplatePost = HBParser.restaurantTemplatePost(rawPost);
        String restaurantTemplate = apiClient.downloadFile(restaurantTemplatePost.uploadFile.getFileName());
        RestaurantTemplateParser parser = RestaurantTemplateParser.create(restaurantTemplate);
        restaurants = parser.restaurants();
    }

    protected final void loadDriverPostFormat() {
        String json = apiClient.runQuery(driverTemplateQuery);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            MessageBlock messageBlock = new MessageBlock((Long)columns[0], (String)columns[1]);
            // FIX THIS, DS: catch and update status here?
            messageBlock.parse();
            driverPostMessageBlocks.add(messageBlock);
        }
    }

    protected final void loadGroupPostFormat() {
        String json = apiClient.runQuery(groupTemplateQuery);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            MessageBlock messageBlock = new MessageBlock((Long)columns[0], (String)columns[1]);
            // FIX THIS, DS: catch and update status here?
            messageBlock.parse();
            groupInstructionMessageBlocks.add(messageBlock);
        }
    }

    protected final void loadBackupDriverPostFormat() {
        String json = apiClient.runQuery(Constants.QUERY_GET_BACKUP_DRIVER_FORMAT);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            MessageBlock messageBlock = new MessageBlock((Long)columns[0], (String)columns[1]);
            // FIX THIS, DS: catch and update status here?
            messageBlock.parse();
            backupDriverMessageBlocks.add(messageBlock);
        }
    }

    protected final String processMessageBlock(MessageBlock messageBlock, MessageBlockContext context) {

        StringBuilder message = new StringBuilder();

        LOGGER.trace("Processing block {}", context.getBlockName());

        for (MessageBlockElement element : messageBlock.getElements()) {
            ProcessingReturnValue returnValue = processElement(element, context);

            if (returnValue.status == ProcessingStatus.CONTINUE) {
                throw new MemberDataException(context.formatException(ERROR_CONTINUE_WITHOUT_LOOP));
            }

            message.append(returnValue.output);
        }

        return message.toString();
    }

    protected final ProcessingReturnValue processElement(
            MessageBlockElement element, MessageBlockContext context) {

        ProcessingReturnValue returnValue;

        LOGGER.trace("processing element {}, {}", element, context);

        if (element instanceof MessageBlockQuotedString) {
            returnValue = processQuotedString((MessageBlockQuotedString)element);
        } else if (element instanceof MessageBlockSimpleRef) {
            returnValue = processSimpleRef((MessageBlockSimpleRef)element, context);
        } else if (element instanceof MessageBlockStructRef) {
            returnValue = processStructRef((MessageBlockStructRef)element, context);
        } else if (element instanceof MessageBlockListRef) {
            returnValue = processListRef((MessageBlockListRef)element, context);
        } else if (element instanceof MessageBlockConditional) {
            returnValue = processConditional((MessageBlockConditional)element, context);
        } else if (element instanceof MessageBlockLoop) {
            returnValue = processLoop((MessageBlockLoop)element, context);
        } else if (element instanceof MessageBlockContinue) {
            returnValue = new ProcessingReturnValue(ProcessingStatus.CONTINUE, "");
        }
        else {
            throw new MemberDataException(
                    context.formatException("unknown element type: " + element + ", " + element.getName()));
        }

        return returnValue;
    }

    // Replace "\\n" with newline.
    //
    protected final ProcessingReturnValue processQuotedString(MessageBlockQuotedString quotedString) {
        return new ProcessingReturnValue(
                ProcessingStatus.COMPLETE,
                quotedString.getValue().replaceAll("\n", "").replaceAll("\\\\n", "\n"));
    }

    // Do simple variable replacement
    //
    protected final  ProcessingReturnValue processSimpleRef(MessageBlockSimpleRef simpleRef, MessageBlockContext context) {
        String varName = simpleRef.getName();
        String value;

        final String firstRestaurant = context.getDriver().getFirstRestaurantName();
        final Restaurant restaurant = restaurants.get(firstRestaurant);
        assert restaurant != null : firstRestaurant + " was not found the in restaurant template post";
        Driver driver = context.getDriver();

        switch (varName) {
            case "ThisDriverUserName":
                value = context.getDriver().getUserName();
                break;
            case "ThisDriverFirstRestaurant":
                value = restaurant.getName();
                break;
            case "ThisDriverFirstRestaurantStartTime":
                value = driver.getStartTime();
                break;
            case "ThisDriverFirstRestaurantClosingTime":
                value = restaurant.getClosingTime();
                break;
            case "FirstRestaurantEmoji":
                value = restaurant.getEmoji();
                break;
            case "ThisDriverGMapURL":
                value = context.getDriver().getgMapURL();
                break;
            default:
                value = versionSpecificSimpleRef(context, varName);
        }

        LOGGER.trace("${{}} = \"{}\"", varName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    protected final ProcessingReturnValue processConditional(
            MessageBlockConditional conditional, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();

        boolean conditionalExpression = evaluateCondition(conditional.getConditional(), context);
        MessageBlockConditional.EvaluationType evaluationType = conditional.getEvaluationType();

        if ((conditionalExpression && (evaluationType == MessageBlockConditional.EvaluationType.EVAL_TRUE))
                || ((! conditionalExpression)) && (evaluationType == MessageBlockConditional.EvaluationType.EVAL_FALSE)) {

            for (MessageBlockElement element : conditional.getElements()) {
                ProcessingReturnValue returnValue = processElement(element, context);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    return new ProcessingReturnValue(ProcessingStatus.CONTINUE, output.toString());
                }
            }
        }

        LOGGER.trace("{} {} = {}", conditional, conditional.getConditional(), conditionalExpression);
        // FIX THIS, DS: rework? remove?
        LOGGER.trace("${{}} = \"{}\"", conditional, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    private boolean evaluateCondition(MessageBlockElement element, MessageBlockContext context) {
        boolean expressionValue;

        if (element instanceof MessageBlockSimpleRef) {
            expressionValue = processBooleanSimpleRef((MessageBlockSimpleRef)element, context);
        } else if (element instanceof MessageBlockListRef) {
            expressionValue = processBooleanListRef((MessageBlockListRef)element, context);
        } else if (element instanceof MessageBlockStructRef) {
            expressionValue = processBooleanStructRef((MessageBlockStructRef)element, context);
        } else {
            throw new MemberDataException(context.formatException("unknown boolean element " + element.getName()));
        }

        LOGGER.trace("${{}} = \"{}\"", element, expressionValue);
        return expressionValue;
    }

    protected final ProcessingReturnValue processLoop(MessageBlockLoop loop, MessageBlockContext context) {

        MessageBlockElement loopRef = loop.getLoopRef();

        if (loopRef instanceof MessageBlockListNameRef) {
            return processLoopListNameRef(loop, (MessageBlockListNameRef) loopRef, context);
        } else {
            assert loopRef instanceof MessageBlockListRef : loopRef;
            return processLoopListRef(loop, (MessageBlockListRef) loopRef, context);
        }
    }

    private boolean processBooleanStructRef(MessageBlockStructRef structRef, MessageBlockContext context) {
        String refName = structRef.getName();

        throw new MemberDataException(context.formatException("Unknown boolean variable &{" + refName + "}"));
    }

    private ProcessingReturnValue processLoopListNameRef(
            MessageBlockLoop loop, MessageBlockListNameRef listNameRef, MessageBlockContext context) {

        String listName = listNameRef.getName();
        ProcessingReturnValue returnValue;

        switch (listName) {
            case "ThisDriverRestaurant":
                returnValue = processPickups(loop, context);
                break;
            case "Consumer":
                returnValue = processDeliveries(loop, context);
                break;
            case "Driver":
                returnValue = processDrivers(loop, context);
                break;
            case "SplitRestaurant":
                returnValue = processSplitRestaurants(loop, context);
                break;
            case "BackupDriver":
                returnValue = processBackupDrivers(loop, context);
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + listName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", listName, returnValue.output);
        return returnValue;
    }

    protected final  ProcessingReturnValue processRestaurantPickups(
            MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        Driver driver = context.getDriver();
        MessageBlockContext deliveryContext = new MessageBlockContext("Delivery", context);

        LOGGER.trace("processRestaurantPickups: {}", deliveryContext);

        Restaurant restaurant = context.getPickupRestaurant();

        // Look through deliveries and find consumers/orders for this restaurant

        for (Delivery delivery : driver.getDeliveries()) {
            if (delivery.getRestaurant().equals(restaurant.getName())) {
                context.setDelivery(delivery);
                for (MessageBlockElement loopElement : loop.getElements()) {
                    ProcessingReturnValue returnValue = processElement(loopElement, deliveryContext);
                    output.append(returnValue.output);

                    if (returnValue.status == ProcessingStatus.CONTINUE) {
                        break;
                    }
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    private ProcessingReturnValue processPickups(MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        Driver driver = context.getDriver();
        MessageBlockContext pickupRestaurantContext = new MessageBlockContext("Pickups", context);

        LOGGER.trace("processPickups: {}", pickupRestaurantContext);

        for (Restaurant pickup : driver.getPickups()) {

            Restaurant restaurant = restaurants.get(pickup.getName());
            assert restaurant != null : "Cannot find restaurant " + pickup.getName();

            pickupRestaurantContext.setPickupRestaurant(pickup);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, pickupRestaurantContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    private ProcessingReturnValue processDeliveries(MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        Driver driver = context.getDriver();
        MessageBlockContext deliveryContext = new MessageBlockContext("Loop", context);

        LOGGER.trace("processDeliveries: {}", deliveryContext);

        for (Delivery delivery : driver.getDeliveries()) {

            deliveryContext.setDelivery(delivery);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, deliveryContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    private ProcessingReturnValue processDrivers(MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext driverContext = new MessageBlockContext("Loop", context);

        LOGGER.trace("processDrivers: {}", driverContext);

        for (Driver driver : drivers) {

            driverContext.setDriver(driver);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, driverContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    // Loop through all of the split restaurants
    //
    private ProcessingReturnValue processSplitRestaurants(MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext splitRestaurantContext = new MessageBlockContext("SplitRestaurants", context);

        LOGGER.trace("processSplitRestaurants: {}", splitRestaurantContext);

        for (Restaurant restaurant : restaurants.values()) {

            if (restaurant.getDrivers().size() < 2) {
                continue;
            }

            splitRestaurantContext.setSplitRestaurant(restaurant);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, splitRestaurantContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    // Loop through all of the backup drivers
    //
    private ProcessingReturnValue processBackupDrivers(MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext backupDriverContext = new MessageBlockContext("BackupDrivers", context);

        LOGGER.trace("processBackupDrivers: {}", backupDriverContext);

        for (String backupDriver : getControlBlock().getBackupDrivers()) {

            backupDriverContext.setBackupDriver(backupDriver);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, backupDriverContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    // Convert to (NNN) NNN.NNNN
    protected final String compactPhone(String phone) {

        String compactPhone = phone.replaceAll("[^\\d]", "");

        if ((compactPhone.length() == 11)  && compactPhone.startsWith("1")) {
            compactPhone = compactPhone.substring(1);
        }

        if (compactPhone.length() == 10) {

            compactPhone =
                    "(" + compactPhone.substring(0, 3) + ") "
                            + compactPhone.substring(3, 6) + '.' + compactPhone.substring(6, 10);
        } else {
            compactPhone = phone;
        }

        return compactPhone;
    }

    enum ProcessingStatus {
        COMPLETE,
        CONTINUE
    }

    static class ProcessingReturnValue {

        final ProcessingStatus status;
        final String output;

        ProcessingReturnValue(ProcessingStatus status, String output) {
            this.status = status;
            this.output = output;
        }

    }
}
