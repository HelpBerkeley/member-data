package org.helpberkeley.memberdata;

import org.quartz.JobExecutionContext;

public class WorkflowJob implements Job {

    public WorkflowJob() {}

    /**
     * Execute Job after dependencies have been fulfilled.
     *
     * @param context
     */
    @Override
    public void execute(JobExecutionContext context) {
        System.out.println("Executing WorkflowJob.");
    }
}
