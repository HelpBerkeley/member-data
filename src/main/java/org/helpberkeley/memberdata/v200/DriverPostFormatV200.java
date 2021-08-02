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
package org.helpberkeley.memberdata.v200;

import org.helpberkeley.memberdata.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DriverPostFormatV200 extends DriverPostFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverPostFormatV200.class);

    private final StringBuilder statusMessages = new StringBuilder();
    private ControlBlockV200 controlBlock;

    public DriverPostFormatV200() {
        super();
    }

    @Override
    protected void initialize(String routedDeliveries, RequestType requestType) {
        loadLastRestaurantTemplate();
        loadDriverPostFormat();
        loadGroupPostFormat();
        loadBackupDriverPostFormat();
        loadRoutedDeliveries(routedDeliveries);
        auditControlBlock();
    }

    @Override
    protected int restaurantTemplateQueryID() {
        return Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE;
    }

    @Override
    protected int driverTemplateQueryID() {
        return Constants.QUERY_GET_DRIVERS_POST_FORMAT;
    }

    @Override
    protected int groupTemplateQueryID() {
        return Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT;
    }

    @Override
    public ControlBlock getControlBlock() {
        return controlBlock;
    }

    @Override
    public List<Driver> getDrivers() {
        return drivers;
    }

    @Override
    public String statusMessages() {
        return statusMessages.toString();
    }

    @Override
    public String generateSummary() {
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

            DriverV200 driverV200 = (DriverV200)driver;

            String originalStartTime = driverV200.getOriginalStartTime();
            String startTime = driverV200.getStartTime();

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
            RestaurantV200 restaurantV200 = (RestaurantV200) restaurant;
            String cleanupDriver;

            totalOrders += restaurantV200.getOrders();

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

    private void auditControlBlock() {

        List<Restaurant> splitRestaurants = new ArrayList<>();

        for (Restaurant restaurant : restaurants.values()) {
            if (restaurant.getDrivers().size() > 1) {
                splitRestaurants.add(restaurant);
            }
        }

        controlBlock.audit(users, restaurants, splitRestaurants);
        statusMessages.append(controlBlock.getWarnings());

        // Check for duplicates between backup drivers and drivers
        HashSet<String> backupDrivers = new HashSet<>(controlBlock.getBackupDrivers());

        for (Driver driver : drivers) {
            if (backupDrivers.contains(driver.getUserName())) {
                statusMessages.append(MessageFormat.format(ERROR_BACKUP_DRIVER_DUPLICATE, driver));
            }
        }
    }

    private void loadRoutedDeliveries(final String routedDeliveries) {
        WorkflowParser parser = WorkflowParser.create(restaurants, routedDeliveries);
        drivers = parser.drivers();
        ControlBlock cb = parser.getControlBlock();
        assert cb instanceof ControlBlockV200 : "Mismatched control block";
        controlBlock = (ControlBlockV200) cb;

        // Add all the individual driver pickups to the global restaurants,
        // so the we can detect split restaurants.
        for (Driver driver : drivers) {
            for (Restaurant pickup : driver.getPickups()) {
                RestaurantV200 restaurant = (RestaurantV200) restaurants.get(pickup.getName());

                // FIX THIS, DS: audit this earlier so that we can come up with a line number
                if (restaurant == null) {
                    throw new MemberDataException("Restaurant \"" + pickup.getName() + "\" from driver "
                            + driver.getUserName() + " not found.  Is it misspelled?");
                }

                restaurant.addDriver(driver);
                restaurant.addOrders(((RestaurantV200)pickup).getOrders());
            }
        }
    }

    @Override
    protected ProcessingReturnValue processStructRef(MessageBlockStructRef structRef, MessageBlockContext context) {
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
    protected ProcessingReturnValue processListRef(MessageBlockListRef listRef, MessageBlockContext context) {
        String listName = listRef.getListName();
        String refName = listRef.getName();
        ProcessingReturnValue returnValue;

        switch (listName) {
            case "ThisDriverRestaurant":
                returnValue = processPickupsListRef(listRef, context);
                break;
            case "Consumer":
                returnValue = processDeliveriesListRef(listRef.getName(), context);
                break;
            case "Driver":
                returnValue = processDriverListRef(listRef, context);
                break;
            case "SplitRestaurant":
                returnValue = processSplitRestaurantListRef(listRef, context);
                break;
            case "Pickup":
                returnValue = processThisRestaurantPickupListRef(refName, context);
                break;
            case "IRestaurant":
                returnValue = processItineraryRestaurantListRef(refName, context);
                break;
            case "IConsumer":
                returnValue = processItineraryDeliveryListRef(listRef.getName(), context);
                break;
            default:
                throw new MemberDataException(context.formatException(
                        "unknown list name &{" + listName + "} in " + "&{" + refName + "}"));
        }

        LOGGER.trace("&{{}} = \"{}\"", refName, returnValue.getOutput());
        return returnValue;
    }

    private ProcessingReturnValue processPickupsListRef(MessageBlockListRef listRef, MessageBlockContext context) {
        String refName = listRef.getName();
        String value;

        // FIX THIS, DS: unify Restaurant so that there are not multiple copies
        RestaurantV200 pickupRestaurant = (RestaurantV200) context.getPickupRestaurant();
        RestaurantV200 globalRestaurant = (RestaurantV200) restaurants.get(pickupRestaurant.getName());

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
            case "ThisDriverRestaurant.ThisDriverOrders":
                value = Long.toString(pickupRestaurant.getOrders());
                break;
            case "ThisDriverRestaurant.TotalOrders":
                value = Long.toString(globalRestaurant.getOrders());
                break;
            case "ThisDriverRestaurant.TotalDrivers":
                value = Long.toString(globalRestaurant.getDrivers().size());
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    @Override
    protected String versionSpecificDriverListRef(MessageBlockContext context, String refName) {

        String value;

        DriverV200 driver = (DriverV200) context.getDriver();
        String firstRestaurantName = driver.getFirstRestaurantName();
        Restaurant restaurant = restaurants.get(firstRestaurantName);
        assert restaurant != null : firstRestaurantName + " was not found the in restaurant template post";

        switch (refName) {
            case "Driver.FirstRestaurantName":
                value = firstRestaurantName;
                break;
            case "Driver.FirstRestaurantStartTime":
                value = driver.getStartTime();
                break;
            case "Driver.FirstRestaurantClosingTime":
                value = restaurant.getClosingTime();
                break;
            case "Driver.SplitRestaurantOrders":
                Restaurant splitRestaurant = context.getSplitRestaurant();
                value = Long.toString(driver.getOrders(splitRestaurant.getName()));
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return value;
    }

    private ProcessingReturnValue processSplitRestaurantListRef(
            MessageBlockListRef listRef, MessageBlockContext context) {

        String refName = listRef.getName();
        String value;

        RestaurantV200 splitRestaurant = (RestaurantV200) context.getSplitRestaurant();
        String name = splitRestaurant.getName();

        switch (refName) {
            case "SplitRestaurant.Name":
                value = name;
                break;
            case "SplitRestaurant.Emoji":
                value = splitRestaurant.getEmoji();
                break;
            case "SplitRestaurant.TotalOrders":
                value = Long.toString(splitRestaurant.getOrders());
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    private ProcessingReturnValue processDeliveriesListRef(String refName, MessageBlockContext context) {
        String value;
        DeliveryV200 delivery = (DeliveryV200) context.getDelivery();

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

    private ProcessingReturnValue processItineraryDeliveryListRef(String refName, MessageBlockContext context) {
        String value;
        DeliveryV200 delivery = (DeliveryV200) context.getItineraryDelivery();

        switch (refName) {
            case "IConsumer.Name":
                value = delivery.getName();
                break;
            case "IConsumer.UserName":
                value = delivery.getUserName();
                break;
            case "IConsumer.CompactPhone":
                value = compactPhone(delivery.getPhone());
                break;
            case "IConsumer.CompactAltPhone":
                value = compactPhone(delivery.getAltPhone());
                break;
            case "IConsumer.City":
                value = delivery.getCity();
                break;
            case "IConsumer.Address":
                value = delivery.getAddress();
                break;
            case "IConsumer.Details":
                value = delivery.getDetails();
                break;
            case "IConsumer.Restaurant":
                value = delivery.getRestaurant();
                break;
            case "IConsumer.RestaurantEmoji":
                value = restaurants.get(delivery.getRestaurant()).getEmoji();
                break;
            case "IConsumer.Normal":
                value = delivery.getNormalRations();
                break;
            case "IConsumer.Veggie":
                value = delivery.getVeggieRations();
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    private ProcessingReturnValue processThisRestaurantPickupListRef(
            String refName, MessageBlockContext context) {

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

    private ProcessingReturnValue processItineraryRestaurantListRef(
            String refName, MessageBlockContext context) {

        String value;
        RestaurantV200 restaurant = (RestaurantV200)context.getItineraryRestaurant();

        switch (refName) {
            case "IRestaurant.Name":
                value = restaurant.getName();
                break;
            case "IRestaurant.Emoji":
                value = restaurant.getEmoji();
                break;
            case "IRestaurant.Address":
                value = restaurant.getAddress();
                break;
            case "IRestaurant.Details":
                value = restaurant.getDetails();
                break;
            case "IRestaurant.ThisDriverRestaurantNoPics":
                // FIX THIS, DS: can we find this in the drivers version of this restaurant?
                RestaurantV200 globalRestaurant = (RestaurantV200) restaurants.get(restaurant.getName());
                value = Boolean.toString(globalRestaurant.getNoPics());
                break;
            case "IRestaurant.ThisDriverOrders":
                // FIX THIS, DS: is this correct?
                value = Long.toString(restaurant.getOrders());
                break;
            default:
                throw new MemberDataException(
                        context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    @Override
    protected ProcessingReturnValue processLoopListRef(
            MessageBlockLoop loop, MessageBlockListRef listRef, MessageBlockContext context) {

        String refName = listRef.getName();
        ProcessingReturnValue returnValue;

        // FIX THIS, DS generalize this nested loop handling

        switch (refName) {
            case "SplitRestaurant.Driver":
                returnValue = processSplitRestaurantDrivers(loop, context);
                break;
            case "ThisDriverRestaurant.Pickup":
                returnValue = processRestaurantPickups(loop, context);
                break;
            default:
                throw new MemberDataException(context.formatException("unknown loop list ref &{" + listRef + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, returnValue.getOutput());
        return returnValue;
    }

    private ProcessingReturnValue processSplitRestaurantDrivers(MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder processedLoop = new StringBuilder();
        MessageBlockContext driverContext = new MessageBlockContext("SplitRestaurantDrivers", context);

        Restaurant splitRestaurant = context.getSplitRestaurant();

        for (Driver driver : splitRestaurant.getDrivers().values()) {

            driverContext.setDriver(driver);

            for (MessageBlockElement element : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(element, driverContext);
                processedLoop.append(returnValue.getOutput());

                if (returnValue.getStatus() == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, processedLoop);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, processedLoop.toString());
    }

    @Override
    protected boolean processBooleanSimpleRef(MessageBlockSimpleRef element, MessageBlockContext context) {
        String refName = element.getName();
        Driver driver = context.getDriver();
        boolean value;

        switch (refName) {
            case "ThisDriverSplitsAnyRestaurant":
                value = driverHasSplitRestaurant(driver);
                break;
            case "ThisDriverAnyCondo":
                value = driver.hasCondo();
                break;
            case "AnySplitRestaurants":
                value = anySplitRestaurants();
                break;
            case "IsFirstRestaurantClosingBefore545PM":
                String restaurantName = driver.getFirstRestaurantName();
                Restaurant restaurant = restaurants.get(restaurantName);
                value = restaurant.closesBefore(545);
                break;
            default:
                throw new MemberDataException(context.formatException("unknown boolean variable ${" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", element, value);
        return value;
    }

    // Do simple variable replacement
    //
    @Override
    protected String  versionSpecificSimpleRef(MessageBlockContext context, String varName) {
        String value;

        final String firstRestaurant = context.getDriver().getFirstRestaurantName();
        final Restaurant restaurant = restaurants.get(firstRestaurant);
        assert restaurant != null : firstRestaurant + " was not found the in restaurant template post";
        Driver driver = context.getDriver();

        switch (varName) {
            case "ThisDriverFirstRestaurantStartTime":
                value = driver.getStartTime();
                break;
            case "ThisDriverFirstRestaurantClosingTime":
                value = restaurant.getClosingTime();
                break;
            default:
                throw new MemberDataException(context.formatException("unknown variable ${" + varName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", varName, value);
        return value;
    }

    @Override
    protected boolean processBooleanListRef(MessageBlockListRef listRef, MessageBlockContext context) {
        String listName = listRef.getListName();
        String refName = listRef.getName();
        boolean value;

        if (listName.equals("ThisDriverRestaurant")) {
            RestaurantV200 restaurant = (RestaurantV200) context.getPickupRestaurant();
            String restaurantName = restaurant.getName();
            RestaurantV200 globalRestaurant = (RestaurantV200) restaurants.get(restaurantName);

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
                value = ((!altPhone.isEmpty()) && (!altPhone.equalsIgnoreCase("none")));
            }
            else if (refName.equals("Consumer.IsCondo")) {
                value = delivery.isCondo();
            }
            else {
                throw new MemberDataException(
                        context.formatException("Unknown boolean variable &{" + refName + "}"));
            }
        } else if (listName.equals("IConsumer")) {
            Delivery delivery = context.getItineraryDelivery();

            if (refName.equals("IConsumer.IsAltPhone")) {
                String altPhone = delivery.getAltPhone();
                value = ((!altPhone.isEmpty()) && (!altPhone.equalsIgnoreCase("none")));
            }
            else if (refName.equals("IConsumer.IsCondo")) {
                value = delivery.isCondo();
            }
            else {
                throw new MemberDataException(
                        context.formatException("Unknown boolean variable &{" + refName + "}"));
            }
        } else if (listName.equals("Itinerary")) {
            ItineraryStop itineraryStop = context.getItineraryStop();

            if (refName.equals("Itinerary.IsRestaurant")) {
                value = (itineraryStop.getType() == ItineraryStopType.PICKUP);
            } else if (refName.equals("Itinerary.IsDelivery")) {
                value = (itineraryStop.getType() == ItineraryStopType.DELIVERY);
            }
            else {
                    throw new MemberDataException(
                            context.formatException("Unknown boolean variable &{" + refName + "}"));
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

    private ProcessingReturnValue processRestaurantPickups(
            MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        Driver driver = context.getDriver();
        MessageBlockContext deliveryContext = new MessageBlockContext("Delivery", context);

        LOGGER.trace("processRestaurantPickups: {}", deliveryContext);

        Restaurant restaurant = context.getPickupRestaurant();

        // Look through deliveries and find consumers/orders for this restaurant

        for (Delivery delivery : driver.getDeliveries()) {
            if (((DeliveryV200)delivery).getRestaurant().equals(restaurant.getName())) {
                context.setDelivery(delivery);
                for (MessageBlockElement loopElement : loop.getElements()) {
                    ProcessingReturnValue returnValue = processElement(loopElement, deliveryContext);
                    output.append(returnValue.getOutput());

                    if (returnValue.getStatus() == ProcessingStatus.CONTINUE) {
                        break;
                    }
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    private boolean driverHasSplitRestaurant(final Driver driver) {

        boolean hasSplit = false;

        for (Restaurant pickup : driver.getPickups()) {
            Restaurant restaurant = restaurants.get(pickup.getName());

            if (restaurant.getDrivers().size() > 1) {
                hasSplit = true;
                break;
            }
        }

        LOGGER.trace("Driver {} has{} split restaurants", driver.getUserName(), hasSplit ? "" : " no");

        return hasSplit;
    }

    private boolean anySplitRestaurants() {
        for (Restaurant restaurant : restaurants.values()) {
            if (restaurant.getDrivers().size() > 1) {
                return true;
            }
        }

        return false;
    }
}
