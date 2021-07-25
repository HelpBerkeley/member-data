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

import org.helpberkeley.memberdata.Delivery;

public class DeliveryV300 extends Delivery {

    private int stdMeals;
    private int altMeals;
    private String typeMeal;
    private int stdGrocery;
    private int altGrocery;
    private String typeGrocery;

    @Override
    public String toString() {
        return getUserName()
                + ", condo:" + isCondo
                + ", meals:" + getStdMeals() + ':' + getAltMeals()
                + ", grocery:" + getStdGrocery() + ':' + getAltGrocery();
    }

    public DeliveryV300(String name, long lineNumber) {
        super(name, lineNumber);
    }

    public void setStdMeals(String stdMeals) {
        this.stdMeals = Integer.parseInt(stdMeals);
    }

    public void setAltMeals(String altMeals) {
        this.altMeals = Integer.parseInt(altMeals);
    }

    public void setTypeMeal(String typeMeal) {
        this.typeMeal = typeMeal;
    }

    public void setStdGrocery(String stdGrocery) {
        this.stdGrocery = Integer.parseInt(stdGrocery);
    }

    public void setAltGrocery(String altGrocery) {
        this.altGrocery = Integer.parseInt(altGrocery);
    }

    public void setTypeGrocery(String typeGrocery) {
        this.typeGrocery = typeGrocery;
    }

    public int getStdMeals() {
        return stdMeals;
    }

    public int getAltMeals() {
        return altMeals;
    }

    public String getTypeMeal() {
        return typeMeal;
    }

    public int getStdGrocery() {
        return stdGrocery;
    }

    public int getAltGrocery() {
        return altGrocery;
    }

    public String getTypeGrocery() {
        return typeGrocery;
    }
}
