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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriverPostFormatV300 extends DriverPostFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverPostFormatV300.class);

    private static final String STD_MEALS = "StandardMeals";
    private static final String ALT_MEALS = "AlternateMeals";
    private static final String STD_GROCERY = "StandardGrocery";
    private static final String ALT_GROCERY = "AlternateGrocery";

    private static final String CONSUMER_STD_MEALS = "Consumer." + STD_MEALS;
    private static final String CONSUMER_ALT_MEALS = "Consumer." + ALT_MEALS;
    private static final String CONSUMER_STD_GROCERY = "Consumer." + STD_GROCERY;
    private static final String CONSUMER_ALT_GROCERY = "Consumer." + ALT_GROCERY;

    private static final String THIS_DRIVER_RESTAURANT_STD_MEALS = "ThisDriverRestaurant." + STD_MEALS;
    private static final String THIS_DRIVER_RESTAURANT_ALT_MEALS = "ThisDriverRestaurant." + ALT_MEALS;
    private static final String THIS_DRIVER_RESTAURANT_STD_GROCERY = "ThisDriverRestaurant." + STD_GROCERY;
    private static final String THIS_DRIVER_RESTAURANT_ALT_GROCERY = "ThisDriverRestaurant." + ALT_GROCERY;

    private final ApiClient apiClient;
    private final List<MessageBlock> driverPostMessageBlocks = new ArrayList<>();
    private final List<MessageBlock> groupInstructionMessageBlocks = new ArrayList<>();
    private final List<MessageBlock> backupDriverMessageBlocks = new ArrayList<>();
    private final Map<String, User> users;
    private final StringBuilder statusMessages = new StringBuilder();
    private final int driverTemplateQuery;
    private final int groupTemplateQuery;
    private final int restaurantTemplateQuery;

    DriverPostFormatV300(ApiClient apiClient, Map<String, User> users, String routedDeliveries) {
        super(apiClient, users, routedDeliveries);
        this.apiClient = apiClient;
        this.users = users;
        // FIX THIS, DS: set up invalid query id?
        this.driverTemplateQuery = 0;
        this.groupTemplateQuery = 0;
        this.restaurantTemplateQuery = 0;

        initialize(routedDeliveries);
    }

    // FIX THIS, DS: cleanup duplicated code in ctor
    DriverPostFormatV300(ApiClient apiClient, Map<String, User> users, String routedDeliveries,
             int restaurantTemplateQuery, int driverTemplateQuery, int groupTemplateQuery) {

        super(apiClient, users, routedDeliveries, driverTemplateQuery, groupTemplateQuery);
        this.apiClient = apiClient;
        this.users = users;
        this.driverTemplateQuery = driverTemplateQuery;
        this.groupTemplateQuery = groupTemplateQuery;
        this.restaurantTemplateQuery = restaurantTemplateQuery;
        initialize(routedDeliveries);
    }

    @Override
    void initialize(String routedDeliveries) {
        loadLastRestaurantTemplate();
        loadDriverPostFormat();
        loadGroupPostFormat();
        loadBackupDriverPostFormat();
        loadRoutedDeliveries(routedDeliveries);
        auditControlBlock();
    }

    @Override
    List<Driver> getDrivers() {
        return drivers;
    }

    @Override
    Map<String, Restaurant> getRestaurants() {
        return restaurants;
    }

    @Override
    String statusMessages() {
        return statusMessages.toString();
    }

    @Override
    String generateSummary() {
        StringBuilder summary = new StringBuilder();

        if (! controlBlock.restaurantsAuditDisabled()) {
            // Restaurants with no drivers
            for (Restaurant restaurant : restaurants.values()) {
                if (restaurant.getDrivers().size() == 0) {
                    summary.append("No drivers going to ").append(restaurant.getName()).append("\n");
                }
            }
        }
        summary.append("\n");

        long numStdMeals = 0;
        long numAltMeals = 0;
        long numStdGrocvery = 0;
        long numAltGrocery = 0;

        for (Driver driver : drivers) {
            String originalStartTime = driver.getOriginalStartTime();
            String startTime = driver.getStartTime();

            if (! startTime.equals(originalStartTime)) {

                summary.append("Driver ").append(driver.getUserName())
                        .append(", start time for ").append(driver.getFirstRestaurantName())
                        .append(" adjusted to ").append(startTime).append(" from ").append(originalStartTime)
                        .append('\n');
            }

            for (String warning : driver.getWarningMessages()) {
                summary.append("Warning: driver ").append(driver.getUserName())
                        .append(" ").append(warning).append('\n');
            }

            // FIX THIS, DS: re-implement
//            for (Delivery delivery : driver.getDeliveries()) {
//                numNormal += Long.parseLong(delivery.getNormalRations());
//                numVeggie += Long.parseLong(delivery.getVeggieRations());
//            }
        }

        summary.append("\n");

        // Split restaurants / cleanup drivers / orders

        long totalOrders = 0;
        boolean headerAdded = false;

        for (Restaurant restaurant : restaurants.values()) {
            String cleanupDriver;

            totalOrders += restaurant.getOrders();

            if (restaurant.getDrivers().size() < 2) {
                continue;
            }

            if (! controlBlock.splitRestaurantAuditsDisabled()) {
                if (!headerAdded) {
                    summary.append("|Split Restaurants|Cleanup Driver|\n");
                    summary.append("|---|---|\n");

                    headerAdded = true;
                }
            }

            if (controlBlock.splitRestaurantAuditsDisabled()) {
                cleanupDriver = "";
            } else {
                cleanupDriver = controlBlock.getSplitRestaurant(restaurant.getName()).getCleanupDriverUserName();
            }

            if (! controlBlock.splitRestaurantAuditsDisabled()) {
                summary.append("|");
                summary.append(restaurant.getName());
                summary.append("|");
                summary.append(cleanupDriver);
                summary.append("|\n");
            }
        }

        // Total

        // Total orders: 19    Meals: 56  Drivers on the road: 4  Normal rations: 24   Veggie rations: 4

        // | Orders | Meals | Drivers | Normal rations| Veggie rations|
        //|---|---|---|---|---|
        //| 10 | 30 | 3 | 14 | 1 |

        // FIX THIS, DS: re-implement
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

        return summary.toString();
    }

    @Override
    List<String> generateDriverPosts() {

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

    @Override
    String generateGroupInstructionsPost() {

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

    @Override
    String generateBackupDriverPost() {

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

    private void auditControlBlock() {

        List<Restaurant> splitRestaurants = new ArrayList<>();

        for (Restaurant restaurant : restaurants.values()) {
            if (restaurant.getDrivers().size() > 1) {
                splitRestaurants.add(restaurant);
            }
        }

        controlBlock.audit(users, restaurants, splitRestaurants);
        statusMessages.append(controlBlock.getWarnings());
    }

    private void loadLastRestaurantTemplate() {
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

    private void loadDriverPostFormat() {
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

    private void loadGroupPostFormat() {
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

    private void loadBackupDriverPostFormat() {
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

    private void loadRoutedDeliveries(final String routedDeliveries) {
        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, routedDeliveries);
        drivers = parser.drivers();
        controlBlock = parser.getControlBlock();

        // Add all the individual driver pickups to the global restaurants,
        // so the we can detect split restaurants.
        for (Driver driver : drivers) {
            for (Restaurant pickup : driver.getPickups()) {
                Restaurant restaurant = restaurants.get(pickup.getName());

                // FIX THIS, DS: audit this earlier so that we can come up with a line number
                if (restaurant == null) {
                    throw new MemberDataException("Restaurant \"" + pickup.getName() + "\" from driver "
                            + driver.getUserName() + " not found.  Is it misspelled?");
                }

                restaurant.addDriver(driver);
                restaurant.addOrders(pickup.getOrders());
            }
        }
    }

    @Override
    ProcessingReturnValue processStructRef(MessageBlockStructRef structRef, MessageBlockContext context) {
        String refName = structRef.getName();
        String value;

        switch (refName) {
            case "OnCallOpsManager.UserName":
                // FIX THIS, DS: how are we handling multiple ops managers?
                value = controlBlock.getFirstOpsManager().getUserName();
                break;
            case "OnCallOpsManager.CompactPhone":
                // FIX THIS, DS: how are we handling multiple ops managers?
                value = compactPhone(controlBlock.getFirstOpsManager().getPhone());
                break;
            case "BackupDriver.UserName":
                value = compactPhone(context.getBackupDriver());
                break;
            default:
                throw new MemberDataException(context.formatException("unknown struct variable ${" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    @Override
    ProcessingReturnValue processDeliveriesListRef(MessageBlockListRef listRef, MessageBlockContext context) {
        String refName = listRef.getName();
        String value;

        DeliveryV300 delivery = (DeliveryV300)context.getDelivery();

        switch (refName) {
            case "Consumer.Name":
                value = delivery.getName();
                break;
            case "Consumer.UserName":
                value = delivery.getUserName();
                break;
            case "Consumer.CompactPhone":
                value = compactPhone(delivery.getPhone());
                break;
            case "Consumer.CompactAltPhone":
                value = compactPhone(delivery.getAltPhone());
                break;
            case "Consumer.City":
                value = delivery.getCity();
                break;
            case "Consumer.Address":
                value = delivery.getAddress();
                break;
            case "Consumer.Details":
                value = delivery.getDetails();
                break;
            case "Consumer.Restaurant":
                value = delivery.getRestaurant();
                break;
            case "Consumer.RestaurantEmoji":
                value = restaurants.get(delivery.getRestaurant()).getEmoji();
                break;
            case CONSUMER_STD_MEALS:
                value = delivery.getStdMeals();
                break;
            case CONSUMER_ALT_MEALS:
                value = delivery.getAltMeals();
                break;
            case "Consumer.AlternateMealType":
                value = delivery.getTypeMeal();
                break;
            case "Consumer.StandardGrocery":
                value = delivery.getStdGrocery();
                break;
            case "Consumer.AlternateGrocery":
                value = delivery.getAltGrocery();
                break;
            case "Consumer.AlternateGroceryType":
                value = delivery.getTypeGrocery();
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    @Override
    ProcessingReturnValue processThisRestaurantPickupListRef(
            MessageBlockListRef listRef, MessageBlockContext context) {

        String refName = listRef.getName();
        String value;

        Restaurant pickupRestaurant = context.getPickupRestaurant();
        DeliveryV300 delivery = (DeliveryV300)context.getDelivery();
        assert delivery.getRestaurant().equals(pickupRestaurant.getName()) :
                delivery.getRestaurant() + " != " + pickupRestaurant.getName();

        switch (refName) {
            case "Pickup.MemberName":
                value = delivery.getName();
                break;
            case "Pickup.UserName":
                value = delivery.getUserName();
                break;
            case "Pickup.StandardMeals":
                value = delivery.getStdMeals();
                break;
            case "Pickup.AlternateMeals":
                value = delivery.getAltMeals();
                break;
            case "Pickup.AlternateMealType":
                value = delivery.getTypeMeal();
                break;
            case "Pickup.StandardGrocery":
                value = delivery.getStdGrocery();
                break;
            case "Pickup.AlternateGrocery":
                value = delivery.getAltGrocery();
                break;
            case "Pickup.AlternateGroceryType":
                value = delivery.getTypeGrocery();
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    @Override
    ProcessingReturnValue processLoopListRef(
            MessageBlockLoop loop, MessageBlockListRef listRef, MessageBlockContext context) {

        String refName = listRef.getName();
        ProcessingReturnValue returnValue;

        // FIX THIS, DS generalize this nested loop handling

        switch (refName) {
            case "ThisDriverRestaurant.AlternateMeals":
                returnValue = processAlternateMealsLoopRef(loop, context);
                break;
            case "ThisDriverRestaurant.AlternateGroceriesMeals":
                returnValue = processAlternateGroceriesLoopRef(loop, context);
                break;
            default:
                throw new MemberDataException(context.formatException("unknown loop list ref &{" + listRef + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, returnValue.output);
        return returnValue;
    }

    @Override
    boolean processBooleanSimpleRef(MessageBlockSimpleRef element, MessageBlockContext context) {
        String refName = element.getName();
        Driver driver = context.getDriver();
        boolean value;

        switch (refName) {
            case "ThisDriverAnyCondo":
                value = driver.hasCondo();
                break;
            default:
                throw new MemberDataException(context.formatException("unknown boolean variable ${" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", element, value);
        return value;
    }

    @Override
    boolean processBooleanListRef(MessageBlockListRef listRef, MessageBlockContext context) {
        String listName = listRef.getListName();
        String refName = listRef.getName();
        boolean value;

        if (listName.equals("ThisDriverRestaurant")) {
            Restaurant restaurant = context.getPickupRestaurant();
            String restaurantName = restaurant.getName();
            Restaurant globalRestaurant = restaurants.get(restaurantName);
            Driver driver = context.getDriver();

            switch (refName) {
                case "ThisDriverRestaurant.AnyMealsOrGroceries":
                    return anyMealsOrGroceries(restaurantName, context.getDriver());
                case THIS_DRIVER_RESTAURANT_STD_MEALS:
                    return anyStandardMeals(restaurantName, driver);
                case THIS_DRIVER_RESTAURANT_ALT_MEALS:
                    return anyAlternateMeals(restaurantName, driver);
                case THIS_DRIVER_RESTAURANT_STD_GROCERY:
                    return anyStandardGroceries(restaurantName, driver);
                case THIS_DRIVER_RESTAURANT_ALT_GROCERY:
                    return anyAlternateGroceries(restaurantName, driver);
                default:
                    throw new MemberDataException(
                            context.formatException("Unknown boolean variable &{" + refName + "}"));
            }

        } else if (listName.equals("Consumer")) {
            DeliveryV300 delivery = (DeliveryV300) context.getDelivery();

            if (refName.equals("Consumer.IsAltPhone")) {
                String altPhone = delivery.getAltPhone();
                value = ((! altPhone.isEmpty()) && (! altPhone.equalsIgnoreCase("none")));
            } else if (refName.equals("Consumer.IsCondo")) {
                value = delivery.isCondo();
            } else if (refName.equals(CONSUMER_STD_MEALS)) {
                value = Integer.valueOf(delivery.getStdMeals()) > 0;
            } else if (refName.equals(CONSUMER_ALT_MEALS)) {
                value = Integer.valueOf(delivery.getAltMeals()) > 0;
            } else if (refName.equals(CONSUMER_STD_GROCERY)) {
                value = Integer.valueOf(delivery.getStdGrocery()) > 0;
            } else if (refName.equals(CONSUMER_ALT_GROCERY)) {
                value = Integer.valueOf(delivery.getAltGrocery()) > 0;
            } else {
                throw new MemberDataException(context.formatException("Unknown boolean variable &{" + refName + "}"));
            }
        } else if (refName.equals("Driver.IsFirstRestaurantClosingBefore7PM")) {
            Driver driver = context.getDriver();
            String firstName = driver.getFirstRestaurantName();
            Restaurant firstRestaurant = restaurants.get(firstName);
            value = firstRestaurant.closesBefore(700);
        } else if (refName.equals("Driver.IsFirstRestaurantClosingBefore545PM")) {
            Driver driver = context.getDriver();
            String firstName = driver.getFirstRestaurantName();
            Restaurant firstRestaurant = restaurants.get(firstName);
            value = firstRestaurant.closesBefore(545);
        } else {
            throw new MemberDataException(context.formatException("Unknown boolean variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", listRef, value);
        return value;
    }

    @Override
    ProcessingReturnValue processListRef(MessageBlockListRef listRef, MessageBlockContext context) {
        String listName = listRef.getListName();
        String refName = listRef.getName();
        ProcessingReturnValue returnValue;

        switch (listName) {
            case "ThisDriverRestaurant":
                returnValue = processPickupsListRef(listRef, context);
                break;
            case "Consumer":
                returnValue = processDeliveriesListRef(listRef, context);
                break;
            default:
                throw new MemberDataException(context.formatException(
                        "unknown list name &{" + listName + "} in " + "&{" + refName + "}"));
        }

        LOGGER.trace("&{{}} = \"{}\"", refName, returnValue.output);
        return returnValue;
    }

    private ProcessingReturnValue processPickupsListRef(MessageBlockListRef listRef, MessageBlockContext context) {
        String refName = listRef.getName();
        String value;

        // FIX THIS, DS: unify Restaurant so that there are not multiple copies
        Restaurant pickupRestaurant = context.getPickupRestaurant();
        Restaurant globalRestaurant = restaurants.get(pickupRestaurant.getName());
        Driver driver = context.getDriver();

        switch (refName) {
            case "ThisDriverRestaurant.Name":
                value = pickupRestaurant.getName();
                break;
            case "ThisDriverRestaurant.Emoji":
                value = globalRestaurant.getEmoji();
                break;
            case "ThisDriverRestaurant.Address":
                value = pickupRestaurant.getAddress();
                break;
            case "ThisDriverRestaurant.Details":
                value = pickupRestaurant.getDetails();
                break;
            case "ThisDriverRestaurant.StandardMeals":
                value = getStandardMeals(pickupRestaurant.getName(), driver);
                break;
            case "ThisDriverRestaurant.AlternateMeals":
                value = "FIX THIS, IMPLEMENT ThisDriverRestaurant.AlternateMeals";
                break;
            case "ThisDriverRestaurant.StandardGroceries":
                value = getStandardGroceries(pickupRestaurant.getName(), driver);
                break;
            case "ThisDriverRestaurant.AlternateGroceries":
                value = "FIX THIS, IMPLEMENT ThisDriverRestaurant.AlternateGroceries";
                break;
            case "ThisDriverRestaurant.AnyMealsOrGroceries":
                value = "FIX THIS, IMPLEMENT ThisDriverRestaurant.AnyMealsOrGroceries";
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    private String getStandardMeals(String restaurantName, Driver driver) {
        int total = 0;

        for (Delivery delivery : driver.getDeliveries()) {
            DeliveryV300 deliveryV300 = (DeliveryV300)delivery;
            total += Integer.valueOf(deliveryV300.getStdMeals());
        }

        return String.valueOf(total);
    }

    private String getStandardGroceries(String restaurantName, Driver driver) {
        int total = 0;

        for (Delivery delivery : driver.getDeliveries()) {
            DeliveryV300 deliveryV300 = (DeliveryV300)delivery;
            total += Integer.valueOf(deliveryV300.getStdGrocery());
        }

        return String.valueOf(total);
    }

    private boolean anyMealsOrGroceries(String restaurantName, Driver driver) {
        for (Delivery delivery : driver.getDeliveries()) {
            DeliveryV300 deliveryV300 = (DeliveryV300)delivery;

            if (! (deliveryV300.getStdMeals().equals("0")
                    && deliveryV300.getAltMeals().equals("0")
                    && deliveryV300.getStdGrocery().equals("0")
                    && deliveryV300.getAltGrocery().equals("0"))) {
                return true;
            }
        }

        return false;
    }

    private boolean anyStandardMeals(String restaurantName, Driver driver) {
        return ! getStandardMeals(restaurantName, driver).equals("0");
    }

    private boolean anyAlternateMeals(String restaurantName, Driver driver) {
        for (Delivery delivery : driver.getDeliveries()) {
            DeliveryV300 deliveryV300 = (DeliveryV300)delivery;

            if (! deliveryV300.getAltMeals().equals("0")) {
                return true;
            }
        }

        return false;
    }

    private boolean anyStandardGroceries(String restaurantName, Driver driver) {
        return ! getStandardGroceries(restaurantName, driver).equals("0");
    }

    private boolean anyAlternateGroceries(String restaurantName, Driver driver) {
        for (Delivery delivery : driver.getDeliveries()) {
            DeliveryV300 deliveryV300 = (DeliveryV300)delivery;

            if (! deliveryV300.getAltGrocery().equals("0")) {
                return true;
            }
        }

        return false;
    }

    protected final  ProcessingReturnValue processAlternateMealsLoopRef(
            MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext altMealsContext = new MessageBlockContext("AlternateMeals", context);
        altMealsContext.setPickupRestaurant(context.getPickupRestaurant());

        LOGGER.trace("processAlternateMeals: {}", altMealsContext);

        Restaurant restaurant = context.getPickupRestaurant();

//        // Look through deliveries and find consumers/orders for this restaurant
//
//        for (Delivery delivery : driver.getDeliveries()) {
//            if (delivery.getRestaurant().equals(restaurant.getName())) {
//                context.setDelivery(delivery);
//                for (MessageBlockElement loopElement : loop.getElements()) {
//                    ProcessingReturnValue returnValue = processElement(loopElement, deliveryContext);
//                    output.append(returnValue.output);
//
//                    if (returnValue.status == ProcessingStatus.CONTINUE) {
//                        break;
//                    }
//                }
//            }
//        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    protected final  ProcessingReturnValue processAlternateGroceriesLoopRef(
            MessageBlockLoop loop, MessageBlockContext context) {
        return null;
    }
}