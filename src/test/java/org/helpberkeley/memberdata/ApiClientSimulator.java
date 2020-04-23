/*
 * Copyright (c) 2020. helpberkeley.org
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ApiClientSimulator extends ApiClient {

    ApiClientSimulator(Properties properties) {
        super(properties);
    }

    @Override
    HttpResponse<String> post(String json) {
        return null;
    }

    @Override
    String runQuery(int queryId) throws ApiException {
        try {
            if (queryId == Constants.QUERY_GET_GROUPS_ID) {
                return getGroupsResult();
            } else if (queryId == Constants.QUERY_GET_GROUP_USERS_ID) {
                return getGroupUsersResult();
            } else if (queryId == Constants.CURRENT_USERS_QUERY) {
                return getUsersResult();
            } else if (queryId == Constants.QUERY_GET_EMAIL_ADDRESSES) {
                return getEmailAddressesResult();
            }
        } catch (IOException|URISyntaxException ex) {
            throw new ApiException("Exception running query " + queryId, ex);
        }

        throw new ApiException("Query " + queryId + " not supported by the simulator");
    }

    @Override
    HttpResponse<String> getUser(long userId) {
        return null;
    }

    @Override
    HttpResponse<String> getLatestPosts() {
        return null;
    }

    @Override
    HttpResponse<String> getCategories() {
        return null;
    }

    @Override
    HttpResponse<String> getGroups() {
        return null;
    }

    @Override
    HttpResponse<String> getGroup(String groupName) {
        return null;
    }

    @Override
    HttpResponse<String> getGroupMembers(String groupName) {
        return null;
    }

    @Override
    HttpResponse<String> getLatestTopics() {
        return null;
    }

    @Override
    HttpResponse<String> getUserFields() {
        return null;
    }

    @Override
    HttpResponse<String> getPost(long postId) {
        return null;
    }

    @Override
    HttpResponse<String> uploadFile() {
        return null;
    }

    @Override
    HttpResponse<String> updatePost(long postId, String body) {
        return null;
    }

    private String getGroupsResult() throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource("groups.json");
        return Files.readString(Paths.get(url.toURI()));
    }

    private String getGroupUsersResult() throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource("group-users.json");
        return Files.readString(Paths.get(url.toURI()));
    }

    private String getUsersResult() throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource("users.json");
        return Files.readString(Paths.get(url.toURI()));
    }

    private String getEmailAddressesResult() throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource("email.json");
        return Files.readString(Paths.get(url.toURI()));
    }
}
