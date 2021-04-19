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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class DriverV300 extends Driver {

    private final List<DeliveryV300> deliveries = new ArrayList<>();

    DriverV300(List<Delivery> deliveries) {
        for (Delivery delivery : deliveries) {
            assert delivery instanceof DeliveryV300;
            this.deliveries.add((DeliveryV300) delivery);
        }
    }

    @Override
    public List<Delivery> getDeliveries() {
        return Collections.unmodifiableList(deliveries);
    }

    public List<DeliveryV300> getDeliveriesV300() {
        return Collections.unmodifiableList(deliveries);
    }

    @Override
    void resetDeliveries(List<Delivery> deliveries) {
        this.deliveries.clear();
        for (Delivery delivery : deliveries) {
            assert delivery instanceof DeliveryV300;
            this.deliveries.add((DeliveryV300) delivery);
        }
    }

    @Override
    void setStartTime() {
        // FIX THIS, DS: move this abstract out of the base
    }

    @Override
    String getStartTime() {
        // FIX THIS, DS: implement
        return "";
    }
}
