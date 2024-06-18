//
// Copyright (c) 2020-2024 helpberkeley.org
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
import java.util.*;

import static java.util.Comparator.reverseOrder;

public class Tables {

    private final List<User> users;

    public Tables(List<User> users) {
        this.users = users;
    }

    public Map<String, User> mapByUserName() {

        Map<String, User> userMap = new HashMap<>();
        for (User user : users) {
            assert ! userMap.containsKey(user.getUserName()) : user.getUserName();
            userMap.put(user.getUserName(), user);
        }

        return userMap;
    }

    List<User> sortByUserName() {
        List<User> sorted = new ArrayList<>(users);
        sorted.sort(Comparator.comparing(User::getUserName, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    List<User> sortByUserId() {
        List<User> sorted = new ArrayList<>(users);
        sorted.sort(Comparator.comparing(User::getId));
        return sorted;
    }

    List<User> sortByCreateTime() {
        List<User> sorted = new ArrayList<>(users);
        sorted.sort(Comparator.comparing(User::getCreateTime));

        return sorted;
    }

    List<User> sortByConsumerThenDriverThenName() {
        List<User> sorted = new ArrayList<>(users);

        sorted.sort(new IsConsumerComparator()
                .thenComparing(new IsDriverComparator()
                .thenComparing(new NameComparator())));

        return sorted;
    }

    /**
     * Get a list of members not in groups (consumer, dispatcher, driver)
     * @return List of recent non-group members.
     */
    List<User> memberOfNoGroups() {
        List<User> noGroups = new ArrayList<>();

        for (User user : sortByUserName()) {
            if (user.isConsumer() || user.isDispatcher()
                    || user.isDriver() || user.isSpecialist() || user.isLogistics()) {
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

        ZonedDateTime threeDaysAgo = ZonedDateTime.now().minusDays(days);

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

            if (user.isGone()) {
                continue;
            }

            if ((!user.isDriver())
                    && (!user.isDispatcher())
                    && (!user.isSpecialist())
                    && (!user.isLogistics())
                    && (!user.isEVolunteers())) {
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

        for (User user : users) {
            if (user.isDriver() && (! user.groupOwner(Constants.GROUP_HELPLINE))) {
                drivers.add(user);
            }
        }

        applyDriverCorrections(drivers);

        drivers.sort(Comparator.comparing(User::isTrainedDriver, reverseOrder())
                .thenComparing(User::isGone)
                .thenComparing(User::isOut)
                .thenComparing(User::isOtherDrivers)
                .thenComparing(User::isEventDriver)
                .thenComparing(User::isLimitedRuns)
                .thenComparing(User::isBiker)
                .thenComparing(User::isAtRisk)
                .thenComparing(User::getSimpleCreateTime, reverseOrder()));

        return drivers;
    }

    /**
     * Remove drivers from the groups which they own.
     */
    private void applyDriverCorrections(final List<User> drivers) {

        List<String> groupsToCorrect = List.of(
                Constants.GROUP_GONE,
                Constants.GROUP_OUT,
                Constants.GROUP_OTHER_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS,
                Constants.GROUP_LIMITED,
                Constants.GROUP_AT_RISK,
                Constants.GROUP_TRAINED_EVENT_DRIVERS);

        for (User driver : drivers) {
            for (String group : groupsToCorrect) {
                if (driver.groupOwner(group)) {
                    driver.leaveGroup(group);
                }
            }
        }
    }

    /**
     * Get a list of available drivers
     * @return List of drivers.
     */
    List<User> availableDrivers() {
        List<User> drivers = new ArrayList<>();

        for (User user : users) {
            if (user.isAvailableDriver() && user.isTrainedDriver()) {
                drivers.add(user);
            }
        }
        return drivers;
    }

    /**
     * Get a list of available event drivers
     * @return List of drivers.
     */
    List<User> availableEventDrivers() {
        List<User> drivers = new ArrayList<>();

        for (User user : users) {
            if ((user.isAvailableDriver() && user.isTrainedDriver())
                    || (user.isAvailableEventDriver() && user.isTrainedEventDriver())) {
                drivers.add(user);
            }
        }
        return drivers;
    }

    /**
     * Get a list of trained but out (not gone) drivers and event drivers
     * @return List of drivers.
     */
    List<User> outDrivers() {
        List<User> drivers = new ArrayList<>();

        for (User user : users) {

            if (user.isGone()) {
                continue;
            }

            if (user.isOut() &&
                ((user.isDriver() && user.isTrainedDriver())
                || (user.isEventDriver() && user.isTrainedEventDriver()))) {
                    drivers.add(user);
            }
        }

        return drivers;
    }

    /**
     * Get a list of dispatchers, sorted by create time
     * @return List of dispatchers
     */
    List<User> dispatchers() {
        List<User> dispatchers = new ArrayList<>();

        for (User user : users) {
            if (user.isDispatcher()) {
                dispatchers.add(user);
            }
        }

        dispatchers.sort(Comparator.comparing(User::getCreateTime));
        return dispatchers;
    }

    /**
     * Return a combined list of:
     *   - consumers
     *   - members that are not consumers, drivers, or dispatchers
     * Primary sort first by consumer or not, secondary sort by create date
     * @return List of inreach users
     */
    List<User> inreach() {

        List<User> inreach = new ArrayList<>();

        for (User user : users) {
            if (user.isConsumer()) {
                inreach.add(user);
            } else if (! (user.isDriver() || user.isDispatcher() || user.isLogistics())) {
                inreach.add(user);
            }
        }

        inreach.sort(new IsConsumerComparator()
                .thenComparing(User::getCreateTime));

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
