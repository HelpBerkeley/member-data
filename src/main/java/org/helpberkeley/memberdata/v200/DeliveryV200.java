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
package org.helpberkeley.memberdata.v200;

import org.helpberkeley.memberdata.Delivery;

public class DeliveryV200 extends Delivery {

    private String normalRations;
    private String veggieRations;
    private String restaurant;

    public DeliveryV200(String name, int lineNumber) {
        super(name, lineNumber);
    }

    @Override
    public String toString() {
        return getUserName()
                + ", condo:" + isCondo
                + ", restaurant: " + getRestaurant()
                + ", rations: " + getNormalRations() + ":" + getVeggieRations();
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public void setNormalRations(String normalRations) {
        this.normalRations = normalRations;
    }

    public void setVeggieRations(String veggieRations) {
        this.veggieRations = veggieRations;
    }

    public String getNormalRations() {
        return normalRations;
    }

    public String getVeggieRations() {
        return veggieRations;
    }

    public String getRestaurant() {
        return restaurant;
    }
}
