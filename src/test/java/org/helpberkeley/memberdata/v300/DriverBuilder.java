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
package org.helpberkeley.memberdata.v300;

import org.helpberkeley.memberdata.Constants;

public class DriverBuilder {

    private String userName = BuilderConstants.DEFAULT_DRIVER_USER_NAME;
    private String name = BuilderConstants.DEFAULT_DRIVER_NAME;

    public DriverBuilder withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public DriverBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return build();
    }

    public String build() {
        return "FALSE,TRUE,"
                + name
                + Constants.CSV_SEPARATOR
                + userName
                + Constants.CSV_SEPARATOR
                + "777-777-7777,none,Hills,Berkeley,77 77th Place,,,,,,,,,"
                + "\n";
    }
}
