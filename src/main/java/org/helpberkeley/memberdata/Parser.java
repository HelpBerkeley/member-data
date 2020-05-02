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

import com.cedarsoftware.util.io.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    static ApiQueryResult parseQueryResult(final String queryResultJson) {
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        Object obj = JsonReader.jsonToJava(queryResultJson, options);
        Map<String, Object> map = (Map<String, Object>)obj;

        Object[] columns = (Object[])map.get("columns");
        Object[] rows = (Object[])map.get("rows");

        return new ApiQueryResult(columns, rows);
    }

    static List<User> users(final Map<String, Group> groups, final ApiQueryResult queryResult) {

        LOGGER.trace("users");

        List<User> users = new ArrayList<>();

        assert queryResult.headers.length == 12 :
                "Unexpected number of columns for users query result: " + queryResult;
        assert queryResult.headers[0].equals(Constants.COLUMN_USER_ID) : queryResult.headers[0];
        assert queryResult.headers[1].equals(Constants.COLUMN_USERNAME) : queryResult.headers[1];
        assert queryResult.headers[2].equals(Constants.COLUMN_NAME) : queryResult.headers[2];
        assert queryResult.headers[3].equals(Constants.COLUMN_ADDRESS) : queryResult.headers[3];
        assert queryResult.headers[4].equals(Constants.COLUMN_PHONE) : queryResult.headers[4];
        assert queryResult.headers[5].equals(Constants.COLUMN_NEIGHBORHOOD) : queryResult.headers[5];
        assert queryResult.headers[6].equals(Constants.COLUMN_CITY) : queryResult.headers[6];
        assert queryResult.headers[7].equals(Constants.COLUMN_APARTMENT) : queryResult.headers[7];
        assert queryResult.headers[8].equals(Constants.COLUMN_CONSUMER_REQUEST) : queryResult.headers[8];
        assert queryResult.headers[9].equals(Constants.COLUMN_VOLUNTEER_REQUEST) : queryResult.headers[9];
        assert queryResult.headers[10].equals(Constants.COLUMN_ALT_PHONE) : queryResult.headers[10];
        assert queryResult.headers[11].equals(Constants.COLUMN_CREATE_TIME) : queryResult.headers[11];

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
            Boolean isApartment = Boolean.valueOf((String)columns[7]);
            Boolean hasConsumerRequest = Boolean.valueOf((String)columns[8]);
            String volunteerRequest = (String)columns[9];
            String altPhone = (String)columns[10];
            String createdAt = (String)columns[11];

            groupMemberships.clear();

            for (Group group : groups.values()) {
                if (group.hasUserId(userId)) {
                    groupMemberships.add(group.name);
                }
            }

            try {
                users.add(User.createUser(name, userName, userId, address, city, phone, altPhone, neighborhood,
                        createdAt, isApartment, hasConsumerRequest, volunteerRequest, groupMemberships));
            } catch (UserException ex) {
                // FIX THIS, DS: get rid of UserException?
                users.add(ex.user);
            }
        }

        return users;
    }

    static Map<Long, String> groupNames(ApiQueryResult queryResult) {
        LOGGER.trace("groupNames");

        Map<Long, String> results = new HashMap<>();

        assert queryResult.headers.length == 2 :
                "Unexpected number of columns for groupNames query result: " + queryResult;
        assert queryResult.headers[0].equals(Constants.COLUMN_ID) : queryResult.headers[0];
        assert queryResult.headers[1].equals(Constants.COLUMN_NAME) : queryResult.headers[1];

        for (Object rowObj :  queryResult.rows) {
            Object[] columns = (Object[])rowObj;

            Long groupId = (Long)columns[0];
            String groupName = (String)columns[1];

            // FIX THIS, DS: was this just a hack for 429 errors?
            if ((groupId == null) || (groupName == null)) {
                LOGGER.trace("getGroupNames skipping id: {}, groupName: {}", groupId, groupName);
                continue;
            }
            results.put(groupId, groupName);
        }

        return results;
    }

    static Map<String, List<Long>> groupUsers(final Map<Long, String> groupNames, final ApiQueryResult queryResult) {
        LOGGER.trace("groupUsers");
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

            // FIX THIS, DS: was this just a hack for 429 errors?
            if ((groupId == null) || (userId == null)){
                LOGGER.trace("groupUsers: skipping null data: {}:{}", groupId, userId);
                continue;
            }

            String groupName = groupNames.get(groupId);
            assert groupName != null : "No group name found for group id " + groupId;

            List<Long> userIds = results.computeIfAbsent(groupName, v -> new ArrayList<>());
            userIds.add(userId);
        }

        return results;
    }

    static List<User> users(final String csvData, final String separator) {

        String[] lines = csvData.split("\n");
        assert lines.length > 0 : csvData;

        String[] headers = lines[0].split(separator);
        assert headers.length == 24 : headers.length + ": " + lines[0];

        assert headers[0].equals(User.ID_COLUMN) : headers[0];
        assert headers[1].equals(User.NAME_COLUMN) : headers[1];
        assert headers[2].equals(User.USERNAME_COLUMN) : headers[2];
        assert headers[3].equals(User.PHONE_NUMBER_COLUMN) : headers[3];
        assert headers[4].equals(User.ALT_PHONE_NUMBER_COLUMN) : headers[4];
        assert headers[5].equals(User.NEIGHBORHOOD_COLUMN) : headers[5];
        assert headers[6].equals(User.CITY_COLUMN) : headers[6];
        assert headers[7].equals(User.ADDRESS_COLUMN) : headers[7];
        assert headers[8].equals(User.CONSUMER_COLUMN) : headers[8];
        assert headers[9].equals(User.DISPATCHER_COLUMN) : headers[9];
        assert headers[10].equals(User.DRIVER_COLUMN) : headers[10];
        assert headers[11].equals(User.CREATED_AT_COLUMN) : headers[11];
        assert headers[12].equals(User.APARTMENT_COLUMN) : headers[12];
        assert headers[13].equals(User.CONSUMER_REQUEST_COLUMN) : headers[13];
        assert headers[14].equals(User.VOLUNTEER_REQUEST_COLUMN) : headers[14];
        assert headers[15].equals(User.SPECIALIST_COLUMN) : headers[15];
        assert headers[16].equals(User.BHS_COLUMN) : headers[16];
        assert headers[17].equals(User.HELPLINE_COLUMN) : headers[17];
        assert headers[18].equals(User.SITELINE_COLUMN) : headers[18];
        assert headers[19].equals(User.INREACH_COLUMN) : headers[19];
        assert headers[20].equals(User.OUTREACH_COLUMN) : headers[20];
        assert headers[21].equals(User.MARKETING_COLUMN) : headers[21];
        assert headers[22].equals(User.MODERATORS_COLUMN) : headers[22];
        assert headers[23].equals(User.WORKFLOW_COLUMN) : headers[23];

        List<User> users = new ArrayList<>();
        List<String> groups = new ArrayList<>();

        for (int index = 1; index < lines.length; index++) {
            String[] columns = lines[index].split(separator);
            assert columns.length == headers.length : columns.length + " != " + headers.length;

            long id = Long.parseLong(columns[0]);
            String name = columns[1];
            String userName = columns[2];
            String phone = columns[3];
            String altPhone = columns[4];
            String neighborhood = columns[5];
            String city = columns[6];
            String address = columns[7];

            groups.clear();
            if (Boolean.parseBoolean(columns[8])) {
                groups.add(Constants.GROUP_CONSUMERS);
            }
            if (Boolean.parseBoolean(columns[9])) {
                groups.add(Constants.GROUP_DISPATCHERS);
            }
            if (Boolean.parseBoolean(columns[10])) {
                groups.add(Constants.GROUP_DRIVERS);
            }

            String createdAt = columns[11];
            Boolean isApartment = Boolean.valueOf(columns[12]);
            Boolean hasConsumerRequest = Boolean.valueOf(columns[13]);
            String volunteerRequest = columns[14];

            if (Boolean.parseBoolean(columns[15])) {
                groups.add(Constants.GROUP_SPECIALISTS);
            }

            if (Boolean.parseBoolean(columns[16])) {
                groups.add(Constants.GROUP_BHS);
            }

            if (Boolean.parseBoolean(columns[17])) {
                groups.add(Constants.GROUP_HELPLINE);
            }

            if (Boolean.parseBoolean(columns[18])) {
                groups.add(Constants.GROUP_SITELINE);
            }

            if (Boolean.parseBoolean(columns[19])) {
                groups.add(Constants.GROUP_INREACH);
            }

            if (Boolean.parseBoolean(columns[20])) {
                groups.add(Constants.GROUP_OUTREACH);
            }

            if (Boolean.parseBoolean(columns[21])) {
                groups.add(Constants.GROUP_MARKETING);
            }

            if (Boolean.parseBoolean(columns[22])) {
                groups.add(Constants.GROUP_MODERATORS);
            }

            if (Boolean.parseBoolean(columns[23])) {
                groups.add(Constants.GROUP_WORKFLOW);
            }

            try {
                users.add(User.createUser(name, userName, id, address, city, phone, altPhone, neighborhood,
                        createdAt, isApartment, hasConsumerRequest, volunteerRequest, groups));
            } catch (UserException ex) {
                users.add(ex.user);
            }
        }

        return users;
    }

    static List<DeliveryData> dailyDeliveries(ApiQueryResult apiQueryResult) {
        assert apiQueryResult.headers.length == 1 : apiQueryResult.headers.length;
        assert apiQueryResult.headers[0].equals("raw");

        List<DeliveryData> dailyDeliveries = new ArrayList<>();

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 1 : columns.length;

            //
            String raw = ((String)columns[0]).trim();

            if (raw.startsWith("Here we post the completed daily spreadsheets")) {
                continue;
            }

            // 2020/03/28
            //
            //[HelpBerkeleyDeliveries - 3_28.csv|attachment](upload://xyzzy.csv) (828 Bytes)

            int index = raw.indexOf('\n');
            String date = raw.substring(0, index);
            index = raw.indexOf("upload://");
            assert index != -1 : raw;
            String shortURL = raw.substring(index);
            index = shortURL.indexOf(')');
            shortURL = shortURL.substring(0, index);

            dailyDeliveries.add(new DeliveryData(date, shortURL, raw));
        }

        return dailyDeliveries;
    }

    // Skip Discourse system users. Not fully formed.
    private static boolean skipUserId(long userId) {
        return (userId == -1) || (userId == -2);
    }

//    static void prettyPrint(final String pageJson) {
//        System.out.println(JsonWriter.formatJson(pageJson));
//    }
}
