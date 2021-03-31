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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OneKitchenDriverPostTest extends TestBase {

    private final Map<String, User> users;

    public OneKitchenDriverPostTest() {
        Loader loader = new Loader(createApiSimulator());
        users = new Tables(loader.load()).mapByUserName();
    }
    @Test
    public void multiDriverMessageTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-turkey.csv");
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_CURRENT_VALIDATED_RESTAURANT_TEMPLATE, "restaurant-template-turkey.json");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(), users, routedDeliveries,
                Constants.QUERY_GET_ONE_KITCHEN_DRIVERS_POST_FORMAT_V1,
                Constants.QUERY_GET_ONE_KITCHEN_GROUP_POST_FORMAT_V1);

        List<String> posts = driverPostFormat.generateDriverPosts();
        assertThat(posts).hasSize(3);

        String post = posts.get(0);
        assertThat(post).contains("@jbDriver");
        assertThat(post).contains("Any issue: call today's on-call ops manager, @JVol, at (123) 456.7890.\n");
        assertThat(post).doesNotContain("You have a condo on your run");
        assertThat(post).doesNotContain("Complete condo instructions");
        assertThat(post).contains("Nourish You!");
        assertThat(post).contains("12:00 PM");
        assertThat(post).contains("Cust Name 1");
        assertThat(post).contains("(555) 555.1112");
        assertThat(post).doesNotContain("Cust Name 4");
        assertThat(post).doesNotContain("Cust Name 5");
        assertThat(post).doesNotContain("Cust Name 6");
        assertThat(post).doesNotContain("Cust Name 7");
        assertThat(post).doesNotContain("Cust Name 8");
        assertThat(post).doesNotContain("take pics");

        post = posts.get(1);
        assertThat(post).contains("@jsDriver");
        assertThat(post).contains("Any issue: call today's on-call ops manager, @JVol, at (123) 456.7890.\n");
        assertThat(post).contains("You have a condo on your run");
        assertThat(post).contains("Complete condo instructions");
        assertThat(post).contains("Nourish You!");
        assertThat(post).contains("12:00 PM");
        assertThat(post).contains("Cust Name 4");
        assertThat(post).contains("Cust Name 5");
        assertThat(post).doesNotContain("Cust Name 1");
        assertThat(post).doesNotContain("Cust Name 2");
        assertThat(post).doesNotContain("Cust Name 6");
        assertThat(post).doesNotContain("Cust Name 7");
        assertThat(post).doesNotContain("Cust Name 8");
        assertThat(post).doesNotContain("take pics");
        System.out.println(post);

        post = posts.get(2);
        assertThat(post).contains("@jcDriver");
        assertThat(post).contains("Any issue: call today's on-call ops manager, @JVol, at (123) 456.7890.\n");
        assertThat(post).doesNotContain("You have a condo on your run");
        assertThat(post).doesNotContain("Complete condo instructions");
        assertThat(post).contains("Nourish You!");
        assertThat(post).contains("12:00 PM");
        assertThat(post).contains("Cust Name 6");
        assertThat(post).contains("Cust Name 7");
        assertThat(post).contains("Cust Name 8");
        assertThat(post).doesNotContain("Cust Name 1");
        assertThat(post).doesNotContain("Cust Name 2");
        assertThat(post).doesNotContain("Cust Name 4");
        assertThat(post).doesNotContain("Cust Name 5");
        assertThat(post).doesNotContain("take pics");
    }

    @Test
    public void multiDriverGroupMessageTest() {
        String routedDeliveries = readResourceFile("routed-deliveries-turkey.csv");
        HttpClientSimulator.setQueryResponseFile(
                Constants.QUERY_GET_CURRENT_VALIDATED_RESTAURANT_TEMPLATE, "restaurant-template-turkey.json");
        DriverPostFormat driverPostFormat = new DriverPostFormat(createApiSimulator(),
                users, routedDeliveries,
                Constants.QUERY_GET_ONE_KITCHEN_DRIVERS_POST_FORMAT_V1,
                Constants.QUERY_GET_ONE_KITCHEN_GROUP_POST_FORMAT_V1);

        String groupPost = driverPostFormat.generateGroupInstructionsPost();
        System.out.println(groupPost);
    }
}
