package org.helpberkeley.memberdata;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingSingletonCacheJobListener implements JobListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobListener.class);

    @Override
    public String getName() {
        return "polling job with singleton cache job listener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        LOGGER.info("Polling job with singleton cache is about to be executed.");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        LOGGER.info("Polling job with singleton cache had its execution vetoed.");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        HBCacheSingleton cache = (HBCacheSingleton) HBCacheSingleton.getInstance();
        if (cache.containsKey(JobDependency.CONSUMER_REQUESTS)) {
            LOGGER.info("job was executed and cache contains dependency: {}", JobDependency.CONSUMER_REQUESTS);
        } else {
            LOGGER.info("job was executed and cache does not contain dependency: {}", JobDependency.CONSUMER_REQUESTS);
        }
    }
}
