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
package org.helpberkeley.memberdata.v300;

import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ThrowableAssert;
import org.helpberkeley.memberdata.*;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DriverPostTest extends org.helpberkeley.memberdata.DriverPostTest {

    @Override
    public int getRestaurantTemplateQuery() {
        return Constants.QUERY_GET_CURRENT_VALIDATED_ONE_KITCHEN_RESTAURANT_TEMPLATE;
    }

    @Override
    public int getDriverPostFormatQuery() {
        return Constants.QUERY_GET_ONE_KITCHEN_DRIVERS_POST_FORMAT_V300;
    }

    @Override
    public int getGroupInstructionsFormatQuery() {
        return Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22;
    }

    @Override
    public String getRoutedDeliveriesFileName() {
        return "routed-deliveries-v300.csv";
    }

    @Override
    public void checkExpectedDeliveries(List<String> posts) {
        assertThat(posts).hasSize(1);
        assertThat(posts).containsExactly("Cust Name 1|(555) 555.1112|(111) 222.3333|"
                        + "123 456th Ave|NoCondo|\n"
                        + "Cust Name 2|(555) 555.2222|NoAltPhone|77 77th St|NoCondo|\n"
                        + "Cust Name 3|(555) 555.3333|NoAltPhone|11 11th St|NoCondo|\n"
                        + "Cust Name 4|(555) 555.4444|NoAltPhone|44 44th St|NoCondo|\n"
                        + "Cust Name 5|(555) 555.5555|NoAltPhone|55 55th St|Condo|listed as a condo but\n"
                        + "Cust Name 6|(555) 555.6666|NoAltPhone|66 66th St|NoCondo|\n"
                        + "Cust Name 7|(555) 555.7777|NoAltPhone|77 77th St|NoCondo|\n"
        );
    }

    @Override
    public void checkCondoConsumers(List<String> posts) {
        assertThat(posts).hasSize(1);
        assertThat(posts).containsExactly("Cust Name 5\n");
    }

    @Test
    public void v300SingleDriverDeliveriesTest() {
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
        assertThat(posts).containsExactly("Cust Name 1|0|1|noRed|0|0|\n"
                + "Cust Name 2|2|0||2|0|\n"
                + "Cust Name 3|0|0||2|0|\n"
                + "Cust Name 4|0|1|veggie|0|1|veg\n"
                + "Cust Name 5|0|1|noPork|0|0|\n"
                + "Cust Name 6|0|0||0|1|custom pick\n"
                + "Cust Name 7|0|1|veggie|0|1|custom pick\n"
        );
    }

    @Test
    public void v300MultiDriverDeliveriesTest() {
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
        String routedDeliveries = readResourceFile("routed-deliveries-multi-driver-v300.csv");
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat (posts).hasSize(2);
        assertThat(posts).containsExactly("Cust Name 1|0|1|noRed|0|0|\n"
                + "Cust Name 2|2|0||2|0|\n"
                + "Cust Name 3|0|0||2|0|\n"
                + "Cust Name 4|0|1|veggie|0|1|veg\n",
                "Cust Name 5|0|1|noPork|0|0|\n"
                + "Cust Name 6|0|0||0|1|custom pick\n"
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
                + "\"|\""
                + " IF &{Consumer.Details} THEN { \"details\" }"
                + "\"|\""
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly("Cust Name 1||alt meals||||\n"
                + "Cust Name 2|std meals||std grocery|||\n"
                + "Cust Name 3|||std grocery|||\n"
                + "Cust Name 4||alt meals||alt grocery||\n"
                + "Cust Name 5||alt meals|||details|\n"
                + "Cust Name 6||||alt grocery||\n"
                + "Cust Name 7||alt meals||alt grocery||\n"
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
    public void v300SingleLocationPickupsTest() {
        String format = "LOOP &{ThisDriverRestaurant} {"
                + " &{ThisDriverRestaurant.Name} \"\\n\""
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
        assertThat(posts).containsExactly("Bauman Meals/Groceries\n"
                + "2|veggie:2:noRed:1:noPork:1:|4|veg:1:custom pick:2:\n");
    }

    @Test
    public void v300MultiLocationPickupsTest() {
        String format = "LOOP &{ThisDriverRestaurant} {"
                + " &{ThisDriverRestaurant.Name} "
                + "\"|\""
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
                + "\"|\""
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile("routed-deliveries-multi-pickup-v300.csv");
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly(
                "RevFoodTruck|2|veggie:2:noRed:1:noPork:1:|0|veg:0:custom pick:0:|\n"
                + "BFN|0|veggie:0:noRed:0:noPork:0:|4|veg:1:custom pick:2:|\n");
    }

    @Test
    public void alternateMealsTest() {
        String format = "LOOP &{AlternateMeals} {"
                + "&{AlternateMeals.Type} "
                + "\":\""
                + "}"
                + "\"\\n\"";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly("veggie:noRed:noPork:\n");
    }

    @Test
    public void alternateGroceriesTest() {
        String format = "LOOP &{AlternateGroceries} {"
                + "&{AlternateGroceries.Type} "
                + "\":\""
                + "}"
                + "\"\\n\"";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly("veg:custom pick:\n");
    }

    @Test
    public void startTimeTest() {
        String format = "\"Start time: \" ${ThisDriverFirstRestaurantStartTime} \"\n\"";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly("Start time: 3:00");
    }

    @Test
    public void v300FirstPickupLocationTest() {
        String format = "LOOP &{Driver} {"
                + " ${FirstPickupLocation} \"\\n\""
                + "}";
        HttpClientSimulator.setQueryResponseData(getGroupInstructionsFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        String post = driverPostFormat.generateGroupInstructionsPost();
        assertThat(post).isEqualTo("Bauman Meals/Groceries\n");
    }

    @Test
    public void v300BackupDriversTest() {
        String format = "LOOP &{BackupDriver} {"
                + " ${BackupDriver.UserName} \"\\n\""
                + "}";
        HttpClientSimulator.setQueryResponseData(getGroupInstructionsFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        String post = driverPostFormat.generateGroupInstructionsPost();
        assertThat(post).isEqualTo("jsDriver\n");
    }

    @Test
    public void v300MissingAltMealTypesTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-missing-alts-v300.csv");
        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(),
                users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(
                "Required AltMealOptions() control block variable is missing.\n"
                + "Required AltGroceryOptions() control block variable is missing.\n");
    }

    @Test
    public void notEnoughStartTimesTest() {
        DeliveryBuilder delivery = new DeliveryBuilder();
        delivery.withStdMeals("1");
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(delivery);

        DriverBlockBuilder driverBlock2 = new DriverBlockBuilder();
        driverBlock2.withDriver(new DriverBuilder().withUserName("jsDriver"));
        driverBlock2.withRestaurant(new RestaurantBuilder());
        driverBlock2.withDelivery(delivery);

        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);
        workflowBuilder.withDriverBlock(driverBlock2);
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withStartTimes("2:00"));

        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(),
                users, workflowBuilder.build(),
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery()));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlockV300.MORE_DRIVERS_THAN_START_TIMES, "2", "1"));
    }

    @Test
    public void generateNoWarningsSummaryTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withStdMeals("1"));

        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withControlBlock(new ControlBlockBuilder().withFoodSources("RevFoodTruck|"));
        workflowBuilder.withDriverBlock(driverBlock);

        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(),
                users, workflowBuilder.build(),
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        String post = driverPostFormat.generateSummary();
        assertThat(post).isEmpty();
    }

    @Test
    public void generateSummaryWithUnvisitedRestaurantTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withDelivery(new DeliveryBuilder().withStdMeals("1"));

        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(),
                users, workflowBuilder.build(),
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        String post = driverPostFormat.generateSummary();
        assertThat(post).isEqualTo("No drivers going to BFN\n\n");
    }

    @Test
    public void generateEmptyDeliveryWarningSummaryTest() {
        DriverBlockBuilder driverBlock = new DriverBlockBuilder();
        driverBlock.withRestaurant(new RestaurantBuilder());
        driverBlock.withRestaurant(new RestaurantBuilder().withName("BFN"));
        driverBlock.withDelivery(new DeliveryBuilder());

        WorkflowBuilder workflowBuilder = new WorkflowBuilder();
        workflowBuilder.withDriverBlock(driverBlock);

        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(),
                users, workflowBuilder.build(),
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        String post = driverPostFormat.generateSummary();
        assertThat(post).isEqualTo(MessageFormat.format(WorkflowParserV300.EMPTY_DELIVERY, 15,
                WorkflowBuilder.DEFAULT_DRIVER_NAME, DeliveryBuilder.DEFAULT_CONSUMER_NAME));
    }
}