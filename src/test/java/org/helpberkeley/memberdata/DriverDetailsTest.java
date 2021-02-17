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

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverDetailsTest extends TestBase {
    @Test
    public void multipleDetailPostingsTest() {
        ApiClient apiClient = createApiSimulator();
        String json = apiClient.runQuery(Constants.QUERY_GET_DRIVER_DETAILS);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        Map<String, DetailsPost> driverDetails = HBParser.driverDetails(apiQueryResult);
        assertThat(driverDetails).hasSize(4);
        assertThat(driverDetails).containsKeys("user1", "user2", "user3", "fred");
        DetailsPost detailsPost = driverDetails.get("user1");
        assertThat(detailsPost.getDetails()).isEqualTo(
                "One of our very first drivers. Test sample from driver status.");
        detailsPost = driverDetails.get("user2");
        assertThat(detailsPost.getDetails()).isEqualTo(
                "also one of our very first drivers. Test sample from driver status. "
                + "Unavailable on 12/7. Test sample from driver status. "
                + "Available on 12/6 for backup. Test sample from driver status.");
        detailsPost = driverDetails.get("user3");
        assertThat(detailsPost.getDetails()).isEqualTo(
            "Our first driver, but also comes with a lot of luggage. Keep a good eye on the guy. "
            + "Test sample from driver status. "
            + "Try to avoid using him since he is backing up a lot of other processes at HB. "
            + "Test sample from driver status.");
        detailsPost = driverDetails.get("fred");
        assertThat(detailsPost.getDetails()).isEqualTo(
                "Does not have a cellphone & can be hard to reach. "
                + "Typically delivers by bike regardless of location. "
                + "Very reliable! Better not use as backup driver, but great for last-minute replace.");
    }
}
