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
    static final String ALT_PHONE_NUMBER_COLUMN = "Phone2 #";
    static final String NEIGHBORHOOD_COLUMN = "Neighborhood";
    static final String CONSUMER_COLUMN = "Consumer";
    static final String DRIVER_COLUMN = "Driver";
    static final String SPECIALIST_COLUMN = "Specialist";
    static final String DISPATCHER_COLUMN = "Dispatcher";
    static final String CREATED_AT_COLUMN = "Created";
    static final String APARTMENT_COLUMN = "Apartment";
    static final String CONSUMER_REQUEST_COLUMN = "Consumer Request";
    static final String VOLUNTEER_REQUEST_COLUMN = "Volunteer Request";
    static final String BHS_COLUMN = "BHS";
    static final String HELPLINE_COLUMN = "HelpLine";
    static final String SITELINE_COLUMN = "SiteLine";
    static final String INREACH_COLUMN = "InReach";
    static final String OUTREACH_COLUMN = "OutReach";
    static final String MARKETING_COLUMN = "Marketing";
    static final String MODERATORS_COLUMN = "Moderator";
    static final String WORKFLOW_COLUMN = "Workflow";
    static final String REFERRAL_COLUMN = "Referral";
    static final String VOICEONLY_COLUMN = "VoiceOnly";
    static final String TRUST_LEVEL_4_COLUMN = "TrustLevel4";
    static final String CUSTOMER_INFO_COLUMN = "CustomerInfo";
    static final String ADVISOR_COLUMN = "Advisor";
    static final String COORDINATOR_COLUMN = "Coordinator";
    static final String ADMIN_COLUMN = "Admin";

    static final String ERROR_PRIMARY_PHONE_MISSING_AREA_CODE = "Primary phone missing area code, assuming 510";
    static final String ERROR_PRIMARY_PHONE_CANNOT_PARSE_PHONE = "Cannot parse primary phone number";
    static final String ERROR_SECOND_PHONE_MISSING_AREA_CODE = "Second phone missing area code, assuming 510";
    static final String ERROR_SECOND_PHONE_CANNOT_PARSE_PHONE = "Cannot parse second phone number";

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
    private String referral;
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

    public String getReferral() {
        return referral == null ? NOT_PROVIDED : referral;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getSimpleCreateTime() {
        return ZonedDateTime.parse(createTime).format(TIME_FORMATTER);
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
        final String referral)  {

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
        this.referral = referral;
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

    Boolean isBHS() {
        return groupMembership.contains(Constants.GROUP_BHS);
    }

    Boolean isHelpLine() {
        return groupMembership.contains(Constants.GROUP_HELPLINE);
    }

    Boolean isSiteLine() {
        return groupMembership.contains(Constants.GROUP_SITELINE);
    }

    Boolean isInReach() {
        return groupMembership.contains(Constants.GROUP_INREACH);
    }

    Boolean isOutReach() {
        return groupMembership.contains(Constants.GROUP_OUTREACH);
    }

    Boolean isMarketing() {
        return groupMembership.contains(Constants.GROUP_MARKETING);
    }

    Boolean isModerator() {
        return groupMembership.contains(Constants.GROUP_MODERATORS);
    }

    Boolean isWorkflow() {
        return groupMembership.contains(Constants.GROUP_WORKFLOW);
    }

    Boolean isVoiceOnly() {
        return groupMembership.contains(Constants.GROUP_VOICEONLY);
    }

    Boolean isTrustLevel4() {
        return groupMembership.contains(Constants.GROUP_TRUST_LEVEL_4);
    }

    Boolean isCustomerInfo() {
        return groupMembership.contains(Constants.GROUP_CUSTOMER_INFO);
    }

    Boolean isAdvisor() {
        return groupMembership.contains(Constants.GROUP_ADVISOR);
    }

    Boolean isCoordinator() {
        return groupMembership.contains(Constants.GROUP_COORDINATOR);
    }

    Boolean isAdmin() {
        return groupMembership.contains(Constants.GROUP_ADMIN);
    }

    @Override
    public String toString() {

        String builder = NAME_COLUMN +
                "=" +
                (name == null ? NOT_PROVIDED : name) +
                ':' +
                USERNAME_COLUMN +
                "=" +
                (userName == null ? NOT_PROVIDED : userName) +
                ':' +
                ID_FIELD +
                "=" +
                id +
                ':' +
                ADDRESS_COLUMN +
                "=" +
                (address == null ? NOT_PROVIDED : address) +
                ':' +
                CITY_COLUMN +
                "=" +
                (city == null ? NOT_PROVIDED : city) +
                ':' +
                PHONE_NUMBER_COLUMN +
                "=" +
                (phoneNumber == null ? NOT_PROVIDED : phoneNumber) +
                ':' +
                ALT_PHONE_NUMBER_COLUMN +
                "=" +
                (altPhoneNumber == null ? NOT_PROVIDED : altPhoneNumber) +
                ':' +
                NEIGHBORHOOD_COLUMN +
                "=" +
                (neighborhood == null ? NOT_PROVIDED : neighborhood) +
                ':' +
                Constants.GROUP_CONSUMERS +
                "=" +
                isConsumer() +
                ':' +
                Constants.GROUP_DISPATCHERS +
                "=" +
                isDispatcher() +
                ':' +
                Constants.GROUP_DRIVERS +
                "=" +
                isDriver() +
                ':' +
                APARTMENT_COLUMN +
                "=" +
                isApartment() +
                ':' +
                REFERRAL_COLUMN +
                "=" +
                (referral == null ? NOT_PROVIDED : referral) +
                ':' +
                CONSUMER_REQUEST_COLUMN +
                "=" +
                hasConsumerRequest() +
                ':' +
                VOICEONLY_COLUMN +
                "=" +
                isVoiceOnly() +
                ':' +
                VOLUNTEER_REQUEST_COLUMN +
                "=" +
                (volunteerRequest == null ? NOT_PROVIDED : volunteerRequest) +
                ':' +
                Constants.GROUP_SPECIALISTS +
                "=" +
                isSpecialist() +
                ':' +
                Constants.GROUP_BHS +
                "=" +
                isBHS() +
                ':' +
                Constants.GROUP_HELPLINE +
                "=" +
                isHelpLine() +
                ':' +
                Constants.GROUP_SITELINE +
                "=" +
                isSiteLine() +
                ':' +
                Constants.GROUP_INREACH +
                "=" +
                isInReach() +
                ':' +
                Constants.GROUP_OUTREACH +
                "=" +
                isOutReach() +
                ':' +
                Constants.GROUP_MARKETING +
                "=" +
                isMarketing() +
                ':' +
                Constants.GROUP_MODERATORS +
                "=" +
                isModerator() +
                ':' +
                Constants.GROUP_WORKFLOW +
                "=" +
                isWorkflow() +
                ':' +
                Constants.GROUP_CUSTOMER_INFO +
                "=" +
                isCustomerInfo() +
                ':' +
                Constants.GROUP_ADVISOR +
                "=" +
                isAdvisor() +
                ':' +
                Constants.GROUP_COORDINATOR +
                "=" +
                isCoordinator() +
                ':' +
                Constants.GROUP_ADMIN +
                "=" +
                isAdmin() +
                ':' +
                REFERRAL_COLUMN +
                '=' +
                referral
                + ':' +
                Constants.COLUMN_CREATE_TIME +
                "=" +
                getSimpleCreateTime() +
                ':';
        return builder;
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
        normalizeReferral();
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
        if (referral != null) {
            referral = referral.replace(oldChar, newChar);
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
        if (referral != null) {
            referral = referral.replace(oldChar, newChar);
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
        if (referral != null) {
            referral = referral.strip();
        }
    }

    // Must be insensitive to null data
    private void auditAndNormalizePhoneNumber() {
        assert phoneNumber != null;

        String digits = phoneNumber.replaceAll("[^\\d]", "");

        switch (digits.length()) {
            case 7:
                dataErrors.add(ERROR_PRIMARY_PHONE_MISSING_AREA_CODE);
                digits = "510" + digits;
                break;
            case 10:
                break;
            case 11:
                if (! digits.startsWith("1")) {
                    dataErrors.add(ERROR_PRIMARY_PHONE_CANNOT_PARSE_PHONE);
                    return;
                } else {
                    digits = digits.substring(1);
                }
                break;
            default:
                dataErrors.add(ERROR_PRIMARY_PHONE_CANNOT_PARSE_PHONE);
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
        if ((altPhoneNumber == null) || altPhoneNumber.trim().isEmpty()){
            altPhoneNumber = NOT_PROVIDED;
            return;
        }

        String digits = altPhoneNumber.replaceAll("[^\\d]", "");

        switch (digits.length()) {
            case 7:
                dataErrors.add(ERROR_SECOND_PHONE_MISSING_AREA_CODE);
                digits = "510" + digits;
                break;
            case 10:
                break;
            case 11:
                if (! digits.startsWith("1")) {
                    dataErrors.add(ERROR_SECOND_PHONE_CANNOT_PARSE_PHONE);
                    return;
                } else {
                    digits = digits.substring(1);
                }
                break;
            default:
                dataErrors.add(ERROR_SECOND_PHONE_CANNOT_PARSE_PHONE);
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

    // can arrive either as null, "", or a value
    private void normalizeReferral() {

        if ((referral == null) || (referral.isEmpty())) {
            referral = NOT_PROVIDED;
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
            final String referral,
            final String... groups) throws UserException {


        User user = new User(name, userName, id, address, city, phoneNumber, altPhoneNumber,
                neighborhood, createdAt, apartment, consumerRequest, volunteerRequest, referral);
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
            final String referral,
            final List<String> groups) throws UserException {


        User user = new User(name, userName, id, address, city, phoneNumber, altPhoneNumber,
                neighborhood, createdAt, apartment, consumerRequest, volunteerRequest, referral);
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

    static String reportCSVHeaders() {

        return ID_COLUMN + Constants.CSV_SEPARATOR
                + CREATED_AT_COLUMN + Constants.CSV_SEPARATOR
                + NAME_COLUMN + Constants.CSV_SEPARATOR
                + USERNAME_COLUMN + Constants.CSV_SEPARATOR
                + PHONE_NUMBER_COLUMN + Constants.CSV_SEPARATOR
                + ALT_PHONE_NUMBER_COLUMN + Constants.CSV_SEPARATOR
                + NEIGHBORHOOD_COLUMN + Constants.CSV_SEPARATOR
                + CITY_COLUMN + Constants.CSV_SEPARATOR
                + ADDRESS_COLUMN + Constants.CSV_SEPARATOR
                + APARTMENT_COLUMN + Constants.CSV_SEPARATOR
                + REFERRAL_COLUMN + Constants.CSV_SEPARATOR
                + CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + VOICEONLY_COLUMN + Constants.CSV_SEPARATOR
                + DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + DISPATCHER_COLUMN + Constants.CSV_SEPARATOR
                + WORKFLOW_COLUMN + Constants.CSV_SEPARATOR
                + INREACH_COLUMN + Constants.CSV_SEPARATOR
                + OUTREACH_COLUMN + Constants.CSV_SEPARATOR
                + HELPLINE_COLUMN + Constants.CSV_SEPARATOR
                + SITELINE_COLUMN + Constants.CSV_SEPARATOR
                + MARKETING_COLUMN + Constants.CSV_SEPARATOR
                + MODERATORS_COLUMN + Constants.CSV_SEPARATOR
                + TRUST_LEVEL_4_COLUMN + Constants.CSV_SEPARATOR
                + SPECIALIST_COLUMN + Constants.CSV_SEPARATOR
                + BHS_COLUMN + Constants.CSV_SEPARATOR
                + CUSTOMER_INFO_COLUMN + Constants.CSV_SEPARATOR
                + ADVISOR_COLUMN + Constants.CSV_SEPARATOR
                + COORDINATOR_COLUMN + Constants.CSV_SEPARATOR
                + ADMIN_COLUMN + Constants.CSV_SEPARATOR
                + CONSUMER_REQUEST_COLUMN + Constants.CSV_SEPARATOR
                + VOLUNTEER_REQUEST_COLUMN + Constants.CSV_SEPARATOR
                + "\n";
    }

    static String rawCSVHeaders() {

        return ID_COLUMN + Constants.CSV_SEPARATOR
                + NAME_COLUMN + Constants.CSV_SEPARATOR
                + USERNAME_COLUMN + Constants.CSV_SEPARATOR
                + PHONE_NUMBER_COLUMN + Constants.CSV_SEPARATOR
                + ALT_PHONE_NUMBER_COLUMN + Constants.CSV_SEPARATOR
                + NEIGHBORHOOD_COLUMN + Constants.CSV_SEPARATOR
                + CITY_COLUMN + Constants.CSV_SEPARATOR
                + ADDRESS_COLUMN + Constants.CSV_SEPARATOR
                + CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + VOICEONLY_COLUMN + Constants.CSV_SEPARATOR
                + DISPATCHER_COLUMN + Constants.CSV_SEPARATOR
                + DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + CREATED_AT_COLUMN + Constants.CSV_SEPARATOR
                + APARTMENT_COLUMN + Constants.CSV_SEPARATOR
                + REFERRAL_COLUMN + Constants.CSV_SEPARATOR
                + CONSUMER_REQUEST_COLUMN + Constants.CSV_SEPARATOR
                + VOLUNTEER_REQUEST_COLUMN + Constants.CSV_SEPARATOR
                + SPECIALIST_COLUMN + Constants.CSV_SEPARATOR
                + BHS_COLUMN + Constants.CSV_SEPARATOR
                + HELPLINE_COLUMN + Constants.CSV_SEPARATOR
                + SITELINE_COLUMN + Constants.CSV_SEPARATOR
                + INREACH_COLUMN + Constants.CSV_SEPARATOR
                + OUTREACH_COLUMN + Constants.CSV_SEPARATOR
                + MARKETING_COLUMN + Constants.CSV_SEPARATOR
                + MODERATORS_COLUMN + Constants.CSV_SEPARATOR
                + TRUST_LEVEL_4_COLUMN + Constants.CSV_SEPARATOR
                + WORKFLOW_COLUMN + Constants.CSV_SEPARATOR
                + CUSTOMER_INFO_COLUMN + Constants.CSV_SEPARATOR
                + ADVISOR_COLUMN + Constants.CSV_SEPARATOR
                + COORDINATOR_COLUMN + Constants.CSV_SEPARATOR
                + ADMIN_COLUMN + Constants.CSV_SEPARATOR
                + "\n";
    }

    String rawToCSV() {

        String csvData = getId() + Constants.CSV_SEPARATOR +
                getName() + Constants.CSV_SEPARATOR +
                getUserName() + Constants.CSV_SEPARATOR +
                getPhoneNumber() + Constants.CSV_SEPARATOR +
                getAltPhoneNumber() + Constants.CSV_SEPARATOR +
                getNeighborhood() + Constants.CSV_SEPARATOR +
                getCity() + Constants.CSV_SEPARATOR +
                getAddress() + Constants.CSV_SEPARATOR +
                isConsumer() + Constants.CSV_SEPARATOR +
                isVoiceOnly() + Constants.CSV_SEPARATOR +
                isDispatcher() + Constants.CSV_SEPARATOR +
                isDriver() + Constants.CSV_SEPARATOR +
                getCreateTime() + Constants.CSV_SEPARATOR +
                isApartment() + Constants.CSV_SEPARATOR +
                getReferral() + Constants.CSV_SEPARATOR +
                hasConsumerRequest() + Constants.CSV_SEPARATOR +
                getVolunteerRequest() + Constants.CSV_SEPARATOR +
                isSpecialist() + Constants.CSV_SEPARATOR +
                isBHS() + Constants.CSV_SEPARATOR +
                isHelpLine() + Constants.CSV_SEPARATOR +
                isSiteLine() + Constants.CSV_SEPARATOR +
                isInReach() + Constants.CSV_SEPARATOR +
                isOutReach() + Constants.CSV_SEPARATOR +
                isMarketing() + Constants.CSV_SEPARATOR +
                isModerator() + Constants.CSV_SEPARATOR +
                isTrustLevel4() + Constants.CSV_SEPARATOR +
                isWorkflow() + Constants.CSV_SEPARATOR +
                isCustomerInfo() + Constants.CSV_SEPARATOR +
                isAdvisor() + Constants.CSV_SEPARATOR +
                isCoordinator() + Constants.CSV_SEPARATOR +
                isAdmin() + Constants.CSV_SEPARATOR +
                '\n';
        return csvData;
    }

    String reportToCSV() {

        String csvData = getId() + Constants.CSV_SEPARATOR +
                getSimpleCreateTime() + Constants.CSV_SEPARATOR +
                getName() + Constants.CSV_SEPARATOR +
                getUserName() + Constants.CSV_SEPARATOR +
                getPhoneNumber() + Constants.CSV_SEPARATOR +
                getAltPhoneNumber() + Constants.CSV_SEPARATOR +
                getNeighborhood() + Constants.CSV_SEPARATOR +
                getCity() + Constants.CSV_SEPARATOR +
                getAddress() + Constants.CSV_SEPARATOR +
                isApartment() + Constants.CSV_SEPARATOR +
                getReferral() + Constants.CSV_SEPARATOR +
                isConsumer() + Constants.CSV_SEPARATOR +
                isVoiceOnly() + Constants.CSV_SEPARATOR +
                isDriver() + Constants.CSV_SEPARATOR +
                isDispatcher() + Constants.CSV_SEPARATOR +
                isWorkflow() + Constants.CSV_SEPARATOR +
                isInReach() + Constants.CSV_SEPARATOR +
                isOutReach() + Constants.CSV_SEPARATOR +
                isHelpLine() + Constants.CSV_SEPARATOR +
                isSiteLine() + Constants.CSV_SEPARATOR +
                isMarketing() + Constants.CSV_SEPARATOR +
                isModerator() + Constants.CSV_SEPARATOR +
                isTrustLevel4() + Constants.CSV_SEPARATOR +
                isSpecialist() + Constants.CSV_SEPARATOR +
                isBHS() + Constants.CSV_SEPARATOR +
                isCustomerInfo() + Constants.CSV_SEPARATOR +
                isAdvisor() + Constants.CSV_SEPARATOR +
                isCoordinator() + Constants.CSV_SEPARATOR +
                isAdmin() + Constants.CSV_SEPARATOR +
                hasConsumerRequest() + Constants.CSV_SEPARATOR +
                getVolunteerRequest() + Constants.CSV_SEPARATOR +
                '\n';
        return csvData;
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
        if (! referral.equals(otherObj.referral)) {
            return false;
        }

        return true;
    }
}
