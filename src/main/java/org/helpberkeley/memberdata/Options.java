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

import java.io.File;
import java.util.Arrays;

public class Options {

    static final String COMMAND_FETCH = "fetch";
    static final String COMMAND_ORDER_HISTORY = "order-history";
    static final String COMMAND_POST_ERRORS = "post-errors";
    static final String COMMAND_UPDATE_ERRORS = "update-errors";
    static final String COMMAND_UPDATE_DISPATCHERS = "update-dispatchers";
    static final String COMMAND_POST_VOLUNTEER_REQUESTS = "post-volunteer-requests";
    static final String COMMAND_POST_CONSUMER_REQUESTS = "post-consumer-requests";
    static final String COMMAND_POST_DRIVERS = "post-drivers";
    static final String COMMAND_POST_ALL_MEMBERS = "post-all-members";
    static final String COMMAND_POST_DISPATCHERS = "post-dispatchers";
    static final String COMMAND_INREACH = "inreach";
    static final String COMMAND_EMAIL = "email";
    static final String COMMAND_WORKFLOW = "workflow";
    static final String COMMAND_ONE_KITCHEN_WORKFLOW = "onekitchen-workflow";
    static final String COMMAND_DRIVER_MESSAGES = "driver-messages";
    static final String COMMAND_ONE_KITCHEN_DRIVER_MESSAGES = "one-kitchen-driver-messages";
    static final String COMMAND_COMPLETED_DAILY_ORDERS = "completed-daily-orders";
    static final String COMMAND_COMPLETED_ONEKITCHEN_ORDERS = "completed-onekitchen-orders";
    static final String COMMAND_DRIVERS = "drivers";
    static final String COMMAND_DRIVER_HISTORY = "driver-history";
    static final String COMMAND_ONEKITCHEN_DRIVER_HISTORY = "onekitchen-driver-history";
    static final String COMMAND_RESTAURANT_TEMPLATE = "restaurant-template";
    static final String COMMAND_ONE_KITCHEN_RESTAURANT_TEMPLATE = "onekitchen-restaurant-template";
    static final String COMMAND_CUSTOMER_CARE_POST = "customer-care";
    static final String COMMAND_FRREG = "frreg";
    static final String COMMAND_WORK_REQUESTS = "work-requests";

    static final String USAGE_ERROR = "Usage error for command ";
    static final String UNKNOWN_COMMAND = USAGE_ERROR + ": unknown command: ";
    static final String TOO_MANY_COMMANDS = USAGE_ERROR + ": too many commands";
    static final String COMMAND_REQUIRES_FILE_NAME = ": command requires a file name parameter";
    static final String COMMAND_REQUIRES_SHORT_URL = ": command requires a short URL";
    static final String COMMAND_REQUIRES_ONE_ARG = ": command requires an argument";
    static final String BAD_SHORT_URL = USAGE_ERROR + ": short url syntax error";
    static final String FILE_DOES_NOT_EXIST = USAGE_ERROR + ": file does not exist: ";

    static final String USAGE =
            "Usage: " + COMMAND_FETCH + "\n"
                    + "    | " + COMMAND_WORK_REQUESTS + " all-members-file\n"
                    + "    | " + COMMAND_COMPLETED_DAILY_ORDERS + " all-members-file\n"
                    + "    | " + COMMAND_COMPLETED_ONEKITCHEN_ORDERS + " all-members-file\n"
//                    + "    | " + COMMAND_DRIVER_MESSAGES + " all-members-file\n"
//                    + "    | " + COMMAND_ONE_KITCHEN_DRIVER_MESSAGES + " all-members-file\n"
                    + "    | " + COMMAND_ORDER_HISTORY + " all-members-file\n"
                    + "    | " + COMMAND_DRIVERS + " all-members-file\n"
                    + "    | " + COMMAND_DRIVER_HISTORY + "\n"
                    + "    | " + COMMAND_ONEKITCHEN_DRIVER_HISTORY + "\n"
                    + "    | " + COMMAND_INREACH + " all-members-file\n"
                    + "    | " + COMMAND_EMAIL + " all-members-file\n"
                    + "    | " + COMMAND_WORKFLOW + " all-members-file [true|false] (post status)\n"
                    + "    | " + COMMAND_ONE_KITCHEN_WORKFLOW + " all-members-file [true|false] [true|false] (post status)\n"
                    + "    | " + COMMAND_POST_ERRORS + " errors-file-name\n"
                    + "    | " + COMMAND_POST_CONSUMER_REQUESTS + " consumer-requests-file-name\n"
                    + "    | " + COMMAND_POST_VOLUNTEER_REQUESTS + " volunteer-requests-file-name\n"
                    + "    | " + COMMAND_POST_DRIVERS + " drivers-file\n"
                    + "    | " + COMMAND_POST_ALL_MEMBERS + " all-members-file\n"
                    + "    | " + COMMAND_POST_DISPATCHERS + " dispatchers-file upload://short-url-file-name\n"
                    + "    | " + COMMAND_UPDATE_ERRORS + " errors-file-name\n"
                    + "    | " + COMMAND_UPDATE_DISPATCHERS + " dispatchers-file-name\n"
                    + "    | " + COMMAND_CUSTOMER_CARE_POST + " all-members-file\n"
                    + "    | " + COMMAND_FRREG + " all-members-file\n"
                    + "    | " + COMMAND_RESTAURANT_TEMPLATE + "\n"
                    + "    | " + COMMAND_ONE_KITCHEN_RESTAURANT_TEMPLATE + "\n";

