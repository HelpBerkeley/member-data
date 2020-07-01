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

public class Delivery {

    private final String name;
    private String userName;
    private String phone;
    private String altPhone;
    private String city;
    private String address;
    private Boolean isCondo;
    private String details;
    private String restaurant;
    private String normalRations;
    private String veggieRations;

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

    public String getNormalRations() {
        return normalRations;
    }

    public String getVeggieRations() {
        return veggieRations;
    }
}