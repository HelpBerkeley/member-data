//
// Copyright (c) 2020-2021 helpberkeley.org
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

    public enum DetailsHandling {
        LAST_POST_WINS,
        CONCATENTATE_MULTIPLE_POSTS
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HBParser.class);

    public static ApiQueryResult parseQueryResult(final String queryResultJson) {
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
        List<String> groupOwnerships = new ArrayList<>();

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
            groupOwnerships.clear();

            for (Group group : groups.values()) {
                if (group.hasMember(userId)) {
                    groupMemberships.add(group.name);
                }
                if (group.hasOwner(userId)) {
                    groupOwnerships.add(group.name);
                }
            }

            Boolean verified = emailVerified.contains(userId);

            try {
                users.add(User.createUser(name, userName, userId, address, city, phone, altPhone, neighborhood,
                        createdAt, isCondo, hasConsumerRequest, volunteerRequest, referral, verified,
                        groupMemberships, groupOwnerships));
            } catch (UserException ex) {
                // FIX THIS, DS: get rid of UserException?
                users.add(ex.user);
            }
        }

        return users;
    }

    static Map<Long, String> groupNames(ApiQueryResult queryResult) {
        Map<Long, String> results = new HashMap<>();

        assert queryResult.headers.length == 2 :
                "Unexpected number of columns for groupNames query result: " + queryResult;
        assert queryResult.headers[0].equals(Constants.COLUMN_ID) : queryResult.headers[0];
        assert queryResult.headers[1].equals(Constants.COLUMN_NAME) : queryResult.headers[1];

        for (Object rowObj :  queryResult.rows) {
            Object[] columns = (Object[])rowObj;

            Long groupId = (Long)columns[0];
            String groupName = (String)columns[1];

            // Skip groups that don't have any software processing
            if (Group.supportedGroup(groupId, groupName)) {
                results.put(groupId, groupName);
            }
        }

        return results;
    }

    static Map<String, Group> groupUsers(final Map<Long, String> groupNames, final ApiQueryResult queryResult) {
        Map<String, Group> groups = new HashMap<>();

        assert queryResult.headers.length == 3 :
                "Unexpected number of columns for groupUsers query result: " + queryResult;
        assert queryResult.headers[0].equals(Constants.COLUMN_GROUP_ID) : queryResult.headers[0];
        assert queryResult.headers[1].equals(Constants.COLUMN_USER_ID) : queryResult.headers[1];
        assert queryResult.headers[2].equals(Constants.COLUMN_GROUP_OWNER) : queryResult.headers[2];

        for (Object rowObj : queryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            Long groupId = (Long)columns[0];
            Long userId = (Long)columns[1];
            Boolean owner = (Boolean)columns[2];

            String groupName = groupNames.get(groupId);

            if (! Group.supportedGroup(groupId, groupName)) {
                continue;
            }

            Group group = groups.computeIfAbsent(groupName, Group::new);
            group.addUser(userId);
            if (owner) {
                group.addOwner(userId);
            }
        }

        return groups;
    }

    // From raw form
    static List<User> users(final String csvData) throws IOException, CsvException {

        CSVReader cvsReader = new CSVReader(new StringReader(csvData));
        List<String[]> lines = cvsReader.readAll();
        assert ! lines.isEmpty();
        String[] headers = lines.get(0);

        assert headers.length == 50 : headers.length;

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
        assert headers[++index].equals(User.FRVOICEONLY_COLUMN) : headers[index];
        assert headers[++index].equals(User.DISPATCHER_COLUMN) : headers[index];
        assert headers[++index].equals(User.DRIVER_COLUMN) : headers[index];
        assert headers[++index].equals(User.TRAINED_DRIVER_COLUMN) : headers[index];
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
        assert headers[++index].equals(User.TRAINED_CUSTOMER_CARE_A_COLUMN) : headers[index];
        assert headers[++index].equals(User.TRAINED_CUSTOMER_CARE_B_COLUMN) : headers[index];
        assert headers[++index].equals(User.INREACH_COLUMN) : headers[index];
        assert headers[++index].equals(User.OUTREACH_COLUMN) : headers[index];
        assert headers[++index].equals(User.MARKETING_COLUMN) : headers[index];
        assert headers[++index].equals(User.MODERATORS_COLUMN) : headers[index];
        assert headers[++index].equals(User.TRUST_LEVEL_4_COLUMN) : headers[index];
        assert headers[++index].equals(User.WORKFLOW_COLUMN) : headers[index];
        assert headers[++index].equals(User.CUSTOMER_INFO_COLUMN) : headers[index];
        assert headers[++index].equals(User.ADVISOR_COLUMN) : headers[index];
        assert headers[++index].equals(User.BOARD_COLUMN) : headers[index];
        assert headers[++index].equals(User.COORDINATOR_COLUMN) : headers[index];
        assert headers[++index].equals(User.LIMITED_RUNS_COLUMN) : headers[index];
        assert headers[++index].equals(User.AT_RISK_COLUMN) : headers[index];
        assert headers[++index].equals(User.BIKERS_COLUMN) : headers[index];
        assert headers[++index].equals(User.OUT_COLUMN) : headers[index];
        assert headers[++index].equals(User.EVENTS_DRIVER_COLUMN) : headers[index];
        assert headers[++index].equals(User.TRAINED_EVENT_DRIVER_COLUMN) : headers[index];
        assert headers[++index].equals(User.GONE_COLUMN) : headers[index];
        assert headers[++index].equals(User.OTHER_DRIVERS_COLUMN) : headers[index];
        assert headers[++index].equals(User.ADMIN_COLUMN) : headers[index];
        assert headers[++index].equals(User.GROUPS_OWNED_COLUMN) : headers[index];
        assert headers[++index].equals(User.MONDAY_FRREG_COLUMN) : headers[index];
        assert headers[++index].equals(User.WEDNESDAY_FRREG_COLUMN) : headers[index];
        assert headers[++index].equals(User.THURSDAY_FRREG_COLUMN) : headers[index];

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
                groups.add(Constants.GROUP_FRVOICEONLY);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_DISPATCHERS);
            }
            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_DRIVERS);
            }
            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_TRAINED_DRIVERS);
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
                groups.add(Constants.GROUP_TRAINED_CUSTOMER_CARE_A);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_TRAINED_CUSTOMER_CARE_B);
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
                groups.add(Constants.GROUP_BOARDMEMBERS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_COORDINATORS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_LIMITED);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_AT_RISK);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_BIKERS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_OUT);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_EVENT_DRIVERS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_TRAINED_EVENT_DRIVERS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_GONE);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_OTHER_DRIVERS);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_ADMIN);
            }

            String groupsOwned = columns[index++];
            List<String> groupsOwnedList = Arrays.asList(groupsOwned.split(Constants.CSV_SEPARATOR));

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_MONDAY_FRREG);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_WEDNESDAY_FRREG);
            }

            if (Boolean.parseBoolean(columns[index++])) {
                groups.add(Constants.GROUP_THURSDAY_FRREG);
            }

            try {
                users.add(User.createUser(name, userName, id, address, city, phone, altPhone,
                        neighborhood, createdAt, isCondo, hasConsumerRequest,
                        volunteerRequest, referral, emailVerified, groups, groupsOwnedList));
            } catch (UserException ex) {
                users.add(ex.user);
            }
        }

        return users;
    }

    static List<DeliveryData> dailyDeliveryPosts(ApiQueryResult apiQueryResult) {
        assert apiQueryResult.headers.length == 3 : apiQueryResult.headers.length;
        assert apiQueryResult.headers[2].equals(Constants.DISCOURSE_COLUMN_RAW);

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

    static Map<String, DetailsPost> deliveryDetails(ApiQueryResult apiQueryResult) {
        assert apiQueryResult.headers.length == 2 : apiQueryResult.headers.length;
        assert apiQueryResult.headers[0].equals(Constants.DISCOURSE_COLUMN_POST_NUMBER);
        assert apiQueryResult.headers[1].equals(Constants.DISCOURSE_COLUMN_RAW);

        Map<String, DetailsPost> deliveryDetails = new HashMap<>();

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

            parseDetails(id, raw, DetailsHandling.LAST_POST_WINS, deliveryDetails);
        }

        return deliveryDetails;
    }

    static Map<String, DetailsPost> driverDetails(ApiQueryResult apiQueryResult) {
        assert apiQueryResult.headers.length == 3 : apiQueryResult.headers.length;
        assert apiQueryResult.headers[0].equals(Constants.DISCOURSE_COLUMN_POST_NUMBER);
        assert apiQueryResult.headers[1].equals("deleted_at");
        assert apiQueryResult.headers[2].equals(Constants.DISCOURSE_COLUMN_RAW);

        Map<String, DetailsPost> driverDetails = new HashMap<>();

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            // FIX THIS, DS: make this a common routine with delivery details

            //
            Long postNumber = ((Long)columns[0]);
            String raw = ((String)columns[2]).trim();

            parseDetails(postNumber, raw, DetailsHandling.CONCATENTATE_MULTIPLE_POSTS, driverDetails);
        }

        return driverDetails;
    }

    /**
     * Find first occurrence of @username, and take everything after it as the details.
     * Replace all newlines with spaces. Multiple spaces are collapsed.
     *
     * @param postNumber post number
     * @param rawPost post text
     * @param handling handling for multiple posts for same user name
     * @param detailsMap map, keyed by user name, to update with details.
     */
    static void parseDetails(long postNumber, final String rawPost,
             DetailsHandling handling, Map<String, DetailsPost> detailsMap) {

        // Normalize EOL
        String raw = rawPost.replaceAll("\\r\\n?", "\n");

        int index = raw.indexOf("@");

        if (index == -1) {
            LOGGER.warn("Skipping post {}. Cannot parse user name in {}", postNumber, raw);
            return;
        }

        // Remove anything before @username
        raw = raw.substring(index);

        // Capture the user name line
        index = raw.indexOf('\n');

        if (index == -1) {
            LOGGER.warn("Skipping post {}. No details found {}", postNumber, raw);
            return;
        }

        String userName = raw.substring(0, index);
        raw = raw.substring(index);

        String regex = "@([A-Za-z0-9._\\-]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(userName);
        if (! matcher.find()) {
            LOGGER.warn("Skipping post {}. No @user not found in post: {}", postNumber, raw);
        }

        userName = matcher.group(1);
        String details = raw.replaceAll("\n", " ").replaceAll("\\s+", " ").trim();

        detailsMap.computeIfAbsent(userName, DetailsPost::new);
        DetailsPost detailsPost = detailsMap.get(userName);

        if (handling == DetailsHandling.LAST_POST_WINS) {
            detailsPost.setDetails(postNumber, details);
        } else {
            assert handling == DetailsHandling.CONCATENTATE_MULTIPLE_POSTS : handling;
            detailsPost.appendDetails(postNumber, details);
        }
    }

    public static String shortURLDiscoursePost(final String line) {
        int index = line.indexOf(Constants.UPLOAD_URI_PREFIX);
        assert index != -1 : line;
        String shortURL = line.substring(index);
        index = shortURL.indexOf(')');
        shortURL = shortURL.substring(0, index);

        return shortURL;
    }

    static String shortURLUploadResponse(final String line) {
        int index = line.indexOf(Constants.UPLOAD_URI_PREFIX);
        assert index != -1 : line;
        String shortURL = line.substring(index);
        index = shortURL.indexOf('"');
        shortURL = shortURL.substring(0, index);

        return shortURL;
    }

    public static String downloadFileName(final String line) {
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

        assert map.containsKey(Constants.DISCOURSE_COLUMN_RAW) : json;
        return (String)map.get(Constants.DISCOURSE_COLUMN_RAW);
    }

    static PostResponse postResponse(final String json) {
        Map<String, Object> options = new HashMap<>();
        options.put(JsonReader.USE_MAPS, Boolean.TRUE);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>)
                JsonReader.jsonToJava(json, options);

        assert map.containsKey("topic_id") : json;
        long topic_id = (long)map.get("topic_id");
        assert map.containsKey(Constants.DISCOURSE_COLUMN_POST_NUMBER);
        long post_number = (long)map.get(Constants.DISCOURSE_COLUMN_POST_NUMBER);
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

    static UploadFile parseFileFromPost(String rawPost) {

        for (String line : rawPost.split("\n")) {
            if (line.contains(Constants.UPLOAD_URI_PREFIX)) {
                String shortURL =  shortURLDiscoursePost(line);
                String fileName = downloadFileName(line);

                return new UploadFile(fileName, shortURL);
            }
        }

        throw new Error("No downloadable link not found in " + rawPost);
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

    static Collection<String> parseDeliveryDrivers(String fileName, String deliveryData) throws IOException, CsvException {
        Set<String> drivers = new HashSet<>();

        // Normalize EOL
        String csvData = deliveryData.replaceAll("\\r\\n?", "\n");

        CSVReader csvReader = new CSVReader(new StringReader(csvData));
        List<String[]> rows = csvReader.readAll();
        assert ! rows.isEmpty() : "parseOrders empty delivery data from " + fileName;

        DeliveryColumns indexes = new DeliveryColumns(fileName, rows.get(0));

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {

            String[] columns = rows.get(rowIndex);

            if (Boolean.parseBoolean(columns[indexes.driver])
                && (! Boolean.parseBoolean(columns[indexes.consumer]))) {
                drivers.add(columns[indexes.userName]);
            }
        }

        return drivers;
    }

    static Collection<String> parseOneKitchenDeliveryDrivers(
            String fileName, String deliveryData) throws IOException, CsvException {
        Set<String> drivers = new HashSet<>();

        // Normalize EOL
        String csvData = deliveryData.replaceAll("\\r\\n?", "\n");

        CSVReader csvReader = new CSVReader(new StringReader(csvData));
        List<String[]> rows = csvReader.readAll();
        assert ! rows.isEmpty() : "parseOrders empty delivery data from " + fileName;

        OneKitchenDeliveryColumns indexes = new OneKitchenDeliveryColumns(fileName, rows.get(0));

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {

            String[] columns = rows.get(rowIndex);

            if (Boolean.parseBoolean(columns[indexes.driver])
                    && (! Boolean.parseBoolean(columns[indexes.consumer]))) {
                drivers.add(columns[indexes.userName]);
            }
        }

        return drivers;
    }

    // Skip Discourse system users. Not fully formed.
    private static boolean skipUserId(long userId) {
        return (userId == -1) || (userId == -2) || (userId == 708) || (userId == 844);
    }

    static class DeliveryColumns {
        private final int consumer;
        private final int driver;
        private final int name;
        private final int userName;
        private final int phoneNumber;
        private final int altPhoneNumber;
        private final int veggie;
        private final int normal;

        DeliveryColumns(final String fileName, final String[] columns) {

            consumer = findOrderColumn(Constants.WORKFLOW_CONSUMER_COLUMN, columns);
            driver = findOrderColumn(Constants.WORKFLOW_DRIVER_COLUMN, columns);
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
            if (driver == -1) {
                errors += "Cannot find column " + Constants.WORKFLOW_DRIVER_COLUMN + "\n";
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

    static class OneKitchenDeliveryColumns {
        private final int consumer;
        private final int driver;
        private final int name;
        private final int userName;
        private final int phoneNumber;
        private final int altPhoneNumber;

        OneKitchenDeliveryColumns(final String fileName, final String[] columns) {

            consumer = findOrderColumn(Constants.WORKFLOW_CONSUMER_COLUMN, columns);
            driver = findOrderColumn(Constants.WORKFLOW_DRIVER_COLUMN, columns);
            name = findOrderColumn(Constants.WORKFLOW_NAME_COLUMN, columns);
            userName = findOrderColumn(Constants.WORKFLOW_USER_NAME_COLUMN, columns);
            phoneNumber = findOrderColumn(Constants.WORKFLOW_PHONE_COLUMN, columns);
            altPhoneNumber = findOrderColumn(Constants.WORKFLOW_ALT_PHONE_COLUMN, columns);

            String errors = "";
            if (consumer == -1) {
                errors += "Cannot find column " + Constants.WORKFLOW_CONSUMER_COLUMN + "\n";
            }
            if (driver == -1) {
                errors += "Cannot find column " + Constants.WORKFLOW_DRIVER_COLUMN + "\n";
            }
            if ((userName == -1) && (name == -1)) {
                errors += "Cannot find either " + Constants.WORKFLOW_NAME_COLUMN
                        + " or " + Constants.WORKFLOW_USER_NAME_COLUMN + " column";
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
