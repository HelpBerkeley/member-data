//
// Copyright (c) 2024 helpberkeley.org
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

import org.quartz.TriggerKey;

public interface Scheduler {

    /**
     * Schedule job to run at a set time.
     *
     * @param cronFormat string representing the job schedule in Cron format
     */
    void scheduleJob(String cronFormat);

    /**
     * Schedule job to run with a set interval in seconds.
     *
     * @param intervalSeconds long representing how frequently the job should run
     */
    void scheduleRecurringJob(long intervalSeconds);

    /**
     * Schedule job to run when a specific event is triggered.
     *
     * @param triggerKey a key representing a Quartz trigger tied to a specific event happening
     */
    void scheduleJobWithTrigger(TriggerKey triggerKey);

    /**
     * Start scheduler, run all jobs according to schedule.
     */
    void start();

    /**
     * Stop scheduler, stop running all scheduled jobs.
     */
    void stop();
}
