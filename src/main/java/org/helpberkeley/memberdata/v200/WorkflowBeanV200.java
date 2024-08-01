/*
 * Copyright (c) 2020-2024. helpberkeley.org
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

import com.opencsv.bean.CsvBindByName;
import org.helpberkeley.memberdata.Constants;
import org.helpberkeley.memberdata.Exporter;
import org.helpberkeley.memberdata.WorkflowBean;

import java.util.ArrayList;
import java.util.List;

public class WorkflowBeanV200 implements WorkflowBean {

    // Consumer,Driver,Name,User Name,Phone #,Phone2 #,Neighborhood,City,Address,Condo,Details,Restaurants,normal,veggie,#orders

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
    @CsvBindByName(column = Constants.WORKFLOW_NORMAL_COLUMN)
    private String normal;
    @CsvBindByName(column = Constants.WORKFLOW_VEGGIE_COLUMN)
    private String veggie;
    @CsvBindByName(column = Constants.WORKFLOW_ORDERS_COLUMN)
    private String orders;

    public WorkflowBeanV200() {

    }

    public String getVersion() {
        return Constants.CONTROL_BLOCK_VERSION_200;
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
            && normal.isEmpty()
            && veggie.isEmpty()
            && orders.isEmpty();
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

    public String getNormal() {
        return normal.trim();
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    public String getVeggie() {
        return veggie.trim();
    }

    public void setVeggie(String veggie) {
        this.veggie = veggie;
    }

    public String getOrders() {
        return orders.trim();
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public List<String> toCSVListRow() {
        return new ArrayList<>(List.of(Exporter.escapeCommas(consumer),
                Exporter.escapeCommas(driver),
                Exporter.escapeCommas(name),
                Exporter.escapeCommas(userName),
                Exporter.escapeCommas(phone),
                Exporter.escapeCommas(altPhone),
                Exporter.escapeCommas(neighborhood),
                Exporter.escapeCommas(city),
                Exporter.escapeCommas(address),
                Exporter.escapeCommas(condo),
                Exporter.escapeCommas(details),
                Exporter.escapeCommas(restaurant),
                Exporter.escapeCommas(normal),
                Exporter.escapeCommas(veggie),
                Exporter.escapeCommas(orders)));
    }

    public List<String> getCSVHeader() {
        return new ArrayList<>(List.of(Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_NAME_COLUMN,
                Constants.WORKFLOW_USER_NAME_COLUMN,
                Constants.WORKFLOW_PHONE_COLUMN,
                Constants.WORKFLOW_ALT_PHONE_COLUMN,
                Constants.WORKFLOW_NEIGHBORHOOD_COLUMN,
                Constants.WORKFLOW_CITY_COLUMN,
                Constants.WORKFLOW_ADDRESS_COLUMN,
                Constants.WORKFLOW_CONDO_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_NORMAL_COLUMN,
                Constants.WORKFLOW_VEGGIE_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN));
    }
}
