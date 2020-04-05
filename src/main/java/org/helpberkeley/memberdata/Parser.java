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
import com.cedarsoftware.util.io.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    static ApiQueryResult parseQueryResult(final String queryResultJson) throws ApiException {
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        Object obj = JsonReader.jsonToJava(queryResultJson, options);
        Map<String, Object> map = (Map<String, Object>)obj;

        Object[] columns = (Object[])map.get("columns");
        Object[] rows = (Object[])map.get("rows");

        return new ApiQueryResult(columns, rows);
    }

    static List<User> users(final Map<String, Group> groups, final ApiQueryResult queryResult) {
        LOGGER.debug("users");

        List<User> users = new ArrayList<>();

        assert queryResult.headers.length == 7 :
                "Unexpected number of columns for users query result: " + queryResult;
        assert queryResult.headers[0].equals(Constants.COLUMN_USER_ID) : queryResult.headers[0];
        assert queryResult.headers[1].equals(Constants.COLUMN_USERNAME) : queryResult.headers[1];
        assert queryResult.headers[2].equals(Constants.COLUMN_NAME) : queryResult.headers[2];
        assert queryResult.headers[3].equals(Constants.COLUMN_ADDRESS) : queryResult.headers[3];
        assert queryResult.headers[4].equals(Constants.COLUMN_PHONE) : queryResult.headers[4];
        assert queryResult.headers[5].equals(Constants.COLUMN_NEIGHBORHOOD) : queryResult.headers[5];
        assert queryResult.headers[6].equals(Constants.COLUMN_CITY) : queryResult.headers[6];

        Group consumers = groups.get(Constants.GROUP_CONSUMERS);
        Group drivers = groups.get(Constants.GROUP_DRIVERS);
        Group dispatchers = groups.get(Constants.GROUP_DISPATCHERS);
        List<String> groupMemberships = new ArrayList<>();

        for (Object rowObj : queryResult.rows) {

            Object[] columns = (Object[])rowObj;

            long userId = (Long)columns[0];

            if (skipUserId(userId)) {
                continue;
            }

            String userName = (String)columns[1];
            String name = (String)columns[2];
            String address = (String)columns[3];
            String phone = (String)columns[4];
            String neighborhood = (String)columns[5];
            String city = (String)columns[6];

            groupMemberships.clear();
            if (consumers.hasUserId(userId)) {
                groupMemberships.add(consumers.name);
            }
            if (drivers.hasUserId(userId)) {
                groupMemberships.add(drivers.name);
            }
            if (dispatchers.hasUserId(userId)) {
                groupMemberships.add(dispatchers.name);
            }

            try {
                users.add(User.createUser(name, userName, userId, address, city, phone, neighborhood, groupMemberships));
            } catch (UserException ex) {
                // FIX THIS, DS: get rid of UserException?
                users.add(ex.user);
            }
        }

        return users;
    }

    static Map<Long, String> groupNames(ApiQueryResult queryResult) {
        LOGGER.debug("groupNames");

        Map<Long, String> results = new HashMap<>();

        assert queryResult.headers.length == 2 :
                "Unexpected number of columns for groupNames query result: " + queryResult;
        assert queryResult.headers[0].equals(Constants.COLUMN_ID) : queryResult.headers[0];
        assert queryResult.headers[1].equals(Constants.COLUMN_NAME) : queryResult.headers[1];

        for (Object rowObj :  queryResult.rows) {
            Object[] columns = (Object[])rowObj;

            Long groupId = (Long)columns[0];
            String groupName = (String)columns[1];

            if ((groupId == null) || (groupName == null)) {
                LOGGER.debug("getGroupNames skipping id: {}, groupName: {}", groupId, groupName);
                continue;
            }
            results.put((Long)columns[0], (String)columns[1]);
        }

        return results;
    }

    static Map<String, List<Long>> groupUsers(final Map<Long, String> groupNames, final ApiQueryResult queryResult) {
        LOGGER.debug("groupUsers");
        Map<String, List<Long>> results = new HashMap<>();

        assert queryResult.headers.length == 2 :
                "Unexpected number of columns for groupUsers query result: " + queryResult;
        assert queryResult.headers[0].equals(Constants.COLUMN_GROUP_ID) : queryResult.headers[0];
        assert queryResult.headers[1].equals(Constants.COLUMN_USER_ID) : queryResult.headers[1];

        for (Object rowObj : queryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 2 : columns.length;

            Long groupId = (Long)columns[0];
            Long userId = (Long)columns[1];

            if ((groupId == null) || (userId == null)){
                LOGGER.debug("groupUsers: skipping null data: {}:{}", groupId, userId);
                continue;
            }

            String groupName = groupNames.get(groupId);
            assert groupName != null : "No group name found for group id " + groupId;

            List<Long> userIds = results.computeIfAbsent(groupName, v -> new ArrayList<>());
            userIds.add(userId);
        }

        return results;
    }

    // Skip Discourse system users. Not fully formed.
    private static boolean skipUserId(long userId) {
        return (userId == -1) || (userId == -2);
    }

    static void prettyPrint(final String pageJson) {
        System.out.println(JsonWriter.formatJson(pageJson));
    }
}
