package org.helpberkeley.memberdata;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class CheckWorkRequestsJobListener implements JobListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobListener.class);

    @Override
    public String getName() {
        return "PollingJob to workflow";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        LOGGER.info("PollingJob is about to be executed.");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        LOGGER.info("PollingJob execution was vetoed.");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext inContext, JobExecutionException inException) {
        LOGGER.info("Job {} was executed!", inContext.getJobDetail().getKey());
        if (inContext.getResult() == "one-kitchen workflow") {
            JobDetail oneKitchenWorkflowJob = newJob(OneKitchenWorkflowJob.class).withIdentity("one-kitchen workflow job").build();
            Trigger oneKitchenWorfklowTrigger = newTrigger().withIdentity("one-kitchen workflow job trigger").startNow().build();
            try {
                inContext.getScheduler().scheduleJob(oneKitchenWorkflowJob, oneKitchenWorfklowTrigger);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        } else if (inContext.getResult() == "regular workflow") {
            JobDetail workflowJob = newJob(WorkflowJob.class).withIdentity("regular workflow job").build();
            Trigger worfklowTrigger = newTrigger().withIdentity("workflow job trigger").startNow().build();
            try {
                inContext.getScheduler().scheduleJob(workflowJob, worfklowTrigger);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
