/*
 * Copyright (c) 2020. helpberkeley.org
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Restaurant {
    private final String name;
    private String address = "";
    private String startTime = "";
    private int startTimeValue = 0;
    private String closingTime = "";
    private int closingTimeValue = 0;
    private String details = "";
    private String emoji = "";
    private String route = "";
    private long orders = 0;
    private final Map<String, Driver> drivers = new HashMap<>();
    private boolean noPics = false;

    Restaurant(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ", orders: " + orders + ", start:" + startTime + ", drivers:" + drivers.keySet();
    }

    void setStartTime(final String startTime) {
        this.startTime = startTime;
        this.startTimeValue = convertTime(startTime);
    }
    void setClosingTime(final String closingTime) {
        this.closingTime = closingTime;
        this.closingTimeValue = convertTime(closingTime);
    }

    // FIX THIS, DS: need audit/assert for expected time string
    private int convertTime(String time) {
        String digits = time.replaceAll("[ :pmPM]", "");
        return Integer.parseInt(digits);
    }
    void setDetails(final String details) {
        this.details = (details == null) ? "" : details;
    }
    void setRoute(final String route) {
        this.route = route;
    }
    void setAddress(final String address) {
        this.address = address;
    }
    void setOrders(final String orders) {

        double numOrders = Double.parseDouble(orders.trim());
        this.orders += Math.round(numOrders);
    }
    void addOrders(long orders) {
        this.orders += orders;
    }
    void addDriver(final Driver driver) {
        assert ! drivers.containsKey(driver.getUserName()) : driver.getUserName();
        drivers.put(driver.getUserName(), driver);
    }
    void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    void setNoPics() {
        noPics = true;
    }

    String getName() {
        return name;
    }

    String getRoute() {
        return route;
    }

    String getStartTime() {
        return startTime;
    }

    String getClosingTime() {
        return closingTime;
    }

    String getDetails() {
        return details;
    }

    String getAddress() {
        return address;
    }

    String getEmoji() {
        return emoji;
    }

    long getOrders() {
        return orders;
    }

    Map<String, Driver> getDrivers() {
        return Collections.unmodifiableMap(drivers);
    }

    boolean getNoPics() {
        return noPics;
    }

    /**
     *
     * @param closingTime 545 means 5:45 PM, 700, 7:00 PM, etc...
     * @return Does this restaurant close before the passed in time?
     */
    boolean closesBefore(int closingTime) {
        return closingTimeValue < closingTime;
    }
}
