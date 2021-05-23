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
package org.helpberkeley.memberdata.v200;

import org.helpberkeley.memberdata.*;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DriverPostTest extends org.helpberkeley.memberdata.DriverPostTest {

    @Override
    public int getRestaurantTemplateQuery() {
        return Constants.QUERY_GET_CURRENT_VALIDATED_DRIVER_MESSAGE_RESTAURANT_TEMPLATE;
    }

    @Override
    public int getDriverPostFormatQuery() {
        return Constants.QUERY_GET_DRIVERS_POST_FORMAT_V23;
    }

    @Override
    public int getGroupInstructionsFormatQuery() {
        return Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22;
    }

    @Override
    public String getRoutedDeliveriesFileName() {
        return "routed-deliveries-v200.csv";
    }

    @Override
    public void checkExpectedDeliveries(List<String> posts) {
        assertThat(posts).hasSize(2);
        assertThat(posts).containsExactly("Cust Name 1|(555) 555.1112|(111) 222.3333|"
                + "123 456th Ave|NoCondo|\n"
                + "Cust Name 2|(555) 555.2222|NoAltPhone|77 77th St|NoCondo|\n"
                + "Cust Name 3|(555) 555.3333|NoAltPhone|11 11th St|NoCondo|\n",
                "Cust Name 4|(555) 555.4444|NoAltPhone|44 44th St|NoCondo|\n"
                + "Cust Name 5|(555) 555.5555|NoAltPhone|55 55th St|Condo|listed as a condo but\n"
                + "Cust Name 6|(555) 555.6666|NoAltPhone|66 66th St|NoCondo|\n"
                + "Cust Name 7|(555) 555.7777|NoAltPhone|77 77th St|NoCondo|\n"
        );
    }

    @Override
    public void checkCondoConsumers(List<String> posts) {
        assertThat(posts).hasSize(2);
        assertThat(posts).containsExactly("", "Cust Name 5\n");
    }

    @Test
    public void singleDriverMessageTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-single.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(1);
        String post = posts.get(0);
        assertThat(post).contains("@jsDriver");
        assertThat(post).contains("Any issue: call today's on-call ops manager, @JVol, at (123) 456.7890.\n");
        assertThat(post).contains("You have a condo on your run");
        assertThat(post).contains("**Complete condo instructions**");
        assertThat(post).contains("Cafe Raj :open_umbrella:");
        assertThat(post).contains("5:10 PM");
        assertThat(post).contains("Cust Name 5");
        assertThat(post).contains("**GMap static URL:** [jsDriver's itinerary](https://www.google.com/maps/dir/x+y+z)");
    }

    @Test
    public void multiDriverMessageTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-v200.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);

        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(2);

        String post = posts.get(0);
//        System.out.println(post);
        assertThat(post).contains("@jbDriver");
        assertThat(post).contains("Any issue: call today's on-call ops manager, @JVol, at (123) 456.7890.\n");
        assertThat(post).doesNotContain("You have a condo on your run");
        assertThat(post).doesNotContain("Complete condo instructions");
        assertThat(post).contains("Talavera");
        assertThat(post).contains("5:05 PM");   // Adjusted start time
        assertThat(post).contains("Sweet Basil");
        assertThat(post).contains("Bopshop");
        assertThat(post).contains("Cust Name 1");
        assertThat(post).contains("(555) 555.1112, (111) 222.3333");
        assertThat(post).contains("Cust Name 2");
        assertThat(post).contains("Cust Name 3");

        post = posts.get(1);
//        System.out.println(post);
        assertThat(post).contains("@jsDriver");
        assertThat(post).contains("Any issue: call today's on-call ops manager, @JVol, at (123) 456.7890.\n");
        assertThat(post).contains("You have a condo on your run");
        assertThat(post).contains("Complete condo instructions");
        assertThat(post).contains("Cafe Raj");
        assertThat(post).contains("5:10 PM");
        assertThat(post).contains("Cust Name 4");
        assertThat(post).contains("Cust Name 5");
        assertThat(post).contains("Cust Name 6");
        assertThat(post).contains("Cust Name 7");
    }

    @Test public void multiDriverWithSplitMessageTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-with-split-restaurant.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);

        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(4);
    }

    @Test public void multiDriverWithMultiSplitsMessageTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-with-split-restaurants.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);

        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(2);
        String post = posts.get(0);
        System.out.println(post);
        assertThat(post).contains("take pics");
        post = posts.get(1);
        post = driverPostFormat.generateSummary();
        assertThat(post).contains("No drivers going to Thai Delight");
        assertThat(post).contains("No drivers going to V&A Cafe");
        assertThat(post).contains("No drivers going to Tacos Sinaloa");
        assertThat(post).contains("No drivers going to Talavera");
        assertThat(post).contains("No drivers going to Gregoire");
        assertThat(post).contains("No drivers going to Da Lian");
        assertThat(post).contains("No drivers going to Sweet Basil");
        assertThat(post).contains("No drivers going to Bopshop");
        assertThat(post).contains("No drivers going to Crepevine");
    }

    @Test public void multiDriverWithMultiSplitsMessageDisableRestaurantAuditTest() {
        String routedDeliveries =
                readResourceFile("routed-deliveries-with-split-restaurants-audit-disabled.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);

        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(2);
        String post = driverPostFormat.generateSummary();
        assertThat(post).doesNotContain("No drivers going to ");
    }

    @Test
    public void generateGroupInstructionsNoSplitsPostTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-v200.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);

        String post = driverPostFormat.generateGroupInstructionsPost();
