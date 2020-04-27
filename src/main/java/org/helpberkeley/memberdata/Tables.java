//
// Copyright (c) 2020 helpberkeley.org
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class Tables {

    private final List<User> users;

    Tables(List<User> users) {
        this.users = users;
    }

//    List<User> sortByName() {
//        List<User> sorted = new ArrayList<>(users);
//
//        sorted.sort(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER));
//        return sorted;
//    }

    List<User> sortByUserName() {
        List<User> sorted = new ArrayList<>(users);
        sorted.sort(Comparator.comparing(User::getUserName, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    List<User> sortByCreateTime() {
        List<User> sorted = new ArrayList<>(users);
        sorted.sort(Comparator.comparing(User::getCreateTime));

        return sorted;
    }

    List<User> sortByConsumerThenDriverThenName() {
        List<User> sorted = new ArrayList<>(users);


//        Comparator<User> comparator = Comparator
//                .comparing(User::isConsumer)
//                .thenComparing(User::isDriver)
//                .thenComparing(User::getName, String.CASE_INSENSITIVE_ORDER);
//
//        sorted.sort(comparator);

        Collections.sort(sorted, new IsConsumerComparator()
            .thenComparing(new IsDriverComparator()
            .thenComparing(new NameComparator())));

        return sorted;
    }

//    List<User> sortByPhoneNumber() {
//        List<User> sorted = new ArrayList<>(users);
//        sorted.sort(Comparator.comparing(User::getPhoneNumber));
//        return sorted;
//    }
//
//    List<User> sortByNeighborHoodThenName() {
//        List<User> sorted = new ArrayList<>(users);
//
//        Comparator<User> comparator = Comparator
//                .comparing(User::getNeighborhood, String.CASE_INSENSITIVE_ORDER)
//                .thenComparing(User::getName, String.CASE_INSENSITIVE_ORDER);
//
//        sorted.sort(comparator);
//        return sorted;
//    }
//
//    List<User> sortByNeighborThenByAddress() {
//        List<User> sorted = new ArrayList<>(users);
//
//        Comparator<User> comparator = Comparator
//                .comparing(User::getNeighborhood, String.CASE_INSENSITIVE_ORDER)
//                .thenComparing(User::getAddress);
//
//        sorted.sort(comparator);
//        return sorted;
//    }


    /**
     * Get a list of members not in groups (consumer, dispatcher, driver)
     * @return List of recent non-group members.
     */
    List<User> memberOfNoGroups() {
        List<User> noGroups = new ArrayList<>();

        for (User user : sortByUserName()) {
            if (user.isConsumer() || user.isDispatcher() || user.isDriver() || user.isSpecialist()) {
                continue;
            }

            noGroups.add(user);
        }

        return noGroups;
    }

    /**
     * Get a list of members created in the last N days
     * @return List of recently created members.
     */
    List<User> recentlyCreated(int days) {
        List<User> recentMembers = new ArrayList<>();

        ZonedDateTime threeDaysAgo = ZonedDateTime.now().minus(days, DAYS);

        for (User user : sortByUserName()) {

            ZonedDateTime createdAt = ZonedDateTime.parse(user.getCreateTime());

            if (createdAt.compareTo(threeDaysAgo) >= 0) {
                recentMembers.add(user);
            }
        }

        return recentMembers;
    }
    /**
     * Get a list of members, not in groups, created in the last three days
     * @return List of recent non-group members.
     */
    List<User> supportedDeliveryCity() {
        List<User> supportedCityList = new ArrayList<>();

        ZonedDateTime threeDaysAgo = ZonedDateTime.now().minus(3, DAYS);

        for (User user : sortByUserName()) {
            if (user.isSupportedCity()) {
                supportedCityList.add(user);
            }
        }

        return supportedCityList;
    }

    /**
     * Get a list of members, who have the consumer request field set
     * @return List of recent members who want meals
     */
    List<User> consumerRequests() {
        List<User> consumerRequests = new ArrayList<>();

        for (User user : sortByUserName()) {
            if (user.hasConsumerRequest() && (! user.isConsumer())) {
                consumerRequests.add(user);
            }
        }

        return consumerRequests;
    }

    /**
     * Get a list of members, who have the volunteer request field set
     * but are not dispatchers or drivers
     * @return List of recent members who want to volunteer
     */
    List<User> volunteerRequests() {
        List<User> volunteerRequests = new ArrayList<>();

        for (User user : sortByUserName()) {
            if (user.getVolunteerRequest().equals(User.NOT_PROVIDED)) {
                continue;
            }

            if ((! user.isDriver()) && (!user.isDispatcher()) && (! user.isSpecialist())) {
                volunteerRequests.add(user);
            }
        }

        return volunteerRequests;
    }

    /**
     * Get a list of drivers
     * @return List of drivers.
     */
    List<User> drivers() {
        List<User> drivers = new ArrayList<>();

        for (User user : sortByUserName()) {
            if (user.isDriver()) {
                drivers.add(user);
            }
        }

        return drivers;
    }

    /**
     * Return a combined list of:
     *   - consumers
     *   - members that are not consumers, drivers, or dispachers
     * Primary sort first by consumer or not, secondary sort by create date
     * @return
     */
    List<User> inreach() {

        List<User> inreach = new ArrayList<>();

        for (User user : users) {
            if (user.isConsumer()) {
                inreach.add(user);
            } else if (! (user.isDriver() || user.isDispatcher())) {
                inreach.add(user);
            }
        }

        Collections.sort(inreach, new IsConsumerComparator()
                .thenComparing(Comparator.comparing(User::getCreateTime)));

        return inreach;
    }

    static class IsConsumerComparator implements Comparator<User> {

        @Override
        public int compare(User u1, User u2) {

            return u2.isConsumer().compareTo(u1.isConsumer());
        }
    }

    static class IsDriverComparator implements Comparator<User> {

        @Override
        public int compare(User u1, User u2) {

            return u2.isDriver().compareTo(u1.isDriver());
        }
    }

    static class NameComparator implements Comparator<User> {

        @Override
        public int compare(User u1, User u2) {

            return u1.getName().compareToIgnoreCase(u2.getName());
        }
    }
}
