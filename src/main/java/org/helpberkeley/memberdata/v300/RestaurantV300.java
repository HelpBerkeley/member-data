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

import org.helpberkeley.memberdata.*;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class RestaurantV300 extends Restaurant {

    public static final String ERROR_NON_EMPTY_COLUMN = "{0} column must be empty";

    private final HashSet<String> alternateMealTypes = new LinkedHashSet<>();
    private final HashSet<String> alternateGroceryTypes = new LinkedHashSet<>();

    public RestaurantV300(ControlBlock controlBlock, String name, int lineNumber) {
        super(name, lineNumber);
        ControlBlockV300 controlBlock1 = (ControlBlockV300) controlBlock;

        if (controlBlock1.getMealSource().equals(name)) {
            if (controlBlock1.getAltMealOptions() != null) {
                alternateMealTypes.addAll(controlBlock1.getAltMealOptions());
            }
        }

        if (controlBlock1.getGrocerySource().equals(name)) {
            if (controlBlock1.getAltGroceryOptions() != null) {
                alternateGroceryTypes.addAll(controlBlock1.getAltGroceryOptions());
            }
        }
    }

    @Override
    public String toString() {
        return getName() + ", start:" + getStartTime() + ", drivers:" + getDrivers().keySet();
    }

    @Override
    protected void setVersionSpecificFields(RestaurantBean restaurantBean) {
    }

    @Override
    protected String setVersionSpecificFields(WorkflowBean workflowBean) {
        WorkflowBeanV300 bean = (WorkflowBeanV300) workflowBean;

        StringBuilder errors = new StringBuilder();

        if (! bean.getStdMeals().isEmpty()) {
            errors.append(MessageFormat.format(
                    ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_STD_MEALS_COLUMN));
        }

        if (! bean.getAltMeals().isEmpty()) {
            errors.append(", ");
            errors.append(MessageFormat.format(
                    ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_ALT_MEALS_COLUMN));
        }

        if (! bean.getTypeMeal().isEmpty()) {
            errors.append(", ");
            errors.append(MessageFormat.format(
                    ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_TYPE_MEAL_COLUMN));
        }

        if (! bean.getStdGrocery().isEmpty()) {
            errors.append(", ");
            errors.append(MessageFormat.format(
                    ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_STD_GROCERY_COLUMN));
        }

        if (! bean.getAltGrocery().isEmpty()) {
            errors.append(", ");
            errors.append(MessageFormat.format(
                    ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_ALT_GROCERY_COLUMN));
        }

        if (! bean.getTypeGrocery().isEmpty()) {
            errors.append(", ");
            errors.append(MessageFormat.format(
                    ERROR_NON_EMPTY_COLUMN, Constants.WORKFLOW_TYPE_GROCERY_COLUMN));
        }

        return errors.toString();
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
}
