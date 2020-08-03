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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Override
    public String toString() {
        return getUserName();
    }

    public String getUserName() {
        return userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
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

    long getOrders(String restaurantName) {
        // FIX THIS, DS: use ordered map for pickups
        for (Restaurant restaurant : pickups) {
            if (restaurantName.equals(restaurant.getName())) {
                return restaurant.getOrders();
            }
        }

        throw new MemberDataException("Could not find restaurant " + restaurantName
                + " in driver " + userName + "'s pickups");
    }

    //
    //  How to calculate the start time for a first restaurant with zero orders
    //
    //    find the StartTime for the first restaurant on the list that has at least one order
    //
    //    If the StartTime for the first restaurant with at least one order
    //    is already 5:00 PM or under 5:00 PM, do not change it.
    //
    //    If the StartTime for the first restaurant with at least one order is later than 5:00 PM
    //    for each restaurant with zero orders that precedes it, take 5 mins off of the StartTime
    //    But never let the StartTime get under 5:00 PM
    //
    // FIX THIS, DS: this algorithm doesn't handle V&A Cafe which has a start time of 4:50.
    //
    String getFirstRestaurantStartTime() {

        if (getPickups().isEmpty()) {
            throw new MemberDataException("Driver " + userName + " has no pickups\n");
        }

        List<Restaurant> zeroOrders = new ArrayList<>();
        Restaurant firstPickupRestaurant = null;

        for (Restaurant restaurant : getPickups()) {
            if (restaurant.getOrders() == 0) {
                zeroOrders.add(restaurant);
            } else {
                firstPickupRestaurant = restaurant;
                break;
            }
        }

        // Do we have only 0 order restaurants?
        if (firstPickupRestaurant == null) {
            return zeroOrders.get(0).getStartTime();
        }

        // No 0 order restaurants?
        if (zeroOrders.size() == 0) {
            return firstPickupRestaurant.getStartTime();
        }

        int firstStartTime = convertStartingTime(firstPickupRestaurant.getStartTime());

        // Is the first pickup restaurant already at 5 or earlier?
        if (firstStartTime <= 500) {
            return firstPickupRestaurant.getStartTime();
        }

        while (firstStartTime > 500) {

        }

        return "";
    }

    int convertStartingTime(String startingTime) {
        String time = startingTime.replaceAll("[ :pmPM]", "");
        return Integer.parseInt(time);
    }
}
