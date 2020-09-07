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


import com.opencsv.bean.CsvToBeanBuilder;

import java.io.StringReader;
import java.util.*;

public class RestaurantTemplateParser {

    static final String TEMPLATE_ERROR = "Restaurant Template Error: ";
    static final String ERROR_NO_DATA = "empty file";
    static final String MISSING_COLUMN_ERROR = "missing column: ";
    static final String MISSING_EMPTY_ROW = "did not find empty row at line ";
    static final String MISSING_VALUE_ERROR = "missing value from column ";
    static final String DUPLICATE_ROUTE_ERROR = " appears more than once in the route block";
    private static final String ROUTE_MARKER = " Route";

    private long lineNumber = 0;
    private final Iterator<RestaurantBean> iterator;
    private int version = Constants.CONTROL_BLOCK_VERSION_UNKNOWN;

    RestaurantTemplateParser(final String csvData) {

        // Normalize EOL
        String normalizedData = csvData.replaceAll("\\r\\n?", "\n");

        if (normalizedData.isEmpty()) {
            throwTemplateError(ERROR_NO_DATA);
        }

        auditColumns(normalizedData);

        List<RestaurantBean> restaurantBeans = new CsvToBeanBuilder<RestaurantBean>(new StringReader(normalizedData))
                .withType(RestaurantBean.class).build().parse();
        iterator = restaurantBeans.iterator();
    }

    /**
     * Return the bean representation of the next row.
     * Increments current line number
     * @return Next bean, or null if at end.
     */
    private RestaurantBean nextRow() {
        if (iterator.hasNext()) {
            lineNumber++;
            return iterator.next();
        }

        return null;
    }

    private void auditColumns(final String csvData) {

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
        String errors = "";
        for (String columnName : columnNames) {
            if (! set.contains(columnName)) {
                errors += MISSING_COLUMN_ERROR + columnName + '\n';
                numErrors++;
            }
        }

        if (numErrors == columnNames.size()) {
            throwTemplateError("All column names missing. Line 1 does not look like a header row");
        }

        if (! errors.isEmpty()) {
            throwTemplateError(errors);
        }
    }

    Map<String, Restaurant> restaurants() {
        Map<String, Restaurant> restaurants = new HashMap<>();

        RestaurantBean bean;

        while ((bean = nextRow()) != null) {

            if (isControlBlockBeginRow(bean)) {
                processControlBlock();
                continue;
            }

            if (version != Constants.CONTROL_BLOCK_CURRENT_VERSION) {
                throw new MemberDataException(
                        "Unsupported control block version: " + version
                                + "\nCurrent supported version is: " + Constants.CONTROL_BLOCK_CURRENT_VERSION + "\n");
            }

            if (isAddressBlockMarker(bean)) {
                processAddressBlock(restaurants);
            } else if (isRoute(bean)) {
                processRouteBlock(bean, restaurants);
            } else if (isEmptyRow(bean)) {
                // ignore
            } else if (isRouteBlockLabelRow(bean)) {
                // ignore
            } else {
                throwTemplateError(MISSING_EMPTY_ROW + lineNumber);
            }
        }

        return restaurants;
    }

    /**
     * The first row of a control block looks like:
     *     FALSE,FALSE,ControlBegin,,,,,,,,,,,,,
     * @param bean RestaurantBean representation of row
     * @return Whether or not the row is the beginning of a control block.
     */
    private boolean isControlBlockBeginRow(RestaurantBean bean) {

        String consumerValue = bean.getConsumer();
        String driverValue = bean.getDriver();
        String directive = bean.getControlBlockDirective();

        return (! Boolean.parseBoolean(consumerValue))
                && (! Boolean.parseBoolean(driverValue))
                && (directive.equals(Constants.CONTROL_BLOCK_BEGIN));
    }

    /**
     * The final row of a control block looks like:
     *     FALSE,FALSE,ControlEnd,,,,,,,,,,,,,
     * @param bean RestaurantBean representation of row
     * @return Whether or not the row is the end of a control block.
     */
    private boolean isControlBlockEndRow(RestaurantBean bean) {

        String consumerValue = bean.getConsumer();
        String driverValue = bean.getDriver();
        String directive = bean.getControlBlockDirective();

        return (! Boolean.parseBoolean(consumerValue))
                && (! Boolean.parseBoolean(driverValue))
                && (directive.equals(Constants.CONTROL_BLOCK_END));
    }

    /**
     * The label row at the beginning of the route section looks like:
     *     ,,,,,,,,,Pics,,,Emojis,Starting,Closing
     * @param bean RestaurantBean representation of row
     * @return Whether or not the row is the end of a control block.
     */
    private boolean isRouteBlockLabelRow(RestaurantBean bean) {

        return bean.getConsumer().isEmpty()
                && bean.getDriver().isEmpty()
                && bean.getRestaurant().isEmpty();
    }

