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
import java.util.List;
import java.util.Objects;

public class Driver {

    private final String userName;
    private final String phoneNumber;
    private final String gMapURL;
    private final List<Restaurant> pickups;
    private final List<Delivery> deliveries;

    Driver(final String userName, final String phoneNumber, List<Restaurant> pickups, List<Delivery> deliveries, final String gmapURL) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.pickups = pickups;
        this.deliveries = deliveries;
        this.gMapURL = gmapURL;
    }

    Driver(final String userName, final String phoneNumber, List<Restaurant> pickups, List<Delivery> deliveries) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.pickups = pickups;
        this.deliveries = deliveries;
        this.gMapURL = null;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getgMapURL() {
        int index = Objects.requireNonNull(gMapURL).indexOf("/@");

        if (index == -1) {
            return gMapURL;
        }

        return "[" + gMapURL.substring(0, index) + "](" + gMapURL + ")";
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

    public List<Delivery> getDeliveries() {
        return Collections.unmodifiableList(deliveries);
    }

    public String getFirstRestaurantName() {
        assert ! pickups.isEmpty();

        return pickups.get(0).getName();
    }

    public int getNumStops() {
        return pickups.size() + deliveries.size();
    }

    public int getNumPickups() {
        return pickups.size();
    }
}
