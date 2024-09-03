/*
 * Copyright (c) 2024 helpberkeley.org
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

import java.util.*;

public class LastRepliesBuilder {

    private final Random random = new Random();

    private final String CONST_DATE = "\"2021/02/28";
    private final String CONST_TIMESTAMP = CONST_DATE + " 16:42:27";

    private final List<String> usernames = Arrays.asList("Somebody", "SomebodyElse", "ThirdPerson", "Xyzzy", "ZZZ", "JVol",
            "MrBackup772", "jsDriver", "jcDriver", "jdDriver", "jbDriver");

    private final Map<String, String> topicsUploadTemplates = Map.of(
            "2844", "[unrouted deliveries|attachement](upload://REPLACE_FILE)",
            "2504", "[routed deliveries|attachement](upload://REPLACE_FILE)",
            "4878", "[REPLACE_FILE|attachment](upload://REPLACE_FILE)",
            "8506", "REPLACE_FILE\n",
            "1860", "[REPLACE_FILE|attachment](upload://REPLACE_FILE) (5.6 KB)",
            "6548", "[REPLACE_FILE|attachment](upload://REPLACE_FILE) (5.6 KB)",
            "859", "[REPLACE_FILE|attachment](upload://REPLACE_FILE) (8.2 KB)",
            "6889", "[REPLACE_FILE|attachment](upload://REPLACE_FILE) (8.2 KB)",
            "10341", "[REPLACE_FILE|attachment](upload://REPLACE_FILE) (8.2 KB)"
    );

    private final Map<Topic, String> topicsDefaultUploadStrings = Map.of(
            Constants.TOPIC_REQUEST_DATA, "[HelpBerkeleyDeliveries - 12_31.csv|attachment](upload://update-member-data-multiple-updates.csv) (8.2 KB)",
            Constants.TOPIC_REQUEST_WORKFLOW, "OneKitchen\n",
            Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES, "[routed-deliveries-v300.csv|attachment](upload://routed-deliveries-v300.csv)",
            Constants.TOPIC_REQUEST_DRIVER_MESSAGES, "[routed deliveries|attachement](upload://routed-deliveries-v200.csv)",
            Constants.TOPIC_REQUEST_DRIVER_ROUTES, "[unrouted deliveries|attachement](upload://unrouted-deliveries.csv)",
            Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS, "[HelpBerkeleyDeliveries - 12_31.csv|attachment](upload://routed-deliveries-v300.csv) (8.2 KB)",
            Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS, "[HelpBerkeleyDeliveries - 12_31.csv|attachment](upload://routed-deliveries-v200.csv) (8.2 KB)",
            Constants.TOPIC_POST_RESTAURANT_TEMPLATE, "[HelpBerkeleyDeliveries - TemplateV2-0-0.csv|attachment](upload://restaurant-template-v200.csv) (5.6 KB)",
            Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE, "[HelpBerkeleyDeliveries - TemplateV2-0-0.csv|attachment](upload://restaurant-template-v300.csv) (5.6 KB)"
    );

    private final String lastRepliesConstantValues = "{\n" +
            "  \"success\": true,\n" +
            "  \"errors\": [],\n" +
            "  \"duration\": 7.9,\n" +
            "  \"result_count\": 9,\n" +
            "  \"params\": {\n" +
            "    \"request_driver_routes\": \"2844\",\n" +
            "    \"request_workflow\": \"8506\",\n" +
            "    \"request_driver_messages\": \"2504\",\n" +
            "    \"request_one_kitchen_driver_messages\": \"4878\",\n" +
            "    \"post_restaurant_template\": \"1860\",\n" +
            "    \"post_onekitchen_restaurant_template\": \"6548\",\n" +
            "    \"post_completed_daily_orders\": \"859\",\n" +
            "    \"post_completed_onekitchen_orders\": \"6889\",\n" +
            "    \"request_updated_member_data\": \"10341\"\n" +
            "  },\n" +
            "  \"columns\": [\n" +
            "    \"topic_id\",\n" +
            "    \"post_number\",\n" +
            "    \"deleted_at\",\n" +
            "    \"raw\",\n" +
            "    \"title\",\n" +
            "    \"username\"\n" +
            "  ],\n" +
            "  \"default_limit\": 1000,\n" +
            "  \"rows\": [\n";

    private final List<String> rows = new ArrayList<>();

    private final List<Topic> requestTopics = List.of(
            Constants.TOPIC_REQUEST_DATA,
            Constants.TOPIC_REQUEST_WORKFLOW,
            Constants.TOPIC_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES,
            Constants.TOPIC_REQUEST_DRIVER_MESSAGES,
            Constants.TOPIC_REQUEST_DRIVER_ROUTES,
            Constants.TOPIC_POST_COMPLETED_ONEKITCHEN_ORDERS,
            Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS,
            Constants.TOPIC_POST_RESTAURANT_TEMPLATE,
            Constants.TOPIC_POST_ONE_KITCHEN_RESTAURANT_TEMPLATE
    );

    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        populateMissingRows();
        stringBuilder.append(lastRepliesConstantValues)
                .append(String.join(",\n", rows))
                .append(" \n]\n}");
        return stringBuilder.toString();
    }

    private void populateMissingRows() {
        Set<Long> rowTopicIds = new HashSet<>();
        for (String row : rows) {
            rowTopicIds.add(getTopicIdFromRow(row));
        }

        List<Topic> unmatchedTopics = new ArrayList<>();
        for (Topic topic : requestTopics) {
            if (!rowTopicIds.contains(topic.getId())) {
                unmatchedTopics.add(topic);
            }
        }

        for (Topic topic: unmatchedTopics) {
            addRowWithTopicAndStatus(topic, true);
        }
    }

    private Long getTopicIdFromRow(String row) {
        row = row.replaceAll("\\[", "");
        String[] parts = row.split(",");
        return Long.parseLong(parts[0].trim());
    }

    public void addAllRowsWithRequest() {
        for (Topic topic : requestTopics) {
            addRowWithRequestTopic(topic);
        }
    }

    public void addRowWithRequestFile(Topic topic, String filename) {
        String uploadString = topicsUploadTemplates.get(String.valueOf(topic.getId())).replace("REPLACE_FILE", filename);
        addRowWithRequestOptions(topic, uploadString, "");
    }

    public void addRowWithRequestFileAndExtra(Topic topic, String filename, String extra) {
        String uploadString = topicsUploadTemplates.get(String.valueOf(topic.getId())).replace("REPLACE_FILE", filename);
        addRowWithRequestOptions(topic, uploadString, extra);
    }

    public void addRowWithRequestTopicAndExtra(Topic topic, String extra) {
        String uploadString = topicsDefaultUploadStrings.get(topic);
        addRowWithRequestOptions(topic, uploadString, extra);
    }

    public void addRowWithRequestTopic(Topic topic) {
        String uploadString = topicsDefaultUploadStrings.get(topic);
        addRowWithRequestOptions(topic, uploadString, "");
    }

    public void addRow(String row) {
        rows.add(row);
    }

    public void addRowWithTopicAndStatus(Topic topic, boolean statusSucceeded) {
        StringBuilder row = new StringBuilder();
        row.append("[ ")
                .append(topic.getId()).append(", ")
                .append(random.nextInt(999)).append(", ")
                .append("null, ")
                .append(CONST_TIMESTAMP).append("\n\n")
                .append(statusSucceeded ? ("Status: Succeeded\nFile: something from " + topic.getId() + "\n")
                        : ("Status: Failed\nSome error message\n")).append("\", \"")
                .append(topic.getName()).append("\", \"")
                .append(usernames.get(random.nextInt(usernames.size())))
                .append("\" ]");
        rows.add(row.toString());
    }

    private void addRowWithRequestOptions(Topic topic, String uploadString, String extra) {
        StringBuilder row = new StringBuilder();
        row.append("[ ")
                .append(topic.getId()).append(", ")
                .append(random.nextInt(999)).append(", ")
                .append("null, ")
                .append(CONST_DATE).append("\n")
                .append(extra).append("\n")
                .append(uploadString).append("\", \"")
                .append(topic.getName()).append("\", \"")
                .append(usernames.get(random.nextInt(usernames.size())))
                .append("\" ]");
        rows.add(row.toString());
    }

}
