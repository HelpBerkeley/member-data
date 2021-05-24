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
import org.helpberkeley.memberdata.route.Route;
import org.helpberkeley.memberdata.v200.DriverPostFormatV200;
import org.helpberkeley.memberdata.v300.DriverPostFormatV300;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.net.HttpURLConnection.HTTP_OK;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

    static final String MEMBERDATA_ERRORS_FILE = "memberdata-errors";
    static final String MEMBERDATA_REPORT_FILE = "member-data-report";

    static final String WORKFLOW_TITLE = "Workflow Data";
    static final String ONE_KITCHEN_WORKFLOW_TITLE = "Free Meals Workflow Data";
    static final String DISPATCHERS_TITLE = "Dispatchers Info";
    static final String INREACH_TITLE = "Customer Info";
    static final String DRIVERS_TITLE = "Volunteer Drivers";
    static final String ORDER_HISTORY_TITLE = "Order History";
    static final String DRIVER_HISTORY_TITLE = "Driver History";

    // FIX THIS, DS: make this less fragile
    static final long MEMBER_DATA_REQUIRING_ATTENTION_TOPIC_ID = 129;
    static final long MEMBER_DATA_REQUIRING_ATTENTION_POST_ID = 1706;
    static final long CONSUMER_REQUESTS_TOPIC_ID = 444;
    static final long CONSUMER_REQUESTS_POST_ID = 1776;
    static final long VOLUNTEER_REQUESTS_TOPIC_ID = 445;
    static final long DRIVERS_POST_TOPIC = 638;
    static final long ALL_MEMBERS_POST_TOPIC = 837;
    static final long WORKFLOW_DATA_TOPIC = 824;
    static final long ONE_KITCHEN_WORKFLOW_DATA_TOPIC = 6658;
    static final long INREACH_POST_TOPIC = 820;
    static final long COMPLETED_DAILY_DELIVERIES_TOPIC = 859;
    static final long DISPATCHERS_POST_TOPIC = 938;
    static final long STONE_TEST_TOPIC = 422;
    static final long DISPATCHERS_POST_ID = 5324;
    static final long ORDER_HISTORY_POST_ID = 6433;
    static final long RESTAURANT_TEMPLATE_POST_ID = 8664;
    static final long DRIVERS_POST_STAGING_TOPIC_ID = 2123;
    static final long DRIVERS_TABLE_SHORT_POST_ID = 44847;
    static final long DRIVERS_TABLE_LONG_POST_ID = 44959;
    static final long DRIVER_HISTORY_POST_ID = 48019;
    static final long CUSTOMER_CARE_MEMBER_DATA_POST_ID = 52234;
    static final long FRREG_POST_ID = 52427;

    public static void main(String[] args) throws IOException, CsvException {

        Options options = new Options(args);
        try {
            options.parse();
        } catch (Options.OptionsException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }

        // Load member data properties
        Properties memberDataProperties = loadProperties();

        // Set up an HTTP client
        ApiClient apiClient = new ApiClient(memberDataProperties);

        // testQuery(apiClient);

        switch (options.getCommand()) {
            case Options.COMMAND_WORK_REQUESTS:
                workRequests(apiClient, options.getFileName());
                break;
            case Options.COMMAND_FETCH:
                fetch(apiClient);
                break;
            case Options.COMMAND_DRIVER_MESSAGES:
                driverMessages(apiClient, options.getFileName());
                break;
            case Options.COMMAND_ONE_KITCHEN_DRIVER_MESSAGES:
                oneKitchenDriverMessages(apiClient, options.getFileName());
                break;
            case Options.COMMAND_DRIVER_ROUTES:
                driverRoutes(apiClient);
                break;
            case Options.COMMAND_ORDER_HISTORY:
                orderHistory(apiClient, options.getFileName());
                break;
            case Options.COMMAND_DRIVERS:
                drivers(apiClient, options.getFileName());
                break;
            case Options.COMMAND_DRIVER_HISTORY:
                driverHistory(apiClient);
                break;
            case Options.COMMAND_RESTAURANT_TEMPLATE:
                restaurantTemplate(apiClient);
                break;
            case Options.COMMAND_ONE_KITCHEN_RESTAURANT_TEMPLATE:
                oneKitchenRestaurantTemplate(apiClient);
                break;
            case Options.COMMAND_POST_ERRORS:
                postUserErrors(apiClient, options.getFileName());
                break;
            case Options.COMMAND_UPDATE_ERRORS:
                updateUserErrors(apiClient, options.getFileName());
                break;
            case Options.COMMAND_POST_VOLUNTEER_REQUESTS:
                postVolunteerRequests(apiClient, options.getFileName());
                break;
            case Options.COMMAND_POST_CONSUMER_REQUESTS:
                postConsumerRequests(apiClient, options.getFileName());
                break;
            case Options.COMMAND_POST_ALL_MEMBERS:
                postAllMembers(apiClient, options.getFileName());
                break;
            case Options.COMMAND_POST_DISPATCHERS:
                postFile(apiClient, options.getFileName(),
                        options.getShortURL(), DISPATCHERS_TITLE, DISPATCHERS_POST_TOPIC);
                break;
            case Options.COMMAND_UPDATE_DISPATCHERS:
                updateDispatchers(apiClient, options.getFileName());
                break;
            case Options.COMMAND_INREACH:
                generateInreach(apiClient, options.getFileName());
                break;
            case Options.COMMAND_CUSTOMER_CARE_POST:
                customerCarePost(apiClient, options.getFileName());
                break;
            case Options.COMMAND_FRREG:
                frreg(apiClient, options.getFileName());
                break;
            case Options.COMMAND_EMAIL:
                generateEmail(apiClient, options.getFileName());
                break;
            case Options.COMMAND_WORKFLOW:
                generateWorkflow(apiClient, options.getFileName());
                break;
            case Options.COMMAND_ONE_KITCHEN_WORKFLOW:
                generateOneKitchenWorkflow(apiClient, options.getFileName());
                break;
            case Options.COMMAND_COMPLETED_DAILY_ORDERS:
                completedDailyOrders(apiClient, options.getFileName());
                break;
            case Options.COMMAND_TEST_REQUEST:
                testRequest(apiClient, options.getFileName());
                break;
            default:
                assert options.getCommand().equals(Options.COMMAND_POST_DRIVERS) : options.getCommand();
                postDrivers(apiClient, options.getFileName());
                break;
        }
    }

    static Properties loadProperties() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL propertiesFile = classLoader.getResource(Constants.MEMBERDATA_PROPERTIES);

        if (propertiesFile == null) {
            LOGGER.error("Required properties file {} cannot be found", Constants.MEMBERDATA_PROPERTIES);
            System.exit(1);
        }

        Properties properties = new Properties();

        //noinspection EmptyFinallyBlock
        try (InputStream is = propertiesFile.openStream())
        {
            properties.load(is);
        } catch (IOException e) {
            throw new MemberDataException(e.getMessage());
        } finally {}

        return properties;
    }

    private static void fetch(ApiClient apiClient) throws IOException, CsvException {
        // Create a User loader
        Loader loader = new Loader(apiClient);

        // Load the member data from the website
        List<User> users = loader.load();

        // Create an exporter
        UserExporter exporter = new UserExporter(users);

        // Export all users
        exporter.allMembersRawToFile(Constants.MEMBERDATA_RAW_FILE);

        // Export all users report
        exporter.allMembersReportToFile(MEMBERDATA_REPORT_FILE);

        // Export any user errors
        exporter.errorsToFile(MEMBERDATA_ERRORS_FILE);

        // Export non-consumer group members, with a consumer request
        exporter.consumerRequestsToFile(Constants.CONSUMER_REQUESTS_FILE);

        // Export new volunteers-consumer group members, with a volunteer request
        exporter.volunteerRequestsToFile(Constants.VOLUNTEER_REQUESTS_FILE);

        // Export dispatchers
        exporter.dispatchersToFile(Constants.DISPATCHERS_FILE);

        // Fetch driver details
        String json = apiClient.runQuery(Constants.QUERY_GET_DRIVER_DETAILS);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        Map<String, DetailsPost> driverDetails = HBParser.driverDetails(apiQueryResult);

        // Fetch driver history
        Map<String, DriverHistory> driverHistory = DriverHistory.getDriverHistory(apiClient);

        // Export drivers
        new DriverExporter(users, driverHistory, driverDetails).driversToFile();
    }

    private static void postConsumerRequests(ApiClient apiClient, final String fileName)
            throws IOException, CsvException {

        String csvData = Files.readString(Paths.get(fileName));
        List<User> users = HBParser.users(csvData);

        StringBuilder postRaw = new StringBuilder();
        String label =  "Newly created members requesting meals -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | Email Verified | Pre-reg | City | Address | Condo | Phone |\n");
        postRaw.append("|---|---|---|---|---|---|---|\n");

        Tables tables = new Tables(users);
        for (User user : tables.sortByCreateTime()) {
            postRaw.append('|');
            postRaw.append(user.getUserName());
            postRaw.append('|');
            postRaw.append(user.getEmailVerified());
            postRaw.append('|');
            postRaw.append((user.isMondayFrreg() || user.isThursdayFrreg()) ? ":fire:" : "");
            postRaw.append('|');
            postRaw.append(user.getCity());
            postRaw.append('|');
            postRaw.append(user.getAddress());
            postRaw.append('|');
            postRaw.append(user.isCondo());
            postRaw.append('|');
            postRaw.append(user.getPhoneNumber());
            postRaw.append("|\n");
        }

        Post post = new Post();
        post.title = label;
        post.topic_id = CONSUMER_REQUESTS_TOPIC_ID;
        post.raw = postRaw.toString();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        LOGGER.info("postConsumerRequests {}", response.statusCode() == HTTP_OK ?
            "" : "failed " + response.statusCode() + ": " + response.body());
    }

    private static void postVolunteerRequests(ApiClient apiClient, final String fileName)
            throws IOException, CsvException {

        String csvData = Files.readString(Paths.get(fileName));
        List<User> users = HBParser.users(csvData);

        StringBuilder postRaw = new StringBuilder();
        String label =  "New members requesting to volunteer -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | Email Verified | Full Name | Phone | City | Volunteer Request |\n");
        postRaw.append("|---|---|---|---|---|---|\n");

        Tables tables = new Tables(users);
        for (User user : new Tables(tables.volunteerRequests()).sortByCreateTime()) {
            postRaw.append('|');
            postRaw.append(user.getUserName());
            postRaw.append('|');
            postRaw.append(user.getEmailVerified());
            postRaw.append('|');
            postRaw.append(user.getName());
            postRaw.append('|');
            postRaw.append(user.getPhoneNumber());
            postRaw.append('|');
            postRaw.append(user.getCity());
            postRaw.append('|');
            postRaw.append(user.getVolunteerRequest());
            postRaw.append("|\n");
        }

        Post post = new Post();
        post.title = label;
        post.topic_id = VOLUNTEER_REQUESTS_TOPIC_ID;
        post.raw = postRaw.toString();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        LOGGER.info("postVolunteerRequests {}", response.statusCode() == HTTP_OK ?
                "" : "failed " + response.statusCode() + ": " + response.body());
    }

    private static void postUserErrors(ApiClient apiClient, final String fileName) throws IOException {

        StringBuilder postRaw = new StringBuilder();

        postRaw.append("** ");
        postRaw.append(ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss")));
        postRaw.append(" **\n\n");

        Post post = new Post();
        post.title = "Member data requiring attention";
        post.topic_id = MEMBER_DATA_REQUIRING_ATTENTION_TOPIC_ID;
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));
        postRaw.append(Files.readString(Paths.get(fileName)));

        post.raw = postRaw.toString();
        HttpResponse<?> response = apiClient.post(post.toJson());
        LOGGER.info("postUserErrors {}", response.statusCode() == HTTP_OK ?
                "" : "failed " + response.statusCode() + ": " + response.body());
    }

    private static void updateUserErrors(ApiClient apiClient, final String fileName) throws IOException {

        String postRaw = "**" +
                "Member data requiring attention -- " +
                ZonedDateTime.now(
                        ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss")) +
                "**\n\n" +
                Files.readString(Paths.get(fileName));
        HttpResponse<?> response = apiClient.updatePost(MEMBER_DATA_REQUIRING_ATTENTION_POST_ID, postRaw);
        LOGGER.info("updateUserErrors {}", response.statusCode() == HTTP_OK ?
                "" : "failed " + response.statusCode() + ": " + response.body());
    }

    private static void postAllMembers(ApiClient apiClient, final String fileName) {
        // Upload it to Discourse
        Upload upload = new Upload(apiClient, fileName);
        // Post
        postFile(apiClient, fileName, upload.getShortURL(), Constants.ALL_MEMBERS_TITLE, ALL_MEMBERS_POST_TOPIC);
    }

    private static void postDrivers(ApiClient apiClient, final String fileName) {
       // Upload it to Discourse
        Upload upload = new Upload(apiClient, fileName);
        // Post
        postFile(apiClient, fileName, upload.getShortURL(), DRIVERS_TITLE, DRIVERS_POST_TOPIC);
    }

    private static void updateDispatchers(ApiClient apiClient, final String fileName) {
        // Upload it to Discourse
        Upload upload = new Upload(apiClient, fileName);
        // Post
        updateFile(apiClient, fileName, upload.getShortURL(), DISPATCHERS_TITLE, DISPATCHERS_POST_ID);
    }

    private static void postFile(ApiClient apiClient, final String fileName, final String shortUrl,
                 final String title, long topicId) {

        String now = ZonedDateTime.now(ZoneId.systemDefault()).format(
                DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        Post post = new Post();
        post.title = title;
        post.topic_id = topicId;
        post.createdAt = now;
        post.raw = "**" +
                title +
                " -- " +
                now +
                "**\n\n" +
                // postRaw.append("[" + fileName + "|attachment](upload://" + fileName + ") (5.49 KB)");
                "[" + fileName + "|attachment](" + shortUrl + ")";

        HttpResponse<?> response = apiClient.post(post.toJson());
        LOGGER.info("postFile {} {}", fileName, response.statusCode() == HTTP_OK ?
                "" : "failed " + response.statusCode() + ": " + response.body());
    }

    private static void updateFile(ApiClient apiClient, final String fileName,
                final String shortUrl, String title, long postId) {

        String now = ZonedDateTime.now(ZoneId.systemDefault()).format(
                DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        String postRaw = "**" +
                title +
                " -- " +
                now +
                "**\n\n" +
                // postRaw.append("[" + fileName + "|attachment](upload://" + fileName + ") (5.49 KB)");
                "[" + fileName + "|attachment](" + shortUrl + ")";
        HttpResponse<?> response = apiClient.updatePost(postId, postRaw);
        LOGGER.info("updateFile {} {}", fileName, response.statusCode() == HTTP_OK ?
                "" : "failed " + response.statusCode() + ": " + response.body());
    }

    private static void generateInreach(ApiClient apiClient, String usersFile) throws IOException, CsvException {
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = HBParser.users(csvData);

        // Download order history file
        OrderHistory orderHistory = OrderHistory.getOrderHistory(apiClient);

        // Generate the InReach file
        String inreachFileName = new UserExporter(users).inreachToFile(orderHistory);

        // Upload the InReach file
        Upload upload = new Upload(apiClient, inreachFileName);

        // Post the file
        postFile(apiClient, upload.getFileName(), upload.getShortURL(), INREACH_TITLE, INREACH_POST_TOPIC);
    }

    private static void generateEmail(ApiClient apiClient, final String usersFile)
            throws IOException, CsvException {
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = HBParser.users(csvData);

        Map<Long, String> emails = new Loader(apiClient).loadEmailAddresses();

        new UserExporter(users).allMembersWithEmailReportToFile(emails);
    }

    private static void generateWorkflow(ApiClient apiClient, final String usersFile)
            throws IOException, CsvException {

        // Read/parse the members data
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = HBParser.users(csvData);

        // Fetch/parse the last restaurant template reply
        String  json = apiClient.runQuery(
                Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        assert apiQueryResult.rows.length == 1;

        Object[] columns = (Object[])apiQueryResult.rows[0];
        assert columns.length == 3 : columns.length;
        String rawPost = (String)columns[2];
        RestaurantTemplatePost restaurantTemplatePost = HBParser.restaurantTemplatePost(rawPost);

        // Fetch/parse the delivery details
        json = apiClient.runQuery(Constants.QUERY_GET_DELIVERY_DETAILS);
        apiQueryResult = HBParser.parseQueryResult(json);
        Map<String, DetailsPost> deliveryDetails = HBParser.deliveryDetails(apiQueryResult);

        // Download the restaurant template file
        String restaurantTemplate = apiClient.downloadFile(restaurantTemplatePost.uploadFile.getFileName());

        // Generate the workflow file
        String workflowFileName =
                new UserExporter(users).workflowToFile(restaurantTemplate, deliveryDetails, Constants.WORKFLOW_FILE);

        // Upload it to Discourse
        Upload upload = new Upload(apiClient, workflowFileName);

        // Create a post in with a link to the uploaded file.
        postFile(apiClient, workflowFileName, upload.getShortURL(), WORKFLOW_TITLE, WORKFLOW_DATA_TOPIC);
    }

    private static void generateOneKitchenWorkflow(ApiClient apiClient, final String usersFile)
            throws IOException, CsvException {

        // Read/parse the members data
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = HBParser.users(csvData);

        // Fetch/parse the last restaurant template reply
        String  json = apiClient.runQuery(
                Constants.QUERY_GET_CURRENT_VALIDATED_ONE_KITCHEN_RESTAURANT_TEMPLATE);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        assert apiQueryResult.rows.length == 1;

        Object[] columns = (Object[])apiQueryResult.rows[0];
        assert columns.length == 3 : columns.length;
        String rawPost = (String)columns[2];
        RestaurantTemplatePost restaurantTemplatePost = HBParser.restaurantTemplatePost(rawPost);

        // Fetch/parse the delivery details
        json = apiClient.runQuery(Constants.QUERY_GET_DELIVERY_DETAILS);
        apiQueryResult = HBParser.parseQueryResult(json);
        Map<String, DetailsPost> deliveryDetails = HBParser.deliveryDetails(apiQueryResult);

        // Download the restaurant template file
        String restaurantTemplate = apiClient.downloadFile(restaurantTemplatePost.uploadFile.getFileName());

        // Generate the workflow file
        String workflowFileName = new UserExporter(users).oneKitchenWorkflowToFile(
                restaurantTemplate, deliveryDetails, Constants.ONE_KITCHEN_WORKFLOW_FILE);

        // Upload it to Discourse
        Upload upload = new Upload(apiClient, workflowFileName);

        // Create a post in with a link to the uploaded file.
        postFile(apiClient, workflowFileName, upload.getShortURL(),
                ONE_KITCHEN_WORKFLOW_TITLE, ONE_KITCHEN_WORKFLOW_DATA_TOPIC);
    }

    private static void driverRoutes(ApiClient apiClient) {
        Query query = new Query(
                Constants.QUERY_GET_LAST_ROUTE_REQUEST_REPLY, Constants.TOPIC_REQUEST_DRIVER_ROUTES);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);

        WorkRequestHandler.Reply reply;

        try {
            reply = requestHandler.getLastReply();
        } catch (MemberDataException ex) {
            LOGGER.warn("getLastReply failed: " + ex + "\n" + ex.getMessage());
            requestHandler.postStatus(WorkRequestHandler.RequestStatus.Failed, ex.getMessage());
            return;
        }

        if (reply instanceof WorkRequestHandler.Status) {
            return;
        }

        WorkRequestHandler.WorkRequest request = (WorkRequestHandler.WorkRequest) reply;
        doDriverRoutes(apiClient, request);
    }

    private static void doDriverRoutes(ApiClient apiClient, WorkRequestHandler.WorkRequest request) {

        // Download file
        String unroutedDeliveries = apiClient.downloadFile(request.uploadFile.getFileName());
        request.postStatus(WorkRequestHandler.RequestStatus.Processing, "");

        Route route = new Route();

        try {
            WorkflowParser workflowParser = WorkflowParser.create(
                    WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, Collections.emptyMap(), unroutedDeliveries);
            List<Driver> drivers = workflowParser.drivers();

            for (Driver driver : drivers) {
                route.route(driver);
            }

            StringBuilder statusMessage = new StringBuilder();
            long locationCalls = route.getLocationCalls();
            long directionsCalls = route.getDirectionsCalls();

            statusMessage.append("|Maps API|Number of Calls|Cost|\n");
            statusMessage.append("|---|---|---|\n");
            statusMessage.append("|Location|")
                    .append(locationCalls)
                    .append('|')
                    .append(String.format("$%.2g", locationCalls * .005))
                    .append("|\n");
            statusMessage.append("|Directions|")
                    .append(directionsCalls)
                    .append('|')
                    .append(String.format("$%.2g", directionsCalls * .005))
                    .append("|\n");
            statusMessage.append("|Total||")
                    .append(String.format("$%.2g", (locationCalls + directionsCalls) * .005))
                    .append("|\n");

            // Create new workflow file
            //
            StringBuilder routedWorkflow = new StringBuilder();

            // Original control block
            routedWorkflow.append(workflowParser.rawControlBlock());

            // Add a separator row for ease of viewing in the Google sheets
            routedWorkflow.append(workflowParser.emptyRow());

            for (Driver driver : drivers) {
                routedWorkflow.append(driver.driverBlock());
                routedWorkflow.append(workflowParser.emptyRow());
            }

            Exporter exporter = new Exporter();
            String fileName = "routed-" + request.uploadFile.getOriginalFileName();
            exporter.writeFile(fileName, routedWorkflow.toString());
            Upload upload = new Upload(apiClient, fileName);
            postRequestDriverRoutesSucceeded(apiClient, fileName, upload.getShortURL(), statusMessage.toString());
            Files.delete(Path.of(fileName));
        } catch (MemberDataException | IOException ex) {
            String reason = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            request.postStatus(WorkRequestHandler.RequestStatus.Failed, reason);
        } finally {
            route.shutdown();
        }
    }

    /**
     * Check the Request Driver Messages topic for a request.
     * Generate driver messages if a request is present.
     *
     * @param apiClient Discourse API handling
     * @param allMembersFile CSV file of all current members
     */
    private static void driverMessages(ApiClient apiClient, String allMembersFile)
            throws IOException, CsvException {
        Query query = new Query(
                Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, Constants.TOPIC_REQUEST_DRIVER_MESSAGES);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);

        WorkRequestHandler.Reply reply;

        try {
            reply = requestHandler.getLastReply();
        } catch (MemberDataException ex) {
            LOGGER.warn("getLastReply failed: " + ex + "\n" + ex.getMessage());
            requestHandler.postStatus(WorkRequestHandler.RequestStatus.Failed, ex.getMessage());
            return;
        }

        // Nothing to do.
        if (reply instanceof WorkRequestHandler.Status) {
            return;
        }

        // Parse users files
        String csvData = Files.readString(Paths.get(allMembersFile));
        Map<String, User> users = new Tables(HBParser.users(csvData)).mapByUserName();

        WorkRequestHandler.WorkRequest request = (WorkRequestHandler.WorkRequest) reply;
        doDriverMessages(apiClient, request, users);
    }

    private static void doDriverMessages(
            ApiClient apiClient, WorkRequestHandler.WorkRequest request, Map<String, User> users) {

        LOGGER.info("Driver message request found:\n" + request);

        long topic = (request.topic != null) ? request.topic : DRIVERS_POST_STAGING_TOPIC_ID;

        String version = request.version;

        // Download file
        String routedDeliveries = apiClient.downloadFile(request.uploadFile.getFileName());
        request.postStatus(WorkRequestHandler.RequestStatus.Processing, "");

        try {
            String statusMessage = generateDriverPosts(apiClient, users, routedDeliveries, topic);
            request.postStatus(WorkRequestHandler.RequestStatus.Succeeded, statusMessage);
        } catch (MemberDataException ex) {
            String reason = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            request.postStatus(WorkRequestHandler.RequestStatus.Failed, reason);
        }
    }

    /**
     * Check the Request Driver Messages topic for a request.
     * Generate driver messages if a request is present.
     *
     * @param apiClient Discourse API handling
     * @param allMembersFile CSV file of all current members
     */
    private static void oneKitchenDriverMessages(ApiClient apiClient, String allMembersFile)
            throws IOException, CsvException {
        Query query = new Query(
                Constants.QUERY_GET_LAST_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES_REPLY,
                Constants.TOPIC_REQUEST_SINGLE_RESTAURANT_DRIVER_MESSAGES);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);

        WorkRequestHandler.Reply reply;

        try {
            reply = requestHandler.getLastReply();
        } catch (MemberDataException ex) {
            LOGGER.warn("getLastReply failed: " + ex + "\n" + ex.getMessage());
            requestHandler.postStatus(WorkRequestHandler.RequestStatus.Failed, ex.getMessage());
            return;
        }

        // Nothing to do.
        if (reply instanceof WorkRequestHandler.Status) {
            return;
        }

        WorkRequestHandler.WorkRequest request = (WorkRequestHandler.WorkRequest) reply;

        // Parse users files
        String csvData = Files.readString(Paths.get(allMembersFile));
        Map<String, User> users = new Tables(HBParser.users(csvData)).mapByUserName();

        doOneKitchenDriverMessages(apiClient, request, users);
    }

    private static void doOneKitchenDriverMessages(
            ApiClient apiClient, WorkRequestHandler.WorkRequest request, Map<String, User> users) {

        long topic = (request.topic != null) ? request.topic : DRIVERS_POST_STAGING_TOPIC_ID;

        // Download file
        String routedDeliveries = apiClient.downloadFile(request.uploadFile.getFileName());
        request.postStatus(WorkRequestHandler.RequestStatus.Processing, "");

        try {
            String statusMessage = generateDriverPosts(apiClient, users, routedDeliveries, topic);
            request.postStatus(WorkRequestHandler.RequestStatus.Succeeded, statusMessage);
        } catch (MemberDataException ex) {
            String reason = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            request.postStatus(WorkRequestHandler.RequestStatus.Failed, reason);

        }
    }

    private static String generateDriverPosts(ApiClient apiClient, Map<String, User> users,
          String routedDeliveries, long topic) {

        StringBuilder statusMessages = new StringBuilder();
        List<String> postURLs = new ArrayList<>();
        String groupPostURL = null;

        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(apiClient, users, routedDeliveries);

        List<String> posts = driverPostFormat.generateDriverPosts();
        Iterator<Driver> driverIterator = driverPostFormat.getDrivers().iterator();
        for (String rawPost : posts) {
            Post post = new Post();
            post.title = "Generated Driver Post";
            post.topic_id = topic;
            post.raw = rawPost;
            post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

            HttpResponse<?> response = apiClient.post(post.toJson());

            LOGGER.info("generateDriversPosts {}", response.statusCode() == HTTP_OK ?
                    "" : "failed " + response.statusCode() + ": " + response.body());

            if (response.statusCode() != HTTP_OK) {
                statusMessages.append("Failed posting driver's message: ")
                        .append(response.statusCode()).append(": ").append(response.body()).append("\n");
            } else {
                PostResponse postResponse = HBParser.postResponse((String)response.body());
                postURLs.add(
                        "["
                        + driverIterator.next().getUserName()
                        + " message](https://go.helpberkeley.org/t/"
                        + postResponse.topicSlug
                        + '/'
                        + postResponse.topicId
                        + '/'
                        + postResponse.postNumber
                        + ')');
            }
        }

        Post post = new Post();
        post.title = "Generated Group Instructions Post";
        post.topic_id = topic;
        post.raw = driverPostFormat.generateGroupInstructionsPost();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        LOGGER.info("generateGroupInstructionsPost {}", response.statusCode() == HTTP_OK ?
                "" : "failed " + response.statusCode() + ": " + response.body());

        if (response.statusCode() != HTTP_OK) {
            statusMessages.append("Failed posting group instructions message: ")
                    .append(response.statusCode()).append(": ").append(response.body()).append("\n");
        } else {
            PostResponse postResponse = HBParser.postResponse((String)response.body());
            groupPostURL = ("https://go.helpberkeley.org/t/"
                    + postResponse.topicSlug
                    + '/'
                    + postResponse.topicId
                    + '/'
                    + postResponse.postNumber);
        }

        String driversTableURL = generateDriversTablePost(
                apiClient, driverPostFormat, topic, statusMessages);
        String ordersTableURL = generateOrdersTablePost(
                apiClient, driverPostFormat, topic, statusMessages);
        String backupDriversPostURL = generateBackupDriversPost(
                apiClient, driverPostFormat, topic, statusMessages);

        statusMessages.append(driverPostFormat.statusMessages());
        statusMessages.append("\n\n");

        statusMessages.append(driverPostFormat.generateSummary());

        statusMessages.append("**Messages Posted to [Get driver messages]")
                .append("(https://go.helpberkeley.org/t/")
                .append(topic)
                .append(")**\n\n");

        if (driversTableURL != null) {
            statusMessages.append("\n[Pickup Manager Drivers Table](").append(driversTableURL).append(")");
        }

        if (ordersTableURL != null) {
            statusMessages.append("\n[Pickup Manager Orders Table](").append(ordersTableURL).append(")");
        }

        if (groupPostURL != null) {
            statusMessages.append("\n[Group Instructions](").append(groupPostURL).append(")");
        }

        statusMessages.append("\n\n");

        for (String url : postURLs) {
            statusMessages.append(url).append("\n");
        }

        if (backupDriversPostURL != null) {
            statusMessages.append("\n[Backup Drivers Message](").append(backupDriversPostURL).append(")");
        }

        return statusMessages.toString();
    }

    private static String generateDriversTablePost(ApiClient apiClient, DriverPostFormat driverPostFormat,
           long topic, StringBuilder statusMessages) {

        if (! (driverPostFormat instanceof DriverPostFormatV300)) {
            return null;
        }

        DriverPostFormatV300 driverPostFormatV300 = (DriverPostFormatV300) driverPostFormat;

        Post post = new Post();
        post.title = "Generated Drivers Table Post";
        post.topic_id = topic;
        post.raw = driverPostFormatV300.generateDriversTablePost();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        LOGGER.info("generateDriversTablePost {}", response.statusCode() == HTTP_OK ?
                "" : "failed " + response.statusCode() + ": " + response.body());

        if (response.statusCode() != HTTP_OK) {
            statusMessages.append("Failed posting pickup manager message: ")
                    .append(response.statusCode()).append(": ").append(response.body()).append("\n");
        } else {
            PostResponse postResponse = HBParser.postResponse((String)response.body());
            return "https://go.helpberkeley.org/t/"
                    + postResponse.topicSlug
                    + '/'
                    + postResponse.topicId
                    + '/'
                    + postResponse.postNumber;
        }

        return null;
    }

    private static String generateOrdersTablePost(ApiClient apiClient, DriverPostFormat driverPostFormat,
                                                   long topic, StringBuilder statusMessages) {

        if (! (driverPostFormat instanceof DriverPostFormatV300)) {
            return null;
        }

        DriverPostFormatV300 driverPostFormatV300 = (DriverPostFormatV300) driverPostFormat;

        Post post = new Post();
        post.title = "Generated Orders Table Post";
        post.topic_id = topic;
        post.raw = driverPostFormatV300.generateOrdersTablePost();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        LOGGER.info("generateOrdersTablePost {}", response.statusCode() == HTTP_OK ?
                "" : "failed " + response.statusCode() + ": " + response.body());

        if (response.statusCode() != HTTP_OK) {
            statusMessages.append("Failed posting pickup manager message: ")
                    .append(response.statusCode()).append(": ").append(response.body()).append("\n");
        } else {
            PostResponse postResponse = HBParser.postResponse((String)response.body());
            return "https://go.helpberkeley.org/t/"
                    + postResponse.topicSlug
                    + '/'
                    + postResponse.topicId
                    + '/'
                    + postResponse.postNumber;
        }

        return null;
    }

    private static String generateBackupDriversPost(ApiClient apiClient, DriverPostFormat driverPostFormat,
                                                  long topic, StringBuilder statusMessages) {

        if (! (driverPostFormat instanceof DriverPostFormatV200)) {
            return null;
        }

        DriverPostFormatV200 driverPostFormatV200 = (DriverPostFormatV200) driverPostFormat;
        Post post = new Post();
        post.title = "Generated Backup Driver Post";
        post.topic_id = topic;
        post.raw = driverPostFormat.generateBackupDriverPost();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        LOGGER.info("generateBackupDriverPost {}", response.statusCode() == HTTP_OK ?
                "" : "failed " + response.statusCode() + ": " + response.body());

        if (response.statusCode() != HTTP_OK) {
            statusMessages.append("Failed posting backup driver message: ")
                    .append(response.statusCode()).append(": ").append(response.body()).append("\n");
        } else {
            PostResponse postResponse = HBParser.postResponse((String)response.body());
            return "https://go.helpberkeley.org/t/"
                    + postResponse.topicSlug
                    + '/'
                    + postResponse.topicId
                    + '/'
                    + postResponse.postNumber;
        }

        return null;
    }

    // FIX THIS, DS: update WorkRequestHandler postStatus() to handle this.  Then we can do end to end tests
    //               using WorkReqeustHandler.getLastStatusPost()
    private static void postRequestDriverRoutesSucceeded(ApiClient apiClient, final String fileName,
            final String shortURL, final String statusMessage) {

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

        String rawPost = timeStamp + "\n"
                + "Status: " + WorkRequestHandler.RequestStatus.Succeeded + "\n"
                + "File: " + "[" + fileName + "](" + shortURL + ")\n"
                + "\n"
                + statusMessage + "\n";

        Post post = new Post();
        post.title = "Request Driver Route status response";
        post.topic_id = Constants.TOPIC_REQUEST_DRIVER_ROUTES.getId();
        post.raw = rawPost;
        post.createdAt = timeStamp;

        HttpResponse<?> response = apiClient.post(post.toJson());

        // FIX THIS, DS: what to do with this error?
        assert response.statusCode() == HTTP_OK : "failed " + response.statusCode() + ": " + response.body();
    }

    // Process the last request in the Post completed daily orders topic
    private static void completedDailyOrders(
            ApiClient apiClient, String allMembersFile) throws IOException, CsvException {
        Query query = new Query(
                Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY, Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);

        WorkRequestHandler.Reply reply;

        try {
            reply = requestHandler.getLastReply();
        } catch (MemberDataException ex) {
            LOGGER.warn("getLastReply failed: " + ex + "\n" + ex.getMessage());
            requestHandler.postStatus(WorkRequestHandler.RequestStatus.Failed, ex.getMessage());
            return;
        }

        if (reply instanceof WorkRequestHandler.Status) {
            return;
        }

        WorkRequestHandler.WorkRequest request = (WorkRequestHandler.WorkRequest) reply;

        // Read users file
        String csvData = Files.readString(Paths.get(allMembersFile));
        // Parse users
        List<User> userList = HBParser.users(csvData);
        // Build a map of users by user name.
        Map<String, User> users = new Tables(userList).mapByUserName();

        doCompletedDailyOrders(apiClient, request, users);
    }

    private static void doCompletedDailyOrders(
            ApiClient apiClient, WorkRequestHandler.WorkRequest request, Map<String, User> users) {

        try {
            if (! request.disableDateAudit) {
                // Check that the date is recent
                auditCompletedOrdersDate(request.date);
            }

            // Download file
            String completedDeliveries = apiClient.downloadFile(request.uploadFile.getFileName());

            // Validate
            DriverPostFormat.create(apiClient, users, completedDeliveries);

        } catch (MemberDataException ex) {
            String reason = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            request.postStatus(WorkRequestHandler.RequestStatus.Failed, reason);
            return;
        }

        // Copy post to Order History Data

        Post post = new Post();
        post.title = request.date;
        post.topic_id = Constants.TOPIC_ORDER_HISTORY_DATA.getId();
        post.raw = request.raw;
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<String> response = apiClient.post(post.toJson());

        if (response.statusCode() != HTTP_OK) {
            // Send status message
            request.postStatus(WorkRequestHandler.RequestStatus.Failed,
                    "Archive of " + request.uploadFile.getOriginalFileName() + " failed: " + response.body());
        } else {
            // Send status message
            request.postStatus(WorkRequestHandler.RequestStatus.Succeeded,
                    request.uploadFile.getOriginalFileName() + " validated and archived for " + request.date);
        }
    }

    private static void auditCompletedOrdersDate(String date) {

        LocalDate completedOrdersDate = LocalDate.parse(date.replaceAll("/", "-"));
        LocalDate today = LocalDate.now(Constants.TIMEZONE);
        long daysBetween = ChronoUnit.DAYS.between(completedOrdersDate, today);

        String error = "";

        if (daysBetween < 0) {
            error = "Invalid date, " +  date + " is in the future.";
        } else if (daysBetween > 7) {
            error = "Invalid date, " +  date + " is more than one week ago.";
        }

        if (! error.isEmpty()) {
            throw new MemberDataException(error);
        }
    }

    private static void orderHistory(ApiClient apiClient, String usersFile)
            throws IOException, CsvException {

        // Load users
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = HBParser.users(csvData);
        Tables tables = new Tables(users);
        Map<String, User> usersByUserName = tables.mapByUserName();

        // Get the last posted order history
        OrderHistory orderHistory = OrderHistory.getOrderHistory(apiClient);

        // Get the order history data posts
        OrderHistoryDataPosts orderHistoryDataPosts = new OrderHistoryDataPosts(apiClient);

        // Merge in the new data
        orderHistory.merge(orderHistoryDataPosts, usersByUserName);

        // Export updated order history
        String fileName = new OrderHistoryExporter(orderHistory).orderHistoryToFile();

        // Upload new order history
        Upload upload = new Upload(apiClient, fileName);

        // Update order history post
        updateFile(apiClient, upload.getFileName(), upload.getShortURL(), ORDER_HISTORY_TITLE, ORDER_HISTORY_POST_ID);

        // Update last processed order history data post number
        orderHistoryDataPosts.updateLastProcessedPost();
    }

    private static void drivers(ApiClient apiClient, String usersFile) throws IOException, CsvException {
        // Load users
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = HBParser.users(csvData);

        // Fetch driver details
        String json = apiClient.runQuery(Constants.QUERY_GET_DRIVER_DETAILS);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        Map<String, DetailsPost> driverDetails = HBParser.driverDetails(apiQueryResult);

        // Fetch driver history
        Map<String, DriverHistory> driverHistory = DriverHistory.getDriverHistory(apiClient);

        DriverExporter driverExporter = new DriverExporter(users, driverHistory, driverDetails);

        // Export drivers
        String fileName = driverExporter.driversToFile();

        // Upload it to Discourse
        Upload upload = new Upload(apiClient, fileName);
        // Post
        postFile(apiClient, fileName, upload.getShortURL(), DRIVERS_TITLE, DRIVERS_POST_TOPIC);

        // Generate short table post
        String post = driverExporter.shortPost();
        // update the posting
        HttpResponse<?> response = apiClient.updatePost(DRIVERS_TABLE_SHORT_POST_ID, post);
        // FIX THIS, DS: what to do with this error?
        assert response.statusCode() == HTTP_OK : "failed " + response.statusCode() + ": " + response.body();

        // Generate long table post
        post = driverExporter.longPost();
        // update the posting
        response = apiClient.updatePost(DRIVERS_TABLE_LONG_POST_ID, post);
        // FIX THIS, DS: what to do with this error?
        assert response.statusCode() == HTTP_OK : "failed " + response.statusCode() + ": " + response.body();

        Optional<Post> value = driverExporter.needsTraining();
        if (value.isPresent()) {
            // post it
            response = apiClient.post(value.get().toJson());
            // FIX THIS, DS: what to do with this error?
            assert response.statusCode() == HTTP_OK : "failed " + response.statusCode() + ": " + response.body();
        }
    }

    private static void driverHistory(ApiClient apiClient) throws IOException, CsvException {
        // Generate the driver history table
        String driverHistoryTable = DriverHistory.generateDriverHistory(apiClient);

        // Export updated order history
        String fileName = new DriverHistoryExporter(driverHistoryTable).driverHistoryToFile();

        // Upload new driver history
        Upload upload = new Upload(apiClient, fileName);

        // Update order history post
        updateFile(apiClient, upload.getFileName(), upload.getShortURL(),
                DRIVER_HISTORY_TITLE, DRIVER_HISTORY_POST_ID);
    }

    private static void restaurantTemplate(ApiClient apiClient) {

        Query query = new Query(
                Constants.QUERY_GET_LAST_RESTAURANT_TEMPLATE_REPLY,
                Constants.TOPIC_POST_RESTAURANT_TEMPLATE);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);

        WorkRequestHandler.Reply reply;

        try {
            reply = requestHandler.getLastReply();
        } catch (MemberDataException ex) {
            LOGGER.warn("getLastReply failed: " + ex + "\n" + ex.getMessage());
            requestHandler.postStatus(WorkRequestHandler.RequestStatus.Failed, ex.getMessage());
            return;
        }

        if (reply instanceof WorkRequestHandler.Status) {
            return;
        }

        WorkRequestHandler.WorkRequest request = (WorkRequestHandler.WorkRequest) reply;
        doRestaurantTemplate(apiClient, request, Constants.TOPIC_RESTAURANT_TEMPLATE_STORAGE.getId());
    }

    private static void oneKitchenRestaurantTemplate(ApiClient apiClient) {

        Query query = new Query(
                Constants.QUERY_GET_LAST_ONE_KITCHEN_RESTAURANT_TEMPLATE_REPLY,
                Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);

        WorkRequestHandler.Reply reply;

        try {
            reply = requestHandler.getLastReply();
        } catch (MemberDataException ex) {
            LOGGER.warn("getLastReply failed: " + ex + "\n" + ex.getMessage());
            requestHandler.postStatus(WorkRequestHandler.RequestStatus.Failed, ex.getMessage());
            return;
        }

        if (reply instanceof WorkRequestHandler.Status) {
            return;
        }

        WorkRequestHandler.WorkRequest request = (WorkRequestHandler.WorkRequest) reply;
        doRestaurantTemplate(apiClient, request, Constants.TOPIC_ONE_KITCHEN_RESTAURANT_TEMPLATE_STORAGE.getId());
    }

    private static void doRestaurantTemplate(
            ApiClient apiClient, WorkRequestHandler.WorkRequest request, int topicId) {

        try {
            // Download file
            String restaurantTemplate = apiClient.downloadFile(request.uploadFile.getFileName());
            RestaurantTemplateParser.create(restaurantTemplate).restaurants();

        } catch (MemberDataException ex) {
            String reason = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            request.postStatus(WorkRequestHandler.RequestStatus.Failed, reason);
            return;
        }

        // Copy post to Restaurant Template Storage

        Post post = new Post();
        post.title = request.date;
        post.topic_id = topicId;
        post.raw = request.raw;
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<String> response = apiClient.post(post.toJson());

        if (response.statusCode() != HTTP_OK) {
            // Send status message
            request.postStatus(WorkRequestHandler.RequestStatus.Failed,
                    "Archive of " + request.uploadFile.getOriginalFileName() + " failed: " + response.body());
        } else {
            // Send status message
            request.postStatus(WorkRequestHandler.RequestStatus.Succeeded,
                    request.uploadFile.getOriginalFileName() + " validated and archived for " + request.date);
        }
    }

    private static void customerCarePost(
            ApiClient apiClient, String usersFile) throws IOException, CsvException {
        // Load users
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = HBParser.users(csvData);

        UserExporter userExporter = new UserExporter(users);

        // Generate customer care member data table
        String post = userExporter.customerCareMemberDataPost();
        // update the posting
        HttpResponse<?> response = apiClient.updatePost(CUSTOMER_CARE_MEMBER_DATA_POST_ID, post);
        // FIX THIS, DS: what to do with this error?
        assert response.statusCode() == HTTP_OK : "failed " + response.statusCode() + ": " + response.body();
    }

    private static void frreg(
            ApiClient apiClient, String usersFile) throws IOException, CsvException {
        // Load users
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = HBParser.users(csvData);

        UserExporter userExporter = new UserExporter(users);

        // Generate customer care member data table
        String post = userExporter.freegPost();
        // update the posting
        HttpResponse<?> response = apiClient.updatePost(FRREG_POST_ID, post);
        // FIX THIS, DS: what to do with this error?
        assert response.statusCode() == HTTP_OK : "failed " + response.statusCode() + ": " + response.body();
    }

    private static void workRequests(ApiClient apiClient, String usersFile) throws IOException, CsvException {
        String json = apiClient.runQuery(Constants.QUERY_GET_LAST_REPLY_FROM_REQUEST_TOPICS_V20);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        assert apiQueryResult.rows.length == 6 : apiQueryResult.rows.length;
        Integer postNumberIndex = apiQueryResult.getColumnIndex(Constants.DISCOURSE_COLUMN_POST_NUMBER);
        assert postNumberIndex != null;
        Integer rawIndex = apiQueryResult.getColumnIndex(Constants.DISCOURSE_COLUMN_RAW);
        assert rawIndex != null;
        Integer topicIdIndex = apiQueryResult.getColumnIndex(Constants.DISCOURSE_COLUMN_TOPIC_ID);
        assert topicIdIndex != null;

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 4 : columns.length;

            Long topicId = (Long)columns[topicIdIndex];
            Long postNumber = (Long)columns[postNumberIndex];
            String raw = (String)columns[rawIndex];

            WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, topicId, postNumber, raw);
            WorkRequestHandler.Reply reply;

            try {
                reply = requestHandler.getLastReply();
            } catch (MemberDataException ex) {
                LOGGER.warn("getLastReply failed: " + ex + "\n" + ex.getMessage());
                requestHandler.postStatus(WorkRequestHandler.RequestStatus.Failed, ex.getMessage());
                continue;
            }

            // Nothing to do.
            if (reply instanceof WorkRequestHandler.Status) {
                continue;
            }

            // Parse users files
            String csvData = Files.readString(Paths.get(usersFile));
            Map<String, User> users = new Tables(HBParser.users(csvData)).mapByUserName();

            WorkRequestHandler.WorkRequest request = (WorkRequestHandler.WorkRequest) reply;

            if (topicId == Constants.TOPIC_REQUEST_DRIVER_MESSAGES.getId()) {
                doDriverMessages(apiClient, request, users);
            } else if (topicId == Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS.getId()) {
                doCompletedDailyOrders(apiClient, request, users);
            } else if (topicId == Constants.TOPIC_REQUEST_SINGLE_RESTAURANT_DRIVER_MESSAGES.getId()) {
                doOneKitchenDriverMessages(apiClient, request, users);
            } else if (topicId == Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE.getId()) {
                doRestaurantTemplate(apiClient, request,
                        Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE.getId());
            } else if (topicId == Constants.TOPIC_POST_RESTAURANT_TEMPLATE.getId()) {
                doRestaurantTemplate(apiClient, request, Constants.TOPIC_POST_RESTAURANT_TEMPLATE.getId());
            } else {
                assert topicId == Constants.TOPIC_REQUEST_DRIVER_ROUTES.getId() : topicId;
                doDriverRoutes(apiClient, request);
            }
        }
    }

    private static void testRequest(ApiClient apiClient, String usersFile) {
        String json = apiClient.runQuery(Constants.QUERY_GET_LAST_TEST_REQUEST);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        assert apiQueryResult.rows.length == 1 : apiQueryResult.rows.length;
        Integer topicIdIndex = apiQueryResult.getColumnIndex(Constants.DISCOURSE_COLUMN_TOPIC_ID);
        assert topicIdIndex != null;
        Integer postNumberIndex = apiQueryResult.getColumnIndex(Constants.DISCOURSE_COLUMN_POST_NUMBER);
        assert postNumberIndex != null;
        Integer rawIndex = apiQueryResult.getColumnIndex(Constants.DISCOURSE_COLUMN_RAW);
        assert rawIndex != null;

        Object rowObj = apiQueryResult.rows[0];
        Object[] columns = (Object[]) rowObj;
        assert columns.length == 4 : columns.length;
        Long topicId = (Long)columns[topicIdIndex];
        Long postNumber = (Long)columns[postNumberIndex];
        String raw = (String)columns[rawIndex];

        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, topicId, postNumber, raw);
        WorkRequestHandler.Reply reply;

        try {
            reply = requestHandler.getLastReply();

            // Nothing to do.
            if (reply instanceof WorkRequestHandler.Status) {
                return;
            }

            // Parse users files
            String csvData = Files.readString(Paths.get(usersFile));
            Map<String, User> users = new Tables(HBParser.users(csvData)).mapByUserName();

            WorkRequestHandler.WorkRequest request = (WorkRequestHandler.WorkRequest) reply;
            request.setTestTopic();
            doOneKitchenDriverMessages(apiClient, request, users);
        } catch (MemberDataException | IOException | CsvException ex) {
            LOGGER.warn("getLastReply failed: " + ex + "\n" + ex.getMessage());
            requestHandler.postStatus(WorkRequestHandler.RequestStatus.Failed, ex.getMessage());
        }
    }

//    private static void testQuery(ApiClient apiClient) {
//
////        String json = apiClient.runQuery(5);
//        String json = apiClient.runQueryWithParam(5, "limit", "100000");
//        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
//
////        json = apiClient.runQuery(5);
////        apiQueryResult = HBParser.parseQueryResult(json);
//
////
//        System.exit(0);
//
////        String json = apiClient.runQuery(Constants.QUERY_GET_GROUP_USERS_ID);
////        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
////        Map<String, List<Long>> groupUsers = HBParser.groupUsers(groupNames, apiQueryResult);
//
//    }
}
