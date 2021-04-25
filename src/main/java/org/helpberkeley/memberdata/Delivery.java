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

public abstract class Delivery {

    protected final String name;
    protected final long lineNumber;
    protected String userName;
    protected String phone;
    protected String altPhone;
    protected String neighborhood;
    protected String city;
    protected String address;
    protected Boolean isCondo;
    protected String details;
    protected String restaurant;
    protected Location location;

    public Delivery(String name, long lineNumber) {
        this.name = name;
        this.lineNumber = lineNumber;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAltPhone(String altPhone) {
        this.altPhone = altPhone;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setIsCondo(Boolean condo) {
        this.isCondo = condo;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }
    public String getUserName() {
        return userName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAltPhone() {
        return altPhone;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public Boolean isCondo() {
        return isCondo;
    }

    public String getDetails() {
        return details;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public String getFullAddress() {
        return address + ", " + city + ", " + "CA";
    }

    public Location getLocation() {
        return location;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public abstract String deliveryRow();
}
