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
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class PostDeleterTest extends TestBase {

    private Path postList;

    @Before
    public void setup() {
        HttpClientSimulator.clearDeleteRequests();
        PostDeleter.NAP_MILLISECONDS = 0;
        PostDeleter.PERMANENT_DELETE_WAIT_MILLISECONDS = 0;
        postList = null;
    }

    @After
    public void cleanup() throws IOException {
        // The pacing tests raise these. Surefire reuses one fork, so leaving them raised makes
        // unrelated test classes sleep.
        PostDeleter.NAP_MILLISECONDS = 0;
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        if (postList != null) {
            Files.deleteIfExists(PostDeleter.journalFor(postList.toString()));
            Files.deleteIfExists(postList);
        }
    }

    private Path writePostList(final String contents) throws IOException {
        postList = Files.createTempFile("post-list", ".csv");
        Files.writeString(postList, contents, StandardCharsets.UTF_8);
        return postList;
    }

    @Test
    public void readPostIdsTest() throws IOException {
        Path file = writePostList("post_id,post_number\n71301,32\n71304,33\n");

        assertThat(PostDeleter.readPostIds(file.toString())).containsExactly(71301L, 71304L);
    }

    @Test
    public void readPostIdsSkipsFirstPostsTest() throws IOException {
        // Post 71300 is its topic's first post - deleting it would delete the whole topic.
        Path file = writePostList("post_id,post_number\n71300,1\n71301,32\n71304,33\n");

        assertThat(PostDeleter.readPostIds(file.toString())).containsExactly(71301L, 71304L);
    }

    @Test
    public void readPostIdsCollapsesDuplicatesTest() throws IOException {
        // A post with two images is two rows, but one post to delete.
        Path file = writePostList("post_id,post_number\n71301,32\n71301,32\n71304,33\n");

        assertThat(PostDeleter.readPostIds(file.toString())).containsExactly(71301L, 71304L);
    }

    @Test
    public void readPostIdsIgnoresExtraColumnsTest() throws IOException {
        // A full data explorer export, with the needed columns in an arbitrary position.
        Path file = writePostList("topic_id,post_number,upload_id,post_id\n"
                + "11102,2,17107,200731\n11102,5,17108,200734\n");

        assertThat(PostDeleter.readPostIds(file.toString())).containsExactly(200731L, 200734L);
    }

    @Test
    public void readPostIdsMissingPostNumberColumnTest() throws IOException {
        Path file = writePostList("post_id\n71301\n");

        Throwable thrown = catchThrowable(() -> PostDeleter.readPostIds(file.toString()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(PostDeleter.COLUMN_POST_NUMBER);
        assertThat(thrown).hasMessageContaining("first post");
    }

    @Test
    public void readPostIdsMissingPostIdColumnTest() throws IOException {
        Path file = writePostList("topic_id,post_number\n11102,2\n");

        Throwable thrown = catchThrowable(() -> PostDeleter.readPostIds(file.toString()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(PostDeleter.COLUMN_POST_ID);
    }

    @Test
    public void readPostIdsNotNumericTest() throws IOException {
        Path file = writePostList("post_id,post_number\n71301,32\nnot-a-number,33\n");

        Throwable thrown = catchThrowable(() -> PostDeleter.readPostIds(file.toString()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("not-a-number");
        assertThat(thrown).hasMessageContaining("line 3");
    }

    @Test
    public void deletePostsDryRunTest() {
        ApiClient apiClient = createApiSimulator();
        PostDeleter deleter = new PostDeleter(apiClient, List.of(71301L, 71304L), null);

        assertThat(deleter.deletePosts(false)).containsExactly(71301L, 71304L);
        assertThat(HttpClientSimulator.getDeleteRequests()).isEmpty();
    }

    @Test
    public void deletePostsTwoPassesTest() {
        ApiClient apiClient = createApiSimulator();
        PostDeleter deleter = new PostDeleter(apiClient, List.of(71301L, 71304L), null);

        deleter.deletePosts(true);

        // Every post soft deleted first, then every post destroyed. The passes must not interleave -
        // Discourse's timer runs from the soft delete, so the gap between them is the whole point.
        assertThat(HttpClientSimulator.getDeleteRequests()).containsExactly(
                Constants.POSTS_BASE + "71301",
                Constants.POSTS_BASE + "71304",
                Constants.POSTS_BASE + "71301" + Constants.FORCE_DESTROY,
                Constants.POSTS_BASE + "71304" + Constants.FORCE_DESTROY);
    }

    @Test
    public void journalRecordsProgressTest() throws IOException {
        Path file = writePostList("post_id,post_number\n71301,32\n71304,33\n");
        Path journal = PostDeleter.journalFor(file.toString());
        ApiClient apiClient = createApiSimulator();

        new PostDeleter(apiClient, PostDeleter.readPostIds(file.toString()), journal).deletePosts(true);

        assertThat(Files.readAllLines(journal)).containsExactly(
                "71301," + PostDeleter.SOFT_DELETED,
                "71304," + PostDeleter.SOFT_DELETED,
                "71301," + PostDeleter.DESTROYED,
                "71304," + PostDeleter.DESTROYED);
    }

    @Test
    public void resumeSkipsCompletedPostsTest() throws IOException {
        Path file = writePostList("post_id,post_number\n71301,32\n71304,33\n");
        Path journal = PostDeleter.journalFor(file.toString());
        // 71301 finished entirely last run; 71304 was soft deleted but not destroyed.
        Files.writeString(journal, "71301,SOFT\n71301,DONE\n71304,SOFT\n", StandardCharsets.UTF_8);
        ApiClient apiClient = createApiSimulator();

        new PostDeleter(apiClient, PostDeleter.readPostIds(file.toString()), journal).deletePosts(true);

        // Only the one outstanding force destroy is issued. Nothing is soft deleted twice, and
        // 71301 is not touched at all.
        assertThat(HttpClientSimulator.getDeleteRequests()).containsExactly(
                Constants.POSTS_BASE + "71304" + Constants.FORCE_DESTROY);
    }

    @Test
    public void resumeIgnoresPartialJournalLineTest() throws IOException {
        Path file = writePostList("post_id,post_number\n71301,32\n");
        Path journal = PostDeleter.journalFor(file.toString());
        // A run killed mid-write. The truncated line must not abort the resume.
        Files.writeString(journal, "71301,SOFT\n7130", StandardCharsets.UTF_8);
        ApiClient apiClient = createApiSimulator();

        new PostDeleter(apiClient, PostDeleter.readPostIds(file.toString()), journal).deletePosts(true);

        assertThat(HttpClientSimulator.getDeleteRequests()).containsExactly(
                Constants.POSTS_BASE + "71301" + Constants.FORCE_DESTROY);
    }

    @Test
    public void forbiddenReportsJournalTest() throws IOException {
        Path file = writePostList("post_id,post_number\n71301,32\n");
        Path journal = PostDeleter.journalFor(file.toString());
        String refusal = "{\"errors\":[\"You must wait 4 minutes before permanently deleting this post"
                + " or a different administrator must do it.\"]}";
        HttpClientSimulator.setDeleteResponseStatus(71301, HTTP_FORBIDDEN, refusal);
        ApiClient apiClient = createApiSimulator();
        PostDeleter deleter =
                new PostDeleter(apiClient, PostDeleter.readPostIds(file.toString()), journal);

        Throwable thrown = catchThrowable(() -> deleter.deletePosts(true));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(refusal);
        // The operator has to be told where to resume from.
        assertThat(thrown).hasMessageContaining(journal.toString());
    }

    @Test
    public void crashMidRunThenResumeTest() throws IOException {
        Path file = writePostList("post_id,post_number\n71301,32\n71304,33\n71305,34\n");
        Path journal = PostDeleter.journalFor(file.toString());
        List<Long> postIds = PostDeleter.readPostIds(file.toString());

        // Die partway through: all 3 soft deleted, then 1 destroyed.
        HttpClientSimulator.failAfterDeletes(4);
        ApiClient apiClient = createApiSimulator();
        Throwable thrown = catchThrowable(
                () -> new PostDeleter(apiClient, postIds, journal).deletePosts(true));
        assertThat(thrown).isNotNull();
        assertThat(Files.readAllLines(journal)).hasSize(4);

        // Resume. Only the two outstanding force destroys are issued - nothing is soft deleted
        // again, and the post that completed is not touched.
        HttpClientSimulator.clearDeleteRequests();
        new PostDeleter(createApiSimulator(), postIds, journal).deletePosts(true);

        assertThat(HttpClientSimulator.getDeleteRequests()).containsExactly(
                Constants.POSTS_BASE + "71304" + Constants.FORCE_DESTROY,
                Constants.POSTS_BASE + "71305" + Constants.FORCE_DESTROY);
        assertThat(Files.readAllLines(journal)).hasSize(6);
    }

    @Test
    public void rateLimitingBacksOffThePaceTest() {
        // A non zero floor, so the pacer is live. It reads the static at construction, so this has
        // to be set before the PostDeleter is built - and put back, or other classes start sleeping.
        PostDeleter.NAP_MILLISECONDS = 1;
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.TOO_MANY_TIMES_429_RESULT, 1);
        ApiClient apiClient = createApiSimulator();
        PostDeleter deleter = new PostDeleter(apiClient, List.of(71301L, 71304L), null);

        // ApiClient absorbs the 429 by retrying, so the run still completes.
        deleter.deletePosts(true);

        assertThat(HttpClientSimulator.getDeleteRequests()).hasSize(4);
        assertThat(deleter.pacer().backoffs()).isEqualTo(1);
        assertThat(deleter.pacer().napMillis()).isEqualTo(AdaptivePacer.MINIMUM_BACKOFF_MILLISECONDS);
    }

    @Test
    public void pacingSurvivesAnAbortedRunTest() {
        PostDeleter.NAP_MILLISECONDS = 1;
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.TOO_MANY_TIMES_429_RESULT, 1);
        // Die in the destroy pass, after the soft delete pass has already backed off.
        HttpClientSimulator.failAfterDeletes(1);
        ApiClient apiClient = createApiSimulator();
        PostDeleter deleter = new PostDeleter(apiClient, List.of(71301L), null);

        assertThat(catchThrowable(() -> deleter.deletePosts(true))).isNotNull();

        // What the run learned is still readable after the abort. This is what the "Run stopped
        // early" report puts in the log, so a killed run does not lose it.
        assertThat(deleter.pacer().backoffs()).isEqualTo(1);
        assertThat(deleter.pacer().napMillis()).isEqualTo(AdaptivePacer.MINIMUM_BACKOFF_MILLISECONDS);
    }

    @Test
    public void cleanRunKeepsTheConfiguredPaceTest() {
        PostDeleter.NAP_MILLISECONDS = 1;
        ApiClient apiClient = createApiSimulator();
        PostDeleter deleter = new PostDeleter(apiClient, List.of(71301L, 71304L), null);

        deleter.deletePosts(true);

        assertThat(deleter.pacer().backoffs()).isEqualTo(0);
        assertThat(deleter.pacer().napMillis()).isEqualTo(1);
    }

    @Test
    public void unDeletablePostIsSkippedTest() {
        // Post 71304 answers 500 on every attempt, so ApiClient exhausts its retries and throws.
        HttpClientSimulator.setDeleteResponseStatus(71304, HTTP_INTERNAL_ERROR);
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        ApiClient apiClient = createApiSimulator();
        PostDeleter deleter =
                new PostDeleter(apiClient, List.of(71301L, 71304L, 71305L), null);

        deleter.deletePosts(true);

        // The other two posts complete both passes. The bad one is left out of the destroy pass
        // entirely - force_destroy on a post that was never soft deleted is refused for a reason
        // that has nothing to do with what went wrong.
        assertThat(HttpClientSimulator.getDeleteRequests()).contains(
                Constants.POSTS_BASE + "71301",
                Constants.POSTS_BASE + "71305",
                Constants.POSTS_BASE + "71301" + Constants.FORCE_DESTROY,
                Constants.POSTS_BASE + "71305" + Constants.FORCE_DESTROY);
        assertThat(HttpClientSimulator.getDeleteRequests())
                .doesNotContain(Constants.POSTS_BASE + "71304" + Constants.FORCE_DESTROY);
    }

    @Test
    public void tooManyUnDeletablePostsGivesUpTest() {
        List<Long> postIds = new ArrayList<>();
        for (long postId = 90001; postId <= 90001 + PostDeleter.MAX_CONSECUTIVE_FAILURES; postId++) {
            HttpClientSimulator.setDeleteResponseStatus(postId, HTTP_INTERNAL_ERROR);
            postIds.add(postId);
        }
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        PostDeleter deleter = new PostDeleter(createApiSimulator(), postIds, null);

        // A dead site should not be ground through one post at a time.
        Throwable thrown = catchThrowable(() -> deleter.deletePosts(true));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("in a row");
    }

    @Test
    public void emptyPostListTest() {
        ApiClient apiClient = createApiSimulator();

        assertThat(new PostDeleter(apiClient, List.of(), null).deletePosts(true)).isEmpty();
        assertThat(HttpClientSimulator.getDeleteRequests()).isEmpty();
    }
}
