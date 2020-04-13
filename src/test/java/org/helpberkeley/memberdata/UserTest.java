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
    }

    @Test
    public void phoneNumberNormalizationTest() throws UserException {
        for (PhoneNumber phoneNumber : testPhoneNumbers) {
            User user = createUserWithPhone(phoneNumber.original);
            assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber.expected);
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

        User user1 = createUser();
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

        User user1 = createUser();
        User user2 = createUserWithAddress(TEST_ADDRESS_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void phoneInequalityTest() throws UserException {

        User user1 = createUser();
        User user2 = createUserWithPhone(TEST_PHONE_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void neighborhoodInequalityTest() throws UserException {

        User user1 = createUser();
        User user2 = createUserWithNeighborhood(TEST_NEIGHBORHOOD_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void cityInequalityTest() throws UserException {

        User user1 = createUserWithCity(Constants.BERKELEY);
        User user2 = createUserWithCity("Some other city");
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void nullNameTest() {
        Throwable thrown = catchThrowable(() -> createUserWithName(null));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException)thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(User.AUDIT_ERROR_MISSING_NAME);
    }

    @Test
    public void nullUserNameTest() {
        Throwable thrown = catchThrowable(() -> createUserWithUserName(null));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException)thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(User.AUDIT_ERROR_MISSING_USERNAME);
    }

    @Test
    public void nullAddressTest() {
        Throwable thrown = catchThrowable(() -> createUserWithAddress(null));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException)thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(User.AUDIT_ERROR_MISSING_ADDRESS);
    }

    @Test
    public void nullCityTest() {
        Throwable thrown = catchThrowable(() -> createUserWithCity(null));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException)thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(User.AUDIT_ERROR_MISSING_CITY);
    }

    @Test
    public void nullNeighborhoodTest() {
        Throwable thrown = catchThrowable(() -> createUserWithNeighborhood(null));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException)thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(User.AUDIT_ERROR_MISSING_NEIGHBORHOOD);
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
    public void unknownBerkeleyNeighborhoodTest() throws UserException {

        String[] unknowns = { "Unknown", "unknown", " unknown " };

        for (String neighborhood : unknowns) {
            Throwable thrown = catchThrowable(() -> createUserWithGroupAndNeighborhood(Constants.GROUP_CONSUMERS, neighborhood));
            assertThat(thrown).isInstanceOf(UserException.class);
            UserException userException = (UserException) thrown;
            assertThat(userException.user).isNotNull();
            assertThat(userException.user.getDataErrors()).contains(
                    User.AUDIT_ERROR_NEIGHBORHOOD_UNKNOWN + neighborhood + ", "
                            + User.ADDRESS_COLUMN + " : " + userException.user.getAddress() + ", : "
                            + User.CITY_COLUMN + " : " + createUser().getCity());
        }
    }

    @Test
    public void unknownNonBerkeleyNeighborhoodTest() throws UserException {
        User user = createUserWithCityAndNeighborhood("Oakland", "unknown");
        assertThat(user.getDataErrors()).isEmpty();
    }

    @Test
    public void unknownBerkeleyNeighborhoodDriverTest() throws UserException {
        Throwable thrown = catchThrowable(() -> createUserWithGroupAndNeighborhood(Constants.GROUP_DRIVERS, "unknown"));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException) thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(
                User.AUDIT_ERROR_NEIGHBORHOOD_UNKNOWN + "unknown, "
                        + User.ADDRESS_COLUMN + " : " + userException.user.getAddress() + ", : "
                        + User.CITY_COLUMN + " : " + createUser().getCity());
    }

    @Test
    public void unknownBerkeleyNeighborhoodDispatcherTest() throws UserException {
        Throwable thrown = catchThrowable(() -> createUserWithGroupAndNeighborhood(Constants.GROUP_CONSUMERS, "unknown"));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException) thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(
                User.AUDIT_ERROR_NEIGHBORHOOD_UNKNOWN + "unknown, "
                        + User.ADDRESS_COLUMN + " : " + userException.user.getAddress() + ", : "
                        + User.CITY_COLUMN + " : " + createUser().getCity());
    }
}
