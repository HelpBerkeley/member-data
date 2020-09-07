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

import org.helpberkeley.memberdata.route.Location;

public class Delivery {

    private final String name;
    private String userName;
    private String phone;
    private String altPhone;
    private String neighborhood;
    private String city;
    private String address;
    private Boolean isCondo;
    private String details;
    private String restaurant;
    private String normalRations;
    private String veggieRations;
    private Location location;

    @Override
    public String toString() {
        return getUserName()
                + ", condo:" + isCondo
                + ", restaurant: " + getRestaurant()
                + ", rations:" + getNormalRations() + ':' + getVeggieRations();
    }

    public Delivery(final String name) {
        this.name = name;
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

    public void setNormalRations(String normalRations) {
        this.normalRations = normalRations;
    }

    public void setVeggieRations(String veggieRations) {
        this.veggieRations = veggieRations;
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

    public String getNeighborhood() {
        return neighborhood;
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

    public String getNormalRations() {
        return normalRations;
    }

    public String getVeggieRations() {
        return veggieRations;
    }

    public String getFullAddress() {
        return address + ", " + city + ", " + "CA";
    }

    public Location getLocation() {
        return location;
    }

    // FIX THIS, DS: columns are hard wired
    //
    public String deliveryRow() {
        StringBuilder row = new StringBuilder();
        String value;

        // Consumer
        row.append("TRUE,");

        // Driver
        row.append("FALSE,");

        // Name
        value = name;
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // User Name
        row.append(userName).append(',');

        // Phone
        row.append(phone).append(',');

        // Alt phone
        row.append(altPhone).append(',');

        // Neighborhood
        value = neighborhood;
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // City
        value = city;
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");


        // Address
        value = address;
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // Condo
        row.append(isCondo).append(",");

        // Details
        value = details;
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // Restaurant
        value = restaurant;
        if (value.contains(",")) {
            value = "\"" + value + "\"";
        }
        row.append(value).append(",");

        // normal
        row.append(normalRations).append(",");

        // veggie
        row.append(veggieRations).append(",");

        // Orders - empty last column

        row.append('\n');

        return row.toString();
    }
}
