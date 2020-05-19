//
// Copyright (c) 2020 helpberkeley.org
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
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
                fileData.append("User: @");
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

    String allMembersWithEmailReportToFile(final Map<Long, String> emailAddresses,
        final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, allMembersWithEmailReport(emailAddresses));
        return outputFileName;
    }

    String drivers() {
        StringBuilder rows = new StringBuilder();
        rows.append(driverHeaders());

        for (User user : tables.drivers()) {
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
            rows.append('\n');
        }

        return rows.toString();
    }

    String driverHeaders() {
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
                + '\n';
    }

    String driversToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, drivers());

        return outputFileName;
    }

    String workflow(final String restaurantBlock) throws IOException, CsvException {

        StringBuilder rows = new StringBuilder();

        if (restaurantBlock.isEmpty()) {
            rows.append(workflowHeaders());
        } else {
            auditWorkflowData(restaurantBlock);
            rows.append(restaurantBlock);
        }

        for (User user : tables.sortByConsumerThenDriverThenName()) {
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
            rows.append(escapeCommas(user.getAddress()));
            rows.append(separator);
            rows.append(user.isCondo());
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
    private void auditWorkflowData(final String workFlowData) throws IOException, CsvException {

        // Normalize EOL - FIX THIS, DS: doe CSVReader do this already?
        String csvData = workFlowData.replaceAll("\\r\\n?", "\n");
        String[] lines = csvData.split("\n");
        assert lines.length != 0 : "missing work flow data";

        CSVReader csvReader = new CSVReader(new StringReader(csvData));
        List<String[]> rows = csvReader.readAll();
        assert ! rows.isEmpty() : "missing work flow data";
        String[] headerColumns = rows.get(0);

        if (! workflowHeaders().equals(lines[0] + "\n")) {
            throw new Error("Header mistmatch: " + lines[0].toString() + " != " + workflowHeaders());
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

    String workflowToFile(final String restaurantBlock, final String fileName) throws IOException, CsvException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, workflow(restaurantBlock));

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
            + "Restaurants"
            + separator
            + "normal"
            + separator
            + "veggie"
            + separator
            + "#orders"
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

    String inreachToFile(final String fileName, OrderHistory orderHistory) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
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
}
