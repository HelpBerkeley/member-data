//
// Copyright (c) 2020-2021 helpberkeley.org
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.helpberkeley.memberdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Group
 */
public class Group {

    static final String NAME_FIELD = "name";
    static final String USERS_FIELD = "users";

    private static final Logger LOGGER = LoggerFactory.getLogger(Group.class);

    final String name;
    private final Set<Long> userIds = new HashSet<>();
    private final Set<Long> ownerIds = new HashSet<>();

    Group(final String name) {

        this.name = name;
    }

    void addUserIDs(List<Long> userIDs) {
        this.userIds.addAll(userIDs);
    }

    void addUser(Long userId) {
        userIds.add(userId);
    }

    void addOwner(Long ownerId) {
        ownerIds.add(ownerId);
    }

    boolean hasMember(long userId) {
        return userIds.contains(userId);
    }

    boolean hasOwner(long userId) {
        return ownerIds.contains(userId);
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

    static boolean supportedGroup(Long groupId, final String groupName) {

        if ((groupId == null) || (groupName == null)) {
            LOGGER.trace("getGroupNames skipping id: {}, groupName: {}", groupId, groupName);
            return false;
        }

        switch (groupName) {
            case Constants.GROUP_CONSUMERS:
            case Constants.GROUP_DRIVERS:
            case Constants.GROUP_DISPATCHERS:
            case Constants.GROUP_SPECIALISTS:
            case Constants.GROUP_BHS:
            case Constants.GROUP_HELPLINE:
            case Constants.GROUP_SITELINE:
            case Constants.GROUP_TRAINED_CUSTOMER_CARE_A:
            case Constants.GROUP_TRAINED_CUSTOMER_CARE_B:
            case Constants.GROUP_INREACH:
            case Constants.GROUP_OUTREACH:
            case Constants.GROUP_MARKETING:
            case Constants.GROUP_MODERATORS:
            case Constants.GROUP_WORKFLOW:
            case Constants.GROUP_VOICEONLY:
            case Constants.GROUP_TRUST_LEVEL_4:
            case Constants.GROUP_CUSTOMER_INFO:
            case Constants.GROUP_ADVISOR:
            case Constants.GROUP_COORDINATORS:
            case Constants.GROUP_ADMIN:
            case Constants.GROUP_PACKERS:
            case Constants.GROUP_BOARDMEMBERS:
            case Constants.GROUP_LIMITED:
            case Constants.GROUP_AT_RISK:
            case Constants.GROUP_BIKERS:
            case Constants.GROUP_OUT:
            case Constants.GROUP_TRAINED_DRIVERS:
            case Constants.GROUP_EVENT_DRIVERS:
            case Constants.GROUP_TRAINED_EVENT_DRIVERS:
            case Constants.GROUP_GONE:
            case Constants.GROUP_OTHER_DRIVERS:
            case Constants.GROUP_FRREG:
                return true;
            default:
                return false;
        }
    }
}
