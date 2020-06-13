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

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Test cases for User
 */
public class UserTest extends TestBase {

    private static class TestField {
        final String original;
        final String expected;

        TestField(final String original, final String expected) {
            this.original = original;
            this.expected = expected;
        }

    }
    private static class PhoneNumber extends TestField {
        PhoneNumber(final String original, final String expected) {
            super(original, expected);
        }
    }
    private static class Address extends TestField {
        Address(final String original, final String expected) {
            super(original, expected);
        }
    }
    private static class City extends TestField {
        City(final String original, final String expected) {
            super(original, expected);
        }
    }

    private static final List<PhoneNumber> testPhoneNumbers = new ArrayList<>();
    private static final List<PhoneNumber> badPhoneNumbers = new ArrayList<>();
    private static final List<PhoneNumber> badAltPhoneNumbers = new ArrayList<>();
    private static final List<Address> testAddresses = new ArrayList<>();
    private static final List<City> testCities = new ArrayList<>();

    /**
     * Create a variety of differently formed phone numbers that we are able to parse
     *
     * FIX THIS, DS: add other variations as we learn them.
     */
    @BeforeClass
    public static void createTestPhoneNumbers() {

        testPhoneNumbers.add(new PhoneNumber("5105551212", "510-555-1212"));
        testPhoneNumbers.add(new PhoneNumber("510 555-1212", "510-555-1212"));
        testPhoneNumbers.add(new PhoneNumber("510 555 1212", "510-555-1212"));
        testPhoneNumbers.add(new PhoneNumber("(510) 555-1212", "510-555-1212"));
        testPhoneNumbers.add(new PhoneNumber("510.555.1212", "510-555-1212"));
        testPhoneNumbers.add(new PhoneNumber("510.555.1212", "510-555-1212"));
        testPhoneNumbers.add(new PhoneNumber("510 555  1212", "510-555-1212"));
        testPhoneNumbers.add(new PhoneNumber("510-555-1212", "510-555-1212"));
        testPhoneNumbers.add(new PhoneNumber("1-510-555-1212", "510-555-1212"));
    }

    @BeforeClass
    public static void createBadPhoneNumbers() {

        badPhoneNumbers.add(new PhoneNumber("745-1211", User.ERROR_PRIMARY_PHONE_MISSING_AREA_CODE));
        badPhoneNumbers.add(new PhoneNumber("9.510.777.8888", User.ERROR_PRIMARY_PHONE_CANNOT_PARSE_PHONE));
        badPhoneNumbers.add(new PhoneNumber("51-777-8888", User.ERROR_PRIMARY_PHONE_CANNOT_PARSE_PHONE));
    }

    @BeforeClass
    public static void createBadAltPhoneNumbers() {

        badAltPhoneNumbers.add(new PhoneNumber("745-1211", User.ERROR_SECOND_PHONE_MISSING_AREA_CODE));
        badAltPhoneNumbers.add(new PhoneNumber("9.510.777.8888", User.ERROR_SECOND_PHONE_CANNOT_PARSE_PHONE));
        badAltPhoneNumbers.add(new PhoneNumber("51-777-8888", User.ERROR_SECOND_PHONE_CANNOT_PARSE_PHONE));
    }

    /**
     * Create a variety of differently formed addresses that we should be able to trim
     *
     * FIX THIS, DS: add other variations as we learn them.
     */
    @BeforeClass
    public static void createTestAddresses() {
        testAddresses.add(new Address("123 ABC St. Berkeley, CA 94708", "123 ABC St."));
        testAddresses.add(new Address("123 abc berkeley california 94708", "123 abc"));
        testAddresses.add(new Address("1142 #7 Berkeley Way", "1142 #7 Berkeley Way"));
        testAddresses.add(new Address("1010 Tenth St.", "1010 Tenth St."));
    }

