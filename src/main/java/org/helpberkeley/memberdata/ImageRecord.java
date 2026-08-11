package org.helpberkeley.memberdata;

class ImageRecord {
    // The id of the post holding this image. Several ImageRecords can share one postId (a post with
    // two uploads yields two rows), so anything acting per-post - see TopicImages.deleteImagePosts -
    // must de-duplicate on it. Not used for downloading; do not remove as unused.
    final long postId;
    final long postNumber;
    final long uploadId;
    final String originalFilename;
    final long filesize;
    final String url;

    ImageRecord(final long postId, final long postNumber, final long uploadId,
                final String originalFilename, final long filesize, final String url) {
        this.postId = postId;
        this.postNumber = postNumber;
        this.uploadId = uploadId;
        this.originalFilename = originalFilename;
        this.filesize = filesize;
        this.url = url;
    }

    /**
     * Output file name: post-&lt;postNumber&gt;-&lt;uploadId&gt;-&lt;sanitized original filename&gt;.
     */
    String fileName() {
        return "post-" + postNumber + "-" + uploadId + "-" + safeBaseName(originalFilename);
    }

    private static String safeBaseName(final String filename) {
        // Strip any path components and neutralize characters that aren't safe in a file name.
        String base = filename.replace('\\', '/');
        int slash = base.lastIndexOf('/');
        if (slash != -1) {
            base = base.substring(slash + 1);
        }
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");
        return base.isEmpty() ? "image" : base;
    }
}
