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
package org.helpberkeley.memberdata.v300;

import com.opencsv.exceptions.CsvException;
import org.helpberkeley.memberdata.DriverHistory;
import org.helpberkeley.memberdata.TestBase;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverHistoryTest extends TestBase {

    @Test
    public void oneKitchenDriverHistoryTest() throws IOException, CsvException {

        String history = DriverHistory.generateOneKitchenDriverHistory(createApiSimulator());
        String[] lines = history.split("\n");
        assertThat(lines).containsExactlyInAnyOrder(
                "username,delivery_date",
                "jbDriver,2020/12/30",
                "MrBackup772,2020/12/30",
                "jbDriver,2021/05/02",
                "jsDriver,2021/05/02",
                "JVol,2021/05/02",
                "MrBackup772,2021/05/02");
    }
}