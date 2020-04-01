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
        String original;
        String expected;

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
        testCities.add(new City("Berkeley", "Berkeley"));
        testCities.add(new City("Berkeley ", "Berkeley"));
        testCities.add(new City(" Berkeley ", "Berkeley"));
        testCities.add(new City("berkeley", "Berkeley"));
        testCities.add(new City("berkly", "Berkeley"));
        testCities.add(new City("berekly", "Berkeley"));
        testCities.add(new City("berkley", "Berkeley"));
    }

    @Test
    public void phoneNumberNormalizationTest() throws UserException {
        for (PhoneNumber phoneNumber : testPhoneNumbers) {
            User user = User.createUser(
                    "u", "u", 1, "a", User.BERKELEY, phoneNumber.original, "g");
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
        User user1 = createUser();
        User user2 = createUser();

        user1.addGroup(Group.CONSUMER);
        assertThat(user1).isNotEqualTo(user2);

        user2.addGroup(Group.CONSUMER);
        assertThat(user1).isEqualTo(user2);

        user1.addGroup(Group.DISPATCHER);
        assertThat(user1).isNotEqualTo(user2);

        user2.addGroup(Group.DISPATCHER);
        assertThat(user1).isEqualTo(user2);

        user1.addGroup(Group.DRIVER);
        assertThat(user1).isNotEqualTo(user2);

        user2.addGroup(Group.DRIVER);
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    public void equalityTestDriverRole() throws UserException {
        User user1 = createTestUser1();
        User user2 = createTestUser1();

        Group group = Group.createGroup(Group.DRIVER, Collections.singletonList(TEST_USER_NAME_1));
        user1.addGroups(Collections.singletonList(group));

        assertThat(user1).isNotEqualTo(user2);

        user2.addGroups(Collections.singletonList(group));

        assertThat(user1).isEqualTo(user2);
    }

    @Test
    public void equalityTestDispatcherRole() throws UserException {
        User user1 = createTestUser1();
        User user2 = createTestUser1();

        Group group = Group.createGroup(Group.DISPATCHER, Collections.singletonList(TEST_USER_NAME_1));
        user1.addGroups(Collections.singletonList(group));

        assertThat(user1).isNotEqualTo(user2);

        user2.addGroups(Collections.singletonList(group));

        assertThat(user1).isEqualTo(user2);
    }

    @Test
    public void equalityTestAllRoles() throws UserException {
        User user1 = createTestUser1();
        User user2 = createTestUser1();

        Group consumers = Group.createGroup(Group.CONSUMER, Collections.singletonList(TEST_USER_NAME_1));
        Group drivers = Group.createGroup(Group.DRIVER, Collections.singletonList(TEST_USER_NAME_1));
        Group dispatchers = Group.createGroup(Group.DISPATCHER, Collections.singletonList(TEST_USER_NAME_1));

        user1.addGroups(List.of(consumers, drivers, dispatchers));
        assertThat(user1).isNotEqualTo(user2);

        user2.addGroups(List.of(consumers));
        assertThat(user1).isNotEqualTo(user2);

        user2.addGroups(List.of(drivers));
        assertThat(user1).isNotEqualTo(user2);

        user2.addGroups(List.of(dispatchers));
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    public void nameInequalityTest() throws UserException {

        User user1 = createUserWithPhone(TEST_ADDRESS_1);
        User user2 = User.createUser(TEST_NAME_2, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1, TEST_NEIGHBORHOOD_1);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void userNameInequalityTest() throws UserException {

        User user1 = createUserWithPhone(TEST_ADDRESS_1);
        User user2 = User.createUser(TEST_NAME_1, TEST_USER_NAME_2,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1, TEST_NEIGHBORHOOD_1);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void idInequalityTest() throws UserException {

        User user1 = createUserWithPhone(TEST_ADDRESS_1);
        User user2 = User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_2, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1, TEST_NEIGHBORHOOD_1);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void addressInequalityTest() throws UserException {

        User user1 = createUserWithPhone(TEST_ADDRESS_1);
        User user2 = User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_2, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1, TEST_NEIGHBORHOOD_1);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void phoneInequalityTest() throws UserException {

        User user1 = createUserWithPhone(TEST_ADDRESS_1);
        User user2 = User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_2, TEST_NEIGHBORHOOD_1);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void neighborhoodInequalityTest() throws UserException {

        User user1 = createUserWithPhone(TEST_ADDRESS_1);
        User user2 = User.createUser(TEST_NAME_1, TEST_USER_NAME_1,
                TEST_ID_1, TEST_ADDRESS_1, User.BERKELEY, TEST_PHONE_1, TEST_NEIGHBORHOOD_2);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void cityInequalityTest() throws UserException {

        User user1 = createUserWithPhone(TEST_ADDRESS_1);

        String city = "Somewhere Else";

        Throwable thrown = catchThrowable(() -> createUserWithCity(city));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException)thrown;
        assertThat(userException.user).isNotNull();
        assertThat(userException.user.getDataErrors()).contains(
                User.AUDIT_ERROR_CITY_IS_NOT_BERKELEY + city);

        assertThat(user1).isNotEqualTo(userException.user);
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
    public void nonBerkeleyCityTest() {

        String city = "Oakland";

        Throwable thrown = catchThrowable(() -> createUserWithCity(city));
        assertThat(thrown).isInstanceOf(UserException.class);
        UserException userException = (UserException)thrown;
        assertThat(userException.user).isNotNull();
    }

    @Test
    public void minimizeAddressTest() throws UserException {
        for (Address address : testAddresses) {
            User user = createUserWithAddress(address.original);
            assertThat(user.getAddress()).isEqualTo(address.expected);
        }
    }

    @Test
    public void isBerkeleyTest() throws UserException {

        for (City city : testCities) {
            User user = createUserWithCity(city.original);
            assertThat(user.getCity()).isEqualTo(city.expected);
        }
    }
}
