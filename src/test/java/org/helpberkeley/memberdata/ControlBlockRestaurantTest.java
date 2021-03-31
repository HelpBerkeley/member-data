/*
 * Copyright (c) 2020-2021. helpberkeley.org
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

public class ControlBlockRestaurantTest extends TestBase {

    @Test
    public void downRevVersionTest() {
        String csvData = readResourceFile("restaurant-template.csv");
        RestaurantTemplateParser parser = new RestaurantTemplateParser(csvData);

        Throwable thrown = catchThrowable(parser::restaurants);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.TEMPLATE_ERROR);
        assertThat(thrown).hasMessageContaining(RestaurantTemplateParser.ERROR_MISSING_OR_UNSUPPORTED_VERSION);
    }

    @Test
    public void parseTest() {

        String csvData = readResourceFile("restaurant-template-v1.csv");
        RestaurantTemplateParser parser = new RestaurantTemplateParser(csvData);
        Map<String, Restaurant> restaurants = parser.restaurants();


        List<String> expectedRestaurants = List.of(
                "Cafe Raj",
                "Kim's Cafe",
                "Talavera",
                "Sweet Basil",
                "Bopshop",
                "Jot Mahal",
                "Da Lian",
                "Thai Delight",
                "V&A Cafe",
                "Gregoire",
                "Crepevine",
                "Kaze Ramen",
                "Tacos Sinaloa"
        );

        assertThat(restaurants).containsOnlyKeys(expectedRestaurants);

        Restaurant restaurant = restaurants.get("Cafe Raj");
        assertThat(restaurant.getRoute()).isEqualTo("Solano Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:10 PM");

        restaurant = restaurants.get("Kim's Cafe");
        assertThat(restaurant.getRoute()).isEqualTo("Solano Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:10 PM");

        restaurant = restaurants.get("Talavera");
        assertThat(restaurant.getRoute()).isEqualTo("Solano Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:00 PM");

        restaurant = restaurants.get("Sweet Basil");
        assertThat(restaurant.getRoute()).isEqualTo("Solano Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:10 PM");

        restaurant = restaurants.get("Bopshop");
        assertThat(restaurant.getRoute()).isEqualTo("Solano Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:10 PM");

        restaurant = restaurants.get("Jot Mahal");
        assertThat(restaurant.getRoute()).isEqualTo("Shattuck Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:10 PM");

        restaurant = restaurants.get("Gregoire");
        assertThat(restaurant.getRoute()).isEqualTo("Shattuck Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:10 PM");

        restaurant = restaurants.get("Crepevine");
        assertThat(restaurant.getRoute()).isEqualTo("Shattuck Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:10 PM");

        restaurant = restaurants.get("Da Lian");
        assertThat(restaurant.getRoute()).isEqualTo("Shattuck Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:20 PM");

        restaurant = restaurants.get("Thai Delight");
        assertThat(restaurant.getRoute()).isEqualTo("Shattuck Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:10 PM");

        restaurant = restaurants.get("V&A Cafe");
        assertThat(restaurant.getRoute()).isEqualTo("Shattuck Route");
        assertThat(restaurant.getStartTime()).isEqualTo("4:50 PM");

        restaurant = restaurants.get("Kaze Ramen");
        assertThat(restaurant.getRoute()).isEqualTo("Shattuck Route");
        assertThat(restaurant.getStartTime()).isEqualTo("5:10 PM");
    }
}
