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
package org.helpberkeley.memberdata.route;

import org.helpberkeley.memberdata.Delivery;
import org.helpberkeley.memberdata.Driver;
import org.helpberkeley.memberdata.TestBase;
import org.helpberkeley.memberdata.WorkflowParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RouteTest extends TestBase {

    @Test
    public void routeTest() {

        String csvData = readResourceFile("unrouted-deliveries.csv");
        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, csvData);
        List<Driver> drivers = workflowParser.drivers();
        Route route = new Route();

        for (Driver driver : drivers) {

            // Copy original delivery list
            List<Delivery> deliveries = new ArrayList<>(driver.getDeliveries());

            route.route(driver);

            // Check that there are still all present, and not duplicated.
            assertThat(driver.getDeliveries()).containsExactlyInAnyOrderElementsOf(deliveries);

            // Check that there they have been re-ordered by routing
            // FIX THIS, DS: how to do this with assertj?
        }
    }
}
