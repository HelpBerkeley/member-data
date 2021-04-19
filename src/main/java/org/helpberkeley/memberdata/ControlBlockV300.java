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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class ControlBlockV300 extends ControlBlock {

    private final List<String> altMealOptions = new ArrayList<>();
    private final List<String> altGroceryOptions = new ArrayList<>();

     ControlBlockV300(String header) {
         super(header);
    }

    @Override
    void audit(Map<String, User> users, Map<String, Restaurant> restaurants, List<Restaurant> splitRestaurants) {
        StringBuilder errors = new StringBuilder();

        auditOpsManager(errors, users);
        auditBackupDrivers(errors, users);

        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }
    }

    @Override
    public String getVersion() {
        return Constants.CONTROL_BLOCK_VERSION_300;
    }

    @Override
    void processAltMealOptions(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
         altMealOptions.addAll(processList(value, lineNumber));
    }

    @Override
    void processAltGroceryOptions(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
        altGroceryOptions.addAll(processList(value, lineNumber));
    }

    @Override
    List<String> processStartTimes(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
        return processList(value, lineNumber);
    }

    @Override
    List<String> processPickupManagers(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
        return processList(value, lineNumber);
    }


    public List<String> getAltMealOptions() {
         return altMealOptions;
    }

    public List<String> getAltGroceryOptions() {
         return altGroceryOptions;
    }

    private List<String> processList(String value, long lineNumber) {
        // FIX THIS, DS: is there auditing to do here?
         return Arrays.asList(value.split("\\s*,\\s*"));
    }
}