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

import static org.assertj.core.api.Assertions.assertThat;

public class TopicImagesTest extends TestBase {

    private static final String EMPTY_RESULT =
            "{ \"columns\": [\"post_id\",\"post_number\",\"upload_id\",\"original_filename\","
                    + "\"extension\",\"filesize\",\"url\"], \"rows\": [] }";

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
