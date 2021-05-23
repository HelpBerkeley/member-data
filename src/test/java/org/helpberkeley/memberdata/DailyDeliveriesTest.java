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

import com.opencsv.exceptions.CsvException;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class DailyDeliveriesTest extends TestBase {

    @Test
    public void expectedOrderColumnsTest() throws IOException, CsvException {
        String header = Constants.WORKFLOW_CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_VEGGIE_COLUMN
                + "\n";

        HBParser.parseOrders("", header);
    }

    @Test
    public void outOfOrderColumnsTest() throws IOException, CsvException {
        String header = Constants.WORKFLOW_CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_VEGGIE_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NAME_COLUMN
                + "\n";

        HBParser.parseOrders("", header);
    }

    @Test
    public void spaceyColumnNamesTest() throws IOException, CsvException {
        String header = " " + Constants.WORKFLOW_CONSUMER_COLUMN + " " + Constants.CSV_SEPARATOR
                + " " + Constants.WORKFLOW_DRIVER_COLUMN + " " + Constants.CSV_SEPARATOR
                + " " + Constants.WORKFLOW_NORMAL_COLUMN + " " + Constants.CSV_SEPARATOR
                + " " + Constants.WORKFLOW_USER_NAME_COLUMN + " " + Constants.CSV_SEPARATOR
                + " " + Constants.WORKFLOW_VEGGIE_COLUMN + " " + Constants.CSV_SEPARATOR
                + " " + Constants.WORKFLOW_NAME_COLUMN
                + "\n";

        HBParser.parseOrders("", header);
    }

    @Test
    public void missingUserNameColumnTest() throws IOException, CsvException {
        String header = Constants.WORKFLOW_CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_VEGGIE_COLUMN
                + "\n";

        HBParser.parseOrders("", header);
    }

    @Test
    public void missingNameColumnTest() throws IOException, CsvException {
        String header = Constants.WORKFLOW_CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_VEGGIE_COLUMN
                + "\n";

        HBParser.parseOrders("", header);
    }

    @Test
    public void missingConsumerColumnTest() {
        String fileName = "missing-consumer-column";
        String header = Constants.WORKFLOW_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_DRIVER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_VEGGIE_COLUMN
                + "\n";

        Throwable thrown = catchThrowable(() -> HBParser.parseOrders(fileName, header));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(fileName);
        assertThat(thrown).hasMessageContaining(Constants.WORKFLOW_CONSUMER_COLUMN);
    }

    @Test
    public void missingDriverColumnTest() {
        String fileName = "missing-driver-column";
        String header = Constants.WORKFLOW_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_VEGGIE_COLUMN
                + "\n";

        Throwable thrown = catchThrowable(() -> HBParser.parseOrders(fileName, header));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(fileName);
        assertThat(thrown).hasMessageContaining(Constants.WORKFLOW_DRIVER_COLUMN);
    }

    @Test
    public void missingVeggieColumnTest() {
        String fileName = "missing-veggie-column";
        String header = Constants.WORKFLOW_CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NORMAL_COLUMN
                + "\n";

        Throwable thrown = catchThrowable(() -> HBParser.parseOrders(fileName, header));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(fileName);
        assertThat(thrown).hasMessageContaining(Constants.WORKFLOW_VEGGIE_COLUMN);
    }

    @Test
    public void missingNormalColumnTest() {
        String fileName = "missing-normal-column";
        String header = Constants.WORKFLOW_CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_USER_NAME_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_VEGGIE_COLUMN + Constants.CSV_SEPARATOR
                + "\n";

        Throwable thrown = catchThrowable(() -> HBParser.parseOrders(fileName, header));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(fileName);
        assertThat(thrown).hasMessageContaining(Constants.WORKFLOW_NORMAL_COLUMN);
    }

    @Test
    public void missingNameAndUserNameColumnsTest() {
        String fileName = "missing-name-username-columns";
        String header = Constants.WORKFLOW_CONSUMER_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_NORMAL_COLUMN + Constants.CSV_SEPARATOR
                + Constants.WORKFLOW_VEGGIE_COLUMN
                + "\n";

        Throwable thrown = catchThrowable(() -> HBParser.parseOrders(fileName, header));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining(fileName);
        assertThat(thrown).hasMessageContaining(Constants.WORKFLOW_USER_NAME_COLUMN);
        assertThat(thrown).hasMessageContaining(Constants.WORKFLOW_NAME_COLUMN);
    }
}
