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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load User and group data from the site.
 */
public class Loader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Loader.class);

    private final ApiClient apiClient;
    private final Map<String, Group> groups = new HashMap<>();
    private final Map<String, String> emailAddresses = new HashMap<>();

    // FIX THIS, DS: refactor these two ctors and the load methods
    public Loader(final ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Return a list of Users fetched from the website.
     *
     * @return User list.
     * @throws IOException          Website interaction exception.
     * @throws InterruptedException Website interaction exception.
     */
    public List<User> load() throws IOException, InterruptedException {
        LOGGER.trace("load");
        loadGroups();
        loadEmailAddresses();
        return loadUsers();
    }

    private void loadGroups() throws IOException, InterruptedException {
        LOGGER.trace("loadGroups");

        assert apiClient != null;
        String json = apiClient.runQuery(Constants.QUERY_GET_GROUPS_ID);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);
        Map<Long, String> groupNames = Parser.groupNames(apiQueryResult);

        json = apiClient.runQuery(Constants.QUERY_GET_GROUP_USERS_ID);
        apiQueryResult = Parser.parseQueryResult(json);
        Map<String, List<Long>> groupUsers = Parser.groupUsers(groupNames, apiQueryResult);

        for (Map.Entry<String, List<Long>> entry : groupUsers.entrySet()) {

            switch (entry.getKey()) {
                case Constants.GROUP_DRIVERS:
                case Constants.GROUP_DISPATCHERS:
                case Constants.GROUP_CONSUMERS:
                case Constants.GROUP_SPECIALISTS:
                    groups.put(entry.getKey(), Group.createGroup(entry.getKey(), entry.getValue()));
            }
        }
    }

    private void loadEmailAddresses() throws IOException, InterruptedException {
        LOGGER.trace("loadEmailAddresses");

        assert apiClient != null;
        String json = apiClient.runQuery(Constants.QUERY_GET_EMAIL_ADDRESSES);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);
        emailAddresses.putAll(Parser.emailAddresses(apiQueryResult));

    }

    private List<User> loadUsers() throws IOException, InterruptedException {
        LOGGER.trace("loadUsers");

        assert apiClient != null;
        String json = apiClient.runQuery(Constants.CURRENT_USERS_QUERY);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);
        return Parser.users(groups, emailAddresses, apiQueryResult);
    }
}
