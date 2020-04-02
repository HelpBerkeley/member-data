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

import com.cedarsoftware.util.io.JsonReader;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Load User and group data from the site.
 */
public class Loader {

    private final ApiClient apiClient;
    private final List<Group> groups = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final List<UserException> userExceptions = new ArrayList<>();

    // FIX THIS, DS: refactor these two ctors and the load methods
    public Loader(final ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Loader() {
        this.apiClient = null;
    }

    public List<UserException> getExceptions() {
        return userExceptions;
    }

    public void clearExceptions() {
        userExceptions.clear();
    }


    public List<User> load(final String json) {
        Object obj = JsonReader.jsonToJava(json);
        return (List<User>)obj;
    }

    /**
     * Return a list of Users fetched from the website.
     *
     * @return User list.
     * @throws IOException          Website interaction exception.
     * @throws InterruptedException Website interaction exception.
     */
    public List<User> load() throws IOException, InterruptedException {
        loadGroups();
        loadUsers(groups);

        return users;
    }

    private void loadGroups() throws IOException, InterruptedException {

        // Get a list of group names
        HttpResponse<String> response = apiClient.getGroups();
        List<String> groupNames = Parser.groupNames(response.body());

        for (String groupName : groupNames) {

            // Get and create a group
            response = apiClient.getGroup(groupName);

            if (response.statusCode() != 200) {
                System.out.println("Error on getGroup " + groupName);
                continue;
            }
            Group group = Parser.group(response.body());

            // Get group members for that group
            response = apiClient.getGroupMembers(groupName);
            List<String> userNames = Parser.groupMembers(response.body());
            group.addMembers(userNames);

            groups.add(group);
        }
    }

    private void loadUsers(final List<Group> groups) throws IOException, InterruptedException {
        // There isn't an endpoint that gives us a list of active users with their profile data.
        // So, first fetch the active users page
        HttpResponse<String> response = apiClient.getActiveUsers();

        // Get the user IDs from it.
        List<Long> activeUserIds = Parser.activeUserIds(response.body());

        // Then fetch and build an object for each user
        for (long userId : activeUserIds) {

            if (skipUserId(userId)) {
                continue;
            }
            response = apiClient.getUser(userId);
            try {
                users.add(Parser.user(response.body(), groups));
            } catch (UserException ex) {
                userExceptions.add(ex);

                // FIX THIS, DS: rationilze when auditing is done
                if (ex.user != null) {
                    users.add(ex.user);
                }
            }
        }
    }

    // Skip Discourse system users. Not fully formed.
    private boolean skipUserId(long userId) {
        return (userId == -1) || (userId == -2);
    }
}
