package org.helpberkeley.memberdata;

import org.junit.Test;
import org.quartz.*;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;

import java.util.Date;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class QuartzTest {

    @Test
    public void simpleJobListenerTest() throws SchedulerException, InterruptedException {
        JobKey jobKey = new JobKey("testKey", "testGroup");
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();
        HBCacheSingleton cache = (HBCacheSingleton) HBCacheSingleton.getInstance();
        if (cache.containsKey(JobDependency.CONSUMER_REQUESTS)) {
            System.out.println("cache contains dependency: " + JobDependency.CONSUMER_REQUESTS);
        } else {
            System.out.println("cache does not contain dependency: " + JobDependency.CONSUMER_REQUESTS);
        }
        JobDetail job1 = newJob(PollingJobSingletonCache.class).withIdentity(jobKey).build();
//        PollingSingletonCacheJobListener job1Listener = new PollingSingletonCacheJobListener();
        sched.getListenerManager().addJobListener(
                new PollingSingletonCacheJobListener(), KeyMatcher.keyEquals(jobKey)
        );
//        Matcher<JobKey> matcher = KeyMatcher.keyEquals(job1.getKey());
//        sched.getListenerManager().addJobListener(job1Listener, matcher);
//        Trigger startNowTrigger = newTrigger().withIdentity("start now").startNow().build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("simpleTrigger", "testGroup")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(10).repeatForever()).build();
        sched.start();
        Date scheduleTime = sched.scheduleJob(job1, trigger);

        List<JobListener> listeners = sched.getListenerManager().getJobListeners();
        for (JobListener jl: listeners) {
            System.out.println(jl.getName());
        }
        Thread.sleep(5000);
//        sched.shutdown(true);
    }

    @Test
    public void simpleSchedTest() throws SchedulerException {
        HBScheduler sched = new HBScheduler();

        JobDetail job1 = newJob(CheckWorkRequestsJob.class).withIdentity("job1", "group1").build();
        job1.getJobDataMap().put(CheckWorkRequestsJob.RUNTIME_DEPENDENCY, "runtime dependency A");
        JobListener job1Listener = new CheckWorkRequestsJobListener(); // attach jobListener to act on results of the job
        sched.addJobListener(job1Listener, job1);
        sched.scheduleJobCronTrigger(job1, "0 * * * * ?");

        JobDetail job2 = newJob(CheckWorkRequestsJob.class).withIdentity("job2", "group1").build();
        job2.getJobDataMap().put(CheckWorkRequestsJob.RUNTIME_DEPENDENCY, "runtime dependency B");
        JobListener job2Listener = new CheckWorkRequestsJobListener();
        sched.addJobListener(job2Listener, job2);
        sched.scheduleJobCronTrigger(job2, "0 * * * * ?");

        sched.start();

        try {
            // wait five minutes to show jobs
            Thread.sleep(60L * 1000L);
        } catch (Exception e) {
        }

        sched.stop();
    }

    @Test
    public void simpleSchedTestWithCache() throws SchedulerException {
        HBScheduler sched = new HBScheduler();
        HBCache cache = new HBCache(2);
        cache.fetch();
        if (cache.hasChanged(JobDependency.CONSUMER_REQUESTS)) {
            JobDetail postConsumerRequest = newJob(PostConsumerRequestJob.class).withIdentity("job1", "group1").build();
            postConsumerRequest.getJobDataMap().put(PostConsumerRequestJob.REQUEST, cache.getLatestVersion(JobDependency.CONSUMER_REQUESTS));
            sched.scheduleJobStartNow(postConsumerRequest);
            sched.start();
        }
        sched.stop();
    }

    @Test
    public void updateSingletonCacheTest() throws SchedulerException, InterruptedException {
        HBScheduler sched = new HBScheduler();
        HBCacheSingleton cache = (HBCacheSingleton) HBCacheSingleton.getInstance();
        if (cache.containsKey(JobDependency.CONSUMER_REQUESTS)) {
            System.out.println("before job execution, cache contains dependency: " + JobDependency.CONSUMER_REQUESTS);
        } else {
            System.out.println("before job execution, cache does not contain dependency: " + JobDependency.CONSUMER_REQUESTS);
        }
        JobDetail job1 = newJob(PollingJobSingletonCache.class).withIdentity("job1", "group1").build();
        sched.scheduleJobStartNow(job1);
        sched.start();
        Thread.sleep(5000);
        sched.stop();
        if (cache.containsKey(JobDependency.CONSUMER_REQUESTS)) {
            System.out.println("after job execution, cache contains dependency: " + JobDependency.CONSUMER_REQUESTS);
        } else {
            System.out.println("after job execution, does not contain dependency: " + JobDependency.CONSUMER_REQUESTS);
        }
        assert (cache.containsKey(JobDependency.CONSUMER_REQUESTS));
    }

    @Test
    public void schedulerContextCacheTest() throws SchedulerException, InterruptedException {
        HBScheduler sched = new HBScheduler();
        HBCache cache = (HBCache) sched.getQuartzScheduler().getContext().get("cache"); // scheduler context should be available to all jobs using the scheduler
        if (cache.containsKey(JobDependency.CONSUMER_REQUESTS)) {
            System.out.println("before job execution, cache contains dependency: " + JobDependency.CONSUMER_REQUESTS);
        } else {
            System.out.println("before job execution, cache does not contain dependency: " + JobDependency.CONSUMER_REQUESTS);
        }
        JobKey jobKey = new JobKey("testKey", "testGroup");
        JobDetail job1 = newJob(PollingJobSchedulerContextCache.class).withIdentity(jobKey).build();
        sched.scheduleJobStartNow(job1);
        sched.start();
        Thread.sleep(5000);
        sched.stop();
        if (cache.containsKey(JobDependency.CONSUMER_REQUESTS)) {
            System.out.println("after job execution, cache contains dependency: " + JobDependency.CONSUMER_REQUESTS);
        } else {
            System.out.println("after job execution, does not contain dependency: " + JobDependency.CONSUMER_REQUESTS);
        }
        assert (cache.containsKey(JobDependency.CONSUMER_REQUESTS));
    }

    @Test
    public void serializableCacheTest() throws SchedulerException, InterruptedException {
        HBScheduler sched = new HBScheduler();
        HBCacheSerializable cache = new HBCacheSerializable(2); // cache must be serializable to be passed in jobDataMap
        if (cache.containsKey(JobDependency.CONSUMER_REQUESTS)) {
            System.out.println("before job execution, cache contains dependency: " + JobDependency.CONSUMER_REQUESTS);
        } else {
            System.out.println("before job execution, cache does not contain dependency: " + JobDependency.CONSUMER_REQUESTS);
        }
        JobKey jobKey = new JobKey("testKey", "testGroup");
        JobDetail job1 = newJob(PollingJobSerializableCache.class).withIdentity(jobKey).build();
        job1.getJobDataMap().put("cache", cache);
        sched.scheduleJobStartNow(job1);
        sched.start();
        Thread.sleep(5000);
        sched.stop();
        if (cache.containsKey(JobDependency.CONSUMER_REQUESTS)) {
            System.out.println("after job execution, cache contains dependency: " + JobDependency.CONSUMER_REQUESTS);
        } else {
            System.out.println("after job execution, does not contain dependency: " + JobDependency.CONSUMER_REQUESTS);
        }
        assert (cache.containsKey(JobDependency.CONSUMER_REQUESTS));
    }
}
