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

import org.helpberkeley.memberdata.Constants;

import static org.helpberkeley.memberdata.v300.ControlBlockTest.EMPTY_ROW;

public class ControlBlockBuilder {

    public static final String DEFAULT_FOOD_SOURCES = "RevFoodTruck|BFN";
    public static final String DEFAULT_START_TIMES = "3:00 PM, 3:15 PM";
    public static final String DEFAULT_PICKUP_MANAGER = "ZZZ";
    public static final String DEFAULT_OPS_MANAGER = "JVol|123-456-7890";
    public static final String DEFAULT_ALT_MEAL_OPTIONS = "veggie, noPork";
    public static final String DEFAULT_ALT_GROCERY_OPTIONS = "veg, custom pick";

    private String startTimes = DEFAULT_START_TIMES;
    private String pickupManager = DEFAULT_PICKUP_MANAGER;
    private String opsManager = DEFAULT_OPS_MANAGER;
    private String foodSources = DEFAULT_FOOD_SOURCES;
    private String altMealOptions = DEFAULT_ALT_MEAL_OPTIONS;
    private String altGroceryOptions = DEFAULT_ALT_GROCERY_OPTIONS;

    @Override
    public String toString() {
        return build();
    }

    public String build() {
        StringBuilder controlBlock = new StringBuilder();

        controlBlock.append(ControlBlockTest.HEADER);
        controlBlock.append(ControlBlockTest.CONTROL_BLOCK_BEGIN_ROW);
        controlBlock.append(ControlBlockTest.CONTROL_BLOCK_VERSION_ROW);
        controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_START_TIMES, quote(startTimes)));
        controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_OPS_MANAGER, opsManager));
        controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_PICKUP_MANAGER, pickupManager));
        controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_FOOD_SOURCES, foodSources));
        controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS,
                altMealOptions.isEmpty() ? altMealOptions : quote(altMealOptions)));
        controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS,
                altGroceryOptions.isEmpty() ? altGroceryOptions : quote(altGroceryOptions)));
        controlBlock.append(ControlBlockTest.CONTROL_BLOCK_END_ROW);
        controlBlock.append(EMPTY_ROW);

        return controlBlock.toString();
    }

    public ControlBlockBuilder withStartTimes(String startTimes) {
        this.startTimes = startTimes;
        return this;
    }

    public ControlBlockBuilder withAltMealOptions(String mealOptions) {
        this.altMealOptions = mealOptions;
        return this;
    }

    public ControlBlockBuilder withAltGroceryOptions(String groceryOptions) {
        this.altGroceryOptions = groceryOptions;
        return this;
    }

    public ControlBlockBuilder withPickupManager(String pickupManager) {
        this.pickupManager = pickupManager;
        return this;
    }

    public ControlBlockBuilder withFoodSources(String foodSources) {
        this.foodSources = foodSources;
        return this;
    }

    public ControlBlockBuilder withOpsManager(String opsManager) {
        this.opsManager = opsManager;
        return this;
    }

    private String getDirectiveRow(String directive) {
        return EMPTY_ROW.replaceFirst(",,,", "FALSE,FALSE," + directive + ",");
    }

    private String getKeyValueRow(String key, String value) {
        return EMPTY_ROW.replaceFirst(",,,,,,,", "FALSE,FALSE,," + key + ",,,," + value);
    }

    private String quote(String quotee) {
        return "\"" + quotee + "\"";
    }
}
