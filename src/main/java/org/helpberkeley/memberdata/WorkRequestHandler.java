/*
 * Copyright (c) 2020. helpberkeley.org
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

import java.io.IOException;
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

    private final ApiClient apiClient;
    private final Query query;
    private Reply lastReply;

    WorkRequestHandler(ApiClient apiClient, Query query) {
        this.apiClient = apiClient;
        this.query = query;
    }

    Reply getLastReply() throws IOException, InterruptedException {

        lastReply = fetchLastReply();

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
                    "Last post (#" + lastReply.postNumber + ") in " + query.topic + " is empty");
        }

        Reply reply = Status.parse(lastReply, lines);

        if (reply == null) {
            reply = WorkRequest.parse(lastReply, lines);
        }

        if (reply == null) {
           throw new MemberDataException(
                    "Post #" + lastReply.postNumber + " in " + query.topic
                    + " is not recognizable as either a work request or a status message.");
        }

        return reply;
    }

    // Generate a reply to the topic id
    void postStatus(RequestStatus status, final String statusMessage) throws IOException, InterruptedException {

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

        String rawPost = timeStamp + "\n"
                + "Status: " + status + "\n"
                + "\n"
                + statusMessage + "\n";

        Post post = new Post();
        post.title = "Status response to post " + lastReply.postNumber;
        assert query.topic != null;
        post.topic_id = query.topic.id;
        post.raw = rawPost;
        post.createdAt = timeStamp;

        HttpResponse<?> response = apiClient.post(post.toJson());

        // FIX THIS, DS: what to do with this error?
        assert response.statusCode() == HTTP_OK : "failed " + response.statusCode() + ": " + response.body();
    }

    private Reply fetchLastReply() throws IOException, InterruptedException {

        String json = apiClient.runQuery(query.id);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);

        // FIX THIS, DS: constant
        Integer postNumberIndex = apiQueryResult.getColumnIndex("post_number");
        assert postNumberIndex != null;
        Integer rawIndex = apiQueryResult.getColumnIndex("raw");
        assert rawIndex != null;

        if (apiQueryResult.rows.length == 0) {
            throw new MemberDataException("Topic not found: " + query.id + ":" + query.topic);
        }

        assert apiQueryResult.rows.length == 1 : apiQueryResult.rows.length;

        Object[] columnObjs = (Object[]) apiQueryResult.rows[0];
        long postNumber = (Long)columnObjs[postNumberIndex];
        String lastReplyRaw = (String)columnObjs[rawIndex];

        // Normalize EOL
        lastReplyRaw = lastReplyRaw.replaceAll("\\r\\n?", "\n");

        return new Reply(apiClient, query, postNumber, lastReplyRaw);
    }

    static class Reply {
        final ApiClient apiClient;
        final Query query;
        final long postNumber;
        final String raw;

        Reply(final ApiClient apiClient, final Query query, long postNumber, final String raw) {
            this.apiClient = apiClient;
            this.query = query;
            this.postNumber = postNumber;
            this.raw = raw;
        }

        Reply(Reply reply) {
            this.apiClient = reply.apiClient;
            this.query = reply.query;
            this.postNumber = reply.postNumber;
            this.raw = reply.raw;
        }
    }

    static class WorkRequest extends Reply {

        final String date;
        final UploadFile uploadFile;

        WorkRequest(Reply reply, final String date, final UploadFile uploadFile) {
            super(reply);
            this.date = date;
            this.uploadFile = uploadFile;
        }

        // Generate a reply to the topic id
        void postStatus(RequestStatus status, final String statusMessage) throws IOException, InterruptedException {

            String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

            String rawPost = timeStamp + "\n"
                    + "\n"
                    + "Status: " + status + "\n"
                    + "File: " + uploadFile.originalFileName + "\n"
                    + "\n"
                    + statusMessage + "\n";

            Post post = new Post();
            post.title = "Status response to post " + postNumber;
            assert query.topic != null;
            post.topic_id = query.topic.id;
            post.raw = rawPost;
            post.createdAt = timeStamp;

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
                throw new MemberDataException("Invalid date in post #" + lastReply.postNumber + " : " + dateLine);
            }

            ListIterator<String> iterator = lines.listIterator(1);

            while (iterator.hasNext()) {
                String line = iterator.next();

                if (line.contains(Constants.UPLOAD_URI_PREFIX)) {
                    String shortURL =  HBParser.shortURL(line);
                    String fileName = HBParser.downloadFileName(line);

                    UploadFile uploadFile = new UploadFile(fileName, shortURL);

                    return new WorkRequest(lastReply, dateLine, uploadFile);
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return "Post: " + postNumber + '\n'
                    + "WorkRequest\n"
                    + "Date: " + date + '\n'
                    + "FileName: " + uploadFile.fileName + '\n';
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
