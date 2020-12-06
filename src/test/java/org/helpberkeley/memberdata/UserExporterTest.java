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

import com.opencsv.exceptions.CsvException;
import org.junit.Test;

import java.io.IOException;
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

        UserExporter exporter = new UserExporter(List.of(u1, u2, u3, u4));
        String fileName = exporter.volunteerRequestsToFile("volunteerRequests.csv");

        String fileData = readFile(fileName);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_1);
        assertThat(fileData).contains(TEST_USER_NAME_2);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_3);
        assertThat(fileData).contains("u4");

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void driversToFileTest() throws UserException, IOException {

        User u1 = createUserWithGroup(TEST_USER_NAME_1, Constants.GROUP_CONSUMERS);
        User u2 = createUserWithGroup(TEST_USER_NAME_2, Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup(TEST_USER_NAME_3, Constants.GROUP_DRIVERS);

        UserExporter exporter = new UserExporter(List.of(u1, u2, u3));
        String fileName = exporter.driversToFile("driverRequests");

        String fileData = readFile(fileName);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_1);
        assertThat(fileData).contains(TEST_USER_NAME_2);
        assertThat(fileData).contains(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void allMembersRawColumnsTest() throws UserException {
        User u1 = createTestUser1();
        UserExporter exporter = new UserExporter(List.of(u1));

        String allMemberRows = exporter.allMembersRaw();
        String[] rows = allMemberRows.split("\n");
        assertThat(rows).hasSize(2);

        String header = rows[0];
        assertThat(header).isEqualTo(User.rawCSVHeaders().trim());

        // FIX THIS, DS: use CSVReader
        String[] headerColumns = header.split(Constants.CSV_SEPARATOR, -1);

        // FIX THIS, DS: add a constant for number of columns expected
        int index = 0;
        assertThat(headerColumns[index++]).isEqualTo(User.ID_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.NAME_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.USERNAME_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.PHONE_NUMBER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.ALT_PHONE_NUMBER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.NEIGHBORHOOD_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.CITY_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.ADDRESS_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.CONSUMER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.VOICEONLY_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.DISPATCHER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.DRIVER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.CREATED_AT_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.CONDO_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.REFERRAL_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.EMAIL_VERIFIED_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.CONSUMER_REQUEST_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.VOLUNTEER_REQUEST_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SPECIALIST_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.BHS_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.HELPLINE_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SITELINE_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.INREACH_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.OUTREACH_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.MARKETING_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.MODERATORS_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.TRUST_LEVEL_4_COLUMN);
        //noinspection UnusedAssignment
        assertThat(headerColumns[index++]).isEqualTo(User.WORKFLOW_COLUMN);

        // FIX THIS, DS: use CSVReader
        String[] columns = rows[1].split(Constants.CSV_SEPARATOR, -1);
        assertThat(headerColumns).hasSameSizeAs(columns);

        index = 0;
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.getId()));
        assertThat(columns[index++]).isEqualTo(u1.getName());
        assertThat(columns[index++]).isEqualTo(u1.getUserName());
        assertThat(columns[index++]).isEqualTo(u1.getPhoneNumber());
        assertThat(columns[index++]).isEqualTo(u1.getAltPhoneNumber());
        assertThat(columns[index++]).isEqualTo(u1.getNeighborhood());
        assertThat(columns[index++]).isEqualTo(u1.getCity());
        assertThat(columns[index++]).isEqualTo(u1.getAddress());
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isVoiceOnly()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isDispatcher()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns[index++]).isEqualTo(u1.getCreateTime());
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isCondo()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.getReferral()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.getEmailVerified()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.hasConsumerRequest()));
        assertThat(columns[index++]).isEqualTo(u1.getVolunteerRequest());
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isSpecialist()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isBHS()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isHelpLine()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isSiteLine()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isInReach()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isOutReach()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isMarketing()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isModerator()));
        //noinspection UnusedAssignment
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isWorkflow()));
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
    public void allMembersReportColumnsTest() throws UserException {
        User u1 = createTestUser1();
        UserExporter exporter = new UserExporter(List.of(u1));

        String allMemberRows = exporter.allMembersReport();
        String[] rows = allMemberRows.split("\n");
        assertThat(rows).hasSize(2);

        String header = rows[0];
        assertThat(header).isEqualTo(User.reportCSVHeaders().trim());

        // FIX THIS, DS: use CSVReader
        String[] headerColumns = header.split(Constants.CSV_SEPARATOR, -1);

        // FIX THIS, DS: add a constant for number of columns expected
        int index = 0;
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_ID_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_CREATED_AT_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_NAME_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_USERNAME_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_PHONE_NUMBER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_ALT_PHONE_NUMBER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_NEIGHBORHOOD_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_CITY_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_ADDRESS_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_CONDO_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_REFERRAL_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_CONSUMER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_VOICEONLY_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_DRIVER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_DISPATCHER_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_WORKFLOW_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_INREACH_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_OUTREACH_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_HELPLINE_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_SITELINE_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_MARKETING_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_MODERATORS_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_TRUST_LEVEL_4_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_SPECIALIST_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_BHS_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_CUSTOMER_INFO_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_ADVISOR_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_COORDINATOR_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_ADMIN_COLUMN);
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_CONSUMER_REQUEST_COLUMN);
        //noinspection UnusedAssignment
        assertThat(headerColumns[index++]).isEqualTo(User.SHORT_VOLUNTEER_REQUEST_COLUMN);

        // FIX THIS, DS: use CSVReader
        String[] columns = rows[1].split(Constants.CSV_SEPARATOR, -1);
        assertThat(headerColumns).hasSameSizeAs(columns);

        index = 0;
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.getId()));
        assertThat(columns[index++]).isEqualTo(u1.getSimpleCreateTime());
        assertThat(columns[index++]).isEqualTo(u1.getName());
        assertThat(columns[index++]).isEqualTo(u1.getUserName());
        assertThat(columns[index++]).isEqualTo(u1.getPhoneNumber());
        assertThat(columns[index++]).isEqualTo(u1.getAltPhoneNumber());
        assertThat(columns[index++]).isEqualTo(u1.getNeighborhood());
        assertThat(columns[index++]).isEqualTo(u1.getCity());
        assertThat(columns[index++]).isEqualTo(u1.getAddress());
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isCondo()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.getReferral()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isVoiceOnly()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isDispatcher()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isWorkflow()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isInReach()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isOutReach()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isHelpLine()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isSiteLine()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isMarketing()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isModerator()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isTrustLevel4()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isSpecialist()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isBHS()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isCustomerInfo()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isAdvisor()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isCoordinator()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.isAdmin()));
        assertThat(columns[index++]).isEqualTo(String.valueOf(u1.hasConsumerRequest()));
        //noinspection UnusedAssignment
        assertThat(columns[index++]).isEqualTo(u1.getVolunteerRequest());
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
        String[] rows = workflowRows.split("\n");
        assertThat(rows).hasSize(2);

        String header = rows[0];
        assertThat(header).isEqualTo(exporter.workflowHeaders().trim());

        // FIX THIS, DS: use CSVReader
        String[] headerColumns = header.split(Constants.CSV_SEPARATOR, -1);

        // FIX THIS, DS: add a constant for number of columns expected
        assertThat(headerColumns[0]).isEqualTo(User.CONSUMER_COLUMN);
        assertThat(headerColumns[1]).isEqualTo(User.DRIVER_COLUMN);
        assertThat(headerColumns[2]).isEqualTo(User.NAME_COLUMN);
        assertThat(headerColumns[3]).isEqualTo(User.USERNAME_COLUMN);
        assertThat(headerColumns[4]).isEqualTo(User.PHONE_NUMBER_COLUMN);
        assertThat(headerColumns[5]).isEqualTo(User.ALT_PHONE_NUMBER_COLUMN);
        assertThat(headerColumns[6]).isEqualTo(User.NEIGHBORHOOD_COLUMN);
        assertThat(headerColumns[7]).isEqualTo(User.CITY_COLUMN);
        assertThat(headerColumns[8]).isEqualTo(User.ADDRESS_COLUMN);
        assertThat(headerColumns[9]).isEqualTo(User.CONDO_COLUMN);
        // FIX THIS, DS: constant
        assertThat(headerColumns[10]).isEqualTo("Details");


        // FIX THIS, DS: use CSVReader
        String[] columns = rows[1].split(Constants.CSV_SEPARATOR, -1);
        assertThat(headerColumns).hasSameSizeAs(columns);

        assertThat(columns[0]).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns[1]).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns[2]).isEqualTo(u1.getName());
        assertThat(columns[3]).isEqualTo(u1.getUserName());
        assertThat(columns[4]).isEqualTo(u1.getPhoneNumber());
        assertThat(columns[5]).isEqualTo(u1.getAltPhoneNumber());
        assertThat(columns[6]).isEqualTo(u1.getNeighborhood());
        assertThat(columns[7]).isEqualTo(u1.getCity());
        assertThat(columns[8]).isEqualTo(u1.getAddress());
        assertThat(columns[9]).isEqualTo(String.valueOf(u1.isCondo()));
    }

    @Test
    public void dispatchersColumnsTest() throws UserException {
        User u1 = createUserWithGroup("u1", Constants.GROUP_DISPATCHERS);
        UserExporter exporter = new UserExporter(List.of(u1));

        String dispatcherRows = exporter.dispatchers();
        String[] rows = dispatcherRows.split("\n");
        assertThat(rows).hasSize(2);

        String header = rows[0];
        assertThat(header).isEqualTo(exporter.dispatchersHeaders().trim());

        // FIX THIS, DS: use CSVReader
        String[] headerColumns = header.split(Constants.CSV_SEPARATOR, -1);

        // FIX THIS, DS: add a constant for number of columns expected
        assertThat(headerColumns[0]).isEqualTo(User.CREATED_AT_COLUMN);
        assertThat(headerColumns[1]).isEqualTo(User.NAME_COLUMN);
        assertThat(headerColumns[2]).isEqualTo(User.USERNAME_COLUMN);
        assertThat(headerColumns[3]).isEqualTo(User.PHONE_NUMBER_COLUMN);
        assertThat(headerColumns[4]).isEqualTo(User.NEIGHBORHOOD_COLUMN);
        assertThat(headerColumns[5]).isEqualTo(User.CITY_COLUMN);
        assertThat(headerColumns[6]).isEqualTo(User.ADDRESS_COLUMN);
        assertThat(headerColumns[7]).isEqualTo(User.CONDO_COLUMN);
        assertThat(headerColumns[8]).isEqualTo(User.DRIVER_COLUMN);
        assertThat(headerColumns[9]).isEqualTo(User.CONSUMER_COLUMN);
        assertThat(headerColumns[10]).isEqualTo(User.DISPATCHER_COLUMN);
        assertThat(headerColumns[11]).isEqualTo(User.BHS_COLUMN);
        assertThat(headerColumns[12]).isEqualTo(User.HELPLINE_COLUMN);
        assertThat(headerColumns[13]).isEqualTo(User.SITELINE_COLUMN);
        assertThat(headerColumns[14]).isEqualTo(User.INREACH_COLUMN);
        assertThat(headerColumns[15]).isEqualTo(User.OUTREACH_COLUMN);
        assertThat(headerColumns[16]).isEqualTo(User.MARKETING_COLUMN);
        assertThat(headerColumns[17]).isEqualTo(User.MODERATORS_COLUMN);
        assertThat(headerColumns[18]).isEqualTo(User.SPECIALIST_COLUMN);
        assertThat(headerColumns[19]).isEqualTo(User.WORKFLOW_COLUMN);

        // FIX THIS, DS: use CSVReader
        String[] columns = rows[1].split(Constants.CSV_SEPARATOR, -1);
        assertThat(headerColumns).hasSameSizeAs(columns);

        assertThat(columns[0]).isEqualTo(u1.getSimpleCreateTime());
        assertThat(columns[1]).isEqualTo(u1.getName());
        assertThat(columns[2]).isEqualTo(u1.getUserName());
        assertThat(columns[3]).isEqualTo(u1.getPhoneNumber());
        assertThat(columns[4]).isEqualTo(u1.getNeighborhood());
        assertThat(columns[5]).isEqualTo(u1.getCity());
        assertThat(columns[6]).isEqualTo(u1.getAddress());
        assertThat(columns[7]).isEqualTo(String.valueOf(u1.isCondo()));
        assertThat(columns[8]).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns[9]).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns[10]).isEqualTo(String.valueOf(u1.isDispatcher()));
        assertThat(columns[11]).isEqualTo(String.valueOf(u1.isBHS()));
        assertThat(columns[12]).isEqualTo(String.valueOf(u1.isHelpLine()));
        assertThat(columns[13]).isEqualTo(String.valueOf(u1.isSiteLine()));
        assertThat(columns[14]).isEqualTo(String.valueOf(u1.isInReach()));
        assertThat(columns[15]).isEqualTo(String.valueOf(u1.isOutReach()));
        assertThat(columns[16]).isEqualTo(String.valueOf(u1.isMarketing()));
        assertThat(columns[17]).isEqualTo(String.valueOf(u1.isModerator()));
        assertThat(columns[18]).isEqualTo(String.valueOf(u1.isSpecialist()));
        assertThat(columns[19]).isEqualTo(String.valueOf(u1.isWorkflow()));
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
}
