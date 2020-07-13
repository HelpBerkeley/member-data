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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpClientSimulator extends HttpClient {

    private static final Map<Integer, String> responseFiles = new HashMap<>();

    static void setQueryResponseFile(int queryId, final String fileName) {
        responseFiles.put(queryId, fileName);
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {

        if (isQuery(request)) {
            return doQuery(request);
        } else if (isPost(request)) {
            return doPost(request);
        } else if (isPut(request)) {
            return doPut(request);
        } else if (isGet(request)) {
            return doGet(request);
        }

        throw new RuntimeException("FIX THIS: request not support by simulator: " + request);
    }

    private boolean isQuery(HttpRequest request) {

        // FIX THIS, DS: is there a constant for this?
        return request.method().equals("POST") && request.uri().toString().startsWith(ApiClient.QUERY_BASE);
    }

    private boolean isPost(HttpRequest request) {

        // FIX THIS, DS: is there a constant for this?
        return request.method().equals("POST") && (! request.uri().toString().startsWith(ApiClient.QUERY_BASE));
    }

    private boolean isPut(HttpRequest request) {
        // FIX THIS, DS: is there a constant for this?
        return request.method().equals("PUT") && (request.uri().toString().startsWith(ApiClient.POSTS_BASE));
    }

    private boolean isGet(HttpRequest request) {
        // FIX THIS, DS: is there a constant for this?
        return request.method().equals("GET");
    }

    private <T> HttpResponse<T> doQuery(HttpRequest request) {

        int queryId = getQueryId(request);
        String dataFile = getQueryResponseFile(queryId);
        return (HttpResponse<T>) new HttpResponseSimulator<>(readFile(dataFile));
    }

    private String getQueryResponseFile(int queryId) {

        String dataFile = responseFiles.remove(queryId);

        if (dataFile != null) {
            return dataFile;
        }

        switch (queryId) {
            case Constants.QUERY_GET_GROUPS_ID:
                dataFile = "groups.json";
                break;
            case Constants.QUERY_GET_GROUP_USERS_ID:
                dataFile = "group-users.json";
                break;
            case Constants.CURRENT_USERS_QUERY:
                dataFile = "users.json";
                break;
            case Constants.QUERY_GET_DAILY_DELIVERIES:
                dataFile = "daily-deliveries.json";
                break;
            case Constants.QUERY_EMAIL_CONFIRMATIONS:
                dataFile = "email-tokens.json";
                break;
            case Constants.QUERY_GET_EMAILS:
                dataFile = "email-addresses.json";
                break;
            case Constants.QUERY_GET_DELIVERY_DETAILS:
                dataFile = "delivery-details.json";
                break;
            case Constants.QUERY_GET_DRIVERS_POST_FORMAT:
                dataFile = "driver-format-topic.json";
                break;
            case Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT:
                dataFile = "group-instructions-post.json";
                break;
            case Constants.QUERY_GET_LAST_ROUTED_WORKFLOW_REPLY:
                dataFile = "last-routed-workflow-reply.json";
                break;
            case Constants.QUERY_GET_LAST_ROUTE_REQUEST_REPLY:
                dataFile = "last-request-driver-routes-reply.json";
                break;
            default:
                throw new RuntimeException("FIX THIS: query " + queryId + " not supported by the simulator");
        }

        return dataFile;
    }

    private <T> HttpResponse<T> doPost(HttpRequest request) {
        String response = readFile("post-response.json");
        return (HttpResponse<T>) new HttpResponseSimulator<>(response);
    }

    private <T> HttpResponse<T> doPut(HttpRequest request) {
        return (HttpResponse<T>) new HttpResponseSimulator<>("");
    }

    private <T> HttpResponse<T> doGet(HttpRequest request) {
        String fileName = request.uri().toString();
        int index = fileName.lastIndexOf(File.separatorChar);
        assertThat(index).as(fileName).isNotEqualTo(-1);
        fileName = fileName.substring(index + 1);

        if (fileName.endsWith(Main.ORDER_HISTORY_POST_ID + ".json")) {
            fileName = "order-history.json";
        } else if (fileName.equals(Main.RESTAURANT_TEMPLATE_POST_ID + ".json")) {
            fileName = "restaurant-template-post.json";
        }

        try {
            return (HttpResponse<T>) new HttpResponseSimulator<>(readFile(fileName));
        } catch (RuntimeException ex) {
            return (HttpResponse<T>) new HttpResponseSimulator<>(ex.getMessage(), HTTP_NOT_FOUND);
        }
    }

    private int getQueryId(HttpRequest request) {
        assertThat(isQuery(request)).isTrue();

        String uri = request.uri().toString();
        int index = uri.lastIndexOf("/run");
        assertThat(index).as(uri).isNotEqualTo(-1);
        String queryId = uri.substring(0, index);
        index = queryId.lastIndexOf('/');
        queryId = queryId.substring(index + 1);

        return Integer.parseInt(queryId);
    }

    private String readFile(final String fileName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(fileName);

        if (url == null) {
            throw new RuntimeException("file " + fileName + " not found");
        }
        try {
            return (Files.readString(Paths.get(url.toURI())));
        } catch (IOException|URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return null;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        return null;
    }

    @Override
    public SSLParameters sslParameters() {
        return null;
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Version version() {
        return null;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        return null;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return null;
    }

    private static class HttpResponseSimulator<String> implements HttpResponse<String> {

        private final String responseBody;
        private final int statusCode;

        HttpResponseSimulator(final String responseBody) {
            this.responseBody = responseBody;
            this.statusCode = HTTP_OK;
        }

        HttpResponseSimulator(final String responseBody, int statusCode) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return null;
        }

        @Override
        public String body() {
            return responseBody;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return null;
        }

        @Override
        public Version version() {
            return null;
        }
    }
}
