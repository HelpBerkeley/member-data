/*
 * Copyright (c) 2020. helpberkeley.org
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
 *
 */
package org.helpberkeley.memberdata;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExporterTest extends TestBase {
    @Test
    public void errorsToFileTest() throws UserException, IOException {

        List<User> users = new ArrayList<>();

        try {
            User u1 = createUserWithUserNameCityAndNeighborhood(
                    TEST_USER_NAME_1, Constants.BERKELEY, "unknown");
        } catch (UserException ex) {
            assertThat(ex.user).isNotNull();
            assertThat(ex.user.getUserName()).isEqualTo(TEST_USER_NAME_1);
            users.add(ex.user);
        }

        User u2 = createUserWithUserNameCityAndNeighborhood(
                TEST_USER_NAME_2, Constants.ALBANY, "Solano");
        users.add(u2);

        User u3 = createUserWithUserNameCityAndNeighborhood(
                TEST_USER_NAME_3, "Altoona", "unknown");
        users.add(u3);

        Exporter exporter = new Exporter(users);
        String fileName = exporter.errorsToFile("errorsToFileTest.txt");
        Path filePath = Paths.get(fileName);
        assertThat(filePath).exists();

        String errorFileData = Files.readString(filePath);
        assertThat(errorFileData).contains(TEST_USER_NAME_1);
        assertThat(errorFileData).doesNotContain(TEST_USER_NAME_2);
        assertThat(errorFileData).doesNotContain(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void consumerRequestsToFileTest() throws UserException, IOException {

        User u1 = createUserWithNoRequestsNoGroups(TEST_USER_NAME_1);
        User u2 = createUserWithConsumerRequest(TEST_USER_NAME_2, true);

        Exporter exporter = new Exporter(List.of(u1, u2));
        String fileName = exporter.consumerRequestsToFile("consumerRequests.csv");
        Path filePath = Paths.get(fileName);
        assertThat(filePath).exists();
        
        String errorFileData = Files.readString(filePath);
        assertThat(errorFileData).contains(TEST_USER_NAME_2);
        assertThat(errorFileData).doesNotContain(TEST_USER_NAME_1);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void volunteerRequestsToFileTest() throws UserException, IOException {

        User u1 = createUserWithNoRequestsNoGroups(TEST_USER_NAME_1);
        User u2 = createUserWithVolunteerRequest(TEST_USER_NAME_2, "Drive");
        User u3 = createUserWithNoRequestsNoGroups(TEST_USER_NAME_3);
        User u4 = createUserWithVolunteerRequest("u4", "Dispatch");

        Exporter exporter = new Exporter(List.of(u1, u2, u3, u4));
        String fileName = exporter.volunteerRequestsToFile("volunteerRequests.csv");
        Path filePath = Paths.get(fileName);
        assertThat(filePath).exists();

        String errorFileData = Files.readString(filePath);
        assertThat(errorFileData).doesNotContain(TEST_USER_NAME_1);
        assertThat(errorFileData).contains(TEST_USER_NAME_2);
        assertThat(errorFileData).doesNotContain(TEST_USER_NAME_3);
        assertThat(errorFileData).contains("u4");

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void driversToFileTest() throws UserException, IOException {

        User u1 = createUserWithGroup(TEST_USER_NAME_1, Constants.GROUP_CONSUMERS);
        User u2 = createUserWithGroup(TEST_USER_NAME_2, Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup(TEST_USER_NAME_3, Constants.GROUP_DRIVERS);

        Exporter exporter = new Exporter(List.of(u1, u2, u3));
        String fileName = exporter.driversToFile("driverRequests.csv");
        Path filePath = Paths.get(fileName);
        assertThat(filePath).exists();

        String errorFileData = Files.readString(filePath);
        assertThat(errorFileData).doesNotContain(TEST_USER_NAME_1);
        assertThat(errorFileData).contains(TEST_USER_NAME_2);
        assertThat(errorFileData).contains(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void allMemberColumnsTest() throws UserException {
        User u1 = createTestUser1();
        Exporter exporter = new Exporter(List.of(u1));

        String allMemberRows = exporter.allMembers();
        String[] rows = allMemberRows.split("\n");
        assertThat(rows).hasSize(2);

        String header = rows[0];
        assertThat(header).isEqualTo(User.csvHeaders(Constants.CSV_SEPARATOR).trim());

        String[] headerColumns = header.split(Constants.CSV_SEPARATOR);

        // FIX THIS, DS: add a constant for number of columns expected
        assertThat(headerColumns[0]).isEqualTo(User.ID_COLUMN);
        assertThat(headerColumns[1]).isEqualTo(User.NAME_COLUMN);
        assertThat(headerColumns[2]).isEqualTo(User.USERNAME_COLUMN);
        assertThat(headerColumns[3]).isEqualTo(User.PHONE_NUMBER_COLUMN);
        assertThat(headerColumns[4]).isEqualTo(User.NEIGHBORHOOD_COLUMN);
        assertThat(headerColumns[5]).isEqualTo(User.CITY_COLUMN);
        assertThat(headerColumns[6]).isEqualTo(User.ADDRESS_COLUMN);
        assertThat(headerColumns[7]).isEqualTo(User.CONSUMER_COLUMN);
        assertThat(headerColumns[8]).isEqualTo(User.DISPATCHER_COLUMN);
        assertThat(headerColumns[9]).isEqualTo(User.DRIVER_COLUMN);
        assertThat(headerColumns[10]).isEqualTo(User.CREATED_AT_COLUMN);
        assertThat(headerColumns[11]).isEqualTo(User.APARTMENT_COLUMN);
        assertThat(headerColumns[12]).isEqualTo(User.CONSUMER_REQUEST_COLUMN);
        assertThat(headerColumns[13]).isEqualTo(User.VOLUNTEER_REQUEST_COLUMN);
        assertThat(headerColumns[14]).isEqualTo(User.SPECIALIST_COLUMN);
        assertThat(headerColumns[15]).isEqualTo(User.EMAIL_COLUMN);

        String[] columns = rows[1].split(Constants.CSV_SEPARATOR);
        assertThat(headerColumns).hasSameSizeAs(columns);

        assertThat(columns[0]).isEqualTo(String.valueOf(u1.getId()));
        assertThat(columns[1]).isEqualTo(u1.getName());
        assertThat(columns[2]).isEqualTo(u1.getUserName());
        assertThat(columns[3]).isEqualTo(u1.getPhoneNumber());
        assertThat(columns[4]).isEqualTo(u1.getNeighborhood());
        assertThat(columns[5]).isEqualTo(u1.getCity());
        assertThat(columns[6]).isEqualTo(u1.getAddress());
        assertThat(columns[7]).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns[8]).isEqualTo(String.valueOf(u1.isDispatcher()));
        assertThat(columns[9]).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns[10]).isEqualTo(u1.getCreateTime());
        assertThat(columns[11]).isEqualTo(String.valueOf(u1.isApartment()));
        assertThat(columns[12]).isEqualTo(String.valueOf(u1.hasConsumerRequest()));
        assertThat(columns[13]).isEqualTo(u1.getVolunteerRequest());
        assertThat(columns[14]).isEqualTo(String.valueOf(u1.isSpecialist()));
        assertThat(columns[15]).isEqualTo(u1.getEmail());
    }

    @Test
    public void allMembersToFileTest() throws UserException, IOException {

        User u1 = createUserWithGroup(TEST_USER_NAME_1, Constants.GROUP_CONSUMERS);
        User u2 = createUserWithGroup(TEST_USER_NAME_2, Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup(TEST_USER_NAME_3, Constants.GROUP_DRIVERS);

        Exporter exporter = new Exporter(List.of(u1, u2, u3));
        String fileName = exporter.allMembersToFile("allMembers.csv");
        Path filePath = Paths.get(fileName);
        assertThat(filePath).exists();

        String errorFileData = Files.readString(filePath);
        assertThat(errorFileData).contains(TEST_USER_NAME_1);
        assertThat(errorFileData).contains(TEST_USER_NAME_2);
        assertThat(errorFileData).contains(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void workflowColumnsTest() throws UserException {
        User u1 = createTestUser1();
        Exporter exporter = new Exporter(List.of(u1));

        String workflowRows = exporter.workflow();
        String[] rows = workflowRows.split("\n");
        assertThat(rows).hasSize(2);

        String header = rows[0];
        assertThat(header).isEqualTo(exporter.workflowHeaders().trim());

        String[] headerColumns = header.split(Constants.CSV_SEPARATOR);

        // FIX THIS, DS: add a constant for number of columns expected
        assertThat(headerColumns[0]).isEqualTo(User.CONSUMER_COLUMN);
        assertThat(headerColumns[1]).isEqualTo(User.DRIVER_COLUMN);
        assertThat(headerColumns[2]).isEqualTo(User.NAME_COLUMN);
        assertThat(headerColumns[3]).isEqualTo(User.USERNAME_COLUMN);
        assertThat(headerColumns[4]).isEqualTo(User.PHONE_NUMBER_COLUMN);
        assertThat(headerColumns[5]).isEqualTo(User.NEIGHBORHOOD_COLUMN);
        assertThat(headerColumns[6]).isEqualTo(User.CITY_COLUMN);
        assertThat(headerColumns[7]).isEqualTo(User.ADDRESS_COLUMN);
        assertThat(headerColumns[8]).isEqualTo(User.APARTMENT_COLUMN);


        String[] columns = rows[1].split(Constants.CSV_SEPARATOR);
        assertThat(headerColumns).hasSameSizeAs(columns);

        assertThat(columns[0]).isEqualTo(String.valueOf(u1.isConsumer()));
        assertThat(columns[1]).isEqualTo(String.valueOf(u1.isDriver()));
        assertThat(columns[2]).isEqualTo(u1.getName());
        assertThat(columns[3]).isEqualTo(u1.getUserName());
        assertThat(columns[4]).isEqualTo(u1.getPhoneNumber());
        assertThat(columns[5]).isEqualTo(u1.getNeighborhood());
        assertThat(columns[6]).isEqualTo(u1.getCity());
        assertThat(columns[7]).isEqualTo(u1.getAddress());
        assertThat(columns[8]).isEqualTo(String.valueOf(u1.isApartment()));
    }

    @Test
    public void workflowToFileTest() throws UserException, IOException {

        User u1 = createUserWithGroup(TEST_USER_NAME_1, Constants.GROUP_CONSUMERS);
        User u2 = createUserWithGroup(TEST_USER_NAME_2, Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup(TEST_USER_NAME_3, Constants.GROUP_DRIVERS);

        Exporter exporter = new Exporter(List.of(u1, u2, u3));
        String fileName = exporter.workflowToFile("workflow.csv");
        Path filePath = Paths.get(fileName);
        assertThat(filePath).exists();

        String errorFileData = Files.readString(filePath);
        assertThat(errorFileData).contains(TEST_USER_NAME_1);
        assertThat(errorFileData).contains(TEST_USER_NAME_2);
        assertThat(errorFileData).contains(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }
}
