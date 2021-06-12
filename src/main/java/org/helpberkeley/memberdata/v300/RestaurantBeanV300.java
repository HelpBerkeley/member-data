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
package org.helpberkeley.memberdata.v300;

import com.opencsv.bean.CsvBindByName;
import org.helpberkeley.memberdata.Constants;
import org.helpberkeley.memberdata.MemberDataException;
import org.helpberkeley.memberdata.RestaurantBean;

public class RestaurantBeanV300 implements RestaurantBean {

    @CsvBindByName(column = Constants.WORKFLOW_CONSUMER_COLUMN)
    private String consumer;

    @CsvBindByName(column = Constants.WORKFLOW_DRIVER_COLUMN)
    private String driver;

    @CsvBindByName(column = Constants.WORKFLOW_NAME_COLUMN)
    private String name;

    @CsvBindByName(column = Constants.WORKFLOW_USER_NAME_COLUMN)
    private String userName;

    @CsvBindByName(column = Constants.WORKFLOW_CITY_COLUMN)
    private String city;

    @CsvBindByName(column = Constants.WORKFLOW_RESTAURANTS_COLUMN)
    private String restaurant;

    @CsvBindByName(column = Constants.WORKFLOW_CONDO_COLUMN)
    private String condo;

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

    @CsvBindByName(column = Constants.WORKFLOW_DETAILS_COLUMN)
    private String details;

    public RestaurantBeanV300() {

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
     * In the restaurant template, in the route block, the
     * consumer column is used for the route.
     *
     * @return route
     */
    public String getRoute() {
        return getConsumer();
    }

    /**
     * In the route block the type grocery column is used for the start time
     *
     * @return start time
     */
    public String getStartTime() {
        return getAltMeals();
    }

    /**
     * In the route block the orders column is used for the closing time
     *
     * @return closing time
     */
    public String getClosingTime() {
        return getTypeMeal();
    }

    /**
     * In the restaurant template the details column is used for the status of the restaurant
     *
     * @return Restaurant active of retired
     */
    public String getActive() {
        return getDetails().trim();
    }

    /**
     * In the restaurant template the alt grocery column is used for the restaurant emoji
     *
     * @return start time
     */
    public String getEmoji() {
        return getStdMeals().trim();
    }

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

    // Column name accessors

    public String consumerColumn() {
        return getColumnName(Constants.WORKFLOW_CONSUMER_COLUMN);
    }

    public String restaurantColumn() {
        return getColumnName(Constants.WORKFLOW_RESTAURANTS_COLUMN);
    }

    public String stdMealsColumn() {
        return getColumnName(Constants.WORKFLOW_STD_MEALS_COLUMN);
    }

    public String altMealsColumn() {
        return getColumnName(Constants.WORKFLOW_ALT_MEALS_COLUMN);
    }

    public String typeMealColumn() {
        return getColumnName(Constants.WORKFLOW_TYPE_MEAL_COLUMN);
    }

    public String altGroceryColumn() {
        return getColumnName(Constants.WORKFLOW_ALT_GROCERY_COLUMN);
    }

    public String startTimeColumn() {
        return altMealsColumn();
    }

    public String closingTimeColumn() {
        return typeMealColumn();
    }

    public String routeColumn() {
        return consumerColumn();
    }

    public String emojiColumn() {
        return stdMealsColumn();
    }


    private String getColumnName(final String fieldName) {
        try {
            CsvBindByName bindByName =
                    RestaurantBeanV300.class.getDeclaredField(fieldName).getAnnotation(CsvBindByName.class);
            return bindByName.column();
        } catch (NoSuchFieldException e) {
            throw new MemberDataException(e);
        }
    }

    // Utility accessors

    public boolean isEmpty() {

        return consumer.isEmpty()
                && driver.isEmpty()
                && restaurant.isEmpty()
                && condo.isEmpty()
                && stdMeals.isEmpty()
                && altMeals.isEmpty()
                && typeMeal.isEmpty()
                && stdGrocery.isEmpty()
                && altGrocery.isEmpty()
                && typeGrocery.isEmpty()
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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
        return typeMeal.trim();
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
        return typeGrocery.trim();
    }

    public void setTypeGrocery(String typeGrocery) {
        this.typeGrocery = typeGrocery;
    }

    public String getDetails() {
        return details.trim();
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
