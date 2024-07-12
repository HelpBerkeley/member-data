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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class WorkflowExporterTestBase extends TestBase {

    @Before
    public void initialize() throws IOException {
        cleanupGeneratedFiles();
    }

    @AfterClass
    public static void cleanup() throws IOException {
        cleanupGeneratedFiles();
    }

    @Test
    public void workflowExporterValidateUpdatedDataTest() throws IOException {
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

    public String getResourceFile() throws IOException {
        return readResourceFile("update-member-data-multiple-updates.csv");
    }
}