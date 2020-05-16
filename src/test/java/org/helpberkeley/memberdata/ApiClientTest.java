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

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class ApiClientTest extends TestBase {

    @Test
    public void getGroupsQueryTest() throws IOException, InterruptedException {
        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_GROUPS_ID);
    }

    @Test
    public void groupsQueryTest() throws IOException, InterruptedException {
        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.QUERY_GET_GROUPS_ID);
    }

    @Test
    public void usersQueryTest() throws IOException, InterruptedException {
        ApiClient apiClient = createApiSimulator();
        apiClient.runQuery(Constants.CURRENT_USERS_QUERY);
    }

    @Test
    public void getErrorTest() throws IOException {
        long postId = 1234567;
        ApiClient apiClient = createApiSimulator();
        Throwable thrown = catchThrowable(() -> apiClient.getPost(postId));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(String.valueOf(postId));
        assertThat(thrown).hasMessageContaining("not found");
    }
}
