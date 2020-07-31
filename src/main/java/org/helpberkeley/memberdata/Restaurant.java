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
    private String details = "";
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

    String getDetails() {
        return details;
    }

    String getAddress() {
        return address;
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

    // FIX THIS, DS: currently hardwired.  Get this from the restaurant template when we add it there.
    //
    boolean closesBefore7PM() {

        boolean closesEarly;

        switch (name) {
            case "Kim's Cafe":
            case "V&A Cafe":
            case "Taco Sinaloa":
                closesEarly = true;
                break;
            default:
                closesEarly = false;
        }

        return closesEarly;
    }

    // FIX THIS, DS: currently hardwired.  Get this from the restaurant template when we add it there.
    //
    String getClosingTime() {

        String value;

        switch (name) {
            case "Kim's Cafe":
            case "Taco Sinaloa":
                value = "6:00 PM";
                break;
            case "V&A Cafe":
                value = "5:00 PM";
                break;
            default:
                throw new MemberDataException(name + ": implement closining time handling");
        }

        return value;
    }
}
