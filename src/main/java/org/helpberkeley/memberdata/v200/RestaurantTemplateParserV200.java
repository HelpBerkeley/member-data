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
package org.helpberkeley.memberdata.v200;

import com.opencsv.bean.CsvToBeanBuilder;
import org.helpberkeley.memberdata.*;

import java.io.StringReader;
import java.util.List;

public class RestaurantTemplateParserV200 extends RestaurantTemplateParser {

    public RestaurantTemplateParserV200(ControlBlock controlBlock, String csvData) {
        super(controlBlock, csvData);
    }

    @Override
    protected List<RestaurantBean> parse(String csvData) {

        try {
            return new CsvToBeanBuilder<RestaurantBean>(
                    new StringReader(csvData)).withType(RestaurantBeanV200.class).build().parse();
        } catch (RuntimeException ex) {
            throw new MemberDataException(ex);
        }
    }

    /**
     * Is the passed in row the start of an restaurant address block?
     * @param bean RestaurantBean row representation
     * @return Whether or not the row is an address block start marker.
     */
    @Override
    protected boolean isAddressBlockMarker(final RestaurantBean bean) {
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


        List<String> headerColumns;

        try (StringReader reader = new StringReader(csvData)) {
            CSVListReader csvListReader = new CSVListReader(reader);
            headerColumns = csvListReader.readNextToList();
        }

        int numErrors = 0;
        StringBuilder errors = new StringBuilder();
        for (String columnName : columnNames) {
            if (! headerColumns.contains(columnName)) {
                errors.append(MISSING_COLUMN_ERROR).append(columnName).append('\n');
                numErrors++;
            }
        }

        if (errors.length() > 0) {
            throwTemplateError(errors.toString());
        }
    }
}
