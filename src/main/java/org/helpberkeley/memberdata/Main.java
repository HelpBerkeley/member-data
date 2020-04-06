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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class Main {

    static final String MEMBERDATA_PROPERTIES = "memberdata.properties";
    static final String HISTORY_PROPERTIES = "history.properties";
    static final String API_USER_PROPERTY = "Api-Username";
    static final String API_KEY_PROPERTY = "Api-Key";
    static final String MEMBER_DATA_HASHCODE_PROPERTY = "MemberData-HashCode";

    // FIX THIS, DS: make this less fragile
    static final long MEMBER_DATA_FOR_DISPATCHES_TOPID_ID = 86;
    static final long MEMBER_DATA_REQUIRING_ATTENTION_TOPIC_ID = 129;

    public static void main(String[] args) throws IOException, InterruptedException, ApiException {

        // Load member data properties
        Properties memberDataProperties = loadProperties(MEMBERDATA_PROPERTIES);

        // Load history
//        Properties history = loadProperties(HISTORY_PROPERTIES);

        // Set up an HTTP client
        ApiClient apiClient = new ApiClient(memberDataProperties);

//        postWithLinkTest(apiClient);
//        System.exit(0);
//        uploadTest(apiClient);

        // Create a User loader
        Loader loader = new Loader(apiClient);

        // Load the member data from the website
        List<User> users = loader.load();

        // Export the data to a CVS file
        Exporter exporter = new Exporter(new Tables(users).sortByUserName());
        exporter.csvToFile(exporter.generateFileName("member-data.csv"));

        // If there are any problems with the user data,
        // post them to the problems topic
        //
        // FIX THIS, DS: reenable this automated posting when there is
        //               support for checking the previous post
        //               for differences.
        postUserExceptions(apiClient, users);

        // Post subset of data needed by dispatchers
        // to make decison for promote-ability of someone to consumer.
//        postNonConsumersTable(apiClient, users);

        // Post the member data to the member data for drivers topic
        postMemberData(apiClient, users);

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
            System.out.println("Required propeteries file " + fileName + " cannot be found");
            System.exit(1);
        }

        Properties properties = new Properties();

        try (InputStream is = propertiesFile.openStream())
        {
            properties.load(is);
        } finally {}

        return properties;
    }

    static void postMemberData(ApiClient apiClient, List<User> users) throws IOException, InterruptedException {
        Tables tables = new Tables(users);
        postTable(apiClient, "Sorted by user name", tables.sortByUserName());
    }

    static void postTable(ApiClient apiClient, String label, List<User> users) throws IOException, InterruptedException {

        StringBuilder postRaw = new StringBuilder();

        postRaw.append("**");
        postRaw.append(label);
        postRaw.append(" -- ");
        postRaw.append(ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss")));
        postRaw.append("**\n\n");

        postRaw.append("| Name | User Name | Phone # | Neighborhood | City | Address | Consumer | Dispatcher | Driver |\n");
        postRaw.append("|---|---|---|---|---|---|---|---|---|\n");

        for (User user : users) {
            postRaw.append('|');
            postRaw.append(user.getName());
            postRaw.append('|');
            postRaw.append(user.getUserName());
            postRaw.append('|');
            postRaw.append(user.getPhoneNumber());
            postRaw.append('|');
            postRaw.append(user.getNeighborhood());
            postRaw.append('|');
            postRaw.append(user.getCity());
            postRaw.append('|');
            postRaw.append(user.getAddress());
            postRaw.append('|');
            postRaw.append(user.isConsumer());
            postRaw.append('|');
            postRaw.append(user.isDispatcher());
            postRaw.append('|');
            postRaw.append(user.isDriver());
            postRaw.append("|\n");
        }

        Post post = new Post();
        post.title = label;
        post.topic_id = MEMBER_DATA_FOR_DISPATCHES_TOPID_ID;
        post.raw = postRaw.toString();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        System.out.println(response);
    }

    static void postUserExceptions(ApiClient apiClient, List<User> users) {


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

        for (User user : users) {
            for (String error : user.getDataErrors()) {
                postRaw.append("User: ");
                postRaw.append(user.getUserName());
                postRaw.append(", Name: ");
                postRaw.append(user.getName());
                postRaw.append(": ");
                postRaw.append(error);
                postRaw.append('\n');
            }
        }

        System.out.println(postRaw.toString());

        // FIX THIS, DS: reenable
//        post.raw = postRaw.toString();
//        HttpResponse<?> response = apiClient.post(post.toJson());
//        System.out.println(response);
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
}

