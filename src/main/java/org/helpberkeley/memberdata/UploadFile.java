/*
 * Copyright (c) 2020.2024. helpberkeley.org
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

import java.text.MessageFormat;

public class UploadFile {
    private final String shortURL;
    private final String fileName;
    private final String originalFileName;
    public static final String INVALID_FILE_PREFIX = "\"{0}\" does not contain a supported file prefix";

    private UploadFile(final String fileName, final String shortURL) {
        this.originalFileName = fileName;
        this.shortURL = shortURL;
        this.fileName = fileNameFromShortURL(shortURL);
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

    private String fileNameFromShortURL(final String shortURL) {
        assert (UploadFile.containsUploadFileURL(shortURL));
        String prefix;
        if (shortURL.startsWith(Constants.UPLOAD_URI_PREFIX)) {
            prefix = Constants.UPLOAD_URI_PREFIX;
        }
        else {
            prefix = Constants.WEB_CSV_PREFIX;
        }
        assert shortURL.length() > prefix.length() : shortURL;
        return shortURL.substring(prefix.length());
    }

    private static String downloadFileName(final String line) {
        UploadFile.auditFilePrefix(line);
        int index = line.indexOf('[');
        assert index != -1 : line;
        int end = line.indexOf('|');
        assert end > index : line;

        return line.substring(index + 1, end);
    }

    private static String shortURLDiscoursePost(final String line) {
        UploadFile.auditFilePrefix(line);
        int index = -1;
        int prefixLength = 0;
        if (line.contains(Constants.UPLOAD_URI_PREFIX)) {
            index = line.indexOf(Constants.UPLOAD_URI_PREFIX);
            prefixLength= Constants.UPLOAD_URI_PREFIX.length();
        }
        else if (line.contains(Constants.WEB_CSV_PREFIX)){
            index = line.indexOf(Constants.WEB_CSV_PREFIX);
            prefixLength= Constants.WEB_CSV_PREFIX.length();
        }
        assert index != -1 : line;
        String shortURL = Constants.UPLOAD_URI_PREFIX.concat(line.substring(index + prefixLength));
        index = shortURL.indexOf(')');
        shortURL = shortURL.substring(0, index);

        return shortURL;
    }

    public static UploadFile createUploadFile(String data){

        String fileName = downloadFileName(data);
        String shortURL = shortURLDiscoursePost(data);
        return new UploadFile(fileName, shortURL);
    }

    public static boolean containsUploadFileURL(String line){
        return line.contains(Constants.UPLOAD_URI_PREFIX) || line.contains(Constants.WEB_CSV_PREFIX);
    }

    public static void auditFilePrefix (final String line) throws MemberDataException {
        if (!(containsUploadFileURL(line))) {
            throw new MemberDataException(
                    MessageFormat.format(UploadFile.INVALID_FILE_PREFIX, line));
        }
    }

    @Override
    public String toString() {
        return fileName + " -> " + shortURL;
    }
}
