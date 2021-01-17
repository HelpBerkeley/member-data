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
    static final String PACKER_COLUMN = "Packer";
    static final String CREATED_AT_COLUMN = "Created";
    static final String CONDO_COLUMN = "Condo";
    static final String CONSUMER_REQUEST_COLUMN = "Consumer Request";
    static final String VOLUNTEER_REQUEST_COLUMN = "Volunteer Request";
    static final String BHS_COLUMN = "BHS";
    static final String HELPLINE_COLUMN = "HelpLine";
    static final String SITELINE_COLUMN = "SiteLine";
    static final String TRAINED_CUSTOMER_CARE_A_COLUMN = "trained_customer_care_A";
    static final String TRAINED_CUSTOMER_CARE_B_COLUMN = "trained_customer_care_B";
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
    static final String BOARD_COLUMN = "Board";
    static final String COORDINATOR_COLUMN = "Coordinator";
    static final String LIMITED_RUNS_COLUMN = "Limited";
    static final String AT_RISK_COLUMN = "at-risk";
    static final String BIKERS_COLUMN = "bikers";
    static final String OUT_COLUMN = "out";
    static final String TRAINED_DRIVER_COLUMN = "trained-driver";
    static final String EVENTS_DRIVER_COLUMN = "events_driver";
    static final String TRAINED_EVENT_DRIVER_COLUMN = "trained_events_driver";
    static final String GONE_COLUMN = "gone";
    static final String OTHER_DRIVERS_COLUMN = "other-drivers";
    static final String ADMIN_COLUMN = "Admin";
    static final String EMAIL_VERIFIED_COLUMN = "Verified";

    static final String SHORT_ID_COLUMN = "ID";
    static final String SHORT_CREATED_AT_COLUMN = "Created";
    static final String SHORT_NAME_COLUMN = "Name";
    static final String SHORT_USERNAME_COLUMN = "UserName";
    static final String SHORT_EMAIL_COLUMN = "EMail";
    static final String SHORT_PHONE_NUMBER_COLUMN = "Phone #";
    static final String SHORT_ALT_PHONE_NUMBER_COLUMN = "Phone2 #";
    static final String SHORT_NEIGHBORHOOD_COLUMN = "Neighborhood";
    static final String SHORT_CITY_COLUMN = "City";
    static final String SHORT_ADDRESS_COLUMN = "Address";
    static final String SHORT_CONDO_COLUMN = "Condo";
    static final String SHORT_REFERRAL_COLUMN = "Refer";
    static final String SHORT_CONSUMER_COLUMN = "Consumer";
    static final String SHORT_VOICEONLY_COLUMN = "Voiceonly";
    static final String SHORT_DRIVER_COLUMN = "Driver";
    static final String SHORT_DISPATCHER_COLUMN = "Dispatcher";
    static final String SHORT_WORKFLOW_COLUMN = "Workflow";
    static final String SHORT_INREACH_COLUMN = "Inreach";
    static final String SHORT_OUTREACH_COLUMN = "Outreach";
    static final String SHORT_HELPLINE_COLUMN = "HelpLine";
    static final String SHORT_SITELINE_COLUMN = "SiteLine";
    static final String SHORT_TRAINED_CUSTOMER_CARE_A_COLUMN = "trainedCCA";
    static final String SHORT_TRAINED_CUSTOMER_CARE_B_COLUMN = "trainedCCB";
    static final String SHORT_MARKETING_COLUMN = "Marktg";
    static final String SHORT_MODERATORS_COLUMN = "Mods";
    static final String SHORT_TRUST_LEVEL_4_COLUMN = "Trust4";
    static final String SHORT_SPECIALIST_COLUMN = "Specs";
    static final String SHORT_PACKER_COLUMN = "Packer";
    static final String SHORT_BHS_COLUMN = "BHS";
    static final String SHORT_CUSTOMER_INFO_COLUMN = "CustInf";
    static final String SHORT_ADVISOR_COLUMN = "Advsr";
    static final String SHORT_BOARD_COLUMN = "Board";
    static final String SHORT_COORDINATOR_COLUMN = "Coordtr";
    static final String SHORT_LIMITED_RUNS_COLUMN = "limited";
    static final String SHORT_AT_RISK_COLUMN = "at-risk";
    static final String SHORT_BIKERS_COLUMN = "biker";
    static final String SHORT_OUT_COLUMN = "out";
    static final String SHORT_TRAINED_DRIVER_COLUMN = "trainedD";
    static final String SHORT_EVENTS_DRIVER_COLUMN = "eventsD";
    static final String SHORT_TRAINED_EVENT_DRIVER_COLUMN = "trainedED";
    static final String SHORT_GONE_COLUMN = "gone";
    static final String SHORT_OTHER_DRIVERS_COLUMN = "other";
    static final String SHORT_ADMIN_COLUMN = "Admin";
    static final String SHORT_CONSUMER_REQUEST_COLUMN = "ConsReq";
    static final String SHORT_VOLUNTEER_REQUEST_COLUMN = "Volunteer Request";
    static final String SHORT_DRIVER_DETAILS_COLUMN = "details";

    static final String ERROR_PRIMARY_PHONE_MISSING_AREA_CODE = "Primary phone missing area code, assuming 510";
    static final String ERROR_PRIMARY_PHONE_CANNOT_PARSE_PHONE = "Cannot parse primary phone number";
    static final String ERROR_SECOND_PHONE_MISSING_AREA_CODE = "Second phone missing area code, assuming 510";
    static final String ERROR_SECOND_PHONE_CANNOT_PARSE_PHONE = "Cannot parse second phone number";

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
    private final Boolean condo;
    private final Boolean consumerRequest;
    private String volunteerRequest;
    private String referral;
    private final Boolean emailVerified;
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

    public String getFullAddress() {
        return address == null ? NOT_PROVIDED : address + ", " + getCity() + ", CA";
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

    public Boolean isCondo() {
        return condo;
    }

    public String getReferral() {
        return referral == null ? NOT_PROVIDED : referral;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
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
        final Boolean condo,
        final Boolean consumerRequest,
        final String volunteerRequest,
        final String referral,
        final Boolean emailVerified)  {

        this.name = name;
        this.userName = userName;
        this.id = id;
        this.address = address;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.altPhoneNumber = altPhoneNumber;
        this.neighborhood = neighborhood;
        this.createTime = createTime;
        this.condo = condo;
        this.consumerRequest = consumerRequest;
        this.volunteerRequest = volunteerRequest;
        this.referral = referral;
        this.emailVerified = emailVerified;
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

    Boolean isPacker() {
        return groupMembership.contains(Constants.GROUP_PACKERS);
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

    Boolean isTrainedCustomerCareA() {
        return groupMembership.contains(Constants.GROUP_TRAINED_CUSTOMER_CARE_A);
    }

    Boolean isTrainedCustomerCareB() {
        return groupMembership.contains(Constants.GROUP_TRAINED_CUSTOMER_CARE_B);
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
        return groupMembership.contains(Constants.GROUP_COORDINATORS);
    }

    Boolean isAdmin() {
        return groupMembership.contains(Constants.GROUP_ADMIN);
    }

    Boolean isBoard() {
        return groupMembership.contains(Constants.GROUP_BOARDMEMBERS);
    }

    Boolean isLimitedRuns() {
        return groupMembership.contains(Constants.GROUP_LIMITED);
    }

    Boolean isAtRisk() {
        return groupMembership.contains(Constants.GROUP_AT_RISK);
    }

    Boolean isBiker() {
        return groupMembership.contains(Constants.GROUP_BIKERS);
    }

    Boolean isOut() {
        return groupMembership.contains(Constants.GROUP_OUT);
    }

    Boolean isTrainedDriver() {
        return groupMembership.contains(Constants.TRAINED_DRIVERS);
    }

    Boolean isEventDriver() {
        return groupMembership.contains(Constants.GROUP_EVENT_DRIVERS);
    }

    Boolean isTrainedEventDriver() {
        return groupMembership.contains(Constants.TRAINED_EVENT_DRIVERS);
    }

    Boolean isGone() {
        return groupMembership.contains(Constants.GROUP_GONE);
    }

    Boolean isOtherDrivers() {
        return groupMembership.contains(Constants.GROUP_OTHER_DRIVERS);
    }

    @Override
    public String toString() {

        return NAME_COLUMN +
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
                Constants.TRAINED_DRIVERS +
                "=" +
                isTrainedDriver() +
                ':' +
                CONDO_COLUMN +
                "=" +
                isCondo() +
                ':' +
                REFERRAL_COLUMN +
                "=" +
                (referral == null ? NOT_PROVIDED : referral) +
                ':' +
                EMAIL_VERIFIED_COLUMN +
                "=" +
                getEmailVerified() +
                ":" +
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
                Constants.GROUP_PACKERS +
                "=" +
                isPacker() +
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
                Constants.GROUP_TRAINED_CUSTOMER_CARE_A +
                "=" +
                isTrainedCustomerCareA() +
                ':' +
                Constants.GROUP_TRAINED_CUSTOMER_CARE_B +
                "=" +
                isTrainedCustomerCareB() +
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
                Constants.GROUP_BOARDMEMBERS +
                "=" +
                isBoard() +
                ':' +
                Constants.GROUP_COORDINATORS +
                "=" +
                isCoordinator() +
                ':' +
                Constants.GROUP_LIMITED +
                "=" +
                isLimitedRuns() +
                ':' +
                Constants.GROUP_AT_RISK +
                "=" +
                isAtRisk() +
                ':' +
                Constants.GROUP_BIKERS +
                "=" +
                isBiker() +
                ':' +
                Constants.GROUP_OUT +
                "=" +
                isOut() +
                ':' +
                Constants.GROUP_EVENT_DRIVERS +
                "=" +
                isEventDriver() +
                ':' +
                Constants.TRAINED_EVENT_DRIVERS +
                "=" +
                isTrainedEventDriver() +
                ':' +
                Constants.GROUP_GONE +
                "=" +
                isGone() +
                ':' +
                Constants.GROUP_OTHER_DRIVERS +
                "=" +
                isOtherDrivers() +
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
    }

    //
    // Originally these were all assertions, but we have seen:
    //   - Discourse supports creating incomplete users
    //   - Incomplete users created by some other means - unknown
    //
    private void auditNullFields() {

        if (name == null) {
            name = NOT_PROVIDED;
            dataErrors.add("Empty name");
        }
        if (userName == null) {
            userName = NOT_PROVIDED;
            dataErrors.add("Empty user name");
        }
        if (address == null) {
            address = NOT_PROVIDED;
            dataErrors.add("Empty address");
        }
        if (city == null) {
            city = NOT_PROVIDED;
            dataErrors.add("Empty city");
        }
        if (phoneNumber == null) {
            phoneNumber = NOT_PROVIDED;
            dataErrors.add("Empty phone number");
        }

        if (neighborhood == null) {
            neighborhood = "";
        }

        if (emailVerified == null) {
            dataErrors.add("Missing emailVerified field");
        }
    }


    // Must be insensitive to null data
    private void normalizeData() {
        removeNewlines();
        removeLeadingTrailingWhitespace();
        auditAndNormalizePhoneNumber();
        auditAndNormalizeAltPhoneNumber();
        auditAndNormalizeCity();
        minimizeAddress();
        normalizeVolunteerRequest();
        normalizeReferral();
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
        if (phoneNumber == null) {
            assert ! dataErrors.isEmpty();
            return;
        }

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

        //noinspection ConstantConditions
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

        altPhoneNumber = altPhoneNumber.trim().toLowerCase();

        switch (altPhoneNumber) {
            case "":
            case "none":
            case "no":
            case "n/a":
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
        if (city == null) {
            assert ! dataErrors.isEmpty();
            return;
        }

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

        if (address == null) {
            assert ! dataErrors.isEmpty();
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

        // FIX THIS, DS: what kind of validation can be done here
        //               to prevent chopping off street address info?

        address = address.substring(0, index);
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

        if (city == null) {
            assert ! dataErrors.isEmpty();
            return false;
        }

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

        if (city == null) {
            assert ! dataErrors.isEmpty();
            return false;
        }

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

        if (city == null) {
            assert ! dataErrors.isEmpty();
            return false;
        }

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
            final Boolean condo,
            final Boolean consumerRequest,
            final String volunteerRequest,
            final String referral,
            final Boolean emailVerified,
            final String... groups) throws UserException {


        User user = new User(name, userName, id, address, city, phoneNumber, altPhoneNumber,
                neighborhood, createdAt, condo, consumerRequest, volunteerRequest, referral, emailVerified);
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
            final Boolean condo,
            final Boolean consumerRequest,
            final String volunteerRequest,
            final String referral,
            final Boolean emailVerified,
            final List<String> groups) throws UserException {


        User user = new User(name, userName, id, address, city, phoneNumber, altPhoneNumber,
                neighborhood, createdAt, condo, consumerRequest, volunteerRequest, referral, emailVerified);
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

    static String reportWithEmailCSVHeaders() {
        return reportHeaders(true);
    }

    static String reportCSVHeaders() {
        return reportHeaders(false);
    }

    private static String reportHeaders(boolean addEmail) {

        return SHORT_ID_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_CREATED_AT_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_NAME_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_USERNAME_COLUMN + Constants.CSV_SEPARATOR
                + (addEmail ? SHORT_EMAIL_COLUMN + Constants.CSV_SEPARATOR : "")
                + SHORT_PHONE_NUMBER_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_ALT_PHONE_NUMBER_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_NEIGHBORHOOD_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_CITY_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_ADDRESS_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_CONDO_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_REFERRAL_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_VOICEONLY_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_TRAINED_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_DISPATCHER_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_WORKFLOW_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_INREACH_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_OUTREACH_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_HELPLINE_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_SITELINE_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_TRAINED_CUSTOMER_CARE_A_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_TRAINED_CUSTOMER_CARE_B_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_MARKETING_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_MODERATORS_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_TRUST_LEVEL_4_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_SPECIALIST_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_PACKER_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_BHS_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_CUSTOMER_INFO_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_ADVISOR_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_BOARD_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_COORDINATOR_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_LIMITED_RUNS_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_AT_RISK_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_BIKERS_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_OUT_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_EVENTS_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_TRAINED_EVENT_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_GONE_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_OTHER_DRIVERS_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_ADMIN_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_CONSUMER_REQUEST_COLUMN + Constants.CSV_SEPARATOR
                + SHORT_VOLUNTEER_REQUEST_COLUMN
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
                + TRAINED_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + CREATED_AT_COLUMN + Constants.CSV_SEPARATOR
                + CONDO_COLUMN + Constants.CSV_SEPARATOR
                + REFERRAL_COLUMN + Constants.CSV_SEPARATOR
                + EMAIL_VERIFIED_COLUMN + Constants.CSV_SEPARATOR
                + CONSUMER_REQUEST_COLUMN + Constants.CSV_SEPARATOR
                + VOLUNTEER_REQUEST_COLUMN + Constants.CSV_SEPARATOR
                + SPECIALIST_COLUMN + Constants.CSV_SEPARATOR
                + PACKER_COLUMN + Constants.CSV_SEPARATOR
                + BHS_COLUMN + Constants.CSV_SEPARATOR
                + HELPLINE_COLUMN + Constants.CSV_SEPARATOR
                + SITELINE_COLUMN + Constants.CSV_SEPARATOR
                + TRAINED_CUSTOMER_CARE_A_COLUMN + Constants.CSV_SEPARATOR
                + TRAINED_CUSTOMER_CARE_B_COLUMN + Constants.CSV_SEPARATOR
                + INREACH_COLUMN + Constants.CSV_SEPARATOR
                + OUTREACH_COLUMN + Constants.CSV_SEPARATOR
                + MARKETING_COLUMN + Constants.CSV_SEPARATOR
                + MODERATORS_COLUMN + Constants.CSV_SEPARATOR
                + TRUST_LEVEL_4_COLUMN + Constants.CSV_SEPARATOR
                + WORKFLOW_COLUMN + Constants.CSV_SEPARATOR
                + CUSTOMER_INFO_COLUMN + Constants.CSV_SEPARATOR
                + ADVISOR_COLUMN + Constants.CSV_SEPARATOR
                + BOARD_COLUMN + Constants.CSV_SEPARATOR
                + COORDINATOR_COLUMN + Constants.CSV_SEPARATOR
                + LIMITED_RUNS_COLUMN + Constants.CSV_SEPARATOR
                + AT_RISK_COLUMN + Constants.CSV_SEPARATOR
                + BIKERS_COLUMN + Constants.CSV_SEPARATOR
                + OUT_COLUMN + Constants.CSV_SEPARATOR
                + EVENTS_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + TRAINED_EVENT_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + GONE_COLUMN + Constants.CSV_SEPARATOR
                + OTHER_DRIVERS_COLUMN + Constants.CSV_SEPARATOR
                + ADMIN_COLUMN
                + "\n";
    }

    String rawToCSV() {

        return getId() + Constants.CSV_SEPARATOR +
                escapeCommas(getName()) + Constants.CSV_SEPARATOR +
                getUserName() + Constants.CSV_SEPARATOR +
                getPhoneNumber() + Constants.CSV_SEPARATOR +
                getAltPhoneNumber() + Constants.CSV_SEPARATOR +
                escapeCommas(getNeighborhood()) + Constants.CSV_SEPARATOR +
                escapeCommas(getCity()) + Constants.CSV_SEPARATOR +
                escapeCommas(getAddress()) + Constants.CSV_SEPARATOR +
                isConsumer() + Constants.CSV_SEPARATOR +
                isVoiceOnly() + Constants.CSV_SEPARATOR +
                isDispatcher() + Constants.CSV_SEPARATOR +
                isDriver() + Constants.CSV_SEPARATOR +
                isTrainedDriver() + Constants.CSV_SEPARATOR +
                getCreateTime() + Constants.CSV_SEPARATOR +
                isCondo() + Constants.CSV_SEPARATOR +
                escapeCommas(getReferral()) + Constants.CSV_SEPARATOR +
                getEmailVerified() + Constants.CSV_SEPARATOR +
                hasConsumerRequest() + Constants.CSV_SEPARATOR +
                getVolunteerRequest() + Constants.CSV_SEPARATOR +
                isSpecialist() + Constants.CSV_SEPARATOR +
                isPacker() + Constants.CSV_SEPARATOR +
                isBHS() + Constants.CSV_SEPARATOR +
                isHelpLine() + Constants.CSV_SEPARATOR +
                isSiteLine() + Constants.CSV_SEPARATOR +
                isTrainedCustomerCareA() + Constants.CSV_SEPARATOR +
                isTrainedCustomerCareB() + Constants.CSV_SEPARATOR +
                isInReach() + Constants.CSV_SEPARATOR +
                isOutReach() + Constants.CSV_SEPARATOR +
                isMarketing() + Constants.CSV_SEPARATOR +
                isModerator() + Constants.CSV_SEPARATOR +
                isTrustLevel4() + Constants.CSV_SEPARATOR +
                isWorkflow() + Constants.CSV_SEPARATOR +
                isCustomerInfo() + Constants.CSV_SEPARATOR +
                isAdvisor() + Constants.CSV_SEPARATOR +
                isBoard() + Constants.CSV_SEPARATOR +
                isCoordinator() + Constants.CSV_SEPARATOR +
                isLimitedRuns() + Constants.CSV_SEPARATOR +
                isAtRisk() + Constants.CSV_SEPARATOR +
                isBiker() + Constants.CSV_SEPARATOR +
                isOut() + Constants.CSV_SEPARATOR +
                isEventDriver() + Constants.CSV_SEPARATOR +
                isTrainedEventDriver() + Constants.CSV_SEPARATOR +
                isGone() + Constants.CSV_SEPARATOR +
                isOtherDrivers() + Constants.CSV_SEPARATOR +
                isAdmin() +
                '\n';
    }

    String reportWithEMailToCSV(final String emailAddress) {
        return report(true, emailAddress);
    }
    String reportToCSV() {
        return report(false, "");
    }

    private String report(boolean addEmail, final String emailAddress) {

        return getId() + Constants.CSV_SEPARATOR +
                getSimpleCreateTime() + Constants.CSV_SEPARATOR +
                escapeCommas(getName()) + Constants.CSV_SEPARATOR +
                getUserName() + Constants.CSV_SEPARATOR +
                (addEmail ? emailAddress + Constants.CSV_SEPARATOR : "") +
                getPhoneNumber() + Constants.CSV_SEPARATOR +
                getAltPhoneNumber() + Constants.CSV_SEPARATOR +
                escapeCommas(getNeighborhood()) + Constants.CSV_SEPARATOR +
                escapeCommas(getCity()) + Constants.CSV_SEPARATOR +
                escapeCommas(getAddress()) + Constants.CSV_SEPARATOR +
                isCondo() + Constants.CSV_SEPARATOR +
                escapeCommas(getReferral()) + Constants.CSV_SEPARATOR +
                isConsumer() + Constants.CSV_SEPARATOR +
                isVoiceOnly() + Constants.CSV_SEPARATOR +
                isDriver() + Constants.CSV_SEPARATOR +
                isTrainedDriver() + Constants.CSV_SEPARATOR +
                isDispatcher() + Constants.CSV_SEPARATOR +
                isWorkflow() + Constants.CSV_SEPARATOR +
                isInReach() + Constants.CSV_SEPARATOR +
                isOutReach() + Constants.CSV_SEPARATOR +
                isHelpLine() + Constants.CSV_SEPARATOR +
                isSiteLine() + Constants.CSV_SEPARATOR +
                isTrainedCustomerCareA() + Constants.CSV_SEPARATOR +
                isTrainedCustomerCareB() + Constants.CSV_SEPARATOR +
                isMarketing() + Constants.CSV_SEPARATOR +
                isModerator() + Constants.CSV_SEPARATOR +
                isTrustLevel4() + Constants.CSV_SEPARATOR +
                isSpecialist() + Constants.CSV_SEPARATOR +
                isPacker() + Constants.CSV_SEPARATOR +
                isBHS() + Constants.CSV_SEPARATOR +
                isCustomerInfo() + Constants.CSV_SEPARATOR +
                isAdvisor() + Constants.CSV_SEPARATOR +
                isBoard() + Constants.CSV_SEPARATOR +
                isCoordinator() + Constants.CSV_SEPARATOR +
                isLimitedRuns() + Constants.CSV_SEPARATOR +
                isAtRisk() + Constants.CSV_SEPARATOR +
                isBiker() + Constants.CSV_SEPARATOR +
                isOut() + Constants.CSV_SEPARATOR +
                isEventDriver() + Constants.CSV_SEPARATOR +
                isTrainedEventDriver() + Constants.CSV_SEPARATOR +
                isGone() + Constants.CSV_SEPARATOR +
                isOtherDrivers() + Constants.CSV_SEPARATOR +
                isAdmin() + Constants.CSV_SEPARATOR +
                hasConsumerRequest() + Constants.CSV_SEPARATOR +
                getVolunteerRequest() +
                '\n';
    }

    @Override
    public boolean equals(Object obj) {

        // It would look nicer to the eye to group all of this as a single return statement
        // with a bunch of && clauses. But debugging why a match is failing becomes
        // a PITA with that structure.

        if (obj == this){
            return true;
        }

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
        if (! condo.equals(otherObj.condo)) {
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

        //noinspection RedundantIfStatement
        if (! emailVerified.equals(otherObj.emailVerified)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }

    private String escapeCommas(final String value) {
        if (value.indexOf(',') == -1) {
            return value;
        }

        return "\"" + value + "\"";
    }
}
