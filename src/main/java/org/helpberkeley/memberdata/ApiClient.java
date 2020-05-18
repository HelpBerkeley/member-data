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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.Random;

import static java.net.HttpURLConnection.HTTP_OK;

public class ApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

    private static final String BASE_URL = "https://go.helpberkeley.org/";
    private static final String ADMIN_BASE = BASE_URL +  "admin/";
    private static final String USER_ENDPOINT_BASE = ADMIN_BASE + "users/";
    private static final String LATEST_POSTS_ENDPOINT = BASE_URL + "posts.json";
    private static final String POSTS_ENDPOINT = BASE_URL + "posts.json";
    static final String POSTS_BASE = BASE_URL + "posts/";
    private static final String USER_FIELDS_ENDPOINT = ADMIN_BASE + "customize/user_fields.json";
    private static final String CATEGORIES_ENDPOINT = BASE_URL + "categories.json";
    private static final String GROUPS_ENDPOINT = BASE_URL + "groups.json";
    private static final String GROUP_ENDPOINT_BASE = BASE_URL + "groups/";
    private static final String LATEST_TOPICS_ENDPOINT = BASE_URL + "latest.json";
    private static final String UPLOADS_ENDPOINT = BASE_URL + "uploads.json";
    private static final String DOWNLOAD_ENDPOINT = BASE_URL + "uploads/short-url/";
    static final String QUERY_BASE = BASE_URL + "admin/plugins/explorer/queries/";

    private final String apiUser;
    private final String apiKey;
    private final HttpClient client;

    static HttpClientFactory httpClientFactory = null;

    ApiClient(final Properties properties) {

        apiUser = properties.getProperty(Main.API_USER_PROPERTY);
        apiKey = properties.getProperty(Main.API_KEY_PROPERTY);

        if ((apiUser == null) || (apiKey == null)) {
            LOGGER.error("Missing {} property or {} property or both", Main.API_USER_PROPERTY, Main.API_KEY_PROPERTY);
            System.exit(1);
        }

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(apiUser, apiKey.toCharArray());
            }
        };

        if (httpClientFactory != null) {
            this.client = httpClientFactory.createClient();

        } else {
            this.client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .authenticator(authenticator)
                    .build();
        }
    }

    ApiClient(final Properties properties, HttpClient httpClient) {

        apiUser = properties.getProperty(Main.API_USER_PROPERTY);
        apiKey = properties.getProperty(Main.API_KEY_PROPERTY);

        if ((apiUser == null) || (apiKey == null)) {
            System.out.println("Missing " + Main.API_USER_PROPERTY + " property or "
                    + Main.API_KEY_PROPERTY + " property, or both");
            System.exit(1);
        }

        this.client = httpClient;
    }

    private HttpResponse<String> get(final String endpoint) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HTTP_OK) {
            // FIX THIS, DS: create a dedicated unchecked for this?
            throw new Error("post(" + POSTS_ENDPOINT + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    HttpResponse<String> post(final String json) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POSTS_ENDPOINT))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HTTP_OK) {
            // FIX THIS, DS: create a dedicated unchecked for this?
            throw new Error("post(" + POSTS_ENDPOINT + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    String runQuery(int queryId) throws IOException, InterruptedException {

        String endpoint = QUERY_BASE + queryId + "/run";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "multipart/form-data")
                .POST(HttpRequest.BodyPublishers.ofString("limit=1000000"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HTTP_OK) {
            // FIX THIS, DS: create a dedicated unchecked for this?
            throw new Error("runQuery(" + endpoint + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    HttpResponse<String> getUser(long userId) throws IOException, InterruptedException {
        String endpoint = USER_ENDPOINT_BASE + userId + ".json";
        return get(endpoint);
    }

    HttpResponse<String> getLatestPosts() throws IOException, InterruptedException {
        return get(LATEST_POSTS_ENDPOINT);
    }

    HttpResponse<String> getCategories() throws IOException, InterruptedException {
        return get(CATEGORIES_ENDPOINT);
    }

    HttpResponse<String> getGroups() throws IOException, InterruptedException {
        return get(GROUPS_ENDPOINT);
    }

    HttpResponse<String> getGroup(String groupName ) throws IOException, InterruptedException {
        String endpoint = GROUP_ENDPOINT_BASE + groupName + ".json";
        return get(endpoint);
    }

    HttpResponse<String> getGroupMembers(String groupName ) throws IOException, InterruptedException {
        String endpoint = GROUP_ENDPOINT_BASE + groupName + "/members.json";
        return get(endpoint);
    }

    HttpResponse<String> getLatestTopics() throws IOException, InterruptedException {
        return get(LATEST_TOPICS_ENDPOINT);
    }

    HttpResponse<String> getUserFields() throws IOException, InterruptedException {
        return get(USER_FIELDS_ENDPOINT);
    }

    String getPost(long postId) throws IOException, InterruptedException {
        String endpoint = POSTS_BASE + postId + ".json";
        // Normalize EOL
        return get(endpoint).body().replaceAll("\\r\\n?", "\n");
    }

    HttpResponse<String> uploadFile() throws IOException, InterruptedException {

        String boundary = new BigInteger(256, new Random()).toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPLOADS_ENDPOINT))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Synchronous", "1")
                .header("Content-Type", "multipart/form-data;boundary=" + boundary)
                .POST(Upload.uploadBody(boundary))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());

    }

    HttpResponse<String> updatePost(long postId, final String body) throws IOException, InterruptedException {

        String endpoint =  POSTS_BASE + postId;
        String postBody = "{ \"post\" : { \"raw\" : \"" + body + "\" } }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(postBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    String downloadFile(final String shortURLFileName) throws IOException, InterruptedException {

        String endpoint = DOWNLOAD_ENDPOINT + shortURLFileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .build();

        HttpResponse<String> response =  client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HTTP_OK) {
            // FIX THIS, DS: create a dedicated unchecked for this?
            throw new Error("downloadFile(" + endpoint + " failed: " + response.statusCode() + ": " + response.body());
        }

        // Normalize EOL
        return response.body().replaceAll("\\r\\n?", "\n");

    }
}
