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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
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
import java.util.concurrent.atomic.AtomicInteger;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HttpClientSimulator extends HttpClient {

    enum SendFailType {
        GOAWAY_IOEXCEPTION,
        TOO_MANY_TIMES_429_RESULT,
        SERVICE_UNAVAILABLE,
    }

    private static final Map<Integer, String> queryResponseFiles = new HashMap<>();
    private static final Map<Integer, String> queryResponseData = new HashMap<>();
    private static final Map<String, String> getResponseFiles = new HashMap<>();
    private static final Map<String, String> getResponseData = new HashMap<>();
    private static final Map<String, String> postResponseData = new HashMap<>();
    private static String getFileName = null;
    private static final AtomicInteger sendFailCount = new AtomicInteger(0);
    private static SendFailType sendFailType = null;

    static void setQueryResponseFile(int queryId, final String fileName) {
        queryResponseFiles.put(queryId, fileName);
    }
    public static void setQueryResponseData(int queryId, final String queryData) {
        queryResponseData.put(queryId, queryData);
    }

    static void setSendFailure(SendFailType failureType, int numFailures) {
        sendFailType = failureType;
        sendFailCount.set(numFailures);
    }

    public static void setGetResponseData(String uri, String data) {
        getResponseData.put(uri, data);
    }

    public static void setPostResponseData(String uri, String data) {
        postResponseData.put(uri, data);
    }

    public static void setGetResponseFile(String uri, String filename) {
        getResponseFiles.put(uri, filename);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> HttpResponse<T> send(HttpRequest request,
            HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {

        if (sendFailCount.get() > 0) {
            sendFailCount.decrementAndGet();

            if (sendFailType == SendFailType.GOAWAY_IOEXCEPTION) {
                throw new IOException("Simulated IOException: GOAWAY");
            } else if (sendFailType == SendFailType.SERVICE_UNAVAILABLE) {
                return (HttpResponse<T>) new HttpResponseSimulator<>(
                        "We are having technical difficulties", Constants.HTTP_SERVICE_UNAVAILABLE);
            } else {
                assertThat(sendFailType).isEqualTo(SendFailType.TOO_MANY_TIMES_429_RESULT);
                return (HttpResponse<T>) new HttpResponseSimulator<>(
                        "Too many times", Constants.HTTP_TOO_MANY_REQUESTS);
            }
        }

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
        return request.method().equals("POST") && request.uri().toString().startsWith(Constants.QUERY_BASE);
    }

    private boolean isPost(HttpRequest request) {

        // FIX THIS, DS: is there a constant for this?
        return request.method().equals("POST") && (! request.uri().toString().startsWith(Constants.QUERY_BASE));
    }

    private boolean isPut(HttpRequest request) {
        // FIX THIS, DS: is there a constant for this?
        return request.method().equals("PUT") && (request.uri().toString().startsWith(Constants.POSTS_BASE));
    }

    private boolean isGet(HttpRequest request) {
        // FIX THIS, DS: is there a constant for this?
        return request.method().equals("GET");
    }

    private <T> HttpResponse<T> doQuery(HttpRequest request) {

        int queryId = getQueryId(request);
        String responseData;

        // Support for tests overriding the query response either with
        // a string or a file.
        //
        if (queryResponseData.containsKey(queryId)) {
            responseData = queryResponseData.remove(queryId);
        } else {
            String dataFile = getQueryResponseFile(queryId);
            responseData = readFile(dataFile);
        }

        //noinspection unchecked
        return (HttpResponse<T>) new HttpResponseSimulator<>(responseData);
    }

    private String getQueryResponseFile(int queryId) {

        String dataFile = queryResponseFiles.remove(queryId);

        if (dataFile != null) {
            return dataFile;
        }

        switch (queryId) {
            case Constants.QUERY_GET_GROUPS_ID:
                dataFile = "groups.json";
                break;
            case Constants.CURRENT_GET_GROUP_USERS_QUERY:
                dataFile = "group-users.json";
                break;
            case Constants.CURRENT_USERS_QUERY:
                dataFile = "users.json";
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
            case Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23:
                dataFile = "driver-format-topic-v23.json";
                break;
            case Constants.QUERY_GET_DRIVERS_POST_FORMAT_V24:
                dataFile = "driver-format-topic-v24.json";
                break;
            case Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22:
                dataFile = "group-instructions-post-v22.json";
                break;
            case Constants.QUERY_GET_ONE_KITCHEN_DRIVERS_POST_FORMAT_V1:
                dataFile = "one-kitchen-driver-message-v10.json";
                break;
            case Constants.QUERY_GET_ONE_KITCHEN_DRIVERS_POST_FORMAT_V300:
                dataFile = "one-kitchen-driver-message-v300.json";
                break;
            case Constants.QUERY_GET_ONE_KITCHEN_GROUP_POST_FORMAT_V1:
                dataFile = "one-kitchen-group-format-v10.json";
                break;
            case Constants.QUERY_GET_ONE_KITCHEN_GROUP_POST_FORMAT_V300:
                dataFile = "one-kitchen-group-format-v300.json";
                break;
            case Constants.QUERY_GET_ONE_KITCHEN_DRIVERS_TABLE_POST_FORMAT_V300:
                dataFile = "one-kitchen-drivers-format-v300.json";
                break;
            case Constants.QUERY_GET_ONE_KITCHEN_ORDERS_TABLE_POST_FORMAT_V300:
                dataFile = "one-kitchen-orders-format-v300.json";
                break;
            case Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY:
                dataFile = "last-routed-workflow-reply.json";
                break;
            case Constants.QUERY_GET_LAST_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES_REPLY:
                dataFile = "last-request-one-kitchen-reply.json";
                break;
            case Constants.QUERY_GET_LAST_ROUTE_REQUEST_REPLY:
                dataFile = "last-request-driver-routes-reply.json";
                break;
            case Constants.QUERY_GET_BACKUP_DRIVER_FORMAT_V12:
                dataFile = "backup-driver-format-v12.json";
                break;
            case Constants.QUERY_GET_LAST_RESTAURANT_TEMPLATE_REPLY:
                dataFile = "restaurant-template-last-reply.json";
                break;
            case Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE:
                dataFile = "current-restaurant-template-post.json";
                break;
            case Constants.QUERY_GET_CURRENT_VALIDATED_ONE_KITCHEN_RESTAURANT_TEMPLATE:
                dataFile = "current-one-kitchen-restaurant-template-post.json";
                break;
            case Constants.QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY:
                dataFile = "last-completed-daily-orders-reply.json";
                break;
            case Constants.QUERY_GET_ORDER_HISTORY_DATA_POSTS:
                dataFile = "order-history-data.json";
                break;
            case Constants.QUERY_GET_ONE_KITCHEN_ORDER_HISTORY_DATA_POSTS:
                dataFile = "onekitchen-order-history-data.json";
                break;
            case Constants.QUERY_GET_DRIVER_DETAILS:
                dataFile = "driver-details-posts.json";
                break;
            case Constants.QUERY_GET_LAST_REPLY_FROM_REQUEST_TOPICS_V20:
            case Constants.QUERY_GET_LAST_REPLY_FROM_REQUEST_TOPICS_V21:
            case Constants.QUERY_GET_LAST_REPLY_FROM_REQUEST_TOPICS_V22:
            case Constants.QUERY_GET_LAST_REPLY_FROM_REQUEST_TOPICS_V23:
            case Constants.QUERY_GET_LAST_REPLY_FROM_REQUEST_TOPICS_V24:
            case Constants.QUERY_GET_LAST_REPLY_FROM_REQUEST_TOPICS_V25:
                dataFile = "last-replies-no-requests.json";
                break;
            case Constants.QUERY_GET_LAST_ONE_KITCHEN_RESTAURANT_TEMPLATE_REPLY:
                dataFile = "one-kitchen-restaurant-template-last-reply.json";
                break;
            default:
                throw new RuntimeException("FIX THIS: query " + queryId + " not supported by the simulator");
        }

        return dataFile;
    }

    private <T> HttpResponse<T> doPost(HttpRequest request) {
        String uri = request.uri().toString();
        String response;

        if (postResponseData.containsKey(uri)) {
            response = postResponseData.get(uri);
        } else if (request.uri().toString().endsWith(Constants.UPLOAD_ENDPOINT)) {
            response = readFile("upload-response.json");
        } else if (request.uri().toString().endsWith(Constants.CHANGE_OWNER)) {
            response = readFile("change-owner-response.json");
        } else {
            response = readFile("post-response.json");
        }

        //noinspection unchecked
        return (HttpResponse<T>) new HttpResponseSimulator<>(response);
    }

    private <T> HttpResponse<T> doPut(HttpRequest request) {
        //noinspection unchecked
        return (HttpResponse<T>) new HttpResponseSimulator<>("");
    }

    private <T> HttpResponse<T> doGet(HttpRequest request) {
        String uri = request.uri().toString();
        int index = uri.lastIndexOf('/');
        assertThat(index).as(uri).isNotEqualTo(-1);
        String fileName = uri.substring(index + 1);
        String fileContent = "";

        if (getResponseFiles.containsKey(uri)) {
            fileName = getResponseFiles.get(uri);
        } else if (getResponseData.containsKey(uri)) {
            fileContent = getResponseData.get(uri);
        } else if (fileName.endsWith(Main.ORDER_HISTORY_POST_ID + ".json")) {
            fileName = "order-history.json";
        } else if (fileName.equals(Main.RESTAURANT_TEMPLATE_POST_ID + ".json")) {
            fileName = "restaurant-template-post.json";
        } else if (fileName.equals(Main.DRIVER_HISTORY_POST_ID + ".json")) {
            fileName = "driver-history-post.json";
        } else if (fileName.equals(Main.ONE_KITCHEN_DRIVER_HISTORY_POST_ID + ".json")) {
            fileName = "driver-history-post.json";
        }

        try {
            //noinspection unchecked
            if (! fileContent.isEmpty()) {
                return (HttpResponse<T>) new HttpResponseSimulator<>(fileContent);
            } else {
                return (HttpResponse<T>) new HttpResponseSimulator<>(readFile(fileName));
            }
        } catch (RuntimeException ex) {
            //noinspection unchecked
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
