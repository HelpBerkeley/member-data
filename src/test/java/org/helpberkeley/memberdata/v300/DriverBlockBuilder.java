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
package org.helpberkeley.memberdata.v300;

import java.util.ArrayList;
import java.util.List;

public class DriverBlockBuilder {

    private final List<RestaurantBuilder> restaurants = new ArrayList<>();
    private final List<DeliveryBuilder> deliveries = new ArrayList<>();
    private DriverBuilder driver = new DriverBuilder();
    private final String gmapURL = WorkflowBuilder.DEFAULT_GMAP_URL;

    @Override
    public String toString() {
        return build();
    }

    public String build() {

        StringBuilder driverBlock = new StringBuilder();

        driverBlock.append(driver.build());
        restaurants.forEach(driverBlock::append);
        deliveries.forEach(driverBlock::append);
        driverBlock.append(driver.build());
        driverBlock.append(gmapURL);
        driverBlock.append(ControlBlockTest.EMPTY_ROW);

        return driverBlock.toString();
    }

    public DriverBlockBuilder withDriver(DriverBuilder driver) {
        this.driver = driver;
        return this;
    }

    public DriverBlockBuilder withDelivery(DeliveryBuilder delivery) {
        deliveries.add(delivery);
        return this;
    }

    public DriverBlockBuilder withRestaurant(RestaurantBuilder restaurant) {
        restaurants.add(restaurant);
        return this;
    }
}
