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
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HBParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(HBParser.class);

    static ApiQueryResult parseQueryResult(final String queryResultJson) {
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        @SuppressWarnings("unchecked")
        Map<String, Object> map =
                (Map<String, Object>)JsonReader.jsonToJava(queryResultJson, options);
        Object[] columns = (Object[])map.get("columns");
        Object[] rows = (Object[])map.get("rows");

        return new ApiQueryResult(columns, rows);
    }

    static List<User> users(final Map<String, Group> groups,
        final Set<Long> emailVerified, final ApiQueryResult queryResult) {

        LOGGER.trace("users");

        List<User> users = new ArrayList<>();

        assert queryResult.headers.length == 14 :
                "Unexpected number of columns for users query result: " + queryResult;

        assert queryResult.headers[0].equals(Constants.COLUMN_USER_ID) : queryResult.headers[0];
        assert queryResult.headers[1].equals(Constants.COLUMN_USERNAME) : queryResult.headers[1];
        assert queryResult.headers[2].equals(Constants.COLUMN_NAME) : queryResult.headers[2];
        assert queryResult.headers[3].equals(Constants.COLUMN_STAGED) : queryResult.headers[3];
        assert queryResult.headers[4].equals(Constants.COLUMN_ADDRESS) : queryResult.headers[4];
        assert queryResult.headers[5].equals(Constants.COLUMN_PHONE) : queryResult.headers[5];
        assert queryResult.headers[6].equals(Constants.COLUMN_NEIGHBORHOOD) : queryResult.headers[6];
        assert queryResult.headers[7].equals(Constants.COLUMN_CITY) : queryResult.headers[7];
        assert queryResult.headers[8].equals(Constants.COLUMN_CONDO) : queryResult.headers[8];
        assert queryResult.headers[9].equals(Constants.COLUMN_CONSUMER_REQUEST) : queryResult.headers[9];
        assert queryResult.headers[10].equals(Constants.COLUMN_VOLUNTEER_REQUEST) : queryResult.headers[10];
        assert queryResult.headers[11].equals(Constants.COLUMN_ALT_PHONE) : queryResult.headers[11];
        assert queryResult.headers[12].equals(Constants.COLUMN_REFERRAL) : queryResult.headers[12];
        assert queryResult.headers[13].equals(Constants.COLUMN_CREATE_TIME) : queryResult.headers[13];

        List<String> groupMemberships = new ArrayList<>();

        for (Object rowObj : queryResult.rows) {

            Object[] columns = (Object[])rowObj;

            long userId = (Long)columns[0];

            if (skipUserId(userId) || (Boolean)columns[3]) {
                continue;
            }

            String userName = (String)columns[1];

            // FIX THIS, DS: need a reliable way to detect anonymized users
            if (userName.startsWith("anon")) {
                continue;
            }
            String name = (String)columns[2];
            String address = (String)columns[4];
            String phone = (String)columns[5];
            String neighborhood = (String)columns[6];
            String city = (String)columns[7];
            Boolean isCondo = Boolean.valueOf((String)columns[8]);
            Boolean hasConsumerRequest = Boolean.valueOf((String)columns[9]);
            String volunteerRequest = (String)columns[10];
            String altPhone = (String)columns[11];
            String referral = (String)columns[12];
            String createdAt = (String)columns[13];

            groupMemberships.clear();

            for (Group group : groups.values()) {
                if (group.hasUserId(userId)) {
                    groupMemberships.add(group.name);
                }
            }

            Boolean verified = emailVerified.contains(userId);

            try {
                users.add(User.createUser(name, userName, userId, address, city, phone, altPhone, neighborhood,
                        createdAt, isCondo, hasConsumerRequest, volunteerRequest, referral, verified,
                        groupMemberships));
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

    // From raw form
    static List<User> users(final String csvData) throws IOException, CsvException {

        CSVReader cvsReader = new CSVReader(new StringReader(csvData));
        List<String[]> lines = cvsReader.readAll();
        assert ! lines.isEmpty();
        String[] headers = lines.get(0);

        assert headers.length == 33 : headers.length;

        int index = 0;
        assert headers[index].equals(User.ID_COLUMN) : headers[index];
        assert headers[++index].equals(User.NAME_COLUMN) : headers[index];
        assert headers[++index].equals(User.USERNAME_COLUMN) : headers[index];
        assert headers[++index].equals(User.PHONE_NUMBER_COLUMN) : headers[index];
        assert headers[++index].equals(User.ALT_PHONE_NUMBER_COLUMN) : headers[index];
        assert headers[++index].equals(User.NEIGHBORHOOD_COLUMN) : headers[index];
        assert headers[++index].equals(User.CITY_COLUMN) : headers[index];
        assert headers[++index].equals(User.ADDRESS_COLUMN) : headers[index];
        assert headers[++index].equals(User.CONSUMER_COLUMN) : headers[index];
        assert headers[++index].equals(User.VOICEONLY_COLUMN) : headers[index];
        assert headers[++index].equals(User.DISPATCHER_COLUMN) : headers[index];
        assert headers[++index].equals(User.DRIVER_COLUMN) : headers[index];
        assert headers[++index].equals(User.CREATED_AT_COLUMN) : headers[index];
        assert headers[++index].equals(User.CONDO_COLUMN) : headers[index];
        assert headers[++index].equals(User.REFERRAL_COLUMN) : headers[index];
        assert headers[++index].equals(User.EMAIL_VERIFIED_COLUMN) : headers[index];
        assert headers[++index].equals(User.CONSUMER_REQUEST_COLUMN) : headers[index];
        assert headers[++index].equals(User.VOLUNTEER_REQUEST_COLUMN) : headers[index];
        assert headers[++index].equals(User.SPECIALIST_COLUMN) : headers[index];
        assert headers[++index].equals(User.PACKER_COLUMN) : headers[index];
        assert headers[++index].equals(User.BHS_COLUMN) : headers[index];
        assert headers[++index].equals(User.HELPLINE_COLUMN) : headers[index];
        assert headers[++index].equals(User.SITELINE_COLUMN) : headers[index];
        assert headers[++index].equals(User.INREACH_COLUMN) : headers[index];
        assert headers[++index].equals(User.OUTREACH_COLUMN) : headers[index];
        assert headers[++index].equals(User.MARKETING_COLUMN) : headers[index];
        assert headers[++index].equals(User.MODERATORS_COLUMN) : headers[index];
        assert headers[++index].equals(User.TRUST_LEVEL_4_COLUMN) : headers[index];
        assert headers[++index].equals(User.WORKFLOW_COLUMN) : headers[index];
        assert headers[++index].equals(User.CUSTOMER_INFO_COLUMN) : headers[index];
        assert headers[++index].equals(User.ADVISOR_COLUMN) : headers[index];
        assert headers[++index].equals(User.COORDINATOR_COLUMN) : headers[index];
        assert headers[++index].equals(User.ADMIN_COLUMN) : headers[index];

        List<User> users = new ArrayList<>();
        List<String> groups = new ArrayList<>();

        Iterator<String[]> iterator = lines.iterator();
        // Skip the header line.
        iterator.next();

        while (iterator.hasNext()) {
            String[] columns = iterator.next();

            assert columns.length == headers.length : columns.length + " != " + headers.length;
            index = 0;

            long id = Long.parseLong(columns[index++]);
            String name = columns[index++];
            String userName = columns[index++];
            String phone = columns[index++];
            String altPhone = columns[index++];
            String neighborhood = columns[index++];
            String city = columns[index++];
            String address = columns[index++];

            groups.clear();
            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_CONSUMERS);
            }
            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_VOICEONLY);
            }
            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_DISPATCHERS);
            }
            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_DRIVERS);
            }

            String createdAt = columns[index++];
            Boolean isCondo = Boolean.valueOf(columns[index++]);
            String referral = columns[index++];
            Boolean emailVerified = Boolean.valueOf(columns[index++]);
            Boolean hasConsumerRequest = Boolean.valueOf(columns[index++]);
            String volunteerRequest = columns[index++];

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_SPECIALISTS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_PACKERS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_BHS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_HELPLINE);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_SITELINE);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_INREACH);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_OUTREACH);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_MARKETING);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_MODERATORS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_TRUST_LEVEL_4);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_WORKFLOW);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_CUSTOMER_INFO);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_ADVISOR);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_COORDINATOR);
            }

            //noinspection UnusedAssignment
            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_ADMIN);
            }

            try {
                users.add(User.createUser(name, userName, id, address, city, phone, altPhone,
                        neighborhood, createdAt, isCondo, hasConsumerRequest,
                        volunteerRequest, referral, emailVerified, groups));
            } catch (UserException ex) {
                users.add(ex.user);
            }
        }

        return users;
    }

    static List<DeliveryData> dailyDeliveryPosts(ApiQueryResult apiQueryResult) {
        assert apiQueryResult.headers.length == 3 : apiQueryResult.headers.length;
        assert apiQueryResult.headers[2].equals("raw");

        List<DeliveryData> dailyDeliveries = new ArrayList<>();

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            //
            String raw = ((String)columns[2]).trim();

            // 2020/03/28
            //
            //[HelpBerkeleyDeliveries - 3_28.csv|attachment](upload://xyzzy.csv) (828 Bytes)

            int index = raw.indexOf('\n');
            if (index == -1) {
                LOGGER.warn("Cannot parse daily deliver post: " + raw);
                continue;
            }
            String date = raw.substring(0, index);

            dailyDeliveries.add(new DeliveryData(date, downloadFileName(raw), shortURLDiscoursePost(raw)));
        }

        return dailyDeliveries;
    }

    static List<DeliveryData> dailyDeliveryPosts(final String csvData) {

        List<DeliveryData> dailyDeliveries = new ArrayList<>();

        String[] lines = csvData.split("\n");
        assert lines.length > 0;

        String header = lines[0];
        assert header.equals(DeliveryData.deliveryPostsHeader().trim());

        for (int index = 1; index < lines.length; index++) {
            // FIX THIS, DS: use CSVReader
            String[] fields = lines[index].split(Constants.CSV_SEPARATOR, -1);
            assert fields.length == 3 : lines[index];
            dailyDeliveries.add(new DeliveryData(fields[0], fields[1], fields[2]));
        }

        return dailyDeliveries;
    }

    static Map<String, String> deliveryDetails(ApiQueryResult apiQueryResult) {
        assert apiQueryResult.headers.length == 2 : apiQueryResult.headers.length;
        assert apiQueryResult.headers[0].equals("post_number");
        assert apiQueryResult.headers[1].equals("raw");

        Map<String, String> deliveryDetails = new HashMap<>();

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 2 : columns.length;

            //
            Long id = ((Long)columns[0]);
            String raw = ((String)columns[1]).trim();

            if (id == 1) {
                assert raw.startsWith("This must be specifically formatted.") : raw;
                continue;
            }

            parseDeliveryDetails(id, raw, deliveryDetails);
        }

        return deliveryDetails;
    }

    /**
     * Find first occurrence of @username, and take everything after it as the details.
     * Replace all newlines with spaces. Multiple spaces are collapsed.
     *
     * @param id post number
     * @param rawPost post text
     * @param deliveryDetails map, keyed by user name, to update with delivery details.
     */
    static void parseDeliveryDetails(long id, final String rawPost, Map<String, String> deliveryDetails) {

        // Normalize EOL
        String raw = rawPost.replaceAll("\\r\\n?", "\n");

        int index = raw.indexOf("@");

        if (index == -1) {
            LOGGER.warn("Skipping post {}. Cannot parse user name in {}", id, raw);
            return;
        }

        // Remove anything before @username
        raw = raw.substring(index);

        // Capture the user name line
        index = raw.indexOf('\n');

        if (index == -1) {
            LOGGER.warn("Skipping post {}. No details found {}", id, raw);
            return;
        }

        String userName = raw.substring(0, index);
        raw = raw.substring(index);

        String regex = "@([A-Za-z0-9._\\-]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(userName);
        if (! matcher.find()) {
            LOGGER.warn("Skipping post {}. No @user not found in post: {}", id, raw);
        }

        userName = matcher.group(1);
        String details = raw.replaceAll("\n", " ").replaceAll("\\s+", " ").trim();

        deliveryDetails.put(userName, details);
    }

    static String shortURLDiscoursePost(final String line) {
        int index = line.indexOf("upload://");
        assert index != -1 : line;
        String shortURL = line.substring(index);
        index = shortURL.indexOf(')');
        shortURL = shortURL.substring(0, index);

        return shortURL;
    }

    static String shortURLUploadResponse(final String line) {
        int index = line.indexOf("upload://");
        assert index != -1 : line;
        String shortURL = line.substring(index);
        index = shortURL.indexOf('"');
        shortURL = shortURL.substring(0, index);

        return shortURL;
    }

    static String downloadFileName(final String line) {
        int index = line.indexOf('[');
        assert index != -1 : line;
        int end = line.indexOf('|');
        assert end > index : line;

        return line.substring(index + 1, end);
    }

    static String postBody(final String json) {
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>)
                JsonReader.jsonToJava(json, options);

        assert map.containsKey("raw") : json;
        return (String)map.get("raw");
    }

    static PostResponse postResponse(final String json) {
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>)
                JsonReader.jsonToJava(json, options);

        assert map.containsKey("topic_id") : json;
        long topic_id = (long)map.get("topic_id");
        assert map.containsKey("post_number") : json;
        long post_number = (long)map.get("post_number");
        assert map.containsKey("topic_slug") : json;
        String topicSlug = (String)map.get("topic_slug");

        return new PostResponse(topic_id, post_number, topicSlug);
    }

    static Map<Long, String> emailAddresses(final ApiQueryResult queryResult) {
        assert queryResult.headers.length == 3 : queryResult;
        assert queryResult.headers[0].equals("user_id") : queryResult;
        assert queryResult.headers[1].equals("username") : queryResult;
        assert queryResult.headers[2].equals("email") : queryResult;

        Map<Long, String> emailAddresses = new HashMap<>();

        for (Object rowObj : queryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            Long userId = (Long)columns[0];
            String email = (String)columns[2];

            if (emailAddresses.containsKey(userId)) {
                // FIX THIS, DS: update the query to just fetch the primary email address
                LOGGER.warn("User id {} has multiple email addresses, skipping {}", userId, email);
            } else {
                emailAddresses.put(userId, email);
            }
        }

        return emailAddresses;
    }

    static OrderHistoryPost orderHistoryPost(final String rawOrderHistoryPost) {

        // "**Order History -- 2020/01/01**\n\n[order_history.csv|attachment](upload://order-history.csv) (37 Bytes)",

        final String start = "**Order History -- ";
        assert rawOrderHistoryPost.startsWith(start) : rawOrderHistoryPost;
        String[] lines = rawOrderHistoryPost.split("\n");
        assert lines.length == 3 : rawOrderHistoryPost;
        String date = lines[0].substring(start.length());
        assert date.endsWith("**");
        date = date.substring(0, date.length() - 2);
        String shortURL =  shortURLDiscoursePost(lines[2]);
        String fileName = downloadFileName(lines[2]);

        return new OrderHistoryPost(date, fileName, shortURL);
    }

    static RestaurantTemplatePost restaurantTemplatePost(final String rawPost) {

        // "Here we put updated restaurant templates for use by our software.
        //
        // Do Not Modify!
        //
        // [HelpBerkeleyDeliveries - Template.csv|attachment](upload://89KcvxqdAnILkXELUtX939365ag.csv) (1.5 KB)"

        for (String line : rawPost.split("\n")) {
            if (line.contains(Constants.UPLOAD_URI_PREFIX)) {
                String shortURL =  shortURLDiscoursePost(line);
                String fileName = downloadFileName(line);
                return new RestaurantTemplatePost(fileName, shortURL);
            }
        }

        throw new Error("Restaurant template upload link not found in " + rawPost);
    }

    // FIX THIS, DS: use CSVReader
    static OrderHistory orderHistory(final String orderHistoryData) {

        String orderHistoryThroughDate;

        // Normalize EOL.
        String history = orderHistoryData.replaceAll("\\r\\n?", "\n");
        String[] lines = history.split("\n");
        assert lines.length > 0;

        String header = lines[0];
        assert header.equals(OrderHistory.csvHeader().trim()) : header + " != " + OrderHistory.csvHeader().trim();

        // Special case for bootstrap when there is no previous data
        if (lines.length == 1) {
            // FIX THIS, DS: make a constant for the beginning of time last order date
            orderHistoryThroughDate = "2020/01/01";
        } else {
            // The first row of the table encodes the orderHistoryThroughDate
            String[] columns = lines[1].split(Constants.CSV_SEPARATOR, -1);
            assert columns[0].equals("0") : lines[1];
            assert columns[1].equals("0") : lines[1];
            assert columns[2].equals("") : lines[1];
            assert ! columns[3].equals("") : lines[1];
            orderHistoryThroughDate = columns[3].trim();
        }

        OrderHistory orderHistory = new OrderHistory(orderHistoryThroughDate);

        // The remaining rows are user history, 0 or 1 row per user.
        for (int index = 2; index < lines.length; index++) {
            String[] columns = lines[index].split(Constants.CSV_SEPARATOR, -1);
            orderHistory.add(columns[0], Integer.parseInt(columns[1]), columns[2], columns[3]);
        }

        return orderHistory;
    }

    static String fileNameFromShortURL(final String shortURL) {
        assert shortURL.startsWith(Constants.UPLOAD_URI_PREFIX);
        assert shortURL.length() > Constants.UPLOAD_URI_PREFIX.length() : shortURL;
        return shortURL.substring(Constants.UPLOAD_URI_PREFIX.length());
    }

    static Set<Long> emailConfirmations(final ApiQueryResult apiQueryResult) {
        assert apiQueryResult.headers.length == 2 : apiQueryResult.headers.length;
        assert apiQueryResult.headers[0].equals("user_id") : apiQueryResult.headers[0];
        assert apiQueryResult.headers[1].equals("confirmed") : apiQueryResult.headers[1];

        Set<Long> confirmations = new HashSet<>();

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 2 : columns.length;

            Long userId = (Long)columns[0];
            assert userId != null;
            Boolean confirmed = (Boolean)columns[1];
            assert confirmed != null;

            if (confirmed) {
                confirmations.add(userId);
            }
        }

        return confirmations;
    }

    static List<UserOrder> parseOrders(String fileName, String deliveryData) throws IOException, CsvException {
        List<UserOrder> userOrders = new ArrayList<>();

        // Normalize EOL
        String csvData = deliveryData.replaceAll("\\r\\n?", "\n");

        CSVReader csvReader = new CSVReader(new StringReader(csvData));
        List<String[]> rows = csvReader.readAll();
        assert ! rows.isEmpty() : "parseOrders empty delivery data from " + fileName;

        DeliveryColumns indexes = new DeliveryColumns(fileName, rows.get(0));

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {

            String[] columns = rows.get(rowIndex);

            if (! Boolean.parseBoolean(columns[indexes.consumer])) {
                continue;
            }

            String user = "";
            String name = "";
            String phone = "";
            String altPhone = "";
            if (indexes.userName != -1) {
                user = columns[indexes.userName];
            }
            if (indexes.name != -1) {
                name = columns[indexes.name];
            }
            if (indexes.phoneNumber != -1) {
                phone = columns[indexes.phoneNumber];
            }
            if (indexes.altPhoneNumber != -1) {
                altPhone = columns[indexes.altPhoneNumber];
            }
            String veggie = columns[indexes.veggie];
            String normal = columns[indexes.normal];

            // FIX THIS, DS: test for number format violation
            if (((! veggie.isEmpty()) && (Integer.parseInt(veggie) != 0))
                    || ((! normal.isEmpty()) && (Integer.parseInt(normal) != 0))) {
                userOrders.add(new UserOrder(name, user, fileName, phone, altPhone));
            }
        }

        return userOrders;
    }

    // Skip Discourse system users. Not fully formed.
    private static boolean skipUserId(long userId) {
        return (userId == -1) || (userId == -2) || (userId == 708) || (userId == 844);
    }

    static class DeliveryColumns {
        private final int consumer;
        private final int name;
        private final int userName;
        private final int phoneNumber;
        private final int altPhoneNumber;
        private final int veggie;
        private final int normal;

        DeliveryColumns(final String fileName, final String[] columns) {

            consumer = findOrderColumn(Constants.WORKFLOW_CONSUMER_COLUMN, columns);
            name = findOrderColumn(Constants.WORKFLOW_NAME_COLUMN, columns);
            userName = findOrderColumn(Constants.WORKFLOW_USER_NAME_COLUMN, columns);
            phoneNumber = findOrderColumn(Constants.WORKFLOW_PHONE_COLUMN, columns);
            altPhoneNumber = findOrderColumn(Constants.WORKFLOW_ALT_PHONE_COLUMN, columns);
            veggie = findOrderColumn(Constants.WORKFLOW_VEGGIE_COLUMN, columns);
            normal = findOrderColumn(Constants.WORKFLOW_NORMAL_COLUMN, columns);

            String errors = "";
            if (consumer == -1) {
                errors += "Cannot find column " + Constants.WORKFLOW_CONSUMER_COLUMN + "\n";
            }
            if ((userName == -1) && (name == -1)) {
                errors += "Cannot find either " + Constants.WORKFLOW_NAME_COLUMN
                        + " or " + Constants.WORKFLOW_USER_NAME_COLUMN + " column";
            }
            if (veggie == -1) {
                errors += "Cannot find column " + Constants.WORKFLOW_VEGGIE_COLUMN + "\n";
            }
            if (normal == -1) {
                errors += "Cannot find column " + Constants.WORKFLOW_NORMAL_COLUMN + "\n";
            }

            if (! errors.isEmpty()) {
                throw new Error("Problem(s) with deliver file: " + fileName + "\n" + errors);
            }
        }

        private int findOrderColumn(final String columnName, final String[] columnNames) {

            String desiredColumnName = columnName.trim()
                    .toLowerCase()
                    .replace(" ", "");

            for (int index = 0; index < columnNames.length; index++) {
                String targetColumnName = columnNames[index].trim()
                        .toLowerCase()
                        .replace(" ", "")
                        .replaceAll("s$", "");

                if (desiredColumnName.equals(targetColumnName)) {
                    return index;
                }
            }

            return -1;
        }
    }
}
