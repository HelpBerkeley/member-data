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

import org.helpberkeley.memberdata.route.GMapApiClient;
import org.helpberkeley.memberdata.route.GmapApiSimulatorFactory;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.time.temporal.ChronoUnit.*;

public class TestBase {

    static final String TEST_NAME_1 = "name 1";
    static final long TEST_ID_1 = 1;
    static final String TEST_USER_NAME_1 = "userName1";
    static final String TEST_ADDRESS_1 = "address 1";
    static final String TEST_CITY_1 = Constants.BERKELEY;
    static final String TEST_PHONE_1 = "111-222-3333";
    static final String TEST_ALT_PHONE_1 = "1-777-888-9999";
    static final String TEST_NEIGHBORHOOD_1 = "neighborhood 1";
    static final Boolean TEST_CONDO_1 = Boolean.FALSE;
    static final String TEST_CREATED_1 = ZonedDateTime.now().minus(6, WEEKS).toString();
    static final Boolean TEST_CONSUMER_REQUEST_1 = false;
    static final String TEST_VOLUNTEER_REQUEST_1 = "Driver";
    static final String TEST_REFERRAL_1 = "Bobby over that old same place";
    static final Boolean TEST_VERIFIED_1 = true;

    static final String TEST_NAME_2 = "name 2";
    static final long TEST_ID_2 = 2;
    static final String TEST_USER_NAME_2 = "userName2";
    static final String TEST_ADDRESS_2 = "address 2";
    static final String TEST_CITY_2 = Constants.ALBANY;
    static final String TEST_PHONE_2 = "222-333-4444";
    static final String TEST_ALT_PHONE_2 = "1-222-333-4444";
    static final String TEST_NEIGHBORHOOD_2 = "neighborhood 2";
    static final Boolean TEST_CONDO_2 = Boolean.TRUE;
    static final String TEST_CREATED_2 = ZonedDateTime.now().minus(8, MINUTES).toString();
    static final Boolean TEST_CONSUMER_REQUEST_2 = true;
    static final String TEST_VOLUNTEER_REQUEST_2 = "Dispatcher";
    static final String TEST_REFERRAL_2 = "none";
    static final Boolean TEST_VERIFIED_2 = true;

    static final String TEST_NAME_3 = "name 3";
    static final long TEST_ID_3 = 3;
    static final String TEST_USER_NAME_3 = "userName3";
    static final String TEST_ADDRESS_3 = "address 3";
    static final String TEST_CITY_3 = Constants.KENSINGTON;
    static final String TEST_PHONE_3 = "333-444-5555";
    static final String TEST_ALT_PHONE_3 = "1-333-444-5555";
    static final String TEST_NEIGHBORHOOD_3 = "neighborhood 3";
    static final Boolean TEST_CONDO_3 = Boolean.FALSE;
    static final String TEST_CREATED_3 = ZonedDateTime.now().minus(1, DAYS).toString();
    static final Boolean TEST_CONSUMER_REQUEST_3 = true;
    static final String TEST_VOLUNTEER_REQUEST_3 = "none";
    static final String TEST_REFERRAL_3 = "none";
    static final Boolean TEST_VERIFIED_3 = true;

    final List<String>  TEST_USER_1_GROUPS = List.of(Constants.GROUP_CONSUMERS);
    final List<String>  TEST_USER_2_GROUPS = List.of(Constants.GROUP_CONSUMERS, Constants.GROUP_DRIVERS);
    final List<String>  TEST_USER_3_GROUPS = List.of(Constants.GROUP_DRIVERS, Constants.GROUP_DISPATCHERS);

    final List<String> NO_GROUP_OWNERSHIPS = List.of();
    final List<String> TEST_USER_1_GROUP_OWNERSHIPS = NO_GROUP_OWNERSHIPS;
    final List<String> TEST_USER_2_GROUP_OWNERSHIPS = NO_GROUP_OWNERSHIPS;
    final List<String> TEST_USER_3_GROUP_OWNERSHIPS = NO_GROUP_OWNERSHIPS;

    static final String[] COMMANDS_WITH_URL = {
            Options.COMMAND_POST_DISPATCHERS,
    };

    static final String[] COMMANDS_WITH_FILE = {
            Options.COMMAND_POST_DRIVERS,
            Options.COMMAND_POST_ALL_MEMBERS,
            Options.COMMAND_POST_ERRORS,
            Options.COMMAND_POST_CONSUMER_REQUESTS,
            Options.COMMAND_POST_VOLUNTEER_REQUESTS,
            Options.COMMAND_UPDATE_ERRORS,
            Options.COMMAND_EMAIL,
            Options.COMMAND_WORKFLOW,
            Options.COMMAND_DRIVER_MESSAGES,
            Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES,
            Options.COMMAND_UPDATE_DISPATCHERS,
            Options.COMMAND_ORDER_HISTORY,
            Options.COMMAND_INREACH,
            Options.COMMAND_COMPLETED_DAILY_ORDERS,
            Options.COMMAND_DRIVERS,
    };

