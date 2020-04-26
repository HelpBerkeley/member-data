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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * User representation
 */
public class User {

    static final String ID_FIELD = "id";

    // Column names for report generation
    //
    static final String ID_COLUMN = "ID";
    static final String NAME_COLUMN = "Name";
    static final String USERNAME_COLUMN = "User Name";
    static final String ADDRESS_COLUMN = "Address";
    static final String CITY_COLUMN = "City";
    static final String PHONE_NUMBER_COLUMN = "Phone #";
    static final String ALT_PHONE_NUMBER_COLUMN = "Second Phone #";
    static final String NEIGHBORHOOD_COLUMN = "Neighborhood";
    static final String CONSUMER_COLUMN = "Consumer";
    static final String DRIVER_COLUMN = "Driver";
    static final String SPECIALIST_COLUMN = "Specialist";
    static final String DISPATCHER_COLUMN = "Dispatcher";
    static final String CREATED_AT_COLUMN = "Created";
    static final String APARTMENT_COLUMN = "Apartment";
    static final String CONSUMER_REQUEST_COLUMN = "Consumer Request";
    static final String VOLUNTEER_REQUEST_COLUMN = "Volunteer Request";
    static final String EMAIL_COLUMN = "Email";

    static final String ERROR_MISSING_AREA_CODE = "Phone missing area code, assuming 510";
    static final String ERROR_CANNOT_PARSE_PHONE = "Cannot parse phone number";

    // Audit error strings
    static final String AUDIT_ERROR_NEIGHBORHOOD_UNKNOWN = "Neighborhood unknown";

    static final String NOT_PROVIDED = "none";

    static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private String name;
    private String userName;
    private final long id;
    private String address;
    private String city;
    private String phoneNumber;
    private String altPhoneNumber;
    private String neighborhood;
    private final String createTime;
    private final Boolean apartment;
    private final Boolean consumerRequest;
    private String volunteerRequest;
    private final String email;
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

    public String getAltPhoneNumber() {
        return altPhoneNumber == null ? NOT_PROVIDED : altPhoneNumber;
    }

    public String getNeighborhood() {
        return neighborhood == null ? NOT_PROVIDED : neighborhood;
    }

    public  String getVolunteerRequest() {
        return volunteerRequest == null ? NOT_PROVIDED : volunteerRequest;
    }

