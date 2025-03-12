package org.helpberkeley.memberdata;

import org.quartz.JobExecutionContext;

public class OneKitchenWorkflowJob implements Job {

    public OneKitchenWorkflowJob() {}

    /**
     * Execute Job after dependencies have been fulfilled.
     *
     * @param context
     */
    @Override
    public void execute(JobExecutionContext context) {
        System.out.println("Executing OneKitchenWorkflowJob.");
    }
}
