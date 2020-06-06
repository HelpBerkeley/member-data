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

import com.opencsv.exceptions.CsvValidationException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class DriverPostTest extends TestBase {

    @Test
    public void parseTest() throws IOException, InterruptedException, CsvValidationException {
        String routedDeliveries = readResourceFile("routed-deliveries.csv");
        DriverPostFormat driverPostFormat =
                new DriverPostFormat(createApiSimulator(), routedDeliveries);
    }

    @Test
    public void generateDriverPostsTest() throws IOException, CsvValidationException, InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries.csv");
        DriverPostFormat driverPostFormat =
                new DriverPostFormat(createApiSimulator(), routedDeliveries);

        List<String> posts = driverPostFormat.generateDriverPosts();

        for (String post : posts) {
            System.out.println(post);
            System.out.println("=====================================================================");
        }
    }

    @Test
    public void generateGroupInstructionsPostTest() throws IOException, CsvValidationException, InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries.csv");
        DriverPostFormat driverPostFormat =
                new DriverPostFormat(createApiSimulator(), routedDeliveries);

        String post = driverPostFormat.generateGroupInstructionsPost();
        System.out.println(post);

    }
}
