/*
 * Copyright (c) 2021-2024. helpberkeley.org
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

import com.opencsv.exceptions.CsvException;
import org.helpberkeley.memberdata.RestaurantTemplateParser;
import org.helpberkeley.memberdata.RestaurantTemplateTestBase;
import org.junit.Test;

import java.io.IOException;

import static org.helpberkeley.memberdata.v300.ControlBlockTest.EMPTY_ROW;

public class RestaurantTemplateTest extends RestaurantTemplateTestBase {

    @Override
    public String getEmptyRow() {
        return org.helpberkeley.memberdata.v300.ControlBlockTest.EMPTY_ROW;
    }

    @Override
    public String getRestaurantTemplate() {
        return readResourceFile("restaurant-template-v300.csv");
    }

    @Override
    public String getControlBlockDirectiveRow(String directive) {
        return EMPTY_ROW.replaceFirst(",,,", "FALSE,FALSE," + directive + ",");
    }

    @Override
    public int zeroOriginControlBlockEndLineNumber() {
        return 59;
    }

    @Test
    public void testTemplateValidation() throws IOException, CsvException {
        String template = readResourceFile("restaurant-template-v300.csv");
        RestaurantTemplateParser parser = RestaurantTemplateParser.create(template);
        parser.restaurants();
    }
}