//        System.out.println(post);
        assertThat(post).doesNotContain("**Split Restaurants**");
        assertThat(post).doesNotContain("closes at");
    }

    @Test
    public void generateGroupInstructionsWithSplitsPostTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-with-split-restaurant.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);

        String post = driverPostFormat.generateGroupInstructionsPost();
//        System.out.println(post);
        assertThat(post).contains("**Split restaurant drivers:**");
        assertThat(post).contains("(888) 888.8888");
        assertThat(post).contains("(999) 999.9999");
        assertThat(post).contains("closes at 5:00 PM.");
    }

    @Test
    public void generateBackupDriverPostTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-v200.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);

        String post = driverPostFormat.generateBackupDriverPost();
//        System.out.println(post);
    }

    @Test
    public void noDeliveriesTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-pickup-only.csv");
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);

        assertThat(driverPostFormat.getDrivers()).hasSize(1);
        Driver driver = driverPostFormat.getDrivers().get(0);
        assertThat(driver.getUserName()).isEqualTo("jsDriver");
        assertThat(driver.getDeliveries()).isEmpty();
        List<String> posts = driverPostFormat.generateDriverPosts();
        String post = posts.get(0);
//        System.out.println(post);
    }

    @Test
    public void missingRationTypeCountTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-missing-ration-type-count.csv");
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("normal and/or veggie rations column is empty");
    }

    // FIX THIS, DS: add V200 specific variables
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
                + " &{ThisDriverRestaurant.TotalDrivers}"
                + "\"|\""
                + " &{ThisDriverRestaurant.TotalOrders}"
                + "\"|\""
                + " IF &{ThisDriverRestaurant.IsSplit} THEN { \"Split\" }"
                + " IF NOT &{ThisDriverRestaurant.IsSplit} THEN { \"NotSplit\" }"
                + "\"|\""
                + " IF &{ThisDriverRestaurant.NoPics} THEN { \"NoPics\" }"
                + " IF NOT &{ThisDriverRestaurant.NoPics} THEN { \"Pics\" }"
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0)).isEqualTo("Talavera|:gloves:|1561 Solano Ave, Berkeley||1|0|NotSplit|Pics\n"
                + "Sweet Basil|:lemon:|1736 Solano Ave, Berkeley||1|1|NotSplit|Pics\n"
                + "Bopshop|:motor_scooter:|1823 Solano Ave, Berkeley||1|2|NotSplit|Pics\n");
        assertThat(posts.get(1)).isEqualTo("Cafe Raj|:open_umbrella:|1158 Solano Ave, Albany"
                + "|One,Two,Three details|1|4|NotSplit|Pics\n");
    }

    @Test
    public void thisDriverRestaurantPickupTest() {
        String format = "LOOP &{ThisDriverRestaurant} { "
                + "LOOP &{ThisDriverRestaurant.Pickup} { "
                + " &{ThisDriverRestaurant.Name}"
                + "\"|\""
                + " &{Pickup.MemberName}"
                + "\"|\""
                + " &{Pickup.UserName}"
                + "\"\\n\""
                + " }"
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(2);
        assertThat(posts).containsExactly("Sweet Basil|Cust Name 2|Cust2\n"
                    + "Bopshop|Cust Name 1|Cust1\n"
                    + "Bopshop|Cust Name 3|Cust3\n",
                    "Cafe Raj|Cust Name 4|Cust4\n"
                    + "Cafe Raj|Cust Name 5|Cust5\n"
                    + "Cafe Raj|Cust Name 6|Cust6\n"
                    + "Cafe Raj|Cust Name 7|Cust7\n");
    }

    @Test
    public void deliveriesV200Test() {
        String format = "LOOP &{Consumer} { "
                + " &{Consumer.Name}"
                + "\"|\""
                + " &{Consumer.CompactPhone}"
                + "\"|\""
                + " IF &{Consumer.IsAltPhone} THEN { &{Consumer.CompactAltPhone} }"
                + " IF NOT &{Consumer.IsAltPhone} THEN { \"NoAltPhone\" } "
                + "\"|\""
                + " &{Consumer.Address}"
                + "\"|\""
                + " IF &{Consumer.IsCondo} THEN { \"Condo\" } "
                + " IF NOT &{Consumer.IsCondo} THEN { \"NoCondo\" } "
                + "\"|\""
                + " &{Consumer.RestaurantEmoji}"
                + "\"|\""
                + " &{Consumer.Details}"
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).containsExactly("Cust Name 1|(555) 555.1112|(111) 222.3333|"
                + "123 456th Ave|NoCondo|:motor_scooter:|\n"
                + "Cust Name 2|(555) 555.2222|NoAltPhone|77 77th St|NoCondo|:lemon:|\n"
                + "Cust Name 3|(555) 555.3333|NoAltPhone|11 11th St|NoCondo|:motor_scooter:|\n",
                "Cust Name 4|(555) 555.4444|NoAltPhone|44 44th St|NoCondo|:open_umbrella:|\n"
                + "Cust Name 5|(555) 555.5555|NoAltPhone|55 55th St|Condo|:open_umbrella:|listed as a condo but\n"
                + "Cust Name 6|(555) 555.6666|NoAltPhone|66 66th St|NoCondo|:open_umbrella:|\n"
                + "Cust Name 7|(555) 555.7777|NoAltPhone|77 77th St|NoCondo|:open_umbrella:|\n"
        );
    }
}
