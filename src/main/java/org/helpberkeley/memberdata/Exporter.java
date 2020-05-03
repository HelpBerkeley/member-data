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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports various groups of full user records
 */
public class Exporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Exporter.class);

    private final Tables tables;
    private final String separator = Constants.CSV_SEPARATOR;

    public Exporter(List<User> users) {
        tables = new Tables(users);
    }

    private String generateFileName(String fileName, String suffix) {

        String timestamp =
                ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuMMdd-HHmm-ss"));
        return fileName + '-' + timestamp + '.' + suffix;
    }

    String getCSVSeparator() {
        return separator;
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
        LOGGER.debug("Wrote: " + outputFileName);

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
        LOGGER.debug("Wrote: " + outputFileName);

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
        LOGGER.debug("Wrote: " + outputFileName);

        return outputFileName;
    }

    String allMembersRawToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, allMembersRaw());
        LOGGER.debug("Wrote: " + outputFileName);
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

        for (User user : tables.sortByUserName()) {
            csvData.append(user.reportToCSV());
        }

        return csvData.toString();
    }

    String allMembersReportToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, allMembersReport());
        LOGGER.debug("Wrote: " + outputFileName);
        return outputFileName;
    }

    String drivers() {
        StringBuilder rows = new StringBuilder();
        rows.append(driverHeaders());

        for (User user : tables.drivers()) {
            rows.append(user.getSimpleCreateTime());
            rows.append(separator);
            rows.append(user.getName());
            rows.append(separator);
            rows.append(user.getUserName());
            rows.append(separator);
            rows.append(user.getPhoneNumber());
            rows.append(separator);
            rows.append(user.getNeighborhood());
            rows.append(separator);
            rows.append(user.getCity());
            rows.append(separator);
            rows.append(user.getAddress());
            rows.append(separator);
            rows.append(user.isApartment());
            rows.append(separator);
            rows.append(user.isDriver());
            rows.append(separator);
            rows.append(user.isConsumer());
            rows.append(separator);
            rows.append(user.isDispatcher());
            rows.append(separator);
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
                + User.APARTMENT_COLUMN
                + separator
                + User.DRIVER_COLUMN
                + separator
                + User.CONSUMER_COLUMN
                + separator
                + User.DISPATCHER_COLUMN
                + separator
                + '\n';
    }

    String driversToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, drivers());
        LOGGER.debug("Wrote: " + outputFileName);

        return outputFileName;
    }

    String workflow() {

        StringBuilder rows = new StringBuilder();

        rows.append(workflowHeaders());

        for (User user : tables.sortByConsumerThenDriverThenName()) {
            rows.append(user.isConsumer());
            rows.append(separator);
            rows.append(user.isDriver());
            rows.append(separator);
            rows.append(user.getName());
            rows.append(separator);
            rows.append(user.getUserName());
            rows.append(separator);
            rows.append(user.getPhoneNumber());
            rows.append(separator);
            rows.append(user.getAltPhoneNumber());
            rows.append(separator);
            rows.append(user.getNeighborhood());
            rows.append(separator);
            rows.append(user.getCity());
            rows.append(separator);
            rows.append(user.getAddress());
            rows.append(separator);
            rows.append(user.isApartment());
            rows.append('\n');
        }

        return rows.toString();
    }

    String workflowToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, workflow());
        LOGGER.debug("Wrote: " + outputFileName);

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
            + User.APARTMENT_COLUMN
            + separator
            + '\n';
    }

    String inreach() {

        StringBuilder rows = new StringBuilder();

        rows.append(inreachHeaders());

        for (User user : tables.inreach()) {
            rows.append(user.getSimpleCreateTime());
            rows.append(separator);
            rows.append(user.getName());
            rows.append(separator);
            rows.append(user.getUserName());
            rows.append(separator);
            rows.append(user.getPhoneNumber());
            rows.append(separator);
            rows.append(user.getAltPhoneNumber());
            rows.append(separator);
            rows.append(user.getCity());
            rows.append(separator);
            rows.append(user.getAddress());
            rows.append(separator);
            rows.append(user.isApartment());
            rows.append(separator);
            rows.append(user.isConsumer());
            rows.append(separator);
            rows.append(user.isDispatcher());
            rows.append(separator);
            rows.append(user.isDriver());
            rows.append(separator);
            rows.append('\n');
        }

        return rows.toString();

    }

    String inreachToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, inreach());
        LOGGER.debug("Wrote: " + outputFileName);

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
            + User.APARTMENT_COLUMN
            + separator
            + User.CONSUMER_COLUMN
            + separator
            + User.DISPATCHER_COLUMN
            + separator
            + User.DRIVER_COLUMN
            + separator
            + '\n';
    }

    String dispatchers() {

        StringBuilder rows = new StringBuilder();

        rows.append(dispatchersHeaders());

        for (User user : tables.dispatchers()) {
            rows.append(user.getSimpleCreateTime());
            rows.append(separator);
            rows.append(user.getName());
            rows.append(separator);
            rows.append(user.getUserName());
            rows.append(separator);
            rows.append(user.getPhoneNumber());
            rows.append(separator);
            rows.append(user.getNeighborhood());
            rows.append(separator);
            rows.append(user.getCity());
            rows.append(separator);
            rows.append(user.getAddress());
            rows.append(separator);
            rows.append(user.isApartment());
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
            rows.append(separator);
            rows.append('\n');
        }

        return rows.toString();

    }

    String dispatchersToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, dispatchers());
        LOGGER.debug("Wrote: " + outputFileName);

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
                + User.APARTMENT_COLUMN
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
                + separator
                + '\n';
    }

    private void writeFile(final String fileName, final String fileData) throws IOException {
        Path filePath = Paths.get(fileName);
        Files.deleteIfExists(filePath);
        Files.createFile(Paths.get(fileName));
        Files.writeString(filePath, fileData);

    }
}
