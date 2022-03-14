/*
 * Copyright (c) 2021-2022. helpberkeley.org
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
package org.helpberkeley.memberdata.v200;

import org.helpberkeley.memberdata.Builder;
import org.helpberkeley.memberdata.Constants;

import static org.helpberkeley.memberdata.v200.BuilderConstants.*;

class DeliveryBuilder implements Builder {

    private String isConsumer = Constants.TRUE;
    private String isDriver = Constants.FALSE;
    private String consumerName = DEFAULT_CONSUMER_NAME;
    private String consumerUserName = DEFAULT_CONSUMER_USER_NAME;
    private String city = "Berkeley";
    private String address = "123 456th St.";
    private String phone = "555-555-1112";
    private String altPhone = "";
    private boolean condo = false;
    private String restaurantName = DEFAULT_RESTAURANT_NAME;
    private String normalMeals = "0";
    private String veggieMeals = "0";

    public DeliveryBuilder withIsConsumer(String isConsumer) {
        this.isConsumer = isConsumer;
        return this;
    }

    public DeliveryBuilder withIsDriver(String isDriver) {
        this.isDriver = isDriver;
        return this;
    }

    public DeliveryBuilder withNormalMeals(String normalMeals) {
        this.normalMeals = normalMeals;
        return this;
    }

    public DeliveryBuilder withVeggieMeals(String veggieMeals) {
        this.veggieMeals = veggieMeals;
        return this;
    }

    public DeliveryBuilder withConsumerName(String consumerName) {
        this.consumerName = consumerName;
        return this;
    }

    public DeliveryBuilder withConsumerUserName(String consumerUserName) {
        this.consumerUserName = consumerUserName;
        return this;
    }

    public DeliveryBuilder withCity(String city) {
        this.city = city;
        return this;
    }

    public DeliveryBuilder withCondo(boolean condo) {
        this.condo = condo;
        return this;
    }

    public DeliveryBuilder withAddress(String address) {
        this.address = address;
        return this;
    }

    public DeliveryBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public DeliveryBuilder withAltPhone(String altPhone) {
        this.altPhone = altPhone;
        return this;
    }

    public DeliveryBuilder withRestaurant(String restaurant) {
        this.restaurantName = restaurant;
        return this;
    }

    @Override
    public String toString() {
        return build();
    }

    public String build() {
        return isConsumer
                + Constants.CSV_SEPARATOR
                + isDriver
                + Constants.CSV_SEPARATOR
                + consumerName + ","
                + consumerUserName + ","
                + phone + ","
                + altPhone + ","
                + "Hills,"
                + city + ","
                + address + ","
                + String.valueOf(condo).toUpperCase() + ","
                + "" + ","
                + restaurantName + ","
                + normalMeals + ","
                + veggieMeals + ","
                + "\n";
    }
}
