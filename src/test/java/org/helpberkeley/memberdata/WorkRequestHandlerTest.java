/*
 * Copyright (c) 2020. helpberkeley.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package org.helpberkeley.memberdata;

import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class WorkRequestHandlerTest extends TestBase {

    private final ApiClient apiClient;

    public WorkRequestHandlerTest() throws IOException {
        apiClient = createApiSimulator();
    }

    @Test
    public void parseWorkRequestTest() throws IOException, InterruptedException {

        Query query = new Query(
                Constants.QUERY_GET_LAST_ROUTED_WORKFLOW_REPLY, Constants.TOPIC_ROUTED_WORKFLOW_DATA);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);
        WorkRequestHandler.Reply reply = requestHandler.getLastReply();
        assertThat(reply).isInstanceOf(WorkRequestHandler.WorkRequest.class);
    }

    @Test
    public void parseStatusTest() throws IOException, InterruptedException {
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_LAST_ROUTED_WORKFLOW_REPLY, "last-routed-workflow-status.json");

        Query query = new Query(
                Constants.QUERY_GET_LAST_ROUTED_WORKFLOW_REPLY, Constants.TOPIC_ROUTED_WORKFLOW_DATA);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);
        WorkRequestHandler.Reply reply = requestHandler.getLastReply();
        assertThat(reply).isInstanceOf(WorkRequestHandler.Status.class);
    }

    @Test
    public void postStatusTest() throws IOException, InterruptedException {
        Query query = new Query(
                Constants.QUERY_GET_LAST_ROUTED_WORKFLOW_REPLY, Constants.TOPIC_ROUTED_WORKFLOW_DATA);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);
        WorkRequestHandler.Reply reply = requestHandler.getLastReply();
        assertThat(reply).isInstanceOf(WorkRequestHandler.WorkRequest.class);

        String statusMessage = "Somewhere the quick brown fox\nand the lazy dog\nare taking the day off\n";
        ((WorkRequestHandler.WorkRequest)reply).postStatus(
                WorkRequestHandler.RequestStatus.Processing, statusMessage);
    }

    @Test
    public void parseUnrecognizedPostTest() {
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_LAST_ROUTED_WORKFLOW_REPLY, "bad-work-request.json");

        Query query = new Query(
                Constants.QUERY_GET_LAST_ROUTED_WORKFLOW_REPLY, Constants.TOPIC_ROUTED_WORKFLOW_DATA);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);
        Throwable thrown = catchThrowable(requestHandler::getLastReply);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Post #9 in Routed Workflow Data is not "
                + "recognizable as either a work request or a status message");
    }
}
