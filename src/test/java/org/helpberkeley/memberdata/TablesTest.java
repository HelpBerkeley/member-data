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

import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TablesTest extends TestBase {

    @Test
    public void noGroupsTest() throws UserException {

        User u1 = createUser();
        User u2 = createUserWithGroup("u1", Constants.GROUP_DISPATCHERS);
        User u3 = createUserWithGroup("u2", Constants.GROUP_CONSUMERS);
        User u4 = createUserWithGroup("u3", Constants.GROUP_DRIVERS);
        User u5 = createUserWithGroup("u4", Constants.GROUP_SPECIALISTS);
        User u6 = createUserWithCity(Constants.ALBANY);

        List<User> users = List.of(u1, u2, u3, u4, u5, u6);
        Tables tables = new Tables(users);

        List<User> noGroups = tables.memberOfNoGroups();
        assertThat(noGroups).containsExactlyInAnyOrder(u1, u6);
    }

    @Test
    public void supportedCityTest() throws UserException {

        User berkeley = createUserWithCity(Constants.BERKELEY);
        User albany = createUserWithCity(Constants.ALBANY);
        User kensington = createUserWithCity(Constants.KENSINGTON);
        User poughkeepise = createUserWithCity("Poughkeepise");

        List<User> users = List.of(berkeley, albany, kensington, poughkeepise);
        Tables tables = new Tables(users);

        List<User> noGroups = tables.supportedDeliveryCity();
        assertThat(noGroups).containsExactlyInAnyOrder(berkeley, albany, kensington);
    }

    @Test
    public void recentlyCreatedTest() throws UserException {

        ZonedDateTime now = ZonedDateTime.now();

        User userNow = createUserWithCreateTime("now", now.toString());
        User userFourDaysAgo = createUserWithCreateTime("4DaysAgo", now.minus(4, DAYS).toString());
        User userFourHoursAgo = createUserWithCreateTime("4HoursAgo", now.minus(4, HOURS).toString());
        // Add a minute of slop because the table is going to recalculate "now"
        User userThreeDaysAgo = createUserWithCreateTime("3DaysAgo",
                now.minus(3, DAYS).plus(1, MINUTES).toString());

        List<User> users = List.of(userNow, userFourDaysAgo, userFourHoursAgo, userThreeDaysAgo);
        Tables tables = new Tables(users);

        List<User> recent = tables.recentlyCreated(3);
        assertThat(recent).containsExactlyInAnyOrder(userNow, userFourHoursAgo, userThreeDaysAgo);
    }

    @Test
    public void sortByCreateTimeTest() throws UserException {

        ZonedDateTime now = ZonedDateTime.now();

        User userNow = createUserWithCreateTime("now", now.toString());
        User userEightSecondsAgo = createUserWithCreateTime("8secsAgo", now.minus(8, SECONDS).toString());
        User userNineSecondsAgo = createUserWithCreateTime("9secsAgo", now.minus(9, SECONDS).toString());
        User userTomorrow = createUserWithCreateTime("tomorrow", now.plus(1, DAYS).toString());

        List<User> users = List.of(userNow, userNineSecondsAgo, userTomorrow, userEightSecondsAgo);
        Tables tables = new Tables(users);

        assertThat(tables.sortByCreateTime()).containsExactly(
                userNineSecondsAgo, userEightSecondsAgo, userNow, userTomorrow);
    }

    @Test
    public void consumerRequestsTest() throws UserException {

        User user1 = createUserWithConsumerRequest("u1");
        User user2 = createUserWithUserName("u3");
        User user3 = createUserWithConsumerRequest("u3");
        User user4 = createUserWithUserName("u4");

        Tables tables = new Tables(List.of(user1, user2, user3, user4));
        assertThat(tables.consumerRequests()).containsExactlyInAnyOrder(user1, user3);
    }

    @Test
    public void consumerRequestsInConsumerGroupTest() throws UserException {

        User user1 = createUserWithConsumerRequestAndConsumerGroup("u1");
        User user2 = createUserWithConsumerRequest("u2");
        User user3 = createUserWithConsumerRequestAndConsumerGroup("u3");
        User user4 = createUserWithConsumerRequest("u4");

        Tables tables = new Tables(List.of(user1, user2, user3, user4));
        assertThat(tables.consumerRequests()).containsExactlyInAnyOrder(user2, user4);
    }

    @Test
    public void volunteerRequestTest() throws UserException {

        User u1 = createTestUser1();
        User u2 = createTestUser2();
        User u3 = createTestUser3();
        // Simulates someone un-setting their initial volunteer request after signing up.
        User u4 = createUserWithVolunteerRequest("u4", "");
        User u5 = createUserWithNoRequestsNoGroups("u5");

        Tables tables = new Tables(List.of(u1, u2, u3, u4, u5));
        assertThat(tables.volunteerRequests()).containsExactlyInAnyOrder(u1);
    }

    @Test
    public void driversTest() throws UserException {
        User u1 = createUser();
        User u2 = createUserWithGroup("u2", Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup("u3", Constants.GROUP_DRIVERS);
        User u4 = createUserWithName("fred");

        Tables tables = new Tables(List.of(u1, u2, u3, u4));
        assertThat(tables.drivers()).containsExactlyInAnyOrder(u2, u3);
    }

    @Test
    public void driversHelpLineGroupOwnerTest() throws UserException {
        User u1 = createUserWithGroupAndGroupOwner("u1", Constants.GROUP_DRIVERS, Constants.GROUP_HELPLINE);
        User u2 = createUserWithGroup("u1", Constants.GROUP_DRIVERS);

        Tables tables = new Tables(List.of(u1, u2));
        assertThat(tables.sortByUserId()).containsExactlyInAnyOrder(u1, u2);
        assertThat(tables.drivers()).containsExactly(u2);
    }

    @Test
    public void driversGoneGroupOnwerTest() throws UserException {
        User u1 = createUserWithGroupsAndGroupsOwned("u1",
                List.of(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS, Constants.GROUP_GONE),
                List.of(Constants.GROUP_GONE));
        assertThat(u1.isAvailableDriver()).isFalse();
        assertThat(u1.isGone()).isTrue();

        Tables tables = new Tables(List.of(u1));
        assertThat(tables.drivers()).containsExactly(u1);
        assertThat(tables.drivers()).hasSize(1);
        User driver = tables.drivers().get(0);
        assertThat(driver.groupOwner(Constants.GROUP_GONE)).isTrue();
        assertThat(driver.isGone()).isFalse();
        assertThat(u1.isAvailableDriver()).isTrue();
    }

    @Test
    public void driversOutGroupOwnerTest() throws UserException {
        User u1 = createUserWithGroupsAndGroupsOwned("u1",
                List.of(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS, Constants.GROUP_OUT),
                List.of(Constants.GROUP_OUT));
        assertThat(u1.isAvailableDriver()).isFalse();
        assertThat(u1.isOut()).isTrue();

        Tables tables = new Tables(List.of(u1));
        assertThat(tables.drivers()).containsExactly(u1);
        assertThat(tables.drivers()).hasSize(1);
        User driver = tables.drivers().get(0);
        assertThat(driver.groupOwner(Constants.GROUP_OUT)).isTrue();
        assertThat(driver.isOut()).isFalse();
        assertThat(u1.isAvailableDriver()).isTrue();
    }

    @Test
    public void driversOtherDriversGroupOwnerTest() throws UserException {
        User u1 = createUserWithGroupsAndGroupsOwned("u1",
                List.of(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS, Constants.GROUP_OTHER_DRIVERS),
                List.of(Constants.GROUP_OTHER_DRIVERS));
        assertThat(u1.isAvailableDriver()).isFalse();
        assertThat(u1.isOtherDrivers()).isTrue();

        Tables tables = new Tables(List.of(u1));
        assertThat(tables.drivers()).containsExactly(u1);
        assertThat(tables.drivers()).hasSize(1);
        User driver = tables.drivers().get(0);
        assertThat(driver.groupOwner(Constants.GROUP_OTHER_DRIVERS)).isTrue();
        assertThat(driver.isOtherDrivers()).isFalse();
        assertThat(u1.isAvailableDriver()).isTrue();
    }

    @Test
    public void driversEventDriverGroupOwnerTest() throws UserException {
        User u1 = createUserWithGroupsAndGroupsOwned("u1",
                List.of(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS,
                        Constants.GROUP_TRAINED_EVENT_DRIVERS, Constants.GROUP_EVENT_DRIVERS),
                List.of(Constants.GROUP_EVENT_DRIVERS));
        assertThat(u1.isAvailableDriver()).isTrue();
        assertThat(u1.isEventDriver()).isTrue();

        Tables tables = new Tables(List.of(u1));
        assertThat(tables.drivers()).containsExactly(u1);
        assertThat(tables.drivers()).hasSize(1);
        User driver = tables.drivers().get(0);
        assertThat(driver.groupOwner(Constants.GROUP_EVENT_DRIVERS)).isTrue();
        assertThat(u1.isAvailableDriver()).isTrue();
        assertThat(u1.isEventDriver()).isFalse();
    }

    @Test
    public void driversLimitedRunsGroupOwnerTest() throws UserException {
        User u1 = createUserWithGroupsAndGroupsOwned("u1",
                List.of(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS, Constants.GROUP_LIMITED),
                List.of(Constants.GROUP_LIMITED));
        assertThat(u1.isAvailableDriver()).isTrue();

        Tables tables = new Tables(List.of(u1));
        assertThat(tables.drivers()).containsExactly(u1);
        assertThat(tables.drivers()).hasSize(1);
        User driver = tables.drivers().get(0);
        assertThat(driver.groupOwner(Constants.GROUP_LIMITED)).isTrue();
        assertThat(driver.isLimitedRuns()).isFalse();
        assertThat(u1.isAvailableDriver()).isTrue();
    }

    @Test
    public void driversAtRiskGroupOwnerTest() throws UserException {
        User u1 = createUserWithGroupsAndGroupsOwned("u1",
                List.of(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS, Constants.GROUP_AT_RISK),
                List.of(Constants.GROUP_AT_RISK));
        assertThat(u1.isAvailableDriver()).isTrue();

        Tables tables = new Tables(List.of(u1));
        assertThat(tables.drivers()).containsExactly(u1);
        assertThat(tables.drivers()).hasSize(1);
        User driver = tables.drivers().get(0);
        assertThat(driver.groupOwner(Constants.GROUP_AT_RISK)).isTrue();
        assertThat(driver.isAtRisk()).isFalse();
        assertThat(u1.isAvailableDriver()).isTrue();
    }

    @Test
    public void driversTrainedEventDriverGroupOwnerTest() throws UserException {
        User u1 = createUserWithGroupsAndGroupsOwned("u1",
                List.of(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS),
                List.of(Constants.GROUP_TRAINED_EVENT_DRIVERS));
        assertThat(u1.isAvailableDriver()).isTrue();

        Tables tables = new Tables(List.of(u1));
        assertThat(tables.drivers()).containsExactly(u1);
        assertThat(tables.drivers()).hasSize(1);
        User driver = tables.drivers().get(0);
        assertThat(driver.groupOwner(Constants.GROUP_TRAINED_EVENT_DRIVERS)).isTrue();
        assertThat(driver.isTrainedEventDriver()).isFalse();
        assertThat(u1.isAvailableDriver()).isTrue();
    }

    @Test
    public void consumerDriverNameTest() throws UserException {

        // create non consumer, non driver 1
        User u1 = createUserWithNameConsumerAndDriver("User 1", false, false);
        // create non consumer, non driver 2
        User u2 = createUserWithNameConsumerAndDriver("User 2", false, false);
        // create non consumer, driver 1
        User u3 = createUserWithNameConsumerAndDriver("User 3", false, true);
        // create non consumer, driver 2
        User u4 = createUserWithNameConsumerAndDriver("User 4", false, true);
        // create consumer, non driver 1
        User u5 = createUserWithNameConsumerAndDriver("User 5", true, false);
        // create consumer, non driver 2
        User u6 = createUserWithNameConsumerAndDriver("User 6", true, false);
        // create consumer, driver 1
        User u7 = createUserWithNameConsumerAndDriver("User 7", true, true);
        // create consumer, driver 2
        User u8 = createUserWithNameConsumerAndDriver("User 8", true, true);

        List<User> users = List.of(u1, u2, u3, u4, u5, u6, u7, u8);

        Tables tables = new Tables(users);

        List<User> sorted = tables.sortByConsumerThenDriverThenName();
        assertThat(sorted).hasSameSizeAs(users);

        // Expected order
        List<User> expected = List.of(u7, u8, u5, u6, u3, u4, u1, u2);

        assertThat(sorted).containsExactlyElementsOf(expected);
    }

    @Test
    public void increachTest() throws UserException {
        ZonedDateTime now = ZonedDateTime.now();
        User u1 = createUserWithCreateTime("u1", now.toString());
        User u2 = createUserWithCreateTime("u2", now.minus(1, DAYS).toString());
        User u3 = createUserWithCreateTime("u3", now.plus(1, DAYS).toString());
        User u4 = createUserWithGroup("u4", Constants.GROUP_DRIVERS);
        User u5 = createUserWithGroup("u5", Constants.GROUP_DISPATCHERS);
        User u6 = createUserWithCreateTimeAndGroup("u6", now.toString(), Constants.GROUP_CONSUMERS);
        User u7 = createUserWithCreateTimeAndGroup(
                "u7", now.minus(10, SECONDS).toString(), Constants.GROUP_CONSUMERS);
        User u8 = createUserWithCreateTimeAndGroup(
                "u8", now.plus(10, SECONDS).toString(), Constants.GROUP_CONSUMERS);

        Tables tables = new Tables(List.of(u1, u2, u3, u4, u5, u6, u7, u8));
        List<User> inreach = tables.inreach();
        assertThat(inreach).containsExactly(u7, u6, u8, u2, u1, u3);
    }

    @Test
    public void dispatchersTest() throws UserException {
        ZonedDateTime now = ZonedDateTime.now();

        User u1 = createUserWithCreateTimeAndGroup("u1", now.toString(), Constants.GROUP_DISPATCHERS);
        User u2 = createUserWithGroup("u2", Constants.GROUP_DRIVERS);
        User u3 = createUserWithCreateTimeAndGroup("u3", now.minus(
                1, DAYS).toString(), Constants.GROUP_DISPATCHERS);
        User u4 = createUserWithCreateTimeAndGroup("u4", now.minus(
                1, MINUTES).toString(), Constants.GROUP_DISPATCHERS);
        User u5 = createUserWithGroup("u5", Constants.GROUP_CONSUMERS);
        User u6 = createUserWithCreateTimeAndGroup("u6", now.plus(
                1, MINUTES).toString(), Constants.GROUP_DISPATCHERS);

        Tables tables = new Tables(List.of(u1, u2, u3, u4, u5, u6));
        List<User> dispatchers = tables.dispatchers();
        assertThat(dispatchers).containsExactly(u3, u4, u1, u6);
    }
}
