/*
 * Copyright (c) 2021. helpberkeley.org
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
package org.helpberkeley.memberdata.v200;

import org.helpberkeley.memberdata.Constants;

import static org.helpberkeley.memberdata.v200.BuilderConstants.*;

public class ControlBlockBuilder {

    public static final String DEFAULT_PICKUP_MANAGER = "ZZZ";
    public static final String DEFAULT_OPS_MANAGER = "JVol|123-456-7890";

    private String opsManager = DEFAULT_OPS_MANAGER;
    private String backupDriver = null;

    @Override
    public String toString() {
        return build();
    }

    public String build() {
        StringBuilder controlBlock = new StringBuilder();

        controlBlock.append(HEADER);
        controlBlock.append(CONTROL_BLOCK_BEGIN_ROW);
        controlBlock.append(CONTROL_BLOCK_VERSION_ROW);

        if (opsManager != null) {
            controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_OPS_MANAGER_USERNAME_AND_PHONE, opsManager));
        }
        if (backupDriver != null) {
            controlBlock.append(getKeyValueRow(Constants.CONTROL_BLOCK_BACKUP_DRIVER, backupDriver));
        }
        controlBlock.append(CONTROL_BLOCK_END_ROW);
        controlBlock.append(EMPTY_ROW);

        return controlBlock.toString();
    }

    public ControlBlockBuilder withOpsManager(String opsManager) {
        this.opsManager = opsManager;
        return this;
    }

    public ControlBlockBuilder withoutOpsManager() {
        this.opsManager = null;
        return this;
    }

    public ControlBlockBuilder withBackupDriver(String backupDriver) {
        this.backupDriver = backupDriver;
        return this;
    }

    private String getDirectiveRow(String directive) {
        return EMPTY_ROW.replaceFirst(",,,", "FALSE,FALSE," + directive + ",");
    }

    private String getKeyValueRow(String key, String value) {
        return EMPTY_ROW.replaceFirst(",,,,,,,", "FALSE,FALSE,," + key + ",,,," + value);
    }

    private String quote(String quotee) {
        return "\"" + quotee + "\"";
    }
}
