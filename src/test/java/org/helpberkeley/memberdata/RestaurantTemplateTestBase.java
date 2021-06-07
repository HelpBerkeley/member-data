/*
 * Copyright (c) 2021. helpberkeley.org
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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public abstract class RestaurantTemplateTestBase extends TestBase {


    public abstract String getEmptyRow();
    public abstract String getRestaurantTemplate();

    @Test
    public void dataRowWithoutEnoughColumnsTest() {
        String emptyRow = getEmptyRow().substring(3);
        String restaurantTemplate = getRestaurantTemplate() + emptyRow;
        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(restaurantTemplate));
        assertThat(thrown).isInstanceOf(MemberDataException.class);

        String expectedMessage = "Error parsing CSV line: "
                + restaurantTemplate.split("\\n").length
                + ". ["
                + emptyRow.replace('\n', ']')
                + "\nNumber of data fields does not match number of headers.";
        assertThat(thrown).hasMessage(expectedMessage);
    }

    @Test
    public void dataRowWithTooManyColumnsTest() {
        String emptyRow = getEmptyRow();
        String restaurantTemplate = getRestaurantTemplate()
                + ",,," + emptyRow;
        Throwable thrown = catchThrowable(() -> RestaurantTemplateParser.create(restaurantTemplate));
        assertThat(thrown).isInstanceOf(MemberDataException.class);

        String expectedMessage = "Error parsing CSV line: "
                + restaurantTemplate.split("\\n").length
                + ". ["
                + ",,,"
                + emptyRow.replace('\n', ']')
                + "\nNumber of data fields does not match number of headers.";

        assertThat(thrown).hasMessage(expectedMessage);
    }
}