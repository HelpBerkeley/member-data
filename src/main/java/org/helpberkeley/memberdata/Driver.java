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

import java.util.List;

public class Driver {

    private final String userName;
    private final String gMapURL;
    private boolean hasCondo;
    private final List<Restaurant> pickups;
    private final List<Delivery> deliveries;

    Driver(final String userName, List<Restaurant> pickups, List<Delivery> deliveries, final String gmapURL) {
        this.userName = userName;
        this.pickups = pickups;
        this.deliveries = deliveries;
        this.gMapURL = gmapURL;

        boolean condo = false;
        for (Delivery delivery : deliveries) {
            if (delivery.isCondo()) {
                condo = true;
                break;
            }
        }
        this.hasCondo = condo;
    }

    public String getUserName() {
        return userName;
    }

    public String getgMapURL() {
        return gMapURL;
    }

    public boolean hasCondo() {
        return hasCondo;
    }

    public List<Restaurant> getPickups() {
        return pickups;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public String getFirstRestaurantName() {
        assert ! pickups.isEmpty();

        return pickups.get(0).getName();
    }
}
