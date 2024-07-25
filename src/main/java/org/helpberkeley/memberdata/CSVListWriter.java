/*
 * Copyright (c) 2024 helpberkeley.org
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

import com.opencsv.CSVWriter;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class CSVListWriter extends CSVWriter {

    public CSVListWriter(Writer writer) {
        super(writer);
    }

    public void writeNextRow(List<String> row) {
        String[] simpleArray = new String[ row.size() ];
        row.toArray( simpleArray );
        writeNext(simpleArray);
    }

    public void writeAllRows(List<List<String>> csvData) {
        List<String[]> listOfArrays = new ArrayList<>();

        for (List<String> innerList : csvData) {
            String[] array = innerList.toArray(new String[0]);
            listOfArrays.add(array);
        }
        writeAll(listOfArrays);
    }
}
