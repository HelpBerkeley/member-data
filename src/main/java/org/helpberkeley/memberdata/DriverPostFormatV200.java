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

public class DriverPostFormatV200 extends DriverPostFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverPostFormatV200.class);

    private final ApiClient apiClient;
    private final List<MessageBlock> driverPostMessageBlocks = new ArrayList<>();
    private final List<MessageBlock> groupInstructionMessageBlocks = new ArrayList<>();
    private final List<MessageBlock> backupDriverMessageBlocks = new ArrayList<>();
    private final Map<String, User> users;
    private final StringBuilder statusMessages = new StringBuilder();
    private final int driverTemplateQuery;
    private final int groupTemplateQuery;
    private final int restaurantTemplateQuery;

    DriverPostFormatV200(ApiClient apiClient, Map<String, User> users, String routedDeliveries) {
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
    DriverPostFormatV200(ApiClient apiClient, Map<String, User> users, String routedDeliveries,
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

        long numVeggie = 0;
        long numNormal = 0;

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

            for (Delivery delivery : driver.getDeliveries()) {
                numNormal += Long.parseLong(((DeliveryV200)delivery).getNormalRations());
                numVeggie += Long.parseLong(((DeliveryV200)delivery).getVeggieRations());
            }
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

        summary.append("\n");
        summary.append("|Orders|Meals|Drivers|Normal rations|Veggie rations|\n");
        summary.append("|---|---|---|---|---|\n");
        summary.append("|");
        summary.append(totalOrders);
        summary.append("|");
        summary.append((numNormal + numVeggie) * 2);
        summary.append("|");
        summary.append(drivers.size());
        summary.append("|");
        summary.append(numNormal);
        summary.append("|");
        summary.append(numVeggie);
        summary.append("|\n");

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

        DeliveryV200 delivery = (DeliveryV200)context.getDelivery();

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
            case "Consumer.Normal":
                value = delivery.getNormalRations();
                break;
            case "Consumer.Veggie":
                value = delivery.getVeggieRations();
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
        DeliveryV200 delivery = (DeliveryV200)context.getDelivery();
        assert delivery.getRestaurant().equals(pickupRestaurant.getName()) :
                delivery.getRestaurant() + " != " + pickupRestaurant.getName();

        switch (refName) {
            case "Pickup.MemberName":
                value = delivery.getName();
                break;
            case "Pickup.UserName":
                value = delivery.getUserName();
                break;
            case "Pickup.Normal":
                value = delivery.getNormalRations();
                break;
            case "Pickup.Veggie":
                value = delivery.getVeggieRations();
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
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

            switch (refName) {
                case "ThisDriverRestaurant.IsSplit":
                    return globalRestaurant.getDrivers().size() > 1;
                case "ThisDriverRestaurant.NoPics":
                    return globalRestaurant.getNoPics();
                case "ThisDriverRestaurant.IsCleanup":
                    String driverUserName = context.getDriver().getUserName();
                    // FIX THIS, DS: what if we aren't in a split restaurant context?  Audit
                    ControlBlock.SplitRestaurant splitRestaurant = controlBlock.getSplitRestaurant(restaurantName);
                    // FIX THIS, DS: what if the control block is missing this split restaurant? Audit
                    return splitRestaurant.getCleanupDriverUserName().equals(driverUserName);
                case "ThisDriverRestaurant.AnyOrder":
                    return (restaurant.getOrders() != 0);
                default:
                    throw new MemberDataException(
                            context.formatException("Unknown boolean variable &{" + refName + "}"));
            }

        } else if (listName.equals("Consumer")) {
            Delivery delivery = context.getDelivery();

            if (refName.equals("Consumer.IsAltPhone")) {
                String altPhone = delivery.getAltPhone();
                value = ((! altPhone.isEmpty()) && (! altPhone.equalsIgnoreCase("none")));
            } else if (refName.equals("Consumer.IsCondo")) {
                value = delivery.isCondo();
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
        } else if (refName.equals("Driver.IsCleanup")) {
            String driverUserName = context.getDriver().getUserName();
            Restaurant restaurant = context.getSplitRestaurant();
            // FIX THIS, DS: what if we aren't in a split restaurant context?  Audit
            ControlBlock.SplitRestaurant splitRestaurant = controlBlock.getSplitRestaurant(restaurant.getName());
            // FIX THIS, DS: what if the control block is missing this split restaurant? Audit
            return splitRestaurant.getCleanupDriverUserName().equals(driverUserName);
        } else {
            throw new MemberDataException(context.formatException("Unknown boolean variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", listRef, value);
        return value;
    }
}