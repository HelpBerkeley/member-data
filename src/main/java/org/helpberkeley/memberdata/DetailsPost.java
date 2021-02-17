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

public class DetailsPost {

    private final String userName;
    private long latestPostNumber = 0;
    private String details;

    public DetailsPost(String userName) {
        this.userName = userName;
    }

    public String getDetails() {
        return details.isEmpty() ? "" : details;
    }

    public long getLatestPostNumber() {
        return latestPostNumber;
    }

    public void setDetails(long postNumber, String details) {
        this.latestPostNumber = postNumber;
        this.details = details;
    }

    public void appendDetails(long postNumber, String details) {
        this.latestPostNumber = postNumber;

        if (this.details == null) {
            this.details = details;
        } else {
            if (! this.details.endsWith(".")) {
                this.details += ".";
            }

            this.details += ' ';
            this.details += details;
        }
    }
}
