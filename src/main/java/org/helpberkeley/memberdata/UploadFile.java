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

import org.helpberkeley.memberdata.v300.ControlBlockV300;

import java.text.MessageFormat;

public class UploadFile {
    private final String shortURL;
    private final String fileName;
    private final String originalFileName;

    private UploadFile(final String fileName, final String shortURL) {
        this.originalFileName = fileName;
        this.shortURL = shortURL;
        this.fileName = HBParser.fileNameFromShortURL(shortURL);
    }

    public String getShortURL() {
        return shortURL;
    }

    public String getFileName() {
        return fileName;
    }

    public final String getOriginalFileName() {
        return originalFileName;
    }

    public static UploadFile createUploadFile(String data){
        String fileName = HBParser.downloadFileName(data);
        String shortURL = HBParser.shortURLDiscoursePost(data);
        return new UploadFile(fileName, shortURL);
    }

    public static boolean auditFilePrefix (final String line)
            throws MemberDataException {
        if ((line.contains(Constants.UPLOAD_URI_PREFIX) || line.contains(Constants.WEB_CSV_PREFIX)) == false) {
            int prefixStart = line.lastIndexOf("]")+2;
            int prefixEnd = line.lastIndexOf("/")+1;
            throw new MemberDataException(
                    MessageFormat.format(ControlBlockV300.INVALID_FILE_PREFIX, line.substring(prefixStart,prefixEnd)));
        }
        return true;
    }

    @Override
    public String toString() {
        return fileName + " -> " + shortURL;
    }
}
