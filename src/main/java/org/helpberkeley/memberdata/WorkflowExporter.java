/*
 * Copyright (c) 2024 helpberkeley.org
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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.*;

public class WorkflowExporter extends Exporter {

    public static final String TOO_MANY_MEMBERS_ERROR = "This is many more members (at least {0}) than the typical run, " +
            "does this sheet contain more members than just the drivers and consumers for this run?\n";
    public static final String NO_MATCHING_MEMBER_ERROR = "UserName {0} at line {1}" +
            " does not match any current members, please update to a current member. Note that UserName is case-sensitive.\n";
    public static final String DRIVER_IS_CONSUMER_ERROR = "Line number {0} begins with TRUE TRUE. " +
            "Is this a driver who is also a consumer? If so, the consumer column must be set to false.\n";
    public static final String HEADER_MISMATCH = "Header mismatch:\n {0} \n {1}\n\n " +
            "If you don't see a mismatch, there may be scratch-work/data in columns to the right that don't have a name, please check and remove it.";

    private final StringBuilder updateWarnings = new StringBuilder();
    private final WorkflowParser parser;
    private final List<WorkflowBean> updatedBeans = new ArrayList<>();
    private final Set<String> updatedUsers = new HashSet<>();
    public static final int DEFAULT_MEMBER_LIMIT = Constants.AVG_RUN_SIZE*10;
    private static int memberLimit = DEFAULT_MEMBER_LIMIT;

    public WorkflowExporter(WorkflowParser parser) {
        this.parser = parser;
        updateWarnings.append("| ").append(Constants.WORKFLOW_USER_NAME_COLUMN).append(" | ").append(Constants.WORKFLOW_NAME_COLUMN)
                .append(" | ").append(Constants.WORKFLOW_PHONE_COLUMN).append(" | ").append(Constants.WORKFLOW_ALT_PHONE_COLUMN)
                .append(" | ").append(Constants.WORKFLOW_NEIGHBORHOOD_COLUMN).append(" | ").append(Constants.WORKFLOW_CITY_COLUMN)
                .append(" | ").append(Constants.WORKFLOW_ADDRESS_COLUMN).append(" | ").append(Constants.WORKFLOW_CONDO_COLUMN)
                .append(" | ").append(Constants.WORKFLOW_DETAILS_COLUMN).append(" |\n");
        updateWarnings.append("|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|\n");
    }

    public String getWarnings() { return updateWarnings.toString(); }

    public Set<String> getUpdatedUsers() { return updatedUsers; }

    public static int getMemberLimit() { return memberLimit; }

    public String updateMemberData(Map<String, User> users, Map<String, DetailsPost> deliveryDetails) {
        StringBuilder errors = new StringBuilder();
        WorkflowBean bean;
        int numMembers = 0;

        while ((bean = parser.nextRow()) != null) {
            if (parser.isMemberRow(bean)) {
                numMembers++;
                if (numMembers > getMemberLimit()) {
                    throw new MemberDataException(MessageFormat.format(TOO_MANY_MEMBERS_ERROR, numMembers));
                }

                User matchingUser = users.get(bean.getUserName());
                if (matchingUser == null) {
                    errors.append(MessageFormat.format(NO_MATCHING_MEMBER_ERROR, bean.getUserName(), parser.lineNumber));
                } else {
                    updateUser(matchingUser, bean, deliveryDetails);
                }
            } else if (Boolean.parseBoolean(bean.getConsumer())) {
                throw new MemberDataException(MessageFormat.format(DRIVER_IS_CONSUMER_ERROR, parser.lineNumber));
            }
            addBean(bean);
        }
        List<String> incomingHeader;
        try (StringReader reader = new StringReader(parser.csvData)) {
            CSVListReader csvReader = new CSVListReader(reader);
            incomingHeader = csvReader.readNextToList();
        }
        List<String> outgoingHeader = updatedBeans.get(0).getCSVHeader();
        if (! incomingHeader.equals(outgoingHeader)) {
            errors.append(MessageFormat.format(HEADER_MISMATCH, incomingHeader, outgoingHeader));
        }
        if (errors.length() != 0) {
            throw new MemberDataException(errors.toString());
        }
        return updatedWorkflowToString();
    }

    private void updateUser(User user, WorkflowBean bean, Map<String, DetailsPost> deliveryDetails) {
        StringBuilder warningString = new StringBuilder("| " + user.getUserName() + " |");

        assert user.getUserName().equals(bean.getUserName()) : user.getUserName() + " != " + bean.getUserName();

        DetailsPost details = deliveryDetails.get(user.getUserName());

        String value = user.getName();
        if (! value.equals(bean.getName())) {
            bean.setName(value);
            warningString.append(" Updated |");
        } else {
            warningString.append(" |");
        }
        value = user.getPhoneNumber();
        if (! value.equals(bean.getPhone())) {
            bean.setPhone(value);
            warningString.append(" Updated |");
        } else {
            warningString.append(" |");
        }
        value = user.getAltPhoneNumber();
        if (! value.equals(bean.getAltPhone())) {
            bean.setAltPhone(value);
            warningString.append(" Updated |");
        } else {
            warningString.append(" |");
        }
        value = user.getNeighborhood();
        if (! value.equals(bean.getNeighborhood())) {
            bean.setNeighborhood(value);
            warningString.append(" Updated |");
        } else {
            warningString.append(" |");
        }
        value = user.getCity();
        if (! value.equals(bean.getCity())) {
            bean.setCity(value);
            warningString.append(" Updated |");
        } else {
            warningString.append(" |");
        }
        value = user.getFullAddress();
        if (! value.equals(bean.getAddress())) {
            bean.setAddress(value);
            warningString.append(" Updated |");
        } else {
            warningString.append(" |");
        }
        value = user.isCondo() ? "TRUE" : "FALSE";
        if (! value.equals(bean.getCondo())) {
            bean.setCondo(value);
            warningString.append(" Updated |");
        } else {
            warningString.append(" |");
        }
        value = details == null ? "" : details.getDetails();
        if (! value.equals(bean.getDetails())) {
            bean.setDetails(value);
            warningString.append(" Updated |");
        } else {
            warningString.append(" |");
        }

        //drivers will be duplicated on the spreadsheet, so we ensure only 1 update warning per updated member
        if (warningString.toString().contains("Updated") && (! updatedUsers.contains(bean.getUserName()))) {
            updatedUsers.add(bean.getUserName());
            updateWarnings.append(warningString).append("\n");
        }
    }

    private void addBean(WorkflowBean bean) {
        updatedBeans.add(bean);
    }

    private String updatedWorkflowToString() {
        try (StringWriter writer = new StringWriter()) {
            CSVListWriter csvWriter = new CSVListWriter(writer);
            List<List<String>> updatedData = new ArrayList<>();

            // append header, since bean initializes to row 2
            updatedData.add(updatedBeans.get(0).getCSVHeader());
            for (WorkflowBean bean : updatedBeans) {
                updatedData.add(bean.toCSVListRow());
            }

            csvWriter.writeAllToList(updatedData);
            return writer.toString();
        } catch (IOException ex) {
            throw new MemberDataException(ex);
        }
    }

    public static void setMemberLimit(int limit) {
        memberLimit = limit;
    }
}

