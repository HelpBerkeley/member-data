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

import com.opencsv.bean.CsvBindByName;

public class WorkflowBeanV300 implements WorkflowBean {

    @CsvBindByName(column = Constants.WORKFLOW_CONSUMER_COLUMN)
    private String consumer;
    @CsvBindByName(column = Constants.WORKFLOW_DRIVER_COLUMN)
    private String driver;
    @CsvBindByName(column = Constants.WORKFLOW_NAME_COLUMN)
    private String name;
    @CsvBindByName(column = Constants.WORKFLOW_USER_NAME_COLUMN)
    private String userName;
    @CsvBindByName(column = Constants.WORKFLOW_PHONE_COLUMN)
    private String phone;
    @CsvBindByName(column = Constants.WORKFLOW_ALT_PHONE_COLUMN)
    private String altPhone;
    @CsvBindByName(column = Constants.WORKFLOW_NEIGHBORHOOD_COLUMN)
    private String neighborhood;
    @CsvBindByName(column = Constants.WORKFLOW_CITY_COLUMN)
    private String city;
    @CsvBindByName(column = Constants.WORKFLOW_ADDRESS_COLUMN)
    private String address;
    @CsvBindByName(column = Constants.WORKFLOW_CONDO_COLUMN)
    private String condo;
    @CsvBindByName(column = Constants.WORKFLOW_DETAILS_COLUMN)
    private String details;
    @CsvBindByName(column = Constants.WORKFLOW_RESTAURANTS_COLUMN)
    private String restaurant;
    @CsvBindByName(column = Constants.WORKFLOW_STD_MEALS_COLUMN)
    private String stdMeals;
    @CsvBindByName(column = Constants.WORKFLOW_ALT_MEALS_COLUMN)
    private String altMeals;
    @CsvBindByName(column = Constants.WORKFLOW_TYPE_MEAL_COLUMN)
    private String typeMeal;
    @CsvBindByName(column = Constants.WORKFLOW_STD_GROCERY_COLUMN)
    private String stdGrocery;
    @CsvBindByName(column = Constants.WORKFLOW_ALT_GROCERY_COLUMN)
    private String altGrocery;
    @CsvBindByName(column = Constants.WORKFLOW_TYPE_GROCERY_COLUMN)
    private String typeGrocery;
    @CsvBindByName(column = Constants.WORKFLOW_ORDERS_COLUMN)
    private String orders;

    public WorkflowBeanV300() {

    }

    public String getVersion() {
        return Constants.CONTROL_BLOCK_VERSION_300;
    }

    // Unsupported in this version
    public String getVeggie() {
        return unsupported(Constants.WORKFLOW_VEGGIE_COLUMN);
    }
    public String getNormal() {
        return unsupported(Constants.WORKFLOW_NORMAL_COLUMN);
    }

    // Accessors for overloaded columns

    /**
     * In control blocks, the Name column is used for the directive
     * @return Control block directive value
     */
    public String getControlBlockDirective() {
        return getName().trim();
    }

    /**
     * In control blocks, the User Name column is used for the key value
     * @return Control block key value
     */
    public String getControlBlockKey() {
        return getUserName().trim();
    }

    /**
     * In control blocks, the City column is used for the data value
     * @return Control block data value
     */
    public String getControlBlockValue() {
        return getCity().trim();
    }

    /**
     * At the end of driver blocks, the next row uses the Consumer column for
     * the Google Maps URL.
     * @return GMap URL
     */
    public String getGMapURL() {
        return getConsumer();
    }

    // Utility accessors

    /**
     * Is this an empty row?
     * @return Empty row or not
     */
    public boolean isEmpty() {
        return consumer.isEmpty()
            && driver.isEmpty()
            && name.isEmpty()
            && userName.isEmpty()
            && phone.isEmpty()
            && altPhone.isEmpty()
            && neighborhood.isEmpty()
            && city.isEmpty()
            && address.isEmpty()
            && condo.isEmpty()
            && details.isEmpty()
            && restaurant.isEmpty()
            && stdMeals.isEmpty()
            && altMeals.isEmpty()
            && typeMeal.isEmpty()
            && stdGrocery.isEmpty()
            && altGrocery.isEmpty()
            && typeGrocery.isEmpty()
            && orders.isEmpty();
    }

    // Annotated accessors for CSV parser

    public String getConsumer() {
        return consumer;
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
        return name.trim();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName.trim();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone.trim();
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAltPhone() {
        return altPhone.trim();
    }

    public void setAltPhone(String altPhone) {
        this.altPhone = altPhone;
    }

    public String getNeighborhood() {
        return neighborhood.trim();
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city.trim();
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address.trim();
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCondo() {
        return condo.trim();
    }

    public void setCondo(String condo) {
        this.condo = condo;
    }

    public String getDetails() {
        return details.trim();
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getRestaurant() {
        return restaurant.trim();
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public String getStdMeals() {
        return stdMeals.trim();
    }

    public void setStdMeals(String stdMeals) {
        this.stdMeals = stdMeals;
    }

    public String getAltMeals() {
        return altMeals.trim();
    }

    public void setAltMeals(String altMeals) {
        this.altMeals = altMeals;
    }

    public String getTypeMeal() {
        return typeMeal;
    }

    public void setTypeMeal(String typeMeal) {
        this.typeMeal = typeMeal;
    }

    public String getStdGrocery() {
        return stdGrocery.trim();
    }

    public void setStdGrocery(String stdGrocery) {
        this.stdGrocery = stdGrocery;
    }

    public String getAltGrocery() {
        return altGrocery.trim();
    }

    public void setAltGrocery(String altGrocery) {
        this.altGrocery = altGrocery;
    }

    public String getTypeGrocery() {
        return typeGrocery;
    }

    public void setTypeGrocery(String typeGrocery) {
        this.typeGrocery = typeGrocery;
    }

    public String getOrders() {
        return orders.trim();
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }
}
