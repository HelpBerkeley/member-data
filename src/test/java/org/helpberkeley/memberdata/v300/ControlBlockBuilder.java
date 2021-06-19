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
import org.helpberkeley.memberdata.MessageSpecFormat;

import java.util.ArrayList;
import java.util.List;

import static org.helpberkeley.memberdata.v300.ControlBlockTest.EMPTY_ROW;

public class ControlBlockBuilder {

    public static final String DEFAULT_FOOD_SOURCES = "RevFoodTruck|BFN";
    public static final String DEFAULT_START_TIMES = "3:00 PM, 3:15 PM";
    public static final String DEFAULT_PICKUP_MANAGER = "ZZZ";
    public static final String DEFAULT_OPS_MANAGER = "JVol|123-456-7890";
    public static final String DEFAULT_ALT_MEAL_OPTIONS = "veggie, noPork";
    public static final String DEFAULT_ALT_GROCERY_OPTIONS = "veg, custom pick";
    public static final String DEFAULT_MESSAGE_FORMAT = MessageSpecFormat.MONDAY.getFormat();

    private final List<String> DEFAULT_PICKUP_MANAGERS_LIST = List.of(DEFAULT_PICKUP_MANAGER);
    private final List<String> DEFAULT_ALT_MEAL_OPTIONS_LIST = List.of(DEFAULT_ALT_MEAL_OPTIONS);
    private final List<String> DEFAULT_ALT_GROCERY_OPTIONS_LIST = List.of(DEFAULT_ALT_GROCERY_OPTIONS);
    private final List<String> DEFAULT_START_TIMES_LIST = List.of(DEFAULT_START_TIMES);
    private final List<String> DEFAULT_MESSAGE_FORMAT_LIST = List.of(DEFAULT_MESSAGE_FORMAT);

    private List<String> startTimes = DEFAULT_START_TIMES_LIST;
    private String opsManager = DEFAULT_OPS_MANAGER;
    private String foodSources = DEFAULT_FOOD_SOURCES;
    private List<String> altMealOptions = DEFAULT_ALT_MEAL_OPTIONS_LIST;
    private List<String> altGroceryOptions = DEFAULT_ALT_GROCERY_OPTIONS_LIST;
    private List<String> messageFormat = DEFAULT_MESSAGE_FORMAT_LIST;
    private String backupDriver = null;
    private List<String> pickupManagers = DEFAULT_PICKUP_MANAGERS_LIST;

    @Override
    public String toString() {
        return build();
    }

    public String build() {
        StringBuilder controlBlock = new StringBuilder();

        controlBlock.append(ControlBlockTest.HEADER);
        controlBlock.append(ControlBlockTest.CONTROL_BLOCK_BEGIN_ROW);
        controlBlock.append(ControlBlockTest.CONTROL_BLOCK_VERSION_ROW);

        if (messageFormat != null) {
            messageFormat.forEach((s) -> controlBlock.append(
                    getKeyValueRow(Constants.CONTROL_BLOCK_MESSAGE_FORMAT, quote(s))));
        }
        if (startTimes != null) {
            startTimes.forEach((s) -> controlBlock.append(
                    getKeyValueRow(Constants.CONTROL_BLOCK_START_TIMES, quote(s))));
        }
        if (opsManager != null) {
            controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_OPS_MANAGER, opsManager));
        }
        if (pickupManagers != null) {
            pickupManagers.forEach((p) -> controlBlock.append(
                    getKeyValueRow(Constants.CONTROL_BLOCK_PICKUP_MANAGER, p)));
        }
        if (foodSources != null) {
            controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_FOOD_SOURCES, foodSources));
        }
        if (altMealOptions != null) {
            if (altMealOptions.isEmpty()) {
                controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS, ""));
            } else {
                altMealOptions.forEach((o) -> controlBlock.append(
                        getKeyValueRow(Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS, quote(o))));
            }
        }
        if (altGroceryOptions != null) {
            if (altGroceryOptions.isEmpty()) {
                controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS, ""));
            } else {
                altGroceryOptions.forEach((o) -> controlBlock.append(
                        getKeyValueRow(Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS, quote(o))));
            }
        }
        if (backupDriver != null) {
            controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_BACKUP_DRIVER, backupDriver));
        }
        controlBlock.append(ControlBlockTest.CONTROL_BLOCK_END_ROW);
        controlBlock.append(EMPTY_ROW);

        return controlBlock.toString();
    }

    public ControlBlockBuilder withStartTimes(String startTimes) {
        if (this.startTimes == DEFAULT_START_TIMES_LIST) {
            this.startTimes = new ArrayList<>();
        }
        this.startTimes.add(startTimes);
        return this;
    }

    public ControlBlockBuilder withoutStartTimes() {
        this.startTimes = null;
        return this;
    }

    public ControlBlockBuilder withAltMealOptions(String mealOptions) {
        if (altMealOptions == DEFAULT_ALT_MEAL_OPTIONS_LIST) {
            altMealOptions = new ArrayList<>();
        }
        altMealOptions.add(mealOptions);
        return this;
    }

    public ControlBlockBuilder withoutAltMealOptions() {
        this.altMealOptions = null;
        return this;
    }

    public ControlBlockBuilder withAltGroceryOptions(String groceryOptions) {
        if (altGroceryOptions == DEFAULT_ALT_GROCERY_OPTIONS_LIST) {
            altGroceryOptions = new ArrayList<>();
        }
        altGroceryOptions.add(groceryOptions);
        return this;
    }

    public ControlBlockBuilder withoutAltGroceryOptions() {
        this.altGroceryOptions = null;
        return this;
    }

    public ControlBlockBuilder withPickupManager(String pickupManager) {
        if (pickupManagers == DEFAULT_PICKUP_MANAGERS_LIST) {
            pickupManagers = new ArrayList<>();
        }
        pickupManagers.add(pickupManager);
        return this;
    }

    public ControlBlockBuilder withoutPickupManager() {
        this.pickupManagers = null;
        return this;
    }

    public ControlBlockBuilder withFoodSources(String foodSources) {
        this.foodSources = foodSources;
        return this;
    }

    public ControlBlockBuilder withoutFoodSources() {
        this.foodSources = null;
        return this;
    }

    public ControlBlockBuilder withOpsManager(String opsManager) {
        this.opsManager = opsManager;
        return this;
    }

    public ControlBlockBuilder withoutOpsManager() {
        this.opsManager = null;
        return this;
    }

    public ControlBlockBuilder withMessageFormat(String format) {
        if (this.messageFormat == DEFAULT_MESSAGE_FORMAT_LIST) {
            this.messageFormat = new ArrayList<>();
        }
        this.messageFormat.add(format);
        return this;
    }

    public ControlBlockBuilder withoutMessageFormat() {
        this.messageFormat = null;
        return this;
    }

    public ControlBlockBuilder withBackupDriver(String backupDriver) {
        this.backupDriver = backupDriver;
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
