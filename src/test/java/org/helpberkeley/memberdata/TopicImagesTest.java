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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class TopicImagesTest extends TestBase {

    private static final String EMPTY_RESULT =
            "{ \"columns\": [\"post_id\",\"post_number\",\"upload_id\",\"original_filename\","
                    + "\"extension\",\"filesize\",\"url\"], \"rows\": [] }";

    private static final String FIRST_POST_ONLY_RESULT =
            "{ \"columns\": [\"post_id\",\"post_number\",\"upload_id\",\"original_filename\","
                    + "\"extension\",\"filesize\",\"url\"], \"rows\": ["
                    + "[71300,1,11460,\"cover.png\",\"png\",12345,\"//cdn.example.com/test-image.png\"]] }";

    @Before
    public void clearDeleteState() {
        // The simulator's delete state is static and the test classes share a JVM.
        HttpClientSimulator.clearDeleteRequests();
        // Otherwise the force tests sleep 500ms per request, and the forbidden test 5 minutes.
        TopicImages.NAP_MILLISECONDS = 0;
        PostDeleter.PERMANENT_DELETE_WAIT_MILLISECONDS = 0;
        PostDeleter.NAP_MILLISECONDS = 0;
    }

    @Test
    public void downloadImagesWritesAllImagesTest() throws Exception {
        ApiClient apiClient = createApiSimulator();
        TopicImages topicImages = new TopicImages(apiClient, 7253);

        Path outputDir = Files.createTempDirectory("topic-images-test");
        try {
            List<Path> written = topicImages.downloadImages(outputDir);

            byte[] expected = Files.readAllBytes(Paths.get(Thread.currentThread()
                    .getContextClassLoader().getResource("test-image.png").toURI()));

            // One file per image row, named post-<postNumber>-<uploadId>-<originalFilename>.
            List<String> names = new ArrayList<>();
            for (Path path : written) {
                names.add(path.getFileName().toString());
                // Bytes are the raw upload bytes - not corrupted.
                assertThat(Files.readAllBytes(path)).isEqualTo(expected);
            }

            assertThat(names).containsExactly(
                    "post-1-11460-cover.png",
                    "post-32-11463-image0.jpeg",
                    "post-32-11464-image1.jpeg",
                    "post-33-11465-photo.jpeg");
        } finally {
            deleteRecursively(outputDir);
        }
    }

    @Test
    public void downloadImagesEmptyTopicTest() throws Exception {
        ApiClient apiClient = createApiSimulator();
        HttpClientSimulator.setQueryResponseData(Constants.QUERY_GET_TOPIC_IMAGES, EMPTY_RESULT);
        TopicImages topicImages = new TopicImages(apiClient, 9999);

        Path outputDir = Files.createTempDirectory("topic-images-empty-test");
        try {
            List<Path> written = topicImages.downloadImages(outputDir);

            assertThat(written).isEmpty();
            try (Stream<Path> entries = Files.list(outputDir)) {
                assertThat(entries.count()).isZero();
            }
        } finally {
            deleteRecursively(outputDir);
        }
    }

    @Test
    public void deleteImagePostsDryRunTest() {
        ApiClient apiClient = createApiSimulator();
        TopicImages topicImages = new TopicImages(apiClient, 7253);

        List<Long> deleted = topicImages.deleteImagePosts(false);

        // Post 71300 is post number 1 and is never deleted. 71301 holds two images but is one post.
        assertThat(deleted).containsExactly(71301L, 71304L);
        // A dry run deletes nothing.
        assertThat(HttpClientSimulator.getDeleteRequests()).isEmpty();
    }

    @Test
    public void deleteImagePostsForceTest() {
        ApiClient apiClient = createApiSimulator();
        TopicImages topicImages = new TopicImages(apiClient, 7253);

        List<Long> deleted = topicImages.deleteImagePosts(true);

        assertThat(deleted).containsExactly(71301L, 71304L);
        // Every post soft deleted, then every post permanently deleted - each exactly once, and
        // post 71300 (post number 1) never. 71301 appears once per pass despite holding two images.
        assertThat(HttpClientSimulator.getDeleteRequests()).containsExactly(
                Constants.POSTS_BASE + "71301",
                Constants.POSTS_BASE + "71304",
                Constants.POSTS_BASE + "71301" + Constants.FORCE_DESTROY,
                Constants.POSTS_BASE + "71304" + Constants.FORCE_DESTROY);
    }

    @Test
    public void deleteImagePostsEmptyTopicTest() {
        ApiClient apiClient = createApiSimulator();
        HttpClientSimulator.setQueryResponseData(Constants.QUERY_GET_TOPIC_IMAGES, EMPTY_RESULT);
        TopicImages topicImages = new TopicImages(apiClient, 9999);

        assertThat(topicImages.deleteImagePosts(true)).isEmpty();
        assertThat(HttpClientSimulator.getDeleteRequests()).isEmpty();
    }

    @Test
    public void deleteImagePostsOnlyFirstPostHasImagesTest() {
        ApiClient apiClient = createApiSimulator();
        HttpClientSimulator.setQueryResponseData(Constants.QUERY_GET_TOPIC_IMAGES, FIRST_POST_ONLY_RESULT);
        TopicImages topicImages = new TopicImages(apiClient, 7253);

        // Nothing is deletable, and that is not an error.
        assertThat(topicImages.deleteImagePosts(true)).isEmpty();
        assertThat(HttpClientSimulator.getDeleteRequests()).isEmpty();
    }

    @Test
    public void deleteImagePostsNotFoundToleratedTest() {
        HttpClientSimulator.setDeleteResponseStatus(71301, HTTP_NOT_FOUND);
        ApiClient apiClient = createApiSimulator();
        TopicImages topicImages = new TopicImages(apiClient, 7253);

        // An already deleted post does not abort the run.
        assertThat(topicImages.deleteImagePosts(true)).containsExactly(71301L, 71304L);
        assertThat(HttpClientSimulator.getDeleteRequests()).hasSize(4);
    }

    @Test
    public void deleteImagePostsForbiddenTest() {
        // Discourse's actual refusal, as seen against the live site.
        String refusal = "{\"errors\":[\"You must wait 4 minutes before permanently deleting this"
                + " post or a different administrator must do it.\"]}";
        HttpClientSimulator.setDeleteResponseStatus(71301, HTTP_FORBIDDEN, refusal);
        ApiClient apiClient = createApiSimulator();
        TopicImages topicImages = new TopicImages(apiClient, 7253);

        Throwable thrown = catchThrowable(() -> topicImages.deleteImagePosts(true));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("71301");
        // Discourse's own reason must reach the operator - the refusals are otherwise identical.
        assertThat(thrown).hasMessageContaining(refusal);
        assertThat(thrown).hasMessageContaining("can_permanently_delete");

        // The permanent delete of 71301 was retried once, after waiting out the timer.
        assertThat(HttpClientSimulator.getDeleteRequests()).containsExactly(
                Constants.POSTS_BASE + "71301",
                Constants.POSTS_BASE + "71304",
                Constants.POSTS_BASE + "71301" + Constants.FORCE_DESTROY,
                Constants.POSTS_BASE + "71301" + Constants.FORCE_DESTROY);
    }

    private static void deleteRecursively(final Path dir) throws IOException {
        if (! Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}
