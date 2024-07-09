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

import java.util.*;

public class WorkflowExporter extends Exporter {

    WorkflowBean bean;
    private StringBuilder updateWarnings;
    private WorkflowParser parser;
    private List<WorkflowBean> updatedBeans;
    private int numMembers;

    public WorkflowExporter(WorkflowParser parser) {
        this.parser = parser;
        updatedBeans = new ArrayList<WorkflowBean>();
        updateWarnings = new StringBuilder("| UserName | Name | Phone | Phone 2 | Neighborhood | City | Address | Condo | Details |\n");
        updateWarnings.append("|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|\n");
        numMembers = 0;
    }

    public String getWarnings() { return updateWarnings.toString(); }

    public String updateMemberData(Map<String, User> users, Map<String, DetailsPost> deliveryDetails) {
        String errors = "";

        while ((bean = parser.nextRow()) != null) {
            if (parser.isMemberRow(bean)) {
                numMembers++;
                if (numMembers > (Constants.AVG_RUN_SIZE*10)) {
                    throw new MemberDataException("This is many more members [" + numMembers +
                            "] than the typical run, did you accidentally upload a sheet with all current members?");
                }

                User matchingUser = users.get(bean.getUserName());
                if (matchingUser == null) {
                    errors += "UserName " + bean.getUserName() + " at line " + parser.lineNumber +
                            " does not match any current members, please update to a current member.\n";
                } else {
                    updateUser(matchingUser, bean, deliveryDetails);
                }
            } else if (Boolean.parseBoolean(bean.getConsumer())) {
                throw new MemberDataException("Linenumber " + parser.lineNumber + " begins with TRUE TRUE. " +
                        "Is this a driver who is also a consumer? If so, the consumer column must be set to false.");
            }
            addBean(bean);
        }
        if (! errors.isEmpty()) {
            throw new MemberDataException(errors);
        }
        return updatedWorkflowToString();
    }

    private void updateUser(User user, WorkflowBean bean, Map<String, DetailsPost> deliveryDetails) {
        String warningString = "| " + user.getUserName() + " |";

        assert user.getUserName().equals(bean.getUserName()) : user.getUserName() + " != " + bean.getUserName();

        DetailsPost details = deliveryDetails.get(user.getUserName());

        String value = escapeCommas(user.getName());
        if (! value.equals(escapeCommas(bean.getName()))) {
            bean.setName(value);
            warningString += " Updated |";
        } else {
            warningString += " |";
        }
        value = user.getPhoneNumber();
        if (! value.equals(bean.getPhone())) {
            bean.setPhone(value);
            warningString += " Updated |";
        } else {
            warningString += " |";
        }
        value = user.getAltPhoneNumber();
        if (! value.equals(bean.getAltPhone())) {
            bean.setAltPhone(value);
            warningString += " Updated |";
        } else {
            warningString += " |";
        }
        value = escapeCommas(user.getNeighborhood());
        if (! value.equals(escapeCommas(bean.getNeighborhood()))) {
            bean.setNeighborhood(value);
            warningString += " Updated |";
        } else {
            warningString += " |";
        }
        value = escapeCommas(user.getCity());
        if (! value.equals(escapeCommas(bean.getCity()))) {
            bean.setCity(value);
            warningString += " Updated |";
        } else {
            warningString += " |";
        }
        value = escapeCommas(user.getAddress());
        if (! value.equals(escapeCommas(bean.getAddress()))) {
            bean.setAddress(value);
            warningString += " Updated |";
        } else {
            warningString += " |";
        }
        value = user.isCondo() ? "TRUE" : "FALSE";
        if (! value.equals(bean.getCondo())) {
            bean.setCondo(value);
            warningString += " Updated |";
        } else {
            warningString += " |";
        }
        value = details == null ? "" : escapeCommas(details.getDetails());
        if (! value.equals(escapeCommas(bean.getDetails()))) {
            bean.setDetails(value);
            warningString += " Updated |";
        } else {
            warningString += " |";
        }

        updateWarnings.append(warningString + "\n");
    }

    private void addBean(WorkflowBean bean) {
        updatedBeans.add(bean);
    }

    private String updatedWorkflowToString() {
        StringBuilder updatedData = new StringBuilder();
        for (WorkflowBean bean : updatedBeans) {
            updatedData.append(bean.toCSVString() + "\n");
        }

        return updatedData.toString();
    }
}
