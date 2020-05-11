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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderHistory {
    private static final String ID_COLUMN = "ID";
    private static final String ORDERS_COLUMN = "Orders";
    private static final String FIRST_ORDER_DATE_COLUMN = "First Order Date";
    private static final String LAST_ORDER_DATE_COLUMN = "Last Order Date";

    private final Map<Long, Row> history = new HashMap<>();
    String historyThroughDate;

    OrderHistory(final String date) {
        historyThroughDate = date;
    }

    void add(final String id, int numOrders, final String firstOrderDate, final String lastOrderDate) {

        long userId = Long.parseLong(id);
        assert ! history.containsKey(userId) : id;
        history.put(userId, new Row(userId, numOrders, firstOrderDate, lastOrderDate));
    }

    void merge(String date, List<UserOrder> userOrders, List<User> users) {

        Tables tables = new Tables(users);
        Map<String, User> usersByUserName = tables.mapByUserName();

        for (UserOrder userOrder : userOrders) {

            User user = usersByUserName.get(userOrder.userName);
            if (user == null) {
                for (User theHardWay : users) {
                    String userOrderName = userOrder.name.trim().toLowerCase();
                    if (theHardWay.getName().trim().toLowerCase().equals(userOrderName)) {
                        user = theHardWay;
                        break;
                    }
                }
            }

            if (user == null) {
                throw new Error("Cannot find user for " + userOrder);
            }

            Row row = history.get(user.getId());
            if (row == null) {
                history.put(user.getId(), new Row(user.getId(), 1, date, date));
            } else {
                row.update(date);
            }
        }

        historyThroughDate = date;
    }

    String export() {
        StringBuilder output = new StringBuilder();

        output.append(csvHeader());

        // This first data row is special.  It encodes the historyThroughDate
        output.append(0);
        output.append(Constants.CSV_SEPARATOR);
        output.append(0);
        output.append(Constants.CSV_SEPARATOR);
        output.append("");
        output.append(Constants.CSV_SEPARATOR);
        output.append(historyThroughDate);
        output.append(Constants.CSV_SEPARATOR);
        output.append('\n');


        for (Row row : history.values()) {
            output.append(row.id);
            output.append(Constants.CSV_SEPARATOR);
            output.append(row.numOrders);
            output.append(Constants.CSV_SEPARATOR);
            output.append(row.firstOrderDate);
            output.append(Constants.CSV_SEPARATOR);
            output.append(row.lastOrderDate);
            output.append(Constants.CSV_SEPARATOR);
            output.append('\n');
        }

        return output.toString();
    }

    static String csvHeader() {
        return ID_COLUMN + Constants.CSV_SEPARATOR
                + ORDERS_COLUMN + Constants.CSV_SEPARATOR
                + FIRST_ORDER_DATE_COLUMN + Constants.CSV_SEPARATOR
                + LAST_ORDER_DATE_COLUMN + Constants.CSV_SEPARATOR
                + "\n";
    }

    private static class Row {
        private final long id;
        private int numOrders;
        private final String firstOrderDate;
        private String lastOrderDate;

        Row(long id, int numOrders, final String firstOrderDate, final String lastOrderDate) {
            this.id = id;
            this.numOrders = numOrders;
            this.firstOrderDate = firstOrderDate;
            this.lastOrderDate = lastOrderDate;
        }

        void update(final String date) {
            lastOrderDate = date;
            numOrders++;
        }
    }
}