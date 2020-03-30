/******************************************************************************
 * Copyright (c) 2020 helpberkeley.org
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
 ******************************************************************************/

package org.helpberkeley.memberdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tables {

    private final List<User> users;

    Tables(List<User> users) {
        this.users = users;
    }

    List<User> sortByName() {
        List<User> sorted = new ArrayList<>(users);

        sorted.sort(Comparator.comparing(User::getName));
        return sorted;
    }

    List<User> sortByUserName() {
        List<User> sorted = new ArrayList<>(users);
        sorted.sort(Comparator.comparing(User::getUserName));
        return sorted;
    }

    List<User> sortByPhoneNumber() {
        List<User> sorted = new ArrayList<>(users);
        sorted.sort(Comparator.comparing(User::getPhoneNumber));
        return sorted;
    }

    List<User> sortByNeighborHoodThenName() {
        List<User> sorted = new ArrayList<>(users);

        Comparator<User> comparator = Comparator
                .comparing(User::getNeighborhood)
                .thenComparing(User::getName);

        sorted.sort(comparator);
        return sorted;
    }

    List<User> sortByNeighborThenByAddress() {
        List<User> sorted = new ArrayList<>(users);

        Comparator<User> comparator = Comparator
                .comparing(User::getNeighborhood)
                .thenComparing(User::getAddress);

        sorted.sort(comparator);
        return sorted;
    }
}
