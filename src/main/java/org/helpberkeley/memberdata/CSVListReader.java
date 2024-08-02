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

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVListReader extends CSVReader {

    public CSVListReader(Reader reader) {
        super(reader);
    }

    public List<List<String>> readAllToList() {
        List<String[]> rows;
        try {
            rows = readAll();
        } catch (IOException | CsvException ex) {
            throw new MemberDataException(ex);
        }
        List<List<String>> listOfLists = new ArrayList<>();

        for (String[] array : rows) {
            List<String> list = new ArrayList<>();
            for (String s: array) {
                list.add(s.trim());
            }
            listOfLists.add(list);
        }
        return listOfLists;
    }

    public List<String> readNextToList() {
        String[] row;
        try {
            row = readNext();
        } catch (IOException | CsvException ex) {
            throw new MemberDataException(ex);
        }
        List<String> rowList = new ArrayList<>();
        for (String s: row) {
            rowList.add(s.trim());
        }
        return rowList;
    }
}
