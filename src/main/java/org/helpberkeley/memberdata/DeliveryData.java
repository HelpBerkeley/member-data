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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeliveryData {

    private static final String DATE_REGEX = "^202[0-9]/[01][0-9]/[0-3][0-9]$";
    private static final Pattern DATE_PATTERN = Pattern.compile(DATE_REGEX);

    final String date;
    final UploadFile uploadFile;

    DeliveryData(String date, final String fileName, String shortURL) {
        this.date = date.trim();
        this.uploadFile = new UploadFile(fileName, shortURL);
        audit();
    }

    // Validate the date string
    private void audit() {
        Matcher matcher = DATE_PATTERN.matcher(date);

        if (! matcher.find()) {
            throw new MemberDataException("\"" + date + "\" is not a valid date. Must be of the form YYYY/MM/DD");
        }
    }

    static String deliveryPostsHeader() {
        // FIX THIS, DS: constants
        return "Date" + Constants.CSV_SEPARATOR
                + "File" + Constants.CSV_SEPARATOR
                + "URL"
                + '\n';
    }

    String getDate() {
        return date;
    }

    UploadFile getUploadFile() {
        return uploadFile;
    }

    @Override
    public String toString() {
        return date + " - " + uploadFile;
    }
}
