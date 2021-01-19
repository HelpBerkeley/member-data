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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryDetailsTest extends TestBase {

    /** Test accepted variations of specifying the user name in a delivery details post */
    @Test
    public void acceptedUserNameTest() {

        List<String> userNameLines = List.of(
                "@testUser :\n",
                "@testUser\n",
                "@testUser:\n",
                "@testUser \n",
                "@testUser : \n",

                " @testUser :\n",
                " @testUser\n",
                " @testUser:\n",
                " @testUser \n",
                " @testUser : \n",

                "\n@testUser :\n",
                "\n@testUser\n",
                "\n@testUser:\n",
                "\n@testUser \n",
                "\n@testUser : \n",

                "\n @testUser :\n",
                "\n @testUser\n",
                "\n @testUser:\n",
                "\n @testUser \n",
                "\n @testUser : \n"
        );

        Map<String, DetailsPost> deliveryDetails = new HashMap<>();
        String details = "Out round back";

        for (String userSpec : userNameLines) {
            deliveryDetails.clear();

            HBParser.parseDetails(1, userSpec + details, HBParser.DetailsHandling.LAST_POST_WINS, deliveryDetails);
            assertThat(deliveryDetails).as(userSpec).containsOnlyKeys("testUser");
            DetailsPost detailsPost = deliveryDetails.get("testUser");
            assertThat(detailsPost.getDetails()).isEqualTo(details);
        }
    }

    @Test
    public void detailsTest() {

        Map<String, DetailsPost> deliveryDetails = new HashMap<>();

        String userSpec = "@testUser\n";
        String details = "simple";

        HBParser.parseDetails(1, userSpec + details, HBParser.DetailsHandling.LAST_POST_WINS, deliveryDetails);
        assertThat(deliveryDetails).containsKey("testUser");
        DetailsPost detailsPost = deliveryDetails.get("testUser");
        assertThat(detailsPost.getDetails()).isEqualTo(details);

        details = "\n\n\n and this is \n\n something else.\n ";
        String expected = "and this is something else.";

        HBParser.parseDetails(1, userSpec + details, HBParser.DetailsHandling.LAST_POST_WINS, deliveryDetails);
        detailsPost = deliveryDetails.get("testUser");
        assertThat(detailsPost.getDetails()).isEqualTo(expected);
    }
}
