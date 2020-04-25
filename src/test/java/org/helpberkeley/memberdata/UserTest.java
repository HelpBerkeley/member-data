package org.helpberkeley.memberdata;

import org.junit.BeforeClass;
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

        badPhoneNumbers.add(new PhoneNumber("745-1211", User.ERROR_MISSING_AREA_CODE));
        badPhoneNumbers.add(new PhoneNumber("9.510.777.8888", User.ERROR_CANNOT_PARSE_PHONE));
        badPhoneNumbers.add(new PhoneNumber("51-777-8888", User.ERROR_CANNOT_PARSE_PHONE));
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
    public void equalityTestNoRoles() throws UserException {
        assertThat(createTestUser1()).isEqualTo(createTestUser1());
    }

    @Test
    public void inEqualityTestNoRoles() throws UserException {
        assertThat(createTestUser1()).isNotEqualTo(createTestUser2());
    }

    @Test
    public void equalityTestConsumerRole() throws UserException {
        User user1 = createUserWithGroup(Constants.GROUP_CONSUMERS);
        User user2 = createUser();

        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroup(Constants.GROUP_CONSUMERS);
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
        User user1 = createUserWithGroup(Constants.GROUP_DRIVERS);
        User user2 = createUser();

        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroup(Constants.GROUP_DRIVERS);
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
        User user1 = createUserWithGroup(Constants.GROUP_DISPATCHERS);
        User user2 = createUser();

        assertThat(user1).isNotEqualTo(user2);

        user2 = createUserWithGroup(Constants.GROUP_DISPATCHERS);
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

        User user1 = createUserWithCreateTime(TEST_CREATED_1);
        User user2 = createUserWithCreateTime(TEST_CREATED_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void cityInequalityTest() throws UserException {

        User user1 = createUserWithCity(Constants.BERKELEY);
        User user2 = createUserWithCity("Some other city");
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void apartmentInequaityTest() throws UserException {
        User user1 = createUserWithApartment(true);
        User user2 = createUserWithApartment(false);
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
        User user1 = createUserWithVolunteerRequest(TEST_VOLUNTEER_REQUEST_1);
        User user2 = createUserWithVolunteerRequest(TEST_VOLUNTEER_REQUEST_2);
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
    public void unknownBerkeleyNeighborhoodDriverTest() {
        Throwable thrown = catchThrowable(() -> createUserWithGroupAndNeighborhood(Constants.GROUP_DRIVERS, "unknown"));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException) thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(User.AUDIT_ERROR_NEIGHBORHOOD_UNKNOWN);
    }

    @Test
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
}
