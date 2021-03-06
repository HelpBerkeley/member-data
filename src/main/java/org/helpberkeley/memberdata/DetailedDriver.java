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
package org.helpberkeley.memberdata;

public class DetailedDriver {
    private final User user;
    private final DetailsPost details;

    public DetailedDriver(User user, DetailsPost details) {
        this.user = user;
        this.details = details;
    }

    @Override
    public String toString() {
        return getUserName();
    }

    public String getUserName() {
        return user.getUserName();
    }

    public String getName() {
        return user.getName();
    }

    public String getCreateDate() {
        return user.getCreateDate();
    }

    public String getCreateTime() {
        return user.getCreateTime();
    }

    public long getLatestDetailsPostNumber() {
        return (details == null) ? 0 : details.getLatestPostNumber();
    }

    public String getDetails() {
        return (details == null) ? "" : details.getDetails();
    }

    public String getPhoneNumber() {
        return user.getPhoneNumber();
    }

    public Boolean isLimitedRuns() {
        return user.isLimitedRuns();
    }

    public Boolean isBiker() {
        return user.isBiker();
    }

    public Boolean isAtRisk() {
        return user.isAtRisk();
    }

    public Boolean isEventDriver() {
        return user.isEventDriver();
    }
}
