//
// Copyright (c) 2020-2024 helpberkeley.org
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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    String errorsToFile(final String fileName) {

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
        try (StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> rows = new ArrayList<>();
            rows.add(User.rawCSVHeaders());

            for (User user : tables.consumerRequests()) {
                rows.add(user.rawToCSV());
            }

            csvWriter.writeAllToList(rows);
            return writer.toString();
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    String consumerRequestsToFile(final String fileName) {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, consumerRequests());

        return outputFileName;
    }

    String volunteerRequests() {
        try (StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> rows = new ArrayList<>();
            rows.add(User.rawCSVHeaders());

            for (User user : tables.volunteerRequests()) {
                rows.add(user.rawToCSV());
            }

            csvWriter.writeAllToList(rows);
            return writer.toString();
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    String volunteerRequestsToFile(final String fileName) {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, volunteerRequests());

        return outputFileName;
    }

    String allMembersRawToFile(final String fileName) {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, allMembersRaw());
        return outputFileName;
    }

    String allMembersRaw() {
        try (StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> dataToEncode = new ArrayList<>();
            dataToEncode.add(User.rawCSVHeaders());

            for (User user : tables.sortByUserName()) {
                dataToEncode.add(user.rawToCSV());
            }

            csvWriter.writeAllToList(dataToEncode);
            return writer.toString();
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    String allMembersReport() {
        try (StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> dataToEncode = new ArrayList<>();
            dataToEncode.add(User.reportCSVHeaders());

            for (User user : tables.sortByUserId()) {
                dataToEncode.add(user.reportToCSV());
            }

            csvWriter.writeAllToList(dataToEncode);
            return writer.toString();
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    String allMembersReportToFile(final String fileName) {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, allMembersReport());
        return outputFileName;
    }

    String allMembersWithEmailReport(final Map<Long, String> emailAddresses) {
        try (StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> dataToEncode = new ArrayList<>();
            dataToEncode.add(User.reportWithEmailCSVHeaders());

            for (User user : tables.sortByUserId()) {
                String emailAddress = emailAddresses.getOrDefault(user.getId(), "");
                dataToEncode.add(user.reportWithEMailToCSV(emailAddress));
            }

            csvWriter.writeAllToList(dataToEncode);
            return writer.toString();
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    void allMembersWithEmailReportToFile(final Map<Long, String> emailAddresses) {

        String outputFileName = generateFileName(Constants.MEMBERDATA_WITH_EMAIL_REPORT_FILE, "csv");
        writeFile(outputFileName, allMembersWithEmailReport(emailAddresses));
    }

    public String workflow(final String restaurantBlock,
        Map<String, DetailsPost> deliveryDetails) {

        try (StringWriter writer = new StringWriter()) {

            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> dataToEncode = new ArrayList<>();

            if (restaurantBlock.isEmpty()) {
                csvWriter.writeNextToList(workflowHeaders());
            }
            else {
                auditWorkflowData(restaurantBlock, workflowHeaders());
                List<List<String>> parsedRestaurantBlock;
                try (StringReader reader = new StringReader(restaurantBlock)) {
                    CSVListReader csvReader = new CSVListReader(reader);
                    parsedRestaurantBlock = csvReader.readAllToList();
                }
                csvWriter.writeAllToList(parsedRestaurantBlock);
            }

            for (User user : tables.sortByConsumerThenDriverThenName()) {

                DetailsPost details = deliveryDetails.get(user.getUserName());

                List<String> row = new ArrayList<>(List.of(user.isConsumer().toString(),
                        user.isDriver().toString(),
                        user.getName(),
                        user.getUserName(),
                        user.getPhoneNumber(),
                        user.getAltPhoneNumber(),
                        user.getNeighborhood(),
                        user.getCity(),
                        user.getFullAddress(),
                        user.isCondo().toString(),
                        details == null ? "" : details.getDetails(),
                        "",
                        "",
                        "",
                        ""));
                dataToEncode.add(row);
            }

            csvWriter.writeAllToList(dataToEncode);
            return writer.toString();
        }
        catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    public String oneKitchenWorkflow(final String restaurantBlock,
                    Map<String, DetailsPost> deliveryDetails) {

        try(StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> dataToEncode = new ArrayList<>();

            if (restaurantBlock.isEmpty()) {
                csvWriter.writeNextToList(workflowHeaders());
            }
            else {
                auditWorkflowData(restaurantBlock, oneKitchenWorkflowHeaders());

                try (StringReader reader = new StringReader(restaurantBlock)) {
                    CSVListReader csvReader = new CSVListReader(new StringReader(restaurantBlock));
                    List<List<String>> parsedRestaurantBlock = csvReader.readAllToList();
                    csvWriter.writeAllToList(parsedRestaurantBlock);
                }
            }

            for (User user : tables.sortByConsumerThenDriverThenName()) {

                DetailsPost details = deliveryDetails.get(user.getUserName());

                List<String> row = new ArrayList<>(List.of(user.isConsumer().toString(),
                        user.isDriver().toString(),
                        user.getName(),
                        user.getUserName(),
                        user.getPhoneNumber(),
                        user.getAltPhoneNumber(),
                        user.getNeighborhood(),
                        user.getCity(),
                        user.getFullAddress(),
                        user.isCondo().toString(),
                        details == null ? "" : details.getDetails(),
                        "",
                        "",
                        "",
                        ""));
                dataToEncode.add(row);
            }

            csvWriter.writeAllToList(dataToEncode);
            return writer.toString();
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    // Audit that all of the columns are expected column names are present, in the expected order
    // and that all of the rows contain the same number of columns.
    //
    private void auditWorkflowData(String workflowData, List<String> headers) {

        List<List<String>> rows;

        try (StringReader stringReader = new StringReader(workflowData)) {
            CSVListReader csvReader = new CSVListReader(stringReader);
            rows = csvReader.readAllToList();
        }

        assert !rows.isEmpty() : "missing work flow data";
        List<String> headerColumns = rows.get(0);

        if (!headers.equals(headerColumns)) {
            throw new MemberDataException("Header mismatch: " + headerColumns + " != " + headers);
        }

        for (int row = 1; row < rows.size(); row++) {

            List<String> columns = rows.get(row);

            if (columns.size() != headerColumns.size()) {
                throw new Error("wrong number of columns in line "
                        + row + 1
                        + " ("
                        + columns.size()
                        + " != "
                        + headerColumns.size()
                        + ")");
            }
        }
    }

    String workflowToFile(final String restaurantBlock, Map<String, DetailsPost> deliveryDetails,
        final String fileName) {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, workflow(restaurantBlock, deliveryDetails));

        return outputFileName;
    }

    List<String> workflowHeaders() {

        return new ArrayList<>(List.of(User.CONSUMER_COLUMN,
                User.DRIVER_COLUMN,
                User.NAME_COLUMN,
                User.USERNAME_COLUMN,
                User.PHONE_NUMBER_COLUMN,
                User.ALT_PHONE_NUMBER_COLUMN,
                User.NEIGHBORHOOD_COLUMN,
                User.CITY_COLUMN,
                User.ADDRESS_COLUMN,
                User.CONDO_COLUMN,
                "Details",
                "Restaurants",
                "normal",
                "veggie",
                "#orders"));
    }

    String oneKitchenWorkflowToFile(final String restaurantBlock, Map<String, DetailsPost> deliveryDetails,
                          final String fileName) {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, oneKitchenWorkflow(restaurantBlock, deliveryDetails));

        return outputFileName;
    }

    List<String> oneKitchenWorkflowHeaders() {

        return new ArrayList<>(List.of(User.CONSUMER_COLUMN,
                User.DRIVER_COLUMN,
                User.NAME_COLUMN,
                User.USERNAME_COLUMN,
                User.PHONE_NUMBER_COLUMN,
                User.ALT_PHONE_NUMBER_COLUMN,
                User.NEIGHBORHOOD_COLUMN,
                User.CITY_COLUMN,
                User.ADDRESS_COLUMN,
                User.CONDO_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN,
                Constants.WORKFLOW_RESTAURANTS_COLUMN,
                Constants.WORKFLOW_STD_MEALS_COLUMN,
                Constants.WORKFLOW_ALT_MEALS_COLUMN,
                Constants.WORKFLOW_TYPE_MEAL_COLUMN,
                Constants.WORKFLOW_STD_GROCERY_COLUMN,
                Constants.WORKFLOW_ALT_GROCERY_COLUMN,
                Constants.WORKFLOW_TYPE_GROCERY_COLUMN));
    }

    String inreach(OrderHistory orderHistory) {

        try (StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> dataToEncode = new ArrayList<>();
            dataToEncode.add(inreachHeaders());

            for (User user : tables.inreach()) {
                OrderHistory.Row userOrderHistory = orderHistory.getRow(user.getId());

                List<String> row = new ArrayList<>(List.of(user.getSimpleCreateTime(),
                        user.getName(),
                        user.getUserName(),
                        user.getPhoneNumber(),
                        user.getAltPhoneNumber(),
                        user.getCity(),
                        user.getAddress(),
                        user.isCondo().toString(),
                        String.valueOf(userOrderHistory != null),
                        userOrderHistory != null ? String.valueOf(userOrderHistory.getNumOrders()) : "",
                        userOrderHistory != null ? userOrderHistory.getFirstOrderDate() : "",
                        userOrderHistory != null ? userOrderHistory.getLastOrderDate() : "",
                        user.isConsumer().toString(),
                        user.isDispatcher().toString(),
                        user.isDriver().toString()));
                dataToEncode.add(row);
            }

            csvWriter.writeAllToList(dataToEncode);
            return writer.toString();
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    String inreachToFile(OrderHistory orderHistory) {
        String outputFileName = generateFileName(Constants.INREACH_FILE, "csv");
        writeFile(outputFileName, inreach(orderHistory));
        return outputFileName;
    }

    List<String> inreachHeaders() {
        return new ArrayList<>(List.of(User.CREATED_AT_COLUMN,
                User.NAME_COLUMN,
                User.USERNAME_COLUMN,
                User.PHONE_NUMBER_COLUMN,
                User.ALT_PHONE_NUMBER_COLUMN,
                User.CITY_COLUMN,
                User.ADDRESS_COLUMN,
                User.CONDO_COLUMN,
                Constants.ORDER_STATUS_COLUMN,
                Constants.ORDER_NUMBER_COLUMN,
                Constants.FIRST_ORDER_DATE_COLUMN,
                Constants.LAST_ORDER_DATE_COLUMN,
                User.CONSUMER_COLUMN,
                User.DISPATCHER_COLUMN,
                User.DRIVER_COLUMN));
    }

    String dispatchers() {

        try (StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> dataToEncode = new ArrayList<>();
            dataToEncode.add(dispatchersHeaders());

            for (User user : tables.dispatchers()) {
                List<String> row = new ArrayList<>(List.of(user.getSimpleCreateTime(),
                        user.getName(),
                        user.getUserName(),
                        user.getPhoneNumber(),
                        user.getNeighborhood(),
                        user.getCity(),
                        user.getAddress(),
                        user.isCondo().toString(),
                        user.isDriver().toString(),
                        user.isConsumer().toString(),
                        user.isDispatcher().toString(),
                        user.isBHS().toString(),
                        user.isHelpLine().toString(),
                        user.isSiteLine().toString(),
                        user.isInReach().toString(),
                        user.isOutReach().toString(),
                        user.isMarketing().toString(),
                        user.isModerator().toString(),
                        user.isSpecialist().toString(),
                        user.isWorkflow().toString()));
                dataToEncode.add(row);
            }

            csvWriter.writeAllToList(dataToEncode);
            return writer.toString();
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    String dispatchersToFile(final String fileName) {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, dispatchers());

        return outputFileName;
    }

    List<String> dispatchersHeaders() {
        return new ArrayList<>(List.of(User.CREATED_AT_COLUMN,
                User.NAME_COLUMN,
                User.USERNAME_COLUMN,
                User.PHONE_NUMBER_COLUMN,
                User.NEIGHBORHOOD_COLUMN,
                User.CITY_COLUMN,
                User.ADDRESS_COLUMN,
                User.CONDO_COLUMN,
                User.DRIVER_COLUMN,
                User.CONSUMER_COLUMN,
                User.DISPATCHER_COLUMN,
                User.BHS_COLUMN,
                User.HELPLINE_COLUMN,
                User.SITELINE_COLUMN,
                User.INREACH_COLUMN,
                User.OUTREACH_COLUMN,
                User.MARKETING_COLUMN,
                User.MODERATORS_COLUMN,
                User.SPECIALIST_COLUMN,
                User.WORKFLOW_COLUMN));
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
            if (user.isFrreg()) {
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
