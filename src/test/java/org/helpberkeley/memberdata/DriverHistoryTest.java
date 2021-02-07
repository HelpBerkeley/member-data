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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverHistoryTest extends TestBase {

    private static final String DRIVER_NAME = "fred";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY/MM/dd");
    private final LocalDate today = LocalDate.now(Constants.TIMEZONE);
    private final String todaysDateStr = today.format(dateTimeFormatter);

    @Test
    public void totalRunsTest() {
        DriverHistory driverHistory = new DriverHistory(DRIVER_NAME);
        assertThat(driverHistory.totalRuns()).isEqualTo(0);

        String run1 = "2020/12/22";
        driverHistory.addRun(run1);
        assertThat(driverHistory.totalRuns()).isEqualTo(1);
        driverHistory.addRun(run1);
        assertThat(driverHistory.totalRuns()).isEqualTo(1);
        String run2 = "2021/01/22";
        driverHistory.addRun(run2);
        assertThat(driverHistory.totalRuns()).isEqualTo(2);
        driverHistory.addRun(run2);
        assertThat(driverHistory.totalRuns()).isEqualTo(2);
    }

    @Test
    public void historyTooOldTest() {
        DriverHistory driverHistory = new DriverHistory(DRIVER_NAME);
        LocalDate tooOld = today.minusWeeks(DriverHistory.WEEKS_OF_HISTORY + 1);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("YYYY/MM/dd");
        driverHistory.addRun(tooOld.format(dateTimeFormatter));
        assertThat(driverHistory.totalRuns()).isEqualTo(1);
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void todayTest() {

        DriverHistory driverHistory = new DriverHistory(DRIVER_NAME, todaysDateStr);
        driverHistory.addRun(todaysDateStr);
        assertThat(driverHistory.totalRuns()).isEqualTo(1);
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(1, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void eachWeekTest() {
        DriverHistory driverHistory = new DriverHistory(DRIVER_NAME, "2020/12/31");

        driverHistory.addRun("2020/12/28");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(1, 0, 0, 0, 0, 0, 0);
        driverHistory.addRun("2020/12/29");
        driverHistory.addRun("2020/12/30");
        driverHistory.addRun("2020/12/31");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(4, 0, 0, 0, 0, 0, 0);
        driverHistory.addRun("2020/12/27");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(4, 1, 0, 0, 0, 0, 0);
        driverHistory.addRun("2020/12/21");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(4, 2, 0, 0, 0, 0, 0);
        driverHistory.addRun("2020/12/20");
        driverHistory.addRun("2020/12/19");
        driverHistory.addRun("2020/12/18");
        driverHistory.addRun("2020/12/17");
        driverHistory.addRun("2020/12/16");
        driverHistory.addRun("2020/12/15");
        driverHistory.addRun("2020/12/14");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(4, 2, 7, 0, 0, 0, 0);
        driverHistory.addRun("2020/12/13");
        driverHistory.addRun("2020/12/08");
        driverHistory.addRun("2020/12/07");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(4, 2, 7, 3, 0, 0, 0);
        driverHistory.addRun("2020/12/06");
        driverHistory.addRun("2020/11/30");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(4, 2, 7, 3, 2, 0, 0);
        driverHistory.addRun("2020/11/29");
        driverHistory.addRun("2020/11/23");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(4, 2, 7, 3, 2, 2, 0);
        driverHistory.addRun("2020/11/22");
        driverHistory.addRun("2020/11/16");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(4, 2, 7, 3, 2, 2, 2);
        driverHistory.addRun("2020/11/15");
        assertThat(driverHistory.getWeeklyRunTotals()).containsExactly(4, 2, 7, 3, 2, 2, 2);
    }
}
