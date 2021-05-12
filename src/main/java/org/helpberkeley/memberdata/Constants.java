//
// Copyright (c) 2020-2021 helpberkeley.org
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

import java.time.ZoneId;

public class Constants {
    static final ZoneId TIMEZONE = ZoneId.of("America/Los_Angeles");
    static final String MEMBERDATA_WITH_EMAIL_REPORT_FILE = "member-data-with-email";
    static final String MEMBERDATA_RAW_FILE = "member-data-raw";
    static final String CONSUMER_REQUESTS_FILE = "consumer-requests";
    static final String VOLUNTEER_REQUESTS_FILE = "volunteer-requests";
    static final String DRIVERS_FILE = "drivers";
    static final String WORKFLOW_FILE = "workflow";
    static final String INREACH_FILE = "inreach";
    static final String DISPATCHERS_FILE = "dispatchers";
    static final String ORDER_HISTORY_FILE = "order-history";
    static final String DRIVER_HISTORY_FILE = "driver-history";
    static final String ALL_MEMBERS_TITLE = "All Members";

    public static final int QUERY_GET_GROUPS_ID = 3;

    public static final int QUERY_GET_GROUP_USERS_V2 = 47;
    public static final int CURRENT_GET_GROUP_USERS_QUERY = QUERY_GET_GROUP_USERS_V2;

    public static final int QUERY_GET_USERS_V17_ID = 23;
    public static final int CURRENT_USERS_QUERY = QUERY_GET_USERS_V17_ID;

    public static final int QUERY_EMAIL_CONFIRMATIONS_V1 = 15;
    public static final int QUERY_GET_EMAILS = 11;
    public static final int QUERY_GET_DELIVERY_DETAILS = 17;
    public static final int QUERY_GET_LAST_REQUEST_DRIVER_MESSAGES_REPLY = 22;
    public static final int QUERY_GET_LAST_ROUTE_REQUEST_REPLY = 25;
    public static final int QUERY_GET_BACKUP_DRIVER_FORMAT_V12 = 28;
    public static final int QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22 = 32;
    public static final int QUERY_GET_DRIVERS_POST_FORMAT_V23 = 33;
    public static final int QUERY_GET_LAST_RESTAURANT_TEMPLATE_REPLY = 36;
    public static final int QUERY_EMAIL_CONFIRMATIONS_V12 = 37;
    public static final int QUERY_GET_LAST_REQUEST_ONE_KITCHEN_DRIVER_MESSAGES_REPLY = 41;
    public static final int QUERY_GET_ONE_KITCHEN_DRIVERS_POST_FORMAT_V1 = 42;
    public static final int QUERY_GET_ONE_KITCHEN_GROUP_POST_FORMAT_V1 = 43;
    public static final int QUERY_GET_LAST_COMPLETED_DAILY_ORDERS_REPLY = 44;
    public static final int QUERY_GET_ORDER_HISTORY_DATA_POSTS = 45;
    public static final int QUERY_GET_DRIVER_DETAILS = 46;
    public static final int QUERY_GET_CURRENT_VALIDATED_RESTAURANT_TEMPLATE = 49;
    public static final int QUERY_GET_LAST_REPLY_FROM_REQUEST_TOPICS = 50;

    // Current spec version for these queries
    public static final int QUERY_GET_DRIVERS_POST_FORMAT = QUERY_GET_DRIVERS_POST_FORMAT_V23;
    public static final int QUERY_GET_GROUP_INSTRUCTIONS_FORMAT = QUERY_GET_GROUP_INSTRUCTIONS_FORMAT_V22;
    public static final int QUERY_GET_BACKUP_DRIVER_FORMAT = QUERY_GET_BACKUP_DRIVER_FORMAT_V12;
    public static final int QUERY_EMAIL_CONFIRMATIONS = QUERY_EMAIL_CONFIRMATIONS_V1;

