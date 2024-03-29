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
package org.helpberkeley.memberdata.v300;

import org.helpberkeley.memberdata.Builder;

public class RestaurantBuilder implements Builder {

    private String name = BuilderConstants.DEFAULT_RESTAURANT_NAME;

    // These are invalid fields for a restaurant (pickup), but are
    // included here for audit testing.
    private String stdMealCount = "";
    private String altMealCount = "";
    private String altMealType = "";
    private String stdGroceryCount = "";
    private String altGroceryCount = "";
    private String altGroceryType = "";

    @Override
    public String toString() {
        return build();
    }

    public String build() {
        return "FALSE,,,,,,,,\"9999 999 St., Berkeley, CA\",FALSE,,"
                + name + ","
                + stdMealCount + ","
                + altMealCount + ","
                + altMealType + ","
                + stdGroceryCount + ","
                + altGroceryCount + ","
                + altGroceryType
                + "\n";
    }

    public RestaurantBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public RestaurantBuilder withStdMealCount(String count) {
        this.stdMealCount = count;
        return this;
    }

    public RestaurantBuilder withAltMealCount(String count) {
        this.altMealCount = count;
        return this;
    }

    public RestaurantBuilder withAltMealType(String type) {
        this.altMealType = type;
        return this;
    }

    public RestaurantBuilder withStdGroceryCount(String count) {
        this.stdGroceryCount = count;
        return this;
    }

    public RestaurantBuilder withAltGroceryCount(String count) {
        this.altGroceryCount = count;
        return this;
    }

    public RestaurantBuilder withAltGroceryType(String type) {
        this.altGroceryType = type;
        return this;
    }
}