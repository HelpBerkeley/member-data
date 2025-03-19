package org.helpberkeley.memberdata;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingJobSingletonCache implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollingJobSingletonCache.class);

    public PollingJobSingletonCache() {}

    /**
     * Execute Job after dependencies have been fulfilled.
     *
     * @param context
     */
    @Override
    public void execute(JobExecutionContext context) {
        LOGGER.info("Executing PollingJobSingletonCache: {}", context.getJobDetail().getKey());
        HBCacheSingleton cache = (HBCacheSingleton) HBCacheSingleton.getInstance();
        cache.fetch(); // or fetch could be an external method
    }
}