    public static final Topic TOPIC_REQUEST_DRIVER_MESSAGES = new Topic("Request Driver Messages", 2504);
    public static final Topic TOPIC_REQUEST_DRIVER_ROUTES = new Topic("Request Driver Routes", 2844);
    public static final Topic TOPIC_POST_RESTAURANT_TEMPLATE = new Topic("Post restaurant template", 1860);
    public static final Topic TOPIC_REQUEST_SINGLE_RESTAURANT_DRIVER_MESSAGES =
            new Topic("Request Single Restaurant Driver Messages", 4878);
    public static final Topic TOPIC_ORDER_HISTORY_DATA = new Topic("Order History Data", 5234);
    public static final Topic TOPIC_POST_COMPLETED_DAILY_ORDERS = new Topic("Post completed daily orders", 859);
    public static final Topic TOPIC_DRIVER_TRAINING_TABLE = new Topic("Post completed daily orders", 5355);
    public static final Topic TOPIC_RESTAURANT_TEMPLATE_STORAGE = new Topic("Restaurant Template Storage", 6055);

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
    public static final String COLUMN_GROUP_OWNER = "owner";
    public static final String COLUMN_DELIVERY_DATE = "delivery_date";

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
    public static final String GROUP_TRAINED_CUSTOMER_CARE_A = "trained_CCA";
    public static final String GROUP_TRAINED_CUSTOMER_CARE_B = "trained_CCB";
    public static final String GROUP_INREACH = "inreach";
    public static final String GROUP_OUTREACH = "outreach";
    public static final String GROUP_MARKETING = "marketing";
    public static final String GROUP_MODERATORS = "moderators";
    public static final String GROUP_WORKFLOW = "workflow";
    public static final String GROUP_VOICEONLY = "voiceonly";
    public static final String GROUP_TRUST_LEVEL_4 = "trust_level_4";
    public static final String GROUP_CUSTOMER_INFO = "customerinfo";
    public static final String GROUP_ADVISOR = "advisors";
    public static final String GROUP_COORDINATORS = "coordinators";
    public static final String GROUP_ADMIN = "admin";
    public static final String GROUP_PACKERS = "HB-Packers";
    public static final String GROUP_BOARDMEMBERS = "boardmembers";
    public static final String GROUP_LIMITED = "limited";
    public static final String GROUP_AT_RISK = "at-risk";
    public static final String GROUP_BIKERS = "bikers";
    public static final String GROUP_OUT = "out";
    public static final String GROUP_TRAINED_DRIVERS = "trained_drivers";
    public static final String GROUP_EVENT_DRIVERS = "event_drivers";
    public static final String GROUP_TRAINED_EVENT_DRIVERS = "trained_edrivers";
    public static final String GROUP_GONE = "gone";
    public static final String GROUP_OTHER_DRIVERS = "other_drivers";
    public static final String GROUP_FRREG = "frreg";
    public static final String GROUP_FRVOICEONLY = "FRvoiceonly";

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
    public static final String CONTROL_BLOCK_LATE_ARRIVAL_AUDIT = "LateArrivalAudit";
    public static final String CONTROL_BLOCK_UNVISITED_RESTAURANTS_AUDIT = "UnvisitedRestaurantsAudit";
    public static final String CONTROL_BLOCK_SPLIT_RESTAURANT_AUDITS = "SplitRestaurantAudits";

    public static final String CONTROL_BLOCK_VALUE_DEFAULT_PREFIX = "ReplaceThisBy";
    public static final String CONTROL_BLOCK_VERSION_UNKNOWN = "0";
    public static final String CONTROL_BLOCK_VERSION_1 = "1";
    public static final String CONTROL_BLOCK_VERSION_2_0_0 = "2-0-0";

    public static final String CONTROL_BLOCK_CURRENT_VERSION = CONTROL_BLOCK_VERSION_2_0_0;


    public static final long UNKNOWN_USER_ID = 852;
    public static final String UNKNOWN_USER = "unknown-user";

    public static final String GMAPS_API_KEY_PROPERTY = "GMaps-Api-Key";
    public static final String MEMBERDATA_PROPERTIES = "memberdata.properties";
    static final String API_USER_PROPERTY = "Api-Username";
    static final String API_KEY_PROPERTY = "Api-Key";

    public static final int HTTP_TOO_MANY_REQUESTS = 429;
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;

    public static final String BIKE_EMOJI = ":bike:";
    public static final String AT_RISK_EMOJI = ":warning:";
    public static final String LIMITED_RUNS_EMOJI = ":red_circle:";
    public static final String GIFT_EMOJI = ":gift:";

    public static final String DISCOURSE_COLUMN_POST_NUMBER = "post_number";
    public static final String DISCOURSE_COLUMN_RAW = "raw";
    public static final String DISCOURSE_COLUMN_TOPIC_ID = "topic_id";
}
