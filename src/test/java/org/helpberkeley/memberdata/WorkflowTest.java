/*
 * Copyright (c) 2020-2024. helpberkeley.org
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
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class WorkflowTest extends TestBase {

    @Test
    public void workflowToFileTest() throws UserException, IOException {

        User u1 = createUserWithGroup(TEST_USER_NAME_1, Constants.GROUP_CONSUMERS);
        User u2 = createUserWithGroup(TEST_USER_NAME_2, Constants.GROUP_DRIVERS);
        User u3 = createUserWithGroup(TEST_USER_NAME_3, Constants.GROUP_DRIVERS);

        UserExporter exporter = new UserExporter(List.of(u1, u2, u3));
        String fileName = exporter.workflowToFile("",
                new HashMap<>(), "workflow.csv");

        String fileData = readFile(fileName);
        assertThat(fileData).contains(TEST_USER_NAME_1);
        assertThat(fileData).contains(TEST_USER_NAME_2);
        assertThat(fileData).contains(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void badRestaurantHeadersTest() {
        UserExporter exporter = new UserExporter(List.of());

        String badRestauarantTemplate = "These,are,not,the,droids,we,are,looking,for";
        Throwable thrown = catchThrowable(() -> exporter.workflow(badRestauarantTemplate, Map.of()));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining("Header mismatch");
        assertThat(thrown).hasMessageContaining(badRestauarantTemplate);
    }

    @Test
    public void badRestaurantRowsTest() throws IOException {
        UserExporter exporter = new UserExporter(List.of());
        String restaurantTemplate;
        try (StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            csvWriter.writeNextToList(exporter.workflowHeaders());
            restaurantTemplate = writer + "these,ducks are, not in,a, row\n";
        }

        Throwable thrown = catchThrowable(() -> exporter.workflow(restaurantTemplate, Map.of()));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining("wrong number of columns in line ");
    }
}
