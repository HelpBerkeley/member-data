/*
 * Copyright (c) 2021. helpberkeley.org
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OrderHistoryDataPosts {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderHistoryData.class);
    private static final String LAST_POST_PROCESSED = "Last post processed: ";

    private static final String TOP_POST  = "This is where the back-end software keeps "
        + " the spreadsheets that it uses for the order history process. "
        + "The posts here are copies of the verified completed daily order spreadsheets "
        + "taken automatically from the [Post completed daily orders topic.]"
        + "https://go.helpberkeley.org/t/post-completed-daily-orders/"
        + Constants.TOPIC_POST_COMPLETED_DAILY_ORDERS
        + "/)\n\n"
        + "*NOTE:* Only the back-end software should post here. "
        + "This post is regenerated and updated daily by the software.\n\n"
        + LAST_POST_PROCESSED;

    private final ApiClient apiClient;
    private final long previousLastPostProcessed;
    private final long currentLastPostProcessed;
    private final SortedMap<String, OrderHistoryData> allPosts = new TreeMap<>();
    private final SortedMap<String, OrderHistoryData> newPosts = new TreeMap<>();

    public OrderHistoryDataPosts(ApiClient apiClient) {
        this.apiClient = apiClient;
        String json = apiClient.runQuery(Constants.QUERY_GET_ORDER_HISTORY_DATA_POSTS);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        previousLastPostProcessed = parseLastProcessedPost(apiQueryResult);
        currentLastPostProcessed = loadPosts(apiQueryResult);
    }

    /**
     * Update the topic post with the last post processed number
     */
    public void updateLastProcessedPost() {

        String postBody = TOP_POST + currentLastPostProcessed;
        apiClient.updatePost(Constants.TOPIC_ORDER_HISTORY_DATA.getId(), postBody);
    }

    public SortedMap<String, OrderHistoryData> getAllPosts() {
        return allPosts;
    }

    public SortedMap<String, OrderHistoryData> getNewPosts() {
        return newPosts;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Find the last post number (1-origin within the Order History Data topic)
     * processed by the previous Order History run.
     */
    private long parseLastProcessedPost(ApiQueryResult apiQueryResult) {
        assert apiQueryResult.headers.length == 3 : apiQueryResult.headers.length;
        assert apiQueryResult.headers[2].equals("raw");
        assert apiQueryResult.rows.length != 0 : "No rows returned";
        Object rowObj = apiQueryResult.rows[0];
        Object[] columns = (Object[]) rowObj;
        assert columns.length == 3 : "Wrong number of columns: " + columns.length;

        String raw = ((String)columns[2]).trim();
        int index = raw.indexOf(LAST_POST_PROCESSED);
        assert index != -1 : LAST_POST_PROCESSED + " not found in " + raw;
        return Long.parseLong(raw.substring(index + LAST_POST_PROCESSED.length()));
    }

    /**
     * Load the map of OrderHistory entries, keyed by date.
     * Duplicates overwrite previous entries.
     *
     * Build the list of entries that are newer that the current setting of lastPostProcessed.
     *
     * Store the post number of the last post.
     *
     * @param apiQueryResult Result from order history data posts query
     * @return Number (1-origin of the last post processed)
     */
    private long loadPosts(ApiQueryResult apiQueryResult) {
        assert apiQueryResult.headers.length == 3 : apiQueryResult.headers.length;
        assert apiQueryResult.headers[0].equals("post_number");
        assert apiQueryResult.headers[2].equals("raw");

        long postNumber = 0;

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 3 : columns.length;

            postNumber = (Long) (columns[0]);

            assert postNumber != 0;
            if (postNumber == 1) {
                continue;
            }

            String raw = ((String) columns[2]).trim();

            OrderHistoryData orderHistoryData = new OrderHistoryData(raw);

            allPosts.put(orderHistoryData.getDate(), orderHistoryData);

            if (postNumber > previousLastPostProcessed) {
                newPosts.put(orderHistoryData.getDate(), orderHistoryData);
            }
        }

        return postNumber;
    }
}
