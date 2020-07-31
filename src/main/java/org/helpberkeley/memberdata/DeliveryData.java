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

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class DeliveryData {

    final String date;
    final UploadFile uploadFile;

    DeliveryData(String date, final String fileName, String shortURL) {
        this.date = date.trim();
        this.uploadFile = new UploadFile(fileName, shortURL);
    }

    // Get daily delivery posts
    static List<DeliveryData> deliveryPosts(ApiClient apiClient) throws IOException, InterruptedException {
        String json = apiClient.runQuery(Constants.QUERY_GET_DAILY_DELIVERIES);
        ApiQueryResult apiQueryResult = HBParser.parseQueryResult(json);
        List<DeliveryData> deliveryPosts = HBParser.dailyDeliveryPosts(apiQueryResult);

        // Sort by date ascending
        deliveryPosts.sort(Comparator.comparing(DeliveryData::getDate));
        return deliveryPosts;
    }

    // Get daily delivery posts
    static List<DeliveryData> deliveryPosts(final String csvData) {
        List<DeliveryData> deliveryPosts = HBParser.dailyDeliveryPosts(csvData);

        // Sort by date ascending
        deliveryPosts.sort(Comparator.comparing(DeliveryData::getDate));
        return deliveryPosts;
    }

    static String deliveryPostsHeader() {
        // FIX THIS, DS: constants
        return "Date" + Constants.CSV_SEPARATOR
                + "File" + Constants.CSV_SEPARATOR
                + "URL"
                + '\n';
    }

    String getDate() {
        return date;
    }

    UploadFile getUploadFile() {
        return uploadFile;
    }

    @Override
    public String toString() {
        return date + " - " + uploadFile;
    }
}
