/*
 * Copyright (c) 2021. helpberkeley.org
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

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DriverPostV300Test extends DriverPostTest {

    @Override
    int getRestaurantTemplateQuery() {
        return Constants.QUERY_GET_CURRENT_VALIDATED_ONE_KITCHEN_RESTAURANT_TEMPLATE;
    }

    @Override
    int getDriverPostFormatQuery() {
        return Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23;
    }

    @Override
    int getGroupInstructionsFormatQuery() {
        return Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22;
    }

    @Override
    String getRoutedDeliveriesFileName() {
        return "routed-deliveries-v300.csv";
    }

    @Override
    void checkExpectedDeliveries(List<String> posts) {
        assertThat(posts).hasSize(1);
        assertThat(posts).containsExactly("Cust Name 1|(555) 555.1112|(111) 222.3333|"
                        + "123 456th Ave|NoCondo|:frog:|\n"
                        + "Cust Name 2|(555) 555.2222|NoAltPhone|77 77th St|NoCondo|:frog:|\n"
                        + "Cust Name 3|(555) 555.3333|NoAltPhone|11 11th St|NoCondo|:frog:|\n"
                        + "Cust Name 4|(555) 555.4444|NoAltPhone|44 44th St|NoCondo|:frog:|\n"
                        + "Cust Name 5|(555) 555.5555|NoAltPhone|55 55th St|Condo|:frog:|listed as a condo but\n"
                        + "Cust Name 6|(555) 555.6666|NoAltPhone|66 66th St|NoCondo|:frog:|\n"
                        + "Cust Name 7|(555) 555.7777|NoAltPhone|77 77th St|NoCondo|:frog:|\n"
        );
    }

    @Override
    void checkCondoConsumers(List<String> posts) {
        assertThat(posts).hasSize(1);
        assertThat(posts).containsExactly("Cust Name 5\n");
    }

    @Test
    public void v300DeliveriesTest() {
        String format = "LOOP &{Consumer} { "
                + " &{Consumer.Name}"
                + "\"|\""
                + " &{Consumer.StandardMeal}"
                + "\"|\""
                + " &{Consumer.AlternateMeal}"
                + "\"|\""
                + " &{Consumer.AlternateMealType}"
                + "\"|\""
                + " &{Consumer.StandardGrocery}"
                + "\"|\""
                + " &{Consumer.AlternateGrocery}"
                + "\"|\""
                + " &{Consumer.AlternateGroceryType}"
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly("Cust Name 1|0|1|noRed|0|0|none\n"
                + "Cust Name 2|2|0|none|2|0|none\n"
                + "Cust Name 3|0|0|none|2|0|none\n"
                + "Cust Name 4|0|1|veggie|0|1|veg\n"
                + "Cust Name 5|0|1|noPork|0|0|none\n"
                + "Cust Name 6|0|0|none|0|1|custom pick\n"
                + "Cust Name 7|0|1|veggie|0|1|custom pick\n"
        );
    }

    @Test
    public void v300ImplicitBooleanTest() {
        String format = "LOOP &{Consumer} { "
                + " &{Consumer.Name}"
                + "\"|\""
                + " IF &{Consumer.StandardMeal} THEN { \"std meals\" }"
                + "\"|\""
                + " IF &{Consumer.AlternateMeal} THEN { \"alt meals\" }"
                + "\"|\""
                + " IF &{Consumer.StandardGrocery} THEN { \"std grocery\" }"
                + "\"|\""
                + " IF &{Consumer.AlternateGrocery} THEN { \"alt grocery\" }"
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly("Cust Name 1||alt meals||\n"
                + "Cust Name 2|std meals||std grocery|\n"
                + "Cust Name 3|||std grocery|\n"
                + "Cust Name 4||alt meals||alt grocery\n"
                + "Cust Name 5||alt meals||\n"
                + "Cust Name 6||||alt grocery\n"
                + "Cust Name 7||alt meals||alt grocery\n"
        );
    }

    @Test
    public void thisDriverRestaurantTest() {
        String format = "LOOP &{ThisDriverRestaurant} { "
                + " &{ThisDriverRestaurant.Name}"
                + "\"|\""
                + " &{ThisDriverRestaurant.Emoji}"
                + "\"|\""
                + " &{ThisDriverRestaurant.Address}"
                + "\"|\""
                + " &{ThisDriverRestaurant.Details}"
                + "\"|\""
                + " &{ThisDriverRestaurant.StandardMeals}"
                + "\"|\""
                + " &{ThisDriverRestaurant.StandardGroceries}"
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly(
                "Bauman Meals/Groceries" // Name
                        + "|:frog:" // Emoji
                        + "|1955 Ninth St., Berkeley, CA"  // Address
                        + "|Come from Hearst, park alongside E side of street past loading dock" // Details
                        + "|2" // StandardMeals
                        + "|4\n" // StandardGroceries
        );
    }

    @Test
    public void v300PickupsBooleansTest() {
        String format = "LOOP &{ThisDriverRestaurant} { "
                + " IF &{ThisDriverRestaurant.AnyMealsOrGroceries} THEN { \"HasMealsOrGroceries\" }"
                + "\"|\""
                + " IF &{ThisDriverRestaurant.StandardMeals} THEN { \"std meals\" }"
                + "\"|\""
                + " IF &{ThisDriverRestaurant.AlternateMeals} THEN { \"alt meals\" }"
                + "\"|\""
                + " IF &{ThisDriverRestaurant.StandardGrocery} THEN { \"std grocery\" }"
                + "\"|\""
                + " IF &{ThisDriverRestaurant.AlternateGrocery} THEN { \"alt grocery\" }"
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly("HasMealsOrGroceries|std meals|alt meals|std grocery|alt grocery\n");
    }

    @Test
    public void v300PickupsTest() {
        String format = "LOOP &{ThisDriverRestaurant} {"
                + " &{ThisDriverRestaurant.StandardMeals}"
                + "\"|\""
                + " LOOP &{ThisDriverRestaurant.AlternateMeals} {"
                + " &{AlternateMeals.Type} \":\" &{AlternateMeals.Count} \":\""
                +" } "
                + "\"|\""
                + " &{ThisDriverRestaurant.StandardGroceries}"
                + "\"|\""
                + " LOOP &{ThisDriverRestaurant.AlternateGroceries} {"
                + " &{AlternateGroceries.Type} \":\" &{AlternateGroceries.Count} \":\""
                +" } "
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly("2|veggie:2:noRed:1:noPork:1:|4|veg:1:custom pick:2:\n");
    }
}
