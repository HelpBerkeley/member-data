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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_OK;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

    static final String MEMBERDATA_PROPERTIES = "memberdata.properties";
    static final String API_USER_PROPERTY = "Api-Username";
    static final String API_KEY_PROPERTY = "Api-Key";

    static final String MEMBERDATA_ERRORS_FILE = "memberdata-errors";
    static final String MEMBERDATA_REPORT_FILE = "member-data-report";
    static final String MEMBERDATA_WITH_EMAIL_REPORT_FILE = "member-data-with-email";
    static final String MEMBERDATA_RAW_FILE = "member-data-raw";
    static final String CONSUMER_REQUESTS_FILE = "consumer-requests";
    static final String VOLUNTEER_REQUESTS_FILE = "volunteer-requests";
    static final String DRIVERS_FILE = "drivers";
    static final String WORKFLOW_FILE = "workflow";
    static final String INREACH_FILE = "inreach";
    static final String DISPATCHERS_FILE = "dispatchers";
    static final String ORDER_HISTORY_FILE = "order-history";
    static final String DELIVERY_POSTS_FILE = "delivery-posts";

    static final String ALL_MEMBERS_TITLE = "All Members";
    static final String WORKFLOW_TITLE = "Workflow Data";
    static final String DISPATCHERS_TITLE = "Dispatchers Info";
    static final String INREACH_TITLE = "Customer Info";
    static final String DRIVERS_TITLE = "Volunteer Drivers";
    static final String ORDER_HISTORY_TITLE = "Order History";

    // FIX THIS, DS: make this less fragile
    static final long MEMBER_DATA_REQUIRING_ATTENTION_TOPIC_ID = 129;
    static final long MEMBER_DATA_REQUIRING_ATTENTION_POST_ID = 1706;
    static final long CONSUMER_REQUESTS_TOPIC_ID = 444;
    static final long CONSUMER_REQUESTS_POST_ID = 1776;
    static final long VOLUNTEER_REQUESTS_TOPIC_ID = 445;
    static final long DRIVERS_POST_TOPIC = 638;
    static final long ALL_MEMBERS_POST_TOPIC = 837;
    static final long WORKFLOW_DATA_TOPIC = 824;
    static final long INREACH_POST_TOPIC = 820;
    static final long COMPLETED_DAILY_DELIVERIES_TOPIC = 859;
    static final long DISPATCHERS_POST_TOPIC = 938;
    static final long STONE_TEST_TOPIC = 422;
    static final long DISPATCHERS_POST_ID = 5324;
    static final long ORDER_HISTORY_TOPIC = 1440;
    static final long ORDER_HISTORY_POST_ID = 6433;
