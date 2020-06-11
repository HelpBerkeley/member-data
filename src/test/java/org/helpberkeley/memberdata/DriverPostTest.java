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

import com.opencsv.exceptions.CsvValidationException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverPostTest extends TestBase {

    @Test
    public void generateDriverPostsTest() throws IOException, CsvValidationException, InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries.csv");
        DriverPostFormat driverPostFormat =
                new DriverPostFormat(createApiSimulator(), routedDeliveries);

        List<String> posts = driverPostFormat.generateDriverPosts();
//for (String post : posts) { System.out.println(post); }
        assertThat(posts).hasSize(2);

        String post = posts.get(0);
        assertThat(post).contains("@jbDriver");
        assertThat(post).doesNotContain("You have a condo on your run!");
        assertThat(post).contains("Talavera");
        assertThat(post).contains("5:00 PM");
        assertThat(post).contains("Sweet Basil");
        assertThat(post).contains("Bopshop");
        assertThat(post).contains("Cust Name 1");
        assertThat(post).contains("Cust Name 2");
        assertThat(post).contains("Cust Name 3");

        post = posts.get(1);
        assertThat(post).contains("@jsDriver");
        assertThat(post).contains("You have a condo on your run!");
        assertThat(post).contains("Cafe Raj");
        assertThat(post).contains("5:10 PM");
        assertThat(post).contains("Cust Name 4");
        assertThat(post).contains("Cust Name 5");
        assertThat(post).contains("Cust Name 6");
        assertThat(post).contains("Cust Name 7");
    }

    @Test
    public void generateGroupInstructionsPostTest() throws IOException, CsvValidationException, InterruptedException {
        String routedDeliveries = readResourceFile("routed-deliveries.csv");
        DriverPostFormat driverPostFormat =
                new DriverPostFormat(createApiSimulator(), routedDeliveries);

        String post = driverPostFormat.generateGroupInstructionsPost();
//        System.out.println(post);

    }

    @Test
    public void splitRestaurantsTest() throws IOException, CsvValidationException, InterruptedException {
        String routedDeliveries = readResourceFile(
                "routed-deliveries-with-split-restaurants.csv");
        DriverPostFormat driverPostFormat =
                new DriverPostFormat(createApiSimulator(), routedDeliveries);

        List<Driver> drivers = driverPostFormat.getDrivers();
        Map<String, Restaurant> restaurants = driverPostFormat.getRestaurants();

        assertThat(drivers).hasSize(3);
        Driver driver = drivers.get(0);
        assertThat(driver.getUserName()).isEqualTo("jbDriver");

        driver = drivers.get(1);
        assertThat(driver.getUserName()).isEqualTo("jsDriver");

        driver = drivers.get(2);
        assertThat(driver.getUserName()).isEqualTo("jcDriver");

        List<String> posts = driverPostFormat.generateDriverPosts();

//        for (String post : posts) {
//            System.out.println(post);
//        }

        String post = posts.get(0);
    }
}
