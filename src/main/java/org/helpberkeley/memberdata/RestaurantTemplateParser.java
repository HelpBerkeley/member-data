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
import java.util.Map;

public class RestaurantTemplateParser {

    static final String ERROR_NO_DATA = "empty restaurant template";
    private static final String ROUTE_MARKER = " Route";

    private final String csvData;
    private final CSVReaderHeaderAware csvReader;

    RestaurantTemplateParser(final String csvData) {
        // Normalize EOL
        this.csvData = csvData.replaceAll("\\r\\n?", "\n");

        if (this.csvData.isEmpty()) {
            throw new MemberDataException(ERROR_NO_DATA);
        }
        try {
            csvReader = new CSVReaderHeaderAware(new StringReader(csvData));
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    private void auditRestaurantColumns() {

        // FIX THIS, DS: implement
    }

    Map<String, Restaurant> restaurants() {
        Map<String, Restaurant> restaurants = new HashMap<>();

        Map<String, String> rowMap;

        try {
            while ((rowMap = csvReader.readMap()) != null) {
                if (isAddressBlockMarker(rowMap)) {
                    processAddressBlock(restaurants);
                } else if (isRoute(rowMap)) {
                    processRouteBlock(rowMap, restaurants);
                } else {
                    if (! isEmptyRow(rowMap)){
                        throw new MemberDataException(
                            "Did not find empty row at line " + csvReader.getLinesRead()
                            + " of the restaurant template");
                    }
                }
            }
        } catch (IOException | CsvValidationException ex) {
            throw new MemberDataException("Error at line " + csvReader.getLinesRead()
                    + " of the restaurant template", ex);
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
            assert ! name.isEmpty() : "missing restaurant name, line " +  csvReader.getLinesRead();
            assert (! restaurants.containsKey(name)) :
                    name + ", line " + csvReader.getLinesRead() + ", has already been seen";
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
            assert routeName.endsWith(ROUTE_MARKER) :
                    routeName + ", line " + csvReader.getLinesRead() + ", does not look like a route";
            String restaurantName = rowMap.get(Constants.WORKFLOW_RESTAURANTS_COLUMN);
            assert ! restaurantName.isEmpty() :
                    routeName + ", line " + csvReader.getLinesRead() + ", missing restaurant name";
            String startTime = rowMap.get(Constants.WORKFLOW_ORDERS_COLUMN);
            assert ! startTime.isEmpty() :
                    routeName + ", line " + csvReader.getLinesRead() + ", missing start time";


            assert ! restaurants.containsKey(restaurantName) : restaurantName + " appears twice in route block";
            Restaurant restaurant = new Restaurant(restaurantName);
            restaurants.put(restaurantName, restaurant);
            restaurant.setRoute(routeName);
            restaurant.setStartTime(startTime);
        } while ((rowMap = csvReader.readMap()) != null);
    }

    // FIX THIS, DS: add test and implement
    static void auditRestaurantColumns(final Map<String, String> row) {
        String errors = "";

//        List<String> requiredColumns = List.of(
//                DeliveryColumns.CONSUMER_COLUMN
//                DeliveryColumns.DRIVER_COLUMN
//                DeliveryColumns.ADDRESS_COLUMN,
//
//                DeliveryColumns.NAME_COLUMN
//                DeliveryColumns.NORMAL_COLUMN
//
//        if (! row.containsKey(DeliveryColumns.NAME_COLUMN)) {
//            errors += "missing "
//        }

    }
}
