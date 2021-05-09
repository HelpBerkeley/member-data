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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class RestaurantV300 extends Restaurant {

    private final ControlBlockV300 controlBlock;
    private final HashSet<String> alternateMealTypes = new LinkedHashSet<>();
    private final HashSet<String> alternateGroceryTypes = new LinkedHashSet<>();

    RestaurantV300(ControlBlock controlBlock, String name) {
        super(name);
        this.controlBlock = (ControlBlockV300) controlBlock;

        if (this.controlBlock.getMealSource().equals(name)) {
            if (this.controlBlock.getAltMealOptions() != null) {
                for (String altMealType : this.controlBlock.getAltMealOptions()) {
                    alternateMealTypes.add(altMealType);
                }
            }
        }

        if (this.controlBlock.getGrocerySource().equals(name)) {
            if (this.controlBlock.getAltGroceryOptions() != null) {
                for (String altGroceryType : this.controlBlock.getAltGroceryOptions()) {
                    alternateGroceryTypes.add(altGroceryType);
                }
            }
        }
    }

    @Override
    public String toString() {
        return getName() + ", start:" + getStartTime() + ", drivers:" + getDrivers().keySet();
    }

    public String pickupRow() {
        throw new MemberDataException("Routing requests for version "
                + Constants.CONTROL_BLOCK_VERSION_300 + " are not current supported");
    }

    @Override
    protected void setVersionSpecificFields(RestaurantBean restaurantBean) {
        RestaurantBeanV300 bean = (RestaurantBeanV300) restaurantBean;
    }

    @Override
    protected String setVersionSpecificFields(WorkflowBean workflowBean) {
        WorkflowBeanV300 bean = (WorkflowBeanV300) workflowBean;

        // FIX THIS, DS: implement
        return "";
    }

    @Override
    protected void mergeInGlobalVersionSpecificFields(Restaurant restaurant) {
        RestaurantV300 globalRestaurant = (RestaurantV300) restaurant;

        if (alternateMealTypes.isEmpty()) {
            alternateMealTypes.addAll(globalRestaurant.alternateMealTypes);
        } else {
            assert alternateMealTypes.containsAll(globalRestaurant.alternateMealTypes);
        }

        if (alternateGroceryTypes.isEmpty()) {
            alternateGroceryTypes.addAll(globalRestaurant.alternateGroceryTypes);
        } else {
            assert alternateGroceryTypes.containsAll(globalRestaurant.alternateGroceryTypes);
        }
    }

    public Set<String> getAlternateMealTypes() {
        return alternateMealTypes;
    }

    public Set<String> getAlternateGroceryTypes() {
        return alternateGroceryTypes;
    }
}