    private void processControlBlock() {
        RestaurantBean bean;
        ControlBlock controlBlock = new ControlBlock();

        while ((bean = nextRow()) != null) {

            if (isControlBlockEndRow(bean)) {
                break;
            }

            if (! ignoreControlBlockRow(bean)) {
                controlBlock.processRow(bean, lineNumber);
            }
        }

        version = controlBlock.getVersion();

        if (version != Constants.CONTROL_BLOCK_CURRENT_VERSION) {
            throw new MemberDataException(
                    "Unsupported control block version: " + version
                        + "\nCurrent supported version is: " + Constants.CONTROL_BLOCK_CURRENT_VERSION + "\n");
        }
    }

    // FIX THIS, DS: move to ControlBlock.  Call by processRow
    private boolean ignoreControlBlockRow(RestaurantBean bean) {

        String directive = bean.getControlBlockDirective();

        if (directive.equals(Constants.CONTROL_BLOCK_COMMENT)) {
            return true;
        }

        return directive.isEmpty()
                && bean.getControlBlockKey().isEmpty()
                && bean.getControlBlockValue().isEmpty();
    }

    /**
     * Is the passed in row the start of an restaurant address block?
     * @param bean RestaurantBean row representation
     * @return Whether or not the row is an address block start marker.
     */
    private boolean isAddressBlockMarker(final RestaurantBean bean) {

        // An address block marker looks like:
        //    FALSE,TRUE,,,,,,,,,,,,,
        //
        return ((! Boolean.parseBoolean(bean.getConsumer()))
                && Boolean.parseBoolean(bean.getDriver())
                && bean.getRestaurant().isEmpty()
                && bean.getOrders().isEmpty()
                && bean.getDetails().isEmpty());
    }

    /**
     * Is the passed in row of a restaurant route entry?
     * @param bean RestaurantBean row representation
     * @return Whether or not the row is a restaurant route entry.
     */
    private boolean isRoute(final RestaurantBean bean) {
        //
        // A route entry looks like:
        //    Solano Route,,,,,,,,,,,Bopshop,,,5:10 PM
        //
        return bean.getRoute().endsWith(ROUTE_MARKER);
    }

    /**
     * Is the passed in row empty?
     * @param bean RestaurantBean row representation
     * @return Whether or not the row is empty
     */
    private boolean isEmptyRow(final RestaurantBean bean) {

        //
        // An empty row looks like:
        //    ,,,,,,,,,,,,,,
        //
        return bean.isEmpty();
    }

    private void processAddressBlock(Map<String, Restaurant> restaurants) {

        RestaurantBean bean;

        while ((bean = nextRow()) != null) {
            if (isEmptyRow(bean)) {
                return;
            }

            String name = bean.getRestaurant();

            if (name.isEmpty()) {
                throwMissingValue(bean.restaurantColumn());
            }
            // FIX THIS, DS: this will never fail.  We aren't populating the map yet.
            if (restaurants.containsKey(name)) {
                throw new MemberDataException(name + " repeats in the restaurant template at line " + lineNumber);
            }

        }
    }

    private void processRouteBlock(RestaurantBean bean, Map<String, Restaurant> restaurants) {

        do {
            if (isEmptyRow(bean)) {
                return;
            }

            String routeName = bean.getRoute();

            if (routeName.isEmpty()) {
                throwMissingValue(bean.routeColumn(), "route name");
            }
            if (! routeName.endsWith(ROUTE_MARKER)) {
                throw new MemberDataException("Line " + lineNumber
                        + " of the restaurant template does not look like a route");
            }

            String restaurantName = bean.getRestaurant();
            if (restaurantName.isEmpty()) {
                throwMissingValue(bean.restaurantColumn());
            }
            String startTime = bean.getStartTime();
            if (startTime.isEmpty()) {
                throwMissingValue(bean.startTimeColumn(), "start time");
            }

            String closingTime = bean.getClosingTime();
            if (closingTime.isEmpty()) {
                throwMissingValue(bean.closingTimeColumn(), "closing time");
            }
            String emoji = bean.getEmoji();
            if (emoji.isEmpty()) {
                throwMissingValue(bean.emojiColumn(), "emoji");
            }

            if (restaurants.containsKey(restaurantName)) {
                throwTemplateError(restaurantName + DUPLICATE_ROUTE_ERROR);
            }
            Restaurant restaurant = new Restaurant(restaurantName);
            restaurants.put(restaurantName, restaurant);
            restaurant.setRoute(routeName);
            restaurant.setStartTime(startTime);
            restaurant.setClosingTime(closingTime);
            restaurant.setEmoji(emoji);

            String noPics = bean.getNoPics();
            if (noPics.toLowerCase().equals(Constants.WORKFLOW_NO_PICS)) {
                restaurant.setNoPics();
            }
        } while ((bean = nextRow()) != null);
    }

    private void throwTemplateError(final String message) {
        throw new MemberDataException(TEMPLATE_ERROR + message);
    }

    private void throwMissingValue(final String columnName) {
        throwTemplateError(MISSING_VALUE_ERROR
                + columnName
                + ", line number "
                + lineNumber);
    }

    private void throwMissingValue(final String columnName, final String columnUse) {
        throwTemplateError("missing "
                + columnUse
                + " value from column "
                + columnName
                + ", line number "
                + lineNumber);
    }
}