    static final String[] COMMANDS_WITH_NO_PARAMETERS = {
            Options.COMMAND_FETCH,
            Options.COMMAND_GET_DAILY_DELIVERIES,
            Options.COMMAND_DRIVER_ROUTES,
            Options.COMMAND_DRIVER_HISTORY,
    };

    static final String TEST_FILE_NAME = "pom.xml";
    static final String TEST_SECOND_FILE_NAME = "LICENSE.txt";
    static final String TEST_THIRD_FILE_NAME = "README.md";
    static final String TEST_SHORT_URL = Constants.UPLOAD_URI_PREFIX + "ab34dezzAndSomethingY.csv";

    @BeforeClass
    public static void installHttpClientSimulatorFactory() {
        ApiClient.httpClientFactory = new HttpClientSimulatorFactory();
        GMapApiClient.apiFactory = new GmapApiSimulatorFactory();
    }

    protected ApiClient createApiSimulator() {
        Properties properties = Main.loadProperties();
        HttpClientSimulator httpClientSimulator = new HttpClientSimulator();
        return new ApiClient(properties, httpClientSimulator);
    }

    protected User createTestUser1() throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1, TEST_ID_1, TEST_ADDRESS_1,
                Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1, TEST_CREATED_1,
                TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1,
                TEST_USER_1_GROUPS, TEST_USER_1_GROUP_OWNERSHIPS);
    }

    protected User createTestUser2() throws UserException {
        return User.createUser(TEST_NAME_2, TEST_USER_NAME_2, TEST_ID_2, TEST_ADDRESS_2,
                Constants.BERKELEY, TEST_PHONE_2, TEST_ALT_PHONE_2, TEST_NEIGHBORHOOD_2, TEST_CREATED_2,
                TEST_CONDO_2, TEST_CONSUMER_REQUEST_2, TEST_VOLUNTEER_REQUEST_2,
                TEST_REFERRAL_2, TEST_VERIFIED_2,
                TEST_USER_2_GROUPS, TEST_USER_2_GROUP_OWNERSHIPS);
    }

    protected User createTestUser3() throws UserException {
        return User.createUser(TEST_NAME_3, TEST_USER_NAME_3, TEST_ID_3, TEST_ADDRESS_3,
                Constants.BERKELEY, TEST_PHONE_3, TEST_ALT_PHONE_3, TEST_NEIGHBORHOOD_3, TEST_CREATED_3,
                TEST_CONDO_3, TEST_CONSUMER_REQUEST_3, TEST_VOLUNTEER_REQUEST_3,
                TEST_REFERRAL_3, TEST_VERIFIED_3,
                TEST_USER_3_GROUPS, TEST_USER_3_GROUP_OWNERSHIPS);
    }

    protected User createTestUser1WithGroups(String... groups) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1, TEST_ID_1, TEST_ADDRESS_1,
                Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1, TEST_CREATED_1,
                TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1, groups);
    }

    protected User createTestUser2WithGroups(String... groups) throws UserException {
        return User.createUser(TEST_NAME_2, TEST_USER_NAME_2, TEST_ID_2, TEST_ADDRESS_2,
                Constants.BERKELEY, TEST_PHONE_2, TEST_ALT_PHONE_2, TEST_NEIGHBORHOOD_2, TEST_CREATED_2,
                TEST_CONDO_2, TEST_CONSUMER_REQUEST_2, TEST_VOLUNTEER_REQUEST_2,
                TEST_REFERRAL_2, TEST_VERIFIED_2, groups);
    }

    protected User createTestUser3WithGroups(String... groups) throws UserException {
        return User.createUser(TEST_NAME_3, TEST_USER_NAME_3, TEST_ID_3, TEST_ADDRESS_3,
                Constants.BERKELEY, TEST_PHONE_3, TEST_ALT_PHONE_3, TEST_NEIGHBORHOOD_3, TEST_CREATED_3,
                TEST_CONDO_3, TEST_CONSUMER_REQUEST_3, TEST_VOLUNTEER_REQUEST_3,
                TEST_REFERRAL_3, TEST_VERIFIED_3, groups);
    }

    // no groups
    protected User createUser() throws UserException {
        return User.createUser(TEST_USER_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1,
                TEST_VOLUNTEER_REQUEST_1, TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithGroups(final String... groups) throws UserException {
        return User.createUser(TEST_USER_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1, groups);
    }

    protected User createUserWithName(final String name) throws UserException {
        return User.createUser(name, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

        protected User createUserWithID(long id) throws UserException {
            return User.createUser(TEST_USER_NAME_1, TEST_USER_NAME_1,
                    id, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                    TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                    TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithUserName(final String userName) throws UserException {
        return User.createUser(TEST_NAME_1, userName,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithAddress(final String address) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, address, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithCity(final String city) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, city, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    User createUserWithPhone(final String phone) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, phone, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    User createUserWithAltPhone(final String phone) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, phone, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    User createUserWithCondo(boolean condo) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, condo, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    User createUserWithConsumerRequest(String userName, boolean consumerRequest) throws UserException {
        return User.createUser(TEST_NAME_1, userName,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, consumerRequest, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithGroup(final String userName, final String group) throws UserException {
        return User.createUser(TEST_NAME_1, userName,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1, group);
    }

    protected User createUserWithGroupAndGroupOwner(final String userName, final String group,
            final String groupOwner) throws UserException {

        return User.createUser(TEST_NAME_1, userName,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1, List.of(group), List.of(groupOwner));
    }
    protected User createUserWithGroupsAndGroupsOwned(final String userName, final List<String> groups,
            final List<String> groupsOwned) throws UserException {

        return User.createUser(TEST_NAME_1, userName,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1, groups, groupsOwned);
    }

    protected User createUserWithNeighborhood(final String neighborhood) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, neighborhood,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithCityAndNeighborhood(
            final String city, final String neighborhood) throws UserException {
        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, city, TEST_PHONE_1, TEST_ALT_PHONE_1, neighborhood,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithCityAndNeighborhood(final String userName,
                                                     final String city, final String neighborhood) throws UserException {
        return User.createUser(TEST_NAME_1, userName,
                TEST_ID_1, TEST_ADDRESS_1, city, TEST_PHONE_1, TEST_ALT_PHONE_1, neighborhood,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithGroupAndNeighborhood(
            final String group, final String neighborhood) throws UserException {

        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, neighborhood,
                TEST_CREATED_1, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1, group);
    }

    protected User createUserWithCreateTime(final String userName, final String createTime) throws UserException {

        return User.createUser(TEST_NAME_1, userName,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                createTime, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithCreateTimeAndGroup(
            final String userName, final String createTime, final String group) throws UserException {

        return User.createUser(TEST_NAME_1, userName,
                TEST_ID_1, TEST_ADDRESS_1, Constants.BERKELEY, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                createTime, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1, group);
    }

    protected User createUserWithCreateTimeAndCity(final String createTime, final String city) throws UserException {

        return User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, city, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1,
                createTime, TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithConsumerRequest(final String userName) throws UserException {

        return User.createUser(TEST_NAME_1, userName, TEST_ID_1, TEST_ADDRESS_1,
                TEST_CITY_1, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1, TEST_CREATED_1,
                TEST_CONDO_1, true, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithReferral(final String userName, final String referral) throws UserException {

        return User.createUser(TEST_NAME_1, userName, TEST_ID_1, TEST_ADDRESS_1,
                TEST_CITY_1, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1, TEST_CREATED_1,
                TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                referral, TEST_VERIFIED_1);
    }
    protected User createUserWithConsumerRequestAndConsumerGroup(final String userName) throws UserException {

        return User.createUser(TEST_NAME_1, userName, TEST_ID_1, TEST_ADDRESS_1,
                TEST_CITY_1, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1, TEST_CREATED_1,
                TEST_CONDO_1, true, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1, Constants.GROUP_CONSUMERS);
    }

    protected User createUserWithVolunteerRequest(final String userName, final String request) throws UserException {

        return User.createUser(TEST_NAME_1, userName, TEST_ID_1, TEST_ADDRESS_1,
                TEST_CITY_1, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1, TEST_CREATED_1,
                TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, request,
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithNoRequestsNoGroups(final String userName) throws UserException {

        return User.createUser(TEST_NAME_1, userName, TEST_ID_1, TEST_ADDRESS_1,
                TEST_CITY_1, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1, TEST_CREATED_1,
                TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, "none",
                TEST_REFERRAL_1, TEST_VERIFIED_1);
    }

    protected User createUserWithEmailVerified(final String userName, boolean verified) throws UserException {

        return User.createUser(TEST_NAME_1, userName, TEST_ID_1, TEST_ADDRESS_1,
                TEST_CITY_1, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1, TEST_CREATED_1,
                TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, verified);
    }

    protected User createUserWithNameConsumerAndDriver(final String name,
        boolean consumer, boolean driver) throws UserException {

        List<String> groups = new ArrayList<>();
        if (consumer) {
            groups.add(Constants.GROUP_CONSUMERS);
        }
        if (driver) {
            groups.add(Constants.GROUP_DRIVERS);
        }

        return User.createUser(name, name, TEST_ID_1, TEST_ADDRESS_1,
                TEST_CITY_1, TEST_PHONE_1, TEST_ALT_PHONE_1, TEST_NEIGHBORHOOD_1, TEST_CREATED_1,
                TEST_CONDO_1, TEST_CONSUMER_REQUEST_1, TEST_VOLUNTEER_REQUEST_1,
                TEST_REFERRAL_1, TEST_VERIFIED_1, groups, NO_GROUP_OWNERSHIPS);
    }

    public String readResourceFile(final String fileName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(fileName);

        if (url == null) {
            throw new RuntimeException("file " + fileName + " not found");
        }
        try {
            return (Files.readString(Paths.get(url.toURI())));
        } catch (IOException|URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    String readFile(final String fileName) {
        try {
        return Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String findResourceFile(final String fileName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(fileName);

        if (url == null) {
            throw new RuntimeException("file " + fileName + " not found");
        }

        return url.getFile();
    }

    String shortBoolean(boolean value) {
        return value ? "Y" : "N";
    }
}
