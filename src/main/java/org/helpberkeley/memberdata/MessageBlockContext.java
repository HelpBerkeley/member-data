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

class MessageBlockContext {
    private final String name;
    private final MessageBlockContext parent;

    private String messageBlockName;
    private long messageBlockPost;
    private Driver driver;
    private Delivery delivery;
    private Restaurant splitRestaurant;
    private Restaurant pickupRestaurant;
    private String backupDriver;
    private String alternateType;
    private String pickupManager;

    MessageBlockContext(String name, MessageBlockContext parent) {
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

        return string.toString();
    }

    void setMessageBlockContext(long postNumber, String name) {
        messageBlockName = name;
        messageBlockPost = postNumber;
    }

    void setDriver(Driver driver) {
        this.driver = driver;
    }

    void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    void setSplitRestaurant(Restaurant splitRestaurant) {
        this.splitRestaurant = splitRestaurant;
    }

    void setPickupRestaurant(Restaurant pickupRestaurant) {
        this.pickupRestaurant = pickupRestaurant;
    }

    void setBackupDriver(String backupDriver) {
        this.backupDriver = backupDriver;
    }

    void setAlternateType(String alternateType) {
        this.alternateType = alternateType;
    }

    void setPickupManager(String pickupManager) {
        this.pickupManager = pickupManager;
    }

    String getBlockName() {

        MessageBlockContext baseContext = this;

        while (baseContext.parent != null) {
            baseContext = baseContext.parent;
        }

        assert baseContext.messageBlockName != null : baseContext;
        return baseContext.messageBlockName;
    }

    long getPostNumber() {

        MessageBlockContext baseContext = this;

        while (baseContext.parent != null) {
            baseContext = baseContext.parent;
        }

        assert baseContext.messageBlockName != null : baseContext;
        return baseContext.messageBlockPost;
    }

    Driver getDriver() {
        if (driver != null) {
            return driver;
        }

        if (parent != null) {
            return parent.getDriver();
        }

        return null;
    }

    Restaurant getSplitRestaurant() {
        if (splitRestaurant != null) {
            return splitRestaurant;
        }

        if (parent != null) {
            return parent.getSplitRestaurant();
        }

        return null;
    }

    Restaurant getPickupRestaurant() {
        if (pickupRestaurant != null) {
            return pickupRestaurant;
        }

        if (parent != null) {
            return parent.getPickupRestaurant();
        }

        return null;
    }

    Delivery getDelivery() {
        if (delivery != null) {
            return delivery;
        }

        if (parent != null) {
            return parent.getDelivery();
        }

        return null;
    }

    String getBackupDriver() {
        if (backupDriver != null) {
            return backupDriver;
        }

        if (parent != null) {
            return parent.getBackupDriver();
        }

        return null;
    }

    String getPickupManager() {
        if (pickupManager != null) {
            return pickupManager;
        }

        if (parent != null) {
            return parent.getPickupManager();
        }

        return null;
    }

    String getAlternateType() {
        return alternateType;
    }

    // FIX THIS, DS: pass in an element and get the line number from it.
    String formatException(String message) {
        String blockName = getBlockName();
        long postNumber = getPostNumber();

        return "Post: " + postNumber + ", block: " + blockName + ": " + message;
    }
}
