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

import java.util.Arrays;

public class Options {

    static final String COMMAND_FETCH = "fetch";
    static final String COMMAND_POST_ERRORS = "post-errors";
    static final String COMMAND_POST_NON_CONSUMERS = "post-non-consumers";
    static final String COMMAND_UPDATE_ERRORS = "update-errors";
    static final String COMMAND_UPDATE_NON_CONSUMERS = "update-non-consumers";
    static final String COMMAND_UPDATE_CONSUMER_REQUESTS = "update-consumer-requests";
    static final String COMMAND_UPDATE_VOLUNTEER_REQUESTS = "update-volunteer-requests";
    static final String COMMAND_UPDATE_DRIVERS = "update-drivers";
    static final String COMMAND_POST_VOLUNTEER_REQUESTS = "post-volunteer-requests";
    static final String COMMAND_POST_CONSUMER_REQUESTS = "post-consumer-requests";

    static final String USAGE_ERROR = "Usage error: ";
    static final String UNKNOWN_COMMAND = USAGE_ERROR + "unknown command: ";
    static final String TOO_MANY_COMMANDS = USAGE_ERROR + "too many commands";
    static final String MISSING_COMMAND = USAGE_ERROR + "no command specified";
    static final String COMMAND_REQUIRES_FILE_NAME = " command requires a file name parameter";

    static final String USAGE =
            "Usage: " + COMMAND_FETCH + "\n"
                    + "    | " + COMMAND_POST_ERRORS + " errors-file-name\n"
                    + "    | " + COMMAND_POST_NON_CONSUMERS + " non-consumers-file-name\n"
                    + "    | " + COMMAND_POST_VOLUNTEER_REQUESTS + " volunteer-requests-file-name\n"
                    + "    | " + COMMAND_UPDATE_ERRORS + " errors-file-name\n"
                    + "    | " + COMMAND_UPDATE_NON_CONSUMERS + " non-consumers-file-name\n"
                    + "    | " + COMMAND_UPDATE_CONSUMER_REQUESTS + " consumer-requests-file-name\n";

    private final String[] args;
    private String command;
    private String fileName;
    private boolean exceptionsEnabled = false;


    Options(final String[] args) {
        this.args = Arrays.copyOf(args, args.length);
    }

    void parse() {
        for (int index = 0; index < args.length; index++) {

            String arg = args[index];

            switch (arg) {
                case COMMAND_FETCH:
                    setCommand(arg);
                    break;
                case COMMAND_POST_ERRORS:
                case COMMAND_POST_NON_CONSUMERS:
                case COMMAND_POST_CONSUMER_REQUESTS:
                case COMMAND_POST_VOLUNTEER_REQUESTS:
                case COMMAND_UPDATE_ERRORS:
                case COMMAND_UPDATE_NON_CONSUMERS:
                case COMMAND_UPDATE_CONSUMER_REQUESTS:
                case COMMAND_UPDATE_VOLUNTEER_REQUESTS:
                case COMMAND_UPDATE_DRIVERS:
                    setCommand(arg);
                    index++;
                    if (index == args.length) {
                        dieUsage(USAGE_ERROR + arg + COMMAND_REQUIRES_FILE_NAME);
                    }
                    fileName = args[index];
                    break;
                default:
                    dieUsage(UNKNOWN_COMMAND + arg);
            }
        }

        if (command == null) {
            dieUsage(MISSING_COMMAND);
        }
    }

    void setExceptions(boolean doExceptions) {
        this.exceptionsEnabled = doExceptions;
    }

    String getCommand() {
        return command;
    }

    String getFileName() {
        return fileName;
    }

    private void setCommand(final String command) {
        if (this.command != null) {
            dieUsage(TOO_MANY_COMMANDS);
        }

        this.command = command;
    }

    private void dieUsage(final String message) {

        if (exceptionsEnabled) {
            throw new OptionsException(message + "\n" + USAGE + "\n");
        }

        System.out.println(message);
        System.out.println(USAGE);
        System.exit(1);
    }

    static class OptionsException extends MemberDataException {
        OptionsException(final String message) {
            super(message);
        }
    }
}
