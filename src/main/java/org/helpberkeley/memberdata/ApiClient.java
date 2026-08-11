//
// Copyright (c) 2020-2024 helpberkeley.org
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

public class ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

    private final String apiUser;
    private final String apiKey;
    private final HttpClient client;

    // Test support
    static HttpClientFactory httpClientFactory = null;
    static long RETRY_NAP_MILLISECONDS = TimeUnit.SECONDS.toMillis(10);

    // Ceiling on a server dictated wait. A site answering "wait an hour" should not silently hang
    // the process for ten hours across the ten retries.
    static final long MAX_RETRY_NAP_MILLISECONDS = TimeUnit.MINUTES.toMillis(5);
    private static final long RETRY_NAP_MARGIN_MILLISECONDS = TimeUnit.SECONDS.toMillis(1);
    private static final Pattern WAIT_SECONDS = Pattern.compile("\"wait_seconds\"\\s*:\\s*(\\d+)");

    // Responses that mean "slow down", counted across retries. PostDeleter samples this to pace
    // itself.
    private long backpressureResponses = 0;

    ApiClient(final Properties properties) {

        apiUser = properties.getProperty(Constants.API_USER_PROPERTY);
        apiKey = properties.getProperty(Constants.API_KEY_PROPERTY);
        auditAPIKey();

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(apiUser, apiKey.toCharArray());
            }
        };

        if (httpClientFactory != null) {
            this.client = httpClientFactory.createClient();

        } else {
        this.client = HttpClient.newBuilder()
//                    .proxy(ProxySelector.of(new InetSocketAddress("localhost", 8080)))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .authenticator(authenticator)
                    .build();
        }
    }

    ApiClient(final Properties properties, HttpClient httpClient) {

        apiUser = properties.getProperty(Constants.API_USER_PROPERTY);
        apiKey = properties.getProperty(Constants.API_KEY_PROPERTY);
        auditAPIKey();
        this.client = httpClient;
    }

    private void auditAPIKey() {
        if ((apiUser == null) || (apiKey == null)) {
            throw new MemberDataException("Missing "
                    + Constants.API_USER_PROPERTY
                    + " property or "
                    + Constants.API_KEY_PROPERTY
                    + " or both");
        }
    }

    private HttpResponse<String> send(HttpRequest request) {
        return send(request, HttpResponse.BodyHandlers.ofString());
    }

    private <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) {

        String lastFailure = null;

        for (int retry = 0; retry < 10; retry++ ) {

            long napMillis = RETRY_NAP_MILLISECONDS;

            try {
                HttpResponse<T> response = client.send(request, bodyHandler);
                int statusCode = response.statusCode();

                // 429 and every 5xx are retried, and all of them tell the caller to slow down.
                // A server error under a bulk write job usually means the job is the reason for
                // it - a single 500, which turned out to be an upstream timeout, once aborted a
                // 4,000 post deletion run that a retry would have carried straight through.
                if ((statusCode == Constants.HTTP_TOO_MANY_REQUESTS) || (statusCode >= 500)) {
                    backpressureResponses++;
                    lastFailure = statusCode + ": " + response.body();
                    LOGGER.warn("send {} failed: {}", request, lastFailure);
                    napMillis = retryNapMillis(response);
                } else {
                    return response;
                }
            } catch (IOException ex) {
                lastFailure = ex.getMessage();
                LOGGER.warn("send {} failed: {}", request, ex.getMessage());
            } catch (InterruptedException ex) {
                throw new RuntimeException("send " + request + " was interrupted");
            }

            if (retry < 9) {
                LOGGER.warn("Failure talking to Discourse, waiting {} seconds and retrying.",
                        TimeUnit.MILLISECONDS.toSeconds(napMillis));
                nap(napMillis);
            }
        }

        LOGGER.warn("10th retry failure seen from Discourse, exiting with a failure");
        // MemberDataException, not a bare RuntimeException: batch callers skip a post that will
        // not delete and carry on, and they must be able to tell that apart from a bug in this
        // program, which has to keep propagating.
        throw new MemberDataException(
                "10 attempts to talk with Discourse failed, last one with " + lastFailure);
    }

    /**
     * Responses telling us to slow down - 429s and server errors - counted across retries. Callers
     * running long batches pace themselves from this; see PostDeleter's use of AdaptivePacer.
     */
    long backpressureResponses() {
        return backpressureResponses;
    }

    /**
     * How long to wait before retrying, taken from what the server said rather than a fixed guess.
     * Discourse's middleware rate limiter sets a Retry-After header; its application level one
     * returns extras.wait_seconds in a JSON body. The stated interval is usually much shorter than
     * ten seconds, and when it is longer, retrying early just burns one of the ten attempts.
     */
    long retryNapMillis(HttpResponse<?> response) {

        // Tests zero RETRY_NAP_MILLISECONDS to disable napping. Honor that ahead of anything the
        // server said, or an injected Retry-After would stall the suite.
        if (RETRY_NAP_MILLISECONDS <= 0) {
            return 0;
        }

        Long seconds = retryAfterSeconds(response);
        if (seconds == null) {
            seconds = waitSecondsFromBody(response);
        }
        if (seconds == null) {
            return RETRY_NAP_MILLISECONDS;
        }

        // Discourse reports whole seconds, rounded down, so its "wait 3 seconds" can mean 3.9.
        long millis = TimeUnit.SECONDS.toMillis(seconds) + RETRY_NAP_MARGIN_MILLISECONDS;

        if (millis > MAX_RETRY_NAP_MILLISECONDS) {
            LOGGER.warn("Discourse asked for a {} second wait, capping it at {} seconds",
                    seconds, TimeUnit.MILLISECONDS.toSeconds(MAX_RETRY_NAP_MILLISECONDS));
            return MAX_RETRY_NAP_MILLISECONDS;
        }

        return millis;
    }

    private Long retryAfterSeconds(HttpResponse<?> response) {

        // A real HttpResponse always has headers. A simulated one need not.
        if (response.headers() == null) {
            return null;
        }

        // Retry-After also has an HTTP-date form. Discourse does not use it, and treating an
        // unparsable value as absent just falls back to the body or the fixed nap.
        return response.headers().firstValue("Retry-After")
                .map(ApiClient::parseSeconds)
                .orElse(null);
    }

    private Long waitSecondsFromBody(HttpResponse<?> response) {

        // The body is whatever the caller's BodyHandler produced - a byte[] for image downloads,
        // or an HTML error page. Only a String body can carry the JSON we are looking for, and a
        // regex reads it without deserializing a shape we have no model class for.
        if (! (response.body() instanceof String)) {
            return null;
        }

        Matcher matcher = WAIT_SECONDS.matcher((String) response.body());
        return matcher.find() ? parseSeconds(matcher.group(1)) : null;
    }

    private static Long parseSeconds(final String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void nap(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) { }
    }

    private HttpResponse<String> get(final String endpoint) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .build();

        HttpResponse<String> response = send(request);

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "post(" + Constants.POSTS_ENDPOINT + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    HttpResponse<String> post(final String json) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Constants.POSTS_ENDPOINT))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = send(request);

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "post(" + Constants.POSTS_ENDPOINT + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    public String runQuery(int queryId) {
        return doRunQuery(queryId);
//        return runQueryWithParam(queryId, "limit", "100000");
    }

    private String doRunQuery(int queryId) {

        String endpoint = Constants.QUERY_BASE + queryId + "/run";


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "multipart/form-data")
                .POST(HttpRequest.BodyPublishers.ofString("limit=10000"))
                .build();



        HttpResponse<String> response = send(request);

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "runQuery(" + endpoint + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    String runQueryWithParam(int queryId, String paramName, String paramValue) {
        return runQueryWithParams(queryId, Map.of(paramName, paramValue));
    }

    String runQueryWithParams(int queryId, Map<String, String> params) {

        String endpoint = Constants.QUERY_BASE + queryId + "/run";

        // Discourse's Data Explorer /run endpoint expects all query parameters bundled into a single
        // "params" form field whose value is a JSON object (e.g. params={"topic_id":"8506"}), alongside
        // a "limit" field. (The previous implementation sent each parameter as its own multipart field
        // via addParamPart - which also emitted a malformed boundary - and never sent a limit, so
        // Discourse silently ignored the parameters.)
        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
                .addPart("params", paramsToJson(params))
                .addPart("limit", "10000");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .version(HttpClient.Version.HTTP_1_1)
                    .setHeader("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + publisher.getBoundary())
                    .setHeader("Api-Key", apiKey)
                    .setHeader("Api-Username", apiUser)
                    .header("Accept", "application/json")
                    .POST(publisher.build())
                    .build();

            HttpResponse<String> response = send(request);

            if (response.statusCode() != HTTP_OK) {
                throw new MemberDataException(
                        "runQuery(" + endpoint + " failed: " + response.statusCode() + ": " + response.body());
            }

            return response.body();
        } catch (URISyntaxException ex) {
            throw new MemberDataException("Failed runQueryWithParameters: " + ex.getMessage());
        }
    }

    // Serialize query parameters as a JSON object for the Data Explorer "params" field.
    static String paramsToJson(Map<String, String> params) {
        StringBuilder json = new StringBuilder("{");
        String separator = "";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            json.append(separator)
                    .append('"').append(jsonEscape(entry.getKey())).append("\":\"")
                    .append(jsonEscape(entry.getValue())).append('"');
            separator = ",";
        }
        return json.append('}').toString();
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    String getPost(long postId) {
        String endpoint = Constants.POSTS_BASE + postId + ".json";
        // Normalize EOL
        return get(endpoint).body().replaceAll("\\r\\n?", "\n");
    }

    String getTopic(long topicId) {
        String endpoint = Constants.TOPICS_BASE + topicId + ".json";
        return get(endpoint).body();
    }

    public HttpResponse<String> updatePost(long postId, final String body) {

        String endpoint =  Constants.POSTS_BASE + postId;
        String postBody = "{ \"post\" : { \"raw\" : \"" + body + "\" } }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(postBody))
                .build();

        return send(request);
    }

    /**
     * Delete a post.
     *
     * permanent == false soft deletes it - Discourse marks it deleted, staff can still see it.
     * permanent == true adds force_destroy=true, which Discourse only honors for an admin API user,
     * with the can_permanently_delete site setting enabled, on a post that is already soft deleted,
     * and - when the same user did the soft delete - not until Post::PERMANENT_DELETE_TIMER
     * (5 minutes) has elapsed. Any of those unmet comes back as HTTP_FORBIDDEN.
     *
     * @return the response. HTTP_OK/HTTP_NO_CONTENT: deleted. HTTP_NOT_FOUND: the post was already
     *         gone, which is not an error - send() retries, and a delete whose response was lost to
     *         a GOAWAY comes back 404 on the retry. HTTP_FORBIDDEN: refused, returned rather than
     *         thrown so the caller can wait out the permanent delete timer and try again. The body
     *         carries Discourse's reason, which is the only way to tell those refusals apart.
     */
    HttpResponse<String> deletePost(long postId, boolean permanent) {

        String endpoint = Constants.POSTS_BASE + postId + (permanent ? Constants.FORCE_DESTROY : "");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                // Ask for JSON so a refusal arrives as Discourse's errors array rather than an HTML page.
                .header("Accept", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = send(request);

        switch (response.statusCode()) {
            case HTTP_OK:
            case HTTP_NO_CONTENT:
                break;
            case HTTP_NOT_FOUND:
                LOGGER.warn("deletePost({}) - post not found, treating it as already deleted", postId);
                break;
            case HTTP_FORBIDDEN:
                LOGGER.warn("deletePost({}) refused: {}", endpoint, response.body());
                break;
            default:
                throw new MemberDataException(
                        "deletePost(" + endpoint + ") failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    public HttpResponse<String> changePostOwner(long topicId, List<Long> postIds, String newOwnerUsername) {
        String endpoint = Constants.TOPICS_BASE + topicId + Constants.CHANGE_OWNER;

        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher();
        for (long postId : postIds) {
            publisher.addPart("post_ids[]", new String(String.valueOf(postId).getBytes(Charset.defaultCharset()),
                    StandardCharsets.UTF_8));
        }
        publisher.addPart("username", new String(newOwnerUsername.getBytes(Charset.defaultCharset()),
                StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .setHeader("Content-Type", "multipart/form-data; charset=UTF-8; boundary="
                        + publisher.getBoundary())
                .setHeader("Api-Key", apiKey)
                .setHeader("Api-Username", apiUser)
                .POST(publisher.build())
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "change post owner(" + endpoint + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    String downloadFile(final String shortURLFileName) {

        String endpoint = Constants.DOWNLOAD_ENDPOINT + shortURLFileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .build();

        HttpResponse<String> response = send(request);

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "downloadFile(" + endpoint + " failed: " + response.statusCode() + ": " + response.body());
        }

        // Normalize EOL
        String fileData = response.body().replaceAll("\\r\\n?", "\n");

        // Ensure that the file data ends with a newline.
        if (! fileData.endsWith("\n")) {
            fileData += "\n";
        }

        return fileData;
    }

    // Download the raw bytes of an upload (e.g. an image) given its Discourse upload URL.
    // Unlike downloadFile, this does not normalize line endings, so binary data is returned intact.
    // The url may be a site-relative path (e.g. "/uploads/...") or an absolute (CDN) URL.
    byte[] downloadImage(final String url) {

        // Discourse's uploads.url can be absolute (http(s)://...), protocol-relative (//host/path,
        // e.g. an S3/CDN URL), site-relative (/uploads/...), or relative (uploads/...).
        String endpoint;
        if (url.startsWith("http")) {
            endpoint = url;
        } else if (url.startsWith("//")) {
            endpoint = "https:" + url;
        } else if (url.startsWith("/")) {
            endpoint = Constants.BASE_URL + url.substring(1);
        } else {
            endpoint = Constants.BASE_URL + url;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .build();

        HttpResponse<byte[]> response = send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "downloadImage(" + endpoint + ") failed: " + response.statusCode());
        }

        return response.body();
    }

    String upload(String fileName) throws URISyntaxException {
        String clientId = "1234b591bb4848dd899b6e6ee0feaff9";

        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
                .addPart("upload_type",
                        new String("composer".getBytes(Charset.defaultCharset()), StandardCharsets.UTF_8))
                .addPart("client_id",
                        new String(clientId.getBytes(Charset.defaultCharset()), StandardCharsets.UTF_8))
                .addPart("files[]", () -> {
                    try {
                        return new FileInputStream(Path.of(fileName).toFile());
                    } catch (FileNotFoundException e) {
                        throw new MemberDataException("upload failed", e);
                    }
                }, fileName, "text/plain");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(Constants.UPLOAD_ENDPOINT))
                .setHeader("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + publisher.getBoundary())
                .setHeader("Api-Key", apiKey)
                .setHeader("Api-Username", apiUser)
                .POST(publisher.build())
                .build();

        HttpResponse<String> response = send(request);
        return response.body();
    }
}
