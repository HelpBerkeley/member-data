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

import java.util.List;

/**
 * Represents a task or unit of work that can be scheduled and executed by a Scheduler.
 * Each Job has specific dependencies (files or other resources) that must be fulfilled
 * before the Job is executed. The dependencies are defined by the Job and are managed
 * by the Scheduler to ensure they are available when the Job runs.
 *
 * If the Scheduler disallows concurrent Job execution, Jobs will be queued sequentially unless they are
 * interrupted by a job with a higher Priority.
 */
public interface Job {

    enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    /**
     * Set job priority. Default job priority is MEDIUM.
     *
     * @param priority
     */
    void setPriority(Priority priority);

    /**
     * Return a list of JobDependency keys for CacheEntries. This is the list of dependencies required
     * to execute the Job.
     *
     * @return List of JobDependencies
     */
    List<JobDependency> getDependencies();

    /**
     * Execute Job after dependencies have been fulfilled.
     */
    void execute(List<CacheEntry> dependencies);
}
