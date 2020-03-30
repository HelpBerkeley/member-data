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


    // FIX THIS, DS: Hardwired names that break if Discourse
    //               changes any them. Unlikely though.
    //
    static final String NAME_FIELD = "name";
    static final String DISPLAY_NAME_FIELD = "display_name";
    static final String MEMBERS_FIELD = "members";

    // Group names - FIX THIS, DS: hardwired
    static final String CONSUMER = "consumers";
    static final String DRIVER = "drivers";
    static final String DISPATCHER = "dispatchers";

    final String name;
    private final String displayName;
    private final Set<String> userNames = new HashSet<>();

    private Group(
        final String name,
        final String displayName) {

        this.name = name;
        this.displayName = displayName;
    }

    void addMembers(List<String> userNames) {

        for (String userName : userNames) {
            this.userNames.add(userName);
        }
    }

    boolean hasUserName(final String userName) {
        return userNames.contains(userName);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(NAME_FIELD);
        builder.append("=");
        builder.append(name);
        builder.append(':');

        builder.append(DISPLAY_NAME_FIELD);
        builder.append("=");
        builder.append(displayName);
        builder.append(':');

        builder.append(MEMBERS_FIELD);
        builder.append("=");
        for (String userName : userNames) {
            builder.append(userName);
            builder.append(',');
        }

        return builder.toString();
    }

    static Group createGroup(Map<String, Object> fieldMap) {

        String name = (String) fieldMap.get(NAME_FIELD);
        String displayName = (String) fieldMap.get(DISPLAY_NAME_FIELD);

        return new Group(name, displayName);
    }

    // CTOR for tests
    //
    static Group createGroup(final String name, List<String> members) {

        Group group = new Group(name, name);
        group.addMembers(members);

        return group;
    }
}
