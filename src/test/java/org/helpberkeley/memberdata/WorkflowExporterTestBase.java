/*
 * Copyright (c) 2024. helpberkeley.org
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

import com.opencsv.exceptions.CsvException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class WorkflowExporterTestBase extends TestBase {

    @Before
    public void initialize() throws IOException {
        cleanupGeneratedFiles();
    }

    @AfterClass
    public static void cleanup() throws IOException {
        cleanupGeneratedFiles();
    }

    public abstract String getResourceFile() throws IOException;
    public abstract String getRestaurantTemplate();
    public abstract String generateWorkflow(
            UserExporter exporter, String restaurantTemplate, Map<String, DetailsPost> details)
            ;

    @Test
    public void workflowExporterValidateUpdatedDataTest() throws IOException, CsvException {
        String deliveries = getResourceFile();
        WorkflowParser parser = WorkflowParser.create(Collections.emptyMap(), deliveries);
        ApiClient apiSim = createApiSimulator();
        List<User> userList = new Loader(apiSim).load();
        Map<String, User> users = new Tables(userList).mapByUserName();
        String json = apiSim.runQuery(Constants.QUERY_GET_DELIVERY_DETAILS);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        Map<String, DetailsPost> deliveryDetails = HBParser.deliveryDetails(apiQueryResult);
        DriverPostFormat driverPostFormatPreUpdate = DriverPostFormat.create(apiSim, users, deliveries);
        WorkflowExporter exporter = new WorkflowExporter(parser);
        String updatedCSVData = exporter.updateMemberData(users, deliveryDetails);
        DriverPostFormat driverPostFormatPostUpdate = DriverPostFormat.create(apiSim, users, updatedCSVData);
    }

    @Test
    public void deliveryDetailsWithPunctuationTest() throws IOException {
        String deliveries = getResourceFile();
        WorkflowParser parser = WorkflowParser.create(Collections.emptyMap(), deliveries);

        // Read/parse the members data
        ApiClient apiSim = createApiSimulator();
        List<User> users = new Loader(apiSim).load();

        String restaurantTemplate = getRestaurantTemplate();

        // Gin up some punctuated delivery details for the delivery recipients
        String somebodyDetails = "put the \"bags\", all of them, on the \"bird feeder\"";
        DetailsPost somebodyDetailsPost = new DetailsPost();
        somebodyDetailsPost.setDetails(0, somebodyDetails);

        String somebodyElseDetails = "knock not \"once\", not \"twice\", but \"thrice!\"";
        DetailsPost somebodyElseDetailsPost = new DetailsPost();
        somebodyElseDetailsPost.setDetails(0, somebodyElseDetails);

        Map<String, DetailsPost> deliveryDetails = Map.of(
                "Somebody", somebodyDetailsPost,
                "SomebodyElse", somebodyElseDetailsPost);

        String workflowCSV = generateWorkflow(new UserExporter(users), restaurantTemplate, deliveryDetails);

        for (String line : workflowCSV.split("\n")) {
            if (line.contains("\"Somebody\"")) {
                // CSVWriter will have changed "something" to ""something""
                assertThat(line).contains(somebodyDetails.replaceAll("\"", "\"\""));
            } else if (line.contains("\"SomebodyElse\"")) {
                // CSVWriter will have changed "something" to ""something""
                assertThat(line).contains(somebodyElseDetails.replaceAll("\"", "\"\""));
            }
        }
    }
}
