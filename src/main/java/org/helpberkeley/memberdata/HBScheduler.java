package org.helpberkeley.memberdata;

import org.quartz.*;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class HBScheduler implements Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HBScheduler.class);
    private HBCache cache;
    private Map<JobKey, String> cronSchedule;
    private org.quartz.Scheduler quartzScheduler;

    public HBScheduler() throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        this.quartzScheduler = sf.getScheduler();
        this.cache = new HBCache(2);
        quartzScheduler.getContext().put("cache", this.cache);
        cronSchedule = new HashMap<>();
    }

    public org.quartz.Scheduler getQuartzScheduler() {
        return this.quartzScheduler;
    }

    public void scheduleJobStartNow(JobDetail jobDetail) throws SchedulerException {
        Trigger startNowTrigger = newTrigger().withIdentity("start now").startNow().build();
        Date scheduleTime = quartzScheduler.scheduleJob(jobDetail, startNowTrigger);
        LOGGER.info("Scheduling job {} to start now: {}", jobDetail.getKey(), scheduleTime);
    }

    @Override
    public void scheduleJobCronTrigger(JobDetail jobDetail, String cronFormat) throws SchedulerException {
        CronTrigger trigger = newTrigger()
                .withSchedule(cronSchedule(cronFormat))
                .build();
        Date scheduleTime = quartzScheduler.scheduleJob(jobDetail, trigger);
        cronSchedule.put(jobDetail.getKey(), cronFormat);
        LOGGER.info("Scheduling job with cron format: [{}] and start time: [{}].", cronFormat, scheduleTime);
    }

    @Override
    public void start() throws SchedulerException {
        quartzScheduler.start();
        LOGGER.info("Starting scheduler");
    }

    @Override
    public void stop() throws SchedulerException {
        quartzScheduler.shutdown(true);
        LOGGER.info("Stopping scheduler after all executing jobs have finished.");
    }

    @Override
    public String getSchedule() {
        LOGGER.info("Getting current schedule");
        return cronSchedule.toString();
    }

    @Override
    public void addJobListener(JobListener listener, JobDetail jobDetail) throws SchedulerException {
        LOGGER.info("Adding job listener to job: {}", jobDetail.getKey());
        Matcher<JobKey> matcher = KeyMatcher.keyEquals(jobDetail.getKey());
        quartzScheduler.getListenerManager().addJobListener(listener, matcher);
        LOGGER.info("Listener added successfully for job: {}", jobDetail.getKey());
    }

}
