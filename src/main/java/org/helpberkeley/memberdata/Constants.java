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
    static final String MEMBERDATA_WITH_EMAIL_REPORT_FILE = "member-data-with-email";
    static final String MEMBERDATA_RAW_FILE = "member-data-raw";
    static final String CONSUMER_REQUESTS_FILE = "consumer-requests";
    static final String VOLUNTEER_REQUESTS_FILE = "volunteer-requests";
    static final String DRIVERS_FILE = "drivers";
    static final String WORKFLOW_FILE = "workflow";
    static final String INREACH_FILE = "inreach";
    static final String DISPATCHERS_FILE = "dispatchers";
    static final String ORDER_HISTORY_FILE = "order-history";
    static final String DELIVERY_POSTS_FILE = "delivery-posts";
    static final String ALL_MEMBERS_TITLE = "All Members";

    //    public static final int QUERY_GET_USERS_ID = 1;
    public static final int QUERY_GET_GROUPS_ID = 3;
    public static final int QUERY_GET_GROUP_USERS_ID = 4;
//    public static final int QUERY_GET_USERS_V11_ID = 8;
//    public static final int QUERY_GET_USERS_V12_ID = 9;
//    public static final int QUERY_GET_USERS_V13_ID = 10;
//    public static final int QUERY_GET_USERS_V14_ID = 12;
//    public static final int QUERY_GET_USERS_V15_ID = 14;
//    public static final int QUERY_GET_USERS_V16_ID = 16;
    public static final int QUERY_GET_USERS_V17_ID = 23;
    public static final int CURRENT_USERS_QUERY = QUERY_GET_USERS_V17_ID;
    public static final int QUERY_GET_DAILY_DELIVERIES = 13;
    public static final int QUERY_EMAIL_CONFIRMATIONS = 15;
    public static final int QUERY_GET_EMAILS = 11;
    public static final int QUERY_GET_DELIVERY_DETAILS = 17;
    public static final int QUERY_GET_DRIVERS_POST_FORMAT_V1 = 19;
    public static final int QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V1 = 20;
    public static final int QUERY_GET_ALL_POSTS_IN_TOPICS = 21;
    public static final int QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY = 22;
    public static final int QUERY_GET_LAST_ROUTE_REQUEST_REPLY = 25;
    public static final int QUERY_GET_DRIVERS_POST_FORMAT_V12 = 26;
    public static final int QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V12 = 27;
    public static final int QUERY_GET_BACKUP_DRIVER_FORMAT_V12 = 28;
    public static final int QUERY_GET_DRIVERS_POST_FORMAT_V21 = 29;
    public static final int QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V21 = 30;
    public static final int QUERY_GET_DRIVERS_POST_FORMAT_V22 = 31;
    public static final int QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22 = 32;
    public static final int QUERY_GET_DRIVERS_POST_FORMAT_V23 = 33;
    public static final int QUERY_GET_RESTAURANT_TEMPLATES = 35;
    public static final int QUERY_GET_LAST_RESTAURANT_TEMPLATE_REPLY = 36;

    // Current spec version for these queries
    public static final int QUERY_GET_DRIVERS_POST_FORMAT = QUERY_GET_DRIVERS_POST_FORMAT_V23;
    public static final int QUERY_GET_GROUP_INSTRUCTIONS_FORMAT = QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22;
    public static final int QUERY_GET_BACKUP_DRIVER_FORMAT = QUERY_GET_BACKUP_DRIVER_FORMAT_V12;

    public static final Topic TOPIC_REQUEST_DRIVER_MESSAGES = new Topic("Request Driver Messages", 2504);
    public static final Topic TOPIC_REQUEST_DRIVER_ROUTES = new Topic("Request Driver Routes", 2844);
    public static final Topic TOPIC_RESTAURANT_TEMPLATES = new Topic("Post restaurant template", 1860);

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_STAGED = "staged";
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
    public static final String WORKFLOW_NEIGHBORHOOD_COLUMN = "Neighborhood";
    public static final String WORKFLOW_CITY_COLUMN = "City";
    public static final String WORKFLOW_ADDRESS_COLUMN = "Address";
    public static final String WORKFLOW_CONDO_COLUMN = "Condo";
    public static final String WORKFLOW_DETAILS_COLUMN = "Details";
    public static final String WORKFLOW_VEGGIE_COLUMN = "veggie";
    public static final String WORKFLOW_NORMAL_COLUMN = "normal";
    public static final String WORKFLOW_RESTAURANTS_COLUMN = "Restaurants";
    public static final String WORKFLOW_ORDERS_COLUMN = "#orders";

    public static final String WORKFLOW_NO_PICS = "no pics";

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

    // Control block values
    public static final String CONTROL_BLOCK_BEGIN = "ControlBegin";
    public static final String CONTROL_BLOCK_END = "ControlEnd";
    public static final String CONTROL_BLOCK_COMMENT = "Comment";
    public static final String CONTROL_BLOCK_OPS_MANAGER = "OpsManager(UserName|Phone)";
    public static final String CONTROL_BLOCK_SPLIT_RESTAURANT = "SplitRestaurant(Name|CleanupDriverUserName)";
    public static final String CONTROL_BLOCK_BACKUP_DRIVER = "BackupDriverUserName";
    public static final String CONTROL_BLOCK_VERSION = "Version";

    public static final String CONTROL_BLOCK_VALUE_DEFAULT_PREFIX = "ReplaceThisBy";
    public static final String CONTROL_BLOCK_VERSION_UNKNOWN = "0";
    public static final String CONTROL_BLOCK_VERSION_1 = "1";
    public static final String CONTROL_BLOCK_VERSION_2_0_0 = "2_0_0";

    public static final String CONTROL_BLOCK_CURRENT_VERSION = CONTROL_BLOCK_VERSION_2_0_0;


    public static final long UNKNOWN_USER_ID = 852;
    public static final String UNKNOWN_USER = "unknown-user";

    public static final String GMAPS_API_KEY_PROPERTY = "GMaps-Api-Key";
    public static final String MEMBERDATA_PROPERTIES = "memberdata.properties";
    static final String API_USER_PROPERTY = "Api-Username";
    static final String API_KEY_PROPERTY = "Api-Key";
}