    /**
     * Create a variety of differently (mis)spellings of Berkeley we should be able to detect
     *
     * FIX THIS, DS: add other variations as we learn them.
     */
    @BeforeClass
    public static void createTestCities() {
        testCities.add(new City(Constants.BERKELEY, Constants.BERKELEY));
        testCities.add(new City("Berkeley ", Constants.BERKELEY));
        testCities.add(new City(" Berkeley ", Constants.BERKELEY));
        testCities.add(new City("berkeley", Constants.BERKELEY));
        testCities.add(new City("berkly", Constants.BERKELEY));
        testCities.add(new City("berekly", Constants.BERKELEY));
        testCities.add(new City("berkley", Constants.BERKELEY));

        testCities.add(new City(Constants.ALBANY, Constants.ALBANY));
        testCities.add(new City("albany", Constants.ALBANY));
        testCities.add(new City(" albany ", Constants.ALBANY));
        testCities.add(new City("ablany", Constants.ALBANY));
        testCities.add(new City("albanny", Constants.ALBANY));
        testCities.add(new City("albny", Constants.ALBANY));
        testCities.add(new City("albney", Constants.ALBANY));
        testCities.add(new City("albnay", Constants.ALBANY));

        testCities.add(new City(Constants.KENSINGTON, Constants.KENSINGTON));
        testCities.add(new City(" kensignton ", Constants.KENSINGTON));
        testCities.add(new City(" kensington ", Constants.KENSINGTON));
        testCities.add(new City("kensigton ", Constants.KENSINGTON));
        testCities.add(new City("kensinton ", Constants.KENSINGTON));
        testCities.add(new City("kennsignton ", Constants.KENSINGTON));
        testCities.add(new City("kensegntin ", Constants.KENSINGTON));
        testCities.add(new City("kensegntin ", Constants.KENSINGTON));

        testCities.add(new City("am not albany", "am not albany"));
        testCities.add(new City("berklee this is not", "berklee this is not"));
        testCities.add(new City("kensington this is not", "kensington this is not"));
    }

