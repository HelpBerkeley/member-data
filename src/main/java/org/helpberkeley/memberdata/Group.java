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

import java.util.*;

/**
 * Group
 */
public class Group {

    static final String NAME_FIELD = "name";
    static final String USERS_FIELD = "users";

    final String name;
    private final Set<Long> userIds = new HashSet<>();

    private Group(final String name) {

        this.name = name;
    }

    void addUserIDs(List<Long> userIDs) {
        userIDs.forEach(this.userIds::add);
    }

    boolean hasUserId(final long userId) {
        return userIds.contains(userId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(NAME_FIELD);
        builder.append("=");
        builder.append(name);
        builder.append(':');

        builder.append(USERS_FIELD);
        builder.append("=");
        for (long userId : userIds) {
            builder.append(userId);
            builder.append(',');
        }

        return builder.toString();
    }

    static Group createGroup(final String name, List<Long> userIDs) {
        Group group = new Group(name);
        group.addUserIDs(userIDs);

        return group;
    }
}
