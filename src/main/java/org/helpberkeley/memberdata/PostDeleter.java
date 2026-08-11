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

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

/**
 * Permanently deletes posts, in two passes over the whole batch: soft delete everything, then
 * force_destroy everything.
 *
 * The two passes span the entire batch on purpose. Discourse refuses to permanently delete a post
 * until its timer since the soft delete expires, so deleting a handful of posts at a time means
 * waiting out that timer once per batch - which at hundreds of small batches dominates everything
 * else. Run as one batch, the soft delete pass takes longer than the timer, and no post is still
 * inside its window by the time the second pass reaches it.
 *
 * Progress is journalled, because a soft deleted post is invisible to the data explorer queries
 * that identify posts to delete (they filter deleted_at IS NULL). A run interrupted between the
 * passes would otherwise strand those posts: still on the site, no longer findable by this tool.
 */
public class PostDeleter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostDeleter.class);

    static final String COLUMN_POST_ID = "post_id";
    static final String COLUMN_POST_NUMBER = "post_number";

    static final String JOURNAL_SUFFIX = ".journal";
    static final String SOFT_DELETED = "SOFT";
    static final String DESTROYED = "DONE";

    // Starting milliseconds between requests, and the floor the adaptive pacing will not go below.
    // Deliberately tunable without a rebuild - the useful value depends on the site's
    // max_admin_api_reqs_per_minute setting.
    //
    // The default keeps a run just under that setting's own default of 60 requests per minute,
    // measured against go.helpberkeley.org: it is a fixed one minute window, and exceeding it
    // costs a wait until the window rolls, which is far more than the pacing ever saves.
    static long NAP_MILLISECONDS = Long.getLong("memberdata.deleteNapMillis", 1100L);
    static long PERMANENT_DELETE_WAIT_MILLISECONDS =
            TimeUnit.MINUTES.toMillis(5) + TimeUnit.SECONDS.toMillis(10);
    // Rough per request round trip, for the dry run's time estimate only.
    private static final long ESTIMATED_REQUEST_MILLISECONDS = 110;

    private final ApiClient apiClient;
    private final List<Long> postIds;
    private final Path journal;
    // Reads NAP_MILLISECONDS once, here - a test changing the static after constructing a
    // PostDeleter will not affect it.
    private final AdaptivePacer pacer = new AdaptivePacer(NAP_MILLISECONDS);
    private final Set<Long> failed = new LinkedHashSet<>();
    private int consecutiveFailures = 0;

    /**
     * @param postIds the posts to delete, de-duplicated, first posts already excluded
     * @param journal file to record progress in, so an interrupted run can resume. May be null,
     *                for batches small enough that redoing the whole thing is cheap.
     */
    PostDeleter(final ApiClient apiClient, final List<Long> postIds, final Path journal) {
        this.apiClient = apiClient;
        this.postIds = postIds;
        this.journal = journal;
    }

    /**
     * Read the posts to delete from a CSV file with post_id and post_number columns, as exported
     * from a data explorer query. Rows for a topic's first post are dropped - deleting one deletes
     * the entire topic - and duplicate post ids are collapsed.
     */
    static List<Long> readPostIds(final String fileName) {

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
        int postIdIndex = headers.indexOf(COLUMN_POST_ID);
        int postNumberIndex = headers.indexOf(COLUMN_POST_NUMBER);

        if (postIdIndex == -1) {
            throw new MemberDataException(fileName + " has no " + COLUMN_POST_ID + " column. Found: "
                    + String.join(", ", headers));
        }
        if (postNumberIndex == -1) {
            throw new MemberDataException(fileName + " has no " + COLUMN_POST_NUMBER + " column, which is"
                    + " required to avoid deleting a topic's first post. Found: " + String.join(", ", headers));
        }

        Set<Long> postIds = new LinkedHashSet<>();
        List<Long> firstPosts = new ArrayList<>();
        int duplicates = 0;

        for (int row = 1; row < rows.size(); row++) {
            List<String> columns = rows.get(row);

            if (columns.size() <= Math.max(postIdIndex, postNumberIndex)) {
                throw new MemberDataException(fileName + " line " + (row + 1) + " has "
                        + columns.size() + " column(s), too few for " + headers.size() + " headers");
            }

            long postId = parseLong(fileName, row + 1, COLUMN_POST_ID, columns.get(postIdIndex));
            long postNumber = parseLong(fileName, row + 1, COLUMN_POST_NUMBER, columns.get(postNumberIndex));

            if (postNumber == 1) {
                firstPosts.add(postId);
            } else if (! postIds.add(postId)) {
                duplicates++;
            }
        }

        if (! firstPosts.isEmpty()) {
            LOGGER.warn("Skipping {} post(s) that are their topic's first post - deleting one deletes the"
                    + " whole topic, every reply included. Post id(s): {}",
                    firstPosts.size(), abbreviate(firstPosts));
        }
        if (duplicates > 0) {
            LOGGER.info("Collapsed {} duplicate row(s) - a post with several images is still one post",
                    duplicates);
        }

        return new ArrayList<>(postIds);
    }

    // Batches run to thousands of posts. Logging every id makes the line unreadable and buries
    // everything around it, so show a sample and leave the full set to the input file.
    private static final int MAX_LOGGED_IDS = 20;
    static final int MAX_CONSECUTIVE_FAILURES = 5;

    private static String abbreviate(final List<Long> postIds) {
        if (postIds.size() <= MAX_LOGGED_IDS) {
            return postIds.toString();
        }
        return postIds.subList(0, MAX_LOGGED_IDS) + " ... and " + (postIds.size() - MAX_LOGGED_IDS)
                + " more";
    }

    private static long parseLong(String fileName, int line, String column, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new MemberDataException(fileName + " line " + line + ": " + column
                    + " is not numeric: " + value);
        }
    }

    /** Journal file for a post list, alongside it. */
    static Path journalFor(final String fileName) {
        return Path.of(fileName + JOURNAL_SUFFIX);
    }

    /**
     * force == false is a dry run: report what would be deleted and delete nothing.
     *
     * @return the ids of the posts that were (dry run: would be) deleted
     */
    List<Long> deletePosts(final boolean force) {

        if (postIds.isEmpty()) {
            LOGGER.info("No posts to delete");
            return List.of();
        }

        if (! force) {
            long millis = 2L * postIds.size() * (NAP_MILLISECONDS + ESTIMATED_REQUEST_MILLISECONDS);
            LOGGER.warn("DRY RUN - nothing was deleted. {} post(s) would be permanently deleted,"
                    + " in two passes of {} requests each, taking at least {} minutes at {}ms"
                    + " starting pacing - longer if the site rate limits.",
                    postIds.size(), postIds.size(), TimeUnit.MILLISECONDS.toMinutes(millis),
                    NAP_MILLISECONDS);
            LOGGER.warn("Re-run with the \"{}\" argument to delete them.", Options.FORCE_ARGUMENT);
            return postIds;
        }

        Set<Long> softDeleted = new LinkedHashSet<>();
        Set<Long> destroyed = new LinkedHashSet<>();
        readJournal(softDeleted, destroyed);

        if ((! softDeleted.isEmpty()) || (! destroyed.isEmpty())) {
            LOGGER.warn("Resuming from {}: {} post(s) already soft deleted, {} already destroyed",
                    journal, softDeleted.size(), destroyed.size());
        }

        // A run of this length can end three ways, and the pacing it learned is worth reporting
        // from all of them - it is the number to start the next run with. Ctrl-C is the reason for
        // the hook: the passes below are mostly spent asleep, so an interrupt is likely to land
        // there, and without this the operator loses everything the run worked out.
        Thread interruptReport = new Thread(() -> reportPacing("Interrupted"));
        Runtime.getRuntime().addShutdownHook(interruptReport);

        try {
            softDeletePass(softDeleted, destroyed);
            destroyPass(destroyed);
        } catch (RuntimeException ex) {
            reportFailures();
            reportPacing("Run stopped early");
            throw ex;
        } finally {
            removeShutdownHook(interruptReport);
        }

        LOGGER.warn("Permanently deleted {} post(s)", postIds.size() - failed.size());
        reportFailures();
        reportPacing("Run complete");

        return postIds;
    }

    // Quiet when the site never rate limited us - there is nothing to carry to the next run.
    private void reportPacing(final String when) {

        if (pacer.backoffs() == 0) {
            return;
        }

        LOGGER.warn("{}: backed off from rate limiting {} time(s), pacing now {}ms between"
                + " requests. Start the next run with -Dmemberdata.deleteNapMillis={} to skip"
                + " re-learning it.", when, pacer.backoffs(), pacer.napMillis(), pacer.napMillis());
    }

    private static void removeShutdownHook(final Thread hook) {
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException alreadyShuttingDown) {
            // The hook is running right now. Nothing to remove.
        }
    }

    /** The pacing this run settled on. Test support. */
    AdaptivePacer pacer() {
        return pacer;
    }

    private void softDeletePass(final Set<Long> softDeleted, final Set<Long> destroyed) {

        List<Long> todo = new ArrayList<>();
        for (long postId : postIds) {
            if ((! softDeleted.contains(postId)) && (! destroyed.contains(postId))) {
                todo.add(postId);
            }
        }

        LOGGER.warn("Soft deleting {} post(s)", todo.size());
        int done = 0;
        for (long postId : todo) {
            long limitedBefore = apiClient.backpressureResponses();

            try {
                int statusCode = apiClient.deletePost(postId, false).statusCode();
                journal(postId, SOFT_DELETED);
                done++;
                noteSuccess();
                LOGGER.info("Soft deleted post {}: {} ({} of {})",
                        postId, statusCode, done, todo.size());
            } catch (MemberDataException ex) {
                // One post that will not delete must not cost the other thousands. It is left
                // out of the destroy pass and reported at the end. Deliberately not every
                // RuntimeException - a bug in this program must still stop the run.
                noteFailure(postId, ex);
            }

            paceAfter(limitedBefore);
        }

        reportPacing("Soft delete pass complete");
    }

    private void destroyPass(final Set<Long> destroyed) {

        List<Long> todo = new ArrayList<>();
        for (long postId : postIds) {
            // A post whose soft delete failed was never deleted, so force_destroy would be
            // refused for the wrong reason. It is already recorded as failed.
            if ((! destroyed.contains(postId)) && (! failed.contains(postId))) {
                todo.add(postId);
            }
        }

        LOGGER.warn("Permanently deleting {} post(s)", todo.size());
        boolean waited = false;
        int done = 0;

        for (long postId : todo) {
            long limitedBefore = apiClient.backpressureResponses();
            HttpResponse<String> response;

            try {
                response = apiClient.deletePost(postId, true);
            } catch (MemberDataException ex) {
                noteFailure(postId, ex);
                paceAfter(limitedBefore);
                continue;
            }

            // Only reached when the batch is small enough that the soft delete pass took less time
            // than Discourse's timer. One wait covers the whole batch - they were all soft deleted
            // in the pass above, so the rest are past the timer by the time their turn comes.
            if ((response.statusCode() == HTTP_FORBIDDEN) && (! waited)) {
                waited = true;
                LOGGER.warn("Permanent delete refused. Discourse refuses one for a while after the same"
                        + " user soft deleted the post - waiting {} seconds, then retrying.",
                        TimeUnit.MILLISECONDS.toSeconds(PERMANENT_DELETE_WAIT_MILLISECONDS));
                nap(PERMANENT_DELETE_WAIT_MILLISECONDS);

                try {
                    response = apiClient.deletePost(postId, true);
                } catch (MemberDataException ex) {
                    noteFailure(postId, ex);
                    paceAfter(limitedBefore);
                    continue;
                }
            }

            if (response.statusCode() == HTTP_FORBIDDEN) {
                // Report what Discourse said. The refusals - not an admin, site setting disabled,
                // timer not expired - are indistinguishable without it.
                throw new MemberDataException("Permanent delete of post " + postId + " was refused by"
                        + " Discourse: " + response.body()
                        + " The API user must be an admin, the can_permanently_delete site setting"
                        + " must be enabled, and Discourse's post deletion timer must have expired."
                        + (journal == null ? "" : " Progress is recorded in " + journal
                        + " - re-running resumes from there."));
            }

            journal(postId, DESTROYED);
            done++;
            noteSuccess();
            LOGGER.info("Permanently deleted post {}: {} ({} of {})",
                    postId, response.statusCode(), done, todo.size());
            paceAfter(limitedBefore);
        }
    }

    private void noteSuccess() {
        consecutiveFailures = 0;
    }

    // A post that will not delete is recorded and skipped. A run of them means the site is unwell
    // rather than the post being odd, and working through thousands more helps nobody.
    private void noteFailure(final long postId, final MemberDataException ex) {

        failed.add(postId);
        consecutiveFailures++;
        LOGGER.warn("Post {} could not be deleted, skipping it: {}", postId, ex.getMessage());

        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
            throw new MemberDataException(consecutiveFailures + " posts in a row could not be"
                    + " deleted, the last being " + postId + ": " + ex.getMessage()
                    + " Giving up rather than working through the rest."
                    + (journal == null ? "" : " Progress is recorded in " + journal
                    + " - re-running resumes from there."));
        }
    }

    private void reportFailures() {

        if (failed.isEmpty()) {
            return;
        }

        LOGGER.warn("{} post(s) could not be deleted and were skipped: {}",
                failed.size(), abbreviate(new ArrayList<>(failed)));
    }

    // Pace from the backpressure the site actually applied, not from a guess made before the run
    // started. ApiClient absorbs a 429 or a server error by retrying, so the counter is the only
    // evidence it happened.
    private void paceAfter(final long limitedBefore) {

        long was = pacer.napMillis();
        long now = pacer.observe(apiClient.backpressureResponses() > limitedBefore);

        if (now != was) {
            // Which direction, and the running total - otherwise gauging how much pushback the
            // run is getting means counting these lines by eye.
            LOGGER.warn("Pacing {} to {}ms (was {}ms); {} throttled or failed request(s) so far",
                    (now > was) ? "up" : "down", now, was, pacer.backoffs());
        }

        nap(now);
    }

    private void readJournal(final Set<Long> softDeleted, final Set<Long> destroyed) {

        if ((journal == null) || (! Files.exists(journal))) {
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(journal, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new MemberDataException("Could not read journal " + journal + ": " + ex.getMessage());
        }

        for (String line : lines) {
            // A run killed mid-write can leave a partial last line. Ignore anything malformed
            // rather than failing - the worst case is redoing one delete, which is harmless.
            String[] fields = line.split(",");
            if (fields.length != 2) {
                continue;
            }
            try {
                long postId = Long.parseLong(fields[0].trim());
                if (fields[1].trim().equals(DESTROYED)) {
                    destroyed.add(postId);
                } else if (fields[1].trim().equals(SOFT_DELETED)) {
                    softDeleted.add(postId);
                }
            } catch (NumberFormatException ignored) { }
        }
    }

    // Appended and flushed per post, so an interrupted run loses at most the last entry.
    private void journal(final long postId, final String state) {

        if (journal == null) {
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(journal, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(postId + "," + state);
            writer.newLine();
        } catch (IOException ex) {
            throw new MemberDataException("Could not write journal " + journal + ": " + ex.getMessage());
        }
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
}
