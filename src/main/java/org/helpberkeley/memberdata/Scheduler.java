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

/**
 * Interface for scheduling and managing the execution of Jobs. The Scheduler is responsible
 * for scheduling Jobs at specified times or intervals, as well as starting and stopping
 * the scheduling process. It also handles dependency fulfillment for each Job before execution.
 *
 * If concurrent job execution is disallowed, jobs may only be interrupted by jobs with a higher Job.Priority.
 */
public interface Scheduler {

    /**
     * Create scheduler and specify whether jobs should execute concurrently.
     *
     * @param allowConcurrency if True, allow multithreading of jobs. Disallow if False.
     */
    void create(boolean allowConcurrency);

    /**
     * Schedule job to run at a set time.
     *
     * @param job to be run
     * @param cronFormat string representing the job schedule in Cron format:
     *        <second> <minute> <hour> <day-of-month> <month> <day-of-week>
     */
    void scheduleJob(Job job, String cronFormat);

    /**
     * Schedule job to run when a specific event is triggered.
     *
     * @param job to be run
     * @param event that triggers the job to run
     */
    void scheduleJobWithEventTrigger(Job job, Event event);

    /**
     * Start scheduler, run all jobs according to schedule.
     */
    void start();

    /**
     * Stop scheduler, stop running all scheduled jobs.
     */
    void stop();

    /**
     * Reports the current job schedule.
     *
     * @return a formatted String with the current job schedule
     */
    String getSchedule();

    /**
     * Remove all jobs from schedule.
     */
    void destroy();
}
