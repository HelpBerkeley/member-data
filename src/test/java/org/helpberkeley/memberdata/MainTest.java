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

import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class MainTest extends TestBase {

    @BeforeClass
    public static void initialize() throws IOException, InterruptedException {
        cleanupGeneratedFiles();

        // Fetches files that will be used by the tests.
        String[] args = { Options.COMMAND_FETCH };
        Main.main(args);
    }

    @AfterClass
    public static void cleanup() throws IOException {
        cleanupGeneratedFiles();
    }

    private  static void cleanupGeneratedFiles() throws IOException {
        Files.list(Paths.get("."))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    String fileName = p.getFileName().toString();
                    if (fileName.endsWith(".csv") ||
                            (fileName.endsWith(".txt") && fileName.startsWith(Main.MEMBERDATA_ERRORS_FILE))) {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Test
    public void postUserErrorsTest() throws IOException, InterruptedException {
        String[] args = { Options.COMMAND_POST_ERRORS, TEST_FILE_NAME };
        Main.main(args);
    }

    @Test
    public void postMemberDataTest() throws IOException, InterruptedException {
        String memberDataFile = findFile(Main.MEMBERDATA_REPORT_FILE, "csv");
        assertThat(memberDataFile).isNotNull();

        String[] args = { Options.COMMAND_POST_ALL_MEMBERS, memberDataFile, TEST_SHORT_URL };
        Main.main(args);
    }

    @Test
    public void postDriversTest() throws IOException, InterruptedException {
        String file = findFile(Main.DRIVERS_FILE, "csv");
        assertThat(file).isNotNull();

        String[] args = { Options.COMMAND_POST_DRIVERS, file, TEST_SHORT_URL };
        Main.main(args);
    }

    @Test
    public void postConsumerRequestsTest() throws IOException, InterruptedException {
        String consumerRequestsFile = findFile(Main.CONSUMER_REQUESTS_FILE, "csv");
        String[] args = { Options.COMMAND_POST_CONSUMER_REQUESTS, consumerRequestsFile };
        Main.main(args);
    }

    @Test
    public void postVolunteerRequestsTest() throws IOException, InterruptedException {
        String consumerRequestsFile = findFile(Main.VOLUNTEER_REQUESTS_FILE, "csv");
        String[] args = { Options.COMMAND_POST_VOLUNTEER_REQUESTS, consumerRequestsFile };
        Main.main(args);
    }

    @Test
    public void postWorkFlowTest() throws IOException, InterruptedException {
        String workflowFile = findFile(Main.WORKFLOW_FILE, "csv");
        String[] args = { Options.COMMAND_POST_WORKFLOW, workflowFile, TEST_SHORT_URL };
        Main.main(args);
    }

    @Test
    public void postInreachTest() throws IOException, InterruptedException {
        String file = findFile(Main.INREACH_FILE, "csv");
        String[] args = { Options.COMMAND_POST_INREACH, file, TEST_SHORT_URL };
        Main.main(args);
    }

    @Test
    public void postDispatchersTest() throws IOException, InterruptedException {
        String file = findFile(Main.DISPATCHERS_FILE, "csv");
        String[] args = { Options.COMMAND_POST_DISPATCHERS, file, TEST_SHORT_URL };
        Main.main(args);
    }

    @Test
    public void updateDispatchersTest() throws IOException, InterruptedException {
        String dispatchersFile = findFile(Main.DISPATCHERS_FILE, "csv");
        String[] args = { Options.COMMAND_UPDATE_DISPATCHERS, dispatchersFile, TEST_SHORT_URL};
        Main.main(args);
    }

    @Test
    public void updateUserErrorsTest() throws IOException, InterruptedException {
        String errorsFile = findFile(Main.MEMBERDATA_ERRORS_FILE, "txt");
        String[] args = { Options.COMMAND_UPDATE_ERRORS, errorsFile };
        Main.main(args);
    }

    @Test
    public void getOrderHistoryTest() throws IOException, InterruptedException {
        String[] args = { Options.COMMAND_GET_ORDER_HISTORY };
        Main.main(args);
    }

    @Test
    public void getDailyDeliveriesTest() throws IOException, InterruptedException {
        String[] args = { Options.COMMAND_GET_DAILY_DELIVERIES };
        Main.main(args);
    }

    @Test
    public void mergeOrderHistoryTest() throws IOException, InterruptedException {

        String[] args = { Options.COMMAND_GET_ORDER_HISTORY };
        Main.main(args);
        String orderHistoryFile = findFile(Main.ORDER_HISTORY_FILE, "csv");

        args = new String[]{Options.COMMAND_GET_DAILY_DELIVERIES};
        Main.main(args);
        String deliveriesPostFile = findFile(Main.DELIVERY_POSTS_FILE, "csv");

        String usersFile = findFile(Main.MEMBERDATA_RAW_FILE, "csv");

        args = new String[] {
                Options.COMMAND_MERGE_ORDER_HISTORY,
                usersFile,
                orderHistoryFile,
                deliveriesPostFile
        };
        Main.main(args);
    }

    @Test
    @Ignore
    public void commandsWithFileTest() throws IOException, InterruptedException {

        for (String command : COMMANDS_WITH_FILE) {
            String[] args = {command, TEST_FILE_NAME};
            Main.main(args);
        }
    }

    @Test
    @Ignore
    public void commandsWithURLTest() throws IOException, InterruptedException {

        for (String command : COMMANDS_WITH_URL) {
            String[] args = {command, TEST_FILE_NAME, TEST_SHORT_URL};
            Main.main(args);
        }
    }

    private String findFile(final String prefix, final String suffix) {

        File dir = new File(".");
        File[] files = dir.listFiles((dir1, name) -> name.startsWith(prefix) && name.endsWith(suffix));

        assertThat(files).hasSize(1);
        return files[0].getName();
    }
}
