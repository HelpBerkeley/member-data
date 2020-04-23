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

    void errorsToFile(final String fileName) throws IOException {

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
        LOGGER.debug("Fetched: " + outputFileName);
    }

    void recentlyCreatedNoGroupsToFile(final String fileName) throws IOException {

        StringBuilder fileData = new StringBuilder();
        fileData.append(User.csvHeaders(separator));

        Tables recent = new Tables(tables.recentlyCreated(3));

        for (User user : recent.memberOfNoGroups()) {
            fileData.append(user.getId());
            fileData.append(separator);
            fileData.append(user.getName());
            fileData.append(separator);
            fileData.append(user.getUserName());
            fileData.append(separator);
            fileData.append(user.getPhoneNumber());
            fileData.append(separator);
            fileData.append(user.getNeighborhood());
            fileData.append(separator);
            fileData.append(user.getCity());
            fileData.append(separator);
            fileData.append(user.getAddress());
            fileData.append(separator);
            fileData.append(user.isConsumer());
            fileData.append(separator);
            fileData.append(user.isDispatcher());
            fileData.append(separator);
            fileData.append(user.isDriver());
            fileData.append(separator);
            fileData.append(user.getCreateTime());
            fileData.append(separator);
            fileData.append(user.isApartment());
            fileData.append(separator);
            fileData.append(user.hasConsumerRequest());
            fileData.append(separator);
            fileData.append(user.getVolunteerRequest());
            fileData.append(separator);
            fileData.append(user.getEmail());
            fileData.append(separator);
            fileData.append('\n');
        }

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, fileData.toString());
        LOGGER.debug("Fetched: " + outputFileName);
    }

    void consumerRequests(final String fileName) throws IOException {

        StringBuilder fileData = new StringBuilder();
        fileData.append(User.csvHeaders(separator));

        for (User user : tables.consumerRequests()) {

            fileData.append(user.getId());
            fileData.append(separator);
            fileData.append(user.getName());
            fileData.append(separator);
            fileData.append(user.getUserName());
            fileData.append(separator);
            fileData.append(user.getPhoneNumber());
            fileData.append(separator);
            fileData.append(user.getNeighborhood());
            fileData.append(separator);
            fileData.append(user.getCity());
            fileData.append(separator);
            fileData.append(user.getAddress());
            fileData.append(separator);
            fileData.append(user.isConsumer());
            fileData.append(separator);
            fileData.append(user.isDispatcher());
            fileData.append(separator);
            fileData.append(user.isDriver());
            fileData.append(separator);
            fileData.append(user.getCreateTime());
            fileData.append(separator);
            fileData.append(user.isApartment());
            fileData.append(separator);
            fileData.append(user.hasConsumerRequest());
            fileData.append(separator);
            fileData.append(user.getVolunteerRequest());
            fileData.append(separator);
            fileData.append(user.isSpecialist());
            fileData.append(separator);
            fileData.append(user.getEmail());
            fileData.append(separator);
            fileData.append('\n');
        }

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, fileData.toString());
        LOGGER.debug("Fetched: " + outputFileName);
    }

    void volunteerRequests(final String fileName) throws IOException {

        StringBuilder fileData = new StringBuilder();
        fileData.append(User.csvHeaders(separator));

        for (User user : tables.volunteerRequests()) {

            fileData.append(user.getId());
            fileData.append(separator);
            fileData.append(user.getName());
            fileData.append(separator);
            fileData.append(user.getUserName());
            fileData.append(separator);
            fileData.append(user.getPhoneNumber());
            fileData.append(separator);
            fileData.append(user.getNeighborhood());
            fileData.append(separator);
            fileData.append(user.getCity());
            fileData.append(separator);
            fileData.append(user.getAddress());
            fileData.append(separator);
            fileData.append(user.isConsumer());
            fileData.append(separator);
            fileData.append(user.isDispatcher());
            fileData.append(separator);
            fileData.append(user.isDriver());
            fileData.append(separator);
            fileData.append(user.getCreateTime());
            fileData.append(separator);
            fileData.append(user.isApartment());
            fileData.append(separator);
            fileData.append(user.hasConsumerRequest());
            fileData.append(separator);
            fileData.append(user.getVolunteerRequest());
            fileData.append(separator);
            fileData.append(user.isSpecialist());
            fileData.append(separator);
            fileData.append(user.getEmail());
            fileData.append(separator);
            fileData.append('\n');
        }

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, fileData.toString());
        LOGGER.debug("Fetched: " + outputFileName);
    }

    void allMembersToFile(final String fileName) throws IOException {

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, allMembersToCSV());
        LOGGER.debug("Fetched: " + outputFileName);
    }

    String allMembersToCSV() {

        StringBuilder csvData = new StringBuilder();
        csvData.append(User.csvHeaders(separator));

        for (User user : tables.sortByUserName()) {
            csvData.append(user.getId());
            csvData.append(separator);
            csvData.append(user.getName());
            csvData.append(separator);
            csvData.append(user.getUserName());
            csvData.append(separator);
            csvData.append(user.getPhoneNumber());
            csvData.append(separator);
            csvData.append(user.getNeighborhood());
            csvData.append(separator);
            csvData.append(user.getCity());
            csvData.append(separator);
            csvData.append(user.getAddress());
            csvData.append(separator);
            csvData.append(user.isConsumer());
            csvData.append(separator);
            csvData.append(user.isDispatcher());
            csvData.append(separator);
            csvData.append(user.isDriver());
            csvData.append(separator);
            csvData.append(user.getCreateTime());
            csvData.append(separator);
            csvData.append(user.isApartment());
            csvData.append(separator);
            csvData.append(user.hasConsumerRequest());
            csvData.append(separator);
            csvData.append(user.getVolunteerRequest());
            csvData.append(separator);
            csvData.append(user.isSpecialist());
            csvData.append(separator);
            csvData.append(user.getEmail());
            csvData.append(separator);
            csvData.append('\n');
        }

        return csvData.toString();
    }

    void drivers(final String fileName) throws IOException {

        // FIX THIS, DS: define constant for separator
        final String separator = ",";

        StringBuilder fileData = new StringBuilder();
        // FIX THIS, DS: define constant for separator
        fileData.append(User.csvHeaders(separator));

        for (User user : tables.drivers()) {

            fileData.append(user.getId());
            fileData.append(separator);
            fileData.append(user.getName());
            fileData.append(separator);
            fileData.append(user.getUserName());
            fileData.append(separator);
            fileData.append(user.getPhoneNumber());
            fileData.append(separator);
            fileData.append(user.getNeighborhood());
            fileData.append(separator);
            fileData.append(user.getCity());
            fileData.append(separator);
            fileData.append(user.getAddress());
            fileData.append(separator);
            fileData.append(user.isConsumer());
            fileData.append(separator);
            fileData.append(user.isDispatcher());
            fileData.append(separator);
            fileData.append(user.isDriver());
            fileData.append(separator);
            fileData.append(user.getCreateTime());
            fileData.append(separator);
            fileData.append(user.isApartment());
            fileData.append(separator);
            fileData.append(user.hasConsumerRequest());
            fileData.append(separator);
            fileData.append(user.getVolunteerRequest());
            fileData.append(separator);
            fileData.append(user.isSpecialist());
            fileData.append(separator);
            fileData.append(user.getEmail());
            fileData.append(separator);
            fileData.append('\n');
        }

        String outputFileName = generateFileName(fileName, "csv");
        writeFile(outputFileName, fileData.toString());
        LOGGER.debug("Fetched: " + outputFileName);
    }

    private void writeFile(final String fileName, final String fileData) throws IOException {
        Path filePath = Paths.get(fileName);
        Files.deleteIfExists(filePath);
        Files.createFile(Paths.get(fileName));
        Files.writeString(filePath, fileData);

    }
}
