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

import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TablesTest extends TestBase {

    @Test
    public void noGroupsTest() throws UserException {

        User u1 = createUser();
        User u2 = createUserWithGroup(Constants.GROUP_DISPATCHERS);
        User u3 = createUserWithGroup(Constants.GROUP_CONSUMERS);
        User u4 = createUserWithGroup(Constants.GROUP_DRIVERS);
        User u5 = createUserWithGroup(Constants.GROUP_SPECIALISTS);
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

        User userNow = createUserWithCreateTime(now.toString());
        User userFourDaysAgo = createUserWithCreateTime(now.minus(4, DAYS).toString());
        User userFourHoursAgo = createUserWithCreateTime(now.minus(4, HOURS).toString());
        // Add a minute of slop because the table is going to recalculate "now"
        User userThreeDaysAgo = createUserWithCreateTime(
                now.minus(3, DAYS).plus(1, MINUTES).toString());

        List<User> users = List.of(userNow, userFourDaysAgo, userFourHoursAgo, userThreeDaysAgo);
        Tables tables = new Tables(users);

        List<User> recent = tables.recentlyCreated(3);
        assertThat(recent).containsExactlyInAnyOrder(userNow, userFourHoursAgo, userThreeDaysAgo);
    }

    @Test
    public void sortByCreateTimeTest() throws UserException {

        ZonedDateTime now = ZonedDateTime.now();

        User userNow = createUserWithCreateTime(now.toString());
        User userEightSecondsAgo = createUserWithCreateTime(now.minus(8, SECONDS).toString());
        User userNineSecondsAgo = createUserWithCreateTime(now.minus(9, SECONDS).toString());
        User userTomorrow = createUserWithCreateTime(now.plus(1, DAYS).toString());

        List<User> users = List.of(userNow, userNineSecondsAgo, userTomorrow, userEightSecondsAgo);
        Tables tables = new Tables(users);

        assertThat(tables.sortByCreateTime()).containsExactly(
                userNineSecondsAgo, userEightSecondsAgo, userNow, userTomorrow);
    }

    @Test
    public void consumerRequestsTest() throws UserException {

        User user1 = createUserWithUserNameAndConsumerRequest("u1");
        User user2 = createUserWithUserName("u3");
        User user3 = createUserWithUserNameAndConsumerRequest("u3");
        User user4 = createUserWithUserName("u4");

        Tables tables = new Tables(List.of(user1, user2, user3, user4));
        assertThat(tables.consumerRequests()).containsExactlyInAnyOrder(user1, user3);
    }

    @Test
    public void consumerRequestsInConsumerGroupTest() throws UserException {

        User user1 = createUserWithUserNameAndConsumerRequestAndConsumerGroup("u1");
        User user2 = createUserWithUserNameAndConsumerRequest("u2");
        User user3 = createUserWithUserNameAndConsumerRequestAndConsumerGroup("u3");
        User user4 = createUserWithUserNameAndConsumerRequest("u4");

        Tables tables = new Tables(List.of(user1, user2, user3, user4));
        assertThat(tables.consumerRequests()).containsExactlyInAnyOrder(user2, user4);
    }

    @Test
    public void volunteerRequestTest() throws UserException {

        User u1 = createTestUser1();
        User u2 = createTestUser2();
        User u3 = createTestUser3();
        // Simulates someone un-setting their initial volunteer request after signing up.
        User u4 = createUserWithVolunteerRequest("");

        Tables tables = new Tables(List.of(u1, u2, u3));
        assertThat(tables.volunteerRequests()).containsExactlyInAnyOrder(u1);
    }

    @Test
    public void driversTest() throws UserException {
        User u1 = createUser();
        User u2 = createUserWithGroup(Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup(Constants.GROUP_DRIVERS);
        User u4 = createUserWithName("fred");

        Tables tables = new Tables(List.of(u1, u2, u3, u4));
        assertThat(tables.drivers()).containsExactlyInAnyOrder(u2, u3);
    }
}
