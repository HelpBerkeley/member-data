//
// Copyright (c) 2020-2021 helpberkeley.org
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

import com.opencsv.exceptions.CsvException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserExporterTest extends TestBase {
    @Test
    public void errorsToFileTest() throws UserException, IOException {

        List<User> users = new ArrayList<>();

        try {
            createUserWithCityAndNeighborhood(TEST_USER_NAME_1, Constants.BERKELEY, "unknown");
        } catch (UserException ex) {
            assertThat(ex.user).isNotNull();
            assertThat(ex.user.getUserName()).isEqualTo(TEST_USER_NAME_1);
            users.add(ex.user);
        }

        User u2 = createUserWithCityAndNeighborhood(
                TEST_USER_NAME_2, Constants.ALBANY, "Solano");
        users.add(u2);

        User u3 = createUserWithCityAndNeighborhood(
                TEST_USER_NAME_3, "Altoona", "unknown");
        users.add(u3);

        UserExporter exporter = new UserExporter(users);
        String fileName = exporter.errorsToFile("errorsToFileTest.txt");

        String fileData = readFile(fileName);
//        assertThat(fileData).contains(TEST_USER_NAME_1);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_2);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void consumerRequestsToFileTest() throws UserException, IOException {

        User u1 = createUserWithNoRequestsNoGroups(TEST_USER_NAME_1);
        User u2 = createUserWithConsumerRequest(TEST_USER_NAME_2, true);

        UserExporter exporter = new UserExporter(List.of(u1, u2));
        String fileName = exporter.consumerRequestsToFile("consumerRequests.csv");

        String fileData = readFile(fileName);
        assertThat(fileData).contains(TEST_USER_NAME_2);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_1);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void volunteerRequestsToFileTest() throws UserException, IOException {

        User u1 = createUserWithNoRequestsNoGroups(TEST_USER_NAME_1);
        User u2 = createUserWithVolunteerRequest(TEST_USER_NAME_2, "Drive");
        User u3 = createUserWithNoRequestsNoGroups(TEST_USER_NAME_3);
        User u4 = createUserWithVolunteerRequest("u4", "Dispatch");
        User u5 = createUserWithVolunteerRequestAndGroup("u5", "Drive", "gone");

        UserExporter exporter = new UserExporter(List.of(u1, u2, u3, u4));
        String fileName = exporter.volunteerRequestsToFile("volunteerRequests.csv");

        String fileData = readFile(fileName);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_1);
        assertThat(fileData).contains(TEST_USER_NAME_2);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_3);
        assertThat(fileData).contains("u4");
        assertThat(fileData).doesNotContain("u5");

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void allMembersRawColumnsTest() throws UserException, IOException, CsvException {
        User u1 = createTestUser1();
        UserExporter exporter = new UserExporter(List.of(u1));

        String allMemberRows = exporter.allMembersRaw();
        CSVListReader csvReader = new CSVListReader(new StringReader(allMemberRows));
        List<List<String>> rows = csvReader.readAllToList();
        assertThat(rows).hasSize(2);

        List<String> header = rows.get(0);
        assertThat(header).isEqualTo(User.rawCSVHeaders());

        // FIX THIS, DS: add a constant for number of columns expected
        int index = 0;
        assertThat(header.get(index++)).isEqualTo(User.ID_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.NAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.USERNAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.PHONE_NUMBER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.ALT_PHONE_NUMBER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.NEIGHBORHOOD_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CITY_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.ADDRESS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CONSUMER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.VOICEONLY_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.FRVOICEONLY_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.DISPATCHER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.TRAINED_DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CREATED_AT_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CONDO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.REFERRAL_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.EMAIL_VERIFIED_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CONSUMER_REQUEST_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.VOLUNTEER_REQUEST_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SPECIALIST_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.LOGISTICS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.BHS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.HELPLINE_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SITELINE_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.TRAINED_CUSTOMER_CARE_A_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.TRAINED_CUSTOMER_CARE_B_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.INREACH_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.OUTREACH_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.MARKETING_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.MODERATORS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.TRUST_LEVEL_4_COLUMN);
        //noinspection UnusedAssignment
        assertThat(header.get(index++)).isEqualTo(User.WORKFLOW_COLUMN);

        List<String> columns = rows.get(1);
        assertThat(header).hasSameSizeAs(columns);

        index = 0;
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.getId()));
        assertThat(columns.get(index++)).isEqualTo(u1.getName());
        assertThat(columns.get(index++)).isEqualTo(u1.getUserName());
        assertThat(columns.get(index++)).isEqualTo(u1.getPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u1.getAltPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u1.getNeighborhood());
        assertThat(columns.get(index++)).isEqualTo(u1.getCity());
        assertThat(columns.get(index++)).isEqualTo(u1.getAddress());
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isVoiceOnly()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isFRVoiceOnly()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isDispatcher()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isTrainedDriver()));
        assertThat(columns.get(index++)).isEqualTo(u1.getCreateTime());
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isCondo()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.getReferral()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.getEmailVerified()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.hasConsumerRequest()));
        assertThat(columns.get(index++)).isEqualTo(u1.getVolunteerRequest());
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isLogistics()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isSpecialist()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isBHS()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isHelpLine()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isSiteLine()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isTrainedCustomerCareA()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isTrainedCustomerCareB()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isInReach()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isOutReach()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isMarketing()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isModerator()));
        //noinspection UnusedAssignment
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isWorkflow()));

        // FIX THIS, DS: check all of the columns
    }

    @Test
    public void allMembersRawToFileTest() throws UserException, IOException {

        User u1 = createUserWithGroup(TEST_USER_NAME_1, Constants.GROUP_CONSUMERS);
        User u2 = createUserWithGroup(TEST_USER_NAME_2, Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup(TEST_USER_NAME_3, Constants.GROUP_DRIVERS);

        UserExporter exporter = new UserExporter(List.of(u1, u2, u3));
        String fileName = exporter.allMembersRawToFile("all-members-raw-test");

        String fileData = readFile(fileName);
        assertThat(fileData).contains(TEST_USER_NAME_1);
        assertThat(fileData).contains(TEST_USER_NAME_2);
        assertThat(fileData).contains(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void allMembersReportColumnsTest() throws UserException, IOException, CsvException {
        User u1 = createTestUser1();
        UserExporter exporter = new UserExporter(List.of(u1));

        String allMemberRows = exporter.allMembersReport();
        CSVListReader csvReader = new CSVListReader(new StringReader(allMemberRows));
        List<List<String>> rows = csvReader.readAllToList();
        assertThat(rows).hasSize(2);

        List<String> header = rows.get(0);
        assertThat(header).isEqualTo(User.reportCSVHeaders());

        // FIX THIS, DS: add a constant for number of columns expected
        int index = 0;
        assertThat(header.get(index++)).isEqualTo(User.SHORT_ID_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_CREATED_AT_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_NAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_USERNAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_PHONE_NUMBER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_ALT_PHONE_NUMBER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_NEIGHBORHOOD_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_CITY_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_ADDRESS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_CONDO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_REFERRAL_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_CONSUMER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_VOICEONLY_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_FRVOICEONLY_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_TRAINED_DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_DISPATCHER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_WORKFLOW_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_INREACH_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_OUTREACH_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_HELPLINE_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_SITELINE_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_TRAINED_CUSTOMER_CARE_A_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_TRAINED_CUSTOMER_CARE_B_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_MARKETING_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_MODERATORS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_TRUST_LEVEL_4_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_SPECIALIST_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_LOGISTICS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_BHS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_CUSTOMER_INFO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_ADVISOR_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_BOARD_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_COORDINATOR_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_LIMITED_RUNS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_AT_RISK_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_BIKERS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_OUT_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_EVENTS_DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_TRAINED_EVENT_DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_GONE_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_OTHER_DRIVERS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_ADMIN_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_CONSUMER_REQUEST_COLUMN);
        //noinspection UnusedAssignment
        assertThat(header.get(index++)).isEqualTo(User.SHORT_VOLUNTEER_REQUEST_COLUMN);

        List<String> columns = rows.get(1);
        assertThat(header).hasSameSizeAs(columns);

        index = 0;
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.getId()));
        assertThat(columns.get(index++)).isEqualTo(u1.getSimpleCreateTime());
        assertThat(columns.get(index++)).isEqualTo(u1.getName());
        assertThat(columns.get(index++)).isEqualTo(u1.getUserName());
        assertThat(columns.get(index++)).isEqualTo(u1.getPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u1.getAltPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u1.getNeighborhood());
        assertThat(columns.get(index++)).isEqualTo(u1.getCity());
        assertThat(columns.get(index++)).isEqualTo(u1.getAddress());
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isCondo()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.getReferral()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isVoiceOnly()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isFRVoiceOnly()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isTrainedDriver()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isDispatcher()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isWorkflow()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isInReach()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isOutReach()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isHelpLine()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isSiteLine()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isTrainedCustomerCareA()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isTrainedCustomerCareB()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isMarketing()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isModerator()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isTrustLevel4()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isSpecialist()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isLogistics()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isBHS()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isCustomerInfo()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isAdvisor()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isBoard()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isCoordinator()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isLimitedRuns()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isAtRisk()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isBiker()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isOut()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isEventDriver()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isTrainedEventDriver()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isGone()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isOtherDrivers()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isAdmin()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.hasConsumerRequest()));
        //noinspection UnusedAssignment
        assertThat(columns.get(index++)).isEqualTo(u1.getVolunteerRequest());
    }

    @Test
    public void allMembersReportToFileTest() throws UserException, IOException {

        User u1 = createUserWithGroup(TEST_USER_NAME_1, Constants.GROUP_CONSUMERS);
        User u2 = createUserWithGroup(TEST_USER_NAME_2, Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup(TEST_USER_NAME_3, Constants.GROUP_DRIVERS);

        UserExporter exporter = new UserExporter(List.of(u1, u2, u3));
        String fileName = exporter.allMembersReportToFile("allMembers.csv");

        String fileData = readFile(fileName);
        assertThat(fileData).contains(TEST_USER_NAME_1);
        assertThat(fileData).contains(TEST_USER_NAME_2);
        assertThat(fileData).contains(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void workflowColumnsTest() throws UserException, IOException, CsvException {
        User u1 = createTestUser1();
        UserExporter exporter = new UserExporter(List.of(u1));

        String workflowRows = exporter.workflow("", new HashMap<>());
        CSVListReader csvReader = new CSVListReader(new StringReader(workflowRows));
        List<List<String>> rows = csvReader.readAllToList();
        assertThat(rows).hasSize(2);

        List<String> header = rows.get(0);
        assertThat(header).isEqualTo(exporter.workflowHeaders());

        // FIX THIS, DS: add a constant for number of columns expected
        int index = 0;
        assertThat(header.get(index++)).isEqualTo(User.CONSUMER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.NAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.USERNAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.PHONE_NUMBER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.ALT_PHONE_NUMBER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.NEIGHBORHOOD_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CITY_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.ADDRESS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CONDO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(Constants.WORKFLOW_DETAILS_COLUMN);

        List<String> columns = rows.get(1);
        assertThat(header).hasSameSizeAs(columns);

        index = 0;
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns.get(index++)).isEqualTo(u1.getName());
        assertThat(columns.get(index++)).isEqualTo(u1.getUserName());
        assertThat(columns.get(index++)).isEqualTo(u1.getPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u1.getAltPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u1.getNeighborhood());
        assertThat(columns.get(index++)).isEqualTo(u1.getCity());
        assertThat(columns.get(index++)).isEqualTo(u1.getFullAddress());
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isCondo()));
    }

    @Test
    public void dispatchersColumnsTest() throws UserException, IOException, CsvException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_DISPATCHERS);
        UserExporter exporter = new UserExporter(List.of(u1));

        String dispatcherRows = exporter.dispatchers();
        CSVListReader csvReader = new CSVListReader(new StringReader(dispatcherRows));
        List<List<String>> rows = csvReader.readAllToList();
        assertThat(rows).hasSize(2);

        List<String> header = rows.get(0);
        assertThat(header).isEqualTo(exporter.dispatchersHeaders());

        // FIX THIS, DS: add a constant for number of columns expected
        int index = 0;
        assertThat(header.get(index++)).isEqualTo(User.CREATED_AT_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.NAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.USERNAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.PHONE_NUMBER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.NEIGHBORHOOD_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CITY_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.ADDRESS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CONDO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.CONSUMER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.DISPATCHER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.BHS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.HELPLINE_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SITELINE_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.INREACH_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.OUTREACH_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.MARKETING_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.MODERATORS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SPECIALIST_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.WORKFLOW_COLUMN);

        List<String> columns = rows.get(1);
        assertThat(header).hasSameSizeAs(columns);

        index = 0;
        assertThat(columns.get(index++)).isEqualTo(u1.getSimpleCreateTime());
        assertThat(columns.get(index++)).isEqualTo(u1.getName());
        assertThat(columns.get(index++)).isEqualTo(u1.getUserName());
        assertThat(columns.get(index++)).isEqualTo(u1.getPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u1.getNeighborhood());
        assertThat(columns.get(index++)).isEqualTo(u1.getCity());
        assertThat(columns.get(index++)).isEqualTo(u1.getAddress());
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isCondo()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isDispatcher()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isBHS()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isHelpLine()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isSiteLine()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isInReach()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isOutReach()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isMarketing()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isModerator()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isSpecialist()));
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.isWorkflow()));
    }

    @Test
    public void dispatchersToFileTest() throws UserException, IOException {

        User u1 = createUserWithGroup(TEST_USER_NAME_1, Constants.GROUP_DISPATCHERS);
        User u2 = createUserWithGroup(TEST_USER_NAME_2, Constants.GROUP_DISPATCHERS);
        User u3 = createUserWithGroup(TEST_USER_NAME_3, Constants.GROUP_DRIVERS);

        UserExporter exporter = new UserExporter(List.of(u1, u2, u3));
        String fileName = exporter.dispatchersToFile("dispatchers.csv");

        String fileData = readFile(fileName);
        assertThat(fileData).contains(TEST_USER_NAME_1);
        assertThat(fileData).contains(TEST_USER_NAME_2);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void nameWithCommaTest() throws UserException, IOException, CsvException {
        User u1 = createUserWithNoRequestsNoGroups("My name has, one comma");
        User u2 = createUserWithVolunteerRequest("My name, has, two commas", "Drive");

        UserExporter exporter = new UserExporter(List.of(u1, u2));

        String exportDataCSV = exporter.allMembersRaw();
        // Verify that we can parse it
        List<User> users = HBParser.users(exportDataCSV);
    }

    @Test
    public void cityAndNeighborhoodWithCommaTest() throws UserException, IOException, CsvException {
        User u1 = createUserWithCityAndNeighborhood("Berkeley", "hills, lower");
        User u2 = createUserWithCityAndNeighborhood("berkeley, east", "flats");

        UserExporter exporter = new UserExporter(List.of(u1, u2));

        String exportDataCSV = exporter.allMembersRaw();
        List<User> users = HBParser.users(exportDataCSV);
    }

    @Test
    public void addressWithCommaTest() throws UserException, IOException, CsvException {
        User u1 = createUserWithAddress("42, Comma, Ln");
        User u2 = createUserWithReferral("I.M. Referredman", "referral, with, commas");

        UserExporter exporter = new UserExporter(List.of(u1, u2));
        String exportDataCSV = exporter.allMembersRaw();
        List<User> users = HBParser.users(exportDataCSV);
    }
}
