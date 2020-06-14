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

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantTemplateParser {

    static final String TEMPLATE_ERROR = "Restaurant Template Error: ";
    static final String ERROR_NO_DATA = "empty file";
    static final String CSV_PARSE_ERROR = "CSV parser error: ";
    static final String MISSING_COLUMN_ERROR = "missing column: ";
    static final String MISSING_EMPTY_ROW = "did not find empty row at line ";
    static final String MISSING_VALUE_ERROR = "missing value from column ";
    static final String DUPLICATE_ROUTE_ERROR = " appears more than once in the route block";
    private static final String ROUTE_MARKER = " Route";

    private final CSVReaderHeaderAware csvReader;

    RestaurantTemplateParser(final String csvData) {
        CSVReaderHeaderAware csvReaderTemp;

        // Normalize EOL
        String normalizedData = csvData.replaceAll("\\r\\n?", "\n");

        if (normalizedData.isEmpty()) {
            throwTemplateError(ERROR_NO_DATA);
        }
        try {
            csvReaderTemp = new CSVReaderHeaderAware(new StringReader(normalizedData));
        } catch (IOException ex) {
            csvReaderTemp = null;
            throwTemplateError(ex.getMessage());
        }
        csvReader = csvReaderTemp;
    }

    private void auditColumns(final Map<String, String> row) {

        String errors = "";

        for (String columnName : List.of(
            Constants.WORKFLOW_CONSUMER_COLUMN,
            Constants.WORKFLOW_DRIVER_COLUMN,
            Constants.WORKFLOW_RESTAURANTS_COLUMN,
            Constants.WORKFLOW_ORDERS_COLUMN,
            Constants.WORKFLOW_DETAILS_COLUMN)) {

            if (! row.containsKey(columnName)) {
                errors += MISSING_COLUMN_ERROR + columnName + '\n';
            }
        }

        if (! errors.isEmpty()) {
            throwTemplateError(errors);
        }
    }

    Map<String, Restaurant> restaurants() {
        Map<String, Restaurant> restaurants = new HashMap<>();

        Map<String, String> rowMap;
        boolean firstLine = true;

        try {
            while ((rowMap = csvReader.readMap()) != null) {

                if (firstLine) {
                    auditColumns(rowMap);
                    firstLine = false;
                }

                if (isAddressBlockMarker(rowMap)) {
                    processAddressBlock(restaurants);
                } else if (isRoute(rowMap)) {
                    processRouteBlock(rowMap, restaurants);
                } else {
                    if (! isEmptyRow(rowMap)){
                        throwTemplateError(MISSING_EMPTY_ROW + csvReader.getLinesRead());
                    }
                }
            }
        } catch (IOException | CsvValidationException ex) {
            throwTemplateError(CSV_PARSE_ERROR + ex.getMessage());
        }

        return restaurants;
    }

    /**
     * Is the passed in row the start of an restaurant address block?
     * @param rowMap Map of column names and values.
     * @return Whether or not the row is an address block start marker.
     */
    private boolean isAddressBlockMarker(final Map<String, String> rowMap) {

        // An address block marker looks like:
        //    FALSE,TRUE,,,,,,,,,,,,,
        //
        return ((! Boolean.parseBoolean(rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN)))
                && Boolean.parseBoolean(rowMap.get(Constants.WORKFLOW_DRIVER_COLUMN))
                && rowMap.get(Constants.WORKFLOW_RESTAURANTS_COLUMN).isEmpty()
                && rowMap.get(Constants.WORKFLOW_ORDERS_COLUMN).isEmpty()
                && rowMap.get(Constants.WORKFLOW_DETAILS_COLUMN).isEmpty());
    }

    /**
     * Is the passed in row of a restaurant route entry?
     * @param rowMap Map of column names and values.
     * @return Whether or not the row is a restaurant route entry.
     */
    private boolean isRoute(final Map<String, String> rowMap) {
        //
        // A route entry looks like:
        //    Solano Route,,,,,,,,,,,Bopshop,,,5:10 PM
        //
        return rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN).endsWith(ROUTE_MARKER);
    }

    /**
     * Is the passed in row empty?
     * @param rowMap Map of column names and values.
     * @return Whether or not the row is empty
     */
    private boolean isEmptyRow(final Map<String, String> rowMap) {

        // An empty row looks like:
        //    ,,,,,,,,,,,,,,
        //
        return (rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN).isEmpty()
                && rowMap.get(Constants.WORKFLOW_DRIVER_COLUMN).isEmpty()
                && rowMap.get(Constants.WORKFLOW_RESTAURANTS_COLUMN).isEmpty()
                && rowMap.get(Constants.WORKFLOW_ORDERS_COLUMN).isEmpty()
                && rowMap.get(Constants.WORKFLOW_DETAILS_COLUMN).isEmpty());
    }

    private void processAddressBlock(Map<String, Restaurant> restaurants) throws IOException, CsvValidationException {

        Map<String, String> rowMap;

        while ((rowMap = csvReader.readMap()) != null) {
            if (isEmptyRow(rowMap)) {
                return;
            }

            String name = rowMap.get(Constants.WORKFLOW_RESTAURANTS_COLUMN);

            if (name.isEmpty()) {
                throwMissingValue(Constants.WORKFLOW_RESTAURANTS_COLUMN);
            }
            if (restaurants.containsKey(name)) {
                throw new MemberDataException(name + " repeats in the restaurant template at line "
                        + csvReader.getLinesRead());
            }
        }
    }

    private void processRouteBlock(
            Map<String, String> rowMap,
            Map<String, Restaurant> restaurants) throws IOException, CsvValidationException {

        // FIX THIS, DS: need to maintain restaurant route order

        do {
            if (isEmptyRow(rowMap)) {
                return;
            }

            String routeName = rowMap.get(Constants.WORKFLOW_CONSUMER_COLUMN);

            if (routeName.isEmpty()) {
                throwMissingValue(Constants.WORKFLOW_CONSUMER_COLUMN, "route name");
            }
            if (! routeName.endsWith(ROUTE_MARKER)) {
                throw new MemberDataException("Line "
                        + csvReader.getLinesRead()
                        + " of the restaurant template does not look like a route");
            }

            String restaurantName = rowMap.get(Constants.WORKFLOW_RESTAURANTS_COLUMN);
            if (restaurantName.isEmpty()) {
                throwMissingValue(Constants.WORKFLOW_RESTAURANTS_COLUMN);
            }
            String startTime = rowMap.get(Constants.WORKFLOW_ORDERS_COLUMN);
            if (startTime.isEmpty()) {
                throwMissingValue(Constants.WORKFLOW_RESTAURANTS_COLUMN, "start time");
            }

            if (restaurants.containsKey(restaurantName)) {
                throwTemplateError(restaurantName + DUPLICATE_ROUTE_ERROR);
            }
            Restaurant restaurant = new Restaurant(restaurantName);
            restaurants.put(restaurantName, restaurant);
            restaurant.setRoute(routeName);
            restaurant.setStartTime(startTime);
        } while ((rowMap = csvReader.readMap()) != null);
    }

    private void throwTemplateError(final String message) {
        throw new MemberDataException(TEMPLATE_ERROR + message);
    }

    private void throwMissingValue(final String columnName) {
        throwTemplateError(MISSING_VALUE_ERROR
                + columnName
                + ", line number "
                + csvReader.getLinesRead());
    }

    private void throwMissingValue(final String columnName, final String columnUse) {
        throwTemplateError("missing "
                + columnUse
                + " value from column "
                + columnName
                + ", line number "
                + csvReader.getLinesRead());
    }
}
