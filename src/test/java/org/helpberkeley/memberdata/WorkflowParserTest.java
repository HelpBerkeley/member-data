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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class WorkflowParserTest extends TestBase {
    @Test
    public void splitRestaurantsTest() throws IOException, InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries-with-split-restaurants.csv");
        DriverPostFormat driverPostFormat =
                new DriverPostFormat(createApiSimulator(), routedDeliveries);

        List<Driver> drivers = driverPostFormat.getDrivers();

        // FIX THIS, DS: add restaurants validation
        driverPostFormat.getRestaurants();

        assertThat(drivers).hasSize(4);
        Driver driver = drivers.get(0);
        assertThat(driver.getUserName()).isEqualTo("jbDriver");

        driver = drivers.get(1);
        assertThat(driver.getUserName()).isEqualTo("jsDriver");

        driver = drivers.get(2);
        assertThat(driver.getUserName()).isEqualTo("jcDriver");

        driver = drivers.get(3);
        assertThat(driver.getUserName()).isEqualTo("jdDriver");

        // FIX THIS, DS: how to validate generated posts?
        driverPostFormat.generateDriverPosts();
        driverPostFormat.generateGroupInstructionsPost();
    }

    @Test
    public void deliveryErrorsTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-delivery-errors.csv");
        Throwable thrown = catchThrowable(() ->
                new DriverPostFormat(createApiSimulator(), routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("missing consumer name",
                "missing user name", "missing phone", "missing city", "missing address",
                "missing restaurant name", "no rations detected");
    }

    @Test
    public void restaurantErrorsTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-restaurant-errors.csv");
        Throwable thrown = catchThrowable(() ->
                new DriverPostFormat(createApiSimulator(), routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("missing restaurant name",
                "missing address", "missing orders");
    }

    @Test
    public void driverErrorsTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-driver-errors.csv");
        Throwable thrown = catchThrowable(() ->
                new DriverPostFormat(createApiSimulator(), routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("missing driver user name",
                "missing driver phone number");
    }

    @Test
    public void missingGMapURLTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-missing-gmap-url.csv");
        Throwable thrown = catchThrowable(() ->
                new DriverPostFormat(createApiSimulator(), routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("missing gmap URL");
    }

    @Test
    public void emptyGMapURLTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-empty-gmap-url.csv");
        Throwable thrown = catchThrowable(() ->
                new DriverPostFormat(createApiSimulator(), routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContainingAll("empty gmap URL");
    }

    @Test
    public void bikeGmapURLTest() {

        String shortURL = "https://123+xyz+ccc+ddd+54";
        String fullURL = shortURL + "/@xyzzy..54.zlkasflkj@asj77";
        String expectedURL = "[" + shortURL + "](" + fullURL + ")";

        Driver driver = new Driver("a", "555-555-1212",
                Collections.emptyList(), Collections.emptyList(), fullURL);

        assertThat(driver.getgMapURL()).isEqualTo(expectedURL);
    }

    @Test
    public void carGmapURLTest() {

        String shortURL = "https://123+xyz+ccc+ddd+54";

        Driver driver = new Driver("a", "555-555-1212",
                Collections.emptyList(), Collections.emptyList(), shortURL);

        assertThat(driver.getgMapURL()).isEqualTo(shortURL);
    }

    @Test
    public void missingHeaderRowTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-missing-header.csv");
        Throwable thrown = catchThrowable(() ->
                new DriverPostFormat(createApiSimulator(), routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("All column names missing. Line 1 does not look like a header row");
    }

    @Test
    public void missingHeaderColumnTest() {
        List<String> columnNames = List.of(
                Constants.WORKFLOW_ADDRESS_COLUMN,
                Constants.WORKFLOW_ALT_PHONE_COLUMN,
                Constants.WORKFLOW_CITY_COLUMN,
                Constants.WORKFLOW_CONDO_COLUMN,
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_NAME_COLUMN,
                Constants.WORKFLOW_NORMAL_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_PHONE_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_USER_NAME_COLUMN,
                Constants.WORKFLOW_VEGGIE_COLUMN);

        for (int columnNum = 0; columnNum < columnNames.size(); columnNum++) {
            // Build header with with columnNum column missing

            String header = "";
            for (int index = 0; index < columnNames.size(); index++) {
                if (index == columnNum) {
                    continue;
                }
                header += columnNames.get(index) + ',';
            }
            header += '\n';

            final String expected = header;

            Throwable thrown = catchThrowable(() ->
                    new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, expected));
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(columnNames.get(columnNum));
        }
    }

    @Test
    public void pickupsAndDeliveriesMismatchTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-order-mismatch.csv");
        Throwable thrown = catchThrowable(() ->
                new DriverPostFormat(createApiSimulator(), routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "orders for Talavera but no deliveries");
        assertThat(thrown).hasMessageContaining(
                "1 orders for Sweet Basil but 2 deliveries");
        assertThat(thrown).hasMessageContaining(
                "2 orders for Bopshop but 1 deliveries");
        assertThat(thrown).hasMessageContaining(
                "1 deliveries for Kim's Cafe but no orders");
    }

    @Test
    public void unroutedWorkflowTest() {
        String unroutedDeliveries = readResourceFile("unrouted-deliveries.csv");
        WorkflowParser workflowParser =
                new WorkflowParser(WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, unroutedDeliveries);
        workflowParser.drivers();
    }

    @Test
    public void unroutedWorkflowMissingEmptyRowTest() {
        String unroutedDeliveries = readResourceFile("unrouted-deliveries-missing-empty.csv");
        WorkflowParser workflowParser =
                new WorkflowParser(WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, unroutedDeliveries);
        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("Line 10 is not empty");
    }

    @Test
    public void ignoreControlBlockTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-with-control-block.csv");
        WorkflowParser workflowParser =
                new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, routedDeliveries);
        workflowParser.drivers();
    }
}
