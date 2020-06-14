//
// Copyright (c) 2020 helpberkeley.org
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.helpberkeley.memberdata;

import java.util.HashMap;
import java.util.Map;

public class ApiQueryResult {

    final String[] headers;
    final Object[] rows;

    final Map<String, Integer> columnIndexes = new HashMap<>();

    ApiQueryResult(final Object[] headers, final Object[] rows) {
        this.headers = validateColumns(headers);
        this.rows = rows;

        validate();
    }

    Integer getColumnIndex(final String columnName) {
        return columnIndexes.get(columnName);
    }

    private String[] validateColumns(final Object[] columns) {

        assert columns != null;

        String[] columnNames = new String[columns.length];

        for (int index = 0; index < columns.length; index++) {
            String columnName = (String)columns[index];
            columnNames[index] = columnName;
            assert ! columnIndexes.containsKey(columnName) : columnName;
            columnIndexes.put(columnName, index);
        }

        return columnNames;
    }

    private void validate() {
        assert headers != null;
        assert rows != null;

        for (Object rowObject : rows) {
            assert rowObject instanceof Object[] : rowObject;
            assert ((Object[]) rowObject).length == headers.length;
        }
    }

//    @Override
//    public String toString() {
//        StringBuilder output = new StringBuilder();
//
//        for (String column : headers) {
//            output.append(column);
//            output.append(Constants.CSV_SEPARATOR);
//        }
//        output.append('\n');
//        output.append(rows.length);
//        output.append(" rows\n");
//
//
//        return output.toString();
//    }
}
