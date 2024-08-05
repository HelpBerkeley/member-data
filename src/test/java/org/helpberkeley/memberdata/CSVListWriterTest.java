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

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CSVListWriterTest {

    @Test
    public void csvListWriterReaderTest() throws IOException {
        StringWriter writer1 = new StringWriter();
        StringWriter writer2 = new StringWriter();

        CSVListWriter csvListWriter1 = new CSVListWriter(writer1);
        CSVListWriter csvListWriter2 = new CSVListWriter(writer2);

        List<List<String>> dataToEncode = new ArrayList<>();

        String col0 = "";
        String col1 = "simple";
        String col2 = "simple with space";
        String col3 = "has, a single comma";
        String col4 = "has, a pair of, commas";
        String col5 = "has, a single quote \"";
        String col6 = "has a \"quoted string\"";
        String col7 = "has multiple \"quoted\" \"strings\"";
        String col8 = "has a comma, and a \"quoted string\"";
        String col9 = "has, multiple commas, and \"quoted\" \"strings\"";
        String col10 = "has, a, comma, \"inside, quoted\" string";

        List<String> row = new ArrayList<>(List.of(col0, col1, col2, col3, col4, col5, col6, col7, col8, col9, col10));
        dataToEncode.add(row);

        csvListWriter1.writeAllToList(dataToEncode);
        csvListWriter1.close();
        csvListWriter2.writeNextToList(row);
        csvListWriter2.close();

        String encodedCSV1 = writer1.toString();
        String encodedRow = writer2.toString();
        CSVListReader csvListReader1 = new CSVListReader(new StringReader(encodedCSV1));
        CSVListReader csvListReader2 = new CSVListReader(new StringReader(encodedRow));
        List<List<String>> csvParsed = csvListReader1.readAllToList();
        List<String> rowParsedFromCSV = csvParsed.get(0);
        List<String> rowParsedReadNext = csvListReader2.readNextToList();

        assertThat(rowParsedFromCSV).isEqualTo(row);
        assertThat(rowParsedReadNext).isEqualTo(row);
        System.out.println(encodedRow);
        System.out.println(rowParsedReadNext.toString().replaceAll("[\\[\\]]", ""));
        assertThat(encodedRow).isNotEqualTo(rowParsedReadNext.toString().replaceAll("[\\[\\]]", ""));
    }

    @Test
    public void csvWriterTest() throws IOException, CsvException {
        List<String[]> dataToEncode = new ArrayList<>();

        String col0 = "";
        String col1 = "simple";
        String col2 = "simple with space";
        String col3 = "has, a single comma";
        String col4 = "has, a pair of, commas";
        String col5 = "has, a single quote \"";
        String col6 = "has a \"quoted string\"";
        String col7 = "has multiple \"quoted\" \"strings\"";
        String col8 = "has a comma, and a \"quoted string\"";
        String col9 = "has, multiple commas, and \"quoted\" \"strings\"";
        String col10 = "has, a, comma, \"inside, quoted\" string";

        String[] row = {col0, col1, col2, col3, col4, col5, col6, col7, col8, col9, col10};
        dataToEncode.add(row);
        String encodedCSV;
        try (StringWriter writer = new StringWriter()) {
            CSVWriter csvWriter = new CSVWriter(writer);
            csvWriter.writeAll(dataToEncode);
            encodedCSV = writer.toString();
        }

        CSVReader csvReader;
        List<String[]> csvParsed;
        try (StringReader reader = new StringReader(encodedCSV)) {
            csvReader = new CSVReader(reader);
            csvParsed = csvReader.readAll();
        }
        String[] parsedRow = csvParsed.get(0);

        // Validate roundtrip
        assertThat(row).isEqualTo(parsedRow);
    }
}
