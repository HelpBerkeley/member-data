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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Exporter {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Exporter.class);
    protected final String separator = Constants.CSV_SEPARATOR;

    protected String generateFileName(String fileName, String suffix) {
        String timestamp = ZonedDateTime.now(ZoneId.systemDefault()).
                format(DateTimeFormatter.ofPattern("uuMMdd-HHmm-ss"));
        return fileName + '-' + timestamp + '.' + suffix;
    }

    protected void writeFile(final String fileName, final String fileData) throws IOException {
        Path filePath = Paths.get(fileName);
        Files.deleteIfExists(filePath);
        Files.createFile(Paths.get(fileName));
        Files.writeString(filePath, fileData);

        LOGGER.debug("Wrote: " + fileName);
    }

    protected String escapeCommas(final String value) {
        if (value.indexOf(',') == -1) {
            return value;
        }

        return "\"" + value + "\"";
    }
}
