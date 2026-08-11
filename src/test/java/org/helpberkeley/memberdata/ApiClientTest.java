//
// Copyright (c) 2020-2021 helpberkeley.org
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

import org.junit.After;
import org.junit.Test;

import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class ApiClientTest extends TestBase {

    @Test
    public void missingAPIUserNameTest() {
        Properties properties = new Properties();
        Throwable thrown = catchThrowable(() -> new ApiClient(properties));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Missing ");
        assertThat(thrown).hasMessageContaining(Constants.API_KEY_PROPERTY);
        assertThat(thrown).hasMessageContaining(Constants.API_USER_PROPERTY);
    }

    @Test
    public void getGroupsQueryTest() {
        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_GROUPS_ID);
    }

    @Test
    public void groupsQueryTest() {
        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_GROUPS_ID);
    }

    @Test
    public void usersQueryTest() {
        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.CURRENT_USERS_QUERY);
    }

    @Test
    public void getErrorTest() {
        long postId = 1234567;
        ApiClient apiClient = createApiSimulator();
        Throwable thrown = catchThrowable(() -> apiClient.getPost(postId));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(String.valueOf(postId));
        assertThat(thrown).hasMessageContaining("not found");
    }

    @Test
    public void goawayRetrySucceedTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.GOAWAY_IOEXCEPTION, 1);

        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_EMAILS);
    }

    @Test
    public void goawayRetryFailTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.GOAWAY_IOEXCEPTION, 10);

        ApiClient apiClient = createApiSimulator();
        Throwable thrown = catchThrowable(() -> apiClient.runQuery(Constants.QUERY_GET_EMAILS));
        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown).hasMessageContaining("10 attempts to talk with Discourse failed");
    }

    @Test
    public void tooManyTimesRetrySucceedTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.TOO_MANY_TIMES_429_RESULT, 1);

        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_EMAILS);
    }

    @Test
    public void tooManyTimesRetryFailTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.TOO_MANY_TIMES_429_RESULT, 10);

        ApiClient apiClient = createApiSimulator();
        Throwable thrown = catchThrowable(() -> apiClient.runQuery(Constants.QUERY_GET_EMAILS));
        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown).hasMessageContaining("10 attempts to talk with Discourse failed");
    }

    @Test
    public void serviceUnavailableRetrySucceedTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.SERVICE_UNAVAILABLE, 1);

        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_EMAILS);
    }

    @Test
    public void serviceUnavailableRetryFailTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.SERVICE_UNAVAILABLE, 10);

        ApiClient apiClient = createApiSimulator();
        Throwable thrown = catchThrowable(() -> apiClient.runQuery(Constants.QUERY_GET_EMAILS));
        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown).hasMessageContaining("10 attempts to talk with Discourse failed");
    }

    @Test
    public void queryWithParamsTest() {
        ApiClient apiClient = createApiSimulator();

        String result = apiClient.runQueryWithParam(Constants.CURRENT_USERS_QUERY, "limit", "100");
        // FIX THIS, DS: update when query parameters are working with Discourse
    }

    @Test
    public void paramsToJsonTest() {
        assertThat(ApiClient.paramsToJson(Map.of("topic_id", "8506")))
                .isEqualTo("{\"topic_id\":\"8506\"}");

        // Values containing JSON metacharacters are escaped.
        assertThat(ApiClient.paramsToJson(Map.of("name", "a\"b\\c")))
                .isEqualTo("{\"name\":\"a\\\"b\\\\c\"}");

        // Multiple parameters, in insertion order.
        Map<String, String> params = new LinkedHashMap<>();
        params.put("topic_id", "8506");
        params.put("months_ago", "1");
        assertThat(ApiClient.paramsToJson(params))
                .isEqualTo("{\"topic_id\":\"8506\",\"months_ago\":\"1\"}");
    }

    @Test
    public void downloadImageReturnsRawBytesTest() throws Exception {
        ApiClient apiClient = createApiSimulator();

        byte[] expected = Files.readAllBytes(Paths.get(Thread.currentThread()
                .getContextClassLoader().getResource("test-image.png").toURI()));

        // A site-relative upload URL.
        byte[] relative = apiClient.downloadImage("/uploads/default/original/3X/a/b/test-image.png");
        // An absolute (CDN) upload URL.
        byte[] absolute = apiClient.downloadImage(
                Constants.BASE_URL + "uploads/default/original/3X/a/b/test-image.png");
        // A protocol-relative S3/CDN upload URL, as returned by Discourse's uploads.url.
        byte[] protocolRelative = apiClient.downloadImage(
                "//cdck-file-uploads-us1.s3.dualstack.us-west-2.amazonaws.com"
                        + "/flex020/uploads/helpberkeley/original/2X/e/test-image.png");

        // Bytes must be returned intact - no EOL normalization, no charset round-trip.
        assertThat(relative).isEqualTo(expected);
        assertThat(absolute).isEqualTo(expected);
        assertThat(protocolRelative).isEqualTo(expected);
    }

    @Test
    public void runQueryWithParamSendsParamsAndLimitTest() {
        ApiClient apiClient = createApiSimulator();

        apiClient.runQueryWithParam(Constants.CURRENT_USERS_QUERY, "topic_id", "8506");

        String body = HttpClientSimulator.lastQueryRequestBody;
        // The parameters must be sent as a single "params" JSON field, plus a "limit" field.
        assertThat(body).contains("name=\"params\"");
        assertThat(body).contains("{\"topic_id\":\"8506\"}");
        assertThat(body).contains("name=\"limit\"");
        assertThat(body).contains("10000");
    }

    @Test
    public void deletePostTest() {
        HttpClientSimulator.clearDeleteRequests();
        ApiClient apiClient = createApiSimulator();

        assertThat(apiClient.deletePost(71301, false).statusCode()).isEqualTo(HTTP_OK);
        assertThat(apiClient.deletePost(71301, true).statusCode()).isEqualTo(HTTP_OK);

        // A soft delete carries no parameter. A permanent one adds force_destroy.
        assertThat(HttpClientSimulator.getDeleteRequests()).containsExactly(
                Constants.POSTS_BASE + "71301",
                Constants.POSTS_BASE + "71301" + Constants.FORCE_DESTROY);
    }

    @Test
    public void deletePostNotFoundTest() {
        HttpClientSimulator.clearDeleteRequests();
        HttpClientSimulator.setDeleteResponseStatus(71301, HTTP_NOT_FOUND);
        ApiClient apiClient = createApiSimulator();

        // Already gone is not a failure - a retried delete whose first response was lost returns 404.
        assertThat(apiClient.deletePost(71301, true).statusCode()).isEqualTo(HTTP_NOT_FOUND);
    }

    @Test
    public void deletePostForbiddenTest() {
        HttpClientSimulator.clearDeleteRequests();
        HttpClientSimulator.setDeleteResponseStatus(71301, HTTP_FORBIDDEN);
        ApiClient apiClient = createApiSimulator();

        // Returned, not thrown - the caller waits out the permanent delete timer and retries.
        assertThat(apiClient.deletePost(71301, true).statusCode()).isEqualTo(HTTP_FORBIDDEN);
    }

    @Test
    public void deletePostFailureTest() {
        HttpClientSimulator.clearDeleteRequests();
        // A client error, so it is reported straight away. Server errors are retried instead -
        // see internalErrorIsRetriedTest.
        HttpClientSimulator.setDeleteResponseStatus(71301, HTTP_BAD_REQUEST);
        ApiClient apiClient = createApiSimulator();

        Throwable thrown = catchThrowable(() -> apiClient.deletePost(71301, true));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("deletePost");
        assertThat(thrown).hasMessageContaining(String.valueOf(HTTP_BAD_REQUEST));
    }

    @Test
    public void deletePostRetryTest() {
        HttpClientSimulator.clearDeleteRequests();
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.SERVICE_UNAVAILABLE, 1);
        ApiClient apiClient = createApiSimulator();

        // DELETE goes through the same retry path as every other request.
        assertThat(apiClient.deletePost(71301, false).statusCode()).isEqualTo(HTTP_OK);
    }

    // Retry nap length. The tests above zero RETRY_NAP_MILLISECONDS and it is a static shared by
    // every test class in the fork, so these set what they depend on and restoreRetryNap() puts
    // the default back.
    @After
    public void restoreRetryNap() {
        ApiClient.RETRY_NAP_MILLISECONDS = DEFAULT_RETRY_NAP_MILLISECONDS;
    }

    private static final long DEFAULT_RETRY_NAP_MILLISECONDS = TimeUnit.SECONDS.toMillis(10);
    // Discourse reports whole seconds rounded down, so ApiClient adds a second of margin.
    private static final long MARGIN = TimeUnit.SECONDS.toMillis(1);

    private static HttpResponse<String> rateLimited(String body, Map<String, List<String>> headers) {
        return HttpClientSimulator.response(body, Constants.HTTP_TOO_MANY_REQUESTS, headers);
    }

    private static Map<String, List<String>> retryAfter(long seconds) {
        return Map.of("Retry-After", List.of(String.valueOf(seconds)));
    }

    @Test
    public void retryAfterHeaderHonoredTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = DEFAULT_RETRY_NAP_MILLISECONDS;
        ApiClient apiClient = createApiSimulator();

        assertThat(apiClient.retryNapMillis(rateLimited("", retryAfter(3))))
                .isEqualTo(TimeUnit.SECONDS.toMillis(3) + MARGIN);
    }

    @Test
    public void waitSecondsFromBodyHonoredTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = DEFAULT_RETRY_NAP_MILLISECONDS;
        ApiClient apiClient = createApiSimulator();

        // Discourse's application level rate limiter answers with no Retry-After header.
        assertThat(apiClient.retryNapMillis(rateLimited(HttpClientSimulator.RATE_LIMITED_BODY, Map.of())))
                .isEqualTo(TimeUnit.SECONDS.toMillis(HttpClientSimulator.RATE_LIMITED_WAIT_SECONDS) + MARGIN);
    }

    @Test
    public void retryAfterHeaderPreferredOverBodyTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = DEFAULT_RETRY_NAP_MILLISECONDS;
        ApiClient apiClient = createApiSimulator();

        assertThat(apiClient.retryNapMillis(
                rateLimited(HttpClientSimulator.RATE_LIMITED_BODY, retryAfter(7))))
                .isEqualTo(TimeUnit.SECONDS.toMillis(7) + MARGIN);
    }

    @Test
    public void noRetryHintFallsBackToFixedNapTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = DEFAULT_RETRY_NAP_MILLISECONDS;
        ApiClient apiClient = createApiSimulator();

        assertThat(apiClient.retryNapMillis(rateLimited("We are having technical difficulties", Map.of())))
                .isEqualTo(DEFAULT_RETRY_NAP_MILLISECONDS);
    }

    @Test
    public void unparsableRetryAfterFallsBackTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = DEFAULT_RETRY_NAP_MILLISECONDS;
        ApiClient apiClient = createApiSimulator();

        // The HTTP-date form of Retry-After, which Discourse does not use.
        Map<String, List<String>> headers = Map.of("Retry-After", List.of("Wed, 21 Oct 2015 07:28:00 GMT"));

        assertThat(apiClient.retryNapMillis(rateLimited("", headers)))
                .isEqualTo(DEFAULT_RETRY_NAP_MILLISECONDS);
    }

    @Test
    public void nonStringBodyFallsBackTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = DEFAULT_RETRY_NAP_MILLISECONDS;
        ApiClient apiClient = createApiSimulator();

        // Image downloads ask for a byte[] body. There is nothing to parse, and nothing may throw.
        HttpResponse<byte[]> response = HttpClientSimulator.response(
                new byte[] { 1, 2, 3 }, Constants.HTTP_TOO_MANY_REQUESTS, Map.of());

        assertThat(apiClient.retryNapMillis(response)).isEqualTo(DEFAULT_RETRY_NAP_MILLISECONDS);
    }

    @Test
    public void retryNapIsCappedTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = DEFAULT_RETRY_NAP_MILLISECONDS;
        ApiClient apiClient = createApiSimulator();

        assertThat(apiClient.retryNapMillis(rateLimited("", retryAfter(TimeUnit.DAYS.toSeconds(1)))))
                .isEqualTo(ApiClient.MAX_RETRY_NAP_MILLISECONDS);
    }

    @Test
    public void zeroedRetryNapIgnoresTheServerTest() {
        // How the tests disable napping. It has to win over anything the server asked for, or an
        // injected Retry-After stalls the suite.
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        ApiClient apiClient = createApiSimulator();

        assertThat(apiClient.retryNapMillis(rateLimited("", retryAfter(30)))).isEqualTo(0);
    }

    @Test
    public void rateLimitedRequestsCountedTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.TOO_MANY_TIMES_429_RESULT, 2);
        ApiClient apiClient = createApiSimulator();

        apiClient.runQuery(Constants.QUERY_GET_EMAILS);

        assertThat(apiClient.backpressureResponses()).isEqualTo(2);
    }

    @Test
    public void serviceUnavailableIsBackpressureTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.SERVICE_UNAVAILABLE, 1);
        ApiClient apiClient = createApiSimulator();

        apiClient.runQuery(Constants.QUERY_GET_EMAILS);

        // A bulk write job is usually the reason the site is unhealthy, so it must slow down.
        assertThat(apiClient.backpressureResponses()).isEqualTo(1);
    }

    @Test
    public void internalErrorIsRetriedTest() {
        HttpClientSimulator.clearDeleteRequests();
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        // One 500 then success. A transient upstream 500 once aborted a 4,000 post run.
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.INTERNAL_ERROR, 1);
        ApiClient apiClient = createApiSimulator();

        assertThat(apiClient.deletePost(71301, false).statusCode()).isEqualTo(HTTP_OK);
        assertThat(apiClient.backpressureResponses()).isEqualTo(1);
    }

    @Test
    public void persistentInternalErrorReportsTheStatusTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailure(HttpClientSimulator.SendFailType.INTERNAL_ERROR, 10);
        ApiClient apiClient = createApiSimulator();

        Throwable thrown = catchThrowable(() -> apiClient.runQuery(Constants.QUERY_GET_EMAILS));
        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown).hasMessageContaining("10 attempts");
        // Giving up must say what it was failing with, not just that it gave up.
        assertThat(thrown).hasMessageContaining(String.valueOf(HTTP_INTERNAL_ERROR));
    }
}