    private final String[] args;
    private String command;
    private String fileName;
    private String shortURL;
    private boolean postStatus = false;


    Options(final String[] args) {
        this.args = Arrays.copyOf(args, args.length);
    }

    void parse() {

        if (args.length == 0) {
            dieUsage();
        }

        // Note: when adding a command, add it to the appropriate COMMANDS_WITH_... array
        //       in TestBase, to get automated testing of option handling for it.

        int index = 0;

        String arg = args[index++];

        switch (arg) {
            case COMMAND_FETCH:
            case COMMAND_DRIVER_HISTORY:
            case COMMAND_ONEKITCHEN_DRIVER_HISTORY:
            case COMMAND_RESTAURANT_TEMPLATE:
            case COMMAND_ONE_KITCHEN_RESTAURANT_TEMPLATE:
                setCommand(arg);
                break;
            case COMMAND_WORK_REQUESTS:
            case COMMAND_POST_ERRORS:
            case COMMAND_POST_CONSUMER_REQUESTS:
            case COMMAND_POST_VOLUNTEER_REQUESTS:
            case COMMAND_UPDATE_ERRORS:
            case COMMAND_EMAIL:
//            case COMMAND_DRIVER_MESSAGES:
//            case COMMAND_ONE_KITCHEN_DRIVER_MESSAGES:
            case COMMAND_POST_ALL_MEMBERS:
            case COMMAND_POST_DRIVERS:
            case COMMAND_UPDATE_DISPATCHERS:
            case COMMAND_ORDER_HISTORY:
            case COMMAND_COMPLETED_DAILY_ORDERS:
            case COMMAND_COMPLETED_ONEKITCHEN_ORDERS:
            case COMMAND_INREACH:
            case COMMAND_CUSTOMER_CARE_POST:
            case COMMAND_FRREG:
            case COMMAND_DRIVERS:
                setCommand(arg);
                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_FILE_NAME);
                }
                fileName = args[index++];
                break;
            case COMMAND_WORKFLOW:
            case COMMAND_ONE_KITCHEN_WORKFLOW:
                setCommand(arg);
                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_FILE_NAME);
                }
                fileName = args[index++];

                if (index < args.length) {
                    postStatus = Boolean.parseBoolean(args[index++]);
                }

                break;
            case COMMAND_POST_DISPATCHERS:
                setCommand(arg);
                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_FILE_NAME);
                }
                fileName = args[index++];

                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_SHORT_URL);
                }
                shortURL = args[index++];
                break;
            default:
                dieMessage(UNKNOWN_COMMAND + arg);
        }

        if (index < args.length) {
            dieUsage();
        }

        if (fileName != null) {
            if (! new File(fileName).exists()) {
                dieMessage(FILE_DOES_NOT_EXIST + fileName);
            }
        }

        if ((shortURL != null) && (! UploadFile.containsUploadFileURL(shortURL))) {
                dieMessage(BAD_SHORT_URL);
        }
    }

    String getCommand() {
        return command;
    }

    String getFileName() {
        return fileName;
    }

    String getShortURL() {
        return shortURL;
    }

    boolean postStatus() {
        return postStatus;
    }

    private void setCommand(final String command) {
        if (this.command != null) {
            dieMessage(TOO_MANY_COMMANDS);
        }

        this.command = command;
    }

    private void dieMessage(final String message) {
        throw new OptionsException(message + "\n" + USAGE + "\n");
    }

    private void dieUsage() throws OptionsException {
        throw new OptionsException(USAGE + "\n");
    }

    static class OptionsException extends MemberDataException {
        OptionsException(final String message) {
            super(message);
        }
    }
}
