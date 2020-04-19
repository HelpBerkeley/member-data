/******************************************************************************
 * Copyright (c) 2020 helpberkeley.org
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
 ******************************************************************************/

package org.helpberkeley.memberdata;

import com.cedarsoftware.util.io.JsonWriter;

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

public class Main {

    static final String MEMBERDATA_PROPERTIES = "memberdata.properties";
    static final String API_USER_PROPERTY = "Api-Username";
    static final String API_KEY_PROPERTY = "Api-Key";

    static final String MEMBERDATA_ERRORS_FILE = "memberdata-errors";
    static final String MEMBERDATA_FILE = "member-data";
    static final String NON_CONSUMERS_FILE = "member-non-consumers";
    static final String CONSUMER_REQUESTS_FILE = "consumer-requests";
    static final String VOLUNTEER_REQUESTS_FILE = "volunteer-requests";
    static final String DRIVERS_FILE = "drivers";

    // FIX THIS, DS: make this less fragile
    static final long MEMBER_DATA_FOR_DISPATCHES_TOPID_ID = 86;
    static final long MEMBER_DATA_REQUIRING_ATTENTION_TOPIC_ID = 129;
    static final long MEMBER_DATA_REQUIRING_ATTENTION_POST_ID = 1706;
    static final long NON_CONSUMERS_TOPIC_ID = 336;
    static final long NON_CONSUMERS_POST_ID = 1219;
    static final long CONSUMER_REQUESTS_TOPIC_ID = 444;
    static final long CONSUMER_REQUESTS_POST_ID = 1776;
    static final long VOLUNTEER_REQUESTS_POST_ID = 1782;
    static final long VOLUNTEER_REQUESTS_TOPIC_ID = 445;
    static final long DRIVERS_POST_ID = 2808;
    static final long DRIVERS_POST_TOPIC = 638;
    static final long STONE_TEST_TOPIC = 422;

