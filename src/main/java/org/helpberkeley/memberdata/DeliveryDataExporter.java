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

import java.io.IOException;
import java.util.List;

public class DeliveryDataExporter extends Exporter {

    private final List<DeliveryData> deliveryData;

    DeliveryDataExporter(List<DeliveryData> deliveryData) {
        this.deliveryData = deliveryData;
    }

    String deliveryPostsToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, deliveryPosts());

        return outputFileName;
    }

    String deliveryPosts() {

        StringBuilder output = new StringBuilder();
        output.append(DeliveryData.deliveryPostsHeader());

        for (DeliveryData item : deliveryData) {

            UploadFile uploadFile = item.getUploadFile();
            output.append(item.getDate());
            output.append(Constants.CSV_SEPARATOR);
            output.append(uploadFile.originalFileName);
            output.append(Constants.CSV_SEPARATOR);
            output.append(uploadFile.shortURL);
            output.append('\n');
        }

        return output.toString();
    }
}
