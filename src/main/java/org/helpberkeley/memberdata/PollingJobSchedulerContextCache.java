package org.helpberkeley.memberdata;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingJobSchedulerContextCache implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(org.helpberkeley.memberdata.PollingJobSchedulerContextCache.class);

    public PollingJobSchedulerContextCache() {}

    /**
     * Execute Job after dependencies have been fulfilled.
     *
     * @param context
     */
    @Override
    public void execute(JobExecutionContext context) {
        LOGGER.info("Executing PollingJobSchedulerContextCache: {}", context.getJobDetail().getKey());
        HBCache cache = null;
        try {
            cache = (HBCache) context.getScheduler().getContext().get("cache");
            LOGGER.info("Successfully retrieved cache from SchedulerContext");
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        cache.fetch();
    }
}
