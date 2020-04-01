/******************************************************************************
 * Copyright (c) 2020 helpberkeley.org
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
 ******************************************************************************/

package org.helpberkeley.memberdata;

import com.cedarsoftware.util.io.JsonObject;

import java.util.*;

/**
 * User representation
 */
public class User {

    // FIX THIS, DS: Hardwired names that break if Discourse
    //               changes any them. Unlikely though.
    //
    static final String NAME_FIELD = "name";
    static final String USERNAME_FIELD = "username";
    static final String ID_FIELD = "id";

    // FIX THIS, DS: Hardwired indexes that break if we
    //               change the meaning of any of these
    //               user fields. No sensitivty here to
    //               the field labels shown to the users
    //               of the site.
    static final String USER_FIELDS_FIELD = "user_fields";
    static final String ADDRESS_USER_FIELD = "1";
    static final String PHONE_NUMBER_USER_FIELD = "2";
    static final String NEIGHBORHOOD_USER_FIELD = "3";
    static final String CITY_USER_FIELD = "6";

    // Column names for report generation
    //
    static final String NAME_COLUMN = "Name";
    static final String USERNAME_COLUMN = "User Name";
    static final String ADDRESS_COLUMN = "Address";
    static final String CITY_COLUMN = "City";
    static final String PHONE_NUMBER_COLUMN = "Phone #";
    static final String NEIGHBORHOOD_COLUMN = "Neighborhood";

    static final String BERKELEY = "Berkeley";

    // Audit error strings
    static final String AUDIT_ERROR_MISSING_NAME = "missing name";
    static final String AUDIT_ERROR_MISSING_USERNAME = "missing user name";
    static final String AUDIT_ERROR_MISSING_ADDRESS = "missing address";
    static final String AUDIT_ERROR_MISSING_CITY = "missing city";
    static final String AUDIT_ERROR_MISSING_PHONE = "missing phone";
    static final String AUDIT_ERROR_MISSING_NEIGHBORHOOD = "missing neighborhood";
    static final String AUDIT_ERROR_CITY_IS_NOT_BERKELEY = "City is not " + BERKELEY + ": ";

    static final String NOT_PROVIDED = "none";

    private String name;
    private String userName;
    private long id;
    private String address;
    private String city;
    private String phoneNumber;
    private String neighborhood;
    private final Set<String> groupMembership = new HashSet<>();
    private final List<String> dataErrors = new ArrayList<>();


    // Getters added to make Comparators easy to define
    public String getName() {
        return name == null ? NOT_PROVIDED : name;
    }

    public String getUserName() {
        return userName == null ? NOT_PROVIDED : userName;
    }

    public long getId() {
        return id;
    }

    public String getAddress() {
        return address == null ? NOT_PROVIDED : address;
    }

    public String getCity() {
        return city == null ? NOT_PROVIDED : city;
    }

    public String getPhoneNumber() {
        return phoneNumber == null ? NOT_PROVIDED : phoneNumber;
    }

    public String getNeighborhood() {
        return neighborhood == null ? NOT_PROVIDED : neighborhood;
    }

    public List<String> getDataErrors() {
        return dataErrors;
    }

    private User(
        final String name,
        final String userName,
        final long id,
        final String address,
        final String city,
        final String phoneNumber,
        final String neighborhood) {

        this.name = name;
        this.userName = userName;
        this.id = id;
        this.address = address;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.neighborhood = neighborhood;
    }

    void addGroups(List<Group> groups) {

        for (Group group : groups) {
            if (group.hasUserName(userName)) {
                groupMembership.add(group.name);
            }
        }
    }

    // Test entry point
    void addGroup(String group) {
        assert ! groupMembership.contains(group) : group;
        groupMembership.add(group);
    }

    boolean isConsumer() {
        return groupMembership.contains(Group.CONSUMER);
    }

    boolean isDispatcher() {
        return groupMembership.contains(Group.DISPATCHER);
    }

    boolean isDriver() {
        return groupMembership.contains(Group.DRIVER);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(NAME_COLUMN);
        builder.append("=");
        builder.append(name == null ? NOT_PROVIDED : name);
        builder.append(':');

        builder.append(USERNAME_COLUMN);
        builder.append("=");
        builder.append(userName == null ? NOT_PROVIDED : name);
        builder.append(':');

        builder.append(ID_FIELD);
        builder.append("=");
        builder.append(id);
        builder.append(':');

        builder.append(ADDRESS_COLUMN);
        builder.append("=");
        builder.append(address == null ? NOT_PROVIDED : name);
        builder.append(':');

        builder.append(CITY_COLUMN);
        builder.append("=");
        builder.append(city == null ? NOT_PROVIDED : name);
        builder.append(':');

        builder.append(PHONE_NUMBER_COLUMN);
        builder.append("=");
        builder.append(phoneNumber == null ? NOT_PROVIDED : name);
        builder.append(':');

        builder.append(NEIGHBORHOOD_COLUMN);
        builder.append("=");
        builder.append(neighborhood == null ? NOT_PROVIDED : name);
        builder.append(':');

        return builder.toString();
    }

    private void auditNullFields() {

        if (name == null) {
            dataErrors.add(AUDIT_ERROR_MISSING_NAME);
        }

        if (userName == null) {
            dataErrors.add(AUDIT_ERROR_MISSING_USERNAME);
        }

        if (address == null) {
            dataErrors.add(AUDIT_ERROR_MISSING_ADDRESS);
        }

        if (city == null) {
            dataErrors.add(AUDIT_ERROR_MISSING_CITY);
        }

        if (phoneNumber == null) {
            dataErrors.add(AUDIT_ERROR_MISSING_PHONE);
        }

        if (neighborhood == null) {
            dataErrors.add(AUDIT_ERROR_MISSING_NEIGHBORHOOD);
        }
    }

