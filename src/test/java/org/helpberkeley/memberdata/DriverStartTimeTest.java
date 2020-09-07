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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverStartTimeTest extends TestBase {

    private static final String CAFE_RAJ = "Cafe Raj";
    private static final String KIMS = "Kim's Cafe";
    private static final String TALAVERA = "Talavera";
    private static final String SWEET_BASIL = "Sweet Basil";
    private static final String BOPSHOP = "Bopshop";
    private static final String JOT_MAHAL = "Jot Mahal";
    private static final String GREGOIRE = "Gregoire";
    private static final String CREPEVINE = "Crepevine";
    private static final String DA_LIAN = "Da Lian";
    private static final String THAI_DELIGHT = "Thai Delight";
    private static final String V_AND_A = "V&A Cafe";
    private static final String KAZE_RAMEN = "Kaze Ramen";
    private static final String TACOS_SINALOA = "Tacos Sinaloa";
    private static final String RIVOLI = "Rivoli";
    private static final String CORSO = "Corso";
    private static final String ANGELINA = "Angelina";
    private static final String AGRODOLCE = "Agrodolce";
    private static final String VANESSA = "Vanessa";
    private static final String FONDA = "Fonda";

    private final Map<String, Restaurant> restaurants;

    public DriverStartTimeTest() {
        String csvData = readResourceFile("restaurant-template-route-test.csv");
        restaurants = new RestaurantTemplateParser(csvData).restaurants();
    }

    /**
     * Test back off start times for each restaurant
     * as a 0 order, sole pickup.
     */
    @Test
    public void singleZeroOrderPickupTest() {

        for (TestData testData : List.of(
                new TestData(List.of(new Pickup(CAFE_RAJ, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(KIMS, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(TALAVERA, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(SWEET_BASIL, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(BOPSHOP, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(JOT_MAHAL, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(GREGOIRE, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(CREPEVINE, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(DA_LIAN, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(THAI_DELIGHT, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(V_AND_A, 0)), "4:50 PM"),
                new TestData(List.of(new Pickup(KAZE_RAMEN, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(TACOS_SINALOA, 0)), "5:00 PM"))) {

            checkStartTime(testData);
        }
    }

    /**
     * Test back off start times for each restaurant
     * as the first in a dual 0 order pickup run.
     */
    @Test
    public void dualZeroOrderPickupTest() {

        for (TestData testData : List.of(
                new TestData(List.of(new Pickup(CAFE_RAJ, 0), new Pickup(KIMS, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(KIMS, 0), new Pickup(TALAVERA, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(TALAVERA, 0), new Pickup(SWEET_BASIL, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(SWEET_BASIL, 0), new Pickup(BOPSHOP, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(BOPSHOP, 0), new Pickup(JOT_MAHAL, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(JOT_MAHAL, 0), new Pickup(GREGOIRE, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(GREGOIRE, 0), new Pickup(CREPEVINE, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(CREPEVINE, 0), new Pickup(DA_LIAN, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(DA_LIAN, 0), new Pickup(THAI_DELIGHT, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(THAI_DELIGHT, 0), new Pickup(KAZE_RAMEN, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(V_AND_A, 0), new Pickup(KAZE_RAMEN, 0)), "4:50 PM"),
                new TestData(List.of(new Pickup(KAZE_RAMEN, 0), new Pickup(TACOS_SINALOA, 0)), "5:00 PM"),
                new TestData(List.of(new Pickup(TACOS_SINALOA, 0), new Pickup(KAZE_RAMEN, 0)), "5:00 PM"))) {

            checkStartTime(testData);
        }
    }

    /** Put V&A in the wrong position **/
    @Test
    public void mayBeReachedAfterClosingTimeTest() {

        TestData testData = new TestData(List.of(new Pickup(THAI_DELIGHT, 0), new Pickup(V_AND_A, 0)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    /**
     * Test 2 restaurant, same route run where no special limits are hit
     */
    @Test
    public void dualMixedSameRouteTest() {
        TestData testData = new TestData(List.of(new Pickup(CAFE_RAJ, 0), new Pickup(KIMS, 1)), "5:05 PM");
        checkStartTime(testData);
    }

    /**
     * Test 2 restaurant, cross route run where no special limits are hit
     */
    @Test
    public void dualMixedCrossRouteTest() {
        TestData testData = new TestData(List.of(new Pickup(CAFE_RAJ, 0), new Pickup(KAZE_RAMEN, 1)), "5:00 PM");
        checkStartTime(testData);
    }

    @Test
    public void line1TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(V_AND_A, 1),
                new Pickup(CREPEVINE, 0),
                new Pickup(DA_LIAN, 3)), "4:50 PM");

        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line9TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CAFE_RAJ, 0),
                new Pickup(DA_LIAN, 4)), "5:00 PM");
        checkStartTime(testData);
    }

    @Test
    public void line14TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(CAFE_RAJ, 0),
                new Pickup(JOT_MAHAL, 0),
                new Pickup(DA_LIAN, 2)), "5:05 PM");
        checkStartTime(testData);
    }

    @Test
    public void line19TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CAFE_RAJ, 0),
                new Pickup(BOPSHOP, 0),
                new Pickup(DA_LIAN, 4)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, DA_LIAN));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line26TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CAFE_RAJ, 0),
                new Pickup(BOPSHOP, 0),
                new Pickup(V_AND_A, 0),
                new Pickup(DA_LIAN, 4)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, DA_LIAN));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line35TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(CAFE_RAJ, 0),
                new Pickup(V_AND_A, 0),
                new Pickup(SWEET_BASIL, 0),
                new Pickup(BOPSHOP, 2)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, BOPSHOP));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line43TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(V_AND_A, 0),
                new Pickup(BOPSHOP, 0),
                new Pickup(SWEET_BASIL, 2),
                new Pickup(CAFE_RAJ, 1)), "4:50 PM");
        checkStartTime(testData);
    }

    @Test
    public void line49TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(CAFE_RAJ, 0),
                new Pickup(V_AND_A, 0),
                new Pickup(SWEET_BASIL, 0),
                new Pickup(BOPSHOP, 0)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line57TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(RIVOLI, 0),
                new Pickup(CORSO, 0),
                new Pickup(V_AND_A, 0)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, CORSO),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line65TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CORSO, 0),
                new Pickup(V_AND_A, 0)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, CORSO),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line73TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CREPEVINE, 0),
                new Pickup(ANGELINA, 2)), "5:10 PM");
        checkStartTime(testData);
    }

    @Test
    public void line79TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CREPEVINE, 0),
                new Pickup(THAI_DELIGHT, 0),
                new Pickup(ANGELINA, 2)), "5:05 PM");
        checkStartTime(testData);
    }

    @Test
    public void line86TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CREPEVINE, 0),
                new Pickup(THAI_DELIGHT, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(ANGELINA, 2)), "5:00 PM");
        checkStartTime(testData);
    }

    @Test
    public void line96TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CREPEVINE, 0),
                new Pickup(THAI_DELIGHT, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(KAZE_RAMEN, 0),
                new Pickup(ANGELINA, 2)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, ANGELINA));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line106TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CREPEVINE, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(CORSO, 0),
                new Pickup(KAZE_RAMEN, 0),
                new Pickup(ANGELINA, 2)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, CORSO),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, ANGELINA));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line116TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(VANESSA, 0),
                new Pickup(CREPEVINE, 0),
                new Pickup(ANGELINA, 2)), "5:05 PM");
        checkStartTime(testData);
    }

    @Test
    public void line123TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(CREPEVINE, 0),
                new Pickup(VANESSA, 0),
                new Pickup(ANGELINA, 2)), "5:00 PM");
        checkStartTime(testData);
    }

    @Test
    public void line129TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(CORSO, 0),
                new Pickup(CREPEVINE, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(JOT_MAHAL, 0),
                new Pickup(KAZE_RAMEN, 0),
                new Pickup(ANGELINA, 2)), "4:50 PM");
        checkStartTime(testData);
    }

    @Test
    public void line138TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(CORSO, 0),
                new Pickup(V_AND_A, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(JOT_MAHAL, 0),
                new Pickup(KAZE_RAMEN, 0),
                new Pickup(ANGELINA, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line148TestCase() {
        TestData testData = new TestData(List.of(
                new Pickup(CORSO, 0),
                new Pickup(V_AND_A, 0),
                new Pickup(FONDA, 0),
                new Pickup(JOT_MAHAL, 0),
                new Pickup(KAZE_RAMEN, 0),
                new Pickup(ANGELINA, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, ANGELINA),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line159() {
        TestData testData = new TestData(List.of(
                new Pickup(CORSO, 0),
                new Pickup(V_AND_A, 0),
                new Pickup(RIVOLI, 0),
                new Pickup(JOT_MAHAL, 0),
                new Pickup(KAZE_RAMEN, 0),
                new Pickup(ANGELINA, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, ANGELINA),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, RIVOLI),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line171() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CREPEVINE, 0),
                new Pickup(THAI_DELIGHT, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(V_AND_A, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, V_AND_A),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line181() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CORSO, 0),
                new Pickup(RIVOLI, 0),
                new Pickup(THAI_DELIGHT, 0),
                new Pickup(V_AND_A, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, CORSO),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, RIVOLI),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line193() {
        TestData testData = new TestData(List.of(
                new Pickup(CORSO, 0),
                new Pickup(JOT_MAHAL, 0),
                new Pickup(RIVOLI, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(V_AND_A, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, RIVOLI),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, V_AND_A));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line204() {
        TestData testData = new TestData(List.of(
                new Pickup(CORSO, 0),
                new Pickup(JOT_MAHAL, 0),
                new Pickup(RIVOLI, 0),
                new Pickup(V_AND_A, 0),
                new Pickup(AGRODOLCE, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, RIVOLI),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, AGRODOLCE));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line215() {
        TestData testData = new TestData(List.of(
                new Pickup(JOT_MAHAL, 0),
                new Pickup(CORSO, 0),
                new Pickup(RIVOLI, 0),
                new Pickup(V_AND_A, 0),
                new Pickup(AGRODOLCE, 2)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, CORSO),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, RIVOLI),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, V_AND_A),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, AGRODOLCE));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line227() {
        TestData testData = new TestData(List.of(
                new Pickup(FONDA, 0),
                new Pickup(CREPEVINE, 0),
                new Pickup(THAI_DELIGHT, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(ANGELINA, 2)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, ANGELINA));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line236() {
        TestData testData = new TestData(List.of(
                new Pickup(FONDA, 0),
                new Pickup(THAI_DELIGHT, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(ANGELINA, 2)), "5:00 PM");
        checkStartTime(testData);
    }

    @Test
    public void line243() {
        TestData testData = new TestData(List.of(
                new Pickup(THAI_DELIGHT, 0),
                new Pickup(FONDA, 0),
                new Pickup(AGRODOLCE, 0),
                new Pickup(ANGELINA, 2)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, FONDA),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, ANGELINA));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line252() {
        TestData testData = new TestData(List.of(
                new Pickup(THAI_DELIGHT, 0),
                new Pickup(FONDA, 0),
                new Pickup(ANGELINA, 2)), "5:00 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, FONDA));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line259() {
        TestData testData = new TestData(List.of(
                new Pickup(V_AND_A, 0),
                new Pickup(FONDA, 0),
                new Pickup(ANGELINA, 2)), "4:50 PM");
        checkStartTime(testData);
    }

    @Test
    public void line265() {
        TestData testData = new TestData(List.of(
                new Pickup(V_AND_A, 0),
                new Pickup(FONDA, 0),
                new Pickup(JOT_MAHAL, 0),
                new Pickup(GREGOIRE, 0),
                new Pickup(ANGELINA, 2)), "4:50 PM");
        checkStartTime(testData);
    }

    @Test
    public void line273() {
        TestData testData = new TestData(List.of(
                new Pickup(V_AND_A, 0),
                new Pickup(FONDA, 0),
                new Pickup(CORSO, 0),
                new Pickup(GREGOIRE, 0),
                new Pickup(ANGELINA, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, CORSO));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line282() {
        TestData testData = new TestData(List.of(
                new Pickup(V_AND_A, 0),
                new Pickup(FONDA, 0),
                new Pickup(CORSO, 0),
                new Pickup(ANGELINA, 0),
                new Pickup(GREGOIRE, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, CORSO),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, GREGOIRE));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line292() {
        TestData testData = new TestData(List.of(
                new Pickup(V_AND_A, 0),
                new Pickup(FONDA, 0),
                new Pickup(CORSO, 0),
                new Pickup(BOPSHOP, 0),
                new Pickup(ANGELINA, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, CORSO),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, ANGELINA));
        checkStartTime(testData, expectedWarnings);
    }

    @Test
    public void line302() {
        TestData testData = new TestData(List.of(
                new Pickup(V_AND_A, 0),
                new Pickup(FONDA, 0),
                new Pickup(CORSO, 0),
                new Pickup(ANGELINA, 0),
                new Pickup(BOPSHOP, 2)), "4:50 PM");
        List<String> expectedWarnings = List.of(
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_CLOSING, CORSO),
                MessageFormat.format(Driver.WARNING_MAY_BE_REACHED_AFTER_EXPECTED, BOPSHOP));
        checkStartTime(testData, expectedWarnings);
    }

    private void checkStartTime(TestData testData) {
        Driver driver = buildDriver(testData);
        assertThat(driver.getStartTime())
                .as(testData.toString())
                .isEqualTo(testData.expectedStartTime);
        assertThat(driver.getWarningMessages()).as(testData.toString()).isEmpty();

    }

    private void checkStartTime(TestData testData, List<String> expectedWarnings) {
        Driver driver = buildDriver(testData);
        assertThat(driver.getStartTime())
                .as(testData.toString())
                .isEqualTo(testData.expectedStartTime);
        assertThat(driver.getWarningMessages())
                .as(testData.toString()).containsOnlyOnceElementsOf(expectedWarnings);
    }

    private Driver buildDriver(TestData testData) {

        List<Restaurant> pickupRestaurants = new ArrayList<>();

        for (Pickup pickup : testData.pickups) {

            Restaurant restaurant = new Restaurant(pickup.restaurantName);
            restaurant.mergeGlobal(restaurants.get(restaurant.getName()));
            restaurant.addOrders(pickup.numOrders);

            pickupRestaurants.add(restaurant);
        }

        WorkflowBean driverBean = new WorkflowBean();
        driverBean.setUserName("jb");

        return new Driver(driverBean, pickupRestaurants, null, "https://something");
    }

    static class TestData {
        final List<Pickup> pickups;
        final String expectedStartTime;

        TestData(List<Pickup> pickups, String expectedStartTime) {
            this.pickups = pickups;
            this.expectedStartTime = expectedStartTime;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            for (Pickup pickup : pickups) {

                builder.append(builder.length() == 0 ? "" : ", ");
                builder.append(pickup.restaurantName);
                builder.append("(");
                builder.append(pickup.numOrders);
                builder.append(")");
            }

            return "Expected Start: " + expectedStartTime + ", Pickups: " + builder.toString();
        }
    }

    static class Pickup {
        final String restaurantName;
        final int numOrders;

        Pickup(String restaurantName, int numOrders) {
            this.restaurantName = restaurantName;
            this.numOrders = numOrders;
        }
    }
}
