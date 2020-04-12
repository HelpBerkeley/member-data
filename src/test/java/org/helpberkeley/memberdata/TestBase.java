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

import java.time.ZonedDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.*;

public class TestBase {

    static final String TEST_NAME_1 = "name 1";
    static final long TEST_ID_1 = 1;
    static final String TEST_USER_NAME_1 = "user name 1";
    static final String TEST_ADDRESS_1 = "address 1";
    static final String TEST_PHONE_1 = "111-222-3333";
    static final String TEST_NEIGHBORHOOD_1 = "neighborhood 1";
    static final Boolean TEST_APARTMENT_1 = Boolean.FALSE;
    static final String TEST_CREATED_1 = ZonedDateTime.now().minus(6, WEEKS).toString();

    static final String TEST_NAME_2 = "name 2";
    static final long TEST_ID_2 = 2;
    static final String TEST_USER_NAME_2 = "user name 2";
    static final String TEST_ADDRESS_2 = "address 2";
    static final String TEST_PHONE_2 = "222-333-4444";
    static final String TEST_NEIGHBORHOOD_2 = "neighborhood 2";
    static final Boolean TEST_APARTMENT_2 = Boolean.TRUE;
    static final String TEST_CREATED_2 = ZonedDateTime.now().minus(8, MINUTES).toString();

    static final String TEST_NAME_3 = "name 3";
    static final long TEST_ID_3 = 3;
    static final String TEST_USER_NAME_3 = "user name 3";
    static final String TEST_ADDRESS_3 = "address 3";
    static final String TEST_PHONE_3 = "333-444-5555";
    static final String TEST_NEIGHBORHOOD_3 = "neighborhood 3";
    static final Boolean TEST_APARTMENT_3 = Boolean.FALSE;
    static final String TEST_CREATED_3 = ZonedDateTime.now().minus(1, DAYS).toString();

    static final String TEST_NAME_4 = "name 4";
    static final long TEST_ID_4 = 4;
    static final String TEST_USER_NAME_4 = "user name 4";
    static final String TEST_ADDRESS_4 = "address 4";
    static final String TEST_PHONE_4 = "444-444-5555";
    static final String TEST_NEIGHBORHOOD_4 = "neighborhood 4";
    static final Boolean TEST_APARTMENT_4 = Boolean.FALSE;
    static final String TEST_CREATED_4 = ZonedDateTime.now().minus(3, DAYS).toString();

    final List<String>  TEST_USER_1_GROUPS = List.of(Constants.GROUP_CONSUMERS);
    final List<String>  TEST_USER_2_GROUPS = List.of(Constants.GROUP_CONSUMERS, Constants.GROUP_DRIVERS);
    final List<String>  TEST_USER_3_GROUPS = List.of(Constants.GROUP_DRIVERS, Constants.GROUP_DISPATCHERS);

    protected User createTestUser1() throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1, TEST_ID_1, TEST_ADDRESS_1,
                User.BERKELEY, TEST_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_APARTMENT_1, TEST_USER_1_GROUPS);
    }

    protected User createTestUser2() throws UserException {
        return User.createUser(TEST_NAME_2, TEST_USER_NAME_2, TEST_ID_2, TEST_ADDRESS_2,
                User.BERKELEY, TEST_PHONE_2, TEST_NEIGHBORHOOD_2,
                TEST_CREATED_2, TEST_APARTMENT_2, TEST_USER_2_GROUPS);
    }

    protected User createTestUser3() throws UserException {
        return User.createUser(TEST_NAME_3, TEST_USER_NAME_3, TEST_ID_3, TEST_ADDRESS_3,
                User.BERKELEY, TEST_PHONE_3, TEST_NEIGHBORHOOD_3,
                TEST_CREATED_3, TEST_APARTMENT_3, TEST_USER_3_GROUPS);
    }

    // no groups
    protected User createUser() throws UserException {
        return User.createUser(TEST_USER_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1,
                TEST_NEIGHBORHOOD_1, TEST_CREATED_1, TEST_APARTMENT_1);
    }

    protected User createUserWithGroups(final String... groups) throws UserException {
        return User.createUser(TEST_USER_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1,
                TEST_NEIGHBORHOOD_1, TEST_CREATED_1, TEST_APARTMENT_1, groups);
    }

    protected User createUserWithName(final String name) throws UserException {
        return User.createUser(name, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1,
                TEST_NEIGHBORHOOD_1, TEST_CREATED_1, TEST_APARTMENT_1);
    }

    protected User createUserWithUserName(final String userName) throws UserException {
        return User.createUser(TEST_NAME_1, userName,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1,
                TEST_NEIGHBORHOOD_1, TEST_CREATED_1, TEST_APARTMENT_1);
    }

    protected User createUserWithAddress(final String address) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, address, User.BERKELEY, TEST_PHONE_1,
                TEST_NEIGHBORHOOD_1, TEST_CREATED_1, TEST_APARTMENT_1);
    }

    protected User createUserWithCity(final String city) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, city, TEST_PHONE_1,
                TEST_NEIGHBORHOOD_1, TEST_CREATED_1, TEST_APARTMENT_1);
    }

    User createUserWithPhone(final String phone) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, phone,
                TEST_NEIGHBORHOOD_1, TEST_CREATED_1, TEST_APARTMENT_1);
    }

    protected User createUserWithGroup(final String group) throws UserException {
        return User.createUser(TEST_USER_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1,
                TEST_NEIGHBORHOOD_1, TEST_CREATED_1, TEST_APARTMENT_1, group);
    }

    protected User createUserWithNeighborhood(final String neighborhood) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_PHONE_1, User.BERKELEY, TEST_PHONE_1,
                neighborhood, TEST_CREATED_1, TEST_APARTMENT_1);
    }
    protected User createUserWithCityAndNeighborhood(
            final String city, final String neighborhood) throws UserException {

        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_PHONE_1, city, TEST_PHONE_1,
                neighborhood, TEST_CREATED_1, TEST_APARTMENT_1);
    }

    protected User createUserWithGroupAndNeighborhood(
            final String group, final String neighborhood) throws UserException {

        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_PHONE_1, User.BERKELEY, TEST_PHONE_1,
                neighborhood, TEST_CREATED_1, TEST_APARTMENT_1, group);
    }

    protected User createUserWithCreateTime(final String createTime) throws UserException {

        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1,
                TEST_NEIGHBORHOOD_1, createTime, TEST_APARTMENT_1);
    }

    protected User createUserWithCreateTimeAndGroup(final String createTime, final String group) throws UserException {

        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1,
                TEST_NEIGHBORHOOD_1, createTime, TEST_APARTMENT_1, group);
    }
}
