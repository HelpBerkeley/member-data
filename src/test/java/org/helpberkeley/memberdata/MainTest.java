/*
 * Copyright (c) 2020-2023 helpberkeley.org
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
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Execute the individual commands, end-to-end.
 * More system path exercise rather than unit test.
 */
public class MainTest extends TestBase {

    @Before
    public void initialize() throws IOException, CsvException {
        cleanupGeneratedFiles();

        // Fetches files that will be used by the tests.
        String[] args = { Options.COMMAND_FETCH };
        Main.main(args);
    }

    @AfterClass
    public static void cleanup() throws IOException {
        cleanupGeneratedFiles();
    }

    private  static void cleanupGeneratedFiles() throws IOException {
        Files.list(Paths.get("."))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    String fileName = p.getFileName().toString();
                    if (fileName.endsWith(".csv") ||
                            (fileName.endsWith(".txt") && fileName.startsWith(Main.MEMBERDATA_ERRORS_FILE))) {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Test
    public void postUserErrorsTest() throws IOException, CsvException {
        String[] args = { Options.COMMAND_POST_ERRORS, TEST_FILE_NAME };
        Main.main(args);
    }

    @Test
    public void postMemberDataTest() throws IOException, CsvException {
        String memberDataFile = findFile(Main.MEMBERDATA_REPORT_FILE, "csv");
        assertThat(memberDataFile).isNotNull();

        String[] args = { Options.COMMAND_POST_ALL_MEMBERS, memberDataFile };
        Main.main(args);
    }

    @Test
    public void postDriversTest() throws IOException, CsvException {
        String file = findFile(Constants.DRIVERS_FILE, "csv");
        assertThat(file).isNotNull();

        String[] args = { Options.COMMAND_POST_DRIVERS, file };
        Main.main(args);
    }

    @Test
    public void postConsumerRequestsTest() throws IOException, CsvException {
        String consumerRequestsFile = findFile(Constants.CONSUMER_REQUESTS_FILE, "csv");
        String[] args = { Options.COMMAND_POST_CONSUMER_REQUESTS, consumerRequestsFile };
        Main.main(args);
    }

    @Test
    public void postVolunteerRequestsTest() throws IOException, CsvException {
        String consumerRequestsFile = findFile(Constants.VOLUNTEER_REQUESTS_FILE, "csv");
        String[] args = { Options.COMMAND_POST_VOLUNTEER_REQUESTS, consumerRequestsFile };
        Main.main(args);
    }

    @Test
    public void postDispatchersTest() throws IOException, CsvException {
        String file = findFile(Constants.DISPATCHERS_FILE, "csv");
        String[] args = { Options.COMMAND_POST_DISPATCHERS, file, TEST_SHORT_URL };
        Main.main(args);
    }

    @Test
    public void updateDispatchersTest() throws IOException, CsvException {
        String dispatchersFile = findFile(Constants.DISPATCHERS_FILE, "csv");
        String[] args = { Options.COMMAND_UPDATE_DISPATCHERS, dispatchersFile };
        Main.main(args);
    }

    @Test
    public void updateUserErrorsTest() throws IOException, CsvException {
        String errorsFile = findFile(Main.MEMBERDATA_ERRORS_FILE, "txt");
        String[] args = { Options.COMMAND_UPDATE_ERRORS, errorsFile };
        Main.main(args);
    }

    @Test
    public void emailTest() throws IOException, CsvException {
        String file = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_EMAIL, file };
        Main.main(args);
    }

    @Test
    public void inreachTest() throws IOException, CsvException {

        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");

        String[] args = new String[] {
                Options.COMMAND_INREACH,
                usersFile
        };
        Main.main(args);
    }

    @Test
    public void workflowTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_WORKFLOW, usersFile };
        Main.main(args);
    }

    @Test
    public void oneKitchenWorkflowTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_WORKFLOW, usersFile };
        Main.main(args);
    }

    @Test
    public void driverMessagesTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Succeeded");
    }

    @Test
    public void driverMessagesV300SheetTest() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "routed-deliveries-v300.csv");
        HttpClientSimulator.setQueryResponseData(Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, Constants.CONTROL_BLOCK_VERSION_300,
                Main.buildTopicURL(Constants.TOPIC_REQUEST_DRIVER_MESSAGES),
                Main.buildTopicURL(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES)));
    }

    @Test
    public void driverMessagesUnsupportedVersionTest() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "routed-deliveries-v1.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(MessageFormat.format(
                Main.UNSUPPORTED_CONTROL_BLOCK_VERSION, "0",
                Main.buildTopicURL(Constants.TOPIC_REQUEST_DRIVER_MESSAGES),
                Constants.CONTROL_BLOCK_VERSION_200));
    }

    @Test
    public void oneKitchenDriverMessagesTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Succeeded");
    }

    @Test
    public void oneKitchenDriverMessagesV200SheetTest() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "routed-deliveries-v200.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, Constants.CONTROL_BLOCK_VERSION_200,
                Main.buildTopicURL(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES),
            Main.buildTopicURL(Constants.TOPIC_REQUEST_DRIVER_MESSAGES)));
    }

    @Test
    public void oneKitchenDriverMessagesUnsupportedVersionTest() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "routed-deliveries-v1.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(MessageFormat.format(
                Main.UNSUPPORTED_CONTROL_BLOCK_VERSION, "0",
                Main.buildTopicURL(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES),
                Constants.CONTROL_BLOCK_VERSION_300));
    }

    @Test
    public void getRoutedWorkflowStatusTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, "last-routed-workflow-status.json");
        String[] args = { Options.COMMAND_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Succeeded");
    }

    @Test
    public void getRoutedWorkflowBadRequestTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, "last-routed-workflow-bad-request.json");
        String[] args = { Options.COMMAND_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Failed");
    }

    @Test
    public void completedOrdersTest() throws IOException, CsvException {
        LocalDate yesterday = LocalDate.now(Constants.TIMEZONE).minusDays(1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String yesterdayStr = yesterday.format(format);

        String completedOrdersRequest =
                "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
                        + "\"rows\": [ "
                        + "[ 42, null, \""
                        + yesterdayStr
                        + "\n[xyzzy.csv|attachment](upload://routed-deliveries-v200.csv) (5.8 KB)\" ] "
                        + "] }";
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY, completedOrdersRequest);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_DAILY_ORDERS, usersFile };
        WorkRequestHandler.clearLastStatusPost();
        Main.main(args);

        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
    }

    @Test
    public void completedOrdersInvalidDateTest() throws IOException, CsvException {
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY, "workrequest-no-date.json");
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_DAILY_ORDERS, usersFile };
        WorkRequestHandler.clearLastStatusPost();
        Main.main(args);

        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: " + WorkRequestHandler.RequestStatus.Failed);
        assertThat(statusPost.raw).contains(WorkRequestHandler.ERROR_INVALID_DATE);
        assertThat(statusPost.topic_id).isEqualTo(Main.COMPLETED_DAILY_DELIVERIES_TOPIC);
    }

    @Test
    public void completedOrdersFutureDateTest() throws IOException, CsvException {
        LocalDate nextWeek = LocalDate.now(Constants.TIMEZONE).plusWeeks(1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String nextWeekStr = nextWeek.format(format);

        String completedOrdersRequest =
            "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
            + "\"rows\": [ "
            + "[ 1, null, \""
            + nextWeekStr
            + "\n[xyzzy.csv|attachment](upload://routed-deliveries-v200.csv) (5.8 KB)\" ] "
            + "] }";
        HttpClientSimulator.setQueryResponseData(
            Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY, completedOrdersRequest);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_DAILY_ORDERS, usersFile };
        WorkRequestHandler.clearLastStatusPost();
        Main.main(args);

        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: " + WorkRequestHandler.RequestStatus.Failed);
        assertThat(statusPost.raw).contains("Invalid date");
        assertThat(statusPost.raw).contains("is in the future.");
        assertThat(statusPost.topic_id).isEqualTo(Main.COMPLETED_DAILY_DELIVERIES_TOPIC);
    }

    @Test
    public void completedOrdersDateTooOldTest() throws IOException, CsvException {
        LocalDate lastYear = LocalDate.now(Constants.TIMEZONE).minusYears(1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String lastYearStr = lastYear.format(format);

        String completedOrdersRequest =
                "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
                        + "\"rows\": [ "
                        + "[ 1, null, \""
                        + lastYearStr
                        + "\n[xyzzy.csv|attachment](upload://routed-deliveries-v200.csv) (5.8 KB)\" ] "
                        + "] }";
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY, completedOrdersRequest);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_DAILY_ORDERS, usersFile };
        WorkRequestHandler.clearLastStatusPost();
        Main.main(args);

        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: " + WorkRequestHandler.RequestStatus.Failed);
        assertThat(statusPost.raw).contains("Invalid date");
        assertThat(statusPost.raw).contains("is more than one week ago.");
        assertThat(statusPost.topic_id).isEqualTo(Main.COMPLETED_DAILY_DELIVERIES_TOPIC);
    }

    @Test
    public void completedOrdersDisableDateAuditTest() throws IOException, CsvException {
        LocalDate lastYear = LocalDate.now(Constants.TIMEZONE).minusYears(1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String lastYearStr = lastYear.format(format);

        String completedOrdersRequest =
                "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
                        + "\"rows\": [ "
                        + "[ 1, null, \""
                        + lastYearStr
                        + "\n"
                        + WorkRequestHandler.DISABLE_DATE_AUDIT
                        + "\n[xyzzy.csv|attachment](upload://routed-deliveries-v200.csv) (5.8 KB)\" ] "
                        + "] }";
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY, completedOrdersRequest);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_DAILY_ORDERS, usersFile };
        WorkRequestHandler.clearLastStatusPost();
        Main.main(args);

        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: " + WorkRequestHandler.RequestStatus.Succeeded);
        assertThat(statusPost.topic_id).isEqualTo(Main.COMPLETED_DAILY_DELIVERIES_TOPIC);
    }

    @Test
    public void completedOneKitchenOrdersTest() throws IOException, CsvException {
        LocalDate yesterday = LocalDate.now(Constants.TIMEZONE).minusDays(1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String yesterdayStr = yesterday.format(format);

        String completedOneKitchenOrdersRequest =
                "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
                        + "\"rows\": [ "
                        + "[ 42, null, \""
                        + yesterdayStr
                        + "\n[xyzzy.csv|attachment](upload://routed-deliveries-v300.csv) (5.8 KB)\" ] "
                        + "] }";
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_ONEKITCHEN_ORDERS_REPLY, completedOneKitchenOrdersRequest);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_ONEKITCHEN_ORDERS, usersFile };
        WorkRequestHandler.clearLastStatusPost();
        Main.main(args);

        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
    }

    @Test
    public void completedOneKitchenOrdersInTheFutureNegativeTest() throws IOException, CsvException {
        LocalDate tomorrow = LocalDate.now(Constants.TIMEZONE).plusDays(1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String tomorrowStr = tomorrow.format(format);

        String completedOneKitchenOrdersRequest =
                "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
                        + "\"rows\": [ "
                        + "[ 42, null, \""
                        + tomorrowStr
                        + "\n[xyzzy.csv|attachment](upload://routed-deliveries-v300.csv) (5.8 KB)\" ] "
                        + "] }";
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_ONEKITCHEN_ORDERS_REPLY, completedOneKitchenOrdersRequest);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_ONEKITCHEN_ORDERS, usersFile };
        WorkRequestHandler.clearLastStatusPost();
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        String statusMessage = WorkRequestHandler.getLastStatusPost().raw;
        assertThat(statusMessage).contains("Status: Failed");
        assertThat(statusMessage).contains(MessageFormat.format(Main.DATE_IS_IN_THE_FUTURE, tomorrowStr));
    }

    @Test
    public void completedOneKitchenOrdersInTheFutureDisableDateAuditTest() throws IOException, CsvException {
        LocalDate tomorrow = LocalDate.now(Constants.TIMEZONE).plusDays(1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String tomorrowStr = tomorrow.format(format);

        String completedOneKitchenOrdersRequest =
                "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
                        + "\"rows\": [ "
                        + "[ 42, null, \""
                        + tomorrowStr
                        + "\n"
                        + WorkRequestHandler.DISABLE_DATE_AUDIT
                        + "\n[xyzzy.csv|attachment](upload://routed-deliveries-v300.csv) (5.8 KB)\" ] "
                        + "] }";
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_ONEKITCHEN_ORDERS_REPLY, completedOneKitchenOrdersRequest);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_ONEKITCHEN_ORDERS, usersFile };
        WorkRequestHandler.clearLastStatusPost();
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        String statusMessage = WorkRequestHandler.getLastStatusPost().raw;
        assertThat(statusMessage).contains("Status: Succeeded");
    }

    @Test
    public void orderHistoryTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ORDER_HISTORY, usersFile };
        Main.main(args);
    }
    
    @Test
    public void driversTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_DRIVERS, usersFile };
        Main.main(args);

        String driversFile = readFile(findFile(Constants.DRIVERS_FILE, "csv"));
        assertThat(driversFile).contains("jbDriver,Y,Y,N,N,3,");
        assertThat(driversFile).contains("jsDriver,Y,Y,N,N,4,");
        assertThat(driversFile).contains("Xyzzy,Y,N,N,N,0,");
    }

    @Test
    public void driverHistoryTest() throws IOException, CsvException {
        String[] args = { Options.COMMAND_DRIVER_HISTORY };
        Main.main(args);
    }

    @Test
    public void oneKitchenDriverHistoryTest() throws IOException, CsvException {
        String[] args = { Options.COMMAND_ONEKITCHEN_DRIVER_HISTORY };
        Main.main(args);
    }

    @Test
    public void restaurantTemplateTest() throws IOException, CsvException {
        String[] args = { Options.COMMAND_RESTAURANT_TEMPLATE };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Succeeded");
    }

    @Test
    public void oneKitchenRestaurantTemplateTest() throws IOException, CsvException {
        String[] args = { Options.COMMAND_ONE_KITCHEN_RESTAURANT_TEMPLATE };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Succeeded");
    }

    @Test
    public void customerCareMemberPostTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_CUSTOMER_CARE_POST, usersFile };
        Main.main(args);
    }

    @Test
    public void frregPostTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_FRREG, usersFile };
        Main.main(args);
    }

    @Test
    public void workRequestsNoRequestsTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_WORK_REQUESTS, usersFile };
        Main.main(args);
    }

    @Test
    public void workRequestsAllRequestsTest() throws IOException, CsvException {
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_REQUESTS_LAST_REPLIES, "last-replies-all-requests.json");
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_WORK_REQUESTS, usersFile };
        Main.main(args);
        assertThat(Files.deleteIfExists(Paths.get(Constants.ONE_KITCHEN_WORKFLOW_REQUEST_FILE))).isTrue();
    }

    @Test
    public void workRequestsDailyWorkflowTest() throws IOException, CsvException {
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_REQUESTS_LAST_REPLIES, "last-replies-daily-workflow-request.json");
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_WORK_REQUESTS, usersFile };
        Main.main(args);
        assertThat(Files.deleteIfExists(Paths.get(Constants.DAILY_WORKFLOW_REQUEST_FILE))).isTrue();
    }

    @Test
    public void workRequestsBadRequestTest() throws IOException, CsvException {
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_REQUESTS_LAST_REPLIES, "last-replies-bad-request.json");
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_WORK_REQUESTS, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
    }

    @Test
    public void completedOneKitchenV200Test() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", "routed-deliveries-v200.csv")
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_ONEKITCHEN_ORDERS_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_ONEKITCHEN_ORDERS, usersFile };
        Main.main(args);
        System.out.println(WorkRequestHandler.getLastStatusPost().raw);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, Constants.CONTROL_BLOCK_VERSION_200,
                Main.buildTopicURL(Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS),
                Main.buildTopicURL(Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS)));
    }

    @Test
    public void completedDailyOrdersV300Test() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", "routed-deliveries-v300.csv")
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_DAILY_ORDERS, usersFile };
        Main.main(args);
        System.out.println(WorkRequestHandler.getLastStatusPost().raw);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, Constants.CONTROL_BLOCK_VERSION_300,
                Main.buildTopicURL(Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS),
                Main.buildTopicURL(Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS)));
    }

    @Test
    public void postOneKitchenRestaurantTemplateV200Test() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", "restaurant-template-v200.csv")
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_ONE_KITCHEN_RESTAURANT_TEMPLATE_REPLY, request);
        String[] args = { Options.COMMAND_ONE_KITCHEN_RESTAURANT_TEMPLATE };
        Main.main(args);
        System.out.println(WorkRequestHandler.getLastStatusPost().raw);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, Constants.CONTROL_BLOCK_VERSION_200,
                Main.buildTopicURL(Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE),
                Main.buildTopicURL(Constants.TOPIC_POST_RESTAURANT_TEMPLATE)));
    }

    @Test
    public void postRestaurantTemplateV300Test() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", "restaurant-template-v300.csv")
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_RESTAURANT_TEMPLATE_REPLY, request);
        String[] args = { Options.COMMAND_RESTAURANT_TEMPLATE };
        Main.main(args);
        System.out.println(WorkRequestHandler.getLastStatusPost().raw);
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, Constants.CONTROL_BLOCK_VERSION_300,
                Main.buildTopicURL(Constants.TOPIC_POST_RESTAURANT_TEMPLATE),
                Main.buildTopicURL(Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE)));
    }

    private String findFile(final String prefix, final String suffix) {

        File dir = new File(".");
        File[] files = dir.listFiles((dir1, name) -> name.startsWith(prefix) && name.endsWith(suffix));

        assertThat(files).isNotNull();
        assertThat(files).hasSize(1);
        return files[0].getName();
    }
}
