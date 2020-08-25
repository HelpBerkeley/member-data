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

import java.io.File;
import java.util.Arrays;

public class Options {

    static final String COMMAND_FETCH = "fetch";
    static final String COMMAND_GET_ORDER_HISTORY = "get-order-history";
    static final String COMMAND_GET_DAILY_DELIVERIES = "get-daily-deliveries";
    static final String COMMAND_MERGE_ORDER_HISTORY = "merge-order-history";
    static final String COMMAND_POST_ERRORS = "post-errors";
    static final String COMMAND_UPDATE_ERRORS = "update-errors";
    static final String COMMAND_UPDATE_DISPATCHERS = "update-dispatchers";
    static final String COMMAND_UPDATE_ORDER_HISTORY = "update-order-history";
    static final String COMMAND_POST_VOLUNTEER_REQUESTS = "post-volunteer-requests";
    static final String COMMAND_POST_CONSUMER_REQUESTS = "post-consumer-requests";
    static final String COMMAND_POST_DRIVERS = "post-drivers";
    static final String COMMAND_POST_ALL_MEMBERS = "post-all-members";
    static final String COMMAND_POST_INREACH = "post-inreach";
    static final String COMMAND_POST_DISPATCHERS = "post-dispatchers";
    static final String COMMAND_INREACH = "inreach";
    static final String COMMAND_EMAIL = "email";
    static final String COMMAND_WORKFLOW = "workflow";
    static final String COMMAND_GENERATE_DRIVERS_POSTS = "generate-drivers-posts";
    static final String COMMAND_GET_ROUTED_WORKFLOW = "get-routed-workflow";
    static final String COMMAND_GET_REQUEST_DRIVER_ROUTES = "get-request-driver-routes";
    static final String COMMAND_REQUEST_DRIVER_ROUTES_SUCCEEDED = "request-driver-routes-succeeded";
    static final String COMMAND_REQUEST_DRIVER_ROUTES_FAILED = "request-driver-routes-failed";

    static final String USAGE_ERROR = "Usage error for command ";
    static final String UNKNOWN_COMMAND = USAGE_ERROR + ": unknown command: ";
    static final String TOO_MANY_COMMANDS = USAGE_ERROR + ": too many commands";
    static final String COMMAND_REQUIRES_FILE_NAME = ": command requires a file name parameter";
    static final String COMMAND_REQUIRES_TWO_FILE_NAMES = ": command requires two file name parameters";
    static final String COMMAND_REQUIRES_THREE_FILE_NAMES = ": command requires three file name parameters";
    static final String COMMAND_REQUIRES_SHORT_URL = ": command requires a short URL";
    static final String COMMAND_REQUIRES_REQUEST_FILE_NAME = ": command requires a request file name parameter";
    static final String BAD_SHORT_URL = USAGE_ERROR + ": short url syntax error";
    static final String FILE_DOES_NOT_EXIST = USAGE_ERROR + ": file does not exist: ";

    static final String USAGE =
            "Usage: " + COMMAND_FETCH + "\n"
                    + "    | " + COMMAND_GET_DAILY_DELIVERIES + "\n"
                    + "    | " + COMMAND_GET_ORDER_HISTORY + "\n"
                    + "    | " + COMMAND_GET_ROUTED_WORKFLOW + "\n"
                    + "    | " + COMMAND_GET_REQUEST_DRIVER_ROUTES + "\n"
                    + "    | " + COMMAND_REQUEST_DRIVER_ROUTES_SUCCEEDED
                                + " uploaded-file-name"
                                + " uploaded-short-url"
                                + " [status messages ...]\n"
                    + "    | " + COMMAND_REQUEST_DRIVER_ROUTES_FAILED
                                + " request-file-name"
                                + " [status messages ...]\n"
                    + "    | " + COMMAND_MERGE_ORDER_HISTORY
                                + " all-members-file order-history-file daily-deliveries-file\n"
                    + "    | " + COMMAND_INREACH + " all-members-file order-history-file\n"
                    + "    | " + COMMAND_EMAIL + " all-members-file\n"
                    + "    | " + COMMAND_WORKFLOW + " all-members-file\n"
                    + "    | " + COMMAND_GENERATE_DRIVERS_POSTS + " routed-workflow-file\n"
                    + "    | " + COMMAND_POST_ERRORS + " errors-file-name\n"
                    + "    | " + COMMAND_POST_CONSUMER_REQUESTS + " consumer-requests-file-name\n"
                    + "    | " + COMMAND_POST_VOLUNTEER_REQUESTS + " volunteer-requests-file-name\n"
                    + "    | " + COMMAND_POST_DRIVERS + " drivers-file upload://short-url-file-name\n"
                    + "    | " + COMMAND_POST_ALL_MEMBERS + " all-members-file\n"
                    + "    | " + COMMAND_POST_INREACH + " inreach-file upload://short-url-file-name\n"
                    + "    | " + COMMAND_POST_DISPATCHERS + " dispatchers-file upload://short-url-file-name\n"
                    + "    | " + COMMAND_UPDATE_ERRORS + " errors-file-name\n"
                    + "    | " + COMMAND_UPDATE_DISPATCHERS + " dispatchers-file-name upload://short-url-file-name\n"
                    + "    | " + COMMAND_UPDATE_ORDER_HISTORY + " order-history-file-name upload://short-url-file-name\n";

