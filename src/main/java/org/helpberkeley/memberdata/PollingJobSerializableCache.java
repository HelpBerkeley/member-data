package org.helpberkeley.memberdata;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingJobSerializableCache implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(org.helpberkeley.memberdata.PollingJobSerializableCache.class);

    public PollingJobSerializableCache() {}

    /**
     * Execute Job after dependencies have been fulfilled.
     *
     * @param context
     */
    @Override
    public void execute(JobExecutionContext context) {
        LOGGER.info("Executing PollingJobSerializableCache: {}", context.getJobDetail().getKey());
        HBCacheSerializable cache = (HBCacheSerializable) context.getMergedJobDataMap().get("cache");

        if (cache != null) {
            LOGGER.info("Successfully retrieved cache from JobDataMap.");
            cache.fetch();
        } else {
            LOGGER.error("Cache not found in JobDataMap.");
        }
    }
}
