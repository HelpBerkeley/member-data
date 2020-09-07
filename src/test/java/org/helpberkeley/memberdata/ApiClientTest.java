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

import org.junit.Test;

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
    public void getGroupsQueryTest() throws InterruptedException {
        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_GROUPS_ID);
    }

    @Test
    public void groupsQueryTest() throws InterruptedException {
        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_GROUPS_ID);
    }

    @Test
    public void usersQueryTest() throws InterruptedException {
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
    public void retrySucceedTest() throws InterruptedException {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailureCount(1);

        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_EMAILS);
    }

    @Test
    public void retryFailTest() {
        ApiClient.RETRY_NAP_MILLISECONDS = 0;
        HttpClientSimulator.setSendFailureCount(10);

        ApiClient apiClient = createApiSimulator();
        Throwable thrown = catchThrowable(() -> apiClient.runQuery(Constants.QUERY_GET_EMAILS));
        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown).hasMessageContaining("10 attempts to talk with Discourse failed");
    }
}