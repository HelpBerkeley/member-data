/*
 * Copyright (c) 2024. helpberkeley.org
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

import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CSVListReaderTest {

    @Test
    public void newlinesTest() {
        String csvData = "h1,h2,h3\n"
                + "a,b,c\n"
                + "1,2,3\n";

        try (StringReader stringReader = new StringReader(csvData)) {
            CSVListReader csvListReader = new CSVListReader(stringReader);

            List<List<String>> rows = csvListReader.readAllToList();
            assertThat(rows).hasSize(3);

            assertThat(rows.get(0)).hasSize(3);
            assertThat(rows.get(0)).containsExactly("h1", "h2", "h3");
            assertThat(rows.get(1)).hasSize(3);
            assertThat(rows.get(1)).containsExactly("a", "b", "c");
            assertThat(rows.get(2)).hasSize(3);
            assertThat(rows.get(2)).containsExactly("1", "2", "3");
        }
    }

    @Test
    public void carriageReturnsTest() {
        String csvData = "h1,h2,h3\r"
                + "a,b,c\r"
                + "1,2,3\r";

        try (StringReader stringReader = new StringReader(csvData)) {
            CSVListReader csvListReader = new CSVListReader(stringReader);

            List<List<String>> rows = csvListReader.readAllToList();
            assertThat(rows).hasSize(3);

            assertThat(rows.get(0)).hasSize(3);
            assertThat(rows.get(0)).containsExactly("h1", "h2", "h3");
            assertThat(rows.get(1)).hasSize(3);
            assertThat(rows.get(1)).containsExactly("a", "b", "c");
            assertThat(rows.get(2)).hasSize(3);
            assertThat(rows.get(2)).containsExactly("1", "2", "3");
        }
    }

    @Test
    public void carriageReturnsAndNewlinesTest() {
        String csvData = "h1,h2,h3\r\n"
                + "a,b,c\r\n"
                + "1,2,3\r\n";

        try (StringReader stringReader = new StringReader(csvData)) {
            CSVListReader csvListReader = new CSVListReader(stringReader);

            List<List<String>> rows = csvListReader.readAllToList();
            assertThat(rows).hasSize(3);

            assertThat(rows.get(0)).hasSize(3);
            assertThat(rows.get(0)).containsExactly("h1", "h2", "h3");
            assertThat(rows.get(1)).hasSize(3);
            assertThat(rows.get(1)).containsExactly("a", "b", "c");
            assertThat(rows.get(2)).hasSize(3);
            assertThat(rows.get(2)).containsExactly("1", "2", "3");
        }
    }


    @Test
    public void mixedCarriageReturnsAndNewlinesTest() {
        String csvData = "h1,h2,h3\r\n"
                + "a,b,c\n"
                + "1,2,3\r";

        try (StringReader stringReader = new StringReader(csvData)) {
            CSVListReader csvListReader = new CSVListReader(stringReader);

            List<List<String>> rows = csvListReader.readAllToList();
            assertThat(rows).hasSize(3);

            assertThat(rows.get(0)).hasSize(3);
            assertThat(rows.get(0)).containsExactly("h1", "h2", "h3");
            assertThat(rows.get(1)).hasSize(3);
            assertThat(rows.get(1)).containsExactly("a", "b", "c");
            assertThat(rows.get(2)).hasSize(3);
            assertThat(rows.get(2)).containsExactly("1", "2", "3");
        }
    }
}
