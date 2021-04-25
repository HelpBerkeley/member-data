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

public interface RestaurantBean {
    String getVersion();

    String getConsumer();
    String getDriver();
    String getName();
    String getUserName();
    String getCity();
    String getRestaurant();
    String getCondo();
    String getNormal();
    String getVeggie();
    String getDetails();

    String getControlBlockDirective();
    String getControlBlockKey();
    String getControlBlockValue();
    String getRoute();
    boolean isEmpty();
    String routeColumn();
    String getActive();
    String getStartTime();
    String getClosingTime();
    String getEmoji();

    String restaurantColumn();
    String startTimeColumn();
    String closingTimeColumn();
    String emojiColumn();

    default String unsupported(String columnName) {
        throw new MemberDataException("Column heading \""
                + columnName + "\" is not supported in control block version " + getVersion());
    }
}
