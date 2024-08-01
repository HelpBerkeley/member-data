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
package org.helpberkeley.memberdata.v200;

import com.opencsv.exceptions.CsvException;
import org.helpberkeley.memberdata.DetailsPost;
import org.helpberkeley.memberdata.UserExporter;
import org.helpberkeley.memberdata.WorkflowExporterTestBase;

import java.io.IOException;
import java.util.Map;

public class WorkflowExporterTestV202 extends WorkflowExporterTestBase {

    @Override
    public String getResourceFile() throws IOException {
        return readResourceFile("update-member-data-v202.csv");
    }

    @Override
    public String getRestaurantTemplate() {
        return readResourceFile("restaurant-template-v202.csv");
    }

    @Override
    public String generateWorkflow(UserExporter exporter, String restaurantTemplate,
                   Map<String, DetailsPost> details) throws IOException, CsvException {
        return exporter.workflow(restaurantTemplate, details);
    }
}
