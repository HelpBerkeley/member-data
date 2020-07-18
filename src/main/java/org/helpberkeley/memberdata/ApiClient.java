//
// Copyright (c) 2020 helpberkeley.org
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
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import static java.net.HttpURLConnection.HTTP_OK;

public class ApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

    private static final String BASE_URL = "https://go.helpberkeley.org/";
    private static final String POSTS_ENDPOINT = BASE_URL + "posts.json";
    static final String POSTS_BASE = BASE_URL + "posts/";
    private static final String UPLOAD_ENDPOINT = BASE_URL + "uploads.json";
    private static final String DOWNLOAD_ENDPOINT = BASE_URL + "uploads/short-url/";
    static final String QUERY_BASE = BASE_URL + "admin/plugins/explorer/queries/";

    private final String apiUser;
    private final String apiKey;
    private final HttpClient client;

    static HttpClientFactory httpClientFactory = null;

    ApiClient(final Properties properties) {

        apiUser = properties.getProperty(Main.API_USER_PROPERTY);
        apiKey = properties.getProperty(Main.API_KEY_PROPERTY);
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
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .authenticator(authenticator)
                    .build();
        }
    }

    ApiClient(final Properties properties, HttpClient httpClient) {

        apiUser = properties.getProperty(Main.API_USER_PROPERTY);
        apiKey = properties.getProperty(Main.API_KEY_PROPERTY);
        auditAPIKey();
        this.client = httpClient;
    }

    private void auditAPIKey() {
        if ((apiUser == null) || (apiKey == null)) {
            throw new MemberDataException("Missing "
                    + Main.API_USER_PROPERTY
                    + " property or "
                    + Main.API_KEY_PROPERTY
                    + " or both");
        }
    }

    private HttpResponse<String> get(final String endpoint) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "post(" + POSTS_ENDPOINT + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    HttpResponse<String> post(final String json) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POSTS_ENDPOINT))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "post(" + POSTS_ENDPOINT + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response;
    }

    String runQuery(int queryId) throws IOException, InterruptedException {

        String endpoint = QUERY_BASE + queryId + "/run";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "multipart/form-data")
                .POST(HttpRequest.BodyPublishers.ofString("limit=1000000"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "runQuery(" + endpoint + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    String runQueryWithParam(int queryId, String paramName, String paramValue) throws IOException, InterruptedException {

        String endpoint = QUERY_BASE + queryId + "/run";

        // String body = "limit=1000000;" + paramName + '=' + paramValue;
        // String body = "{params={\"" + paramName + "\":\"" + paramValue + "\"}";
        String body = "\"" + paramName + "\":\"" + paramValue + "\"";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "multipart/form-data")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HTTP_OK) {
            throw new MemberDataException(
                    "runQuery(" + endpoint + " failed: " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    String getPost(long postId) throws IOException, InterruptedException {
        String endpoint = POSTS_BASE + postId + ".json";
        // Normalize EOL
        return get(endpoint).body().replaceAll("\\r\\n?", "\n");
    }

    HttpResponse<String> updatePost(long postId, final String body) throws IOException, InterruptedException {

        String endpoint =  POSTS_BASE + postId;
        String postBody = "{ \"post\" : { \"raw\" : \"" + body + "\" } }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(postBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    String downloadFile(final String shortURLFileName) throws IOException, InterruptedException {

        String endpoint = DOWNLOAD_ENDPOINT + shortURLFileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .build();

        HttpResponse<String> response =  client.send(request, HttpResponse.BodyHandlers.ofString());

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

    void uploadFile(final String fileName, final String fileData) throws IOException, InterruptedException {

        // FIX THIS, DS: generate unique id? Not strictly necessary here
        String boundary = "---------------------------86904839212366218363208480977";
        String contentType = "multipart/form-data; boundary=" + boundary;

        StringBuilder body = new StringBuilder();
        body.append(boundary).append("\r\n");
        body.append(boundary).append("Content-Disposition: form-data; name=\"type\"\r\n\r\ncomposer\r\n");
        body.append(boundary).append("\r\n");
        body.append("Content-Disposition: form-data; name=\"files[]\"; filename=\"").append(fileName).append("\"\r\n");
        body.append("Content-Disposition: form-data; name=\"files[]\"; filename=\"").append(fileName).append("\"\r\n");
        body.append("Content-Type: text/csv\r\n\r\n");
        body.append(fileData).append("\r\n");
        body.append(boundary).append("--\r\n");

        StringBuilder filesArg = new StringBuilder();

        // form parameters
//        data.put("client_id", "1234b591bb4848dd899b6e6ee0feaff9");

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .uri(URI.create(UPLOAD_ENDPOINT))
                .header("Content-Type", contentType)
                .header("Api-Username", apiUser)
                .header("Api-Key", apiKey)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // print status code
        System.out.println(response.statusCode());

        // print response body
        System.out.println(response.body());
    }

    private HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