    public Boolean isApartment() {
        return apartment;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getSimpleCreateTime() {
        return ZonedDateTime.parse(createTime).format(TIME_FORMATTER);
    }

    public String getEmail()
    {
       return email;
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
        final String altPhoneNumber,
        final String neighborhood,
        final String createTime,
        final Boolean apartment,
        final Boolean consumerRequest,
        final String volunteerRequest,
        final String email) {

        this.name = name;
        this.userName = userName;
        this.id = id;
        this.address = address;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.altPhoneNumber = altPhoneNumber;
        this.neighborhood = neighborhood;
        this.createTime = createTime;
        this.apartment = apartment;
        this.consumerRequest = consumerRequest;
        this.volunteerRequest = volunteerRequest;
        this.email = email;
    }

    Boolean hasConsumerRequest() {
        return consumerRequest;
    }

    Boolean isConsumer() {
        return groupMembership.contains(Constants.GROUP_CONSUMERS);
    }

    Boolean isDispatcher() {
        return groupMembership.contains(Constants.GROUP_DISPATCHERS);
    }

    Boolean isDriver() {
        return groupMembership.contains(Constants.GROUP_DRIVERS);
    }
    Boolean isSpecialist() {
        return groupMembership.contains(Constants.GROUP_SPECIALISTS);
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
        builder.append(userName == null ? NOT_PROVIDED : userName);
        builder.append(':');

        builder.append(ID_FIELD);
        builder.append("=");
        builder.append(id);
        builder.append(':');

        builder.append(ADDRESS_COLUMN);
        builder.append("=");
        builder.append(address == null ? NOT_PROVIDED : address);
        builder.append(':');

        builder.append(CITY_COLUMN);
        builder.append("=");
        builder.append(city == null ? NOT_PROVIDED : city);
        builder.append(':');

        builder.append(PHONE_NUMBER_COLUMN);
        builder.append("=");
        builder.append(phoneNumber == null ? NOT_PROVIDED :phoneNumber);
        builder.append(':');

        builder.append(ALT_PHONE_NUMBER_COLUMN);
        builder.append("=");
        builder.append(altPhoneNumber == null ? NOT_PROVIDED :altPhoneNumber);
        builder.append(':');

        builder.append(NEIGHBORHOOD_COLUMN);
        builder.append("=");
        builder.append(neighborhood == null ? NOT_PROVIDED : neighborhood);
        builder.append(':');

        builder.append(Constants.GROUP_CONSUMERS);
        builder.append("=");
        builder.append(isConsumer());
        builder.append(':');

        builder.append(Constants.GROUP_DISPATCHERS);
        builder.append("=");
        builder.append(isDispatcher());
        builder.append(':');

        builder.append(Constants.GROUP_DRIVERS);
        builder.append("=");
        builder.append(isDriver());
        builder.append(':');

        builder.append(APARTMENT_COLUMN);
        builder.append("=");
        builder.append(isApartment());
        builder.append(':');

        builder.append(CONSUMER_REQUEST_COLUMN);
        builder.append("=");
        builder.append(hasConsumerRequest());
        builder.append(':');

        builder.append(VOLUNTEER_REQUEST_COLUMN);
        builder.append("=");
        builder.append(volunteerRequest == null ? NOT_PROVIDED : volunteerRequest);
        builder.append(':');

        builder.append(Constants.GROUP_SPECIALISTS);
        builder.append("=");
        builder.append(isSpecialist());
        builder.append(':');

        builder.append(Constants.COLUMN_EMAIL);
        builder.append("=");
        builder.append(getEmail());
        builder.append(':');

        builder.append(Constants.COLUMN_CREATE_TIME);
        builder.append("=");
        builder.append(getSimpleCreateTime());
        builder.append(':');

        return builder.toString();
    }

    private void auditNullFields() {

        assert name != null;
        assert userName != null;
        assert address != null;
        assert city != null;
        assert phoneNumber != null;
        assert neighborhood != null;
    }

    // Must be insensitive to null data
    private void normalizeData() {
        removeCommas();
        removeNewlines();
        removeLeadingTrailingWhitespace();
        auditAndNormalizePhoneNumber();
        auditAndNormalizeAltPhoneNumber();
        auditAndNormalizeCity();
        auditNeighborhood();
        minimizeAddress();
        normalizeVolunteerRequest();
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
        if (altPhoneNumber != null) {
            altPhoneNumber = altPhoneNumber.replace(oldChar, newChar);
        }
        if (neighborhood != null) {
            neighborhood = neighborhood.replace(oldChar, newChar);
        }
    }

    // Must be insensitive to null data
    private void removeNewlines() {
        char oldChar = '\n';
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
        if (altPhoneNumber != null) {
            altPhoneNumber = altPhoneNumber.replace(oldChar, newChar);
        }
        if (neighborhood != null) {
            neighborhood = neighborhood.replace(oldChar, newChar);
        }
    }

    // Must be insensitive to null data
    private void removeLeadingTrailingWhitespace() {

        if (name != null) {
            name = name.strip();
        }
        if (userName != null) {
            userName = userName.strip();
        }
        if (address != null) {
            address = address.strip();
        }
        if (city != null) {
            city = city.strip();
        }
        if (phoneNumber != null) {
            phoneNumber = phoneNumber.strip();
        }
        if (altPhoneNumber != null) {
            altPhoneNumber = altPhoneNumber.strip();
        }
        if (neighborhood != null) {
            neighborhood = neighborhood.strip();
        }
    }

    // Must be insensitive to null data
    private void auditAndNormalizePhoneNumber() {
        assert phoneNumber != null;

        String digits = phoneNumber.replaceAll("[^\\d]", "");

        switch (digits.length()) {
            case 7:
                dataErrors.add(ERROR_MISSING_AREA_CODE);
                digits = "510" + digits;
                break;
            case 10:
                break;
            case 11:
                if (! digits.startsWith("1")) {
                    dataErrors.add(ERROR_CANNOT_PARSE_PHONE);
                    return;
                } else {
                    digits = digits.substring(1);
                }
                break;
            default:
                dataErrors.add(ERROR_CANNOT_PARSE_PHONE);
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

    // Must be insensitive to null data
    private void auditAndNormalizeAltPhoneNumber() {
        if (altPhoneNumber == null) {
            altPhoneNumber = NOT_PROVIDED;
            return;
        }

        String digits = altPhoneNumber.replaceAll("[^\\d]", "");

        switch (digits.length()) {
            case 7:
                dataErrors.add(ERROR_MISSING_AREA_CODE);
                digits = "510" + digits;
                break;
            case 10:
                break;
            case 11:
                if (! digits.startsWith("1")) {
                    dataErrors.add(ERROR_CANNOT_PARSE_PHONE);
                    return;
                } else {
                    digits = digits.substring(1);
                }
                break;
            default:
                dataErrors.add(ERROR_CANNOT_PARSE_PHONE);
                return;
        }

        assert digits.length() == 10 : this;
        String newPhoneNumber = digits.substring(0, 3);
        newPhoneNumber += "-";
        newPhoneNumber += digits.substring(3, 6);
        newPhoneNumber += "-";
        newPhoneNumber += digits.substring(6, 10);

        altPhoneNumber = newPhoneNumber;

    }

    // must be insensitive to null data
    private void auditAndNormalizeCity() {
        assert city != null;

        if (cityIsBerkeley()) {
            city = Constants.BERKELEY;
        } else if (cityIsAlbany()) {
            city = Constants.ALBANY;
        } else if (cityIsKensington()) {
            city = Constants.KENSINGTON;
        }
    }

    // Try to remove the city portion of the address, if it is Berkeley.
    private void minimizeAddress() {

        assert address != null;

        String lowerCase = address.toLowerCase();

        // Berkeley Way is a street in Berkeley
        if (lowerCase.contains("berkeley way")) {
            return;
        }

        int index = lowerCase.indexOf(" berkeley");
        if (index == -1) {
            return;
        }

        // FIX THIS, DS: what kind of validation can be done here
        //               to prevent chopping off street address info?

        address = address.substring(0, index);
    }

    // must be insensitive to null data
    private void auditNeighborhood() {
        assert neighborhood != null;

        if (! isSupportedCity()) {
            return;
        }

        if (neighborhood.toLowerCase().trim().contains("unknown")) {
            dataErrors.add(AUDIT_ERROR_NEIGHBORHOOD_UNKNOWN);
        }
    }

    // can arrive either as null, "", or a value
    private void normalizeVolunteerRequest() {

        if ((volunteerRequest == null) || (volunteerRequest.isEmpty())) {
            volunteerRequest = NOT_PROVIDED;
        }
    }

    boolean isSupportedCity() {
        return cityIsBerkeley() || cityIsKensington() || cityIsAlbany();
    }

    private boolean cityIsBerkeley() {

        assert city != null;

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

    private boolean cityIsAlbany() {

        assert city != null;

        // Convert to lower case
        String cityName = city.toLowerCase();

        // Remove leading trailing whitespace
        cityName = cityName.trim();

        // Has to at least start with an 'a'
        if (! cityName.startsWith("a")) {
            return false;
        }

        // Remove all vowels by y
        cityName = cityName.replaceAll("[aeiou]", "");

        // Replace all repeating characters with single characters - e.g. bkkllyy -> bkly
        cityName = cityName.replaceAll("(.)\\1+","$1");

        // Look for the expected spelling and some possible misspellings:

        switch (cityName) {
            case "blny":
            case "lbny":
            case "lbn":
            case "bny":
            case "bnny":
                return true;
        }

        return false;
    }

    private boolean cityIsKensington() {

        assert city != null;

        // Convert to lower case
        String cityName = city.toLowerCase();

        // Remove leading trailing whitespace
        cityName = cityName.trim();

        // Has to at least start with an 'k'
        if (! cityName.startsWith("k")) {
            return false;
        }

        // Remove all vowels by y
        cityName = cityName.replaceAll("[aeiouy]", "");

        // Replace all repeating characters with single characters - e.g. bkkllxx -> bklx
        cityName = cityName.replaceAll("(.)\\1+","$1");

        // Look for the expected spelling and some possible misspellings:

        switch (cityName) {
            case "knsngtn":
            case "knsgntn":
            case "knstgn":
            case "knsgtn":
            case "knstn":
            case "knsntn":
            case "ksntn":
            case "ksgtn":
                return true;
        }

        return false;
    }

    // CTOR for test usage
    static User createUser(
            final String name,
            final String userName,
            final long id,
            final String address,
            final String city,
            final String phoneNumber,
            final String altPhoneNumber,
            final String neighborhood,
            final String createdAt,
            final Boolean apartment,
            final Boolean consumerRequest,
            final String volunteerRequest,
            final String email,
            final String... groups) throws UserException {


        User user = new User(name, userName, id, address, city, phoneNumber, altPhoneNumber,
                neighborhood, createdAt, apartment, consumerRequest, volunteerRequest, email);
        for (String group : groups) {
            assert ! user.groupMembership.contains(group) : group;
            user.groupMembership.add(group);
        }

        user.auditNullFields();
        user.normalizeData();

        if (! user.dataErrors.isEmpty()) {
            throw new UserException(user);
        }

        return user;
    }

    static User createUser(
            final String name,
            final String userName,
            final long id,
            final String address,
            final String city,
            final String phoneNumber,
            final String altPhoneNumber,
            final String neighborhood,
            final String createdAt,
            final Boolean apartment,
            final Boolean consumerRequest,
            final String volunteerRequest,
            final String email,
            final List<String> groups) throws UserException {


        User user = new User(name, userName, id, address, city, phoneNumber, altPhoneNumber,
                neighborhood, createdAt, apartment, consumerRequest, volunteerRequest, email);
        for (String group : groups) {
            assert ! user.groupMembership.contains(group) : group;
            user.groupMembership.add(group);
        }

        user.auditNullFields();
        user.normalizeData();

        if (! user.dataErrors.isEmpty()) {
            throw new UserException(user);
        }

        return user;
    }

    static String csvHeaders(final String separator) {

        return ID_COLUMN + separator
                + NAME_COLUMN + separator
                + USERNAME_COLUMN + separator
                + PHONE_NUMBER_COLUMN + separator
                + ALT_PHONE_NUMBER_COLUMN + separator
                + NEIGHBORHOOD_COLUMN + separator
                + CITY_COLUMN + separator
                + ADDRESS_COLUMN + separator
                + CONSUMER_COLUMN + separator
                + DISPATCHER_COLUMN + separator
                + DRIVER_COLUMN + separator
                + CREATED_AT_COLUMN + separator
                + APARTMENT_COLUMN + separator
                + CONSUMER_REQUEST_COLUMN + separator
                + VOLUNTEER_REQUEST_COLUMN + separator
                + SPECIALIST_COLUMN + separator
                + EMAIL_COLUMN + separator
                + "\n";
    }

    @Override
    public boolean equals(Object obj) {

        // It would look nicer to the eye to group all of this as a single return statement
        // with a bunch of && clauses. But debugging why a match is failing becomes
        // a PITA with that structure.

        if (!(obj instanceof User)) {
            return false;
        }

        User otherObj = (User)obj;

        if (! name.equals(otherObj.name)) {
            return false;
        }
        if (! userName.equals(otherObj.userName)) {
            return false;
        }
        if (! (id == otherObj.id)) {
            return false;
        }
        if (! address.equals(otherObj.address)) {
            return false;
        }
        if (! city.equals(otherObj.city)) {
            return false;
        }
        if (! phoneNumber.equals(otherObj.phoneNumber)) {
            return false;
        }
        if (! altPhoneNumber.equals(otherObj.altPhoneNumber)) {
            return false;
        }
        if (! neighborhood.equals(otherObj.neighborhood)) {
            return false;
        }
        if (! groupMembership.equals(otherObj.groupMembership)) {
            return false;
        }
        if (! createTime.equals(otherObj.createTime)) {
            return false;
        }
        if (! apartment.equals(otherObj.apartment)) {
            return false;
        }
        if (! consumerRequest.equals(otherObj.consumerRequest)) {
            return false;
        }
        if (! volunteerRequest.equals(otherObj.volunteerRequest)) {
            return false;
        }

        return true;
    }
}
