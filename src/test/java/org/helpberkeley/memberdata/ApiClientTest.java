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

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

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
}
