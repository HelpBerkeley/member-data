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

import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class OrderHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderHistory.class);

    private static final String ID_COLUMN = "ID";
    private static final String ORDERS_COLUMN = "Orders";
    private static final String FIRST_ORDER_DATE_COLUMN = "First Order Date";
    private static final String LAST_ORDER_DATE_COLUMN = "Last Order Date";

    private final Map<Long, Row> history = new TreeMap<>();
    String historyThroughDate;

    OrderHistory(final String date) {
        historyThroughDate = date;
    }

    /**
     * Create an OrderHistory populated with the latest posted spreadsheet in the Order History topic
     *
     * @param apiClient Discourse API client handle
     * @return Current OrderHistory
     */
    public static OrderHistory getOrderHistory(ApiClient apiClient) {
        // Fetch the order history post
        String json = apiClient.getPost(Main.ORDER_HISTORY_POST_ID);

        // Parse the order history post
        String rawPost = HBParser.postBody(json);
        OrderHistoryPost orderHistoryPost = HBParser.orderHistoryPost(rawPost);

        // Download the order history file
        String orderHistoryCSV = apiClient.downloadFile(orderHistoryPost.uploadFile.getFileName());

        // Parse the order history data into an OrderHistory object
        return HBParser.orderHistory(orderHistoryCSV);
    }

    Row getRow(long userId) {
        return history.get(userId);
    }

    void add(final String id, int numOrders, final String firstOrderDate, final String lastOrderDate) {

        long userId = Long.parseLong(id);
        assert ! history.containsKey(userId) : id;
        history.put(userId, new Row(userId, numOrders, firstOrderDate, lastOrderDate));
    }

    /**
     * Merge the spreadsheets from the new order history data posts.
     * If any of the new posts contain older data, reprocess from the beginning.
     *
     * @param dataPosts Posts from the Order History Data topic
     * @param usersByUserName All members
     */
    void merge(OrderHistoryDataPosts dataPosts, Map<String, User> usersByUserName) throws IOException, CsvException {

        SortedMap<String, OrderHistoryData> newPosts = dataPosts.getNewPosts();

        // Nothing to merge
        if (newPosts.isEmpty()) {
            return;
        }

        if (historyThroughDate.compareTo(newPosts.firstKey()) < 0) {
            //merge
            doMerge(dataPosts.getApiClient(), newPosts, usersByUserName);
        } else {
            LOGGER.info("Full order history merge");
            // replace
            history.clear();
            doMerge(dataPosts.getApiClient(), dataPosts.getAllPosts(), usersByUserName);
        }
    }

    private void doMerge(ApiClient apiClient, Map<String, OrderHistoryData> postsToProcess,
                 Map<String, User> usersByUserName) throws IOException, CsvException {

        // If a reset of the order history has been done, we are going to download
        // all of the delivery files.  Avoid getting rate limited by Discourse, which
        // occurs when there are more than 60 requests per minute on a connection.
        long napTime = (postsToProcess.size() > 10) ? TimeUnit.SECONDS.toMillis(1) : 0;

        for (OrderHistoryData orderHistoryData : postsToProcess.values()) {
            LOGGER.debug("processing " + orderHistoryData);
            // Download the delivery file
            UploadFile uploadFile = orderHistoryData.getUploadFile();
            String deliveries = apiClient.downloadFile(uploadFile.getFileName());
            // Parse list of user restaurant orders
            List<UserOrder> userOrders = HBParser.parseOrders(uploadFile.getOriginalFileName(), deliveries);

            // Merge the data into the existing order history
            merge(orderHistoryData.getDate(), userOrders, usersByUserName);

            try {
                Thread.sleep(napTime);
            } catch (InterruptedException ignored) { }
        }
    }

    // FIX THIS, DS: make this private and rename to mergeOrders
    void merge(String date, List<UserOrder> userOrders, Map<String, User> usersByUserName) {

        for (UserOrder userOrder : userOrders) {

            User user = usersByUserName.get(userOrder.userName);
            if (user == null) {
                user = findUserTheHardWay(userOrder, usersByUserName);
            }

            if (user == null) {
                LOGGER.warn("Unknown user " + userOrder.userName + ": unknown user");
                user = usersByUserName.get(Constants.UNKNOWN_USER);
                assert user != null : "Could not find " + Constants.UNKNOWN_USER;
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

    private User findUserTheHardWay(UserOrder userOrder, Map<String, User> users) {

        // Iterate through users, checking by name, then phone, then alt phone

        String name = userOrder.name.trim().toLowerCase().replaceAll(" ", "");
        String phone = userOrder.phone.trim().toLowerCase().replaceAll(" ", "");
        String altPhone = userOrder.altPhone.trim().toLowerCase().replaceAll(" ", "");

        for (User theHardWay : users.values()) {
            if ((! name.isEmpty() && name.equals(
                    theHardWay.getName().trim().toLowerCase().replaceAll(" ", "")))) {
                return theHardWay;
            }
            String theHardWayPhone =
                    theHardWay.getPhoneNumber().trim().toLowerCase().replaceAll(" ", "");
            String theHardWayAltPhone =
                    theHardWay.getAltPhoneNumber().trim().toLowerCase().replaceAll(" ", "");

            if ((! phone.isEmpty()) &&
                    (phone.equals(theHardWayPhone) || phone.equals(theHardWayAltPhone))) {
                return theHardWay;
            }

            if ((! altPhone.isEmpty()) &&
                    (altPhone.equals(theHardWayPhone) || altPhone.equals(theHardWayAltPhone))) {
                return theHardWay;
            }
        }

        return null;
    }

    String export() {
        StringBuilder output = new StringBuilder();

        output.append(csvHeader());

        // This first data row is special.  It encodes the historyThroughDate
        output.append(0);
        output.append(Constants.CSV_SEPARATOR);
        output.append(0);
        output.append(Constants.CSV_SEPARATOR);
        output.append(Constants.CSV_SEPARATOR);
        output.append(historyThroughDate);
        output.append('\n');


        for (Row row : history.values()) {
            output.append(row.id);
            output.append(Constants.CSV_SEPARATOR);
            output.append(row.numOrders);
            output.append(Constants.CSV_SEPARATOR);
            output.append(row.firstOrderDate);
            output.append(Constants.CSV_SEPARATOR);
            output.append(row.lastOrderDate);
            output.append('\n');
        }

        return output.toString();
    }

    static String csvHeader() {
        return ID_COLUMN + Constants.CSV_SEPARATOR
                + ORDERS_COLUMN + Constants.CSV_SEPARATOR
                + FIRST_ORDER_DATE_COLUMN + Constants.CSV_SEPARATOR
                + LAST_ORDER_DATE_COLUMN
                + "\n";
    }

    static class Row {
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

        int getNumOrders() {
            return numOrders;
        }

        String getFirstOrderDate() {
            return firstOrderDate;
        }

        String getLastOrderDate() {
            return lastOrderDate;
        }
    }
}
