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

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverPostFormat.class);

//    private final ApiClient apiClient;
//    private final List<MessageBlock> driverPostMessageBlocks = new ArrayList<>();
//    private final List<MessageBlock> groupInstructionMessageBlocks = new ArrayList<>();
//    private final List<MessageBlock> backupDriverMessageBlocks = new ArrayList<>();
//    private final Map<String, User> users;
//    private List<Driver> drivers;
//    private ControlBlock controlBlock;
//    private Map<String, Restaurant> restaurants;
//    private String restaurantTemplateVersion;
//    private final StringBuilder statusMessages = new StringBuilder();
//    private final int driverTemplateQuery;
//    private final int groupTemplateQuery;
//
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
                driverPostFormat = new DriverPostFormatV200(apiClient, users, normalized);
                break;
            case Constants.CONTROL_BLOCK_VERSION_300:
                driverPostFormat = new DriverPostFormatV300(apiClient, users, normalized);
                break;
            default:
                throw new MemberDataException(
                        "Control block version " + controlBlock.getVersion() + " is not supported.\n");
        }

        return driverPostFormat;
    }

    // FIX THIS, DS: cleanup duplicated code in ctor
    public static DriverPostFormat create(ApiClient apiClient, Map<String, User> users,
            String routedDeliveries, int driverTemplateQuery, int groupTemplateQuery) {

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
                driverPostFormat = new DriverPostFormatV200(apiClient, users, normalized,
                        driverTemplateQuery, groupTemplateQuery);
                break;
            case Constants.CONTROL_BLOCK_VERSION_300:
                driverPostFormat = new DriverPostFormatV300(apiClient, users, normalized,
                        driverTemplateQuery, groupTemplateQuery);
                break;
            default:
                throw new MemberDataException(
                        "Control block version " + controlBlock.getVersion() + " is not supported.\n");
        }

        return driverPostFormat;
    }

    protected DriverPostFormat(ApiClient apiClient, Map<String, User> users, String routedDeliveries) {
//        this.apiClient = apiClient;
//        this.users = users;
//        this.driverTemplateQuery = Constants.QUERY_GET_DRIVERS_POST_FORMAT;
//        this.groupTemplateQuery = Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT;
//        initialize(routedDeliveries);
    }

    // FIX THIS, DS: cleanup duplicated code in ctor
    protected DriverPostFormat(ApiClient apiClient, Map<String, User> users,
             String routedDeliveries, int driverTemplateQuery, int groupTemplateQuery) {

//        this.apiClient = apiClient;
//        this.users = users;
//        this.driverTemplateQuery = driverTemplateQuery;
//        this.groupTemplateQuery = groupTemplateQuery;
//        initialize(routedDeliveries);
    }

    abstract void initialize(String routedDeliveries);
    abstract List<String> generateDriverPosts();
    abstract List<Driver> getDrivers();
    abstract String generateGroupInstructionsPost();
    abstract String generateBackupDriverPost();
    abstract String statusMessages();
    abstract String generateSummary();
    abstract Map<String, Restaurant> getRestaurants();

