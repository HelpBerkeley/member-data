/*
 * Copyright (c) 2021-2024. helpberkeley.org
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

import com.opencsv.exceptions.CsvException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverExporterTest extends TestBase {

    @Test
    public void driversToFileTest() throws UserException, IOException {

        User u1 = createUserWithGroup(TEST_USER_NAME_1, Constants.GROUP_CONSUMERS);
        User u2 = createUserWithGroup(TEST_USER_NAME_2, Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup(TEST_USER_NAME_3, Constants.GROUP_DRIVERS);

        DriverExporter exporter = new DriverExporter(List.of(u1, u2, u3), Map.of(), Map.of());

        String fileName = exporter.driversToFile();

        String fileData = readFile(fileName);
        assertThat(fileData).doesNotContain(TEST_USER_NAME_1);
        assertThat(fileData).contains(TEST_USER_NAME_2);
        assertThat(fileData).contains(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void wellFormedCSVTest() throws UserException, IOException, CsvException {
        User u1 = createTestUser1WithGroups(
                Constants.GROUP_DRIVERS,
                Constants.GROUP_TRAINED_DRIVERS,
                Constants.GROUP_LIMITED,
                Constants.GROUP_AT_RISK,
                Constants.GROUP_BIKERS,
                Constants.GROUP_OUT,
                Constants.GROUP_EVENT_DRIVERS,
                Constants.GROUP_TRAINED_EVENT_DRIVERS,
                Constants.GROUP_GONE,
                Constants.GROUP_OTHER_DRIVERS);

        User u2 = createTestUser2WithGroups(
                Constants.GROUP_DRIVERS,
                Constants.GROUP_LIMITED,
                Constants.GROUP_BIKERS,
                Constants.GROUP_EVENT_DRIVERS,
                Constants.GROUP_GONE);

        User u3 = createTestUser3WithGroups(
                Constants.GROUP_DRIVERS,
                Constants.GROUP_TRAINED_DRIVERS,
                Constants.GROUP_AT_RISK,
                Constants.GROUP_OUT,
                Constants.GROUP_TRAINED_EVENT_DRIVERS,
                Constants.GROUP_OTHER_DRIVERS);

        Map<String, DetailsPost> details = new HashMap<>();
        String u1Details = "twas brillig and the slithy toves";
        String u2Details = "all mimsy were the borogroves";
        String u3Details = "";

        DetailsPost detailsPost = new DetailsPost();
        detailsPost.setDetails(1, u1Details);
        details.put(u1.getUserName(), detailsPost);

        detailsPost = new DetailsPost();
        detailsPost.setDetails(2, u2Details);
        details.put(u2.getUserName(), detailsPost);

        DriverExporter exporter = new DriverExporter(List.of(u1, u2, u3), Map.of(), details);

        String fileName = exporter.driversToFile();

        String csvData = readFile(fileName);
        assertThat(csvData).contains(TEST_USER_NAME_1);
        assertThat(csvData).contains(TEST_USER_NAME_2);
        assertThat(csvData).contains(TEST_USER_NAME_3);

        CSVListReader csvReader = new CSVListReader(new StringReader(csvData));
        List<List<String>> rows = csvReader.readAllToList();

        List<String> header = rows.get(0);
        assertThat(header).isEqualTo(exporter.driverHeaders());

        assertThat(header).hasSize(26);

        int index = 0;
        assertThat(header.get(index++)).isEqualTo(User.CREATED_AT_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.NAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.USERNAME_COLUMN);
        assertThat(header.get(index++)).isEqualTo(DriverExporter.IN_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_TRAINED_DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_BIKERS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_LIMITED_RUNS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(DriverExporter.TOTAL_RUNS_COLUMNS);
        assertThat(header.get(index++)).isEqualTo(DriverExporter.SIX_WEEKS_AGO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(DriverExporter.FIVE_WEEKS_AGO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(DriverExporter.FOUR_WEEKS_AGO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(DriverExporter.THREE_WEEKS_AGO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(DriverExporter.TWO_WEEKS_AGO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(DriverExporter.ONE_WEEK_AGO_COLUMN);
        assertThat(header.get(index++)).isEqualTo(DriverExporter.THIS_WEEK_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_PHONE_NUMBER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_ALT_PHONE_NUMBER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_CITY_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_ADDRESS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_AT_RISK_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_GONE_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_OUT_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_OTHER_DRIVERS_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_EVENTS_DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_TRAINED_EVENT_DRIVER_COLUMN);
        assertThat(header.get(index++)).isEqualTo(User.SHORT_DRIVER_DETAILS_COLUMN);

        // Tables.drivers() sort order is going to return these as u3, u1, u2

        List<String> columns = rows.get(1);
        assertThat(header).hasSameSizeAs(columns);

        index = 0;
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u3.getCreateDate()));
        assertThat(columns.get(index++)).isEqualTo(u3.getName());
        assertThat(columns.get(index++)).isEqualTo(u3.getUserName());
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isAvailableDriver()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isTrainedDriver()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isBiker()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isLimitedRuns()));
        assertThat(columns.get(index++)).isEqualTo("0");
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEqualTo(u3.getPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u3.getAltPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u3.getCity());
        assertThat(columns.get(index++)).isEqualTo(u3.getAddress());
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isAtRisk()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isGone()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isOut()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isOtherDrivers()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isEventDriver()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u3.isTrainedEventDriver()));
        assertThat(columns.get(index++)).isEqualTo(u3Details);

        columns = rows.get(2);
        assertThat(header).hasSameSizeAs(columns);

        index = 0;
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u1.getCreateDate()));
        assertThat(columns.get(index++)).isEqualTo(u1.getName());
        assertThat(columns.get(index++)).isEqualTo(u1.getUserName());
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isAvailableDriver()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isTrainedDriver()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isBiker()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isLimitedRuns()));
        assertThat(columns.get(index++)).isEqualTo("0");
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEqualTo(u1.getPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u1.getAltPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u1.getCity());
        assertThat(columns.get(index++)).isEqualTo(u1.getAddress());
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isAtRisk()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isGone()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isOut()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isOtherDrivers()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isEventDriver()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u1.isTrainedEventDriver()));
        assertThat(columns.get(index++)).isEqualTo(u1Details);

        columns = rows.get(3);
        assertThat(header).hasSameSizeAs(columns);

        index = 0;
        assertThat(columns.get(index++)).isEqualTo(String.valueOf(u2.getCreateDate()));
        assertThat(columns.get(index++)).isEqualTo(u2.getName());
        assertThat(columns.get(index++)).isEqualTo(u2.getUserName());
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isAvailableDriver()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isTrainedDriver()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isBiker()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isLimitedRuns()));
        assertThat(columns.get(index++)).isEqualTo("0");
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEmpty();
        assertThat(columns.get(index++)).isEqualTo(u2.getPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u2.getAltPhoneNumber());
        assertThat(columns.get(index++)).isEqualTo(u2.getCity());
        assertThat(columns.get(index++)).isEqualTo(u2.getAddress());
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isAtRisk()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isGone()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isOut()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isOtherDrivers()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isEventDriver()));
        assertThat(columns.get(index++)).isEqualTo(shortBoolean(u2.isTrainedEventDriver()));
        assertThat(columns.get(index++)).isEqualTo(u2Details);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void shortPostNoAvailableDriversTest() throws UserException {

        User u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS);
        Tables tables = new Tables(List.of(u1));
        assertThat(tables.availableDrivers()).isEmpty();

        u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS);
        tables = new Tables(List.of(u1));
        assertThat(tables.availableDrivers()).containsExactly(u1);

        u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS, Constants.GROUP_BIKERS);
        tables = new Tables(List.of(u1));
        assertThat(tables.availableDrivers()).containsExactly(u1);

        u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS,
                Constants.GROUP_BIKERS, Constants.GROUP_AT_RISK);
        tables = new Tables(List.of(u1));
        assertThat(tables.availableDrivers()).containsExactly(u1);

        u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS,
                Constants.GROUP_BIKERS, Constants.GROUP_AT_RISK, Constants.GROUP_LIMITED);
        tables = new Tables(List.of(u1));
        assertThat(tables.availableDrivers()).containsExactly(u1);

        u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS, Constants.GROUP_OUT);
        tables = new Tables(List.of(u1));
        assertThat(tables.availableDrivers()).isEmpty();

        u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS, Constants.GROUP_GONE);
        tables = new Tables(List.of(u1));
        assertThat(tables.availableDrivers()).isEmpty();
    }

    @Test
    public void detailedDriverNoDetailsTest() throws UserException {

        User u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS);
        User u2 = createTestUser2WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS);
        User u3 = createTestUser3WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS);

        List<User> drivers = List.of(u3, u1, u2);
        DriverExporter driverExporter = new DriverExporter(drivers, Map.of(), Map.of());

        assertThat(driverExporter.availableDriversWithDetailsPosts()).isEmpty();

        List<DetailedDriver> detailedDrivers = driverExporter.availableDetailedDriversByReverseCreationDate();

        assertThat(detailedDrivers.get(0).getUserName()).isEqualTo(u2.getUserName());
        assertThat(detailedDrivers.get(0).getLatestDetailsPostNumber()).isEqualTo(0L);
        assertThat(detailedDrivers.get(0).getDetails()).isEmpty();
        assertThat(detailedDrivers.get(1).getUserName()).isEqualTo(u3.getUserName());
        assertThat(detailedDrivers.get(1).getLatestDetailsPostNumber()).isEqualTo(0L);
        assertThat(detailedDrivers.get(1).getDetails()).isEmpty();
        assertThat(detailedDrivers.get(2).getUserName()).isEqualTo(u1.getUserName());
        assertThat(detailedDrivers.get(2).getLatestDetailsPostNumber()).isEqualTo(0L);
        assertThat(detailedDrivers.get(2).getDetails()).isEmpty();
    }

    @Test
    public void detailedDriverAllDetailsSortTest() throws UserException {

        User u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS);
        User u2 = createTestUser2WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS);
        User u3 = createTestUser3WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS);

        List<User> drivers = List.of(u1, u2, u3);

        Map<String, DetailsPost> details = new HashMap<>();
        String u1Details = "twas brillig and the slithy toves";
        String u2Details = "all mimsy were the borogroves";
        String u3Details = "did gyre and gimble in the wabe";

        DetailsPost detailsPost = new DetailsPost();
        detailsPost.setDetails(1, u1Details);
        details.put(u1.getUserName(), detailsPost);

        detailsPost = new DetailsPost();
        detailsPost.setDetails(3, u2Details);
        details.put(u2.getUserName(), detailsPost);

        detailsPost = new DetailsPost();
        detailsPost.setDetails(2, u3Details);
        details.put(u3.getUserName(), detailsPost);

        DriverExporter driverExporter = new DriverExporter(drivers, Map.of(), details);
        List<DetailedDriver> detailedDrivers = driverExporter.availableDriversWithDetailsPosts();

        assertThat(detailedDrivers).hasSameSizeAs(drivers);

        assertThat(detailedDrivers.get(0).getUserName()).isEqualTo(u2.getUserName());
        assertThat(detailedDrivers.get(0).getLatestDetailsPostNumber()).isEqualTo(3);
        assertThat(detailedDrivers.get(0).getDetails()).isEqualTo(u2Details);
        assertThat(detailedDrivers.get(1).getUserName()).isEqualTo(u3.getUserName());
        assertThat(detailedDrivers.get(1).getLatestDetailsPostNumber()).isEqualTo(2);
        assertThat(detailedDrivers.get(1).getDetails()).isEqualTo(u3Details);
        assertThat(detailedDrivers.get(2).getUserName()).isEqualTo(u1.getUserName());
        assertThat(detailedDrivers.get(2).getLatestDetailsPostNumber()).isEqualTo(1);
        assertThat(detailedDrivers.get(2).getDetails()).isEqualTo(u1Details);
    }

    @Test
    public void availableEventDriversTest() throws UserException {
        User u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS);
        User u2 = createTestUser2WithGroups(Constants.GROUP_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS);
        User u3 = createTestUser3WithGroups(Constants.GROUP_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS);
        List<User> drivers = List.of(u1, u2, u3);
        DriverExporter driverExporter = new DriverExporter(drivers, Map.of(), Map.of());

        List<DetailedDriver> detailedDrivers =
                driverExporter.availableDetailedEventDriversByReverseCreationDate();
        assertThat(detailedDrivers).hasSameSizeAs(drivers);
        assertThat(detailedDrivers.get(0).getUserName()).isEqualTo(u2.getUserName());
        assertThat(detailedDrivers.get(1).getUserName()).isEqualTo(u3.getUserName());
        assertThat(detailedDrivers.get(2).getUserName()).isEqualTo(u1.getUserName());
    }

    @Test
    public void availableEventDriversWithDetailsTest() throws UserException {
        User u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS);
        User u2 = createTestUser2WithGroups(Constants.GROUP_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS);
        User u3 = createTestUser3WithGroups(Constants.GROUP_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS);
        List<User> drivers = List.of(u1, u2, u3);

        Map<String, DetailsPost> details = new HashMap<>();
        String u1Details = "twas brillig and the slithy toves";
        String u2Details = "all mimsy were the borogroves";
        DetailsPost detailsPost = new DetailsPost();
        detailsPost.setDetails(1, u1Details);
        details.put(u1.getUserName(), detailsPost);

        detailsPost = new DetailsPost();
        detailsPost.setDetails(3, u2Details);
        details.put(u2.getUserName(), detailsPost);

        DriverExporter driverExporter = new DriverExporter(drivers, Map.of(), details);

        List<DetailedDriver> detailedDrivers =
                driverExporter.availableEventDriversWithDetailsPosts();
        assertThat(detailedDrivers).hasSize(2);
        assertThat(detailedDrivers.get(0).getUserName()).isEqualTo(u2.getUserName());
        assertThat(detailedDrivers.get(1).getUserName()).isEqualTo(u1.getUserName());
    }

    @Test
    public void availableDriversAreAvailableEventDriversTest() throws UserException {
        User u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS);
        User u2 = createTestUser2WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS);
        User u3 = createTestUser3WithGroups(Constants.GROUP_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS);
        List<User> drivers = List.of(u1, u2, u3);
        DriverExporter driverExporter = new DriverExporter(drivers, Map.of(), Map.of());

        List<DetailedDriver> detailedDrivers =
                driverExporter.availableDetailedEventDriversByReverseCreationDate();
        assertThat(detailedDrivers).hasSameSizeAs(drivers);
        assertThat(detailedDrivers.get(0).getUserName()).isEqualTo(u2.getUserName());
        assertThat(detailedDrivers.get(1).getUserName()).isEqualTo(u3.getUserName());
        assertThat(detailedDrivers.get(2).getUserName()).isEqualTo(u1.getUserName());
    }

    @Test
    public void availableDriversWithDetailsAreEventDriversTest() throws UserException {
        User u1 = createTestUser1WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS);
        User u2 = createTestUser2WithGroups(Constants.GROUP_DRIVERS, Constants.GROUP_TRAINED_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS);
        User u3 = createTestUser3WithGroups(Constants.GROUP_DRIVERS,
                Constants.GROUP_EVENT_DRIVERS, Constants.GROUP_TRAINED_EVENT_DRIVERS);
        List<User> drivers = List.of(u1, u2, u3);

        Map<String, DetailsPost> details = new HashMap<>();
        String u1Details = "twas brillig and the slithy toves";
        String u2Details = "all mimsy were the borogroves";
        DetailsPost detailsPost = new DetailsPost();
        detailsPost.setDetails(1, u1Details);
        details.put(u1.getUserName(), detailsPost);

        detailsPost = new DetailsPost();
        detailsPost.setDetails(3, u2Details);
        details.put(u2.getUserName(), detailsPost);

        DriverExporter driverExporter = new DriverExporter(drivers, Map.of(), details);

        List<DetailedDriver> detailedDrivers =
                driverExporter.availableEventDriversWithDetailsPosts();
        assertThat(detailedDrivers).hasSize(2);
        assertThat(detailedDrivers.get(0).getUserName()).isEqualTo(u2.getUserName());
        assertThat(detailedDrivers.get(1).getUserName()).isEqualTo(u1.getUserName());
    }
}
