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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class CategoryImagesTest extends TestBase {

    private static final String BROKEN_QUERY_RESULT = "{\"columns\": [\"nonsense\"], \"rows\": []}";

    private static final String HEADER =
            "\"" + CategoryImages.COLUMN_TOPIC_ID + "\",\"" + CategoryImages.COLUMN_TOPIC_NAME
            + "\",\"" + CategoryImages.COLUMN_POST_NUMBER + "\",\"" + CategoryImages.COLUMN_POST_ID
            + "\",\"" + CategoryImages.COLUMN_IMAGE_NAME + "\"";

    @Before
    public void setup() {
        CategoryImages.NAP_MILLISECONDS = 0;
        HttpClientSimulator.clearQueryResponseData();
        HttpClientSimulator.clearSendFailures();
    }

    @After
    public void cleanup() {
        // Surefire reuses one fork, so anything left set here leaks into the next test - in this
        // class or another one. A non zero nap is the expensive kind of leak: it makes unrelated
        // tests sleep.
        CategoryImages.NAP_MILLISECONDS = 0;
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.clearQueryResponseData();
        HttpClientSimulator.clearSendFailures();
    }

    /** A QUERY_GET_CATEGORY_TOPICS response naming these topics, titled "Topic <id>". */
    private static void queueCategoryTopics(final long... topicIds) {

        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"errors\": [],")
                .append("\"columns\": [\"topic_id\", \"title\", \"created_at\"],")
                .append("\"rows\": [");

        for (int index = 0; index < topicIds.length; index++) {
            if (index > 0) {
                json.append(",");
            }
            json.append("[").append(topicIds[index]).append(",\"Topic ").append(topicIds[index])
                    .append("\",\"2024-01-01T00:00:00.000Z\"]");
        }

        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_CATEGORY_TOPICS, json.append("]}").toString());
    }

    /** A QUERY_GET_TOPIC_IMAGES response. Rows are {postNumber, uploadId, filesize}. */
    private static void queueTopicImages(final long[][] rows) {

        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"errors\": [],")
                .append("\"columns\": [\"post_id\", \"post_number\", \"upload_id\","
                        + " \"original_filename\", \"extension\", \"filesize\", \"url\"],")
                .append("\"rows\": [");

        for (int index = 0; index < rows.length; index++) {
            if (index > 0) {
                json.append(",");
            }
            json.append("[9").append(rows[index][0]).append(",").append(rows[index][0]).append(",")
                    .append(rows[index][1]).append(",\"image.jpeg\",\"jpeg\",")
                    .append(rows[index][2]).append(",\"//cdn.example.com/image.jpeg\"]");
        }

        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_TOPIC_IMAGES, json.append("]}").toString());
    }

    // ---- listing ----

    @Test
    public void listsEveryTopicsImagesTest() {
        queueCategoryTopics(7253, 8000);
        queueTopicImages(new long[][] { {32, 11463, 1234}, {33, 11465, 5678} });
        queueTopicImages(new long[][] { {5, 12000, 4242} });

        List<String> lines = new CategoryImages(createApiSimulator(), "Deliveries").list();

        assertThat(lines).containsExactly(
                HEADER,
                "\"7253\",\"Topic 7253\",\"32\",\"932\",\"image.jpeg\"",
                "\"7253\",\"Topic 7253\",\"33\",\"933\",\"image.jpeg\"",
                "\"8000\",\"Topic 8000\",\"5\",\"95\",\"image.jpeg\"");
    }

    @Test
    public void commaInATopicTitleDoesNotSplitTheRowTest() {
        // Half the titles in the real category look like this. Joining strings would produce a row
        // with six fields, and the whole point of the CSV is that something downstream reads it.
        HttpClientSimulator.setQueryResponseData(Constants.QUERY_GET_CATEGORY_TOPICS,
                "{\"success\": true, \"errors\": [],"
                        + "\"columns\": [\"topic_id\", \"title\"],"
                        + "\"rows\": [[7253,\"Free Groceries 3/23, 3pm-ish run\"]]}");
        queueTopicImages(new long[][] { {32, 11463, 1234} });

        List<String> lines = new CategoryImages(createApiSimulator(), "Deliveries").list();

        assertThat(lines).containsExactly(HEADER,
                "\"7253\",\"Free Groceries 3/23, 3pm-ish run\",\"32\",\"932\",\"image.jpeg\"");
    }

    @Test
    public void topicWithNoImagesContributesNothingTest() {
        queueCategoryTopics(7253, 8000);
        queueTopicImages(new long[][] { });
        queueTopicImages(new long[][] { {5, 12000, 4242} });

        List<String> lines = new CategoryImages(createApiSimulator(), "Deliveries").list();

        assertThat(lines).containsExactly(
                HEADER, "\"8000\",\"Topic 8000\",\"5\",\"95\",\"image.jpeg\"");
    }

    @Test
    public void emptyCategoryTest() {
        queueCategoryTopics();

        // The header still goes out - an empty listing should be an empty CSV, not an empty file,
        // which reads downstream as a missing post_id column rather than as no posts.
        assertThat(new CategoryImages(createApiSimulator(), "Deliveries").list())
                .containsExactly(HEADER);
    }

    @Test
    public void unreadableTopicIsSkippedTest() {
        queueCategoryTopics(7253, 8000);
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_TOPIC_IMAGES, BROKEN_QUERY_RESULT);
        queueTopicImages(new long[][] { {5, 12000, 4242} });

        CategoryImages categoryImages = new CategoryImages(createApiSimulator(), "Deliveries");
        List<String> lines = categoryImages.list();

        // Listing is read only, so one unreadable topic is worth reporting and carrying on from.
        assertThat(lines).containsExactly(
                HEADER, "\"8000\",\"Topic 8000\",\"5\",\"95\",\"image.jpeg\"");
        assertThat(categoryImages.unreadableTopics()).containsExactly(7253L);
    }

    @Test
    public void writesRowsDeletePostsCanReadTest() throws IOException {
        // Post 71301 holds two uploads, so two rows, and post #1 is a row that must never be
        // deleted - deleting a topic's first post deletes the topic.
        queueCategoryTopics(7253);
        HttpClientSimulator.setQueryResponseData(Constants.QUERY_GET_TOPIC_IMAGES,
                "{\"success\": true, \"errors\": [],"
                        + "\"columns\": [\"post_id\", \"post_number\", \"upload_id\","
                        + " \"original_filename\", \"extension\", \"filesize\", \"url\"],"
                        + "\"rows\": [[71300,1,11460,\"cover.png\",\"png\",1,\"//c/i.png\"],"
                        + "[71301,32,11463,\"image0.jpeg\",\"jpeg\",1,\"//c/i.png\"],"
                        + "[71301,32,11464,\"image1.jpeg\",\"jpeg\",1,\"//c/i.png\"],"
                        + "[71304,33,11465,\"photo.jpeg\",\"jpeg\",1,\"//c/i.png\"]]}");

        List<String> lines = new CategoryImages(createApiSimulator(), "Deliveries").list();

        Path listing = Files.createTempFile("category-images", ".csv");
        try {
            Files.write(listing, lines, StandardCharsets.UTF_8);

            // The reason the rows carry post_id and post_number: no conversion step. The duplicate
            // rows for post 71301 collapse, and the first post is dropped.
            assertThat(PostDeleter.readPostIds(listing.toString()))
                    .containsExactly(71301L, 71304L);
        } finally {
            Files.deleteIfExists(listing);
        }
    }

    @Test
    public void tooManyUnreadableTopicsGivesUpTest() {
        long[] topicIds = new long[CategoryImages.MAX_CONSECUTIVE_FAILURES + 3];
        for (int index = 0; index < topicIds.length; index++) {
            topicIds[index] = 7000 + index;
            HttpClientSimulator.setQueryResponseData(
                    Constants.QUERY_GET_TOPIC_IMAGES, BROKEN_QUERY_RESULT);
        }
        queueCategoryTopics(topicIds);

        CategoryImages categoryImages = new CategoryImages(createApiSimulator(), "Deliveries");

        // A dead site should not be ground through one topic at a time, each attempt carrying
        // ApiClient's ten retries behind it.
        Throwable thrown = catchThrowable(categoryImages::list);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("in a row");
        assertThat(categoryImages.unreadableTopics())
                .hasSize(CategoryImages.MAX_CONSECUTIVE_FAILURES);
    }

    // ---- pacing ----

    @Test
    public void rateLimitingBacksOffThePaceTest() {
        // A non zero floor, so the pacer is live. It reads the static at construction, so this has
        // to be set before the CategoryImages is built - and put back, or other classes sleep.
        CategoryImages.NAP_MILLISECONDS = 1;
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        queueCategoryTopics(7253, 8000);
        queueTopicImages(new long[][] { {32, 11463, 1234} });
        queueTopicImages(new long[][] { {5, 12000, 4242} });
        // After the category topics query, so the 429 lands on a topic query - which is the one
        // the loop paces around.
        HttpClientSimulator.setSendFailure(
                HttpClientSimulator.SendFailType.TOO_MANY_TIMES_429_RESULT, 1, 1);

        CategoryImages categoryImages = new CategoryImages(createApiSimulator(), "Deliveries");
        List<String> lines = categoryImages.list();

        // ApiClient absorbs the 429 by retrying, so the listing is still complete - header and a
        // row from each of the two topics.
        assertThat(lines).hasSize(3);
        assertThat(categoryImages.pacer().backoffs()).isEqualTo(1);
        assertThat(categoryImages.pacer().napMillis())
                .isEqualTo(AdaptivePacer.MINIMUM_BACKOFF_MILLISECONDS);
    }

    @Test
    public void cleanRunKeepsTheConfiguredPaceTest() {
        CategoryImages.NAP_MILLISECONDS = 1;
        queueCategoryTopics(7253);
        queueTopicImages(new long[][] { {32, 11463, 1234} });

        CategoryImages categoryImages = new CategoryImages(createApiSimulator(), "Deliveries");
        categoryImages.list();

        assertThat(categoryImages.pacer().backoffs()).isEqualTo(0);
        assertThat(categoryImages.pacer().napMillis()).isEqualTo(1);
    }

    @Test
    public void pacingIsOnByDefaultTest() {
        // The whole point of this class. A listing that queries once per topic with no pacing
        // collects a rate limit refusal every time the site's request window rolls.
        assertThat(Long.getLong("memberdata.listNapMillis", 1100L)).isGreaterThan(1000L);
    }
}
