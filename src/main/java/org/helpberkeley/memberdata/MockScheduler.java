package org.helpberkeley.memberdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class MockScheduler implements Scheduler {
    private boolean allowConcurrency;
    private Cache cache;
    private Map<Job, String> cronSchedule;
    private Queue<Job> jobQueue;
    private Boolean isRunning;

    public MockScheduler(boolean allowConcurrency, Cache cache) {
        this.allowConcurrency = allowConcurrency;
        this.cache = cache;
        cronSchedule = new HashMap<>();
        System.out.println("Scheduler created with allowConcurrency: " + allowConcurrency);
    }

    @Override
    public void scheduleJob(Job job, String cronFormat) {
        cronSchedule.put(job, cronFormat);
        System.out.println("Scheduling job with cron format: " + cronFormat);
    }

    @Override
    public void scheduleJobWithEventTrigger(Job job, Event event) {
        System.out.println("Scheduling job with event trigger: " + event.getId());
    }

    @Override
    public void start() {
        System.out.println("Starting scheduler");
    }

    @Override
    public void stop() {
        System.out.println("Stopping scheduler");
    }

    @Override
    public String getSchedule() {
        System.out.println("Getting current schedule");
        return cronSchedule.toString();
    }

    @Override
    public void destroy() {
        System.out.println("Destroying scheduler and clearing jobs");
    }
}
