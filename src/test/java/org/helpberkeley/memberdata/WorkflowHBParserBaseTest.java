/*
 * Copyright (c) 2020-2021 helpberkeley.org
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public abstract class WorkflowHBParserBaseTest extends TestBase {

    protected final Map<String, User> users;

    public WorkflowHBParserBaseTest() {
        Loader loader = new Loader(createApiSimulator());
        users = new Tables(loader.load()).mapByUserName();
    }

    public abstract List<String> getColumnNames();
    public abstract String getMinimumControlBlock();

//    @Test
//    public void deliveryErrorsTest() {
//        String routedDeliveries = readResourceFile("routed-deliveries-delivery-errors.csv");
//        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
//                routedDeliveries,
//                Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE,
//                Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23,
//                Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContainingAll("missing consumer name",
//                "missing user name", "missing phone", "missing city", "missing address",
//                "missing restaurant name", "normal and/or veggie rations column is empty");
//    }
//
//    @Test
//    public void restaurantErrorsTest() {
//        String routedDeliveries = readResourceFile("routed-deliveries-restaurant-errors.csv");
//        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
//                routedDeliveries,
//                Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE,
//                Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23,
//                Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContainingAll("missing restaurant name",
//                "missing address", "missing orders");
//    }
//
//    @Test
//    public void driverErrorsTest() {
//        String routedDeliveries = readResourceFile("routed-deliveries-driver-errors.csv");
//        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
//                routedDeliveries,
//                Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE,
//                Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23,
//                Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContainingAll("missing driver user name",
//                "missing driver phone number");
//    }
//
//    @Test
//    public void missingGMapURLTest() {
//        String routedDeliveries = readResourceFile("routed-deliveries-missing-gmap-url.csv");
//        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
//                routedDeliveries,
//                Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE,
//                Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23,
//                Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContainingAll("missing gmap URL");
//    }
//
//    @Test
//    public void emptyGMapURLTest() {
//        String routedDeliveries = readResourceFile("routed-deliveries-empty-gmap-url.csv");
//        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
//                routedDeliveries,
//                Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE,
//                Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23,
//                Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContainingAll("empty gmap URL");
//    }
//
    @Test
    public void missingHeaderRowTest() {
        String controlBlock = getMinimumControlBlock();
        // Remove header line
        String headerless = controlBlock.substring(controlBlock.indexOf('\n') + 1 );
        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users, headerless));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlock.MISSING_OR_INVALID_HEADER_ROW, "duplicate element: FALSE"));
    }

    @Test
    public void missingHeaderColumnTest() {
        List<String> columnNames = getColumnNames();

        for (int columnNum = 0; columnNum < columnNames.size(); columnNum++) {
            // Build header with with columnNum column missing

            StringBuilder header = new StringBuilder();
            for (int index = 0; index < columnNames.size(); index++) {
                if (index == columnNum) {
                    continue;
                }
                header.append(columnNames.get(index)).append(',');
            }
            header.append('\n');

            String minimumControlBlock = getMinimumControlBlock();
            header.append(minimumControlBlock);

            final String expected = header.toString();

            Throwable thrown = catchThrowable(() -> WorkflowParser.create(
                            WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, Collections.emptyMap(), expected));
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(columnNames.get(columnNum));
        }
    }
//
//    @Test
//    public void pickupsAndDeliveriesMismatchTest() {
//        String routedDeliveries = readResourceFile("routed-deliveries-order-mismatch.csv");
//        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
//                routedDeliveries,
//                Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE,
//                Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23,
//                Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContaining(
//                "orders for Talavera but no deliveries");
//        assertThat(thrown).hasMessageContaining(
//                "1 orders for Sweet Basil but 2 deliveries");
//        assertThat(thrown).hasMessageContaining(
//                "2 orders for Bopshop but 1 deliveries");
//        assertThat(thrown).hasMessageContaining(
//                "1 deliveries for Kim's Cafe but no orders");
//    }
//
//    @Test
//    public void pickupsAndDeliveriesEmptyDeliveryMismatchTest() {
//        String routedDeliveries = readResourceFile("routed-deliveries-order-mismatch-empty-delivery.csv");
//        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
//                routedDeliveries,
//                Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE,
//                Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23,
//                Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContaining(
//                "orders for Talavera but no deliveries");
//        assertThat(thrown).hasMessageContaining(
//                "1 orders for Sweet Basil but 2 deliveries");
//        assertThat(thrown).hasMessageContaining(
//                "2 orders for Bopshop but 1 deliveries");
//        assertThat(thrown).hasMessageContaining(
//                "1 deliveries for Kim's Cafe but no orders");
//    }
//
//    @Test
//    public void unroutedWorkflowTest() {
//        String unroutedDeliveries = readResourceFile("unrouted-deliveries.csv");
//        WorkflowParser workflowParser = WorkflowParser.create(
//                WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, Map.of(), unroutedDeliveries);
//        workflowParser.drivers();
//    }
//
//    @Test
//    public void unroutedWorkflowMissingEmptyRowTest() {
//        String unroutedDeliveries = readResourceFile("unrouted-deliveries-missing-empty.csv");
//        WorkflowParser workflowParser = WorkflowParser.create(
//                WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, Collections.emptyMap(), unroutedDeliveries);
//        Throwable thrown = catchThrowable(workflowParser::drivers);
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContaining("Line 13 is not empty");
//    }
//
//    @Test
//    public void unroutedWorkflowUnterminatedDriverTest() {
//        String unroutedDeliveries = readResourceFile("unrouted-deliveries-unterminated-driver.csv");
//        WorkflowParser workflowParser = WorkflowParser.create(
//                WorkflowParser.Mode.DRIVER_ROUTE_REQUEST, Collections.emptyMap(), unroutedDeliveries);
//        Throwable thrown = catchThrowable(workflowParser::drivers);
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContaining("Line 19 driver block for jsDriver missing closing driver row");
//    }
//
//    @Test
//    public void duplicateDriverDuplicateRestaurantsTest() {
//        String routedDeliveries = readResourceFile("routed-deliveries-duplicate-driver.csv");
//        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
//                routedDeliveries,
//                Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE,
//                Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23,
//                Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22));
//        assertThat(thrown).isInstanceOf(MemberDataException.class);
//        assertThat(thrown).hasMessageContaining("Duplicate driver \"jbDriver\" at line 34");
//    }
}