    // Must be insensitive to null data
    private void normalizeData() {
        removeCommas();
        auditAndNormalizePhoneNumber();
        auditAndNormalizeCity();
        minimizeAddress();
    }

    // Must be insensitive to null data
    private void removeCommas() {
        char oldChar = ',';
        char newChar = ' ';

        if (name != null) {
            name = name.replace(oldChar, newChar);
        }
        if (userName != null) {
            userName = userName.replace(oldChar, newChar);
        }
        if (address != null) {
            address = address.replace(oldChar, newChar);
        }
        if (city != null) {
            city = city.replace(oldChar, newChar);
        }
        if (phoneNumber != null) {
            phoneNumber = phoneNumber.replace(oldChar, newChar);
        }
        if (neighborhood != null) {
            neighborhood = neighborhood.replace(oldChar, newChar);
        }
    }

    // Must be insensitive to null data
    private void auditAndNormalizePhoneNumber() {
        if (phoneNumber == null) {
            return;
        }

        String digits = phoneNumber.replaceAll("[^\\d]", "");

        switch (digits.length()) {
            case 7:
                dataErrors.add("Assuming 510. Missing area code");
                digits = "510" + digits;
                break;
            case 10:
                break;
            case 11:
                if (! digits.startsWith("1")) {
                    dataErrors.add("Cannot parse phone number: " + phoneNumber);
                } else {
                    digits = digits.substring(1);
                }
                break;
            default:
                dataErrors.add("Cannot parse phone number: " + phoneNumber);
                return;
        }

        assert digits.length() == 10 : this;
        String newPhoneNumber = digits.substring(0, 3);
        newPhoneNumber += "-";
        newPhoneNumber += digits.substring(3, 6);
        newPhoneNumber += "-";
        newPhoneNumber += digits.substring(6, 10);

        phoneNumber = newPhoneNumber;

    }

    // must be insensitive to null data
    private void auditAndNormalizeCity() {
        if (city == null) {
            return;
        }

        if (cityIsBerkeley()) {
            city = BERKELEY;
        }
    }

    // Try to remove the city portion of the address, if it is Berkeley.
    private void minimizeAddress() {

        if (address == null) {
            return;
        }

        String lowerCase = address.toLowerCase();

        // Berkeley Way is a street in Berkeley
        if (lowerCase.contains("berkeley way")) {
            return;
        }

        int index = lowerCase.indexOf(" berkeley");
        if (index == -1) {
            return;
        }

        String newAddress = address.substring(0, index);

        // FIX THIS, DS: what kind of validation can be done here
        //               to prevent chopping off street address info?

        address = newAddress;
    }


    private boolean cityIsBerkeley() {

        // Convert to lower case
        String cityName = city.toLowerCase();

        // Remove leading trailing whitespace
        cityName = cityName.trim();

        // Remove all vowels by y
        cityName = cityName.replaceAll("[aeiou]", "");

        // Replace all repeating characters with single characters - e.g. bkkllyy -> bkly
        cityName = cityName.replaceAll("(.)\\1+","$1");

        // Look for the expected spelling and some possible misspellings:

        switch (cityName) {
            case "brkly":
            case "brkyl":
            case "brlky":
            case "brlyk":

            case "bkrly":
            case "bkryl":
            case "blrky":
            case "blryk":

            case "bkly":
            case "blky":

            case "bkl":
            case "bky":

            case "blk":
            case "bly":
                return true;
        }

        return false;
    }

    static User createUser(Map<String, Object> fieldMap) throws UserException {

        String name = (String) fieldMap.get(NAME_FIELD);
        String userName = (String) fieldMap.get(USERNAME_FIELD);
        Long id = (Long) fieldMap.get(ID_FIELD);

        JsonObject obj = (JsonObject)fieldMap.get(USER_FIELDS_FIELD);

        // FIX THIS, DS: refactor to remove the cast
        LinkedHashMap<String, Object> userFieldsMap = (LinkedHashMap<String, Object>)obj;
        String address = null;
        String city = null;
        String phoneNumber = null;
        String neighborhood = null;

        if (userFieldsMap != null) {
            address = (String) userFieldsMap.get(ADDRESS_USER_FIELD);
            city = (String) userFieldsMap.get(CITY_USER_FIELD);
            phoneNumber = (String) userFieldsMap.get(PHONE_NUMBER_USER_FIELD);
            neighborhood = (String) userFieldsMap.get(NEIGHBORHOOD_USER_FIELD);
        }

        User user = new User(name, userName, id, address, city, phoneNumber, neighborhood);
        user.auditNullFields();
        user.normalizeData();

        if (! user.dataErrors.isEmpty()) {
            throw new UserException(user);
        }

        return user;
    }

    // CTOR for test usage
    static User createUser(
            final String name,
            final String userName,
            final long id,
            final String address,
            final String city,
            final String phoneNumber,
            final String neighborhood) throws UserException {


        User user = new User(name, userName, id, address, city, phoneNumber, neighborhood);
        user.auditNullFields();
        user.normalizeData();

        if (! user.dataErrors.isEmpty()) {
            throw new UserException(user);
        }

        return user;
    }

    @Override
    public boolean equals(Object obj) {

        return (obj instanceof  User)
                && name.equals(((User)obj).name)
                && userName.equals(((User)obj).userName)
                && (id == ((User)obj).id)
                && address.equals(((User)obj).address)
                && phoneNumber.equals(((User)obj).phoneNumber)
                && neighborhood.equals(((User)obj).neighborhood)
                && groupMembership.equals(((User)obj).groupMembership);
    }
}
