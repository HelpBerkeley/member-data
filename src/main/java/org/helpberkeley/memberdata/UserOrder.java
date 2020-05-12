/*
 * Copyright (c) 2020. helpberkeley.org
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

public class UserOrder {
    final String name;
    final String userName;
    final String deliveryFile;

    UserOrder(final String name, final String userName, final String deliveryFile) {
        this.name = name;
        this.userName = userName;
        this.deliveryFile = deliveryFile;
    }

    @Override
    public String toString() {
        return "Name=" + this.name + Constants.CSV_SEPARATOR
                + "User Name=" + this.userName + Constants.CSV_SEPARATOR
                + "Delivery File=" + deliveryFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserOrder)) {
            return false;
        }

        UserOrder otherOrder = (UserOrder)obj;

        if (! name.equals(otherOrder.name)) {
            return false;
        }

        if (! userName.equals(otherOrder. userName)) {
            return false;
        }

        if (! deliveryFile.equals(otherOrder. deliveryFile)) {
            return false;
        }

        return true;
    }

}
