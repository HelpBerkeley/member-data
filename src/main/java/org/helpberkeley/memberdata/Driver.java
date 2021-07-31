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

import org.helpberkeley.memberdata.v200.DriverV200;
import org.helpberkeley.memberdata.v200.WorkflowBeanV200;
import org.helpberkeley.memberdata.v300.DriverV300;
import org.helpberkeley.memberdata.v300.WorkflowBeanV300;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Driver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Driver.class);

    private String gMapURL = null;
    private WorkflowBean bean;
    protected final List<String> warningMessages = new ArrayList<>();
    protected boolean disableLateArrivalAudit = false;

    protected final List<ItineraryStop> itinerary = new ArrayList<>();
    protected final List<Restaurant> pickups = new ArrayList<>();
    protected final List<Delivery> deliveries = new ArrayList<>();

    protected Driver() {

    }

    public static Driver createDriver(WorkflowBean driverBean) {
        Driver driver;

        if (driverBean instanceof WorkflowBeanV200) {
            driver = new DriverV200();
        } else if (driverBean instanceof WorkflowBeanV300) {
            driver = new DriverV300();
        } else {
            throw new MemberDataException("Version not supported for " + driverBean);
        }

        driver.bean = driverBean;
        return driver;
    }

    public abstract void initialize();
    public abstract String getStartTime();

    public void addRestaurant(Restaurant restaurant) {
        pickups.add(restaurant);
        itinerary.add(restaurant);
    }

    public void addDelivery(Delivery delivery) {
        deliveries.add(delivery);
        itinerary.add(delivery);
    }

    public void setGMapURL(String gMapURL) {
        this.gMapURL = gMapURL;
    }

    public void setDisableLateArrivalAudit(boolean doAudit) {
        disableLateArrivalAudit = doAudit;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public List<ItineraryStop> getItinerary() {
        return itinerary;
    }

    @Override
    public String toString() {
        return getUserName();
    }

    public String getUserName() {
        return bean.getUserName();
    }

    public String getName() {
        return bean.getName();
    }

    public String getPhoneNumber() {
        return bean.getPhone();
    }

    public String getgMapURL() {
        return gMapURL;
    }

    public boolean hasCondo() {
        for (Delivery delivery : deliveries) {
            if (delivery.isCondo()) {
                return true;
            }
        }
        return false;
    }

    public List<Restaurant> getPickups() {
        return Collections.unmodifiableList(pickups);
    }

    public Restaurant getPickup(String restaurantName) {
        for (Restaurant restaurant : pickups) {
            if (restaurant.getName().equals(restaurantName)) {
                return restaurant;
            }
        }

        return null;
    }

    public String getFirstRestaurantName() {
        assert ! pickups.isEmpty();

        return pickups.get(0).getName();
    }

    public void addWarning(String warning) {
        warningMessages.add(warning);
    }

    public final List<String> getWarningMessages() {
        return warningMessages;
    }
}