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
    static final String COMMAND_POST_WORKFLOW = "post-workflow";
    static final String COMMAND_POST_INREACH = "post-inreach";
    static final String COMMAND_POST_DISPATCHERS = "post-dispatchers";

    static final String USAGE_ERROR = "Usage error for command ";
    static final String UNKNOWN_COMMAND = USAGE_ERROR + ": unknown command: ";
    static final String TOO_MANY_COMMANDS = USAGE_ERROR + ": too many commands";
    static final String MISSING_COMMAND = USAGE_ERROR + ": no command specified";
    static final String COMMAND_REQUIRES_FILE_NAME = ": command requires a file name parameter";
    static final String COMMAND_REQUIRES_TWO_FILE_NAMES = ": command requires two file name parameters";
    static final String COMMAND_REQUIRES_THREE_FILE_NAMES = ": command requires three file name parameters";
    static final String COMMAND_REQUIRES_SHORT_URL = ": command requires a short URL";
    static final String BAD_SHORT_URL = USAGE_ERROR + ": short url syntax error";
    static final String FILE_DOES_NOT_EXIST = USAGE_ERROR + ": file does not exist: ";

    static final String USAGE =
            "Usage: " + COMMAND_FETCH + "\n"
                    + "    | " + COMMAND_GET_DAILY_DELIVERIES + "\n"
                    + "    | " + COMMAND_GET_ORDER_HISTORY + "\n"
                    + "    | " + COMMAND_MERGE_ORDER_HISTORY
                                + " all-members-file order-history-file daily-deliveries-file\n"
                    + "    | " + COMMAND_POST_ERRORS + " errors-file-name\n"
                    + "    | " + COMMAND_POST_CONSUMER_REQUESTS + " consumer-requests-file-name\n"
                    + "    | " + COMMAND_POST_VOLUNTEER_REQUESTS + " volunteer-requests-file-name\n"
                    + "    | " + COMMAND_POST_DRIVERS + " drivers-file upload://short-url-file-name\n"
                    + "    | " + COMMAND_POST_ALL_MEMBERS + " all-members-file upload://short-url-file-name\n"
                    + "    | " + COMMAND_POST_WORKFLOW + " workflow-file upload://short-url-file-name\n"
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


    Options(final String[] args) {
        this.args = Arrays.copyOf(args, args.length);
    }

    void parse() {
        for (int index = 0; index < args.length; index++) {

            String arg = args[index];

            switch (arg) {
                case COMMAND_FETCH:
                case COMMAND_GET_ORDER_HISTORY:
                case COMMAND_GET_DAILY_DELIVERIES:
                    setCommand(arg);
                    break;
                case COMMAND_POST_ERRORS:
                case COMMAND_POST_CONSUMER_REQUESTS:
                case COMMAND_POST_VOLUNTEER_REQUESTS:
                case COMMAND_UPDATE_ERRORS:
                    setCommand(arg);
                    index++;
                    if (index == args.length) {
                        dieUsage(USAGE_ERROR + arg + COMMAND_REQUIRES_FILE_NAME);
                    }
                    fileName = args[index];
                    break;
                case COMMAND_POST_ALL_MEMBERS:
                case COMMAND_POST_WORKFLOW:
                case COMMAND_POST_DRIVERS:
                case COMMAND_POST_INREACH:
                case COMMAND_POST_DISPATCHERS:
                case COMMAND_UPDATE_DISPATCHERS:
                case COMMAND_UPDATE_ORDER_HISTORY:
                    setCommand(arg);
                    index++;
                    if (index == args.length) {
                        dieUsage(USAGE_ERROR + arg + COMMAND_REQUIRES_FILE_NAME);
                    }
                    fileName = args[index];

                    index++;
                    if (index == args.length) {
                        dieUsage(USAGE_ERROR + arg + COMMAND_REQUIRES_SHORT_URL);
                    }
                    shortURL = args[index];
                    break;
                case COMMAND_MERGE_ORDER_HISTORY:
                    setCommand(arg);
                    index++;
                    if (index == args.length) {
                        dieUsage(USAGE_ERROR + arg + COMMAND_REQUIRES_THREE_FILE_NAMES);
                    }
                    fileName = args[index];

                    index++;
                    if (index == args.length) {
                        dieUsage(USAGE_ERROR + arg + COMMAND_REQUIRES_THREE_FILE_NAMES);
                    }
                    secondFileName = args[index];

                    index++;
                    if (index == args.length) {
                        dieUsage(USAGE_ERROR + arg + COMMAND_REQUIRES_THREE_FILE_NAMES);
                    }
                    thirdFileName = args[index];
                    break;
                default:
                    dieUsage(UNKNOWN_COMMAND + arg);
            }
        }

        if (command == null) {
            dieUsage(MISSING_COMMAND);
        }

        if (fileName != null) {
            if (! new File(fileName).exists()) {
                dieUsage(FILE_DOES_NOT_EXIST + fileName);
            }
        }

        if (secondFileName != null) {
            if (! new File(secondFileName).exists()) {
                dieUsage(FILE_DOES_NOT_EXIST + secondFileName);
            }
        }

        if (thirdFileName != null) {
            if (! new File(thirdFileName).exists()) {
                dieUsage(FILE_DOES_NOT_EXIST + thirdFileName);
            }
        }

        if ((shortURL != null) && (! shortURL.startsWith("upload://"))) {
                dieUsage(BAD_SHORT_URL);
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

    private void setCommand(final String command) {
        if (this.command != null) {
            dieUsage(TOO_MANY_COMMANDS);
        }

        this.command = command;
    }

    private void dieUsage(final String message) {
        throw new OptionsException(message + "\n" + USAGE + "\n");
    }

    static class OptionsException extends MemberDataException {
        OptionsException(final String message) {
            super(message);
        }
    }
}
