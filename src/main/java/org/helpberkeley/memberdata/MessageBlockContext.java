/*
 * Copyright (c) 2021. helpberkeley.org
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

import java.text.MessageFormat;

public class MessageBlockContext {

    public static final String ERROR_WRONG_ITINERARY_STOP_TYPE =
            "itinerary stop from worksheet line {0} is not a {1}";
    public static final String MESSAGE_ERROR = "Block {0}: {1}.\n"
            + "https://go.helpberkeley.org/t/{2}/{3}\n";
    private final String name;
    private final MessageBlockContext parent;

    private MessageBlock messageBlock;
    private Driver driver;
    private Delivery delivery;
    private Restaurant splitRestaurant;
    private Restaurant pickupRestaurant;
    private String backupDriver;
    private String alternateType;
    private String pickupManager;
    private ItineraryStop itineraryStop;

    public MessageBlockContext(String name, MessageBlockContext parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        string.append(name);
        MessageBlockContext context = this;
        while ((context = context.parent) != null) {
            string.insert(0, "->");
            string.insert(0, context.name);
        }
        string.insert(0, "Context: ");

        if (getDriver() != null) {
            string.append(" driver:").append(getDriver()).append(", ");
        }
        if (getDelivery() != null) {
            string.append(" delivery: ").append(getDelivery()).append(", ");
        }
        if (getSplitRestaurant() != null) {
            string.append(" splitRestaurant: ").append(getSplitRestaurant()).append(", ");
        }
        if (getPickupRestaurant() != null) {
            string.append(" pickupRestaurant: ").append(getPickupRestaurant()).append(", ");
        }
        if (getBackupDriver() != null) {
            string.append(" backupDriver: ").append(getBackupDriver()).append(", ");
        }
        if (getItineraryStop() != null) {
            string.append(" itineraryStop: ").append(getItineraryStop()).append(", ");
        }

        return string.toString();
    }

    public void setMessageBlock(MessageBlock messageBlock) {
        this.messageBlock = messageBlock;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    void setSplitRestaurant(Restaurant splitRestaurant) {
        this.splitRestaurant = splitRestaurant;
    }

    public void setPickupRestaurant(Restaurant pickupRestaurant) {
        this.pickupRestaurant = pickupRestaurant;
    }

    void setBackupDriver(String backupDriver) {
        this.backupDriver = backupDriver;
    }

    public void setAlternateType(String alternateType) {
        this.alternateType = alternateType;
    }

    public void setPickupManager(String pickupManager) {
        this.pickupManager = pickupManager;
    }

    public void setItineraryStop(ItineraryStop itineraryStop) {
        this.itineraryStop = itineraryStop;
    }

    String getBlockName() {

        MessageBlockContext baseContext = this;

        while (baseContext.parent != null) {
            baseContext = baseContext.parent;
        }

        assert baseContext.messageBlock != null : baseContext;
        assert baseContext.messageBlock.getName() != null : baseContext.messageBlock;
        return baseContext.messageBlock.getName();
    }

    long getPostNumber() {

        MessageBlockContext baseContext = this;

        while (baseContext.parent != null) {
            baseContext = baseContext.parent;
        }

        assert baseContext.messageBlock != null : baseContext;
        return baseContext.messageBlock.getPostNumber();
    }

    long getTopic() {

        MessageBlockContext baseContext = this;

        while (baseContext.parent != null) {
            baseContext = baseContext.parent;
        }

        assert baseContext.messageBlock != null : baseContext;
        return baseContext.messageBlock.getTopic();
    }

    public Driver getDriver() {
        if (driver != null) {
            return driver;
        }

        if (parent != null) {
            return parent.getDriver();
        }

        return null;
    }

    public Restaurant getSplitRestaurant() {
        if (splitRestaurant != null) {
            return splitRestaurant;
        }

        if (parent != null) {
            return parent.getSplitRestaurant();
        }

        return null;
    }

    public Restaurant getPickupRestaurant() {
        if (pickupRestaurant != null) {
            return pickupRestaurant;
        }

        if (parent != null) {
            return parent.getPickupRestaurant();
        }

        return null;
    }

    public Delivery getDelivery() {
        if (delivery != null) {
            return delivery;
        }

        if (parent != null) {
            return parent.getDelivery();
        }

        return null;
    }

    public Restaurant getItineraryRestaurant() {

        if (itineraryStop == null) {
            throw new MemberDataException(
                    formatException("Not looping over Itinerary. IRestaurant not available"));
        }

        if (itineraryStop.getType() != ItineraryStopType.PICKUP) {
            throw new MemberDataException(formatException(MessageFormat.format(
                    ERROR_WRONG_ITINERARY_STOP_TYPE, itineraryStop.getLineNumber(), "restaurant")));
        }

        return (Restaurant)itineraryStop;
    }

    public Delivery getItineraryDelivery() {

        if (itineraryStop == null) {
            throw new MemberDataException(
                    formatException("Not looping over Itinerary. IConsumer not available"));
        }

        if (itineraryStop.getType() != ItineraryStopType.DELIVERY) {
            throw new MemberDataException(formatException(MessageFormat.format(
                    ERROR_WRONG_ITINERARY_STOP_TYPE, itineraryStop.getLineNumber(), "delivery")));
        }

        return (Delivery)itineraryStop;
    }

    public String getBackupDriver() {
        if (backupDriver != null) {
            return backupDriver;
        }

        if (parent != null) {
            return parent.getBackupDriver();
        }

        return null;
    }

    public String getPickupManager() {
        if (pickupManager != null) {
            return pickupManager;
        }

        if (parent != null) {
            return parent.getPickupManager();
        }

        return null;
    }

    public String getAlternateType() {
        return alternateType;
    }

    public ItineraryStop getItineraryStop() {
        return itineraryStop;
    }

    public boolean isItinerary() {
        return itineraryStop != null;
    }

    public String formatException(String message) {
        return MessageFormat.format(
                MESSAGE_ERROR, getBlockName(), message, getTopic(), getPostNumber());
    }
}
