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
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_OK;

public class ApiClient {
    private static final String BASE_URL = "https://go.helpberkeley.org/";
    private static final String POSTS_ENDPOINT = BASE_URL + "posts.json";
    static final String POSTS_BASE = BASE_URL + "posts/";
    static final String UPLOAD_ENDPOINT = BASE_URL + "uploads.json";
    private static final String DOWNLOAD_ENDPOINT = BASE_URL + "uploads/short-url/";
    static final String QUERY_BASE = BASE_URL + "admin/plugins/explorer/queries/";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

    private final String apiUser;
    private final String apiKey;
    private final HttpClient client;

    // Test support
    static HttpClientFactory httpClientFactory = null;
    static long RETRY_NAP_MILLISECONDS = TimeUnit.SECONDS.toMillis(10);

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

        for (int retry = 0; retry < 10; retry++ ) {
            //noinspection LoggingSimilarMessage
            try {
                HttpResponse <String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                switch (response.statusCode()) {
                    case Constants.HTTP_TOO_MANY_REQUESTS:
                    case Constants.HTTP_SERVICE_UNAVAILABLE:
                        LOGGER.warn("send {} failed: {}", request, response.body());
                        break;
                    default:
                        return response;
                }
            } catch (IOException ex) {
                LOGGER.warn("send {} failed: {}", request, ex.getMessage());
            } catch (InterruptedException ex) {
                throw new RuntimeException("send " + request + " was interrupted");
            }

            if (retry < 9) {
                LOGGER.warn("Failure talking to Discourse, waiting 10 seconds and retrying.");
                nap(RETRY_NAP_MILLISECONDS);
            }
        }

        LOGGER.warn("10th retry failure seen from Discourse, exiting with a failure");
        throw new RuntimeException("10 attempts to talk with Discourse failed");
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
                    "post(" + POSTS_ENDPOINT + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    HttpResponse<String> post(final String json) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POSTS_ENDPOINT))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = send(request);

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "post(" + POSTS_ENDPOINT + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    public String runQuery(int queryId) {
        return doRunQuery(queryId);
//        return runQueryWithParam(queryId, "limit", "100000");
    }

    private String doRunQuery(int queryId) {

        String endpoint = QUERY_BASE + queryId + "/run";


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "multipart/form-data")
                .POST(HttpRequest.BodyPublishers.ofString("limit=1000000"))
                .build();



        HttpResponse<String> response = send(request);

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "runQuery(" + endpoint + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    String runQueryWithParam(int queryId, String paramName, String paramValue) {

        String endpoint = QUERY_BASE + queryId + "/run";

        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
                .addParamPart(paramName, paramValue);
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

    String getPost(long postId) {
        String endpoint = POSTS_BASE + postId + ".json";
        // Normalize EOL
        return get(endpoint).body().replaceAll("\\r\\n?", "\n");
    }

    public HttpResponse<String> updatePost(long postId, final String body) {

        String endpoint =  POSTS_BASE + postId;
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

    String downloadFile(final String shortURLFileName) {

        String endpoint = DOWNLOAD_ENDPOINT + shortURLFileName;

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

    String upload(String fileName) throws URISyntaxException {
        String clientId = "1234b591bb4848dd899b6e6ee0feaff9";

        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher()
                .addPart("type",
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
                .uri(new URI(UPLOAD_ENDPOINT))
                .setHeader("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + publisher.getBoundary())
                .setHeader("Api-Key", apiKey)
                .setHeader("Api-Username", apiUser)
                .POST(publisher.build())
                .build();

        HttpResponse<String> response = send(request);
        return response.body();
    }
}
