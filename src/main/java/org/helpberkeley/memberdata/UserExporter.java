//
// Copyright (c) 2020-2021 helpberkeley.org
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.helpberkeley.memberdata;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.StringReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Exports various groups of full user records
 */
public class UserExporter extends Exporter {

    private final Tables tables;

    public UserExporter(List<User> users) {
        tables = new Tables(users);
    }

    String errorsToFile(final String fileName) throws IOException {

        StringBuilder fileData = new StringBuilder();

        for (User user : tables.sortByUserName()) {
            for (String error : user.getDataErrors()) {
                fileData.append("User: ");
                fileData.append(user.getUserName());
                fileData.append(": ");
                fileData.append(error);
                fileData.append('\n');
            }
        }

        String outputFileName = generateFileName(fileName, "txt");
        writeFile(outputFileName, fileData.toString());

        return outputFileName;
    }

    String consumerRequests() {

        StringBuilder rows = new StringBuilder();
        rows.append(User.rawCSVHeaders());

        for (User user : tables.consumerRequests()) {
            rows.append(user.rawToCSV());
        }

        return rows.toString();
    }

    String consumerRequestsToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, consumerRequests());

        return outputFileName;
    }

    String volunteerRequests() {

        StringBuilder rows = new StringBuilder();
        rows.append(User.rawCSVHeaders());

        for (User user : tables.volunteerRequests()) {
            rows.append(user.rawToCSV());
        }

        return rows.toString();
    }

    String volunteerRequestsToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, volunteerRequests());

        return outputFileName;
    }

    String allMembersRawToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, allMembersRaw());
        return outputFileName;
    }

    String allMembersRaw() {

        StringBuilder csvData = new StringBuilder();
        csvData.append(User.rawCSVHeaders());

        for (User user : tables.sortByUserName()) {
            csvData.append(user.rawToCSV());
        }

        return csvData.toString();
    }

    String allMembersReport() {

        StringBuilder csvData = new StringBuilder();
        csvData.append(User.reportCSVHeaders());

        for (User user : tables.sortByUserId()) {
            csvData.append(user.reportToCSV());
        }

        return csvData.toString();
    }

    String allMembersReportToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, allMembersReport());
        return outputFileName;
    }

    String allMembersWithEmailReport(final Map<Long, String> emailAddresses) {

        StringBuilder csvData = new StringBuilder();
        csvData.append(User.reportWithEmailCSVHeaders());

        for (User user : tables.sortByUserId()) {
            String emailAddress = emailAddresses.getOrDefault(user.getId(), "");
            csvData.append(user.reportWithEMailToCSV(emailAddress));
        }

        return csvData.toString();
    }

    void allMembersWithEmailReportToFile(final Map<Long, String> emailAddresses) throws IOException {

        String outputFileName = generateFileName(Constants.MEMBERDATA_WITH_EMAIL_REPORT_FILE, "csv");
        writeFile(outputFileName, allMembersWithEmailReport(emailAddresses));
    }

    String workflow(final String restaurantBlock,
        Map<String, DetailsPost> deliveryDetails) throws IOException, CsvException {

        StringBuilder rows = new StringBuilder();

        if (restaurantBlock.isEmpty()) {
            rows.append(workflowHeaders());
        } else {
            auditWorkflowData(restaurantBlock, workflowHeaders());
            rows.append(restaurantBlock);
        }

        for (User user : tables.sortByConsumerThenDriverThenName()) {

            DetailsPost details = deliveryDetails.get(user.getUserName());

            rows.append(user.isConsumer());
            rows.append(separator);
            rows.append(user.isDriver());
            rows.append(separator);
            rows.append(escapeCommas(user.getName()));
            rows.append(separator);
            rows.append(user.getUserName());
            rows.append(separator);
            rows.append(user.getPhoneNumber());
            rows.append(separator);
            rows.append(user.getAltPhoneNumber());
            rows.append(separator);
            rows.append(escapeCommas(user.getNeighborhood()));
            rows.append(separator);
            rows.append(escapeCommas(user.getCity()));
            rows.append(separator);
            rows.append(escapeCommas(user.getFullAddress()));
            rows.append(separator);
            rows.append(user.isCondo());
            rows.append(separator);
            rows.append(details == null ? "" : escapeCommas(details.getDetails()));
            rows.append(separator);
            rows.append(separator);
            rows.append(separator);
            rows.append(separator);
            rows.append('\n');
        }

        return rows.toString();
    }

    String oneKitchenWorkflow(final String restaurantBlock,
                    Map<String, DetailsPost> deliveryDetails) throws IOException, CsvException {

        StringBuilder rows = new StringBuilder();

        if (restaurantBlock.isEmpty()) {
            rows.append(workflowHeaders());
        } else {
            auditWorkflowData(restaurantBlock, oneKitchenWorkflowHeaders());
            rows.append(restaurantBlock);
        }

        for (User user : tables.sortByConsumerThenDriverThenName()) {

            DetailsPost details = deliveryDetails.get(user.getUserName());

            rows.append(user.isConsumer());
            rows.append(separator);
            rows.append(user.isDriver());
            rows.append(separator);
            rows.append(escapeCommas(user.getName()));
            rows.append(separator);
            rows.append(user.getUserName());
            rows.append(separator);
            rows.append(user.getPhoneNumber());
            rows.append(separator);
            rows.append(user.getAltPhoneNumber());
            rows.append(separator);
            rows.append(escapeCommas(user.getNeighborhood()));
            rows.append(separator);
            rows.append(escapeCommas(user.getCity()));
            rows.append(separator);
            rows.append(escapeCommas(user.getFullAddress()));
            rows.append(separator);
            rows.append(user.isCondo());
            rows.append(separator);
            rows.append(details == null ? "" : escapeCommas(details.getDetails()));
            rows.append(separator);
            rows.append(separator);
            rows.append(separator);
            rows.append(separator);
            rows.append('\n');
        }

        return rows.toString();
    }

    // Audit that all of the columns are expected column names are present, in the expected order
    // and that all of the rows contain the same number of columns.
    //
    private void auditWorkflowData(String workflowData, String headers) throws IOException, CsvException {

        // Normalize EOL - FIX THIS, DS: doe CSVReader do this already?
        String csvData = workflowData.replaceAll("\\r\\n?", "\n");
        String[] lines = csvData.split("\n");
        assert lines.length != 0 : "missing work flow data";

        CSVReader csvReader = new CSVReader(new StringReader(csvData));
        List<String[]> rows = csvReader.readAll();
        assert ! rows.isEmpty() : "missing work flow data";
        String[] headerColumns = rows.get(0);

        if (! headers.equals(lines[0] + "\n")) {
            throw new Error("Header mismatch: " + lines[0] + " != " + headers);
        }

        for (int row = 1; row < rows.size(); row++) {

            String[] columns = rows.get(row);

            if (columns.length != headerColumns.length) {
                throw new Error("wrong number of columns in line "
                        + row + 1
                        + " ("
                        + columns.length
                        + " != "
                        + headerColumns.length
                        + ")");
            }
        }
    }

    String workflowToFile(final String restaurantBlock, Map<String, DetailsPost> deliveryDetails,
        final String fileName) throws IOException, CsvException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, workflow(restaurantBlock, deliveryDetails));

        return outputFileName;
    }

    String workflowHeaders() {

        return User.CONSUMER_COLUMN
            + separator
            + User.DRIVER_COLUMN
            + separator
            + User.NAME_COLUMN
            + separator
            + User.USERNAME_COLUMN
            + separator
            + User.PHONE_NUMBER_COLUMN
            + separator
            + User.ALT_PHONE_NUMBER_COLUMN
            + separator
            + User.NEIGHBORHOOD_COLUMN
            + separator
            + User.CITY_COLUMN
            + separator
            + User.ADDRESS_COLUMN
            + separator
            + User.CONDO_COLUMN
            + separator
            + "Details"
            + separator
            + "Restaurants"
            + separator
            + "normal"
            + separator
            + "veggie"
            + separator
            + "#orders"
            + '\n';
    }

    String oneKitchenWorkflowToFile(final String restaurantBlock, Map<String, DetailsPost> deliveryDetails,
                          final String fileName) throws IOException, CsvException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, oneKitchenWorkflow(restaurantBlock, deliveryDetails));

        return outputFileName;
    }

    String oneKitchenWorkflowHeaders() {

        return User.CONSUMER_COLUMN
                + separator
                + User.DRIVER_COLUMN
                + separator
                + User.NAME_COLUMN
                + separator
                + User.USERNAME_COLUMN
                + separator
                + User.PHONE_NUMBER_COLUMN
                + separator
                + User.ALT_PHONE_NUMBER_COLUMN
                + separator
                + User.NEIGHBORHOOD_COLUMN
                + separator
                + User.CITY_COLUMN
                + separator
                + User.ADDRESS_COLUMN
                + separator
                + User.CONDO_COLUMN
                + separator
                + Constants.WORKFLOW_DETAILS_COLUMN
                + separator
                + Constants.WORKFLOW_RESTAURANTS_COLUMN
                + separator
                + Constants.WORKFLOW_STD_MEALS_COLUMN
                + separator
                + Constants.WORKFLOW_ALT_MEALS_COLUMN
                + separator
                + Constants.WORKFLOW_TYPE_MEAL_COLUMN
                + separator
                + Constants.WORKFLOW_STD_GROCERY_COLUMN
                + separator
                + Constants.WORKFLOW_ALT_GROCERY_COLUMN
                + separator
                + Constants.WORKFLOW_TYPE_GROCERY_COLUMN
                + '\n';
    }

    String inreach(OrderHistory orderHistory) {

        StringBuilder rows = new StringBuilder();

        rows.append(inreachHeaders());

        for (User user : tables.inreach()) {
            OrderHistory.Row userOrderHistory = orderHistory.getRow(user.getId());

            rows.append(user.getSimpleCreateTime());
            rows.append(separator);
            rows.append(escapeCommas(user.getName()));
            rows.append(separator);
            rows.append(user.getUserName());
            rows.append(separator);
            rows.append(user.getPhoneNumber());
            rows.append(separator);
            rows.append(user.getAltPhoneNumber());
            rows.append(separator);
            rows.append(escapeCommas(user.getCity()));
            rows.append(separator);
            rows.append(escapeCommas(user.getAddress()));
            rows.append(separator);
            rows.append(user.isCondo());
            rows.append(separator);
            rows.append(Boolean.valueOf(userOrderHistory != null));
            rows.append(separator);
            rows.append(userOrderHistory != null ? userOrderHistory.getNumOrders() : "");
            rows.append(separator);
            rows.append(userOrderHistory != null ? userOrderHistory.getFirstOrderDate() : "");
            rows.append(separator);
            rows.append(userOrderHistory != null ? userOrderHistory.getLastOrderDate() : "");
            rows.append(separator);
            rows.append(user.isConsumer());
            rows.append(separator);
            rows.append(user.isDispatcher());
            rows.append(separator);
            rows.append(user.isDriver());
            rows.append('\n');
        }

        return rows.toString();

    }

    String inreachToFile(OrderHistory orderHistory) throws IOException {
        String outputFileName = generateFileName(Constants.INREACH_FILE, "csv");
        writeFile(outputFileName, inreach(orderHistory));
        return outputFileName;
    }

    String inreachHeaders() {
        return User.CREATED_AT_COLUMN
            + separator
            + User.NAME_COLUMN
            + separator
            + User.USERNAME_COLUMN
            + separator
            + User.PHONE_NUMBER_COLUMN
            + separator
            + User.ALT_PHONE_NUMBER_COLUMN
            + separator
            + User.CITY_COLUMN
            + separator
            + User.ADDRESS_COLUMN
            + separator
            + User.CONDO_COLUMN
            + separator
            + Constants.ORDER_STATUS_COLUMN
            + separator
            + Constants.ORDER_NUMBER_COLUMN
            + separator
            + Constants.FIRST_ORDER_DATE_COLUMN
            + separator
            + Constants.LAST_ORDER_DATE_COLUMN
            + separator
            + User.CONSUMER_COLUMN
            + separator
            + User.DISPATCHER_COLUMN
            + separator
            + User.DRIVER_COLUMN
            + '\n';
    }

    String dispatchers() {

        StringBuilder rows = new StringBuilder();

        rows.append(dispatchersHeaders());

        for (User user : tables.dispatchers()) {
            rows.append(user.getSimpleCreateTime());
            rows.append(separator);
            rows.append(escapeCommas(user.getName()));
            rows.append(separator);
            rows.append(user.getUserName());
            rows.append(separator);
            rows.append(user.getPhoneNumber());
            rows.append(separator);
            rows.append(escapeCommas(user.getNeighborhood()));
            rows.append(separator);
            rows.append(escapeCommas(user.getCity()));
            rows.append(separator);
            rows.append(escapeCommas(user.getAddress()));
            rows.append(separator);
            rows.append(user.isCondo());
            rows.append(separator);
            rows.append(user.isDriver());
            rows.append(separator);
            rows.append(user.isConsumer());
            rows.append(separator);
            rows.append(user.isDispatcher());
            rows.append(separator);
            rows.append(user.isBHS());
            rows.append(separator);
            rows.append(user.isHelpLine());
            rows.append(separator);
            rows.append(user.isSiteLine());
            rows.append(separator);
            rows.append(user.isInReach());
            rows.append(separator);
            rows.append(user.isOutReach());
            rows.append(separator);
            rows.append(user.isMarketing());
            rows.append(separator);
            rows.append(user.isModerator());
            rows.append(separator);
            rows.append(user.isSpecialist());
            rows.append(separator);
            rows.append(user.isWorkflow());
            rows.append('\n');
        }

        return rows.toString();

    }

    String dispatchersToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, dispatchers());

        return outputFileName;
    }

    String dispatchersHeaders() {
        return User.CREATED_AT_COLUMN
                + separator
                + User.NAME_COLUMN
                + separator
                + User.USERNAME_COLUMN
                + separator
                + User.PHONE_NUMBER_COLUMN
                + separator
                + User.NEIGHBORHOOD_COLUMN
                + separator
                + User.CITY_COLUMN
                + separator
                + User.ADDRESS_COLUMN
                + separator
                + User.CONDO_COLUMN
                + separator
                + User.DRIVER_COLUMN
                + separator
                + User.CONSUMER_COLUMN
                + separator
                + User.DISPATCHER_COLUMN
                + separator
                + User.BHS_COLUMN
                + separator
                + User.HELPLINE_COLUMN
                + separator
                + User.SITELINE_COLUMN
                + separator
                + User.INREACH_COLUMN
                + separator
                + User.OUTREACH_COLUMN
                + separator
                + User.MARKETING_COLUMN
                + separator
                + User.MODERATORS_COLUMN
                + separator
                + User.SPECIALIST_COLUMN
                + separator
                + User.WORKFLOW_COLUMN
                + '\n';
    }

    String customerCareMemberDataPost() {

        StringBuilder output = new StringBuilder();

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

        output.append("Updated: ").append(timeStamp).append("\n");

        output.append("|UserName|Full Name|C|Phone|Alt. Phone|\n");
        output.append("|---|---|---|---|---|\n");

        for (User user : tables.sortByUserName()) {

            output.append(user.getUserName());
            output.append('|');
            output.append(user.getName());
            output.append('|');
            output.append(user.isConsumer() ? "Y" : "");
            output.append('|');
            output.append(user.getPhoneNumber());
            output.append('|');
            output.append(user.getAltPhoneNumber());
            output.append("|\n");
        }

        return output.toString();
    }

    String freegPost() {

        StringBuilder output = new StringBuilder();

        String timeStamp = ZonedDateTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss"));

        output.append("Updated: ").append(timeStamp).append("\n");

        output.append("|UserName|Full Name|Phone|Alt. Phone|\n");
        output.append("|---|---|---|---|\n");

        for (User user : tables.sortByUserName()) {

            if (user.isMondayFrreg() || user.isThursdayFrreg()) {
                output.append(user.getUserName());
                output.append('|');
                output.append(user.getName());
                output.append('|');
                output.append(user.getPhoneNumber());
                output.append('|');
                output.append(user.getAltPhoneNumber());
                output.append("|\n");
            }
        }

        return output.toString();
    }
}