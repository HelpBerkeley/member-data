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

class DeliveryBuilder {
    private String consumerUserName = "Cust name 1";
    private String consumerName = "cust1";
    private String city = "Berkeley";
    private String address = "123 456th St.";
    private String phone = "555-555-1112";
    private String stdMeals = "";
    private String altMeals = "";
    private String typeMeal = "";
    private String stdGrocery = "";
    private String altGrocery = "";
    private String typeGrocery = "";

    public DeliveryBuilder withStdMeals(String stdMeals) {
        this.stdMeals = stdMeals;
        return this;
    }

    public DeliveryBuilder withAltMeals(String altMeals) {
        this.altMeals = altMeals;
        return this;
    }

    public DeliveryBuilder withTypeMeal(String typeMeal) {
        this.typeMeal = typeMeal;
        return this;
    }

    public DeliveryBuilder withStdGrocery(String stdGrocery) {
        this.stdGrocery = stdGrocery;
        return this;
    }

    public DeliveryBuilder withAltGrocery(String altGrocery) {
        this.altGrocery = altGrocery;
        return this;
    }

    public DeliveryBuilder withTypeGrocery(String typeGrocery) {
        this.typeGrocery = typeGrocery;
        return this;
    }

    public DeliveryBuilder withConsumerName(String consumerName) {
        this.consumerName = consumerName;
        return this;
    }

    public DeliveryBuilder withConsumerUserName(String consumerUserName) {
        this.consumerUserName = consumerUserName;
        return this;
    }

    public DeliveryBuilder withCity(String city) {
        this.city = city;
        return this;
    }

    public DeliveryBuilder withAddress(String address) {
        this.address = address;
        return this;
    }

    public DeliveryBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    @Override
    public String toString() {
        return build();
    }

    public String build() {
        return "TRUE,FALSE,"
                + consumerName
                + ","
                + consumerUserName
                + ","
                + phone
                + ",111-222-3333,"
                + "Hills,"
                + city
                + ","
                + address
                + ",FALSE,,,"
                + stdMeals
                + ","
                + altMeals
                + ","
                + typeMeal
                + ","
                + stdGrocery
                + ","
                + altGrocery
                + ","
                + typeGrocery
                + "\n";
    }
}
