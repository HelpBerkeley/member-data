package org.helpberkeley.memberdata;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class HBScheduler implements Scheduler {
//    private Cache cache;
    private Map<JobKey, String> cronSchedule;
    private org.quartz.Scheduler quartzScheduler;

    public HBScheduler() throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        this.quartzScheduler = sf.getScheduler();
//        this.allowConcurrency = allowConcurrency;
//        this.cache = cache;
        cronSchedule = new HashMap<>();
    }

    @Override
    public void scheduleJobCronTrigger(JobDetail jobDetail, String cronFormat) throws SchedulerException {
        CronTrigger trigger = newTrigger()
                .withSchedule(cronSchedule(cronFormat))
                .build();
        Date scheduleTime = quartzScheduler.scheduleJob(jobDetail, trigger);
        cronSchedule.put(jobDetail.getKey(), cronFormat);
        System.out.println("Scheduling job with cron format: [" + cronFormat + "] and start time: [" + scheduleTime +"].");
    }

    @Override
    public void start() throws SchedulerException {
        quartzScheduler.start();
        System.out.println("Starting scheduler");
    }

    @Override
    public void stop() throws SchedulerException {
        quartzScheduler.shutdown(true);
        System.out.println("Stopping scheduler after all executing jobs have finished.");
    }

    @Override
    public String getSchedule() {
        System.out.println("Getting current schedule");
        return cronSchedule.toString();
    }

    @Override
    public void addJobListener(JobListener listener, JobDetail jobDetail) throws SchedulerException {
        Matcher<JobKey> matcher = KeyMatcher.keyEquals(jobDetail.getKey());
        quartzScheduler.getListenerManager().addJobListener(listener, matcher);
    }

}
