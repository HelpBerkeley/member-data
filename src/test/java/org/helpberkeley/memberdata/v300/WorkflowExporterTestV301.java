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
package org.helpberkeley.memberdata.v300;

import org.helpberkeley.memberdata.DetailsPost;
import org.helpberkeley.memberdata.UserExporter;
import org.helpberkeley.memberdata.WorkflowExporterTestBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class WorkflowExporterTestV301 extends WorkflowExporterTestBase {

    @Override
    public String getResourceFile() throws IOException {
        String changedFilename = changeResourceCBVersion("update-member-data-multiple-updates.csv", "3-0-1");
        return Files.readString(Paths.get(changedFilename));
    }

    @Override
    public String getRestaurantTemplate() {
        return readResourceFile("restaurant-template-v301.csv");
    }

    @Override
    public String generateWorkflow(UserExporter exporter, String restaurantTemplate, Map<String,
            DetailsPost> details) {
        return exporter.oneKitchenWorkflow(restaurantTemplate, details);
    }
}