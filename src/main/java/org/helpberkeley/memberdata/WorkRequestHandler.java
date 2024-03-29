/*
 * Copyright (c) 2020-2023. helpberkeley.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package org.helpberkeley.memberdata;

import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.HttpURLConnection.HTTP_OK;

public class WorkRequestHandler {

    public enum RequestType {
        UPLOAD("upload"),
        ONE_KITCHEN("OneKitchen"),
        DAILY("Daily");

        RequestType(String type) {
            this.type = type;
        }

        public static RequestType fromString(String name) {
            for (RequestType requestType : RequestType.values()) {
                if (requestType.type.equalsIgnoreCase(name)) {
                    return requestType;
                }
            }

            throw new MemberDataException(name + " is not a known RequestType");
        }

        private final String type;
    }

    public static final String ERROR_INVALID_DATE =
            "Invalid date in post. The first line must contain only the date, formatted as: **YYYY/MM/DD**";
    public static final String TOPIC_DIRECTIVE_NOT_SUPPORTED =
            "Topic: no longer supported as directive. Use \"Test topic\" instead.";

    public static final String DISABLE_DATE_AUDIT = "disable date audit";

    private final ApiClient apiClient;
    private final Query query;
    private Reply lastReply;

    // Support for end-to-end testing through main()
    private static Post lastStatusPost;

    WorkRequestHandler(ApiClient apiClient, Query query) {
        this.apiClient = apiClient;
        this.query = query;
    }

    WorkRequestHandler(ApiClient apiClient, long topicId, long postNumber, String raw) {
        this.apiClient = apiClient;
        this.query = null;

        // Normalize EOL
        this.lastReply = new Reply(apiClient, topicId, postNumber, raw.replaceAll("\\r\\n?", "\n"));
    }

    // Support for end-to-end testing through main()
    static void clearLastStatusPost() {
        lastStatusPost = null;
    }

    // Support for end-to-end testing through main()
    static Post getLastStatusPost() {
        return lastStatusPost;
    }

    Reply getLastReply() {

        if (lastReply != null) {
            assert query == null;
        } else {
            lastReply = fetchLastReply();
        }

        List<String> lines = new ArrayList<>();

        for (String line : lastReply.raw.split("\n")) {
            String trimmed = line.trim();

            if (! trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }

        if (lines.isEmpty()) {
            // FIX THIS, DS: make this a generic can't parse error?  Use constant, for testability
            throw new MemberDataException(
                    "Last post (#" + lastReply.postNumber + ") in " + query.getTopic() + " is empty");
        }

        Reply reply = Status.parse(lastReply, lines);

        if (reply == null) {
            reply = WorkRequest.parse(lastReply, lines);
        }

        if (reply == null) {
           throw new MemberDataException(
                    "Post #" + lastReply.postNumber + " in " + query.getTopic()
                    + " is not recognizable as either a work request or a status message.");
        }

        return reply;
    }

    // Generate a reply to the topic id
    void postStatus(RequestStatus status, final String statusMessage) {

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

        String rawPost = timeStamp + "\n"
                + "Status: " + status + "\n"
                + "\n"
                + statusMessage + "\n";

        Post post = new Post();
        post.title = "Status response to post " + lastReply.postNumber;
        post.topic_id = getTopicId();
        post.raw = rawPost;
        post.createdAt = timeStamp;

        lastStatusPost = post;

        HttpResponse<?> response = apiClient.post(post.toJson());

        // FIX THIS, DS: what to do with this error?
        assert response.statusCode() == HTTP_OK : "failed " + response.statusCode() + ": " + response.body();
    }

    private Reply fetchLastReply() {

        String json = apiClient.runQuery(query.getId());
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        Integer postNumberIndex = apiQueryResult.getColumnIndex(Constants.DISCOURSE_COLUMN_POST_NUMBER);
        assert postNumberIndex != null;
        Integer rawIndex = apiQueryResult.getColumnIndex(Constants.DISCOURSE_COLUMN_RAW);
        assert rawIndex != null;

        if (apiQueryResult.rows.length == 0) {
            throw new MemberDataException("Topic not found: " + query.getId() + ":" + query.getTopic());
        }

        assert apiQueryResult.rows.length == 1 : apiQueryResult.rows.length;

        Object[] columnObjs = (Object[]) apiQueryResult.rows[0];
        long postNumber = (Long)columnObjs[postNumberIndex];
        String lastReplyRaw = (String)columnObjs[rawIndex];

        // Normalize EOL
        lastReplyRaw = lastReplyRaw.replaceAll("\\r\\n?", "\n");

        return new Reply(apiClient, query.getTopic().getId(), postNumber, lastReplyRaw);
    }

    long getTopicId() {
        long topicId;

        if (lastReply != null) {
            topicId = lastReply.topicId;
        } else {
            assert query != null;
            assert query.getTopic() != null;
            topicId = query.getTopic().getId();
        }

        return topicId;
    }

    static class Reply {
        final ApiClient apiClient;
        final long topicId;
        final long postNumber;
        final String raw;

        Reply(final ApiClient apiClient, long topicId, long postNumber, final String raw) {
            this.apiClient = apiClient;
            this.topicId = topicId;
            this.postNumber = postNumber;
            this.raw = raw;
        }

        Reply(Reply reply) {
            this.apiClient = reply.apiClient;
            this.topicId = reply.topicId;
            this.postNumber = reply.postNumber;
            this.raw = reply.raw;
        }
    }

    static class WorkRequest extends Reply {
        final String date;
        final UploadFile uploadFile;
        Long topic;
        final String version;
        final boolean disableDateAudit;
        final RequestType requestType;
        final List<String> otherLines = new ArrayList<>();

        WorkRequest(Reply reply, String date, UploadFile uploadFile,
                    Long topic, String version, boolean disableDateAudit) {
            super(reply);
            this.date = date;
            this.uploadFile = uploadFile;
            this.topic = topic;
            this.version = version;
            this.disableDateAudit = disableDateAudit;
            this.requestType = RequestType.UPLOAD;
        }

        WorkRequest(Reply reply, String date, RequestType requestType) {
            super(reply);
            this.date = date;
            this.requestType = requestType;
            this.uploadFile = null;
            this.topic = topic;
            this.disableDateAudit = false;
            this.version = null;
        }

        void setTestTopic() {
            this.topic = Main.STONE_TEST_TOPIC;
        }

        RequestType getRequestType() {
            return requestType;
        }

        // Generate a reply to the topic id
        void postStatus(RequestStatus status, final String statusMessage) {

            String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

            String rawPost = timeStamp + "\n"
                    + "\n"
                    + "Status: " + status + "\n";

            if (uploadFile != null) {
                rawPost += "File: " + uploadFile.getOriginalFileName() + "\n";
            }

            rawPost += "\n";
            rawPost += statusMessage + "\n";

            Post post = new Post();
            post.title = "Status response to post " + postNumber;
            assert topicId != 0;
            post.topic_id = topicId;
            post.raw = rawPost;
            post.createdAt = timeStamp;

            lastStatusPost = post;

            HttpResponse<?> response = apiClient.post(post.toJson());
            // FIX THIS, DS: what to do with this error?
            assert response.statusCode() == HTTP_OK : "failed " + response.statusCode() + ": " + response.body();
        }

        static Reply parse(Reply lastReply, final List<String> lines) {

            assert ! lines.isEmpty();
            String dateLine = lines.get(0);

            // Validate date
            String regex = "^202[0-9]/[01][0-9]/[0-3][0-9]$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(dateLine);

            if (! matcher.find()) {
                throw new MemberDataException(ERROR_INVALID_DATE);
            }

            ListIterator<String> iterator = lines.listIterator(1);
            Long topic = null;
            String version = null;
            boolean disableDateAudit = false;

            while (iterator.hasNext()) {
                String line = iterator.next().trim();

                if (line.startsWith("Status:")) {
                    return null;
                }

                if (line.startsWith("Topic:")) {
                    throw new MemberDataException(
                            "Post #" + lastReply.postNumber + " is not a valid request\n"
                                    + TOPIC_DIRECTIVE_NOT_SUPPORTED);
                } else if (line.equalsIgnoreCase("test topic")) {
                    topic = Main.STONE_TEST_TOPIC;
                } else if (line.startsWith("Version:")) {
                    version = line.replaceAll("Version:", "").trim();
                } else if (line.toLowerCase().startsWith(DISABLE_DATE_AUDIT)) {
                    disableDateAudit = true;
                } else if (line.contains(Constants.UPLOAD_URI_PREFIX)) {
                    String shortURL =  HBParser.shortURLDiscoursePost(line);
                    String fileName = HBParser.downloadFileName(line);

                    UploadFile uploadFile = new UploadFile(fileName, shortURL);

                    return new WorkRequest(lastReply, dateLine, uploadFile, topic, version, disableDateAudit);
                } else if (line.equalsIgnoreCase(Constants.ONE_KITCHEN_WORKFLOW)
                    || line.equalsIgnoreCase(Constants.DAILY_WORKFLOW)) {
                    return new WorkRequest(lastReply, dateLine, RequestType.fromString(line));
                }

            }

            throw new MemberDataException("Post #" + lastReply.postNumber + " is not a valid request");
        }

        @Override
        public String toString() {
            return "Post: " + postNumber + '\n'
                    + "WorkRequest\n"
                    + "Date: " + date + '\n'
                    + "Topic: " + topic + '\n'
                    + "Version: " + version + '\n'
                    + "FileName: " + uploadFile.getFileName() + '\n';
        }
    }

    enum RequestStatus {
        Processing,
        Succeeded,
        Failed;

        static RequestStatus fromString(final String enumValueName) {
            RequestStatus requestStatus;

            if (Processing.toString().equals(enumValueName)) {
                requestStatus = Processing;
            } else if (Succeeded.toString().equals(enumValueName)) {
                requestStatus = Succeeded;
            } else if (Failed.toString().equals(enumValueName)) {
                return Failed;
            } else {
                    throw new MemberDataException("unknown RequestStatus value: " + enumValueName);
            }

            return requestStatus;
        }
    }

    static class Status extends Reply {

        final RequestStatus requestStatus;
        final String time;

        Status(Reply reply, RequestStatus requestStatus, final  String time) {
            super(reply);
            this.requestStatus = requestStatus;
            this.time = time;
        }

        static Reply parse(Reply lastReply, final List<String> lines) {

            assert ! lines.isEmpty();
            String dateLine = lines.get(0);

            // Validate date
            String regex = "^202[0-9]/[01][0-9]/[0-3][0-9] [012][0-9]:[0-5][0-9]:[0-5][0-9]$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(dateLine);

            if (! matcher.find()) {
                return null;
            }

            assert lines.size() > 0;
            String status = lines.get(1);

            regex = "^Status: ([A-Z][a-z]+)$";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(status);

            if (! matcher.find()) {
                return null;
            }
            status = matcher.group(1);
            RequestStatus requestStatus = RequestStatus.fromString(status);

            return new Status(lastReply, requestStatus, dateLine);
        }
        @Override
        public String toString() {
            return "Post: " + postNumber + '\n'
                    + "Status: " + requestStatus + '\n'
                    + "Time: " + time + '\n';
        }
    }
}
