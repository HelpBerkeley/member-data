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

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DailyDeliveriesTest extends TestBase {
    @Test
    public void parseDailyDeliveriesQueryTest() throws IOException, InterruptedException {
        ApiClient apiClient = createApiSimulator();
        String jsonData = apiClient.runQuery(Constants.QUERY_GET_DAILY_DELIVERIES);
        ApiQueryResult queryResult = Parser.parseQueryResult(jsonData);
        List<DeliveryData> deliveries = Parser.dailyDeliveryPosts(queryResult);
    }

    @Test
    public void expectedOrderColumnsTest() {
        String header = Parser.DeliveryColumns.CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.VEGGIE_COLUMN + Constants.CSV_SEPARATOR
                + "\n";

        Parser.parseOrders("", header);
    }

    @Test
    public void outOfOrderColumnsTest() {
        String header = Parser.DeliveryColumns.CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.VEGGIE_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NAME_COLUMN + Constants.CSV_SEPARATOR
                + "\n";

        Parser.parseOrders("", header);
    }

    @Test
    public void spaceyColumnNamesTest() {
        String header = " " + Parser.DeliveryColumns.CONSUMER_COLUMN + " " + Constants.CSV_SEPARATOR
                + " " + Parser.DeliveryColumns.NORMAL_COLUMN + " " + Constants.CSV_SEPARATOR
                + " " + Parser.DeliveryColumns.USER_NAME_COLUMN + " " + Constants.CSV_SEPARATOR
                + " " + Parser.DeliveryColumns.VEGGIE_COLUMN + " " + Constants.CSV_SEPARATOR
                + " " + Parser.DeliveryColumns.NAME_COLUMN + " " + Constants.CSV_SEPARATOR
                + "\n";

        Parser.parseOrders("", header);
    }

    @Test
    public void missingUserNameColumnTest() {
        String header = Parser.DeliveryColumns.CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.VEGGIE_COLUMN + Constants.CSV_SEPARATOR
                + "\n";

        Parser.parseOrders("", header);
    }

    @Test
    public void missingNameColumnTest() {
        String header = Parser.DeliveryColumns.CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.VEGGIE_COLUMN + Constants.CSV_SEPARATOR
                + "\n";

        Parser.parseOrders("", header);
    }

    @Test
    public void missingConsumerColumnTest() {
        String fileName = "missing-consumer-column";
        String header = Parser.DeliveryColumns.NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.VEGGIE_COLUMN + Constants.CSV_SEPARATOR
                + "\n";

        Throwable thrown = catchThrowable(() -> Parser.parseOrders(fileName, header));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(fileName);
        assertThat(thrown).hasMessageContaining(Parser.DeliveryColumns.CONSUMER_COLUMN);
    }

    @Test
    public void missingVeggieColumnTest() {
        String fileName = "missing-veggie-column";
        String header = Parser.DeliveryColumns.CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + "\n";

        Throwable thrown = catchThrowable(() -> Parser.parseOrders(fileName, header));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(fileName);
        assertThat(thrown).hasMessageContaining(Parser.DeliveryColumns.VEGGIE_COLUMN);
    }

    @Test
    public void missingNormalColumnTest() {
        String fileName = "missing-normal-column";
        String header = Parser.DeliveryColumns.CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.VEGGIE_COLUMN + Constants.CSV_SEPARATOR
                + "\n";

        Throwable thrown = catchThrowable(() -> Parser.parseOrders(fileName, header));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(fileName);
        assertThat(thrown).hasMessageContaining(Parser.DeliveryColumns.NORMAL_COLUMN);
    }

    @Test
    public void missingNameAndUserNameColumnsTest() {
        String fileName = "missing-name-username-columns";
        String header = Parser.DeliveryColumns.CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Parser.DeliveryColumns.VEGGIE_COLUMN + Constants.CSV_SEPARATOR
                + "\n";

        Throwable thrown = catchThrowable(() -> Parser.parseOrders(fileName, header));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(fileName);
        assertThat(thrown).hasMessageContaining(Parser.DeliveryColumns.USER_NAME_COLUMN);
        assertThat(thrown).hasMessageContaining(Parser.DeliveryColumns.NAME_COLUMN);
    }
}
