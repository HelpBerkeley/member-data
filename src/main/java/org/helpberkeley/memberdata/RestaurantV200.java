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
package org.helpberkeley.memberdata;

public class RestaurantV200 extends Restaurant {

    private boolean noPics = false;
    private long orders = 0;
    private final ControlBlockV200 controlBlock;

    RestaurantV200(ControlBlock controlBlock, String name) {
        super(name);
        this.controlBlock = (ControlBlockV200) controlBlock;
    }

    @Override
    public String toString() {
        return getName() + ", orders: " + orders + ", start:" + getStartTime() + ", drivers:" + getDrivers().keySet();
    }

    // FIX THIS, DS: columns are hard wired
    //
    public String pickupRow() {
        StringBuilder row = new StringBuilder();

        // Consumer, Driver, Name, UserName, Phone, Alt Phone, Neighborhood, City
        row.append("FALSE,,,,,,,,");

        // Address
        row.append('"').append(address).append('"').append(',');

        // Condo
        row.append("FALSE,");

        // Details
        if (! details.isEmpty()) {
            row.append('"').append(details).append('"').append(',');
        } else {
            row.append(',');
        }

        // Restaurant
        row.append('"').append(name).append('"').append(',');

        // Empty columns for  normal and veggie
        row.append(",,");

        // Orders
        row.append(orders);

        row.append('\n');

        return row.toString();
    }

    @Override
    protected void setVersionSpecificFields(RestaurantBean restaurantBean) {

        RestaurantBeanV200 bean = (RestaurantBeanV200) restaurantBean;

        if (bean.getNoPics().equalsIgnoreCase(Constants.WORKFLOW_NO_PICS)) {
            noPics = true;
        }
    }

    @Override
    protected String setVersionSpecificFields(WorkflowBean workflowBean) {

        String errors = "";

        WorkflowBeanV200 bean = (WorkflowBeanV200) workflowBean;
        String orders = bean.getOrders();
        if (orders.isEmpty()) {
             errors = "missing orders";
        } else {
            double numOrders = Double.parseDouble(orders.trim());
            this.orders += Math.round(numOrders);
        }

        return errors;
    }

    @Override
    protected void mergeInGlobalVersionSpecificFields(Restaurant restaurant) {
        RestaurantV200 globalRestaurant = (RestaurantV200) restaurant;
        noPics = globalRestaurant.noPics;
    }

    void addOrders(long orders) {
        this.orders += orders;
    }

    public long getOrders() {
        return orders;
    }

    public boolean getNoPics() {
        return noPics;
    }
}
