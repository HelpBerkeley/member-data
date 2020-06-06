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

public class Constants {
//    public static final int QUERY_GET_USERS_ID = 1;
    public static final int QUERY_GET_GROUPS_ID = 3;
    public static final int QUERY_GET_GROUP_USERS_ID = 4;
//    public static final int QUERY_GET_USERS_V11_ID = 8;
//    public static final int QUERY_GET_USERS_V12_ID = 9;
//    public static final int QUERY_GET_USERS_V13_ID = 10;
//    public static final int QUERY_GET_USERS_V14_ID = 12;
//    public static final int QUERY_GET_USERS_V15_ID = 14;
    public static final int QUERY_GET_USERS_V16_ID = 16;
    public static final int CURRENT_USERS_QUERY = QUERY_GET_USERS_V16_ID;
    public static final int QUERY_GET_DAILY_DELIVERIES = 13;
    public static final int QUERY_EMAIL_CONFIRMATIONS = 15;
    public static final int QUERY_GET_EMAILS = 11;
    public static final int QUERY_GET_DELIVERY_DETAILS = 17;
    public static final int QUERY_GET_DRIVERS_POST_FORMAT = 19;
    public static final int QUERY_GET_GROUP_INSTRUCTIONS_FORMAT = 20;

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_NEIGHBORHOOD = "neighborhood";
    public static final String COLUMN_CONDO = "condo";
    public static final String COLUMN_CREATE_TIME = "created_at";
    public static final String COLUMN_CONSUMER_REQUEST = "consumer_request";
    public static final String COLUMN_VOLUNTEER_REQUEST = "volunteer_request";
    public static final String COLUMN_ALT_PHONE = "alt_phone";
    public static final String COLUMN_REFERRAL = "referral";

    public static final String ORDER_STATUS_COLUMN = "OrderStatus";
    public static final String ORDER_NUMBER_COLUMN = "OrderNumber";
    public static final String FIRST_ORDER_DATE_COLUMN = "First order date";
    public static final String LAST_ORDER_DATE_COLUMN = "Last order date";

    public static final String WORKFLOW_CONSUMER_COLUMN = "Consumer";
    public static final String WORKFLOW_DRIVER_COLUMN = "Driver";
    public static final String WORKFLOW_NAME_COLUMN = "Name";
    public static final String WORKFLOW_USER_NAME_COLUMN = "User Name";
    public static final String WORKFLOW_PHONE_COLUMN = "Phone #";
    public static final String WORKFLOW_ALT_PHONE_COLUMN = "Phone2 #";
    public static final String WORKFLOW_CITY_COLUMN = "City";
    public static final String WORKFLOW_ADDRESS_COLUMN = "Address";
    public static final String WORKFLOW_CONDO_COLUMN = "Condo";
    public static final String WORKFLOW_DETAILS_COLUMN = "Details";
    public static final String WORKFLOW_VEGGIE_COLUMN = "veggie";
    public static final String WORKFLOW_NORMAL_COLUMN = "normal";
    public static final String WORKFLOW_RESTAURANTS_COLUMN = "Restaurants";
    public static final String WORKFLOW_ORDERS_COLUMN = "#orders";

    // Group names - FIX THIS, DS: hardwired
    public static final String GROUP_CONSUMERS = "consumers";
    public static final String GROUP_DRIVERS = "drivers";
    public static final String GROUP_DISPATCHERS = "dispatchers";
    public static final String GROUP_SPECIALISTS = "specialists";
    public static final String GROUP_BHS = "BHS";
    public static final String GROUP_HELPLINE = "helpline";
    public static final String GROUP_SITELINE = "siteline";
    public static final String GROUP_INREACH = "inreach";
    public static final String GROUP_OUTREACH = "outreach";
    public static final String GROUP_MARKETING = "marketing";
    public static final String GROUP_MODERATORS = "moderators";
    public static final String GROUP_WORKFLOW = "workflow";
    public static final String GROUP_VOICEONLY = "voiceonly";
    public static final String GROUP_TRUST_LEVEL_4 = "trust_level_4";
    public static final String GROUP_CUSTOMER_INFO = "customerinfo";
    public static final String GROUP_ADVISOR = "advisor";
    public static final String GROUP_COORDINATOR = "coordinator";
    public static final String GROUP_ADMIN = "admin";

    public static final String BERKELEY = "Berkeley";
    public static final String ALBANY = "Albany";
    public static final String KENSINGTON = "Kensington";

    public static final String CSV_SEPARATOR = ",";

    public static final String UPLOAD_URI_PREFIX = "upload://";
}
