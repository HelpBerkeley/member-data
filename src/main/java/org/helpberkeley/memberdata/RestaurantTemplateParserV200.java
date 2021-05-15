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

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class RestaurantTemplateParserV200 extends RestaurantTemplateParser {

    RestaurantTemplateParserV200(ControlBlock controlBlock, String csvData) {
        super(controlBlock, csvData);
    }

    @Override
    protected List<RestaurantBean> parse(String csvData) {

        return new CsvToBeanBuilder<RestaurantBean>(
                new StringReader(csvData)).withType(RestaurantBeanV200.class).build().parse();
    }

    /**
     * Is the passed in row the start of an restaurant address block?
     * @param bean RestaurantBean row representation
     * @return Whether or not the row is an address block start marker.
     */
    @Override
    boolean isAddressBlockMarker(final RestaurantBean bean) {
        assert bean instanceof RestaurantBeanV200;

        // An address block marker looks like:
        //    FALSE,TRUE,,,,,,,,,,,,,
        //
        return ((! Boolean.parseBoolean(bean.getConsumer()))
                && Boolean.parseBoolean(bean.getDriver())
                && bean.getRestaurant().isEmpty()
                && ((RestaurantBeanV200)bean).getOrders().isEmpty()
                && bean.getDetails().isEmpty());
    }

    @Override
    protected void auditColumns(final String csvData) {

        List<String> columnNames =  List.of(
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_VEGGIE_COLUMN,
                Constants.WORKFLOW_NORMAL_COLUMN,
                Constants.WORKFLOW_ORDERS_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN,
                Constants.WORKFLOW_CONDO_COLUMN);

        // get the header line
        String header = csvData.substring(0, csvData.indexOf('\n'));

        String[] columns = header.split(",");
        Set<String> set = new HashSet<>(Arrays.asList(columns));

        int numErrors = 0;
        StringBuilder errors = new StringBuilder();
        for (String columnName : columnNames) {
            if (! set.contains(columnName)) {
                errors.append(MISSING_COLUMN_ERROR).append(columnName).append('\n');
                numErrors++;
            }
        }

        if (errors.length() > 0) {
            throwTemplateError(errors.toString());
        }
    }
}