//    static final long RESTAURANT_TEMPLATE_POST_ID = 8664;
    static final long DELIVERY_DETAILS_TOPIC_ID = 1818;
    static final long RESTAURANT_TEMPLATE_POST_ID = 9739;

    public static void main(String[] args) throws IOException, InterruptedException, CsvException {

        Options options = new Options(args);
        try {
            options.parse();
        } catch (Options.OptionsException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        }

        // Load member data properties
        Properties memberDataProperties = loadProperties(MEMBERDATA_PROPERTIES);

        // Set up an HTTP client
        ApiClient apiClient = new ApiClient(memberDataProperties);

        switch (options.getCommand()) {
            case Options.COMMAND_FETCH:
                fetch(apiClient);
                break;
            case Options.COMMAND_GET_ORDER_HISTORY:
                getOrderHistory(apiClient);
                break;
            case Options.COMMAND_GET_DELIVERY_DETAILS:
                getDeliveryDetails(apiClient);
                break;
            case Options.COMMAND_MERGE_ORDER_HISTORY:
                mergeOrderHistory(apiClient, options.getFileName(),
                        options.getSecondFileName(), options.getThirdFileName());
                break;
            case Options.COMMAND_UPDATE_ORDER_HISTORY:
                updateFile(apiClient, options.getFileName(),
                        options.getShortURL(), ORDER_HISTORY_TITLE, ORDER_HISTORY_POST_ID);
                break;
            case Options.COMMAND_GET_DAILY_DELIVERIES:
                getDailyDeliveryPosts(apiClient);
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
                postFile(apiClient, options.getFileName(),
                        options.getShortURL(), ALL_MEMBERS_TITLE, ALL_MEMBERS_POST_TOPIC);
                break;
            case Options.COMMAND_POST_WORKFLOW:
                postFile(apiClient, options.getFileName(),
                        options.getShortURL(), WORKFLOW_TITLE, WORKFLOW_DATA_TOPIC);
                break;
            case Options.COMMAND_POST_INREACH:
                postFile(apiClient, options.getFileName(),
                        options.getShortURL(), INREACH_TITLE, INREACH_POST_TOPIC);
                break;
            case Options.COMMAND_POST_DISPATCHERS:
                postFile(apiClient, options.getFileName(),
                        options.getShortURL(), DISPATCHERS_TITLE, DISPATCHERS_POST_TOPIC);
                break;
            case Options.COMMAND_UPDATE_DISPATCHERS:
                updateFile(apiClient, options.getFileName(),
                        options.getShortURL(), DISPATCHERS_TITLE, DISPATCHERS_POST_ID);
                break;
            case Options.COMMAND_INREACH:
                generateInreach(options.getFileName(), options.getSecondFileName());
                break;
            case Options.COMMAND_EMAIL:
                generateEmail(apiClient, options.getFileName());
                break;
            case Options.COMMAND_WORKFLOW:
                generateWorkflow(apiClient, options.getFileName());
                break;
            default:
                assert options.getCommand().equals(Options.COMMAND_POST_DRIVERS) : options.getCommand();
                postFile(apiClient, options.getFileName(),
                        options.getShortURL(), DRIVERS_TITLE, DRIVERS_POST_TOPIC);
                break;
        }
    }

    static Properties loadProperties(final String fileName) throws IOException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL propertiesFile = classLoader.getResource(fileName);

        if (propertiesFile == null) {
            LOGGER.error("Required properties file {} cannot be found", fileName);
            System.exit(1);
        }

        Properties properties = new Properties();

        //noinspection EmptyFinallyBlock
        try (InputStream is = propertiesFile.openStream())
        {
            properties.load(is);
        } finally {}

        return properties;
    }

    static void fetch(ApiClient apiClient) throws InterruptedException, IOException {
        // Create a User loader
        Loader loader = new Loader(apiClient);

        // Load the member data from the website
        List<User> users = loader.load();

        // Create an exporter
        UserExporter exporter = new UserExporter(users);

        // Export all users
        exporter.allMembersRawToFile(MEMBERDATA_RAW_FILE);

        // Export all users report
        exporter.allMembersReportToFile(MEMBERDATA_REPORT_FILE);

        // Export any user errors
        exporter.errorsToFile(MEMBERDATA_ERRORS_FILE);

        // Export non-consumer group members, with a consumer request
        exporter.consumerRequestsToFile(CONSUMER_REQUESTS_FILE);

        // Export new volunteers-consumer group members, with a volunteer request
        exporter.volunteerRequestsToFile(VOLUNTEER_REQUESTS_FILE);

        // Export drivers
        exporter.driversToFile(DRIVERS_FILE);

        // Export dispatchers
        exporter.dispatchersToFile(DISPATCHERS_FILE);
    }

    static void postConsumerRequests(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException, CsvException {

        String csvData = Files.readString(Paths.get(fileName));
        List<User> users = Parser.users(csvData);

        StringBuilder postRaw = new StringBuilder();
        String label =  "Newly created members requesting meals -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | Email Verified | City | Address | Condo | Phone |\n");
        postRaw.append("|---|---|---|---|---|---|\n");

        Tables tables = new Tables(users);
        for (User user : tables.sortByCreateTime()) {
            postRaw.append('|');
            postRaw.append('@');
            postRaw.append(user.getUserName());
            postRaw.append('|');
            postRaw.append(user.getEmailVerified());
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

    static void postVolunteerRequests(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException, CsvException {

        String csvData = Files.readString(Paths.get(fileName));
        List<User> users = Parser.users(csvData);

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
            postRaw.append('@');
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

    static void postUserErrors(ApiClient apiClient, final String fileName) throws IOException, InterruptedException {

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

    static void updateUserErrors(ApiClient apiClient, final String fileName) throws IOException, InterruptedException {

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

    static void postFile(ApiClient apiClient, final String fileName, final String shortUrl,
                 final String title, long topicId) throws IOException, InterruptedException {

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

    static void updateFile(ApiClient apiClient, final String fileName,
                final String shortUrl, String title, long postId) throws IOException, InterruptedException {

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

    static void getOrderHistory(ApiClient apiClient) throws IOException, InterruptedException {

        // Fetch the order history post
        String json = apiClient.getPost(ORDER_HISTORY_POST_ID);

        // Parse the order history post
        String rawPost = Parser.postBody(json);
        OrderHistoryPost orderHistoryPost = Parser.orderHistoryPost(rawPost);

        // Download the order history data file
        String orderHistoryData = apiClient.downloadFile(orderHistoryPost.uploadFile.fileName);
        // Parse and load the order history into the OrderHistory object
        OrderHistory orderHistory = Parser.orderHistory(orderHistoryData);

        // Export the order history to a file
        new OrderHistoryExporter(orderHistory).orderHistoryToFile(ORDER_HISTORY_FILE);
    }

    static void getDailyDeliveryPosts(ApiClient apiClient) throws IOException, InterruptedException {
        List<DeliveryData> deliveryPosts = DeliveryData.deliveryPosts(apiClient);
        new DeliveryDataExporter(deliveryPosts).deliveryPostsToFile(DELIVERY_POSTS_FILE);
    }

    // FIX THIS, DS: remove?
    static void getDeliveryDetails(ApiClient apiClient) throws IOException, InterruptedException {
        String json = apiClient.runQuery(Constants.QUERY_GET_DELIVERY_DETAILS);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);
        Map<String, String> deliveryDetails = Parser.deliveryDetails(apiQueryResult);
    }

    static void mergeOrderHistory(ApiClient apiClient, final String usersFile,
        final String orderHistoryFile, final String deliveryPostsFile) throws IOException, InterruptedException, CsvException {

        // Load order history
        String csvData = Files.readString(Paths.get(orderHistoryFile));
        OrderHistory orderHistory = Parser.orderHistory(csvData);

        // Load delivery posts
        csvData = Files.readString(Paths.get(deliveryPostsFile));
        List<DeliveryData> deliveryDataList = DeliveryData.deliveryPosts(csvData);

        // Load users
        csvData = Files.readString(Paths.get(usersFile));
        List<User> users = Parser.users(csvData);

        List<DeliveryData> filesToProcess = new ArrayList<>();

        for (DeliveryData deliveryData : deliveryDataList) {
            // Process those newer than order history date
            if (deliveryData.date.compareTo(orderHistory.historyThroughDate) > 0) {
                filesToProcess.add(deliveryData);
            }
        }

        // If a reset of the order history has been done, we are going to download
        // all of the delivery files.  Avoid getting rate limited by Discourse, which
        // occurs when there are more than 60 requests per minute on a connection.
        long napTime = (filesToProcess.size() > 10) ? TimeUnit.SECONDS.toMillis(1) : 0;

        for (DeliveryData deliveryData : filesToProcess) {
                LOGGER.debug("processing " + deliveryData);
                // Download the delivery file
                String deliveries = apiClient.downloadFile(deliveryData.uploadFile.fileName);
                // Parse list of user restaurant orders
                List<UserOrder> userOrders = Parser.parseOrders(deliveryData.uploadFile.originalFileName, deliveries);
                // Merge the data into the existing order history
                orderHistory.merge(deliveryData.date, userOrders, users);

                Thread.sleep(napTime);
        }

        // Export updated order history
        new OrderHistoryExporter(orderHistory).orderHistoryToFile(ORDER_HISTORY_FILE);
    }

    static void generateInreach(final String usersFile, final String orderHistoryFile) throws IOException, CsvException {
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = Parser.users(csvData);

        csvData = Files.readString(Paths.get(orderHistoryFile));
        OrderHistory orderHistory = Parser.orderHistory(csvData);

        new UserExporter(users).inreachToFile(INREACH_FILE, orderHistory);
    }

    static void generateEmail(ApiClient apiClient, final String usersFile)
            throws IOException, InterruptedException, CsvException {
        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = Parser.users(csvData);

        Map<Long, String> emails = new Loader(apiClient).loadEmailAddresses();

        new UserExporter(users).allMembersWithEmailReportToFile(emails, MEMBERDATA_WITH_EMAIL_REPORT_FILE);
    }

    static void generateWorkflow(ApiClient apiClient, final String usersFile)
            throws IOException, InterruptedException, CsvException {

        String csvData = Files.readString(Paths.get(usersFile));
        List<User> users = Parser.users(csvData);

        String rawPost = Parser.postBody(apiClient.getPost(RESTAURANT_TEMPLATE_POST_ID));
        RestaurantTemplatePost restaurantTemplatePost = Parser.restaurantTemplatePost(rawPost);

        String json = apiClient.runQuery(Constants.QUERY_GET_DELIVERY_DETAILS);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);
        Map<String, String> deliveryDetails = Parser.deliveryDetails(apiQueryResult);

        String restaurantTemplate = apiClient.downloadFile(restaurantTemplatePost.uploadFile.fileName);
        new UserExporter(users).workflowToFile(restaurantTemplate, deliveryDetails, WORKFLOW_FILE);
    }
}

