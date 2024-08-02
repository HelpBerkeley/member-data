/*
 * Copyright (c) 2020-2024. helpberkeley.org
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


import org.helpberkeley.memberdata.v200.RestaurantTemplateParserV200;
import org.helpberkeley.memberdata.v300.RestaurantTemplateParserV300;

import java.text.MessageFormat;
import java.util.*;

public abstract class RestaurantTemplateParser {

    static final String TEMPLATE_ERROR = "Restaurant Template Error: ";
    public static final String NO_FORMULA_ROWS_FOUND = TEMPLATE_ERROR + "\n" +
            "No Formula rows found within the Control Block. At least one valid Formula row is required.";
    public static final String MISSING_FORMULA_VALUE = "Invalid or missing Formula value found at line {0}." +
            " Formula values must begin with \"= or they will not import/export correctly.\n";
    static final String ERROR_NO_DATA = "empty file";
    static final String ERROR_MISSING_OR_UNSUPPORTED_VERSION = "Missing or unsupported version: ";
    public static final String MISSING_COLUMN_ERROR = "missing column: ";
    static final String MISSING_EMPTY_ROW = "did not find empty row at line ";
    static final String MISSING_VALUE_ERROR = "missing value from column ";
    static final String DUPLICATE_ROUTE_ERROR = " appears more than once in the route block";
    private static final String ROUTE_MARKER = " Route";

    // Keep the line numbers in sync with the restaurant template.
    // CsvToBeanBuilder swallows the header row and nextRow() pre-increments line number
    private int lineNumber = 1;
    private final Iterator<RestaurantBean> iterator;
    private String version = Constants.CONTROL_BLOCK_VERSION_UNKNOWN;
    private final ControlBlock controlBlock;
    private boolean hasFormulaDirective = false;
    private boolean validateFormulas = false;


    protected RestaurantTemplateParser(ControlBlock controlBlock, String csvData) {

        this.controlBlock = controlBlock;
        auditColumns(csvData);

        List<RestaurantBean> restaurantBeans = parse(csvData);
        iterator = restaurantBeans.iterator();
    }

    public static RestaurantTemplateParser create(String csvData) {

        // Normalize EOL
        String normalizedCSV = csvData.replaceAll("\\r\\n?", "\n");
        ControlBlock controlBlock;

        if (csvData.isEmpty()) {
            throw new MemberDataException(TEMPLATE_ERROR + ERROR_NO_DATA);
        }

        try {
            controlBlock = ControlBlock.create(normalizedCSV);
        } catch (MemberDataException ex) {
            throw new MemberDataException(TEMPLATE_ERROR + "\n" + ex.getMessage());
        }

        String version = controlBlock.getVersion();
        if (version.equals(Constants.CONTROL_BLOCK_VERSION_UNKNOWN)) {
            throw new MemberDataException("Restaurant template is missing the control block");
        } else if (controlBlock.versionIsCompatible(Constants.CONTROL_BLOCK_VERSION_200)) {
            return new RestaurantTemplateParserV200(controlBlock, normalizedCSV);
        } else if (controlBlock.versionIsCompatible(Constants.CONTROL_BLOCK_VERSION_300)) {
            return new RestaurantTemplateParserV300(controlBlock, normalizedCSV);
        } else {
            throw new MemberDataException(MessageFormat.format(
                    ControlBlock.UNSUPPORTED_VERSION_FOR, version, "restaurant templates."));
        }
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

    protected abstract void auditColumns(String csvData);
    protected abstract List<RestaurantBean> parse(String csvData);
    protected abstract boolean isAddressBlockMarker(RestaurantBean bean);

    public Map<String, Restaurant> restaurantsWithFormulaAudit() {
        this.validateFormulas = true;
        return restaurants();
    }

    public Map<String, Restaurant> restaurants() {
        Map<String, Restaurant> restaurants = new HashMap<>();

        RestaurantBean bean;

        while ((bean = nextRow()) != null) {

            if (isControlBlockBeginRow(bean)) {
                processControlBlock();
                continue;
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

    public String getVersion() {
        return version;
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
        String errors = "";
        RestaurantBean bean;

        while ((bean = nextRow()) != null) {

            errors += auditControlBlockRow(bean);

            if (isControlBlockEndRow(bean)) {
                break;
            }

            if (! ignoreControlBlockRow(bean)) {
                controlBlock.processRow(bean, lineNumber);
            }
        }

        if (validateFormulas && (! hasFormulaDirective)) {
            throw new MemberDataException(NO_FORMULA_ROWS_FOUND);
        }

        version = controlBlock.getVersion();

        if ((!controlBlock.versionIsCompatible(Constants.CONTROL_BLOCK_VERSION_200))
                && (!controlBlock.versionIsCompatible(Constants.CONTROL_BLOCK_VERSION_300))
                && (!version.equals(Constants.CONTROL_BLOCK_VERSION_1))) {
            throw new MemberDataException(
                    TEMPLATE_ERROR + "\n" + ERROR_MISSING_OR_UNSUPPORTED_VERSION + "\n");
        }

        if (! errors.isEmpty()) {
            throwTemplateError(errors);
        }
    }

    private String auditControlBlockRow(RestaurantBean bean) {
        String errors = "";

        if (! bean.getConsumer().equalsIgnoreCase("false")) {
            errors += "Control block " + Constants.WORKFLOW_CONSUMER_COLUMN
                    + " column does not contain FALSE, at line " + lineNumber + ".\n";
        }
        if (! bean.getDriver().equalsIgnoreCase("false")) {
            errors += "Control block " + Constants.WORKFLOW_DRIVER_COLUMN
                    + " column does not contain FALSE, at line " + lineNumber + ".\n";
        }

        String directive = bean.getControlBlockDirective();

        switch (directive) {
            case Constants.CONTROL_BLOCK_FORMULA:
                hasFormulaDirective = true;
                if (validateFormulas) {
                    errors += auditControlBlockFormula(bean);
                }
                break;
            case "":
            case Constants.CONTROL_BLOCK_COMMENT:
            case Constants.CONTROL_BLOCK_END:
                break;
            default:
                errors += MessageFormat.format(ControlBlock.ERROR_UNKNOWN_DIRECTIVE, directive, lineNumber);
        }

        return errors;
    }

    private String auditControlBlockFormula(RestaurantBean bean) {
        String errors = "";
        boolean containsFormulas = false;
        List<String> formulas = bean.getFormulas();

        for (String s: formulas) {
            if (s.startsWith("=")) {
                containsFormulas = true;
            }
        }

        if (!containsFormulas) {
            errors += MessageFormat.format(MISSING_FORMULA_VALUE, lineNumber);
        }
        return errors;
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

            assert ! version.equals(Constants.CONTROL_BLOCK_VERSION_1);

            if (! Boolean.parseBoolean(bean.getActive())) {
                continue;
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
            Restaurant restaurant = Restaurant.createRestaurant(controlBlock, restaurantName, lineNumber);
            restaurants.put(restaurantName, restaurant);
            restaurant.setRoute(routeName);
            restaurant.setStartTime(startTime);
            restaurant.setClosingTime(closingTime);
            restaurant.setEmoji(emoji);

            restaurant.setVersionSpecificFields(bean);
        } while ((bean = nextRow()) != null);
    }

    protected void throwTemplateError(final String message) {
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
