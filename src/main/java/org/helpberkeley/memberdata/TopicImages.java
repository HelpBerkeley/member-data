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
public class TopicImages {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicImages.class);

    static final String COLUMN_POST_ID = "post_id";
    static final String COLUMN_POST_NUMBER = "post_number";
    static final String COLUMN_UPLOAD_ID = "upload_id";
    static final String COLUMN_ORIGINAL_FILENAME = "original_filename";
    static final String COLUMN_FILESIZE = "filesize";
    static final String COLUMN_URL = "url";

    private final ApiClient apiClient;
    private final long topicId;
    private final List<ImageRecord> images;

    TopicImages(final ApiClient apiClient, final long topicId) {
        this.apiClient = apiClient;
        this.topicId = topicId;
        this.images = fetchImageRecords();
    }

    List<ImageRecord> getImages() {
        return images;
    }

    private List<ImageRecord> fetchImageRecords() {

        String json = apiClient.runQueryWithParams(Constants.QUERY_GET_TOPIC_IMAGES,
                Map.of("topic_id", String.valueOf(topicId)));
        ApiQueryResult queryResult = HBParser.parseQueryResult(json);

        int postIdIndex = queryResult.getColumnIndex(COLUMN_POST_ID);
        int postNumberIndex = queryResult.getColumnIndex(COLUMN_POST_NUMBER);
        int uploadIdIndex = queryResult.getColumnIndex(COLUMN_UPLOAD_ID);
        int filenameIndex = queryResult.getColumnIndex(COLUMN_ORIGINAL_FILENAME);
        int filesizeIndex = queryResult.getColumnIndex(COLUMN_FILESIZE);
        int urlIndex = queryResult.getColumnIndex(COLUMN_URL);

        List<ImageRecord> records = new ArrayList<>();
        for (Object rowObject : queryResult.rows) {
            Object[] columns = (Object[]) rowObject;
            records.add(new ImageRecord(
                    (Long) columns[postIdIndex],
                    (Long) columns[postNumberIndex],
                    (Long) columns[uploadIdIndex],
                    (String) columns[filenameIndex],
                    (Long) columns[filesizeIndex],
                    (String) columns[urlIndex]));
        }

        return records;
    }

    /**
     * Download every Discourse-hosted image in the topic into outputDir, returning the paths written.
     * An image-free topic writes nothing and is not an error.
     */
    List<Path> downloadImages(final Path outputDir) {

        if (images.isEmpty()) {
            LOGGER.info("No Discourse-hosted images found in topic {}", topicId);
            return List.of();
        }

        try {
            Files.createDirectories(outputDir);
        } catch (IOException ex) {
            throw new MemberDataException("Could not create output directory " + outputDir + ": "
                    + ex.getMessage());
        }

        List<Path> written = new ArrayList<>();
        for (ImageRecord image : images) {
            byte[] bytes = apiClient.downloadImage(image.url);
            Path outputPath = outputDir.resolve(image.fileName());
            try {
                Files.write(outputPath, bytes);
            } catch (IOException ex) {
                throw new MemberDataException("Could not write image " + outputPath + ": "
                        + ex.getMessage());
            }
            written.add(outputPath);
            LOGGER.info("Downloaded {} ({} bytes) to {}", image.originalFilename, bytes.length, outputPath);
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        LOGGER.info("Downloaded {} image(s) from topic {} to {}", written.size(), topicId, outputDir);
        return written;
    }

    /** Default output directory for a topic's images. */
    static Path defaultOutputDir(final long topicId) {
        return Path.of("topic-" + topicId + "-images");
    }
}