//
//    List<Driver> getDrivers() {
//        return drivers;
//    }
//
//    Map<String, Restaurant> getRestaurants() {
//        return restaurants;
//    }
//
//    String statusMessages() {
//        return statusMessages.toString();
//    }
//
//    String generateSummary() {
//        StringBuilder summary = new StringBuilder();
//
//        if (! controlBlock.restaurantsAuditDisabled()) {
//            // Restaurants with no drivers
//            for (Restaurant restaurant : restaurants.values()) {
//                if (restaurant.getDrivers().size() == 0) {
//                    summary.append("No drivers going to ").append(restaurant.getName()).append("\n");
//                }
//            }
//        }
//        summary.append("\n");
//
//        long numVeggie = 0;
//        long numNormal = 0;
//
//        for (Driver driver : drivers) {
//            String originalStartTime = driver.getOriginalStartTime();
//            String startTime = driver.getStartTime();
//
//            if (! startTime.equals(originalStartTime)) {
//
//                summary.append("Driver ").append(driver.getUserName())
//                        .append(", start time for ").append(driver.getFirstRestaurantName())
//                        .append(" adjusted to ").append(startTime).append(" from ").append(originalStartTime)
//                        .append('\n');
//            }
//
//            for (String warning : driver.getWarningMessages()) {
//                summary.append("Warning: driver ").append(driver.getUserName())
//                        .append(" ").append(warning).append('\n');
//            }
//
//            for (Delivery delivery : driver.getDeliveries()) {
//                numNormal += Long.parseLong(delivery.getNormalRations());
//                numVeggie += Long.parseLong(delivery.getVeggieRations());
//            }
//        }
//
//        summary.append("\n");
//
//        // Split restaurants / cleanup drivers / orders
//
//        long totalOrders = 0;
//        boolean headerAdded = false;
//
//        for (Restaurant restaurant : restaurants.values()) {
//            String cleanupDriver;
//
//            totalOrders += restaurant.getOrders();
//
//            if (restaurant.getDrivers().size() < 2) {
//                continue;
//            }
//
//            if (! controlBlock.splitRestaurantAuditsDisabled()) {
//                if (!headerAdded) {
//                    summary.append("|Split Restaurants|Cleanup Driver|\n");
//                    summary.append("|---|---|\n");
//
//                    headerAdded = true;
//                }
//            }
//
//            if (controlBlock.splitRestaurantAuditsDisabled()) {
//                cleanupDriver = "";
//            } else {
//                cleanupDriver = controlBlock.getSplitRestaurant(restaurant.getName()).getCleanupDriverUserName();
//            }
//
//            if (! controlBlock.splitRestaurantAuditsDisabled()) {
//                summary.append("|");
//                summary.append(restaurant.getName());
//                summary.append("|");
//                summary.append(cleanupDriver);
//                summary.append("|\n");
//            }
//        }
//
//        // Total
//
//        // Total orders: 19    Meals: 56  Drivers on the road: 4  Normal rations: 24   Veggie rations: 4
//
//        // | Orders | Meals | Drivers | Normal rations| Veggie rations|
//        //|---|---|---|---|---|
//        //| 10 | 30 | 3 | 14 | 1 |
//
//        summary.append("\n");
//        summary.append("|Orders|Meals|Drivers|Normal rations|Veggie rations|\n");
//        summary.append("|---|---|---|---|---|\n");
//        summary.append("|");
//        summary.append(totalOrders);
//        summary.append("|");
//        summary.append((numNormal + numVeggie) * 2);
//        summary.append("|");
//        summary.append(drivers.size());
//        summary.append("|");
//        summary.append(numNormal);
//        summary.append("|");
//        summary.append(numVeggie);
//        summary.append("|\n");
//
//        return summary.toString();
//    }
//
//    List<String> generateDriverPosts() {
//
//        List<String> driverPosts = new ArrayList<>();
//        for (Driver driver : drivers) {
//            StringBuilder post = new StringBuilder();
//
//            MessageBlockContext context = new MessageBlockContext("Base", null);
//            context.setDriver(driver);
//
//            for (MessageBlock messageBlock : driverPostMessageBlocks) {
//
//                context.setMessageBlockContext(messageBlock.getPostNumber(), messageBlock.getName());
//
//                if (messageBlock.getName().equalsIgnoreCase("comment")) {
//                    continue;
//                }
//
//                post.append(processMessageBlock(messageBlock, context));
//            }
//
//            driverPosts.add(post.toString());
//        }
//
//        return driverPosts;
//    }
//
//    String generateGroupInstructionsPost() {
//
//        StringBuilder post = new StringBuilder();
//
//        MessageBlockContext context = new MessageBlockContext("Base", null);
//
//        for (MessageBlock messageBlock : groupInstructionMessageBlocks) {
//
//            context.setMessageBlockContext(messageBlock.getPostNumber(), messageBlock.getName());
//
//            if (messageBlock.name.equalsIgnoreCase("comment")) {
//                continue;
//            }
//
//            post.append(processMessageBlock(messageBlock, context));
//        }
//
//        return post.toString();
//    }
//
//    String generateBackupDriverPost() {
//
//        StringBuilder post = new StringBuilder();
//
//        MessageBlockContext context = new MessageBlockContext("Base", null);
//
//        for (MessageBlock messageBlock : backupDriverMessageBlocks) {
//
//            context.setMessageBlockContext(messageBlock.getPostNumber(), messageBlock.getName());
//
//            if (messageBlock.name.equalsIgnoreCase("comment")) {
//                continue;
//            }
//
//            post.append(processMessageBlock(messageBlock, context));
//        }
//
//        return post.toString();
//    }
//
//    private void auditControlBlock() {
//
//        List<Restaurant> splitRestaurants = new ArrayList<>();
//
//        for (Restaurant restaurant : restaurants.values()) {
//            if (restaurant.getDrivers().size() > 1) {
//                splitRestaurants.add(restaurant);
//            }
//        }
//
//        controlBlock.audit(users, restaurants, splitRestaurants);
//        statusMessages.append(controlBlock.getWarnings());
//    }
//
//    private void loadLastRestaurantTemplate() {
//        String  json = apiClient.runQuery(Constants.QUERY_GET_CURRENT_VALIDATED_RESTAURANT_TEMPLATE);
//        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
//        assert apiQueryResult.rows.length == 1;
//
//        Object[] columns = (Object[])apiQueryResult.rows[0];
//        assert columns.length == 3 : columns.length;
//        String rawPost = (String)columns[2];
//        RestaurantTemplatePost restaurantTemplatePost = HBParser.restaurantTemplatePost(rawPost);
//        String restaurantTemplate = apiClient.downloadFile(restaurantTemplatePost.uploadFile.getFileName());
//        RestaurantTemplateParser parser = RestaurantTemplateParser.create(restaurantTemplate);
//        restaurantTemplateVersion = parser.getVersion();
//        restaurants = parser.restaurants();
//    }
//
//    private void loadDriverPostFormat() {
//        String json = apiClient.runQuery(driverTemplateQuery);
//        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
//
//        for (Object rowObj : apiQueryResult.rows) {
//            Object[] columns = (Object[]) rowObj;
//            assert columns.length == 3 : columns.length;
//
//            MessageBlock messageBlock = new MessageBlock((Long)columns[0], (String)columns[1]);
//            // FIX THIS, DS: catch and update status here?
//            messageBlock.parse();
//            driverPostMessageBlocks.add(messageBlock);
//        }
//    }
//
//    private void loadGroupPostFormat() {
//        String json = apiClient.runQuery(groupTemplateQuery);
//        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
//
//        for (Object rowObj : apiQueryResult.rows) {
//            Object[] columns = (Object[]) rowObj;
//            assert columns.length == 3 : columns.length;
//
//            MessageBlock messageBlock = new MessageBlock((Long)columns[0], (String)columns[1]);
//            // FIX THIS, DS: catch and update status here?
//            messageBlock.parse();
//            groupInstructionMessageBlocks.add(messageBlock);
//        }
//    }
//
//    private void loadBackupDriverPostFormat() {
//        String json = apiClient.runQuery(Constants.QUERY_GET_BACKUP_DRIVER_FORMAT);
//        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
//
//        for (Object rowObj : apiQueryResult.rows) {
//            Object[] columns = (Object[]) rowObj;
//            assert columns.length == 3 : columns.length;
//
//            MessageBlock messageBlock = new MessageBlock((Long)columns[0], (String)columns[1]);
//            // FIX THIS, DS: catch and update status here?
//            messageBlock.parse();
//            backupDriverMessageBlocks.add(messageBlock);
//        }
//    }
//
//    private void loadRoutedDeliveries(final String routedDeliveries) {
//        WorkflowParser parser = WorkflowParser.create(
//                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, routedDeliveries);
//        drivers = parser.drivers();
//        controlBlock = parser.getControlBlock();
//
//        // Add all the individual driver pickups to the global restaurants,
//        // so the we can detect split restaurants.
//        for (Driver driver : drivers) {
//            for (Restaurant pickup : driver.getPickups()) {
//                Restaurant restaurant = restaurants.get(pickup.getName());
//
//                // FIX THIS, DS: audit this earlier so that we can come up with a line number
//                if (restaurant == null) {
//                    throw new MemberDataException("Restaurant \"" + pickup.getName() + "\" from driver "
//                            + driver.getUserName() + " not found.  Is it misspelled?");
//                }
//
//                restaurant.addDriver(driver);
//                restaurant.addOrders(pickup.getOrders());
//            }
//        }
//    }
//
//    private boolean driverHasSplitRestaurant(final Driver driver) {
//
//        boolean hasSplit = false;
//
//        for (Restaurant pickup : driver.getPickups()) {
//            Restaurant restaurant = restaurants.get(pickup.getName());
//
//            if (restaurant.getDrivers().size() > 1) {
//                hasSplit = true;
//                break;
//            }
//        }
//
//        LOGGER.trace("Driver {} has{} split restaurants", driver.getUserName(), hasSplit ? "" : " no");
//
//        return hasSplit;
//    }
//
//    private String processMessageBlock(MessageBlock messageBlock, MessageBlockContext context) {
//
//        StringBuilder message = new StringBuilder();
//
//        LOGGER.trace("Processing block {}", context.messageBlockName);
//
//        for (MessageBlockElement element : messageBlock.getElements()) {
//            message.append(processElement(element, context));
//        }
//
//        return message.toString();
//    }
//
//    private String processElement(MessageBlockElement element, MessageBlockContext context) {
//
//        String processedValue;
//
//        LOGGER.trace("processing element {}, {}", element, context);
//
//        if (element instanceof MessageBlockQuotedString) {
//            processedValue = processQuotedString((MessageBlockQuotedString)element);
//        } else if (element instanceof MessageBlockSimpleRef) {
//            processedValue = processSimpleRef((MessageBlockSimpleRef)element, context);
//        } else if (element instanceof MessageBlockStructRef) {
//            processedValue = processStructRef((MessageBlockStructRef)element, context);
//        } else if (element instanceof MessageBlockListRef) {
//            processedValue = processListRef((MessageBlockListRef)element, context);
//        } else if (element instanceof MessageBlockConditional) {
//            processedValue = processConditional((MessageBlockConditional)element, context);
//        } else if (element instanceof MessageBlockLoop) {
//            processedValue = processLoop((MessageBlockLoop)element, context);
//        }
//        else {
//            throw new MemberDataException(
//                    context.formatException("unknown element type: " + element + ", " + element.getName()));
//        }
//
//        return processedValue;
//    }
//
//    // Remove any newlines.
//    // Replace "\\n" with newline.
//    //
//    private String processQuotedString(MessageBlockQuotedString quotedString) {
//        return quotedString.getValue().replaceAll("\n", "").replaceAll("\\\\n", "\n");
//    }
//
//    // Do simple variable replacement
//    //
//    private String processSimpleRef(MessageBlockSimpleRef simpleRef, MessageBlockContext context) {
//        String varName = simpleRef.getName();
//        String value;
//
//        final String firstRestaurant = context.getDriver().getFirstRestaurantName();
//        final Restaurant restaurant = restaurants.get(firstRestaurant);
//        assert restaurant != null : firstRestaurant + " was not found the in restaurant template post";
//
//        switch (varName) {
//            case "ThisDriverUserName":
//                value = context.getDriver().getUserName();
//                break;
//            case "ThisDriverFirstRestaurant":
//                value = restaurant.getName();
//                break;
//            case "ThisDriverFirstRestaurantStartTime":
//                value = context.getDriver().getStartTime();
//                break;
//            case "ThisDriverFirstRestaurantClosingTime":
//                value = restaurant.getClosingTime();
//                break;
//            case "FirstRestaurantEmoji":
//                value = restaurant.getEmoji();
//                break;
//            case "ThisDriverGMapURL":
//                value = context.getDriver().getgMapURL();
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown variable ${" + varName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", varName, value);
//        return value;
//    }
//
//    private String processStructRef(MessageBlockStructRef structRef, MessageBlockContext context) {
//        String refName = structRef.getName();
//        String value;
//
//        switch (refName) {
//            case "OnCallOpsManager.UserName":
//                // FIX THIS, DS: how are we handling multiple ops managers?
//                value = controlBlock.getFirstOpsManager().getUserName();
//                break;
//            case "OnCallOpsManager.CompactPhone":
//                // FIX THIS, DS: how are we handling multiple ops managers?
//                value = compactPhone(controlBlock.getFirstOpsManager().getPhone());
//                break;
//            case "BackupDriver.UserName":
//                value = compactPhone(context.getBackupDriver());
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown struct variable ${" + refName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", refName, value);
//        return value;
//    }
//
//    private String processListRef(MessageBlockListRef listRef, MessageBlockContext context) {
//        String listName = listRef.getListName();
//        String refName = listRef.getName();
//        String value;
//
//        switch (listName) {
//            case "ThisDriverRestaurant":
//                value = processPickupsListRef(listRef, context);
//                break;
//            case "Consumer":
//                value = processDeliveriesListRef(listRef, context);
//                break;
//            case "Driver":
//                value = processDriverListRef(listRef, context);
//                break;
//            case "SplitRestaurant":
//                value = processSplitRestaurantListRef(listRef, context);
//                break;
//            case "Pickup":
//                value = processThisRestaurantPickupListRef(listRef, context);
//                break;
//            default:
//                throw new MemberDataException(context.formatException(
//                        "unknown list name &{" + listName + "} in " + "&{" + refName + "}"));
//        }
//
//        LOGGER.trace("&{{}} = \"{}\"", refName, value);
//        return value;
//    }
//
//    private String processPickupsListRef(MessageBlockListRef listRef, MessageBlockContext context) {
//        String refName = listRef.getName();
//        String value;
//
//        // FIX THIS, DS: unify Restaurant so that there are not multiple copies
//        Restaurant pickupRestaurant = context.getPickupRestaurant();
//        Restaurant globalRestaurant = restaurants.get(pickupRestaurant.getName());
//
//        switch (refName) {
//            case "ThisDriverRestaurant.Name":
//                value = pickupRestaurant.getName();
//                break;
//            case "ThisDriverRestaurant.Emoji":
//                value = globalRestaurant.getEmoji();
//                break;
//            case "ThisDriverRestaurant.Address":
//                value = pickupRestaurant.getAddress();
//                break;
//            case "ThisDriverRestaurant.Details":
//                value = pickupRestaurant.getDetails();
//                break;
//            case "ThisDriverRestaurant.ThisDriverOrders":
//                value = Long.toString(pickupRestaurant.getOrders());
//                break;
//            case "ThisDriverRestaurant.TotalOrders":
//                value = Long.toString(globalRestaurant.getOrders());
//                break;
//            case "ThisDriverRestaurant.TotalDrivers":
//                value = Long.toString(globalRestaurant.getDrivers().size());
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", refName, value);
//        return value;
//    }
//
//    private String processDeliveriesListRef(MessageBlockListRef listRef, MessageBlockContext context) {
//        String refName = listRef.getName();
//        String value;
//
//        Delivery delivery = context.getDelivery();
//
//        switch (refName) {
//            case "Consumer.Name":
//                value = delivery.getName();
//                break;
//            case "Consumer.UserName":
//                value = delivery.getUserName();
//                break;
//            case "Consumer.CompactPhone":
//                value = compactPhone(delivery.getPhone());
//                break;
//            case "Consumer.CompactAltPhone":
//                value = compactPhone(delivery.getAltPhone());
//                break;
//            case "Consumer.City":
//                value = delivery.getCity();
//                break;
//            case "Consumer.Address":
//                value = delivery.getAddress();
//                break;
//            case "Consumer.Details":
//                value = delivery.getDetails();
//                break;
//            case "Consumer.Restaurant":
//                value = delivery.getRestaurant();
//                break;
//            case "Consumer.RestaurantEmoji":
//                value = restaurants.get(delivery.getRestaurant()).getEmoji();
//                break;
//            case "Consumer.Normal":
//                value = delivery.getNormalRations();
//                break;
//            case "Consumer.Veggie":
//                value = delivery.getVeggieRations();
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", refName, value);
//        return value;
//    }
//
//    private String processDriverListRef(MessageBlockListRef listRef, MessageBlockContext context) {
//        String refName = listRef.getName();
//        String value;
//
//        Driver driver = context.getDriver();
//        String firstRestaurantName = driver.getFirstRestaurantName();
//        Restaurant restaurant = restaurants.get(firstRestaurantName);
//        assert restaurant != null : firstRestaurantName + " was not found the in restaurant template post";
//
//        switch (refName) {
//            case "Driver.UserName":
//                value = driver.getUserName();
//                break;
//            case "Driver.FirstRestaurantName":
//                value = firstRestaurantName;
//                break;
//            case "Driver.FirstRestaurantStartTime":
//                value = driver.getStartTime();
//                break;
//            case "Driver.FirstRestaurantClosingTime":
//                value = restaurant.getClosingTime();
//                break;
//            case "Driver.SplitRestaurantOrders":
//                Restaurant splitRestaurant = context.getSplitRestaurant();
//                value = Long.toString(driver.getOrders(splitRestaurant.getName()));
//                break;
//            case "Driver.CompactPhone":
//                value = compactPhone(driver.getPhoneNumber());
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", refName, value);
//        return value;
//    }
//
//    private String processSplitRestaurantListRef(MessageBlockListRef listRef, MessageBlockContext context) {
//        String refName = listRef.getName();
//        String value;
//
//        Restaurant splitRestaurant = context.getSplitRestaurant();
//        String name = splitRestaurant.getName();
//
//        switch (refName) {
//            case "SplitRestaurant.Name":
//                value = name;
//                break;
//            case "SplitRestaurant.Emoji":
//                value = splitRestaurant.getEmoji();
//                break;
//            case "SplitRestaurant.TotalOrders":
//                value = Long.toString(splitRestaurant.getOrders());
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", refName, value);
//        return value;
//    }
//
//    private String processThisRestaurantPickupListRef(MessageBlockListRef listRef, MessageBlockContext context) {
//        String refName = listRef.getName();
//        String value;
//
//        Restaurant pickupRestaurant = context.getPickupRestaurant();
//        Delivery delivery = context.getDelivery();
//        assert delivery.getRestaurant().equals(pickupRestaurant.getName()) :
//                delivery.getRestaurant() + " != " + pickupRestaurant.getName();
//
//        switch (refName) {
//            case "Pickup.MemberName":
//                value = delivery.getName();
//                break;
//            case "Pickup.UserName":
//                value = delivery.getUserName();
//                break;
//            case "Pickup.Normal":
//                value = delivery.getNormalRations();
//                break;
//            case "Pickup.Veggie":
//                value = delivery.getVeggieRations();
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", refName, value);
//        return value;
//    }
//
//    private String processConditional(MessageBlockConditional conditional, MessageBlockContext context) {
//
//        StringBuilder output = new StringBuilder();
//
//        boolean conditionalExpression = evaluateCondition(conditional.getConditional(), context);
//        MessageBlockConditional.EvaluationType evaluationType = conditional.getEvaluationType();
//
//        if ((conditionalExpression && (evaluationType == MessageBlockConditional.EvaluationType.EVAL_TRUE))
//            || ((! conditionalExpression)) && (evaluationType == MessageBlockConditional.EvaluationType.EVAL_FALSE)) {
//
//            for (MessageBlockElement element : conditional.getElements()) {
//                output.append(processElement(element, context));
//            }
//        }
//
//        LOGGER.trace("{} {} = {}", conditional, conditional.getConditional(), conditionalExpression);
//        // FIX THIS, DS: rework? remove?
//        LOGGER.trace("${{}} = \"{}\"", conditional, output.toString());
//        return output.toString();
//    }
//
//    private boolean evaluateCondition(MessageBlockElement element, MessageBlockContext context) {
//        boolean expressionValue;
//
//        if (element instanceof MessageBlockSimpleRef) {
//            expressionValue = processBooleanSimpleRef((MessageBlockSimpleRef)element, context);
//        } else if (element instanceof MessageBlockListRef) {
//            expressionValue = processBooleanListRef((MessageBlockListRef)element, context);
//        } else if (element instanceof MessageBlockStructRef) {
//            expressionValue = processBooleanStructRef((MessageBlockStructRef)element, context);
//        } else {
//            throw new MemberDataException(context.formatException("unknown boolean element " + element.getName()));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", element, expressionValue);
//        return expressionValue;
//    }
//
//    private boolean processBooleanSimpleRef(MessageBlockSimpleRef element, MessageBlockContext context) {
//        String refName = element.getName();
//        Driver driver = context.getDriver();
//        boolean value;
//
//        switch (refName) {
//            case "ThisDriverSplitsAnyRestaurant":
//                value = driverHasSplitRestaurant(driver);
//                break;
//            case "ThisDriverAnyCondo":
//                value = driver.hasCondo();
//                break;
//            case "AnySplitRestaurants":
//                value = anySplitRestaurants();
//                break;
//            case "IsFirstRestaurantClosingBefore545PM":
//                String restaurantName = driver.getFirstRestaurantName();
//                Restaurant restaurant = restaurants.get(restaurantName);
//                value = restaurant.closesBefore(545);
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown boolean variable ${" + refName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", element, value);
//        return value;
//    }
//
//    private boolean anySplitRestaurants() {
//        for (Restaurant restaurant : restaurants.values()) {
//            if (restaurant.getDrivers().size() > 1) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    private boolean processBooleanListRef(MessageBlockListRef listRef, MessageBlockContext context) {
//        String listName = listRef.getListName();
//        String refName = listRef.getName();
//        boolean value;
//
//        if (listName.equals("ThisDriverRestaurant")) {
//            Restaurant restaurant = context.getPickupRestaurant();
//            String restaurantName = restaurant.getName();
//            Restaurant globalRestaurant = restaurants.get(restaurantName);
//
//            switch (refName) {
//                case "ThisDriverRestaurant.IsSplit":
//                    return globalRestaurant.getDrivers().size() > 1;
//                case "ThisDriverRestaurant.NoPics":
//                    return globalRestaurant.getNoPics();
//                case "ThisDriverRestaurant.IsCleanup":
//                    String driverUserName = context.getDriver().getUserName();
//                    // FIX THIS, DS: what if we aren't in a split restaurant context?  Audit
//                    ControlBlock.SplitRestaurant splitRestaurant = controlBlock.getSplitRestaurant(restaurantName);
//                    // FIX THIS, DS: what if the control block is missing this split restaurant? Audit
//                    return splitRestaurant.getCleanupDriverUserName().equals(driverUserName);
//                case "ThisDriverRestaurant.AnyOrder":
//                    return (restaurant.getOrders() != 0);
//                default:
//                    throw new MemberDataException(
//                            context.formatException("Unknown boolean variable &{" + refName + "}"));
//            }
//
//        } else if (listName.equals("Consumer")) {
//            Delivery delivery = context.getDelivery();
//
//            if (refName.equals("Consumer.IsAltPhone")) {
//                String altPhone = delivery.getAltPhone();
//                value = ((! altPhone.isEmpty()) && (! altPhone.equalsIgnoreCase("none")));
//            } else if (refName.equals("Consumer.IsCondo")) {
//                value = delivery.isCondo();
//            } else {
//                throw new MemberDataException(context.formatException("Unknown boolean variable &{" + refName + "}"));
//            }
//        } else if (refName.equals("Driver.IsFirstRestaurantClosingBefore7PM")) {
//            Driver driver = context.getDriver();
//            String firstName = driver.getFirstRestaurantName();
//            Restaurant firstRestaurant = restaurants.get(firstName);
//            value = firstRestaurant.closesBefore(700);
//        } else if (refName.equals("Driver.IsFirstRestaurantClosingBefore545PM")) {
//            Driver driver = context.getDriver();
//            String firstName = driver.getFirstRestaurantName();
//            Restaurant firstRestaurant = restaurants.get(firstName);
//            value = firstRestaurant.closesBefore(545);
//        } else if (refName.equals("Driver.IsCleanup")) {
//            String driverUserName = context.getDriver().getUserName();
//            Restaurant restaurant = context.getSplitRestaurant();
//            // FIX THIS, DS: what if we aren't in a split restaurant context?  Audit
//            ControlBlock.SplitRestaurant splitRestaurant = controlBlock.getSplitRestaurant(restaurant.getName());
//            // FIX THIS, DS: what if the control block is missing this split restaurant? Audit
//            return splitRestaurant.getCleanupDriverUserName().equals(driverUserName);
//        } else {
//            throw new MemberDataException(context.formatException("Unknown boolean variable &{" + refName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", listRef, value);
//        return value;
//    }
//
//    private boolean processBooleanStructRef(MessageBlockStructRef structRef, MessageBlockContext context) {
//        String refName = structRef.getName();
//
//        throw new MemberDataException(context.formatException("Unknown boolean variable &{" + refName + "}"));
//    }
//
//    private String processLoop(MessageBlockLoop loop, MessageBlockContext context) {
//
//        MessageBlockElement loopRef = loop.getLoopRef();
//
//        if (loopRef instanceof MessageBlockListNameRef) {
//            return processLoopListNameRef(loop, (MessageBlockListNameRef) loopRef, context);
//        } else {
//            assert loopRef instanceof MessageBlockListRef : loopRef;
//            return processLoopListRef(loop, (MessageBlockListRef) loopRef, context);
//        }
//    }
//
//    private String processLoopListNameRef(
//            MessageBlockLoop loop, MessageBlockListNameRef listNameRef, MessageBlockContext context) {
//
//        String listName = listNameRef.getName();
//        String processedLoop;
//
//        switch (listName) {
//            case "ThisDriverRestaurant":
//                processedLoop = processPickups(loop, context);
//                break;
//            case "Consumer":
//                processedLoop = processDeliveries(loop, context);
//                break;
//            case "Driver":
//                processedLoop = processDrivers(loop, context);
//                break;
//            case "SplitRestaurant":
//                processedLoop = processSplitRestaurants(loop, context);
//                break;
//            case "BackupDriver":
//                processedLoop = processBackupDrivers(loop, context);
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown list variable &{" + listName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", listName, processedLoop);
//        return processedLoop;
//    }
//
//    private String processLoopListRef(
//            MessageBlockLoop loop, MessageBlockListRef listRef, MessageBlockContext context) {
//
//        String refName = listRef.getName();
//        String processedLoop;
//
//        // FIX THIS, DS generalize this nested loop handling
//
//        switch (refName) {
//            case "SplitRestaurant.Driver":
//                processedLoop = processSplitRestaurantDrivers(loop, context);
//                break;
//            case "ThisDriverRestaurant.Pickup":
//                processedLoop = processRestaurantPickups(loop, context);
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown loop list ref &{" + listRef + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", refName, processedLoop);
//        return processedLoop;
//    }
//
//    private String processSplitRestaurantDrivers(MessageBlockLoop loop, MessageBlockContext context) {
//
//        StringBuilder processedLoop = new StringBuilder();
//        MessageBlockContext driverContext = new MessageBlockContext("SplitRestaurantDrivers", context);
//
//        Restaurant splitRestaurant = context.getSplitRestaurant();
//
//        for (Driver driver : splitRestaurant.getDrivers().values()) {
//
//            driverContext.setDriver(driver);
//
//            for (MessageBlockElement element : loop.getElements()) {
//                processedLoop.append(processElement(element, driverContext));
//            }
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", loop, processedLoop);
//        return processedLoop.toString();
//    }
//
//    private String processRestaurantPickups(MessageBlockLoop loop, MessageBlockContext context) {
//
//        StringBuilder output = new StringBuilder();
//        Driver driver = context.getDriver();
//        MessageBlockContext deliveryContext = new MessageBlockContext("Delivery", context);
//
//        LOGGER.trace("processRestaurantPickups: {}", deliveryContext);
//
//        Restaurant restaurant = context.getPickupRestaurant();
//
//        // Look through deliveries and find consumers/orders for this restaurant
//
//        for (Delivery delivery : driver.getDeliveries()) {
//            if (delivery.getRestaurant().equals(restaurant.getName())) {
//                context.setDelivery(delivery);
//                for (MessageBlockElement loopElement : loop.getElements()) {
//                    output.append(processElement(loopElement, deliveryContext));
//                }
//            }
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", loop, output.toString());
//        return output.toString();
//    }
//
//    private String processPickups(MessageBlockLoop loop, MessageBlockContext context) {
//
//        StringBuilder output = new StringBuilder();
//        Driver driver = context.getDriver();
//        MessageBlockContext pickupRestaurantContext = new MessageBlockContext("Pickups", context);
//
//        LOGGER.trace("processPickups: {}", pickupRestaurantContext);
//
//        for (Restaurant pickup : driver.getPickups()) {
//
//            Restaurant restaurant = restaurants.get(pickup.getName());
//            assert restaurant != null : "Cannot find restaurant " + pickup.getName();
//
//            pickupRestaurantContext.setPickupRestaurant(pickup);
//
//            for (MessageBlockElement loopElement : loop.getElements()) {
//                output.append(processElement(loopElement, pickupRestaurantContext));
//            }
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", loop, output.toString());
//        return output.toString();
//    }
//
//    private String processDeliveries(MessageBlockLoop loop, MessageBlockContext context) {
//
//        StringBuilder output = new StringBuilder();
//        Driver driver = context.getDriver();
//        MessageBlockContext deliveryContext = new MessageBlockContext("Loop", context);
//
//        LOGGER.trace("processDeliveries: {}", deliveryContext);
//
//        for (Delivery delivery : driver.getDeliveries()) {
//
//            deliveryContext.setDelivery(delivery);
//
//            for (MessageBlockElement loopElement : loop.getElements()) {
//                output.append(processElement(loopElement, deliveryContext));
//            }
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", loop, output.toString());
//        return output.toString();
//    }
//
//    private String processDrivers(MessageBlockLoop loop, MessageBlockContext context) {
//
//        StringBuilder output = new StringBuilder();
//        MessageBlockContext driverContext = new MessageBlockContext("Loop", context);
//
//        LOGGER.trace("processDrivers: {}", driverContext);
//
//        for (Driver driver : drivers) {
//
//            driverContext.setDriver(driver);
//
//            for (MessageBlockElement loopElement : loop.getElements()) {
//                output.append(processElement(loopElement, driverContext));
//            }
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", loop, output.toString());
//        return output.toString();
//    }
//
//    // Loop through all of the split restaurants
//    //
//    private String processSplitRestaurants(MessageBlockLoop loop, MessageBlockContext context) {
//
//        StringBuilder output = new StringBuilder();
//        MessageBlockContext splitRestaurantContext = new MessageBlockContext("SplitRestaurants", context);
//
//        LOGGER.trace("processSplitRestaurants: {}", splitRestaurantContext);
//
//        for (Restaurant restaurant : restaurants.values()) {
//
//            if (restaurant.getDrivers().size() < 2) {
//                continue;
//            }
//
//            splitRestaurantContext.setSplitRestaurant(restaurant);
//
//            for (MessageBlockElement loopElement : loop.getElements()) {
//                output.append(processElement(loopElement, splitRestaurantContext));
//            }
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", loop, output.toString());
//        return output.toString();
//    }
//
//    // Loop through all of the backup drivers
//    //
//    private String processBackupDrivers(MessageBlockLoop loop, MessageBlockContext context) {
//
//        StringBuilder output = new StringBuilder();
//        MessageBlockContext backupDriverContext = new MessageBlockContext("BackupDrivers", context);
//
//        LOGGER.trace("processBackupDrivers: {}", backupDriverContext);
//
//        for (String backupDriver : controlBlock.getBackupDrivers()) {
//
//            backupDriverContext.setBackupDriver(backupDriver);
//
//            for (MessageBlockElement loopElement : loop.getElements()) {
//                output.append(processElement(loopElement, backupDriverContext));
//            }
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", loop, output.toString());
//        return output.toString();
//    }
//
//    // Convert to (NNN) NNN.NNNN
//    private String compactPhone(String phone) {
//
//        String compactPhone = phone.replaceAll("[^\\d]", "");
//
//        if ((compactPhone.length() == 11)  && compactPhone.startsWith("1")) {
//            compactPhone = compactPhone.substring(1);
//        }
//
//        if (compactPhone.length() == 10) {
//
//            compactPhone =
//                    "(" + compactPhone.substring(0, 3) + ") "
//                    + compactPhone.substring(3, 6) + '.' + compactPhone.substring(6, 10);
//        } else {
//            compactPhone = phone;
//        }
//
//        return compactPhone;
//    }
//
//    static class MessageBlockContext {
//        private final String name;
//        private final MessageBlockContext parent;
//
//        private String messageBlockName;
//        private long messageBlockPost;
//        private Driver driver;
//        private Delivery delivery;
//        private Restaurant splitRestaurant;
//        private Restaurant pickupRestaurant;
//        private String backupDriver;
//
//        MessageBlockContext(String name, MessageBlockContext parent) {
//            this.name = name;
//            this.parent = parent;
//        }
//
//        @Override
//        public String toString() {
//            StringBuilder string = new StringBuilder();
//
//            string.append(name);
//            MessageBlockContext context = this;
//            while ((context = context.parent) != null) {
//                string.insert(0, "->");
//                string.insert(0, context.name);
//            }
//            string.insert(0, "Context: ");
//
//            if (getDriver() != null) {
//                string.append(" driver:").append(getDriver()).append(", ");
//            }
//            if (getDelivery() != null) {
//                string.append(" delivery: ").append(getDelivery()).append(", ");
//            }
//            if (getSplitRestaurant() != null) {
//                string.append(" splitRestaurant: ").append(getSplitRestaurant()).append(", ");
//            }
//            if (getPickupRestaurant() != null) {
//                string.append(" pickupRestaurant: ").append(getPickupRestaurant()).append(", ");
//            }
//            if (getBackupDriver() != null) {
//                string.append(" backupDriver: ").append(getBackupDriver()).append(", ");
//            }
//
//            return string.toString();
//        }
//
//        void setMessageBlockContext(long postNumber, String name) {
//            messageBlockName = name;
//            messageBlockPost = postNumber;
//        }
//
//        void setDriver(Driver driver) {
//            this.driver = driver;
//        }
//
//        void setDelivery(Delivery delivery) {
//            this.delivery = delivery;
//        }
//
//        void setSplitRestaurant(Restaurant splitRestaurant) {
//            this.splitRestaurant = splitRestaurant;
//        }
//
//        void setPickupRestaurant(Restaurant pickupRestaurant) {
//            this.pickupRestaurant = pickupRestaurant;
//        }
//
//        void setBackupDriver(String backupDriver) {
//            this.backupDriver = backupDriver;
//        }
//
//        String getBlockName() {
//
//            MessageBlockContext baseContext = this;
//
//            while (baseContext.parent != null) {
//                baseContext = baseContext.parent;
//            }
//
//            assert baseContext.messageBlockName != null : baseContext;
//            return baseContext.messageBlockName;
//        }
//
//        long getPostNumber() {
//
//            MessageBlockContext baseContext = this;
//
//            while (baseContext.parent != null) {
//                baseContext = baseContext.parent;
//            }
//
//            assert baseContext.messageBlockName != null : baseContext;
//            return baseContext.messageBlockPost;
//        }
//
//        Driver getDriver() {
//            if (driver != null) {
//                return driver;
//            }
//
//            if (parent != null) {
//                return parent.getDriver();
//            }
//
//            return null;
//        }
//
//        Restaurant getSplitRestaurant() {
//            if (splitRestaurant != null) {
//                return splitRestaurant;
//            }
//
//            if (parent != null) {
//                return parent.getSplitRestaurant();
//            }
//
//            return null;
//        }
//
//        Restaurant getPickupRestaurant() {
//            if (pickupRestaurant != null) {
//                return pickupRestaurant;
//            }
//
//            if (parent != null) {
//                return parent.getPickupRestaurant();
//            }
//
//            return null;
//        }
//
//        Delivery getDelivery() {
//            if (delivery != null) {
//                return delivery;
//            }
//
//            if (parent != null) {
//                return parent.getDelivery();
//            }
//
//            return null;
//        }
//
//        String getBackupDriver() {
//            if (backupDriver != null) {
//                return backupDriver;
//            }
//
//            if (parent != null) {
//                return parent.getBackupDriver();
//            }
//
//            return null;
//        }
//
//        // FIX THIS, DS: pass in an element and get the line number from it.
//        String formatException(String message) {
//            String blockName = getBlockName();
//            long postNumber = getPostNumber();
//
//            return  "Post: " + postNumber + ", block: " + blockName + ": " + message;
//        }
//    }
}
