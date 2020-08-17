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

import java.text.MessageFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Driver {

    static final String WARNING_MAY_BE_REACHED_AFTER_CLOSING =
                "restaurant {0} may be reached after closing time";
    static final String WARNING_MAY_BE_REACHED_AFTER_EXPECTED =
                "restaurant {0}  may be reached later than when the driver is expected";

    private static final LocalTime FIVE_PM = LocalTime.of(5, 0);

    private final String userName;
    private final String phoneNumber;
    private final String gMapURL;
    private final List<Restaurant> pickups;
    private final List<Delivery> deliveries;
    private String originalStartTime;
    private String startTime;
    private final List<String> warningMessages = new ArrayList<>();

    Driver(final String userName, final String phoneNumber,
           List<Restaurant> pickups, List<Delivery> deliveries, final String gmapURL) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.pickups = pickups;
        this.deliveries = deliveries;
        this.gMapURL = gmapURL;
        setStartTime();
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

    String getStartTime() {
        return startTime;
    }

    String getOriginalStartTime() {
        return originalStartTime;
    }

    List<String> getWarningMessages() {
        return warningMessages;
    }

    private void setStartTime() {

        Restaurant restaurant = pickups.get(0);

        // FIX THIS, DS: verify that upstream auditing can make this an assertion
        if (restaurant == null) {
            throw new MemberDataException("Driver " + getUserName() + " has no pickups");
        }

        originalStartTime = restaurant.getStartTime();
        startTime = originalStartTime;

        if (restaurant.getOrders() == 0) {
            startTime = calculateFirstRestaurantStartTime();
            generateStartTimeWarnings();
        }
    }

    //
    //  How to calculate the start time for a first restaurant with zero orders
    //
    // See: https://go.helpberkeley.org/t/spec-start-time-for-runs-with-leading-0-order-restaurants/3499
    //
    private String calculateFirstRestaurantStartTime() {

        Restaurant firstRestaurant = pickups.get(0);
        assert firstRestaurant != null : "Driver " + userName + " has no pickups";
        assert firstRestaurant.getOrders() == 0 : "Driver " + userName + ", first pickup is not 0 order";

        LocalTime firstCloseTime = convertTime(firstRestaurant.getClosingTime());

        // If the starting zero order restaurant RS has a closing time where ClosingTimeS - 10 mins < 5:00pm,
        // then CalculatedStartTimeS = ClosingTimeS - 10 mins

        if ( firstCloseTime.minusMinutes(10).compareTo(FIVE_PM) < 0) {
            return formatTime(firstCloseTime.minusMinutes(10));
        }


        // find the StartTime0 for the first restaurant R0 on the list that has at least one order
        // If StartTime0 <= 5pm, CalculatedStartTimeS = StartTime0

        Restaurant firstOrderRestaurant = getFirstOrderRestaurant();

        if (firstOrderRestaurant == null) {
            return formatTime(FIVE_PM);
        }

        LocalTime firstOrderRestaurantStartTime = convertTime(firstOrderRestaurant.getStartTime());

        if ( firstOrderRestaurantStartTime.compareTo(FIVE_PM) <= 0) {
            return formatTime(firstOrderRestaurantStartTime);
        }

        // for each restaurant zero order Rp before R0,
        // take out 5 minutes off of StartTime0 if Rp-> Rp-1 is the same route,
        // take out 10 minutes if Rp->Rp-1 is a different route,
        // until you find CalculatedStartTimeS for the starting restaurant.
        // If CalculatedStartTimeS <5pm, then CalculatedStartTimeS = 5pm

        Restaurant prev = null;

        for (Restaurant restaurant : pickups) {

            if (prev != null) {
                firstOrderRestaurantStartTime = firstOrderRestaurantStartTime.minusMinutes(5);

                if (! prev.getRoute().equals(restaurant.getRoute())) {
                    firstOrderRestaurantStartTime = firstOrderRestaurantStartTime.minusMinutes(5);
                }
            }

            if (restaurant == firstOrderRestaurant) {
                break;
            }

            prev = restaurant;
        }

        if (firstOrderRestaurantStartTime.compareTo(FIVE_PM) < 0) {
            firstOrderRestaurantStartTime = FIVE_PM;
        }

        return formatTime(firstOrderRestaurantStartTime);
    }

//    From CalculatedStartTimeS,
//    calculate CalculatedStartTimeP for each restaurant P
//    from restaurant S to restaurant 0.
//
//    For each restaurant P,
//    IF CalculatedStartTimeP > ClosingTimeP - 10 minutes,
//    THEN
//    {
//        write diagnosis warning:
//    "Warning: restaurant P may be reached after closing time"
//    }
//
//    IF R0 has at least one order AND CalculatedTime0 > StartTime0,
//    THEN
//    {
//        write diagnosis warning:
//    "Warning: restaurant 0 may be reached later than when driver is expected."
//    }

    private void generateStartTimeWarnings() {

        LocalTime startTime = convertTime(this.startTime);

        Restaurant prev = null;

        for (Restaurant pickup : pickups) {

            if (prev != null) {
                startTime = startTime.plusMinutes(prev.getRoute().equals(pickup.getRoute()) ? 5 : 10);
            }

            LocalTime closingTime = convertTime(pickup.getClosingTime());

            if (startTime.compareTo(closingTime.minusMinutes(10)) > 0) {
                warningMessages.add(MessageFormat.format(WARNING_MAY_BE_REACHED_AFTER_CLOSING, pickup.getName()));
            }

            if (pickup.getOrders() > 0) {

                if (startTime.compareTo(convertTime(pickup.getStartTime())) > 0) {
                    warningMessages.add(MessageFormat.format(WARNING_MAY_BE_REACHED_AFTER_EXPECTED, pickup.getName()));
                }

                break;
            }

            prev = pickup;
        }
    }

    private Restaurant getFirstOrderRestaurant() {
        for (Restaurant restaurant : pickups) {
            if (restaurant.getOrders() > 0) {
                return restaurant;
            }
        }

        return null;
    }

    private String formatTime(LocalTime localTime) {
        return localTime.format(DateTimeFormatter.ofPattern("K:mm")) + " PM";
    }

    LocalTime convertTime(String time) {
        String timeStr = time.replaceAll("[ pmPM]", "");
        int colonIndex = timeStr.indexOf(':');
        if (colonIndex == -1) {
            throw new MemberDataException("Restaurant time " + time + " is not of the form: \"5:45 PM\"");
        }

        // Need to pre-pad with a 0, to make a 2 digit hour, to make LocalTime happy.
        if (colonIndex == 1) {
            timeStr = "0" + timeStr;
        }

        return LocalTime.parse(timeStr);
    }
}