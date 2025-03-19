package org.helpberkeley.memberdata;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostConsumerRequestJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostConsumerRequestJob.class);
    public static final String REQUEST = "request";

    public PostConsumerRequestJob() {}

    /**
     * Execute Job after dependencies have been fulfilled.
     *
     * @param context
     */
    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        String request = data.getString(REQUEST);
        LOGGER.info("Posting ConsumerRequest: {}", request);
    }
}
