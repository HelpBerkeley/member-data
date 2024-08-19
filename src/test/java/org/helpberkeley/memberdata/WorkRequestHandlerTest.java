/*
// Copyright (c) 2020-2021 helpberkeley.org
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

import java.text.MessageFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class WorkRequestHandlerTest extends TestBase {

    private final ApiClient apiClient;
    private final int queryId = Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY;

    public WorkRequestHandlerTest() {
        apiClient = createApiSimulator();
    }

    @Test
    public void parseWorkRequestTest() {

        Query query = new Query(queryId, Constants.TOPIC_REQUEST_DRIVER_MESSAGES);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);
        WorkRequestHandler.Reply reply = requestHandler.getLastReply();
        assertThat(reply).isInstanceOf(WorkRequestHandler.WorkRequest.class);
    }

    @Test
    public void parseWorkRequestWithoutDateTest() {

        HttpClientSimulator.setQueryResponseFile(queryId, "workrequest-no-date.json");
        Query query = new Query(queryId, Constants.TOPIC_REQUEST_DRIVER_MESSAGES);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);
        Throwable thrown = catchThrowable(requestHandler::getLastReply);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(WorkRequestHandler.ERROR_INVALID_DATE);
    }

    @Test
    public void parseStatusTest() {
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, "last-routed-workflow-status.json");

        Query query = new Query(queryId, Constants.TOPIC_REQUEST_DRIVER_MESSAGES);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);
        WorkRequestHandler.Reply reply = requestHandler.getLastReply();
        assertThat(reply).isInstanceOf(WorkRequestHandler.Status.class);
    }

    @Test
    public void postStatusTest() {
        Query query = new Query(queryId, Constants.TOPIC_REQUEST_DRIVER_MESSAGES);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);
        WorkRequestHandler.Reply reply = requestHandler.getLastReply();
        assertThat(reply).isInstanceOf(WorkRequestHandler.WorkRequest.class);

        String statusMessage = "Somewhere the quick brown fox\nand the lazy dog\nare taking the day off\n";
        ((WorkRequestHandler.WorkRequest)reply).postStatus(
                WorkRequestHandler.RequestStatus.Processing, statusMessage);
    }

    @Test
    public void versionTest() {
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, "v1-work-request.json");
        Query query = new Query(queryId, Constants.TOPIC_REQUEST_DRIVER_MESSAGES);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);

        WorkRequestHandler.Reply reply = requestHandler.getLastReply();
        assertThat(reply).isInstanceOf(WorkRequestHandler.WorkRequest.class);
        WorkRequestHandler.WorkRequest workRequest = (WorkRequestHandler.WorkRequest)reply;
        assertThat(workRequest.version).isEqualTo("1");
    }

    @Test
    public void badTopicDirectiveTest() {
        String badURL = "go.helpberkeley.org/t/";
        String driverMessagesRequest =
                "{ \"success\": true, \"columns\": [ \"post_number\", \"deleted_at\", \"raw\" ], "
                        + "\"rows\": [ "
                        + "[ 1, null, \""
                        + "2021/01/01"
                        + "\nTopic: " + badURL + "\n"
                        + "[xyzzy.csv|attachment](upload://routed-deliveries-v200.csv) (5.8 KB)\" ] "
                        + "] }";
        HttpClientSimulator.setQueryResponseData(
                Constants.QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY, driverMessagesRequest);

        Query query = new Query(queryId, Constants.TOPIC_REQUEST_DRIVER_MESSAGES);
        WorkRequestHandler requestHandler = new WorkRequestHandler(apiClient, query);

        Throwable thrown = catchThrowable(requestHandler::getLastReply);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(MessageFormat.format(HBParser.INVALID_TOPIC_URL, badURL));

    }
}
