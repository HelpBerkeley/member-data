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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists the Discourse-hosted images in every topic of a category.
 *
 * One QUERY_GET_TOPIC_IMAGES data explorer query per topic, so a category of hundreds of topics is
 * hundreds of requests - the same shape of run as PostIdResolver, and paced the same way. Each of
 * those queries joins posts to upload_references to uploads, so they are not cheap reads, and
 * firing them off as fast as the site will answer just collects a rate limit refusal every time the
 * site's request window rolls.
 *
 * A topic that cannot be read is reported and skipped, since listing is read-only and one
 * unreadable topic tells us nothing about the rest.
 */
public class CategoryImages {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryImages.class);

    // Starting milliseconds between queries, and the floor the adaptive pacing will not go below.
    // The default keeps a run just under the 60 requests per minute that a default Discourse
    // allows an admin API user - see PostDeleter.NAP_MILLISECONDS for the measurement.
    static long NAP_MILLISECONDS = Long.getLong("memberdata.listNapMillis", 1100L);
    // How often to log progress. Hundreds of queries is several minutes with nothing else to see,
    // and a paced run spends most of its time asleep.
    private static final int PROGRESS_INTERVAL = 25;
    static final int MAX_CONSECUTIVE_FAILURES = 5;

    static final String COLUMN_TOPIC_ID = "topic_id";
    static final String COLUMN_TOPIC_NAME = "topic_name";
    static final String COLUMN_POST_NUMBER = "post_number";
    static final String COLUMN_POST_ID = "post_id";
    static final String COLUMN_IMAGE_NAME = "image_name";

    private final ApiClient apiClient;
    private final String categoryName;
    // Reads NAP_MILLISECONDS once, here - see PostDeleter for why that matters to the tests.
    private final AdaptivePacer pacer = new AdaptivePacer(NAP_MILLISECONDS);
    private final List<Long> unreadableTopics = new ArrayList<>();
    private int consecutiveFailures = 0;

    // One row at a time through the same writer the CSV files this program writes go through, so
    // the quoting matches them - and so a topic title containing a comma, which several of them
    // do, cannot split a row.
    private final StringWriter rowBuffer = new StringWriter();
    private final CSVListWriter csvWriter = new CSVListWriter(rowBuffer);

    CategoryImages(final ApiClient apiClient, final String categoryName) {
        this.apiClient = apiClient;
        this.categoryName = categoryName;
    }

    /**
     * List every Discourse-hosted image in the category as CSV on stdout, a header and then one row
     * per image.
     *
     * The rows carry both post_id and post_number, so delete-posts reads this output directly - it
     * collapses the duplicate post ids that a post with several uploads produces, and skips any
     * first post.
     *
     * The rows are printed as they are found rather than at the end - this is a long run, and
     * waiting for it to finish before showing anything would make a rate limit pause look like a
     * hang.
     *
     * @return the lines printed, header first
     */
    List<String> list() {

        List<String> lines = new ArrayList<>();

        // Ahead of the empty check, so the command always emits a readable CSV. A file of no rows
        // and no header fails downstream as a missing post_id column rather than as no posts.
        lines.add(printed(csvLine(List.of(COLUMN_TOPIC_ID, COLUMN_TOPIC_NAME, COLUMN_POST_NUMBER,
                COLUMN_POST_ID, COLUMN_IMAGE_NAME))));

        List<Topic> topics = new CategoryTopics(apiClient, categoryName).getTopics();

        if (topics.isEmpty()) {
            LOGGER.info("No topics found in category {}", categoryName);
            return lines;
        }

        LOGGER.warn("Listing the images in {} topic(s) of category {}", topics.size(), categoryName);

        long totalBytes = 0;
        int topicsDone = 0;

        for (Topic topic : topics) {
            long limitedBefore = apiClient.backpressureResponses();

            for (ImageRecord image : imagesFor(topic)) {
                lines.add(printed(csvLine(List.of(
                        String.valueOf(topic.getId()),
                        topic.getName(),
                        String.valueOf(image.postNumber),
                        String.valueOf(image.postId),
                        image.originalFilename))));
                totalBytes += image.filesize;
            }

            topicsDone++;
            if ((topicsDone % PROGRESS_INTERVAL) == 0) {
                LOGGER.info("Listed {} of {} topic(s), {} image(s) so far",
                        topicsDone, topics.size(), lines.size() - 1);
            }

            paceAfter(limitedBefore);
        }

        // Minus the header row. The byte total is not a column - it is only ever a whole-run
        // question, and this is where it gets answered.
        LOGGER.warn("{} image(s) totalling {} bytes in {} topic(s) of category {}",
                lines.size() - 1, totalBytes, topics.size(), categoryName);

        if (! unreadableTopics.isEmpty()) {
            LOGGER.warn("{} topic(s) could not be read and are missing from this listing: {}",
                    unreadableTopics.size(), unreadableTopics);
        }

        reportPacing();
        return lines;
    }

    /**
     * One CSV line, quoted the way the CSV files this program writes are quoted - which is to say
     * every field, so the line always ends in the quote character and stripping the writer's line
     * terminator cannot eat anything else.
     */
    private String csvLine(final List<String> row) {
        rowBuffer.getBuffer().setLength(0);
        csvWriter.writeNextToList(row);
        csvWriter.flushQuietly();
        return rowBuffer.toString().strip();
    }

    private String printed(final String line) {
        System.out.println(line);
        return line;
    }

    /** An unreadable topic contributes no images rather than ending the run. */
    private List<ImageRecord> imagesFor(final Topic topic) {

        try {
            List<ImageRecord> images = new TopicImages(apiClient, topic.getId()).getImages();
            consecutiveFailures = 0;
            return images;
        } catch (RuntimeException ex) {
            // One unreadable topic must not cost the other hundreds, but a run of them means the
            // site is unwell rather than the topic being odd, and grinding through the rest - each
            // with ApiClient's ten retries behind it - helps nobody.
            consecutiveFailures++;
            unreadableTopics.add(topic.getId());
            LOGGER.warn("Could not read topic {}: {}", topic.getId(), ex.getMessage());

            if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                throw new MemberDataException(consecutiveFailures + " topics in a row could not be"
                        + " read, the last being " + topic.getId() + ": " + ex.getMessage()
                        + " Giving up rather than working through the rest.");
            }

            return List.of();
        }
    }

    // Same shape as PostIdResolver.paceAfter - the same query, once per topic, at the same scale.
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
                + " Start the next run with -Dmemberdata.listNapMillis={} to skip re-learning it.",
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

    /** The topics whose query failed, and so are missing from the listing. Test support. */
    List<Long> unreadableTopics() {
        return unreadableTopics;
    }
}
