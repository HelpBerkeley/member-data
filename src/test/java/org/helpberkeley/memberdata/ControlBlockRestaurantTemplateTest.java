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

import org.helpberkeley.memberdata.v200.RestaurantV200;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ControlBlockRestaurantTemplateTest extends TestBase {

    // FIX THIS, DS; abstract and make multi-version
    private final static String MINIMUM_CONTROL_BLOCK =
            "FALSE,FALSE," + Constants.CONTROL_BLOCK_BEGIN + ",,,,,,,,,,,,\n" +
            "FALSE,FALSE,,Version,,,,2-0-0,,,,,,,\n" +
            "FALSE,FALSE," + Constants.CONTROL_BLOCK_END + ",,,,,,,,,,,,\n";

    @Test
    public void emptyDataTest() {
        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(""));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(RestaurantTemplateParser.TEMPLATE_ERROR
                + RestaurantTemplateParser.ERROR_NO_DATA);
    }

    @Test
    public void csvParseErrorTest() {
        final String badCSV = "This is neither poetry nor CSV data.\n1,2,3,4\n";
        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(badCSV).restaurants());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(ControlBlock.BAD_HEADER_ROW);
    }

    @Test
    public void missingConsumerColumnTest() {
        final String template = String.join(",", List.of(
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN))
                + "\n"
                + MINIMUM_CONTROL_BLOCK;

        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(template));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(
                RestaurantTemplateParser.MISSING_COLUMN_ERROR + Constants.WORKFLOW_CONSUMER_COLUMN);
    }

    @Test
    public void missingDriverColumnTest() {
        String template = String.join(",", List.of(
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN))
                + "\n"
                + MINIMUM_CONTROL_BLOCK;

        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(template));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(
                RestaurantTemplateParser.MISSING_COLUMN_ERROR + Constants.WORKFLOW_DRIVER_COLUMN);
    }

    @Test
    public void missingRestaurantsColumnTest() {
        final String template = String.join(",", List.of(
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN))
                + "\n"
                + MINIMUM_CONTROL_BLOCK;

        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(template));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(
                RestaurantTemplateParser.MISSING_COLUMN_ERROR + Constants.WORKFLOW_RESTAURANTS_COLUMN);
    }

    @Test
    public void missingCondoColumnTest() {
        final String template = String.join(",", List.of(
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN))
                + "\n"
                + MINIMUM_CONTROL_BLOCK;

        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(template));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(
                RestaurantTemplateParser.MISSING_COLUMN_ERROR + Constants.WORKFLOW_CONDO_COLUMN);
    }

    @Test
    public void missingOrdersColumnTest() {
        final String template = String.join(",", List.of(
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN))
                + "\n"
                + MINIMUM_CONTROL_BLOCK;

        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(template));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(
                RestaurantTemplateParser.MISSING_COLUMN_ERROR + Constants.WORKFLOW_ORDERS_COLUMN);
    }

    @Test
    public void missingDetailsColumnTest() {
        final String template = String.join(",", List.of(
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN))
                + "\n"
                + MINIMUM_CONTROL_BLOCK;

        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(template));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(
                RestaurantTemplateParser.MISSING_COLUMN_ERROR + Constants.WORKFLOW_DETAILS_COLUMN);
    }

    @Test
    public void missingRestaurantNameTest() {

        final String badTemplate = readResourceFile("restaurant-template-missing-name.csv");

        Throwable thrown = catchThrowable(()
                -> RestaurantTemplateParser.create(badTemplate).restaurants());
        assertThat(thrown).isInstanceOf(MemberDataException.class);

        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.MISSING_VALUE_ERROR
            + Constants.WORKFLOW_RESTAURANTS_COLUMN);
    }

    @Test
    public void restaurantRepeatedTest() {

        final String badTemplate = readResourceFile("restaurant-template-name-repeated.csv");

        Throwable thrown = catchThrowable(()
                -> RestaurantTemplateParser.create(badTemplate).restaurants());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);

        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(
                "Talavera" + RestaurantTemplateParser.DUPLICATE_ROUTE_ERROR);
    }

    @Test
    public void emptyRowInRouteBlockTest() {

        final String csvData = readResourceFile("restaurant-template-empty-route-row.csv");

        Map<String, Restaurant> restaurants = RestaurantTemplateParser.create(csvData).restaurants();
        assertThat(restaurants).hasSize(2);
        assertThat(restaurants).containsKeys("Cafe Raj", "Kim's Cafe");
    }

    @Test
    public void missingRouteNameTest() {

        final String csvData = readResourceFile("restaurant-template-missing-route.csv");

        Throwable thrown = catchThrowable(() ->
            RestaurantTemplateParser.create(csvData).restaurants());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining("missing route name value from column Consumer");
    }

    @Test
    public void noPicsRestaurantTest() {
        final String csvData = readResourceFile("restaurant-template-v200.csv");
        RestaurantTemplateParser parser = RestaurantTemplateParser.create(csvData);

        for (Restaurant restaurant : parser.restaurants().values()) {

            RestaurantV200 restaurantV200 = (RestaurantV200) restaurant;

            if (restaurant.getName().equals("Gregoire")) {
                assertThat(restaurantV200).as(restaurantV200.getName())
                        .extracting(RestaurantV200::getNoPics).isEqualTo(true);
            } else if (restaurant.getName().equals("Da Lian")) {
                assertThat(restaurantV200).as(restaurantV200.getName()).
                        extracting(RestaurantV200::getNoPics).isEqualTo(true);
            } else {
                assertThat(restaurantV200).as(restaurantV200.getName()).
                        extracting(RestaurantV200::getNoPics).isEqualTo(false);
            }
        }
    }
}
