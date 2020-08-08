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

import com.opencsv.bean.CsvBindByName;

public class RestaurantBean {

    @CsvBindByName(column = Constants.WORKFLOW_CONSUMER_COLUMN)
    private String consumer;

    @CsvBindByName(column = Constants.WORKFLOW_DRIVER_COLUMN)
    private String driver;

    @CsvBindByName(column = Constants.WORKFLOW_NAME_COLUMN)
    private String name;

    @CsvBindByName(column = Constants.WORKFLOW_RESTAURANTS_COLUMN)
    private String restaurant;

    @CsvBindByName(column = Constants.WORKFLOW_CONDO_COLUMN)
    private String condo;

    @CsvBindByName(column = Constants.WORKFLOW_ORDERS_COLUMN)
    private String orders;

    @CsvBindByName(column = Constants.WORKFLOW_DETAILS_COLUMN)
    private String details;

    public RestaurantBean() {

    }

    // Accessors for overloaded columns

    /**
     * In the restaurant template, in the route block, the
     * consumer column is used for the route.
     *
     * @return route
     */
    public String getRoute() {
        return getConsumer();
    }

    /**
     * In the restaurant template, in the route block, the
     * orders column is used for the start time
     *
     * @return start time
     */
    public String getStartTime() {
        return getOrders();
    }

    /**
     * In the restaurant template, in the route block, the
     * condo column is used for No Pics
     *
     * @return No Pics
     */
    public String getNoPics() {
        return getCondo();
    }

    // Accessors for overloaded columns

    /**
     * In control blocks, the Name column is used for the directive
     * @return Control block directive value
     */
    public String getControlBlockDirective() {
        return getName().trim();
    }

    // Column name accessors

    public String consumerColumn() {
        return getColumnName("consumer");
    }

    public String restaurantColumn() {
        return getColumnName("restaurant");
    }

    public String ordersColumn() {
        return getColumnName("orders");
    }

    public String startTimeColumn() {
        return ordersColumn();
    }

    public String routeColumn() {
        return consumerColumn();
    }


    private String getColumnName(final String fieldName) {
        try {
            CsvBindByName bindByName =
                    RestaurantBean.class.getDeclaredField(fieldName).getAnnotation(CsvBindByName.class);
            return bindByName.column();
        } catch (NoSuchFieldException e) {
            throw new MemberDataException(e.getMessage());
        }
    }

    // Utility accessors

    public boolean isEmpty() {

        return consumer.isEmpty()
                && driver.isEmpty()
                && restaurant.isEmpty()
                && condo.isEmpty()
                && orders.isEmpty()
                && details.isEmpty();

    }

    // Annotated accessors for CSV parser

    public String getConsumer() {
        return consumer.trim();
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getDriver() {
        return driver.trim();
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRestaurant() {
        return restaurant.trim();
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public String getCondo() {
        return condo.trim();
    }

    public void setCondo(String condo) {
        this.condo = condo;
    }

    public String getOrders() {
        return orders.trim();
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public String getDetails() {
        return details.trim();
    }

    public void setDetails(String details) {
        this.details = details;
    }
}