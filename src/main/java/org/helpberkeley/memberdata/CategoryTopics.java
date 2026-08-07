//
// Copyright (c) 2024 helpberkeley.org
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fetches, and acts on, the Discourse-hosted images in a topic's posts. The set of images is
 * determined by the QUERY_GET_TOPIC_IMAGES data explorer query, which returns one row per image
 * upload in the topic's non-deleted posts.
 */
public class CategoryTopics {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryTopics.class);

    static final String COLUMN_TOPIC_ID = "topic_id";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_CREATED_AT = "created_at";

    private final ApiClient apiClient;
    private final List<Topic> topics;

    CategoryTopics(final ApiClient apiClient, final String categoryName) {
        this.apiClient = apiClient;
        this.topics = fetchTopics(categoryName);
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public List<Long> getTopicIds() {
        List<Long> topicIds = new ArrayList<>();
        topics.forEach(topic -> topicIds.add(topic.getId()));

        return topicIds;
    }

    private List<Topic> fetchTopics(final String categoryName) {

        String json = apiClient.runQueryWithParams(Constants.QUERY_GET_CATEGORY_TOPICS,
                Map.of("category_name", categoryName));
        ApiQueryResult queryResult = HBParser.parseQueryResult(json);

        int topicIdIndex = queryResult.getColumnIndex(COLUMN_TOPIC_ID);
        int topicNameIndex = queryResult.getColumnIndex(COLUMN_TITLE);

        List<Topic> topics = new ArrayList<>();

        for (Object rowObject : queryResult.rows) {
            Object[] columns = (Object[]) rowObject;
            topics.add(new Topic(
                    (String) columns[topicNameIndex], (Long)columns[topicIdIndex]));
        }

        return topics;
    }

    private List<Long> fetchTopicIds(final String categoryName) {

        String json = apiClient.runQueryWithParams(Constants.QUERY_GET_CATEGORY_TOPICS,
                Map.of("category_name", categoryName));
        ApiQueryResult queryResult = HBParser.parseQueryResult(json);

        int topicIdIndex = queryResult.getColumnIndex(COLUMN_TOPIC_ID);

        List<Long> topicIds = new ArrayList<>();

        for (Object rowObject : queryResult.rows) {
            Object[] columns = (Object[]) rowObject;
            topicIds.add((Long)columns[topicIdIndex]);
        }

        return topicIds;
    }
}
