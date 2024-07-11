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

public interface WorkflowBean {
    String getVersion();

    String getConsumer();
    String getDriver();
    String getName();
    String getUserName();
    String getPhone();
    String getAltPhone();
    String getNeighborhood();
    String getCity();
    String getAddress();
    String getCondo();
    String getDetails();
    String getRestaurant();
    String getNormal();
    String getVeggie();
    String getOrders();

    void setName(String name);
    void setPhone(String phone);
    void setAltPhone(String altPhone);
    void setNeighborhood(String neighborhood);
    void setCity(String city);
    void setAddress(String address);
    void setCondo(String condo);
    void setDetails(String details);

    boolean isEmpty();

    String getControlBlockDirective();
    String getControlBlockKey();
    String getControlBlockValue();
    String getGMapURL();
    String toCSVString();
    String getCSVHeader();

    default String unsupported(String columnName) {
        throw new MemberDataException("Column heading \""
                + columnName + "\" is not supported in control block version " + getVersion() + " series.");
    }

}
