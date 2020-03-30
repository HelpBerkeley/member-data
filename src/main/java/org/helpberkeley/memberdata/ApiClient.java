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

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class ApiClient {

    private static final String BASE_URL = "https://www.helpberkeley.org/";
    private static final String ADMIN_BASE = BASE_URL +  "admin/";
    private static final String ACTIVE_USERS_ENDPOINT = ADMIN_BASE + "/users/list/active.json";
    private static final String USER_ENDPOINT_BASE = ADMIN_BASE + "users/";
    private static final String LATEST_POSTS_ENDPOINT = BASE_URL + "posts.json";
    private static final String POSTS_ENDPOINT = BASE_URL + "posts.json";
    private static final String POSTS_BASE = BASE_URL + "posts/";
    private static final String USER_FIELDS_ENDPOINT = ADMIN_BASE + "customize/user_fields.json";
    private static final String CATEGORIES_ENDPOINT = BASE_URL + "categories.json";
    private static final String GROUPS_ENDPOINT = BASE_URL + "groups.json";
    private static final String GROUP_ENDPOINT_BASE = BASE_URL + "groups/";
    private static final String LATEST_TOPICS_ENDPOINT = BASE_URL + "latest.json";
    private static final String UPLOADS_ENDPOINT = BASE_URL + "uploads.json";

    private final String apiUser;
    private final String apiKey;
    private final HttpClient client;

    ApiClient(final Properties properties) {

        apiUser = properties.getProperty(Main.API_USER_PROPERTY);
        apiKey = properties.getProperty(Main.API_KEY_PROPERTY);

        if ((apiUser == null) || (apiKey == null)) {
            System.out.println("Missing " + Main.API_USER_PROPERTY + " property or "
                    + Main.API_KEY_PROPERTY + " property, or both");
            System.exit(1);
        }

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(apiUser, apiKey.toCharArray());
            }
        };

        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .authenticator(authenticator)
                .build();
    }

    HttpResponse<String> get(final String endpoint) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> getActiveUsers() throws IOException, InterruptedException {
        return get(ACTIVE_USERS_ENDPOINT);
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

    HttpResponse<String> getPost(long postId) throws IOException, InterruptedException {
        String endpoint = POSTS_BASE + postId + ".json";
        return get(endpoint);
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

        return response;
    }

    HttpResponse<String> uploadFile(Upload upload) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://helpberkeley.org/uploads.json"))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(upload.generateBody()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response;

    }

    HttpResponse<String> updatePost(long topicId, long postId, final String oldBody, final String body) throws IOException, InterruptedException {

//        String endpoint =  POSTS_BASE + postId + ".json";
        String endpoint =  POSTS_BASE + postId;

        PostUpdate postUpdate = new PostUpdate(topicId, body, oldBody, "", body);
//        PostUpdate postUpdate = new PostUpdate(body, oldBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(postUpdate.toJson()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response;
    }
}
