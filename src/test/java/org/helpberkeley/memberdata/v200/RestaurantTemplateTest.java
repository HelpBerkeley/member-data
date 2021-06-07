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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class RestaurantTemplateTest extends RestaurantTemplateTestBase {

    private static final String HEADER = "Consumer,Driver,Name,User Name,Phone #,Phone2 #,"
        + "Neighborhood,City,Address,Condo,Details,Restaurants,normal,veggie,#orders\n";
    private static final String CONTROL_BLOCK_BEGIN_ROW = "FALSE,FALSE,ControlBegin,,,,,,,,,,,,\n";
    private static final String CONTROL_BLOCK_VERSION_ROW = "FALSE,FALSE,,Version,,,,2-0-0,,,,,,,\n";
    private static final String CONTROL_BLOCK_END_ROW = "FALSE,FALSE,ControlEnd,,,,,,,,,,,,\n";

    private static final String CONTROL_BLOCK =
            HEADER + CONTROL_BLOCK_BEGIN_ROW + CONTROL_BLOCK_VERSION_ROW + CONTROL_BLOCK_END_ROW;

    @Override
    public String getEmptyRow() {
        return org.helpberkeley.memberdata.v200.ControlBlockTest.EMPTY_ROW;
    }

    @Override
    public String getRestaurantTemplate() {
        return readResourceFile("restaurant-template-v200.csv");
    }

//,,,,,,,,,,,,,,
//        FALSE,TRUE,,,,,,,,,,,,,
//        FALSE,,,,,,,,"1158 Solano Ave, Albany",FALSE,,Cafe Raj,,,
//,,,,,,,,,,,,,,
//        FALSE,TRUE,,,,,,,,,,,,,
//        FALSE,,,,,,,,"1543 Shattuck Ave, Berkeley",FALSE,,Jot Mahal,,,
//,,,,,,,,,,,,,,
//,,,,,,,,,,,,,,
//,,,,,,,,,Pics,,,Emojis,Starting,Closing
//        Solano Route,,,,,,,,,,,Cafe Raj,:open_umbrella:,5:10 PM,10:00 PM
//        Shattuck Route,,,,,,,,,,,Jot Mahal,:tractor:,5:10 PM,9:00 PM

    @Test
    public void missingEmojiTest() {
        String csvData = CONTROL_BLOCK
                + "Solano Route,,,,,,,,,,TRUE,Cafe Raj,:open_umbrella:,5:10 PM,10:00 PM\n"
                + "Shattuck Route,,,,,,,,,,TRUE,Jot Mahal,,5:10 PM,9:00 PM\n";

        RestaurantTemplateParser parser = RestaurantTemplateParser.create(csvData);

        Throwable thrown = catchThrowable(parser::restaurants);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(
                "Restaurant Template Error: missing emoji value from column normal, line number 5");
    }

    @Test
    public void inactiveRestaurantTest() {
        String templateData = readResourceFile("restaurant-template-v200.csv");
        RestaurantTemplateParser parser = RestaurantTemplateParser.create(templateData);

        Map<String, Restaurant> restaurants = parser.restaurants();
        assertThat(parser.getVersion()).isEqualTo(Constants.CONTROL_BLOCK_VERSION_200);
        assertThat(restaurants).doesNotContainKey("Kaze Ramen");
    }

    @Test
    public void turkeyTemplateTest() {
        String templateData = readResourceFile("restaurant-template-turkey.csv");
        RestaurantTemplateParser parser = RestaurantTemplateParser.create(templateData);

        Map<String, Restaurant> restaurants = parser.restaurants();
        assertThat(parser.getVersion()).isEqualTo(Constants.CONTROL_BLOCK_VERSION_200);
        assertThat(restaurants).containsKey("Nourish You!");
    }

    @Test
    public void v1UnsupportedTest() {
        String templateData = readResourceFile("restaurant-template-v1.csv");
        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(templateData));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Control block version 1 is not supported for restaurant templates");
    }
}
