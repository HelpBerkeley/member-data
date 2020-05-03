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

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Ignore
public class MainTest extends TestBase {

    @BeforeClass
    public static void cleanupGeneratedFiles() throws IOException {
        Files.list(Paths.get("."))
                .filter(Files::isRegularFile)
                .forEach(System.out::println);
    }

    @Test
    public void fetchTest() throws IOException, InterruptedException {
        String[] args = { Options.COMMAND_FETCH };
        Main.main(args);
    }

    @Test
    public void postConsumerRequestsTest() throws IOException, InterruptedException {
        String[] args = { Options.COMMAND_POST_CONSUMER_REQUESTS, TEST_FILE_NAME };
        Main.main(args);
    }
    @Test
    public void commandsWithFileTest() throws IOException, InterruptedException {

        for (String command : COMMANDS_WITH_FILE) {
            String[] args = {command, TEST_FILE_NAME};
            Main.main(args);
        }
    }

    @Test
    public void commandsWithURLTest() throws IOException, InterruptedException {

        for (String command : COMMANDS_WITH_URL) {
            String[] args = {command, TEST_FILE_NAME, TEST_SHORT_URL};
            Main.main(args);
        }
    }
}
