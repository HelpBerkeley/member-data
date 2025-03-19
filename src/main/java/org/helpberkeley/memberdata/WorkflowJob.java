package org.helpberkeley.memberdata;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowJob.class);

    public WorkflowJob() {}

    /**
     * Execute Job after dependencies have been fulfilled.
     *
     * @param context
     */
    @Override
    public void execute(JobExecutionContext context) {
        LOGGER.info("Executing WorkflowJob.");
    }
}
