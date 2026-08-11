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
import java.util.LinkedHashMap;
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

    // Test support - zeroed by tests so they don't sleep. See ApiClient.RETRY_NAP_MILLISECONDS.
    static long NAP_MILLISECONDS = 500L;

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
            nap(NAP_MILLISECONDS);
        }

        LOGGER.info("Downloaded {} image(s) from topic {} to {}", written.size(), topicId, outputDir);
        return written;
    }

    /**
     * Permanently delete the posts in this topic that contain Discourse-hosted images.
     *
     * force == false is a dry run: the posts that would be destroyed are logged, and nothing is deleted.
     *
     * The topic's first post is never deleted - Discourse deletes the whole topic along with it - so any
     * images it holds are left in place, with a warning.
     *
     * The deleting itself is done by PostDeleter - see there for why it takes two passes.
     *
     * @return the ids of the posts that were (dry run: would be) deleted, in post number order
     */
    List<Long> deleteImagePosts(final boolean force) {

        if (images.isEmpty()) {
            LOGGER.info("No Discourse-hosted images found in topic {}", topicId);
            return List.of();
        }

        // Group by post - a post with several images yields several rows, but is one post to delete.
        Map<Long, List<ImageRecord>> postImages = new LinkedHashMap<>();
        for (ImageRecord image : images) {
            postImages.computeIfAbsent(image.postId, postId -> new ArrayList<>()).add(image);
        }

        // Never delete the topic's first post - that deletes the entire topic, every reply included.
        List<Long> firstPostIds = new ArrayList<>();
        for (Map.Entry<Long, List<ImageRecord>> entry : postImages.entrySet()) {
            if (entry.getValue().get(0).postNumber == 1) {
                firstPostIds.add(entry.getKey());
                LOGGER.warn("Post 1 of topic {} contains {} image(s) that will NOT be deleted - deleting a"
                        + " topic's first post deletes the whole topic. Left in place: {}",
                        topicId, entry.getValue().size(), imageSummary(entry.getValue()));
            }
        }
        postImages.keySet().removeAll(firstPostIds);

        if (postImages.isEmpty()) {
            LOGGER.info("Topic {} has no deletable image posts", topicId);
            return List.of();
        }

        List<Long> postIds = new ArrayList<>(postImages.keySet());

        for (Map.Entry<Long, List<ImageRecord>> entry : postImages.entrySet()) {
            List<ImageRecord> postImageRecords = entry.getValue();
            long totalSize = 0;
            for (ImageRecord image : postImageRecords) {
                totalSize += image.filesize;
            }
            LOGGER.info("Post {} (#{} in topic {}): {} image(s), {} bytes: {}",
                    entry.getKey(), postImageRecords.get(0).postNumber, topicId,
                    postImageRecords.size(), totalSize, imageSummary(postImageRecords));
        }
        LOGGER.info("Note: deleting these posts removes their upload references. Discourse reaps the"
                + " uploaded files themselves later, and not at all if a surviving post still uses them.");

        if (! force) {
            LOGGER.warn("DRY RUN - nothing was deleted. To permanently delete these {} post(s), re-run as"
                    + " \"{} {} {}\"", postIds.size(),
                    Options.COMMAND_DELETE_TOPIC_IMAGE_POSTS, topicId, Options.FORCE_ARGUMENT);
            return postIds;
        }

        // No journal - a single topic's posts are cheap to redo if the run is interrupted.
        // Bulk deletion across many topics goes through Options.COMMAND_DELETE_POSTS, which does
        // journal, because a soft deleted post is invisible to the query that found it.
        new PostDeleter(apiClient, postIds, null).deletePosts(true);

        LOGGER.warn("Permanently deleted {} post(s) from topic {}", postIds.size(), topicId);
        return postIds;
    }

    private static String imageSummary(final List<ImageRecord> imageRecords) {
        List<String> summaries = new ArrayList<>();
        for (ImageRecord image : imageRecords) {
            summaries.add(image.originalFilename + " (upload " + image.uploadId + ")");
        }
        return String.join(", ", summaries);
    }

    private void nap(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** Default output directory for a topic's images. */
    static Path defaultOutputDir(final long topicId) {
        return Path.of("topic-" + topicId + "-images");
    }
}
