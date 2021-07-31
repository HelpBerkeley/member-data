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

import org.helpberkeley.memberdata.v200.RestaurantV200;
import org.helpberkeley.memberdata.v300.RestaurantV300;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Restaurant implements ItineraryStop {
    protected final String name;
    protected final int lineNumber;
    protected String address = "";
    private String startTime = "";
    private String closingTime = "";
    private int closingTimeValue = 0;
    protected String details = "";
    private String emoji = "";
    private String route = "";
    private final Map<String, Driver> drivers = new HashMap<>();

    protected Restaurant(String name, int lineNumber) {
        this.name = name;
        this.lineNumber = lineNumber;
    }

    public static Restaurant createRestaurant(ControlBlock controlBlock, String name, int lineNumber) {
        switch (controlBlock.getVersion()) {
            case Constants.CONTROL_BLOCK_VERSION_200:
                return new RestaurantV200(controlBlock, name, lineNumber);
            case Constants.CONTROL_BLOCK_VERSION_300:
                return new RestaurantV300(controlBlock, name, lineNumber);
            default:
                throw new MemberDataException("Control block version " + controlBlock.getVersion()
                        + " is not supported for restaurant creation");
        }
    }

    protected abstract void setVersionSpecificFields(RestaurantBean restaurantBean);
    protected abstract String setVersionSpecificFields(WorkflowBean workflowBean);
    protected abstract void mergeInGlobalVersionSpecificFields(Restaurant globalRestaurant);

    @Override
    public ItineraryStopType getType() {
        return ItineraryStopType.PICKUP;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    public void setStartTime(final String startTime) {
        this.startTime = startTime;
    }
    public void setClosingTime(final String closingTime) {
        this.closingTime = closingTime;
        this.closingTimeValue = convertTime(closingTime);
    }

    // FIX THIS, DS: need audit/assert for expected time string
    private int convertTime(String time) {
        String digits = time.replaceAll("[ :pmPM]", "");
        return Integer.parseInt(digits);
    }
    void setDetails(final String details) {
        this.details = (details == null) ? "" : details;
    }
    public void setRoute(final String route) {
        this.route = route;
    }
    void setAddress(final String address) {
        this.address = address;
    }
    public void addDriver(final Driver driver) {
        assert ! drivers.containsKey(driver.getUserName()) : driver.getUserName();
        drivers.put(driver.getUserName(), driver);
    }
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getName() {
        return name;
    }

    public String getRoute() {
        return route;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public String getDetails() {
        return details;
    }

    public String getAddress() {
        return address;
    }

    public String getEmoji() {
        return emoji;
    }

    public Map<String, Driver> getDrivers() {
        return Collections.unmodifiableMap(drivers);
    }

    protected void mergeGlobal(Restaurant globalRestaurant) {

        // FIX THIS, DS: add enough data to audit address against template?

        route = globalRestaurant.route;
        startTime = globalRestaurant.startTime;
        closingTime = globalRestaurant.closingTime;
        emoji = globalRestaurant.emoji;
        mergeInGlobalVersionSpecificFields(globalRestaurant);
    }

    /**
     *
     * @param closingTime 545 means 5:45 PM, 700, 7:00 PM, etc...
     * @return Does this restaurant close before the passed in time?
     */
    public boolean closesBefore(int closingTime) {
        return closingTimeValue < closingTime;
    }
}
