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

class ControlBlock {

    private final Map<String, String> controlBlockUniqueKeys = new HashMap<>();
    private final Map<String, List<String>> controlBlockNoneUniqueKeys = new HashMap<>();

    ControlBlock() {

    }

    void processRow(WorkflowBean bean, long lineNumber) {

        String variable = bean.getControlBlockKey().replaceAll("\\s", "");
        String value = bean.getControlBlockValue().replaceAll("\\s", "");

        switch (variable) {
            case Constants.CONTROL_BLOCK_OPS_MANAGER:
                processOpsManager(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_SPLIT_RESTAURANT:
                processSplitRestaurant(value, lineNumber);
                break;
            case Constants.CONTROL_BLOCK_BACKUP_DRIVER:
                processBackupDriver(value, lineNumber);
                break;
            default:
                throw new MemberDataException("Unknown key \"" + variable + "\" in the "
                        + Constants.WORKFLOW_USER_NAME_COLUMN + " column at line " + lineNumber + ".\n");
        }
    }

    private void processOpsManager(final String value, long lineNumber) {
        System.out.println("Ops Manager: " + value);
    }

    private void processSplitRestaurant(final String value, long lineNumber) {
        System.out.println("Split restaurant : " + value);
    }

    private void processBackupDriver(final String value, long lineNumber) {
        System.out.println("Backup driver : " + value);
    }

    private void addControlBlockValue(final String key, final String value) {

//        switch (key) {
//            case Constants.DATA_KEY_OP_MANANGER_USER_NAME:
//            case Constants.DATA_KEY_OP_MANANGER_PHONE:
//                if (controlBlockUniqueKeys.containsKey(key)) {
//                    throw new MemberDataException(
//                            "Duplicate data key name \"" + key + "\", line " + csvReader.getLinesRead());
//                }
//
//            case Constants.DATA_KEY_BACKUP_DRIVER:
//                break;
//            default:
//                throw new MemberDataException(
//                        "Unknown data key name \"" + key + "\", line " + csvReader.getLinesRead());
//        }
    }
}