    public static void main(String[] args) throws IOException, InterruptedException, ApiException {

        Options options = new Options(args);
        options.parse();

        // Load member data properties
        Properties memberDataProperties = loadProperties(MEMBERDATA_PROPERTIES);

        // Set up an HTTP client
        ApiClient apiClient = new ApiClient(memberDataProperties);

        switch (options.getCommand()) {
            case Options.COMMAND_FETCH:
                // Create a User loader
                Loader loader = new Loader(apiClient);

                // Load the member data from the website
                List<User> users = loader.load();

                // Create an exporter
                Exporter exporter = new Exporter(users);

                // Export any user errors
                exporter.errorsToFile(MEMBERDATA_ERRORS_FILE);

                // Export all users
                exporter.allMembersToFile(MEMBERDATA_FILE);

                // Export recent, no-group members
//                exporter.recentlyCreatedNoGroupsToFile(NON_CONSUMERS_FILE);

                // Export non-consumer group members, with a consumer request
                exporter.consumerRequests(CONSUMER_REQUESTS_FILE);

                // Export new volunteers-consumer group members, with a volunteer request
                exporter.volunteerRequests(VOLUNTEER_REQUESTS_FILE);

                // Export drivers
                exporter.drivers(DRIVERS_FILE);
                break;
            case Options.COMMAND_POST_ERRORS:
                postUserErrors(apiClient, options.getFileName());
                break;
            case Options.COMMAND_UPDATE_NON_CONSUMERS:
                updateNonConsumersTable(apiClient, options.getFileName());
                break;
            case Options.COMMAND_UPDATE_ERRORS:
                updateUserErrors(apiClient, options.getFileName());
                break;
            case Options.COMMAND_UPDATE_CONSUMER_REQUESTS:
                updateConsumerRequests(apiClient, options.getFileName());
                break;
            case Options.COMMAND_UPDATE_VOLUNTEER_REQUESTS:
                updateVolunteerRequests(apiClient, options.getFileName());
                break;
            case Options.COMMAND_UPDATE_DRIVERS:
                updateDrivers(apiClient, options.getFileName(), options.getShortURL());
                break;
            case Options.COMMAND_POST_VOLUNTEER_REQUESTS:
                postVolunteerRequests(apiClient, options.getFileName());
                break;
            case Options.COMMAND_POST_CONSUMER_REQUESTS:
                postConsumerRequests(apiClient, options.getFileName());
                break;
            case Options.COMMAND_POST_DRIVERS:
                postDrivers(apiClient, options.getFileName(), options.getShortURL());
                break;
            default:
                assert options.getCommand().equals(Options.COMMAND_POST_NON_CONSUMERS) : options.getCommand();
                postNonConsumersTable(apiClient, options.getFileName());
                break;
        }

//        postWithLinkTest(apiClient);
//        System.exit(0);
//        uploadTest(apiClient);




        // Post the member data to the member data for drivers topic
//        postMemberData(apiClient, users);

        // Post subset of data needed by dispatchers
        // to make decision for promote-ability of someone to consumer.
//        postConsumerPromotionData(apiClient, nonConsumers);


//        usersToJson(apiClient);
//        usersToFile(apiClient);
//        postTableSortedByUserName(apiClient);
//        printCSVSortedByUser(apiClient);
//        getPostTest(apiClient);
//        latestPosts(apiClient);
//        postCSVTable(apiClient);

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

//    static void postMemberData(ApiClient apiClient, List<User> users) throws IOException, InterruptedException {
//        Tables tables = new Tables(users);
//        postFullMemberTable(apiClient, "Sorted by user name", tables.sortByUserName());
//    }
//
//    static void postFullMemberTable(ApiClient apiClient, String label, List<User> users) throws IOException, InterruptedException {
//
//        StringBuilder postRaw = new StringBuilder();
//
//        postRaw.append("**");
//        postRaw.append(label);
//        postRaw.append(" -- ");
//        postRaw.append(ZonedDateTime.now(
//                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss")));
//        postRaw.append("**\n\n");
//
//        postRaw.append("| Name | User Name | Phone # | Neighborhood | City | Address | Consumer | Dispatcher | Driver |\n");
//        postRaw.append("|---|---|---|---|---|---|---|---|---|\n");
//
//        for (User user : users) {
//            postRaw.append('|');
//            postRaw.append(user.getName());
//            postRaw.append('|');
//            postRaw.append(user.getUserName());
//            postRaw.append('|');
//            postRaw.append(user.getPhoneNumber());
//            postRaw.append('|');
//            postRaw.append(user.getNeighborhood());
//            postRaw.append('|');
//            postRaw.append(user.getCity());
//            postRaw.append('|');
//            postRaw.append(user.getAddress());
//            postRaw.append('|');
//            postRaw.append(user.isConsumer());
//            postRaw.append('|');
//            postRaw.append(user.isDispatcher());
//            postRaw.append('|');
//            postRaw.append(user.isDriver());
//            postRaw.append("|\n");
//        }
//
//        Post post = new Post();
//        post.title = label;
//        post.topic_id = MEMBER_DATA_FOR_DISPATCHES_TOPID_ID;
//        post.raw = postRaw.toString();
//        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
//                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));
//
//        HttpResponse<?> response = apiClient.post(post.toJson());
//        System.out.println(response);
//    }

    static void postNonConsumersTable(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException {

        String csvData = Files.readString(Paths.get(fileName));
        // FIX THIS, DS: constant for separator
        List<User> users = Parser.users(csvData, ",");

        StringBuilder postRaw = new StringBuilder();
        String label =  "Non-consumer members -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | Address | Apartment | Neighborhood | City |\n");
        postRaw.append("|---|---|---|---|---|\n");

        for (User user : users) {
            if (user.isConsumer()) {
                continue;
            }
            postRaw.append('|');
            postRaw.append('@');
            postRaw.append(user.getUserName());
            postRaw.append('|');
            postRaw.append(user.getAddress());
            postRaw.append('|');
            postRaw.append(user.isApartment());
            postRaw.append('|');
            postRaw.append(user.getNeighborhood());
            postRaw.append('|');
            postRaw.append(user.getCity());
            postRaw.append("|\n");
        }

        Post post = new Post();
        post.title = label;
        post.topic_id = NON_CONSUMERS_TOPIC_ID;
        post.raw = postRaw.toString();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        System.out.println(response);
    }

    static void updateNonConsumersTable(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException {

        String csvData = Files.readString(Paths.get(fileName));
        // FIX THIS, DS: constant for separator
        List<User> users = Parser.users(csvData, ",");

        StringBuilder postRaw = new StringBuilder();
        String label =  "Recently created members not in any groups  -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | Address | Apartment | Neighborhood | City |\n");
        postRaw.append("|---|---|---|---|---|\n");

        Tables tables = new Tables(users);
        for (User user : tables.memberOfNoGroups()) {
            postRaw.append('|');
            postRaw.append('@');
            postRaw.append(user.getUserName());
            postRaw.append('|');
            postRaw.append(user.getAddress());
            postRaw.append('|');
            postRaw.append(user.isApartment());
            postRaw.append('|');
            postRaw.append(user.getNeighborhood());
            postRaw.append('|');
            postRaw.append(user.getCity());
            postRaw.append("|\n");
        }

        HttpResponse<?> response = apiClient.updatePost(NON_CONSUMERS_POST_ID, postRaw.toString());
        System.out.println(response);
    }

    static void updateConsumerRequests(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException {

        String csvData = Files.readString(Paths.get(fileName));
        // FIX THIS, DS: constant for separator
        List<User> users = Parser.users(csvData, ",");

        StringBuilder postRaw = new StringBuilder();
        String label =  "Newly created members requesting meals -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | City | Address | Apartment | Phone |\n");
        postRaw.append("|---|---|---|---|---|\n");

        Tables tables = new Tables(users);
        for (User user : tables.sortByUserName()) {
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

        HttpResponse<?> response = apiClient.updatePost(CONSUMER_REQUESTS_POST_ID, postRaw.toString());
        System.out.println(response);
    }

    static void postConsumerRequests(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException {

        String csvData = Files.readString(Paths.get(fileName));
        // FIX THIS, DS: constant for separator
        List<User> users = Parser.users(csvData, ",");

        StringBuilder postRaw = new StringBuilder();
        String label =  "Newly created members requesting meals -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | City | Address | Apartment | Phone |\n");
        postRaw.append("|---|---|---|---|---|\n");

        Tables tables = new Tables(users);
        for (User user : tables.sortByUserName()) {
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

    static void updateVolunteerRequests(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException {

        String csvData = Files.readString(Paths.get(fileName));
        // FIX THIS, DS: constant for separator
        List<User> users = Parser.users(csvData, ",");

        StringBuilder postRaw = new StringBuilder();
        String label =  "New members requesting to volunteer -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("Placeholder to be updated by the back-end software\n\n");

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | Full Name | Phone | City |\n");
        postRaw.append("|---|---|---|---|---|\n");

//        Tables tables = new Tables(users);
//        for (User user : tables.memberOfNoGroups()) {
//            postRaw.append('|');
//            postRaw.append(user.getUserName());
//            postRaw.append('|');
//            postRaw.append(user.getAddress());
//            postRaw.append('|');
//            postRaw.append(user.isApartment());
//            postRaw.append('|');
//            postRaw.append(user.getNeighborhood());
//            postRaw.append('|');
//            postRaw.append(user.getCity());
//            postRaw.append("|\n");
//        }

        HttpResponse<?> response = apiClient.updatePost(VOLUNTEER_REQUESTS_POST_ID, postRaw.toString());
        System.out.println(response);
    }

    static void postVolunteerRequests(ApiClient apiClient, final String fileName)
            throws IOException, InterruptedException {

        String csvData = Files.readString(Paths.get(fileName));
        // FIX THIS, DS: constant for separator
        List<User> users = Parser.users(csvData, ",");

        StringBuilder postRaw = new StringBuilder();
        String label =  "New members requesting to volunteer -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        postRaw.append("| User Name | Full Name | Phone | City | Volunteer Request |\n");
        postRaw.append("|---|---|---|---|---|---|\n");

        Tables tables = new Tables(users);
        for (User user : tables.volunteerRequests()) {
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

    static void postDrivers(ApiClient apiClient, final String fileName, final String shortUrl)
            throws IOException, InterruptedException {

        StringBuilder postRaw = new StringBuilder();

        postRaw.append("** ");
        postRaw.append("Volunteer Drivers -- ");
        postRaw.append(ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss")));
        postRaw.append(" **\n\n");

        // postRaw.append("[" + fileName + "|attachment](upload://" + fileName + ") (5.49 KB)");
        postRaw.append("[" + fileName + "|attachment](" + shortUrl + ")");

        Post post = new Post();
        post.title = "Volunteer Drivers";
        post.topic_id = DRIVERS_POST_TOPIC;
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        post.raw = postRaw.toString();
        HttpResponse<?> response = apiClient.post(post.toJson());
        System.out.println(response);
    }

    static void updateDrivers(ApiClient apiClient, final String fileName, final String shortUrl)
            throws IOException, InterruptedException {

        String csvData = Files.readString(Paths.get(fileName));
        // FIX THIS, DS: constant for separator
        List<User> users = Parser.users(csvData, ",");

        StringBuilder postRaw = new StringBuilder();
        String label =  "Volunteer Drivers -- " + ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss"));

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append("**\n\n");

        // postRaw.append("[" + fileName + "|attachment](upload://" + fileName + ") (5.49 KB)");
        postRaw.append("[" + fileName + "|attachment](" + shortUrl + ")");

        HttpResponse<?> response = apiClient.updatePost(DRIVERS_POST_ID, postRaw.toString());
        System.out.println(response);
    }

    static void usersToJson(ApiClient apiClient) throws IOException, InterruptedException, ApiException {
        Loader loader = new Loader(apiClient);
        Exporter exporter = new Exporter(loader.load());
        System.out.println(exporter.jsonString());
    }

    static void usersToFile(ApiClient apiClient) throws IOException, InterruptedException, ApiException {
        Loader loader = new Loader(apiClient);
        Exporter exporter = new Exporter(loader.load());
        exporter.jsonToFile("users.json");
    }

    static void getPostTest(ApiClient apiClient) throws IOException, InterruptedException {

        HttpResponse<String> response = apiClient.getPost(239);
        System.out.println(response.body());
    }

    static void getCategories(ApiClient apiClient) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.getCategories();
        System.out.println(JsonWriter.formatJson(response.body()));
    }


    static void getUserFields(ApiClient apiClient) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.getUserFields();
        System.out.println(JsonWriter.formatJson(response.body()));
    }

    static void latestPosts(ApiClient apiClient) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.getLatestPosts();

        System.out.println(JsonWriter.formatJson(response.body()));
    }

    static void latestTopics(ApiClient apiClient) throws IOException, InterruptedException {
        HttpResponse<String> response = apiClient.getLatestTopics();

        System.out.println(JsonWriter.formatJson(response.body()));
    }

//
//    static void postTables(ApiClient apiClient) throws IOException, InterruptedException {
//        List<User> users = getUsers(apiClient);
//        Tables tables = new Tables(users);
//
//        postTable(apiClient, "Sorted by Name", tables.sortByName());
//        postTable(apiClient, "Sorted by User Name", tables.sortByUserName());
//        postTable(apiClient, "Sorted by Phone", tables.sortByPhoneNumber());
//        postTable(apiClient, "Sorted by Neighborhood/Name", tables.sortByNeighborHoodThenName());
//    }
//    static void postCSVTables(ApiClient apiClient) throws IOException, InterruptedException {
//        List<User> users = getUsers(apiClient);
//        Tables tables = new Tables(users);
//
//        postCSV(apiClient, "Sorted by Name", tables.sortByName());
//        postCSV(apiClient, "Sorted by Phone", tables.sortByPhoneNumber());
//        postCSV(apiClient, "Sorted by Neighborhood/Name", tables.sortByNeighborHoodThenName());
//    }


    static void uploadTest(ApiClient apiClient) throws IOException, InterruptedException {

        apiClient.uploadFile();

    }

    static void postWithLinkTest(ApiClient apiClient) throws IOException, InterruptedException {

        StringBuilder postRaw = new StringBuilder();

        postRaw.append("**Testing Post via API with embedded link**");
        postRaw.append(" -- ");

        postRaw.append("[member-data-200405-1850.csv|attachment](upload://5F02xqSRuzJfqKTCFkSTdZLEiUD.csv) (5.49 KB)");

        Post post = new Post();
        post.title = "test upload";
        post.topic_id = 322;
        post.raw = postRaw.toString();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        System.out.println(response);
    }

    static void postWithMemberLinkTest(ApiClient apiClient) throws IOException, InterruptedException {

        StringBuilder postRaw = new StringBuilder();

        postRaw.append("**Testing Post via API with member link**\n");
        postRaw.append("@stone");


        Post post = new Post();
        post.title = "test post";
        post.topic_id = 422;
        post.raw = postRaw.toString();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        System.out.println(response);
    }
}

