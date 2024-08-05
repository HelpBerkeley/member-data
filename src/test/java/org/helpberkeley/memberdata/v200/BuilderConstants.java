/*
 * Copyright (c) 2021-2024. helpberkeley.org
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

import org.helpberkeley.memberdata.Constants;

public class BuilderConstants {
    public static final String HEADER =
            "Consumer,Driver,Name,User Name,Phone #,Phone2 #,Neighborhood,City,"
                    + "Address,Condo,Details,Restaurants,normal,veggie,#orders\n";
    public static final String EMPTY_ROW = ",,,,,,,,,,,,,,\n";
    public static final String CONTROL_BLOCK_BEGIN_ROW =
            "FALSE,FALSE," + Constants.CONTROL_BLOCK_BEGIN + ",,,,,,,,,,,,\n";
    public static final String CONTROL_BLOCK_END_ROW =
            "FALSE,FALSE," + Constants.CONTROL_BLOCK_END + ",,,,,,,,,,,,\n";
    public static final String CONTROL_BLOCK_VERSION_ROW =
            "FALSE,FALSE,,Version,,,," + Constants.CONTROL_BLOCK_VERSION_200 + ",,,,,,,\n";
    public static final String DEFAULT_CONSUMER_NAME = "Cust name 1";
    public static final String DEFAULT_CONSUMER_USER_NAME = "cust1";
    public static final String DEFAULT_RESTAURANT_NAME = "Cafe Raj";
    public static final String DEFAULT_RESTAURANT_ADDRESS = "1234 1234th Ave";
    public static final String DEFAULT_DRIVER_USER_NAME = "jbDriver";
    public static final String DEFAULT_DRIVER_NAME = "Joe B. Driver";
}
