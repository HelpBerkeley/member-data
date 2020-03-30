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

    static final String PROPERTIES_FILE = "memberdata.properties";
    static final String API_USER_PROPERTY = "Api-Username";
    static final String API_KEY_PROPERTY = "Api-Key";

    public static void main(String[] args) throws IOException, InterruptedException {

        // Load properties file
        Properties properties = loadProperties();

        // Set up an HTTP client
        ApiClient apiClient = new ApiClient(properties);

        // Create a User loader
        Loader loader = new Loader(apiClient);

        // Load the member data from the website
        List<User> users = loader.load();

        // If there are any problems with the user data,
        // post them to the problems topic
        if (! loader.getExceptions().isEmpty()) {
            postUserExceptions(apiClient, loader.getExceptions());
        }

        // Post the member data to the member data for drivers topic
        postMemberData(apiClient, users);

//        usersToJson(apiClient);
//        usersToFile(apiClient);
//        postTableSortedByUserName(apiClient);
//        printCSVSortedByUser(apiClient);
//        getPostTest(apiClient);
//        updateTest(apiClient);
//        latestPosts(apiClient);
//        postCSVTable(apiClient);
//        uploadFile(apiClient);

    }

    static Properties loadProperties() throws IOException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL propertiesFile = classLoader.getResource(PROPERTIES_FILE);

        if (propertiesFile == null) {
            System.out.println("Required propeteries file " + PROPERTIES_FILE + " cannot be found");
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
        post.topic_id = 86;
        post.raw = postRaw.toString();
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        HttpResponse<?> response = apiClient.post(post.toJson());
        System.out.println(response);
    }

    static void postUserExceptions(ApiClient apiClient, List<UserException> exceptions)
            throws IOException, InterruptedException {

        StringBuilder postRaw = new StringBuilder();


        postRaw.append("** ");
        postRaw.append(ZonedDateTime.now(
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss")));
        postRaw.append(" **\n\n");

        Post post = new Post();
        post.title = "Member data requiring attention";
        post.topic_id = 129;
        post.createdAt = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));

        for (UserException ex : exceptions) {
            User user = ex.user;

            for (String error : user.getDataErrors()) {
                postRaw.append("User: ");
                postRaw.append(user.getUserName());
                postRaw.append(": ");
                postRaw.append(error);
                postRaw.append('\n');
            }
        }
        post.raw = postRaw.toString();

        HttpResponse<?> response = apiClient.post(post.toJson());
        System.out.println(response);
    }

    static void printCSVSortedByUser(ApiClient apiClient) throws IOException, InterruptedException {
        Loader loader = new Loader(apiClient);
        List<User> users = loader.load();
        Tables tables = new Tables(users);
        String csv = generateCSV(tables.sortByUserName());
        System.out.println(csv);
    }

    static void usersToJson(ApiClient apiClient) throws IOException, InterruptedException {
        Loader loader = new Loader(apiClient);
        Exporter exporter = new Exporter(loader.load());
        System.out.println(exporter.jsonString());
    }

    static void usersToFile(ApiClient apiClient) throws IOException, InterruptedException {
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

    static void updateTest(ApiClient apiClient) throws IOException, InterruptedException {

        String oldBody = "a1b2c3d4e5";
        String newBody = "abcdefghij";

        HttpResponse<String> response = apiClient.updatePost(32, 239, oldBody, newBody);
        System.out.println(response);
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

    static String generateCSV(List<User> users) {

        String separator = ",";

        StringBuilder postRaw = new StringBuilder();

        postRaw.append("Name");
        postRaw.append(separator);
        postRaw.append("User Name");
        postRaw.append(separator);
        postRaw.append("Phone #");
        postRaw.append(separator);
        postRaw.append("Neighborhood");
        postRaw.append(separator);
        postRaw.append("Address");
        postRaw.append(separator);
        postRaw.append("City");
        postRaw.append(separator);
        postRaw.append("Consumer");
        postRaw.append(separator);
        postRaw.append("Dispatcher");
        postRaw.append(separator);
        postRaw.append("Driver");
        postRaw.append(separator);
        postRaw.append('\n');


        for (User user : users) {
            postRaw.append(user.getName());
            postRaw.append(separator);
            postRaw.append(user.getUserName());
            postRaw.append(separator);
            postRaw.append(user.getPhoneNumber());
            postRaw.append(separator);
            postRaw.append(user.getNeighborhood());
            postRaw.append(separator);
            postRaw.append(user.getAddress());
            postRaw.append(separator);
            postRaw.append(user.getCity());
            postRaw.append(separator);
            postRaw.append(user.isConsumer());
            postRaw.append(separator);
            postRaw.append(user.isDispatcher());
            postRaw.append(separator);
            postRaw.append(user.isDriver());
            postRaw.append(separator);
            postRaw.append('\n');
        }

        return postRaw.toString();
    }

    static void uploadFile(ApiClient apiClient) throws IOException, InterruptedException {
        Upload upload = new Upload("xyzzy.cvs", "a,b,c,\n1,2,3\n");

        HttpResponse<String> response = apiClient.uploadFile(upload);
        System.out.println(response);
    }
}

