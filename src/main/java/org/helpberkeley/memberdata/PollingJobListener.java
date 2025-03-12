package org.helpberkeley.memberdata;

import org.quartz.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class PollingJobListener implements JobListener {
    @Override
    public String getName() {
        return "PollingJob to workflow";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        System.out.println("PollingJob is about to be executed.");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        System.out.println("PollingJob execution was vetoed.");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext inContext, JobExecutionException inException) {
        if (inContext.getResult() == "one-kitchen workflow") {
            JobDetail oneKitchenWorkflowJob = newJob(OneKitchenWorkflowJob.class).withIdentity("one-kitchen workflow job").build();
            Trigger oneKitchenWorfklowTrigger = newTrigger().withIdentity("one-kitchen workflow job trigger").build();
            try {
                inContext.getScheduler().scheduleJob(oneKitchenWorkflowJob, oneKitchenWorfklowTrigger);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        } else if (inContext.getResult() == "regular workflow") {
            JobDetail workflowJob = newJob(WorkflowJob.class).withIdentity("regular workflow job").build();
            Trigger worfklowTrigger = newTrigger().withIdentity("workflow job trigger").build();
            try {
                inContext.getScheduler().scheduleJob(workflowJob, worfklowTrigger);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
