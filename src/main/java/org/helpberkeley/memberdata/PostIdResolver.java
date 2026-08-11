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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Turns (topic_id, post_number) pairs into the post ids that delete-posts needs.
 *
 * The QUERY_GET_TOPIC_IMAGES data explorer query already returns both post_id and post_number for
 * a topic's image bearing posts, so one query per topic resolves every pair in it. For a list
 * spread over hundreds of topics that is far fewer requests than looking up posts one at a time.
 *
 * A pair that does not resolve is reported, not fatal. The commonest reason is that the post no
 * longer holds a Discourse-hosted image - including because it has already been soft deleted, which
 * that query filters out.
 */
public class PostIdResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostIdResolver.class);

    static final String COLUMN_TOPIC_ID = "topic_id";
    static final String COLUMN_POST_NUMBER = "post_number";
    static final String COLUMN_POST_ID = "post_id";

    static final String UNRESOLVED_SUFFIX = ".unresolved.csv";

    // Starting milliseconds between queries, and the floor the adaptive pacing will not go below.
    static long NAP_MILLISECONDS = Long.getLong("memberdata.resolveNapMillis", 500L);
    // How often to log progress. Hundreds of queries is several minutes with nothing else to see.
    private static final int PROGRESS_INTERVAL = 25;
    private static final int MAX_LOGGED_PAIRS = 20;
    static final int MAX_CONSECUTIVE_FAILURES = 5;

    private final ApiClient apiClient;
    private final List<TopicPost> wanted;
    private final AdaptivePacer pacer = new AdaptivePacer(NAP_MILLISECONDS);
    private int consecutiveFailures = 0;

    PostIdResolver(final ApiClient apiClient, final List<TopicPost> wanted) {
        this.apiClient = apiClient;
        this.wanted = wanted;
    }

    /** One requested post, identified the way the input file identifies it. */
    static class TopicPost {
        final long topicId;
        final long postNumber;

        TopicPost(final long topicId, final long postNumber) {
            this.topicId = topicId;
            this.postNumber = postNumber;
        }

        @Override
        public boolean equals(Object other) {
            if (! (other instanceof TopicPost)) {
                return false;
            }
            TopicPost that = (TopicPost) other;
            return (topicId == that.topicId) && (postNumber == that.postNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(topicId, postNumber);
        }

        @Override
        public String toString() {
            return "topic " + topicId + " post #" + postNumber;
        }
    }

    /** What a resolve() run produced. */
    static class Resolution {
        // post id, in the order the pairs were listed
        final List<Long> postIds = new ArrayList<>();
        final List<TopicPost> resolved = new ArrayList<>();
        final List<TopicPost> unresolved = new ArrayList<>();
    }

    /**
     * Read the posts to resolve from a CSV file with topic_id and post_number columns. Rows for a
     * topic's first post are dropped - deleting one deletes the entire topic - and duplicate pairs
     * are collapsed.
     */
    static List<TopicPost> readTopicPosts(final String fileName) {

        List<List<String>> rows;
        try (FileReader reader = new FileReader(fileName)) {
            rows = new CSVListReader(reader).readAllToList();
        } catch (IOException ex) {
            throw new MemberDataException("Could not read " + fileName + ": " + ex.getMessage());
        }

        if (rows.isEmpty()) {
            throw new MemberDataException(fileName + " is empty");
        }

        List<String> headers = rows.get(0);
        int topicIdIndex = headers.indexOf(COLUMN_TOPIC_ID);
        int postNumberIndex = headers.indexOf(COLUMN_POST_NUMBER);

        if (topicIdIndex == -1) {
            throw new MemberDataException(fileName + " has no " + COLUMN_TOPIC_ID + " column. Found: "
                    + String.join(", ", headers));
        }
        if (postNumberIndex == -1) {
            throw new MemberDataException(fileName + " has no " + COLUMN_POST_NUMBER + " column. Found: "
                    + String.join(", ", headers));
        }

        Set<TopicPost> topicPosts = new LinkedHashSet<>();
        List<TopicPost> firstPosts = new ArrayList<>();
        int duplicates = 0;

        for (int row = 1; row < rows.size(); row++) {
            List<String> columns = rows.get(row);

            if (columns.size() <= Math.max(topicIdIndex, postNumberIndex)) {
                throw new MemberDataException(fileName + " line " + (row + 1) + " has "
                        + columns.size() + " column(s), too few for " + headers.size() + " headers");
            }

            long topicId = parseLong(fileName, row + 1, COLUMN_TOPIC_ID, columns.get(topicIdIndex));
            long postNumber =
                    parseLong(fileName, row + 1, COLUMN_POST_NUMBER, columns.get(postNumberIndex));

            TopicPost topicPost = new TopicPost(topicId, postNumber);

            if (postNumber == 1) {
                firstPosts.add(topicPost);
            } else if (! topicPosts.add(topicPost)) {
                duplicates++;
            }
        }

        if (! firstPosts.isEmpty()) {
            LOGGER.warn("Skipping {} post(s) that are their topic's first post - deleting one deletes"
                    + " the whole topic, every reply included: {}",
                    firstPosts.size(), abbreviate(firstPosts));
        }
        if (duplicates > 0) {
            LOGGER.info("Collapsed {} duplicate row(s)", duplicates);
        }

        return new ArrayList<>(topicPosts);
    }

    private static String abbreviate(final List<TopicPost> topicPosts) {
        List<TopicPost> shown = topicPosts.size() <= MAX_LOGGED_PAIRS
                ? topicPosts : topicPosts.subList(0, MAX_LOGGED_PAIRS);
        List<String> descriptions = new ArrayList<>();
        for (TopicPost topicPost : shown) {
            descriptions.add(topicPost.toString());
        }
        String joined = String.join(", ", descriptions);
        return (shown.size() == topicPosts.size())
                ? joined : joined + " ... and " + (topicPosts.size() - shown.size()) + " more";
    }

    private static long parseLong(String fileName, int line, String column, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new MemberDataException(fileName + " line " + line + ": " + column
                    + " is not numeric: " + value);
        }
    }

    /** Default output file name for an input file: "list.csv" becomes "list-post-ids.csv". */
    static Path defaultOutputFor(final String fileName) {
        int dot = fileName.lastIndexOf('.');
        String base = (dot == -1) ? fileName : fileName.substring(0, dot);
        return Path.of(base + "-post-ids.csv");
    }

    /** Companion file listing the pairs that could not be resolved. */
    static Path unresolvedFor(final Path output) {
        return Path.of(output + UNRESOLVED_SUFFIX);
    }

    /** Runs one query per distinct topic and matches its post numbers against the wanted list. */
    Resolution resolve() {

        Resolution resolution = new Resolution();

        if (wanted.isEmpty()) {
            LOGGER.info("No posts to resolve");
            return resolution;
        }

        // Group the wanted posts by topic, in first seen order, so each topic is queried once.
        Map<Long, List<TopicPost>> byTopic = new LinkedHashMap<>();
        for (TopicPost topicPost : wanted) {
            byTopic.computeIfAbsent(topicPost.topicId, topicId -> new ArrayList<>()).add(topicPost);
        }

        LOGGER.warn("Resolving {} post(s) across {} topic(s)", wanted.size(), byTopic.size());

        int topicsDone = 0;
        for (Map.Entry<Long, List<TopicPost>> entry : byTopic.entrySet()) {

            long topicId = entry.getKey();
            long limitedBefore = apiClient.backpressureResponses();

            Map<Long, Long> postIdByNumber = postIdsForTopic(topicId);

            if (postIdByNumber == null) {
                // The query failed. Every post wanted from this topic is unresolved, and the run
                // carries on - one bad topic must not cost the others.
                resolution.unresolved.addAll(entry.getValue());
            } else {
                for (TopicPost topicPost : entry.getValue()) {
                    Long postId = postIdByNumber.get(topicPost.postNumber);
                    if (postId == null) {
                        resolution.unresolved.add(topicPost);
                    } else {
                        resolution.postIds.add(postId);
                        resolution.resolved.add(topicPost);
                    }
                }
            }

            topicsDone++;
            if ((topicsDone % PROGRESS_INTERVAL) == 0) {
                LOGGER.info("Resolved {} of {} topic(s), {} post id(s) so far",
                        topicsDone, byTopic.size(), resolution.postIds.size());
            }

            paceAfter(limitedBefore);
        }

        LOGGER.warn("Resolved {} of {} wanted post(s)", resolution.postIds.size(), wanted.size());

        if (! resolution.unresolved.isEmpty()) {
            LOGGER.warn("{} post(s) could not be resolved - the commonest reason is that the post no"
                    + " longer holds a Discourse-hosted image, including because it has already been"
                    + " soft deleted: {}",
                    resolution.unresolved.size(), abbreviate(resolution.unresolved));
        }

        reportPacing();
        return resolution;
    }

    // Null means the query failed, which is different from a topic with no image posts.
    private Map<Long, Long> postIdsForTopic(final long topicId) {

        List<ImageRecord> images;
        try {
            images = new TopicImages(apiClient, topicId).getImages();
            consecutiveFailures = 0;
        } catch (RuntimeException ex) {
            // One unreadable topic must not cost the other hundreds, but a run of them means the
            // site is unwell rather than the topic being odd, and grinding through the rest -
            // each with ApiClient's ten retries behind it - helps nobody.
            consecutiveFailures++;
            LOGGER.warn("Could not read topic {}: {}", topicId, ex.getMessage());

            if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                throw new MemberDataException(consecutiveFailures + " topics in a row could not be"
                        + " read, the last being " + topicId + ": " + ex.getMessage()
                        + " Giving up rather than working through the rest.");
            }

            return null;
        }

        // A post with several uploads is several rows sharing one post id.
        Map<Long, Long> postIdByNumber = new LinkedHashMap<>();
        for (ImageRecord image : images) {
            postIdByNumber.putIfAbsent(image.postNumber, image.postId);
        }

        return postIdByNumber;
    }

    // Same shape as PostDeleter.paceAfter - hundreds of data explorer queries get rate limited too.
    private void paceAfter(final long limitedBefore) {

        long was = pacer.napMillis();
        long now = pacer.observe(apiClient.backpressureResponses() > limitedBefore);

        if (now != was) {
            LOGGER.warn("Pacing {} to {}ms (was {}ms); {} throttled or failed request(s) so far",
                    (now > was) ? "up" : "down", now, was, pacer.backoffs());
        }

        nap(now);
    }

    private void reportPacing() {

        if (pacer.backoffs() == 0) {
            return;
        }

        LOGGER.warn("Backed off from rate limiting {} time(s), pacing now {}ms between queries."
                + " Start the next run with -Dmemberdata.resolveNapMillis={} to skip re-learning it.",
                pacer.backoffs(), pacer.napMillis(), pacer.napMillis());
    }

    private void nap(long milliseconds) {
        if (milliseconds <= 0) {
            return;
        }
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** The pacing this run settled on. Test support. */
    AdaptivePacer pacer() {
        return pacer;
    }

    /**
     * Write the resolved post ids where delete-posts can read them, and any unresolved pairs to a
     * companion file - which is itself a valid input to this command, so a fixed up subset can be
     * re-run.
     */
    static void write(final Resolution resolution, final Path output) {

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(COLUMN_POST_ID, COLUMN_POST_NUMBER, COLUMN_TOPIC_ID));

        for (int index = 0; index < resolution.postIds.size(); index++) {
            TopicPost topicPost = resolution.resolved.get(index);
            rows.add(List.of(String.valueOf(resolution.postIds.get(index)),
                    String.valueOf(topicPost.postNumber), String.valueOf(topicPost.topicId)));
        }

        writeCsv(output, rows);
        LOGGER.warn("Wrote {} post id(s) to {}", resolution.postIds.size(), output);

        if (resolution.unresolved.isEmpty()) {
            return;
        }

        List<List<String>> unresolvedRows = new ArrayList<>();
        unresolvedRows.add(List.of(COLUMN_TOPIC_ID, COLUMN_POST_NUMBER));
        for (TopicPost topicPost : resolution.unresolved) {
            unresolvedRows.add(List.of(String.valueOf(topicPost.topicId),
                    String.valueOf(topicPost.postNumber)));
        }

        Path unresolved = unresolvedFor(output);
        writeCsv(unresolved, unresolvedRows);
        LOGGER.warn("Wrote {} unresolved post(s) to {} - review these before deleting anything",
                resolution.unresolved.size(), unresolved);
    }

    private static void writeCsv(final Path file, final List<List<String>> rows) {
        try (CSVListWriter writer = new CSVListWriter(new FileWriter(file.toFile()))) {
            writer.writeAllToList(rows);
        } catch (IOException ex) {
            throw new MemberDataException("Could not write " + file + ": " + ex.getMessage());
        }
    }
}
