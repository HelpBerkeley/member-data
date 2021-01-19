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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Load User and group data from the site.
 */
public class Loader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Loader.class);

    private final ApiClient apiClient;
    private final Map<String, Group> groups = new HashMap<>();
    private Set<Long> emailConfirmations;

    // FIX THIS, DS: refactor these two ctors and the load methods
    public Loader(final ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Return a list of Users fetched from the website.
     *
     * @return User list.
     */
    public List<User> load() {
        LOGGER.trace("load");
        loadGroups();
        loadEmailConfirmations();
        return loadUsers();
    }

    Map<Long, String> loadEmailAddresses() {
        assert apiClient != null;
        String json = apiClient.runQuery(Constants.QUERY_GET_EMAILS);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        return HBParser.emailAddresses(apiQueryResult);
    }

    private void loadGroups() {

        assert apiClient != null;
        String json = apiClient.runQuery(Constants.QUERY_GET_GROUPS_ID);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        Map<Long, String> groupNames = HBParser.groupNames(apiQueryResult);

        json = apiClient.runQuery(Constants.QUERY_GET_GROUP_USERS_ID);
        apiQueryResult = HBParser.parseQueryResult(json);
        Map<String, List<Long>> groupUsers = HBParser.groupUsers(groupNames, apiQueryResult);

        for (Map.Entry<String, List<Long>> entry : groupUsers.entrySet()) {

            switch (entry.getKey()) {
                case Constants.GROUP_CONSUMERS:
                case Constants.GROUP_DRIVERS:
                case Constants.GROUP_DISPATCHERS:
                case Constants.GROUP_SPECIALISTS:
                case Constants.GROUP_BHS:
                case Constants.GROUP_HELPLINE:
                case Constants.GROUP_SITELINE:
                case Constants.GROUP_TRAINED_CUSTOMER_CARE_A:
                case Constants.GROUP_TRAINED_CUSTOMER_CARE_B:
                case Constants.GROUP_INREACH:
                case Constants.GROUP_OUTREACH:
                case Constants.GROUP_MARKETING:
                case Constants.GROUP_MODERATORS:
                case Constants.GROUP_WORKFLOW:
                case Constants.GROUP_VOICEONLY:
                case Constants.GROUP_TRUST_LEVEL_4:
                case Constants.GROUP_CUSTOMER_INFO:
                case Constants.GROUP_ADVISOR:
                case Constants.GROUP_COORDINATORS:
                case Constants.GROUP_ADMIN:
                case Constants.GROUP_PACKERS:
                case Constants.GROUP_BOARDMEMBERS:
                case Constants.GROUP_LIMITED:
                case Constants.GROUP_AT_RISK:
                case Constants.GROUP_BIKERS:
                case Constants.GROUP_OUT:
                case Constants.GROUP_TRAINED_DRIVERS:
                case Constants.GROUP_EVENT_DRIVERS:
                case Constants.GROUP_GONE:
                case Constants.GROUP_OTHER_DRIVERS:
                    groups.put(entry.getKey(), Group.createGroup(entry.getKey(), entry.getValue()));
                    break;
                default:
                    break;
            }
        }
    }

    private void loadEmailConfirmations() {
        assert apiClient != null;
        String json = apiClient.runQuery(Constants.QUERY_EMAIL_CONFIRMATIONS);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        emailConfirmations = HBParser.emailConfirmations(apiQueryResult);
    }

    private List<User> loadUsers() {
        LOGGER.trace("loadUsers");

        assert apiClient != null;
        String json = apiClient.runQuery(Constants.CURRENT_USERS_QUERY);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        return HBParser.users(groups, emailConfirmations, apiQueryResult);
    }
}
