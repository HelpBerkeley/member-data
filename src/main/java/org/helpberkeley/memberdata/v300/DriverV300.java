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

import org.helpberkeley.memberdata.Delivery;
import org.helpberkeley.memberdata.Driver;

import java.util.List;

public class DriverV300 extends Driver {

    private String startTime;

    public DriverV300() {

    }

    @Override
    public void initialize() {

    }

    void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Override
    public String getStartTime() {
        assert startTime != null;
        return startTime;
    }

    String getStandardMeals() {

        int standardMeals = 0;
            for (DeliveryV300 delivery : (List<DeliveryV300>)(List<? extends Delivery>)deliveries) {
            standardMeals += delivery.getStdMeals();
        }

        return String.valueOf(standardMeals);
    }

    String getStandardGroceries() {

        int standardGroceries = 0;
            for (DeliveryV300 delivery : (List<DeliveryV300>)(List<? extends Delivery>)deliveries) {
            standardGroceries += delivery.getStdGrocery();
        }

        return String.valueOf(standardGroceries);
    }

    int getAltMeals(String mealType) {
        int altMeals = 0;
        for (DeliveryV300 delivery : (List<DeliveryV300>)(List<? extends Delivery>)deliveries) {
            int numAltMeals = delivery.getAltMeals();

            if ((numAltMeals > 0) && delivery.getTypeMeal().equals(mealType)) {
                altMeals += numAltMeals;
            }
        }

        return altMeals;
    }

    int getAltGroceries(String groceryType) {
        int altGroceries = 0;
        for (DeliveryV300 delivery : (List<DeliveryV300>)(List<? extends Delivery>)deliveries) {
            int numAltGroceries = delivery.getAltGrocery();

            if ((numAltGroceries > 0) && delivery.getTypeGrocery().equals(groceryType)) {
                altGroceries += numAltGroceries;
            }
        }

        return altGroceries;
    }
}
