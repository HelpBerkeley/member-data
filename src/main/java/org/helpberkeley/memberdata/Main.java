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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import static java.net.HttpURLConnection.HTTP_OK;

public class Main {

    static final String MEMBERDATA_PROPERTIES = "memberdata.properties";
    static final String API_USER_PROPERTY = "Api-Username";
    static final String API_KEY_PROPERTY = "Api-Key";

    static final String MEMBERDATA_ERRORS_FILE = "memberdata-errors";
    static final String MEMBERDATA_REPORT_FILE = "member-data-report";
    static final String MEMBERDATA_RAW_FILE = "member-data-raw";
    static final String CONSUMER_REQUESTS_FILE = "consumer-requests";
    static final String VOLUNTEER_REQUESTS_FILE = "volunteer-requests";
    static final String DRIVERS_FILE = "drivers";
    static final String WORKFLOW_FILE = "workflow";
    static final String INREACH_FILE = "inreach";
    static final String DISPATCHERS_FILE = "dispatchers";

    static final String ALL_MEMBERS_TITLE = "All Members";
    static final String WORKFLOW_TITLE = "Workflow Data";
    static final String DISPATCHERS_TITLE = "Dispatchers Info";
    static final String INREACH_TITLE = "Customer Info";
    static final String DRIVERS_TITLE = "Volunteer Drivers";

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

    public static void main(String[] args) throws IOException, InterruptedException {

        Options options = new Options(args);
        options.parse();

        // Load member data properties
        Properties memberDataProperties = loadProperties(MEMBERDATA_PROPERTIES);

        // Set up an HTTP client
        ApiClient apiClient = new ApiClient(memberDataProperties);

        switch (options.getCommand()) {
            case Options.COMMAND_FETCH:
                fetch(apiClient);
                break;
            case Options.COMMAND_GET_DAILY_DELIVERIES:
                getDailyDeliveries(apiClient);
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
            System.out.println("Required properties file " + fileName + " cannot be found");
            System.exit(1);
        }

        Properties properties = new Properties();

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
        Exporter exporter = new Exporter(users);

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

        // Export workflow
        exporter.workflowToFile(WORKFLOW_FILE);

        // Export inreach
        exporter.inreachToFile(INREACH_FILE);

        // Export dispatchers
        exporter.dispatchersToFile(DISPATCHERS_FILE);
    }

    static void postConsumerRequests(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException {

        String csvData = Files.readString(Paths.get(fileName));
        // FIX THIS, DS: constant for separator
        List<User> users = Parser.users(csvData, Constants.CSV_SEPARATOR);

        StringBuilder postRaw = new StringBuilder();
        String label =  "Newly created members requesting meals -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | City | Address | Apartment | Phone |\n");
        postRaw.append("|---|---|---|---|---|\n");

        Tables tables = new Tables(users);
        for (User user : tables.sortByCreateTime()) {
            postRaw.append('|');
            postRaw.append('@');
            postRaw.append(user.getUserName());
            postRaw.append('|');
            postRaw.append(user.getCity());
            postRaw.append('|');
            postRaw.append(user.getAddress());
            postRaw.append('|');
            postRaw.append(user.isApartment());
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
        System.out.println(response);
    }

    static void postVolunteerRequests(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException {

        String csvData = Files.readString(Paths.get(fileName));
        List<User> users = Parser.users(csvData, Constants.CSV_SEPARATOR);

        StringBuilder postRaw = new StringBuilder();
        String label =  "New members requesting to volunteer -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | Full Name | Phone | City | Volunteer Request |\n");
        postRaw.append("|---|---|---|---|---|---|\n");

        Tables tables = new Tables(users);
        for (User user : new Tables(tables.volunteerRequests()).sortByCreateTime()) {
            postRaw.append('|');
            postRaw.append('@');
            postRaw.append(user.getUserName());
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
        System.out.println(response);
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
        System.out.println(response);
    }

    static void updateUserErrors(ApiClient apiClient, final String fileName) throws IOException, InterruptedException {

        String postRaw = "**" +
                "Member data requiring attention -- " +
                ZonedDateTime.now(
                        ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss")) +
                "**\n\n" +
                Files.readString(Paths.get(fileName));
        HttpResponse<?> response = apiClient.updatePost(MEMBER_DATA_REQUIRING_ATTENTION_POST_ID, postRaw);
        System.out.println(response);
    }

    static void postFile(ApiClient apiClient, final String fileName, final String shortUrl,
                 final String title, long topicId) throws IOException, InterruptedException {

        String now = ZonedDateTime.now(ZoneId.systemDefault()).format(
                DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        Post post = new Post();
        post.title = title;
        post.topic_id = topicId;
        post.createdAt = now;
        String postRaw = "**" +
                title +
                " -- " +
                now +
                "**\n\n" +
                // postRaw.append("[" + fileName + "|attachment](upload://" + fileName + ") (5.49 KB)");
                "[" + fileName + "|attachment](" + shortUrl + ")";
        post.raw = postRaw;

        HttpResponse<?> response = apiClient.post(post.toJson());
        System.out.println(response);
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
        System.out.println(response);
    }

    static void getDailyDeliveries(ApiClient apiClient) throws IOException, InterruptedException {
        String json = apiClient.runQuery(Constants.QUERY_GET_DAILY_DELIVERIES);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);
        List<DeliveryData> deliveries = Parser.dailyDeliveries(apiQueryResult);
        for (DeliveryData deliveryData : deliveries) {
            HttpResponse<String> response = apiClient.downloadFile(deliveryData.fileName);
            if (response.statusCode() == HTTP_OK) {
                System.out.println("downloaded " + deliveryData);
                deliveryData.setDeliveryData(response.body());
            } else {
                System.out.println("Failed downloading " + deliveryData + ": " + response.body());
            }
        }
    }
}

