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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class PostIdResolverTest extends TestBase {

    private final List<Path> tempFiles = new ArrayList<>();

    @Before
    public void setup() {
        PostIdResolver.NAP_MILLISECONDS = 0;
        HttpClientSimulator.clearQueryResponseData();
    }

    @After
    public void cleanup() throws IOException {
        // Surefire reuses one fork, so anything left queued or raised here leaks into the next
        // test - in this class or another one.
        PostIdResolver.NAP_MILLISECONDS = 0;
        HttpClientSimulator.clearQueryResponseData();
        for (Path file : tempFiles) {
            Files.deleteIfExists(file);
        }
        tempFiles.clear();
    }

    private Path tempFile(final String contents) throws IOException {
        Path file = Files.createTempFile("topic-posts", ".csv");
        tempFiles.add(file);
        Files.writeString(file, contents, StandardCharsets.UTF_8);
        return file;
    }

    private Path tempOutput() throws IOException {
        Path file = Files.createTempFile("post-ids", ".csv");
        tempFiles.add(file);
        tempFiles.add(PostIdResolver.unresolvedFor(file));
        return file;
    }

    /** A query 90 response. Rows are {postId, postNumber, uploadId}. */
    private static String imageQueryResult(final long topicId, final long[][] rows) {

        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"errors\": [], \"params\": {\"topic_id\": \"")
                .append(topicId).append("\"},")
                .append("\"columns\": [\"post_id\", \"post_number\", \"upload_id\","
                        + " \"original_filename\", \"extension\", \"filesize\", \"url\"],")
                .append("\"rows\": [");

        for (int index = 0; index < rows.length; index++) {
            if (index > 0) {
                json.append(",");
            }
            json.append("[").append(rows[index][0]).append(",").append(rows[index][1]).append(",")
                    .append(rows[index][2])
                    .append(",\"image.jpeg\",\"jpeg\",1234,\"//cdn.example.com/image.jpeg\"]");
        }

        return json.append("]}").toString();
    }

    private static void queueTopicImages(final long topicId, final long[][] rows) {
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_TOPIC_IMAGES, imageQueryResult(topicId, rows));
    }

    // ---- readTopicPosts ----

    @Test
    public void readTopicPostsTest() throws IOException {
        Path file = tempFile("topic_id,post_number\n7253,32\n7253,33\n8000,5\n");

        List<PostIdResolver.TopicPost> topicPosts = PostIdResolver.readTopicPosts(file.toString());

        assertThat(topicPosts).hasSize(3);
        assertThat(topicPosts.get(0).topicId).isEqualTo(7253);
        assertThat(topicPosts.get(0).postNumber).isEqualTo(32);
        assertThat(topicPosts.get(2).topicId).isEqualTo(8000);
    }

    @Test
    public void readTopicPostsIgnoresExtraColumnsTest() throws IOException {
        Path file = tempFile("upload_id,post_number,filesize,topic_id\n17107,2,999,11102\n");

        List<PostIdResolver.TopicPost> topicPosts = PostIdResolver.readTopicPosts(file.toString());

        assertThat(topicPosts).hasSize(1);
        assertThat(topicPosts.get(0).topicId).isEqualTo(11102);
        assertThat(topicPosts.get(0).postNumber).isEqualTo(2);
    }

    @Test
    public void readTopicPostsCollapsesDuplicatesTest() throws IOException {
        // A post with two images is two rows in the source query, but one post.
        Path file = tempFile("topic_id,post_number\n7253,32\n7253,32\n7253,33\n");

        assertThat(PostIdResolver.readTopicPosts(file.toString())).hasSize(2);
    }

    @Test
    public void readTopicPostsSkipsFirstPostsTest() throws IOException {
        Path file = tempFile("topic_id,post_number\n7253,1\n7253,32\n");

        List<PostIdResolver.TopicPost> topicPosts = PostIdResolver.readTopicPosts(file.toString());

        assertThat(topicPosts).hasSize(1);
        assertThat(topicPosts.get(0).postNumber).isEqualTo(32);
    }

    @Test
    public void readTopicPostsMissingTopicIdColumnTest() throws IOException {
        Path file = tempFile("post_id,post_number\n71301,32\n");

        Throwable thrown = catchThrowable(() -> PostIdResolver.readTopicPosts(file.toString()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(PostIdResolver.COLUMN_TOPIC_ID);
    }

    @Test
    public void readTopicPostsMissingPostNumberColumnTest() throws IOException {
        Path file = tempFile("topic_id\n7253\n");

        Throwable thrown = catchThrowable(() -> PostIdResolver.readTopicPosts(file.toString()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(PostIdResolver.COLUMN_POST_NUMBER);
    }

    @Test
    public void readTopicPostsNotNumericTest() throws IOException {
        Path file = tempFile("topic_id,post_number\n7253,32\nnot-a-number,33\n");

        Throwable thrown = catchThrowable(() -> PostIdResolver.readTopicPosts(file.toString()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("not-a-number");
        assertThat(thrown).hasMessageContaining("line 3");
    }

    @Test
    public void defaultOutputNameTest() {
        assertThat(PostIdResolver.defaultOutputFor("my-list.csv"))
                .isEqualTo(Path.of("my-list-post-ids.csv"));
        // No extension to replace.
        assertThat(PostIdResolver.defaultOutputFor("my-list"))
                .isEqualTo(Path.of("my-list-post-ids.csv"));
    }

    // ---- resolve ----

    @Test
    public void resolvesAcrossTopicsTest() {
        queueTopicImages(7253, new long[][] { {71300, 1, 11460}, {71301, 32, 11463}, {71304, 33, 11465} });
        queueTopicImages(8000, new long[][] { {80001, 5, 12000}, {80002, 9, 12001} });

        PostIdResolver resolver = new PostIdResolver(createApiSimulator(), List.of(
                new PostIdResolver.TopicPost(7253, 32),
                new PostIdResolver.TopicPost(7253, 33),
                new PostIdResolver.TopicPost(8000, 9)));

        PostIdResolver.Resolution resolution = resolver.resolve();

        assertThat(resolution.postIds).containsExactly(71301L, 71304L, 80002L);
        assertThat(resolution.unresolved).isEmpty();
    }

    @Test
    public void repeatedTopicIsQueriedOnceTest() {
        // Two wanted posts in one topic must not cost two queries. Only one response is queued -
        // a second query would fall through to the fixture file and resolve the wrong ids.
        queueTopicImages(7253, new long[][] { {71301, 32, 11463}, {71304, 33, 11465} });

        PostIdResolver resolver = new PostIdResolver(createApiSimulator(), List.of(
                new PostIdResolver.TopicPost(7253, 32),
                new PostIdResolver.TopicPost(7253, 33)));

        assertThat(resolver.resolve().postIds).containsExactly(71301L, 71304L);
    }

    @Test
    public void postWithSeveralUploadsResolvesOnceTest() {
        // Post 71301 has two uploads, so two rows, but it is one post.
        queueTopicImages(7253, new long[][] { {71301, 32, 11463}, {71301, 32, 11464} });

        PostIdResolver resolver = new PostIdResolver(createApiSimulator(),
                List.of(new PostIdResolver.TopicPost(7253, 32)));

        assertThat(resolver.resolve().postIds).containsExactly(71301L);
    }

    @Test
    public void unresolvedPostNumberDoesNotStopTheRunTest() {
        queueTopicImages(7253, new long[][] { {71301, 32, 11463} });
        queueTopicImages(8000, new long[][] { {80001, 5, 12000} });

        // Post #99 of topic 7253 holds no image - already deleted, or never had one.
        PostIdResolver resolver = new PostIdResolver(createApiSimulator(), List.of(
                new PostIdResolver.TopicPost(7253, 99),
                new PostIdResolver.TopicPost(7253, 32),
                new PostIdResolver.TopicPost(8000, 5)));

        PostIdResolver.Resolution resolution = resolver.resolve();

        assertThat(resolution.postIds).containsExactly(71301L, 80001L);
        assertThat(resolution.unresolved).hasSize(1);
        assertThat(resolution.unresolved.get(0).topicId).isEqualTo(7253);
        assertThat(resolution.unresolved.get(0).postNumber).isEqualTo(99);
    }

    @Test
    public void failedTopicQueryDoesNotStopTheRunTest() {
        // A response the parser cannot make sense of - no post_id column.
        HttpClientSimulator.setQueryResponseData(Constants.QUERY_GET_TOPIC_IMAGES,
                "{\"columns\": [\"nonsense\"], \"rows\": []}");
        queueTopicImages(8000, new long[][] { {80001, 5, 12000} });

        PostIdResolver resolver = new PostIdResolver(createApiSimulator(), List.of(
                new PostIdResolver.TopicPost(7253, 32),
                new PostIdResolver.TopicPost(7253, 33),
                new PostIdResolver.TopicPost(8000, 5)));

        PostIdResolver.Resolution resolution = resolver.resolve();

        // Everything wanted from the broken topic is unresolved; the next topic still resolves.
        assertThat(resolution.postIds).containsExactly(80001L);
        assertThat(resolution.unresolved).hasSize(2);
    }

    @Test
    public void tooManyFailedTopicsGivesUpTest() {
        List<PostIdResolver.TopicPost> wanted = new ArrayList<>();
        for (int topic = 1; topic <= PostIdResolver.MAX_CONSECUTIVE_FAILURES + 3; topic++) {
            HttpClientSimulator.setQueryResponseData(Constants.QUERY_GET_TOPIC_IMAGES,
                    "{\"columns\": [\"nonsense\"], \"rows\": []}");
            wanted.add(new PostIdResolver.TopicPost(topic, 2));
        }

        PostIdResolver resolver = new PostIdResolver(createApiSimulator(), wanted);

        // A dead site should not be ground through one topic at a time.
        Throwable thrown = catchThrowable(resolver::resolve);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("in a row");
    }

    @Test
    public void emptyWantedListTest() {
        PostIdResolver resolver = new PostIdResolver(createApiSimulator(), List.of());

        assertThat(resolver.resolve().postIds).isEmpty();
    }

    // ---- output ----

    @Test
    public void writesAFileDeletePostsCanReadTest() throws IOException {
        queueTopicImages(7253, new long[][] { {71301, 32, 11463}, {71304, 33, 11465} });
        Path output = tempOutput();

        PostIdResolver resolver = new PostIdResolver(createApiSimulator(), List.of(
                new PostIdResolver.TopicPost(7253, 32),
                new PostIdResolver.TopicPost(7253, 33)));
        PostIdResolver.write(resolver.resolve(), output);

        assertThat(Files.readAllLines(output).get(0)).isEqualTo("\"post_id\",\"post_number\",\"topic_id\"");

        // The whole point of the file: delete-posts reads it back.
        assertThat(PostDeleter.readPostIds(output.toString())).containsExactly(71301L, 71304L);
    }

    @Test
    public void writesUnresolvedCompanionFileTest() throws IOException {
        queueTopicImages(7253, new long[][] { {71301, 32, 11463} });
        Path output = tempOutput();

        PostIdResolver resolver = new PostIdResolver(createApiSimulator(), List.of(
                new PostIdResolver.TopicPost(7253, 32),
                new PostIdResolver.TopicPost(7253, 99)));
        PostIdResolver.write(resolver.resolve(), output);

        Path unresolved = PostIdResolver.unresolvedFor(output);
        assertThat(Files.exists(unresolved)).isTrue();
        assertThat(Files.readAllLines(unresolved)).containsExactly(
                "\"topic_id\",\"post_number\"", "\"7253\",\"99\"");

        // And it is itself a valid input, so a fixed up subset can be re-run.
        assertThat(PostIdResolver.readTopicPosts(unresolved.toString())).hasSize(1);
    }

    @Test
    public void noUnresolvedFileWhenEverythingResolvesTest() throws IOException {
        queueTopicImages(7253, new long[][] { {71301, 32, 11463} });
        Path output = tempOutput();

        PostIdResolver resolver = new PostIdResolver(createApiSimulator(),
                List.of(new PostIdResolver.TopicPost(7253, 32)));
        PostIdResolver.write(resolver.resolve(), output);

        assertThat(Files.exists(PostIdResolver.unresolvedFor(output))).isFalse();
    }
}
