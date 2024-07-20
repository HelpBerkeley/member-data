/*
 * Copyright (c) 2020-2024 helpberkeley.org
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

        WorkRequestHandler.clearLastStatusPost();
    }

    @AfterClass
    public static void cleanup() throws IOException {
        cleanupGeneratedFiles();
        WorkRequestHandler.clearLastStatusPost();
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
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.raw).contains(MessageFormat.format(Main.MESSAGES_POST_TO,
                Constants.TOPIC_DRIVERS_POST_STAGING.getName(),
                String.valueOf(Constants.TOPIC_DRIVERS_POST_STAGING.getId())));
    }
    @Test
    public void driverMessagesTestTopicTest() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE_EXTRA)
                .replace("REPLACE_DATE", yesterday())
                .replace("REPLACE_EXTRA", "Test topic")
                .replaceAll("REPLACE_FILENAME", "routed-deliveries-v200.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.raw).contains(MessageFormat.format(Main.MESSAGES_POST_TO,
                Constants.TOPIC_STONE_TEST_TOPIC.getName(),
                String.valueOf(Constants.TOPIC_STONE_TEST_TOPIC.getId())));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_DRIVER_MESSAGES.getId());
    }

    @Test
    public void driverMessagesSheetTestV300() throws IOException, CsvException {
        driverMessagesSheetTest("routed-deliveries-v300.csv", "3-0-0");
    }

    @Test
    public void driverMessagesSheetTestV301() throws IOException, CsvException {
        driverMessagesSheetTest("routed-deliveries-v301.csv", "3-0-1");
    }

    @Test
    public void driverMessagesSheetTestV302() throws IOException, CsvException {
        driverMessagesSheetTest("routed-deliveries-v302.csv", "3-0-2");
    }

    private void driverMessagesSheetTest(String filepath, String version) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", filepath);
        HttpClientSimulator.setQueryResponseData(Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Fail");
        assertThat(statusPost.raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, version,
                Main.buildTopicURL(Constants.TOPIC_REQUEST_DRIVER_MESSAGES),
                Main.buildTopicURL(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES)));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_DRIVER_MESSAGES.getId());
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
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Fail");
        assertThat(statusPost.raw).contains(MessageFormat.format(
                Main.UNSUPPORTED_CONTROL_BLOCK_VERSION, "0",
                Main.buildTopicURL(Constants.TOPIC_REQUEST_DRIVER_MESSAGES),
                Constants.CONTROL_BLOCK_VERSION_200));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_DRIVER_MESSAGES.getId());
    }

    @Test
    public void oneKitchenDriverMessagesTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.raw).contains(MessageFormat.format(Main.MESSAGES_POST_TO,
                Constants.TOPIC_DRIVERS_POST_STAGING.getName(),
                String.valueOf(Constants.TOPIC_DRIVERS_POST_STAGING.getId())));
    }

    @Test
    public void oneKitchenDriverMessagesTestTopicTest() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE_EXTRA)
                .replace("REPLACE_DATE", yesterday())
                .replace("REPLACE_EXTRA", "Test topic")
                .replaceAll("REPLACE_FILENAME", "routed-deliveries-v300.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.raw).contains(MessageFormat.format(Main.MESSAGES_POST_TO,
                Constants.TOPIC_STONE_TEST_TOPIC.getName(),
                String.valueOf(Constants.TOPIC_STONE_TEST_TOPIC.getId())));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES.getId());
    }

    @Test
    public void oneKitchenDriverMessagesMissingFormulaTest() throws IOException, CsvException {
        // FIX THIS, DS: update this test to send errors in the user request upload file
        //               see issue: Invalid formula not being detected #14
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "restaurant-template-v302-missing-formula.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_CURRENT_VALIDATED_ONE_KITCHEN_RESTAURANT_TEMPLATE, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES.getId());
    }

    @Test
    public void oneKitchenDriverMessagesMissingFormulaDirectiveTest() throws IOException, CsvException {
        // FIX THIS, DS: update this test to send errors in the user request upload file
        //               see issue: Invalid formula not being detected #14
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "restaurant-template-v302-missing-formula-directive.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_CURRENT_VALIDATED_ONE_KITCHEN_RESTAURANT_TEMPLATE, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Succeeded");
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES.getId());
    }

    @Test
    public void oneKitchenDriverMessagesV200SheetTest() throws IOException, CsvException {
        oneKitchenDriverMessagesWrongSheetTest("routed-deliveries-v200.csv", "2-0-0");
    }

    @Test
    public void oneKitchenDriverMessagesV300SheetTest() throws IOException, CsvException {
        oneKitchenDriverMessagesRightSheetTest("routed-deliveries-v300.csv");
    }

    private void oneKitchenDriverMessagesRightSheetTest(String filename) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", filename);
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES.getId());
        assertThat(statusPost.raw).contains(MessageFormat.format(Main.MESSAGES_POST_TO,
                Constants.TOPIC_DRIVERS_POST_STAGING.getName(),
                String.valueOf(Constants.TOPIC_DRIVERS_POST_STAGING.getId())));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES.getId());
    }

    private void oneKitchenDriverMessagesWrongSheetTest(String filename, String version) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", filename);
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Fail");
        assertThat(statusPost.raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, version,
                Main.buildTopicURL(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES),
                Main.buildTopicURL(Constants.TOPIC_REQUEST_DRIVER_MESSAGES)));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES.getId());
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
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Failed");
        assertThat(statusPost.raw).contains(MessageFormat.format(
                Main.UNSUPPORTED_CONTROL_BLOCK_VERSION, "0",
                Main.buildTopicURL(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES),
                Constants.CONTROL_BLOCK_VERSION_300));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES.getId());
    }

    @Test
    public void getRoutedWorkflowStatusTest() throws IOException, CsvException {
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, "last-routed-workflow-status.json");
        String[] args = { Options.COMMAND_DRIVER_MESSAGES, usersFile };
        Main.main(args);
        // There should be nothing to do. All topics have
        // status messages as their last reply.
        assertThat(WorkRequestHandler.getLastStatusPost()).isNull();
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
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS.getId());
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
    public void completedOrdersDisableDateAuditV200Test() throws IOException, CsvException {
        completedOrdersDisableDateAudit("routed-deliveries-v200.csv");
    }

    @Test
    public void completedOrdersDisableDateAuditV202Test() throws IOException, CsvException {
        completedOrdersDisableDateAudit("routed-deliveries-v202.csv");
    }

    private void completedOrdersDisableDateAudit(String filepath) throws IOException, CsvException {
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
                        + "\n[xyzzy.csv|attachment](upload://" + filepath + ") (5.8 KB)\" ] "
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
    public void completedOneKitchenOrdersV300Test() throws IOException, CsvException {
        completedOneKitchenOrders("routed-deliveries-v300.csv");
    }

    @Test
    public void completedOneKitchenOrdersV301Test() throws IOException, CsvException {
        completedOneKitchenOrders("routed-deliveries-v301.csv");
    }

    @Test
    public void completedOneKitchenOrdersV302Test() throws IOException, CsvException {
        completedOneKitchenOrders("routed-deliveries-v302.csv");
    }

    private void completedOneKitchenOrders(String filepath) throws IOException, CsvException {
        LocalDate yesterday = LocalDate.now(Constants.TIMEZONE).minusDays(1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String yesterdayStr = yesterday.format(format);

        String completedOneKitchenOrdersRequest =
                "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
                        + "\"rows\": [ "
                        + "[ 42, null, \""
                        + yesterdayStr
                        + "\n[xyzzy.csv|attachment](upload://" + filepath + ") (5.8 KB)\" ] "
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
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS.getId());
    }

    @Test
    public void completedOneKitchenOrdersInTheFutureNegativeV300Test() throws IOException, CsvException {
        completedOneKitchenOrdersInTheFutureNegative("routed-deliveries-v300.csv");
    }

    @Test
    public void completedOneKitchenOrdersInTheFutureNegativeV301Test() throws IOException, CsvException {
        completedOneKitchenOrdersInTheFutureNegative("routed-deliveries-v301.csv");
    }

    @Test
    public void completedOneKitchenOrdersInTheFutureNegativeV302Test() throws IOException, CsvException {
        completedOneKitchenOrdersInTheFutureNegative("routed-deliveries-v302.csv");
    }

    private void completedOneKitchenOrdersInTheFutureNegative(String filepath) throws IOException, CsvException {
        LocalDate tomorrow = LocalDate.now(Constants.TIMEZONE).plusDays(1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String tomorrowStr = tomorrow.format(format);

        String completedOneKitchenOrdersRequest =
                "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
                        + "\"rows\": [ "
                        + "[ 42, null, \""
                        + tomorrowStr
                        + "\n[xyzzy.csv|attachment](upload://" + filepath + ") (5.8 KB)\" ] "
                        + "] }";
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_ONEKITCHEN_ORDERS_REPLY, completedOneKitchenOrdersRequest);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_ONEKITCHEN_ORDERS, usersFile };
        WorkRequestHandler.clearLastStatusPost();
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Failed");
        assertThat(statusPost.raw).contains(MessageFormat.format(Main.DATE_IS_IN_THE_FUTURE, tomorrowStr));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS.getId());
    }

    @Test
    public void completedOneKitchenOrdersInTheFutureDisableDateAuditV300Test() throws IOException, CsvException {
        completedOneKitchenOrdersInTheFutureDisableDateAudit("routed-deliveries-v300.csv");
    }

    @Test
    public void completedOneKitchenOrdersInTheFutureDisableDateAuditV301Test() throws IOException, CsvException {
        completedOneKitchenOrdersInTheFutureDisableDateAudit("routed-deliveries-v301.csv");
    }

    @Test
    public void completedOneKitchenOrdersInTheFutureDisableDateAuditV302Test() throws IOException, CsvException {
        completedOneKitchenOrdersInTheFutureDisableDateAudit("routed-deliveries-v302.csv");
    }

    private void completedOneKitchenOrdersInTheFutureDisableDateAudit(String filepath) throws IOException, CsvException {
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
                        + "\n[xyzzy.csv|attachment](upload://" + filepath + ") (5.8 KB)\" ] "
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
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS.getId());
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
    public void workRequestsBadPreviousRequestTest() throws IOException, CsvException {
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_REQUESTS_LAST_REPLIES, "last-replies-bad-request.json");
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_WORK_REQUESTS, usersFile };
        Main.main(args);
        // There should be nothing to do. All topics have
        // status messages as their last reply.
        assertThat(WorkRequestHandler.getLastStatusPost()).isNull();
    }

    @Test
    public void workflowParserUpdateMemberDataTest() {
        String deliveries = readResourceFile("update-member-data-multiple-updates.csv");
        WorkflowParser parser = WorkflowParser.create(Collections.emptyMap(), deliveries);
        ApiClient apiSim = createApiSimulator();
        List<User> userList = new Loader(apiSim).load();
        Map<String, User> users = new Tables(userList).mapByUserName();
        String json = apiSim.runQuery(Constants.QUERY_GET_DELIVERY_DETAILS);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        Map<String, DetailsPost> deliveryDetails = HBParser.deliveryDetails(apiQueryResult);
        WorkflowExporter exporter = new WorkflowExporter(parser);
        String updatedCSVData = exporter.updateMemberData(users, deliveryDetails);
        assertThat(updatedCSVData).doesNotContain("Cust Name");
        assertThat(updatedCSVData).contains(
                "Ms. Somebody,Somebody,123-456-7890,510-015-5151,Unknown,Berkeley,\"542 11dy 7th Street, Berkeley, CA\",FALSE");
        assertThat(updatedCSVData).contains(
                "\"Mr. Somebody, Esq.\",SomebodyElse,123-456-7890,510-015-5151,Unknown,Berkeley,\"542 11dy 7th Street, Apt 3g, Berkeley, CA\",FALSE");
        assertThat(updatedCSVData).contains(
                "THE THIRD PERSON,ThirdPerson,123-456-7890,510-222-7777,Unknown,Berkeley,\"4 Fortieth Blvd, Berkeley, CA\",FALSE,\"something, with, a, lot, of commas.\"");
        assertThat(updatedCSVData).contains(
                "X Y ZZY,Xyzzy,555-555-5555,123-456-0000,N.BerkHills/Tilden,Berkeley,\"1223 Main St., Berkeley, CA\",FALSE");
        assertThat(updatedCSVData).contains(
                "Zees McZeesy,ZZZ,123-456-7890,none,unknown,Berkeley,\"3 Place Place Square, Berkeley, CA\",TRUE");
        assertThat(updatedCSVData).contains(
                "Joseph R. Volunteer,JVol,123-456-7890,none,unknown,Berkeley,\"47 74th Ave, Berkeley, CA\",TRUE");
        assertThat(updatedCSVData).contains(
                "Scotty J Backup 772th,MrBackup772,123-456-7890,none,unknown,Berkeley,\"38 38th Ave, Berkeley, CA\",TRUE");
    }

    @Test
    public void updateMemberDataRequestMultipleUpdatesTest() throws IOException, CsvException {
        String request = readResourceFile(DATA_REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "update-member-data-multiple-updates.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_REQUESTS_LAST_REPLIES, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = {Options.COMMAND_WORK_REQUESTS, usersFile};
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Succeeded");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("| jbDriver | | | | Updated | | Updated | Updated | |");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("| Somebody | | Updated | Updated | Updated | | Updated | | Updated |");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("| SomebodyElse | Updated | Updated | Updated | Updated | | Updated | | Updated |");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("| ThirdPerson | Updated | Updated | Updated | Updated | | Updated | | Updated |");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("| Xyzzy | Updated | Updated | Updated | Updated | | Updated | | |");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("| ZZZ | Updated | | | Updated | | Updated | | Updated |");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("| JVol | Updated | Updated | | Updated | | Updated | Updated | |");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("| MrBackup772 | Updated | Updated | | Updated | | Updated | Updated | |");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("| jbDriver | | | | Updated | | Updated | Updated | |");
    }

    @Test
    public void updateMemberDataRequestMissingUsersTest() throws IOException, CsvException {
        String request = readResourceFile(DATA_REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "update-member-data-no-matching-users.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_REQUESTS_LAST_REPLIES, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_WORK_REQUESTS, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(
                MessageFormat.format(WorkflowExporter.NO_MATCHING_MEMBER_ERROR, "Cust1", "19"));
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(
                MessageFormat.format(WorkflowExporter.NO_MATCHING_MEMBER_ERROR, "Cust2", "20"));
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(
                MessageFormat.format(WorkflowExporter.NO_MATCHING_MEMBER_ERROR, "Cust3", "21"));
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(
                MessageFormat.format(WorkflowExporter.NO_MATCHING_MEMBER_ERROR, "Cust4", "22"));
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(
                MessageFormat.format(WorkflowExporter.NO_MATCHING_MEMBER_ERROR, "Cust5", "23"));
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(
                MessageFormat.format(WorkflowExporter.NO_MATCHING_MEMBER_ERROR, "Cust6", "24"));
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(
                MessageFormat.format(WorkflowExporter.NO_MATCHING_MEMBER_ERROR, "Cust7", "25"));
    }

    @Test
    public void updateMemberDataDriverIsConsumer() throws IOException, CsvException {
        String request = readResourceFile(DATA_REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "update-member-data-driver-is-consumer.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_REQUESTS_LAST_REPLIES, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_WORK_REQUESTS, usersFile };
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Fail");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(
                MessageFormat.format(WorkflowExporter.DRIVER_IS_CONSUMER_ERROR, "17"));
    }

    @Test
    public void updateMemberDataTooManyMembers() {
        String deliveries = readResourceFile("update-member-data-multiple-updates.csv");
        WorkflowParser parser = WorkflowParser.create(Collections.emptyMap(), deliveries);
        ApiClient apiSim = createApiSimulator();
        List<User> userList = new Loader(apiSim).load();
        Map<String, User> users = new Tables(userList).mapByUserName();
        String json = apiSim.runQuery(Constants.QUERY_GET_DELIVERY_DETAILS);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        Map<String, DetailsPost> deliveryDetails = HBParser.deliveryDetails(apiQueryResult);
        WorkflowExporter exporter = new WorkflowExporter(parser);
        exporter.changeMemberLimit(2);
        Throwable thrown = catchThrowable(() -> exporter.updateMemberData(users, deliveryDetails));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(WorkflowExporter.TOO_MANY_MEMBERS_ERROR, 3));
    }

    @Test
    public void updateMemberDataNoUpdatesTest() throws IOException, CsvException {
        String request = readResourceFile(DATA_REQUEST_TEMPLATE)
                .replace("REPLACE_DATE", yesterday())
                .replaceAll("REPLACE_FILENAME", "update-member-data-no-updates.csv");
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_REQUESTS_LAST_REPLIES, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = {Options.COMMAND_WORK_REQUESTS, usersFile};
        Main.main(args);
        assertThat(WorkRequestHandler.getLastStatusPost()).isNotNull();
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains("Status: Succeeded");
        assertThat(WorkRequestHandler.getLastStatusPost().raw).contains(MessageFormat.format(Main.UPDATE_USERS_NO_UPDATES, "update-member-data-no-updates.csv"));
    }

    @Test
    public void completedOneKitchenV200Test() throws IOException, CsvException {
        completedOneKitchenWrongTopic("routed-deliveries-v200.csv", "2-0-0");
    }

    @Test
    public void completedOneKitchenV202Test() throws IOException, CsvException {
        completedOneKitchenWrongTopic("routed-deliveries-v202.csv", "2-0-2");
    }

    private void completedOneKitchenWrongTopic(String filepath, String version) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", filepath)
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_ONEKITCHEN_ORDERS_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_ONEKITCHEN_ORDERS, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Fail");
        assertThat(statusPost.raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, version,
                Main.buildTopicURL(Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS),
                Main.buildTopicURL(Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS)));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS.getId());
    }

    @Test
    public void completedOneKitchenV300Test() throws IOException, CsvException {
        completedOneKitchenRightTopic("routed-deliveries-v300.csv");
    }

    @Test
    public void completedOneKitchenV301Test() throws IOException, CsvException {
        completedOneKitchenRightTopic("routed-deliveries-v301.csv");
    }

    @Test
    public void completedOneKitchenV302Test() throws IOException, CsvException {
        completedOneKitchenRightTopic("routed-deliveries-v302.csv");
    }

    private void completedOneKitchenRightTopic(String filepath) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", filepath)
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_ONEKITCHEN_ORDERS_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_ONEKITCHEN_ORDERS, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS.getId());
    }

    @Test
    public void completedDailyOrdersV300Test() throws IOException, CsvException {
        completedDailyOrdersWrongTopic("routed-deliveries-v300.csv", "3-0-0");
    }

    @Test
    public void completedDailyOrdersV301Test() throws IOException, CsvException {
        completedDailyOrdersWrongTopic("routed-deliveries-v301.csv", "3-0-1");
    }

    @Test
    public void completedDailyOrdersV302Test() throws IOException, CsvException {
        completedDailyOrdersWrongTopic("routed-deliveries-v302.csv", "3-0-2");
    }

    private void completedDailyOrdersWrongTopic(String filepath, String version) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", filepath)
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_DAILY_ORDERS, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Fail");
        assertThat(statusPost.raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, version,
                Main.buildTopicURL(Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS),
                Main.buildTopicURL(Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS)));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS.getId());
    }

    @Test
    public void completedDailyOrdersV200Test() throws IOException, CsvException {
        completedDailyOrdersRightTopic("routed-deliveries-v200.csv");
    }

    @Test
    public void completedDailyOrdersV202Test() throws IOException, CsvException {
        completedDailyOrdersRightTopic("routed-deliveries-v202.csv");
    }

    private void completedDailyOrdersRightTopic(String filepath) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", filepath)
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY, request);
        String usersFile = findFile(Constants.MEMBERDATA_RAW_FILE, "csv");
        String[] args = { Options.COMMAND_COMPLETED_DAILY_ORDERS, usersFile };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS.getId());
    }

    @Test
    public void postOneKitchenRestaurantTemplateV200Test() throws IOException, CsvException {
        postOneKitchenRestaurantTemplateWrongTopic("restaurant-template-v200.csv", "2-0-0");
    }

    @Test
    public void postOneKitchenRestaurantTemplateV202Test() throws IOException, CsvException {
        postOneKitchenRestaurantTemplateWrongTopic("restaurant-template-v202.csv", "2-0-2");
    }

    private void postOneKitchenRestaurantTemplateWrongTopic(String filepath, String version) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", filepath)
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_ONE_KITCHEN_RESTAURANT_TEMPLATE_REPLY, request);
        String[] args = { Options.COMMAND_ONE_KITCHEN_RESTAURANT_TEMPLATE };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Fail");
        assertThat(statusPost.raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, version,
                Main.buildTopicURL(Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE),
                Main.buildTopicURL(Constants.TOPIC_POST_RESTAURANT_TEMPLATE)));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE.getId());
    }

    @Test
    public void postOneKitchenRestaurantTemplateMissingFormula() throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", "restaurant-template-v302-missing-formula.csv")
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_ONE_KITCHEN_RESTAURANT_TEMPLATE_REPLY, request);
        String[] args = { Options.COMMAND_ONE_KITCHEN_RESTAURANT_TEMPLATE };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Fail");
        assertThat(statusPost.raw).contains(
                MessageFormat.format(RestaurantTemplateParser.MISSING_FORMULA_VALUE, "52"));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE.getId());
    }

    @Test
    public void postOneKitchenRestaurantTemplateWithFormulas() throws IOException, CsvException {
        postOneKitchenRestaurantTemplateRightTopic("restaurant-template-v302-with-formulas.csv");
    }

    @Test
    public void postOneKitchenRestaurantTemplateV300Test() throws IOException, CsvException {
        postOneKitchenRestaurantTemplateRightTopic("restaurant-template-v300.csv");
    }

    @Test
    public void postOneKitchenRestaurantTemplateV301Test() throws IOException, CsvException {
        postOneKitchenRestaurantTemplateRightTopic("restaurant-template-v301.csv");
    }

    @Test
    public void postOneKitchenRestaurantTemplateV302Test() throws IOException, CsvException {
        postOneKitchenRestaurantTemplateRightTopic("restaurant-template-v302.csv");
    }

    private void postOneKitchenRestaurantTemplateRightTopic(String filepath) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", filepath)
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_ONE_KITCHEN_RESTAURANT_TEMPLATE_REPLY, request);
        String[] args = { Options.COMMAND_ONE_KITCHEN_RESTAURANT_TEMPLATE };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE.getId());
    }

    @Test
    public void postRestaurantTemplateV300Test() throws IOException, CsvException {
        postRestaurantTemplateWrongTopic("restaurant-template-v300.csv", "3-0-0");
    }

    @Test
    public void postRestaurantTemplateV301Test() throws IOException, CsvException {
        postRestaurantTemplateWrongTopic("restaurant-template-v301.csv", "3-0-1");
    }

    @Test
    public void postRestaurantTemplateV302Test() throws IOException, CsvException {
        postRestaurantTemplateWrongTopic("restaurant-template-v302.csv", "3-0-2");
    }

    private void postRestaurantTemplateWrongTopic(String filepath, String version) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", filepath)
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_RESTAURANT_TEMPLATE_REPLY, request);
        String[] args = { Options.COMMAND_RESTAURANT_TEMPLATE };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Fail");
        assertThat(statusPost.raw).contains(MessageFormat.format(
                Main.WRONG_REQUEST_TOPIC, version,
                Main.buildTopicURL(Constants.TOPIC_POST_RESTAURANT_TEMPLATE),
                Main.buildTopicURL(Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE)));
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_RESTAURANT_TEMPLATE.getId());
    }

    @Test
    public void postRestaurantTemplateV200Test() throws IOException, CsvException {
        postRestaurantTemplateRightTopic("restaurant-template-v200.csv");
    }

    @Test
    public void postRestaurantTemplateV202Test() throws IOException, CsvException {
        postRestaurantTemplateRightTopic("restaurant-template-v202.csv");
    }

    private void postRestaurantTemplateRightTopic(String filepath) throws IOException, CsvException {
        String request = readResourceFile(REQUEST_TEMPLATE)
                .replaceAll("REPLACE_FILENAME", filepath)
                .replace("REPLACE_DATE", yesterday());
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_RESTAURANT_TEMPLATE_REPLY, request);
        String[] args = { Options.COMMAND_RESTAURANT_TEMPLATE };
        Main.main(args);
        Post statusPost = WorkRequestHandler.getLastStatusPost();
        assertThat(statusPost).isNotNull();
        assertThat(statusPost.raw).contains("Status: Succeeded");
        assertThat(statusPost.topic_id).isEqualTo(Constants.TOPIC_POST_RESTAURANT_TEMPLATE.getId());
    }

    private String findFile(final String prefix, final String suffix) {

        File dir = new File(".");
        File[] files = dir.listFiles((dir1, name) -> name.startsWith(prefix) && name.endsWith(suffix));

        assertThat(files).isNotNull();
        assertThat(files).hasSize(1);
        return files[0].getName();
    }
}