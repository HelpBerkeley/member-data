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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DriverPostTest extends TestBase {

    private final Map<String, User> users;

    public DriverPostTest() throws InterruptedException {
        Loader loader = new Loader(createApiSimulator());
        users = new Tables(loader.load()).mapByUserName();
    }

    @Test
    public void singleDriverMessageTest() throws InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries-single.csv");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries);
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
    public void multiDriverMessageTest() throws InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries.csv");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries);

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

    @Test public void multiDriverWithSplitMessageTest() throws InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries-with-split-restaurant.csv");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries);

        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(4);
    }

    @Test public void multiDriverWithMultiSplitsMessageTest() throws InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries-with-split-restaurants.csv");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries,
                Constants.QUERY_GET_DRIVERS_POST_FORMAT,
                Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT);

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

    @Test public void multiDriverWithMultiSplitsMessageDisableRestaurantAuditTest() throws InterruptedException {
        String routedDeliveries =
                readResourceFile("routed-deliveries-with-split-restaurants-audit-disabled.csv");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries);

        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(2);
        String post = driverPostFormat.generateSummary();
        assertThat(post).doesNotContain("No drivers going to ");
    }

    @Test
    public void generateGroupInstructionsNoSplitsPostTest() throws InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries.csv");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries);

        String post = driverPostFormat.generateGroupInstructionsPost();
//        System.out.println(post);
        assertThat(post).doesNotContain("**Split Restaurants**");
        assertThat(post).doesNotContain("closes at");
    }

    @Test
    public void generateGroupInstructionsWithSplitsPostTest() throws InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries-with-split-restaurant.csv");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries);

        String post = driverPostFormat.generateGroupInstructionsPost();
//        System.out.println(post);
        assertThat(post).contains("**Split restaurant drivers:**");
        assertThat(post).contains("(888) 888.8888");
        assertThat(post).contains("(999) 999.9999");
        assertThat(post).contains("closes at 5:00 PM.");
    }

    @Test
    public void generateBackupDriverPostTest() throws InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries.csv");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries);

        String post = driverPostFormat.generateBackupDriverPost();
//        System.out.println(post);
    }

    @Test
    public void noDeliveriesTest() throws InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries-pickup-only.csv");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries);

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
        Throwable thrown = catchThrowable(() -> new DriverPostFormat(createApiSimulator(),
                users, Constants.CONTROL_BLOCK_CURRENT_VERSION, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("normal and/or veggie rations column is empty");
    }
}