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

public class ApiQueryResult {

    final String[] headers;
    final Object[] rows;

    ApiQueryResult(final Object[] headers, final Object[] rows) throws ApiException {
        this.headers = validateColumns(headers);
        this.rows = rows;

        validate();
    }

    private String[] validateColumns(final Object[] columns) {

        if (columns == null) {
            return new String[0];
        }

        String[] columnNames = new String[columns.length];

        for (int index = 0; index < columns.length; index++) {
            columnNames[index] = (String)columns[index];
        }

        return columnNames;
    }

    private void validate() throws ApiException {
        if ((headers == null) || (rows == null)) {
            throw new ApiException("Null data: " + toString());
        }

        for (Object rowObject : rows) {
            if (!(rowObject instanceof Object[])) {
                throw new ApiException("Data row is not an array: " + rowObject);
            }

            if (((Object[]) rowObject).length != headers.length) {
                throw new ApiException("Columns/row data: mismatch" + toString());
            }
        }
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        if (headers == null) {
            builder.append("No columns");
        } else {
            builder.append("Columns: ");

            for (String column : headers) {
                builder.append(column);
                builder.append(',');
            }
        }
        builder.append('\n');
        if (rows == null) {
            builder.append("No columns");
        } else {
            builder.append(rows.length);
            builder.append(" rows");
        }
        builder.append('\n');

        return builder.toString();
    }
}