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

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

public class TablesTest extends TestBase {

    @Test
    public void nonConsumersTest() throws UserException {

        User userNow = createUserWithCreateTime(ZonedDateTime.now().toString());
        User userFourDaysAgo = createUserWithCreateTime(ZonedDateTime.now().minus(4, DAYS).toString());
        User userFourHoursAgo = createUserWithCreateTime(ZonedDateTime.now().minus(4, HOURS).toString());
        User userAlmostThreeDaysAgo = createUserWithCreateTime(
                ZonedDateTime.now().minus(3, DAYS).plus(1, MINUTES).toString());

        List<User> users = List.of(userNow, userFourDaysAgo, userFourHoursAgo, userAlmostThreeDaysAgo);
        Tables tables = new Tables(users);

        List<User> nonConsumers = tables.nonConsumers();
        assertThat(nonConsumers).containsExactlyInAnyOrder(userNow, userFourHoursAgo, userAlmostThreeDaysAgo);
    }

    @Test
    public void noGroupsTest() throws UserException {

        User userNow = createUserWithCreateTime(ZonedDateTime.now().toString());
        User userFourDaysAgo = createUserWithCreateTime(ZonedDateTime.now().minus(4, DAYS).toString());
        User userFourHoursAgo = createUserWithCreateTime(ZonedDateTime.now().minus(4, HOURS).toString());
        User userAlmostThreeDaysAgo = createUserWithCreateTime(
                ZonedDateTime.now().minus(3, DAYS).plus(1, MINUTES).toString());
        User userDriver = createUserWithCreateTimeAndGroup(
                ZonedDateTime.now().minus(3, DAYS).plus(1, MINUTES).toString(),
                Constants.GROUP_DISPATCHERS);

        List<User> users = List.of(
                userNow, userFourDaysAgo, userFourHoursAgo, userAlmostThreeDaysAgo, userDriver);
        Tables tables = new Tables(users);

        List<User> noGroups = tables.noGroups();
        assertThat(noGroups).containsExactlyInAnyOrder(userNow, userFourHoursAgo, userAlmostThreeDaysAgo);
    }
}