    @Test
    public void phoneNumberNormalizationTest() throws UserException {
        for (PhoneNumber phoneNumber : testPhoneNumbers) {
            User user = createUserWithPhone(phoneNumber.original);
            assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber.expected);
        }
    }

    @Test
    public void phoneNumberErrorsTest() {
        for (PhoneNumber phoneNumber : badPhoneNumbers) {
            Throwable thrown = catchThrowable(() -> createUserWithPhone(phoneNumber.original));
            assertThat(thrown).isInstanceOf(UserException.class);
            UserException userException = (UserException) thrown;
            assertThat(userException.user).isNotNull();
            assertThat(userException.user.getDataErrors()).contains(phoneNumber.expected);
        }
    }

    @Test
    public void altPhoneNumberErrorsTest() {
        for (PhoneNumber phoneNumber : badAltPhoneNumbers) {
            Throwable thrown = catchThrowable(() -> createUserWithAltPhone(phoneNumber.original));
            assertThat(thrown).isInstanceOf(UserException.class);
            UserException userException = (UserException) thrown;
            assertThat(userException.user).isNotNull();
            assertThat(userException.user.getDataErrors()).contains(phoneNumber.expected);
        }
    }

    @Test
    public void emptyAltPhoneNumberTest() throws UserException {
        User u1 = createUserWithAltPhone("");
        assertThat(u1.getAltPhoneNumber()).isEqualTo(User.NOT_PROVIDED);
    }

    @Test
    public void equalityTestNoRoles() throws UserException {
        assertThat(createTestUser1()).isEqualTo(createTestUser1());
    }

    @Test
    public void inEqualityTestNoRoles() throws UserException {
        assertThat(createTestUser1()).isNotEqualTo(createTestUser2());
    }

    @Test
    public void equalityTestEmailVerified() throws UserException {
        User u1 = createUserWithEmailVerified("u1", false);
        User u2 = createUserWithEmailVerified("u1", true);

        assertThat(u1).isNotEqualTo(u2);
        assertThat(u1).isEqualTo(u1);
        assertThat(u2).isEqualTo(u2);
    }

    @Test
    public void equalityTestConsumerRole() throws UserException {
        User user1 = createUserWithGroup("u1", Constants.GROUP_CONSUMERS);
        User user2 = createUser();

        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroup("u1", Constants.GROUP_CONSUMERS);
        assertThat(user1).isEqualTo(user2);

        user1 = createUserWithGroups(Constants.GROUP_CONSUMERS, Constants.GROUP_DISPATCHERS);
        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroups(Constants.GROUP_CONSUMERS, Constants.GROUP_DISPATCHERS);
        assertThat(user1).isEqualTo(user2);

        user1 = createUserWithGroups(Constants.GROUP_CONSUMERS, Constants.GROUP_DISPATCHERS, Constants.GROUP_DRIVERS);
        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroups(Constants.GROUP_CONSUMERS, Constants.GROUP_DISPATCHERS, Constants.GROUP_DRIVERS);
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    public void equalityTestDriverRole() throws UserException {
        User user1 = createUserWithGroup("u1", Constants.GROUP_DRIVERS);
        User user2 = createUser();

        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroup("u1", Constants.GROUP_DRIVERS);
        assertThat(user1).isEqualTo(user2);

        user1 = createUserWithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_DISPATCHERS);
        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_DISPATCHERS);
        assertThat(user1).isEqualTo(user2);

        user1 = createUserWithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_DISPATCHERS, Constants.GROUP_CONSUMERS);
        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_DISPATCHERS, Constants.GROUP_CONSUMERS);
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    public void equalityTestDispatcherRole() throws UserException {
        User user1 = createUserWithGroup("u1", Constants.GROUP_DISPATCHERS);
        User user2 = createUser();

        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroup("u1", Constants.GROUP_DISPATCHERS);
        assertThat(user1).isEqualTo(user2);

        user1 = createUserWithGroups(Constants.GROUP_DISPATCHERS, Constants.GROUP_DRIVERS);
        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroups(Constants.GROUP_DISPATCHERS, Constants.GROUP_DRIVERS);
        assertThat(user1).isEqualTo(user2);

        user1 = createUserWithGroups(Constants.GROUP_DISPATCHERS, Constants.GROUP_DRIVERS, Constants.GROUP_CONSUMERS);
        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroups(Constants.GROUP_DISPATCHERS, Constants.GROUP_DRIVERS, Constants.GROUP_CONSUMERS);
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    public void nameInequalityTest() throws UserException {

        User user1 = createUser();
        User user2 = createUserWithName(TEST_NAME_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void userNameInequalityTest() throws UserException {

        User user1 = createUserWithUserName(TEST_USER_NAME_1);
        User user2 = createUserWithUserName(TEST_USER_NAME_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void idInequalityTest() throws UserException {

        User user1 = createUser();
        User user2 = createUserWithID(TEST_ID_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void addressInequalityTest() throws UserException {

        User user1 = createUserWithAddress(TEST_ADDRESS_1);
        User user2 = createUserWithAddress(TEST_ADDRESS_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void altPhoneInequalityTest() throws UserException {

        User user1 = createUserWithAltPhone(TEST_PHONE_1);
        User user2 = createUserWithAltPhone(TEST_PHONE_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void phoneInequalityTest() throws UserException {

        User user1 = createUserWithPhone(TEST_PHONE_1);
        User user2 = createUserWithPhone(TEST_PHONE_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void neighborhoodInequalityTest() throws UserException {

        User user1 = createUserWithNeighborhood(TEST_NEIGHBORHOOD_1);
        User user2 = createUserWithNeighborhood(TEST_NEIGHBORHOOD_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void createTimeInequalityTest() throws UserException {

        User user1 = createUserWithCreateTime("u1", TEST_CREATED_1);
        User user2 = createUserWithCreateTime("u1", TEST_CREATED_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void cityInequalityTest() throws UserException {

        User user1 = createUserWithCity(Constants.BERKELEY);
        User user2 = createUserWithCity("Some other city");
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void condoInequaityTest() throws UserException {
        User user1 = createUserWithCondo(true);
        User user2 = createUserWithCondo(false);
        assertThat(user1).isNotEqualTo(user2);

    }

    @Test
    public void consumerRequestInequaityTest() throws UserException {
        User user1 = createUserWithConsumerRequest(TEST_USER_NAME_1, true);
        User user2 = createUserWithConsumerRequest(TEST_USER_NAME_1, false);
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void volunteerRequestInequaityTest() throws UserException {
        User user1 = createUserWithVolunteerRequest(TEST_USER_NAME_1, TEST_VOLUNTEER_REQUEST_1);
        User user2 = createUserWithVolunteerRequest(TEST_USER_NAME_1, TEST_VOLUNTEER_REQUEST_2);
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void referralRequestInequaityTest() throws UserException {
        User user1 = createUserWithReferral(TEST_USER_NAME_1, TEST_REFERRAL_1);
        User user2 = createUserWithReferral(TEST_USER_NAME_1, TEST_REFERRAL_2);
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void mismatchObjectInequalityTest() throws UserException {
        User user1 = createUser();
        assertThat(user1).isNotEqualTo(this);
    }

    @Test
    public void nullObjectInequalityTest() throws UserException {
        User user1 = createUser();
        assertThat(user1).isNotEqualTo(null);
    }

    @Test
    public void minimizeAddressTest() throws UserException {
        for (Address address : testAddresses) {
            User user = createUserWithAddress(address.original);
            assertThat(user.getAddress()).isEqualTo(address.expected);
        }
    }

    @Test
    public void isSupportedCityTest() throws UserException {

        for (City city : testCities) {
            User user = createUserWithCity(city.original);
            assertThat(user.getCity()).isEqualTo(city.expected);
        }
    }

    @Test
    @Ignore
    public void unknownBerkeleyNeighborhoodTest() {

        String[] unknowns = { "Unknown", "unknown", " unknown " };

        for (String neighborhood : unknowns) {
            Throwable thrown = catchThrowable(() -> createUserWithGroupAndNeighborhood(Constants.GROUP_CONSUMERS, neighborhood));
            assertThat(thrown).isInstanceOf(UserException.class);
            UserException userException = (UserException) thrown;
            assertThat(userException.user).isNotNull();
            assertThat(userException.user.getDataErrors()).contains(User.AUDIT_ERROR_NEIGHBORHOOD_UNKNOWN);
        }
    }

    @Test
    public void unknownNonBerkeleyNeighborhoodTest() throws UserException {
        User user = createUserWithCityAndNeighborhood("Oakland", "unknown");
        assertThat(user.getDataErrors()).isEmpty();
    }

    @Test
    @Ignore
    public void unknownBerkeleyNeighborhoodDriverTest() {
        Throwable thrown = catchThrowable(() -> createUserWithGroupAndNeighborhood(Constants.GROUP_DRIVERS, "unknown"));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException) thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(User.AUDIT_ERROR_NEIGHBORHOOD_UNKNOWN);
    }

    @Test
    @Ignore
    public void unknownBerkeleyNeighborhoodDispatcherTest() {
        Throwable thrown = catchThrowable(() -> createUserWithGroupAndNeighborhood(Constants.GROUP_CONSUMERS, "unknown"));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException) thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(User.AUDIT_ERROR_NEIGHBORHOOD_UNKNOWN);
    }

    @Test
    public void toStringTest() throws UserException {
        User user = createTestUser1();

        assertThat(user.toString()).contains(TEST_NAME_1);
        assertThat(user.toString()).contains(TEST_USER_NAME_1);
        assertThat(user.toString()).contains(TEST_ADDRESS_1);
        assertThat(user.toString()).contains(TEST_PHONE_1);
        assertThat(user.toString()).contains(TEST_CITY_1);
        assertThat(user.toString()).contains(TEST_NEIGHBORHOOD_1);
        assertThat(user.toString()).contains(TEST_VOLUNTEER_REQUEST_1);

        for (String group : TEST_USER_1_GROUPS) {
            assertThat(user.toString()).contains(group);
        }
    }

    @Test
    public void groupBHSTest() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_BHS);
        User u2 = createUserWithGroup("u1", Constants.GROUP_BHS);
        User u3 = createUser();

        assertThat(u1.isBHS()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isBHS()).isFalse();
    }

    @Test
    public void groupHelpLine() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_HELPLINE);
        User u2 = createUserWithGroup("u1", Constants.GROUP_HELPLINE);
        User u3 = createUser();

        assertThat(u1.isHelpLine()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isHelpLine()).isFalse();
    }

    @Test
    public void groupSiteLine() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_SITELINE);
        User u2 = createUserWithGroup("u1", Constants.GROUP_SITELINE);
        User u3 = createUser();

        assertThat(u1.isSiteLine()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isSiteLine()).isFalse();
    }

    @Test
    public void groupInReach() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_INREACH);
        User u2 = createUserWithGroup("u1", Constants.GROUP_INREACH);
        User u3 = createUser();

        assertThat(u1.isInReach()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isInReach()).isFalse();
    }

    @Test
    public void groupOutReach() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_OUTREACH);
        User u2 = createUserWithGroup("u1", Constants.GROUP_OUTREACH);
        User u3 = createUser();

        assertThat(u1.isOutReach()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isOutReach()).isFalse();
    }

    @Test
    public void groupMarketeting() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_MARKETING);
        User u2 = createUserWithGroup("u1", Constants.GROUP_MARKETING);
        User u3 = createUser();

        assertThat(u1.isMarketing()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isMarketing()).isFalse();
    }

    @Test
    public void groupModerators() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_MODERATORS);
        User u2 = createUserWithGroup("u1", Constants.GROUP_MODERATORS);
        User u3 = createUser();

        assertThat(u1.isModerator()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isModerator()).isFalse();
    }

    @Test
    public void groupWorkflow() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_WORKFLOW);
        User u2 = createUserWithGroup("u1", Constants.GROUP_WORKFLOW);
        User u3 = createUser();

        assertThat(u1.isWorkflow()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isWorkflow()).isFalse();
    }

    @Test
    public void groupVoiceOnly() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_VOICEONLY);
        User u2 = createUserWithGroup("u1", Constants.GROUP_VOICEONLY);
        User u3 = createUser();

        assertThat(u1.isVoiceOnly()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isVoiceOnly()).isFalse();
    }

    @Test
    public void groupTrustLevel4() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_TRUST_LEVEL_4);
        User u2 = createUserWithGroup("u1", Constants.GROUP_TRUST_LEVEL_4);
        User u3 = createUser();

        assertThat(u1.isTrustLevel4()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isTrustLevel4()).isFalse();
    }

    @Test
    public void groupAdvisor() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_ADVISOR);
        User u2 = createUserWithGroup("u1", Constants.GROUP_ADVISOR);
        User u3 = createUser();

        assertThat(u1.isAdvisor()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isAdvisor()).isFalse();
    }

    @Test
    public void groupCoordinator() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_COORDINATOR);
        User u2 = createUserWithGroup("u1", Constants.GROUP_COORDINATOR);
        User u3 = createUser();

        assertThat(u1.isCoordinator()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isCoordinator()).isFalse();
    }

    @Test
    public void groupAdmin() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_ADMIN);
        User u2 = createUserWithGroup("u1", Constants.GROUP_ADMIN);
        User u3 = createUser();

        assertThat(u1.isAdmin()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isAdmin()).isFalse();
    }

    @Test
    public void groupCustomerInfo() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_CUSTOMER_INFO);
        User u2 = createUserWithGroup("u1", Constants.GROUP_CUSTOMER_INFO);
        User u3 = createUser();

        assertThat(u1.isCustomerInfo()).isTrue();
        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u3.isCustomerInfo()).isFalse();
    }

    @Test
    public void nameWithCommaTest() throws UserException {
        String name = "J, Alfred, Prufrock";
        User u1 = createUserWithName(name);
        assertThat(u1.getName()).isEqualTo(name);
    }

    @Test
    public void neighborhoodWithCommaTest() throws UserException {
        String neighborhood = "Somewhere, over, the rainbow";
        User u1 = createUserWithNeighborhood(neighborhood);
        assertThat(u1.getNeighborhood()).isEqualTo(neighborhood);
    }

    @Test
    public void cityWithCommaTest() throws UserException {
        String city = "Anytown, U.S.A.";
        User u1 = createUserWithCity(city);
        assertThat(u1.getCity()).isEqualTo(city);
    }

    @Test
    public void addressWithCommaTest() throws UserException {
        String address = "1,2,3 O, Larry-o";
        User u1 = createUserWithAddress(address);
        assertThat(u1.getAddress()).isEqualTo(address);
    }

    @Test
    public void referralWithCommaTest() throws UserException {
        String referral = "I heard it through the grapevine, not much longer, would you be mine";
        User u1 = createUserWithReferral("u1", referral);
        assertThat(u1.getReferral()).isEqualTo(referral);
    }
}
