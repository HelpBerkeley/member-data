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
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpClientSimulator extends HttpClient {

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {

        if (isQuery(request)) {
            return doQuery(request);
        } else if (isPost(request)) {
            return doPost(request);
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

    private <T> HttpResponse<T> doQuery(HttpRequest request) {

        String dataFile;
        int queryId = getQueryId(request);

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
            default:
                throw new RuntimeException("FIX THIS: query " + queryId + " not supported by the simulator");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(dataFile);
        try {
            return (HttpResponse<T>) new HttpResponseSimulator<String>(Files.readString(Paths.get(url.toURI())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    private <T> HttpResponse<T> doPost(HttpRequest request) {
        return (HttpResponse<T>) new HttpResponseSimulator<String>("");
    }

    private int getQueryId(HttpRequest request) {
        assertThat(isQuery(request)).isTrue();

        String uri = request.uri().toString();
        int index = uri.lastIndexOf("/run");
        assertThat(index).isNotEqualTo(-1);
        String queryId = uri.substring(0, index);
        index = queryId.lastIndexOf('/');
        queryId = queryId.substring(index + 1);

        return Integer.parseInt(queryId);
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

        HttpResponseSimulator(final String responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public int statusCode() {
            return HTTP_OK;
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
