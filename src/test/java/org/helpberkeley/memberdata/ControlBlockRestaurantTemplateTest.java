package org.helpberkeley.memberdata;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ControlBlockRestaurantTemplateTest extends TestBase {
    @Test
    public void emptyDataTest() {
        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(""));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(RestaurantTemplateParser.TEMPLATE_ERROR
                + RestaurantTemplateParser.ERROR_NO_DATA);
    }

    @Test
    public void csvParseErrorTest() {
        final String badCSV = "This is neither poetry nor CSV data.\n1,2,3,4\n";
        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(badCSV).restaurants());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining("All column names missing. Line 1 does not look like a header row");
    }

    @Test
    public void missingConsumerColumnTest() {
        final String template = String.join(",", List.of(
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN))
                + "\n";

        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(template));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(
                RestaurantTemplateParser.MISSING_COLUMN_ERROR + Constants.WORKFLOW_CONSUMER_COLUMN);
    }

    @Test
    public void missingDriverColumnTest() {
        final String template = String.join(",", List.of(
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN))
                + "\n";

        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(template));
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
                + "\n";

        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(template));
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
                + "\n";

        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(template));
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
                + "\n";

        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(template));
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
                + "\n";

        Throwable thrown = catchThrowable(() -> new RestaurantTemplateParser(template));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(
                RestaurantTemplateParser.MISSING_COLUMN_ERROR + Constants.WORKFLOW_DETAILS_COLUMN);
    }

    @Test
    public void missingRestaurantNameTest() {

        final String badTemplate = readResourceFile("restaurant-template-missing-name.csv");

        Throwable thrown = catchThrowable(()
                -> new RestaurantTemplateParser(badTemplate).restaurants());
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
                -> new RestaurantTemplateParser(badTemplate).restaurants());
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

        Map<String, Restaurant> restaurants = new RestaurantTemplateParser(csvData).restaurants();
        assertThat(restaurants).hasSize(2);
        assertThat(restaurants).containsKeys("Cafe Raj", "Kim's Cafe");
    }

    @Test
    public void missingRouteNameTest() {

        final String csvData = readResourceFile("restaurant-template-missing-route.csv");

        Throwable thrown = catchThrowable(() ->
            new RestaurantTemplateParser(csvData).restaurants());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining("missing route name value from column Consumer");
    }

    @Test
    public void noPicsRestaurantTest() {
        final String csvData = readResourceFile("restaurant-template-v2-0-0.csv");
        RestaurantTemplateParser parser = new RestaurantTemplateParser(csvData);

        for (Restaurant restaurant : parser.restaurants().values()) {

            if (restaurant.getName().equals("Gregoire")) {
                assertThat(restaurant).as(restaurant.getName())
                        .extracting(Restaurant::getNoPics).isEqualTo(true);
            } else if (restaurant.getName().equals("Da Lian")) {
                assertThat(restaurant).as(restaurant.getName()).
                        extracting(Restaurant::getNoPics).isEqualTo(true);
            } else {
                assertThat(restaurant).as(restaurant.getName()).
                        extracting(Restaurant::getNoPics).isEqualTo(false);
            }
        }
    }
}
