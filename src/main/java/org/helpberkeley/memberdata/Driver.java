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

import org.helpberkeley.memberdata.route.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Driver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Driver.class);

    private String gMapURL = null;
    private WorkflowBean bean;
    protected List<Restaurant> pickups;
    private List<Delivery> deliveries;
    private long routeSeconds = 0;
    protected final List<String> warningMessages = new ArrayList<>();
    protected boolean disableLateArrivalAudit = false;

    protected Driver() {

    }

    public static Driver createDriver(WorkflowBean driverBean, List<Restaurant> pickups,
                   List<Delivery> deliveries, String gmapURL, boolean disableLateArrivalAudit) {

        Driver driver;

        if (driverBean instanceof WorkflowBeanV200) {
            driver = new DriverV200();
        } else if (driverBean instanceof WorkflowBeanV300) {
            driver = new DriverV300();
        } else {
            throw new MemberDataException("Version not supported for " + driverBean);
        }

        driver.bean = driverBean;
        driver.pickups = pickups;
        driver.deliveries = deliveries;
        driver.gMapURL = gmapURL;
        driver.disableLateArrivalAudit = disableLateArrivalAudit;
        driver.setStartTime();

        return driver;
    }

    abstract void setStartTime();

    public static Driver createDriver(WorkflowBean driverBean, List<Restaurant> pickups, List<Delivery> deliveries) {

        Driver driver;

        if (driverBean instanceof WorkflowBeanV200) {
            driver = new DriverV200();
        } else if (driverBean instanceof WorkflowBeanV300) {
            driver = new DriverV300();
        } else {
            throw new MemberDataException("Version not supported for " + driverBean);
        }

        driver.bean = driverBean;
        driver.pickups = pickups;
        driver.deliveries = deliveries;
//        driver.setStartTime();

        return driver;
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

    public String getFullAddress() {
        return bean.getAddress() + ", " + bean.getCity() + ", " + "CA";
    }

    public long getRouteSeconds() {
        return routeSeconds;
    }

    public List<Restaurant> getPickups() {
        return Collections.unmodifiableList(pickups);
    }

    public List<Delivery> getDeliveries() {
        return Collections.unmodifiableList(deliveries);
    }

    public void setDeliveries(List<Delivery> deliveries, long totalSeconds) {
        this.deliveries.clear();
        this.deliveries.addAll(deliveries);
        generateURL();
        this.routeSeconds = totalSeconds;
    }

    public String getFirstRestaurantName() {
        assert ! pickups.isEmpty();

        return pickups.get(0).getName();
    }

    private void generateURL() {
        StringBuilder url = new StringBuilder();

        url.append("https://www.google.com/maps/dir/");

        // The driver's home address is the beginning of the route.
        String address = getFullAddress()
                .replaceAll("\\s+", " ")
                .replaceAll(" ", "+")
                .replaceAll("#", "%23");
        url.append(address).append('/');

        for (Restaurant pickup : pickups) {

            address = pickup.getAddress().replaceAll(" ", "+").replaceAll("#", "%23");
            url.append(address).append('/');
        }

        Location prevLocation = null;

        for (Delivery delivery : deliveries) {

            Location location = delivery.getLocation();

            if (location.equals(prevLocation)) {
                LOGGER.info("Skipping adding duplicate location {} to route", delivery.getFullAddress());
            } else {
                address = delivery.getAddress()
                        .replaceAll("\\s+", " ")
                        .replaceAll(" ", "+")
                        .replaceAll("#", "%23");
                url.append(address).append('/');
            }
            prevLocation = location;
        }

        // The driver's home address is also the end of the route.
        address = getFullAddress()
                .replaceAll("\\s+", " ")
                .replaceAll(" ", "+")
                .replaceAll("#", "%23");
        url.append(address).append('/');

        // FIX THIS, DS: remove trailing '/' ?

        gMapURL = url.toString();
    }

    public String driverBlock() {

        StringBuilder block = new StringBuilder();

        String driverRow = driverRow();

        block.append(driverRow);

        for (Restaurant pickup : pickups) {
            block.append(pickup.pickupRow());
        }

        for (Delivery delivery : deliveries) {
            block.append(delivery.deliveryRow());
        }

        block.append(driverRow);
        block.append('"').append(gMapURL).append('"').append('\n');

        return block.toString();
    }

    // FIX THIS, DS: this is hardwired to column order/length
    private String driverRow() {

        StringBuilder row = new StringBuilder();
        String value;

        // Consumer
        row.append("FALSE,");

        // Driver
        row.append("TRUE,");

        // Name
        value = bean.getName();
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // User Name
        value = bean.getUserName();
        assert ! value.contains((",")) : "bad user name: " + value;
        row.append(value).append(",");

        // Phone
        value = bean.getPhone();
        assert ! value.contains((",")) : "bad phone: " + value;
        row.append(value).append(",");

        // Alt Phone
        value = bean.getAltPhone();
        assert ! value.contains((",")) : "bad alt phone: " + value;
        row.append(value).append(",");

        // Neighborhood
        value = bean.getNeighborhood();
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // City
        value = bean.getCity();
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // Address
        value = bean.getAddress();
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // Condo
        value = bean.getCondo();
        row.append(value).append(",");

        // Details
        value = bean.getDetails();
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // Empty columns for Restaurants,normal,veggie,#orders
        row.append(",,,\n");

        return row.toString();
    }

    List<String> getWarningMessages() {
        return warningMessages;
    }
}