    private final String[] args;
    private String command;
    private String fileName;
    private String secondFileName;
    private String thirdFileName;
    private String shortURL;
    private String requestFileName;
    private String statusMessage = "";


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
            case COMMAND_GET_ORDER_HISTORY:
            case COMMAND_GET_DAILY_DELIVERIES:
            case COMMAND_GET_ROUTED_WORKFLOW:
            case COMMAND_GET_REQUEST_DRIVER_ROUTES:
                setCommand(arg);
                break;
            case COMMAND_POST_ERRORS:
            case COMMAND_POST_CONSUMER_REQUESTS:
            case COMMAND_POST_VOLUNTEER_REQUESTS:
            case COMMAND_UPDATE_ERRORS:
            case COMMAND_EMAIL:
            case COMMAND_GENERATE_DRIVERS_POSTS:
            case COMMAND_WORKFLOW:
            case COMMAND_POST_ALL_MEMBERS:
                setCommand(arg);
                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_FILE_NAME);
                }
                fileName = args[index++];
                break;
            case COMMAND_POST_DRIVERS:
            case COMMAND_POST_INREACH:
            case COMMAND_POST_DISPATCHERS:
            case COMMAND_UPDATE_DISPATCHERS:
            case COMMAND_UPDATE_ORDER_HISTORY:
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
            case COMMAND_INREACH:
                setCommand(arg);
                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_TWO_FILE_NAMES);
                }
                fileName = args[index++];

                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_TWO_FILE_NAMES);
                }
                secondFileName = args[index++];
                break;
            case COMMAND_MERGE_ORDER_HISTORY:
                setCommand(arg);
                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_THREE_FILE_NAMES);
                }
                fileName = args[index++];

                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_THREE_FILE_NAMES);
                }
                secondFileName = args[index++];

                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_THREE_FILE_NAMES);
                }
                thirdFileName = args[index++];
                break;
            case COMMAND_REQUEST_DRIVER_ROUTES_SUCCEEDED:
                setCommand(arg);
                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_FILE_NAME);
                }
                fileName = args[index++];

                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_SHORT_URL);
                }
                shortURL = args[index];

                while (++index < args.length) {
                    statusMessage += args[index] + " ";
                }

                break;
            case COMMAND_REQUEST_DRIVER_ROUTES_FAILED:
                setCommand(arg);
                if (index == args.length) {
                    dieMessage(USAGE_ERROR + arg + COMMAND_REQUIRES_REQUEST_FILE_NAME);
                }
                requestFileName = args[index];

                while (++index < args.length) {
                    statusMessage += args[index] + " ";
                }

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

        if (secondFileName != null) {
            if (! new File(secondFileName).exists()) {
                dieMessage(FILE_DOES_NOT_EXIST + secondFileName);
            }
        }

        if (thirdFileName != null) {
            if (! new File(thirdFileName).exists()) {
                dieMessage(FILE_DOES_NOT_EXIST + thirdFileName);
            }
        }

        if ((shortURL != null) && (! shortURL.startsWith("upload://"))) {
                dieMessage(BAD_SHORT_URL);
        }
    }

    String getCommand() {
        return command;
    }

    String getFileName() {
        return fileName;
    }

    String getSecondFileName() {
        return secondFileName;
    }

    String getThirdFileName() {
        return thirdFileName;
    }

    String getShortURL() {
        return shortURL;
    }

    String getRequestFileName() {
        return requestFileName;
    }

    String getStatusMessage() {
        return statusMessage;
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
