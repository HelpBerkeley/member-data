//
// Copyright (c) 2020-2024 helpberkeley.org
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

    public enum ReportHeaderOption {
        ADD_EMAIL,
        NO_EMAIL
    }

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
    static final String LOGISTICS_COLUMN = "Logistics";
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
    static final String GROUPS_OWNED_COLUMN = "groups-owned";
    static final String MONDAY_FRREG_COLUMN = "MOfrreg";
    static final String WEDNESDAY_FRREG_COLUMN = "WEfrreg";
    static final String THURSDAY_FRREG_COLUMN = "THfrreg";
    static final String FRVOICEONLY_COLUMN = "FRVoiceOnly";
    static final String EVOLUNTEERS_COLUMN = "evolunteers";

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
    static final String SHORT_FRVOICEONLY_COLUMN = "FRVoiceonly";
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
    static final String SHORT_LOGISTICS_COLUMN = "Logistics";
    static final String SHORT_BHS_COLUMN = "BHS";
    static final String SHORT_CUSTOMER_INFO_COLUMN = "CustInf";
    static final String SHORT_ADVISOR_COLUMN = "Advsr";
    static final String SHORT_BOARD_COLUMN = "Board";
    static final String SHORT_COORDINATOR_COLUMN = "Coordtr";
    static final String SHORT_LIMITED_RUNS_COLUMN = "limited";
    static final String SHORT_AT_RISK_COLUMN = "at-risk";
    static final String SHORT_BIKERS_COLUMN = "bike";
    static final String SHORT_OUT_COLUMN = "out";
    static final String SHORT_TRAINED_DRIVER_COLUMN = "Dtrain'd";
    static final String SHORT_EVENTS_DRIVER_COLUMN = "event";
    static final String SHORT_TRAINED_EVENT_DRIVER_COLUMN = "Etrain'd";
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
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy/MM/dd");

    private static final String[] BERKLEY_BERKELEY_STREETS = {
            // Berkeley Square
            "brkly sqr",
            "brkly sq.",
            // Berkeley Way
            "brkly wy",
    };

    private static final String[] ALBANY_ALBANY_STREETS = {
            // Albany Terrace
            "lbny trc",
            "lbny tr",
    };

    private static final String[] KENSINGTON_KENSINGTON_STREETS = {
            // Kensington Park Road
            "nsngtn prk rd",
            "nsngtn pk rd",
            "nsngtn pk",
    };

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
    private final Set<String> groupOwnerships = new TreeSet<>();
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

    public String getCreateDate() {
        return ZonedDateTime.parse(createTime).format(DATE_FORMATTER);
    }

    public List<String> getDataErrors() {
        return dataErrors;
    }

    protected User(
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

    Boolean isLogistics() {
        return groupMembership.contains(Constants.GROUP_LOGISTICS);
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

    Boolean isFRVoiceOnly() {
        return groupMembership.contains(Constants.GROUP_FRVOICEONLY);
    }

    Boolean isEVolunteers() {
        return groupMembership.contains(Constants.GROUP_EVOLUNTEERS);
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
        return groupMembership.contains(Constants.GROUP_TRAINED_DRIVERS);
    }

    Boolean isEventDriver() {
        return groupMembership.contains(Constants.GROUP_EVENT_DRIVERS);
    }

    Boolean isMondayFrreg() {
        return groupMembership.contains(Constants.GROUP_MONDAY_FRREG);
    }

    Boolean isWednesdayFrreg() {
        return groupMembership.contains(Constants.GROUP_WEDNESDAY_FRREG);
    }

    Boolean isThursdayFrreg() {
        return groupMembership.contains(Constants.GROUP_THURSDAY_FRREG);
    }

    Boolean isFrreg() {
        return (isMondayFrreg() || isWednesdayFrreg() || isThursdayFrreg());
    }

    Boolean isTrainedEventDriver() {
        return groupMembership.contains(Constants.GROUP_TRAINED_EVENT_DRIVERS);
    }

    Boolean isGone() {
        return groupMembership.contains(Constants.GROUP_GONE);
    }

    Boolean isOtherDrivers() {
        return groupMembership.contains(Constants.GROUP_OTHER_DRIVERS);
    }

    Boolean isAvailableDriver() {
        return isDriver() && ! (isGone() || isOut() || isOtherDrivers());
    }

    Boolean isAvailableEventDriver() {
        return isEventDriver() && ! (isGone() || isOut() || isOtherDrivers());
    }

    Boolean groupOwner(String groupName) {
        return groupOwnerships.contains(groupName);
    }

    String groupsOwned() {
        return groupOwnerships.isEmpty() ? "" : "\"" + String.join(Constants.CSV_SEPARATOR, groupOwnerships) + "\"";
    }

    void leaveGroup(String groupName) {
        groupMembership.remove(groupName);
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
                Constants.GROUP_TRAINED_DRIVERS +
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
                FRVOICEONLY_COLUMN +
                "=" +
                isFRVoiceOnly() +
                ':' +
                VOLUNTEER_REQUEST_COLUMN +
                "=" +
                (volunteerRequest == null ? NOT_PROVIDED : volunteerRequest) +
                ':' +
                Constants.GROUP_SPECIALISTS +
                "=" +
                isSpecialist() +
                ':' +
                Constants.GROUP_LOGISTICS +
                "=" +
                isLogistics() +
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
                Constants.GROUP_TRAINED_EVENT_DRIVERS +
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
        // FIX THIS, DS: can we escape them?
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

        String digits = phoneNumber.replaceAll("\\D", "");

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

        String digits = altPhoneNumber.replaceAll("\\D", "");

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

        // Remove leading trailing whitespace
        String addr = address.trim();

        // Lower case
        addr = addr.toLowerCase();

        // Remove all vowels but y
        String compressed = addr.replaceAll("[aeiou]", "");

        // Replace all repeating characters with single characters - e.g. bkkllyy -> bkly
        compressed = compressed.replaceAll("(.)\\1+","$1");

        if ((city.equals(Constants.BERKELEY) && streetContainsBerkeley(compressed))
            || (city.equals(Constants.ALBANY) && streetContainsAlbany(compressed))
            || (city.equals(Constants.KENSINGTON) && streetContainsKensington(compressed))) {

            return;
        }

        int cityIndex;

        switch (city) {
            case Constants.BERKELEY:
                cityIndex = addr.indexOf(" berkeley");
                break;
            case Constants.ALBANY:
                cityIndex = addr.indexOf(" albany");
                break;
            case Constants.KENSINGTON:
                cityIndex = addr.indexOf(" kensington");
                break;
            default:
                return;
        }

        // FIX THIS, DS: what kind of validation can be done here
        //               to prevent chopping off street address info?

        if (cityIndex != -1) {
            address = address.substring(0, cityIndex);
        }
    }

    private boolean streetContainsBerkeley(String compressedAddress) {
        for (String street : BERKLEY_BERKELEY_STREETS) {
            if (compressedAddress.contains(street)) {
                return true;
            }
        }

        return false;
    }

    private boolean streetContainsAlbany(String compressedAddress) {
        for (String street : ALBANY_ALBANY_STREETS) {
            if (compressedAddress.contains(street)) {
                return true;
            }
        }

        return false;
    }

    private boolean streetContainsKensington(String compressedAddress) {
        for (String street : KENSINGTON_KENSINGTON_STREETS) {
            if (compressedAddress.contains(street)) {
                return true;
            }
        }

        return false;
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

        // Remove all vowels but y
        cityName = cityName.replaceAll("[aeiou]", "");

        // Replace all repeating characters with single characters - e.g. bkkllyy -> bkly
        cityName = cityName.replaceAll("(.)\\1+","$1");

        // Look for the expected spelling and some possible misspellings:

        switch (cityName) {
            case "brkly":
            case "brkyl":
            case "brlky":
            case "brlyk":
            case "brky":

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

        // Remove all vowels but y
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

        // Remove all vowels but y
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
            final List<String> groupMemberships,
            final List<String> groupOwnerships) throws UserException {


        User user = new User(name, userName, id, address, city, phoneNumber, altPhoneNumber,
                neighborhood, createdAt, condo, consumerRequest, volunteerRequest, referral, emailVerified);
        for (String group : groupMemberships) {
            assert ! user.groupMembership.contains(group) : group;
            user.groupMembership.add(group);
        }
        for (String group : groupOwnerships) {
            assert ! user.groupOwnerships.contains(group) : group;
            user.groupOwnerships.add(group);
        }

        user.auditNullFields();
        user.normalizeData();

        if (! user.dataErrors.isEmpty()) {
            throw new UserException(user);
        }

        return user;
    }

    static List<String> reportWithEmailCSVHeaders() {
        return reportHeaders(ReportHeaderOption.ADD_EMAIL);
    }

    static List<String> reportCSVHeaders() {
        return reportHeaders(ReportHeaderOption.NO_EMAIL);
    }

    private static List<String> reportHeaders(ReportHeaderOption option) {

        List<String> headers = new ArrayList<>(List.of(SHORT_ID_COLUMN,
                SHORT_CREATED_AT_COLUMN, SHORT_NAME_COLUMN,
                SHORT_USERNAME_COLUMN));
        if (option == ReportHeaderOption.ADD_EMAIL) {
            headers.add(SHORT_EMAIL_COLUMN);
        }
        List<String> headersContinued = new ArrayList<>(List.of(SHORT_PHONE_NUMBER_COLUMN,
                SHORT_ALT_PHONE_NUMBER_COLUMN,
                SHORT_NEIGHBORHOOD_COLUMN,
                SHORT_CITY_COLUMN,
                SHORT_ADDRESS_COLUMN,
                SHORT_CONDO_COLUMN,
                SHORT_REFERRAL_COLUMN,
                SHORT_CONSUMER_COLUMN,
                SHORT_VOICEONLY_COLUMN,
                SHORT_FRVOICEONLY_COLUMN,
                SHORT_DRIVER_COLUMN,
                SHORT_TRAINED_DRIVER_COLUMN,
                SHORT_DISPATCHER_COLUMN,
                SHORT_WORKFLOW_COLUMN,
                SHORT_INREACH_COLUMN,
                SHORT_OUTREACH_COLUMN,
                SHORT_HELPLINE_COLUMN,
                SHORT_SITELINE_COLUMN,
                SHORT_TRAINED_CUSTOMER_CARE_A_COLUMN,
                SHORT_TRAINED_CUSTOMER_CARE_B_COLUMN,
                SHORT_MARKETING_COLUMN,
                SHORT_MODERATORS_COLUMN,
                SHORT_TRUST_LEVEL_4_COLUMN,
                SHORT_SPECIALIST_COLUMN,
                SHORT_LOGISTICS_COLUMN,
                SHORT_BHS_COLUMN,
                SHORT_CUSTOMER_INFO_COLUMN,
                SHORT_ADVISOR_COLUMN,
                SHORT_BOARD_COLUMN,
                SHORT_COORDINATOR_COLUMN,
                SHORT_LIMITED_RUNS_COLUMN,
                SHORT_AT_RISK_COLUMN,
                SHORT_BIKERS_COLUMN,
                SHORT_OUT_COLUMN,
                SHORT_EVENTS_DRIVER_COLUMN,
                SHORT_TRAINED_EVENT_DRIVER_COLUMN,
                SHORT_GONE_COLUMN,
                SHORT_OTHER_DRIVERS_COLUMN,
                SHORT_ADMIN_COLUMN,
                SHORT_CONSUMER_REQUEST_COLUMN,
                SHORT_VOLUNTEER_REQUEST_COLUMN));

        headers.addAll(headersContinued);
        return headers;
    }

    static List<String> rawCSVHeaders() {

        return new ArrayList<>(List.of(ID_COLUMN,
                NAME_COLUMN,
                USERNAME_COLUMN,
                PHONE_NUMBER_COLUMN,
                ALT_PHONE_NUMBER_COLUMN,
                NEIGHBORHOOD_COLUMN,
                CITY_COLUMN,
                ADDRESS_COLUMN,
                CONSUMER_COLUMN,
                VOICEONLY_COLUMN,
                FRVOICEONLY_COLUMN,
                DISPATCHER_COLUMN,
                DRIVER_COLUMN,
                TRAINED_DRIVER_COLUMN,
                CREATED_AT_COLUMN,
                CONDO_COLUMN,
                REFERRAL_COLUMN,
                EMAIL_VERIFIED_COLUMN,
                CONSUMER_REQUEST_COLUMN,
                VOLUNTEER_REQUEST_COLUMN,
                SPECIALIST_COLUMN,
                LOGISTICS_COLUMN,
                BHS_COLUMN,
                HELPLINE_COLUMN,
                SITELINE_COLUMN,
                TRAINED_CUSTOMER_CARE_A_COLUMN,
                TRAINED_CUSTOMER_CARE_B_COLUMN,
                INREACH_COLUMN,
                OUTREACH_COLUMN,
                MARKETING_COLUMN,
                MODERATORS_COLUMN,
                TRUST_LEVEL_4_COLUMN,
                WORKFLOW_COLUMN,
                CUSTOMER_INFO_COLUMN,
                ADVISOR_COLUMN,
                BOARD_COLUMN,
                COORDINATOR_COLUMN,
                LIMITED_RUNS_COLUMN,
                AT_RISK_COLUMN,
                BIKERS_COLUMN,
                OUT_COLUMN,
                EVENTS_DRIVER_COLUMN,
                TRAINED_EVENT_DRIVER_COLUMN,
                GONE_COLUMN,
                OTHER_DRIVERS_COLUMN,
                ADMIN_COLUMN,
                GROUPS_OWNED_COLUMN,
                MONDAY_FRREG_COLUMN,
                WEDNESDAY_FRREG_COLUMN,
                THURSDAY_FRREG_COLUMN,
                EVOLUNTEERS_COLUMN));
    }

    List<String> rawToCSV() {
        return new ArrayList<>(List.of(String.valueOf(getId()),
                getName(),
                getUserName(),
                getPhoneNumber(),
                getAltPhoneNumber(),
                getNeighborhood(),
                getCity(),
                getAddress(),
                isConsumer().toString(),
                isVoiceOnly().toString(),
                isFRVoiceOnly().toString(),
                isDispatcher().toString(),
                isDriver().toString(),
                isTrainedDriver().toString(),
                getCreateTime(),
                isCondo().toString(),
                getReferral(),
                getEmailVerified().toString(),
                hasConsumerRequest().toString(),
                getVolunteerRequest(),
                isSpecialist().toString(),
                isLogistics().toString(),
                isBHS().toString(),
                isHelpLine().toString(),
                isSiteLine().toString(),
                isTrainedCustomerCareA().toString(),
                isTrainedCustomerCareB().toString(),
                isInReach().toString(),
                isOutReach().toString(),
                isMarketing().toString(),
                isModerator().toString(),
                isTrustLevel4().toString(),
                isWorkflow().toString(),
                isCustomerInfo().toString(),
                isAdvisor().toString(),
                isBoard().toString(),
                isCoordinator().toString(),
                isLimitedRuns().toString(),
                isAtRisk().toString(),
                isBiker().toString(),
                isOut().toString(),
                isEventDriver().toString(),
                isTrainedEventDriver().toString(),
                isGone().toString(),
                isOtherDrivers().toString(),
                isAdmin().toString(),
                groupsOwned(),
                isMondayFrreg().toString(),
                isWednesdayFrreg().toString(),
                isThursdayFrreg().toString(),
                isEVolunteers().toString()));
    }

    List<String> reportWithEMailToCSV(final String emailAddress) {
        return report(ReportHeaderOption.ADD_EMAIL, emailAddress);
    }
    List<String> reportToCSV() {
        return report(ReportHeaderOption.NO_EMAIL, "");
    }

    private List<String> report(ReportHeaderOption option, final String emailAddress) {

        List<String> reportRow = new ArrayList<>(List.of(String.valueOf(getId()),
                getSimpleCreateTime(),
                getName(),
                getUserName()));
        if (option == ReportHeaderOption.ADD_EMAIL) {
            reportRow.add(emailAddress);
        }
        List<String> reportRowContinued = new ArrayList<>(List.of(getPhoneNumber(),
                getAltPhoneNumber(),
                getNeighborhood(),
                getCity(),
                getAddress(),
                isCondo().toString(),
                getReferral(),
                isConsumer().toString(),
                isVoiceOnly().toString(),
                isFRVoiceOnly().toString(),
                isDriver().toString(),
                isTrainedDriver().toString(),
                isDispatcher().toString(),
                isWorkflow().toString(),
                isInReach().toString(),
                isOutReach().toString(),
                isHelpLine().toString(),
                isSiteLine().toString(),
                isTrainedCustomerCareA().toString(),
                isTrainedCustomerCareB().toString(),
                isMarketing().toString(),
                isModerator().toString(),
                isTrustLevel4().toString(),
                isSpecialist().toString(),
                isLogistics().toString(),
                isBHS().toString(),
                isCustomerInfo().toString(),
                isAdvisor().toString(),
                isBoard().toString(),
                isCoordinator().toString(),
                isLimitedRuns().toString(),
                isAtRisk().toString(),
                isBiker().toString(),
                isOut().toString(),
                isEventDriver().toString(),
                isTrainedEventDriver().toString(),
                isGone().toString(),
                isOtherDrivers().toString(),
                isAdmin().toString(),
                hasConsumerRequest().toString(),
                getVolunteerRequest()));
        reportRow.addAll(reportRowContinued);
        return reportRow;
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
}
