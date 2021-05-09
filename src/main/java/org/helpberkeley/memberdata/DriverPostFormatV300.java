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
import java.util.Iterator;
import java.util.List;

public class DriverPostFormatV300 extends DriverPostFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverPostFormatV300.class);

    private static final String STD_MEALS = "StandardMeals";
    private static final String ALT_MEALS = "AlternateMeals";
    private static final String STD_MEAL = "StandardMeal";
    private static final String ALT_MEAL = "AlternateMeal";
    private static final String STD_GROCERY = "StandardGrocery";
    private static final String ALT_GROCERY = "AlternateGrocery";

    private static final String CONSUMER_STD_MEAL = "Consumer." + STD_MEAL;
    private static final String CONSUMER_ALT_MEAL = "Consumer." + ALT_MEAL;
    private static final String CONSUMER_STD_GROCERY = "Consumer." + STD_GROCERY;
    private static final String CONSUMER_ALT_GROCERY = "Consumer." + ALT_GROCERY;
    private static final String CONSUMER_DETAILS = "Consumer.Details";

    private static final String THIS_DRIVER_RESTAURANT_STD_MEALS = "ThisDriverRestaurant." + STD_MEALS;
    private static final String THIS_DRIVER_RESTAURANT_ALT_MEALS = "ThisDriverRestaurant." + ALT_MEALS;
    private static final String THIS_DRIVER_RESTAURANT_STD_GROCERY = "ThisDriverRestaurant." + STD_GROCERY;
    private static final String THIS_DRIVER_RESTAURANT_ALT_GROCERY = "ThisDriverRestaurant." + ALT_GROCERY;

    private ControlBlockV300 controlBlock;
    private final StringBuilder statusMessages = new StringBuilder();
    private final List<MessageBlock> driversTableMessageBlocks = new ArrayList<>();
    private final List<MessageBlock> ordersTableMessageBlocks = new ArrayList<>();

    DriverPostFormatV300() {
        super();
    }

    @Override
    ControlBlock getControlBlock() {
        return controlBlock;
    }

    @Override
    void initialize(String routedDeliveries) {
        loadLastRestaurantTemplate();
        loadDriverPostFormat();
        loadGroupPostFormat();
        loadBackupDriverPostFormat();
        loadOrdersTablePostFormat();
        loadDriversTablePostFormat();
        loadRoutedDeliveries(routedDeliveries);
        auditControlBlock();
        setDriverStartTimes();
    }

    @Override
    public List<Driver> getDrivers() {
        return drivers;
    }

    @Override
    String statusMessages() {
        return statusMessages.toString();
    }

    @Override
    public String generateSummary() {

        // FIX THIS, DS: implement
        return "Summary not yet implemented\n";
    }

    public String generateDriversTablePost() {

        StringBuilder post = new StringBuilder();

        MessageBlockContext context = new MessageBlockContext("Base", null);

        for (MessageBlock messageBlock : driversTableMessageBlocks) {

            context.setMessageBlockContext(messageBlock.getPostNumber(), messageBlock.getName());

            if (messageBlock.name.equalsIgnoreCase("comment")) {
                continue;
            }

            post.append(processMessageBlock(messageBlock, context));
        }

        return post.toString();
    }

    public String generateOrdersTablePost() {

        StringBuilder post = new StringBuilder();

        MessageBlockContext context = new MessageBlockContext("Base", null);

        for (MessageBlock messageBlock : ordersTableMessageBlocks) {

            context.setMessageBlockContext(messageBlock.getPostNumber(), messageBlock.getName());

            if (messageBlock.name.equalsIgnoreCase("comment")) {
                continue;
            }

            post.append(processMessageBlock(messageBlock, context));
        }

        return post.toString();
    }

    private void auditControlBlock() {

        controlBlock.audit(users, getDrivers());
        statusMessages.append(controlBlock.getWarnings());
    }

    private void loadRoutedDeliveries(final String routedDeliveries) {
        WorkflowParser parser = WorkflowParser.create(
                WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, restaurants, routedDeliveries);
        drivers = parser.drivers();

        ControlBlock cb = parser.getControlBlock();
        assert cb instanceof ControlBlockV300 : "Mismatched control block";
        controlBlock = (ControlBlockV300)cb;

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
            }
        }
    }

    // Do simple variable replacement
    //
    @Override
    String versionSpecificSimpleRef(MessageBlockContext context, String varName) {

        String value;

        switch (varName) {
            case "FirstPickupLocation":
                value = controlBlock.getMealSource();
                break;
            case "TotalStandardGrocery":
                value = getStandardGroceryTotal();
                break;
            case "TotalStandardMeal":
                value = getStandardMealTotal();
                break;
            default:
                throw new MemberDataException(context.formatException("unknown variable ${" + varName + "}"));
        }

        return value;
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

    private ProcessingReturnValue processDeliveriesListRef(MessageBlockListRef listRef, MessageBlockContext context) {
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
            case CONSUMER_STD_MEAL:
                value = delivery.getStdMeals();
                break;
            case CONSUMER_ALT_MEAL:
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

//    private ProcessingReturnValue processThisRestaurantPickupListRef(
//            MessageBlockListRef listRef, MessageBlockContext context) {
//
//        String refName = listRef.getName();
//        String value;
//
//        Restaurant pickupRestaurant = context.getPickupRestaurant();
//        DeliveryV300 delivery = (DeliveryV300)context.getDelivery();
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
//            case "Pickup.StandardMeals":
//                value = delivery.getStdMeals();
//                break;
//            case "Pickup.AlternateMeals":
//                value = delivery.getAltMeals();
//                break;
//            case "Pickup.AlternateMealType":
//                value = delivery.getTypeMeal();
//                break;
//            case "Pickup.StandardGrocery":
//                value = delivery.getStdGrocery();
//                break;
//            case "Pickup.AlternateGrocery":
//                value = delivery.getAltGrocery();
//                break;
//            case "Pickup.AlternateGroceryType":
//                value = delivery.getTypeGrocery();
//                break;
//            default:
//                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
//        }
//
//        LOGGER.trace("${{}} = \"{}\"", refName, value);
//        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
//    }

    @Override
    protected ProcessingReturnValue processVersionSpecificLoopListNameRef(
            String listName, MessageBlockLoop loop, MessageBlockContext context) {

        ProcessingReturnValue returnValue;

        switch (listName) {
            case "AlternateMeals":
                returnValue = processAltMealTypesLoopRef(loop, context);
                break;
            case "AlternateGroceries":
                returnValue = processAltGroceryTypesLoopRef(loop, context);
                break;
            case "PickupManager":
                returnValue = processPickupManagerLoopRef(loop, context);
                break;
            default:
                throw new MemberDataException(
                        context.formatException("unknown list variable &{" + listName + "}"));
        }
        LOGGER.trace("${{}} = \"{}\"", listName, returnValue);
        return returnValue;
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
            case "ThisDriverRestaurant.AlternateGroceries":
                returnValue = processAlternateGroceriesLoopRef(loop, context);
                break;
            case "Driver.Consumer":
                returnValue = processDeliveriesLoopRef(loop, context);
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

        if ("ThisDriverAnyCondo".equals(refName)) {
            value = driver.hasCondo();
        } else {
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
            Driver driver = context.getDriver();

            switch (refName) {
                case "ThisDriverRestaurant.AnyMealsOrGroceries":
                    return anyMealsOrGroceries(context.getDriver());
                case THIS_DRIVER_RESTAURANT_STD_MEALS:
                    return anyStandardMeals(restaurantName, driver);
                case THIS_DRIVER_RESTAURANT_ALT_MEALS:
                    return anyAlternateMeals(driver);
                case THIS_DRIVER_RESTAURANT_STD_GROCERY:
                    return anyStandardGroceries(restaurantName, driver);
                case THIS_DRIVER_RESTAURANT_ALT_GROCERY:
                    return anyAlternateGroceries(driver);
                default:
                    throw new MemberDataException(
                            context.formatException("Unknown boolean variable &{" + refName + "}"));
            }

        } else if (listName.equals("Consumer")) {
            DeliveryV300 delivery = (DeliveryV300) context.getDelivery();

            switch (refName) {
                case "Consumer.IsAltPhone":
                    String altPhone = delivery.getAltPhone();
                    value = ((!altPhone.isEmpty()) && (!altPhone.equalsIgnoreCase("none")));
                    break;
                case "Consumer.IsCondo":
                    value = delivery.isCondo();
                    break;
                case CONSUMER_STD_MEAL:
                    value = Integer.parseInt(delivery.getStdMeals()) > 0;
                    break;
                case CONSUMER_ALT_MEAL:
                    value = Integer.parseInt(delivery.getAltMeals()) > 0;
                    break;
                case CONSUMER_STD_GROCERY:
                    value = Integer.parseInt(delivery.getStdGrocery()) > 0;
                    break;
                case CONSUMER_ALT_GROCERY:
                    value = Integer.parseInt(delivery.getAltGrocery()) > 0;
                    break;
                case CONSUMER_DETAILS:
                    value = ! delivery.getDetails().isEmpty();
                    break;
                default:
                    throw new MemberDataException(context.formatException("Unknown boolean variable &{" + refName + "}"));
            }
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
            case "Driver":
                returnValue = processDriverListRef(listRef, context);
                break;
            case "AlternateMeals":
                returnValue = processAlternateMealsListRef(listRef, context);
                break;
            case "AlternateGroceries":
                returnValue = processAlternateGroceriesListRef(listRef, context);
                break;
            case "PickupManager":
                returnValue = processPickupManagerListRef(listRef, context);
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
            case "ThisDriverRestaurant.StandardGroceries":
                value = getStandardGroceries(pickupRestaurant.getName(), driver);
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    private ProcessingReturnValue processAlternateMealsListRef(
            MessageBlockListRef listRef, MessageBlockContext context) {

        String refName = listRef.getName();
        String value;

        switch (refName) {
            case "AlternateMeals.Type":
                value = context.getAlternateType();
                break;
            case "AlternateMeals.Count":
                value = getAlternateMealTotal(context);
                break;
            case "AlternateMeals.Total":
                assert ! context.getAlternateType().isEmpty() : "no alternate meal type set";
                value = getAllDriverAlternateMealsTotal(context.getAlternateType());
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    private ProcessingReturnValue processAlternateGroceriesListRef(
            MessageBlockListRef listRef, MessageBlockContext context) {

        String refName = listRef.getName();
        String value;

        switch (refName) {
            case "AlternateGroceries.Type":
                value = context.getAlternateType();
                break;
            case "AlternateGroceries.Count":
                value = getAlternateGroceryTotal(context);
                break;
            case "AlternateGroceries.Total":
                assert ! context.getAlternateType().isEmpty() : "no alternate grocery type set";
                value = getAllDriverAlternateGroceriesTotal(context.getAlternateType());
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    private ProcessingReturnValue processPickupManagerListRef(
            MessageBlockListRef listRef, MessageBlockContext context) {

        String refName = listRef.getName();
        String value;

        switch (refName) {
            case "PickupManager.UserName":
                value = context.getPickupManager();
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, value);
    }

    private String getStandardMeals(String restaurantName, Driver driver) {
        int total = 0;

        if (restaurantName.equals(controlBlock.getMealSource())) {
            for (Delivery delivery : driver.getDeliveries()) {
                DeliveryV300 deliveryV300 = (DeliveryV300) delivery;
                total += Integer.parseInt(deliveryV300.getStdMeals());
            }
        }

        return String.valueOf(total);
    }

    private String getStandardGroceries(String restaurantName, Driver driver) {
        int total = 0;

        if (restaurantName.equals(controlBlock.getGrocerySource())) {
            for (Delivery delivery : driver.getDeliveries()) {
                DeliveryV300 deliveryV300 = (DeliveryV300) delivery;
                total += Integer.parseInt(deliveryV300.getStdGrocery());
            }
        }

        return String.valueOf(total);
    }

    private boolean anyMealsOrGroceries(Driver driver) {
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

    private boolean anyAlternateMeals(Driver driver) {
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

    private boolean anyAlternateGroceries(Driver driver) {
        for (Delivery delivery : driver.getDeliveries()) {
            DeliveryV300 deliveryV300 = (DeliveryV300)delivery;

            if (! deliveryV300.getAltGrocery().equals("0")) {
                return true;
            }
        }

        return false;
    }
    protected final  ProcessingReturnValue processAltMealTypesLoopRef(
            MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext altMealsContext = new MessageBlockContext("AlternateMeals", context);
        altMealsContext.setPickupRestaurant(context.getPickupRestaurant());

        LOGGER.trace("processAlternateMealTypesLoop: {}", altMealsContext);

        for (String alternateMealType : controlBlock.getAltMealOptions()) {

            altMealsContext.setAlternateType(alternateMealType);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, altMealsContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    private ProcessingReturnValue processAltGroceryTypesLoopRef(
            MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext altGroceriesContext = new MessageBlockContext("AlternateGroceries", context);

        LOGGER.trace("processAlternateGroceryTypesLoop: {}", altGroceriesContext);

        for (String alternateGroceryType : controlBlock.getAltGroceryOptions()) {

            altGroceriesContext.setAlternateType(alternateGroceryType);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, altGroceriesContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    protected final  ProcessingReturnValue processAlternateMealsLoopRef(
            MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext altMealsContext = new MessageBlockContext("AlternateMeals", context);
        altMealsContext.setPickupRestaurant(context.getPickupRestaurant());

        LOGGER.trace("processAlternateMeals: {}", altMealsContext);

        for (String alternateMealType : controlBlock.getAltMealOptions()) {

            altMealsContext.setAlternateType(alternateMealType);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, altMealsContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    protected final  ProcessingReturnValue processAlternateGroceriesLoopRef(
            MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext altGroceriesContext = new MessageBlockContext("AlternateGroceries", context);
        altGroceriesContext.setPickupRestaurant(context.getPickupRestaurant());

        LOGGER.trace("processAlternateGroceries: {}", altGroceriesContext);

        for (String alternateGroceryType : controlBlock.getAltGroceryOptions()) {

            altGroceriesContext.setAlternateType(alternateGroceryType);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, altGroceriesContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    protected final  ProcessingReturnValue processDeliveriesLoopRef(
            MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext deliveriesContext = new MessageBlockContext("Delivery", context);

        LOGGER.trace("processDeliveriesLoopRef: {}", deliveriesContext);

        Driver driver =  context.getDriver();

        for (Delivery delivery : driver.getDeliveries()) {

            deliveriesContext.setDelivery(delivery);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, deliveriesContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    private ProcessingReturnValue processPickupManagerLoopRef(
            MessageBlockLoop loop, MessageBlockContext context) {

        StringBuilder output = new StringBuilder();
        MessageBlockContext pickpManagerContext = new MessageBlockContext("PickupManager", context);

        LOGGER.trace("processPickupManagerLoopRef: {}", pickpManagerContext);

        for (String pickupManager : controlBlock.getPickupManagers()) {

            pickpManagerContext.setPickupManager(pickupManager);

            for (MessageBlockElement loopElement : loop.getElements()) {
                ProcessingReturnValue returnValue = processElement(loopElement, pickpManagerContext);
                output.append(returnValue.output);

                if (returnValue.status == ProcessingStatus.CONTINUE) {
                    break;
                }
            }
        }

        LOGGER.trace("${{}} = \"{}\"", loop, output);
        return new ProcessingReturnValue(ProcessingStatus.COMPLETE, output.toString());
    }

    String getAlternateMealTotal(MessageBlockContext context) {
        String mealType = context.getAlternateType();
        DriverV300 driver = (DriverV300) context.getDriver();
        int total = 0;
        RestaurantV300 restaurant = (RestaurantV300) context.getPickupRestaurant();
        String restaurantName;


        // FIX THIS, DS: we are hitting this from two different contexts

        if (restaurant == null) {
            restaurantName = ((ControlBlockV300)controlBlock).getMealSource();
        } else {
            restaurantName = restaurant.getName();
        }

        if (restaurantName.equals(controlBlock.getMealSource())) {
            for (DeliveryV300 delivery : driver.getDeliveriesV300()) {
                if (mealType.equals(delivery.getTypeMeal())) {
                    total += Integer.parseInt(delivery.getAltMeals());
                }
            }
        }

        return String.valueOf(total);
    }

    String getAlternateGroceryTotal(MessageBlockContext context) {
        String groceryType = context.getAlternateType();
        DriverV300 driver = (DriverV300) context.getDriver();
        RestaurantV300 restaurant = (RestaurantV300) context.getPickupRestaurant();
        String restaurantName;
        int total = 0;

        // FIX THIS, DS: we are hitting this from two different contexts

        if (restaurant == null) {
            restaurantName = ((ControlBlockV300)controlBlock).getGrocerySource();
        } else {
            restaurantName = restaurant.getName();
        }

        if (restaurantName.equals(controlBlock.getGrocerySource())) {
            for (DeliveryV300 delivery : driver.getDeliveriesV300()) {
                if (groceryType.equals(delivery.getTypeGrocery())) {
                    total += Integer.parseInt(delivery.getAltGrocery());
                }
            }
        }

        return String.valueOf(total);
    }

    private void setDriverStartTimes() {
        assert controlBlock.getStartTimes().size() >= drivers.size();

        Iterator<String> startTimesIterator = controlBlock.getStartTimes().iterator();

        for (Driver driver : drivers) {
            DriverV300 driverV300 = (DriverV300) driver;
            assert startTimesIterator.hasNext();
            driverV300.setStartTime(startTimesIterator.next());
        }
    }

    private void loadDriversTablePostFormat() {
        // FIX THIS, DS: refactor
        String json = apiClient.runQuery(Constants.QUERY_GET_ONE_KITCHEN_DRIVERS_TABLE_POST_FORMAT_V300);

        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            MessageBlock messageBlock = new MessageBlock((Long)columns[0], (String)columns[1]);
            // FIX THIS, DS: catch and update status here?
            messageBlock.parse();
            driversTableMessageBlocks.add(messageBlock);
        }
    }

    private void loadOrdersTablePostFormat() {
        // FIX THIS, DS: refactor
        String json = apiClient.runQuery(Constants.QUERY_GET_ONE_KITCHEN_ORDERS_TABLE_POST_FORMAT_V300);

        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            MessageBlock messageBlock = new MessageBlock((Long)columns[0], (String)columns[1]);
            // FIX THIS, DS: catch and update status here?
            messageBlock.parse();
            ordersTableMessageBlocks.add(messageBlock);
        }
    }

    @Override
    String  versionSpecificDriverListRef(MessageBlockContext context, String refName) {

        String value;
        DriverV300 driver = (DriverV300) context.getDriver();

        switch (refName) {
            case "Driver.StartTime":
                value = driver.getStartTime();
                break;
            case "Driver.StandardMeals":
                value =  driver.getStandardMeals();
                break;
            case "Driver.StandardGroceries":
                value =  driver.getStandardGroceries();
                break;
            default:
                throw new MemberDataException(context.formatException("unknown list variable &{" + refName + "}"));
        }

        LOGGER.trace("${{}} = \"{}\"", refName, value);
        return value;
    }

    // FIX THIS, DS: change the driver internal representation to integer
    //               to avoid all this string/int conversion
    private String getStandardMealTotal() {
        int total = 0;
        for (Driver driver : drivers) {
            total += Integer.parseInt(((DriverV300)driver).getStandardMeals());
        }

        return String.valueOf(total);
    }

    // FIX THIS, DS: change the driver internal representation to integer
    //               to avoid all this string/int conversion
    private String getStandardGroceryTotal() {
        int total = 0;
        for (Driver driver : drivers) {
            total += Integer.parseInt(((DriverV300)driver).getStandardGroceries());
        }

        return String.valueOf(total);
    }

    // FIX THIS, DS: change the driver internal representation to integer
    //               to avoid all this string/int conversion
    private String getAllDriverAlternateMealsTotal(String mealType) {
        int total = 0;
        for (Driver driver : drivers) {
            total += ((DriverV300)driver).getAltMeals(mealType);
        }

        return String.valueOf(total);
    }

    // FIX THIS, DS: change the driver internal representation to integer
    //               to avoid all this string/int conversion
    private String getAllDriverAlternateGroceriesTotal(String groceryType) {
        int total = 0;
        for (Driver driver : drivers) {
            total += ((DriverV300)driver).getAltGroceries(groceryType);
        }

        return String.valueOf(total);
    }
}