package org.helpberkeley.memberdata;

import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.matchers.KeyMatcher;

import static org.quartz.JobBuilder.newJob;

public class QuartzTest {

    @Test
    public void run() throws SchedulerException {
        HBScheduler sched = new HBScheduler();

        JobDetail job1 = newJob(PollingJob.class).withIdentity("job1", "group1").build();
        job1.getJobDataMap().put(PollingJob.RUNTIME_DEPENDENCY, "runtime dependency A");
        JobListener job1Listener = new PollingJobListener();
        sched.addJobListener(job1Listener, job1);
        sched.scheduleJobCronTrigger(job1, "0 * * * * ?");

        JobDetail job2 = newJob(PollingJob.class).withIdentity("job2", "group1").build();
        job2.getJobDataMap().put(PollingJob.RUNTIME_DEPENDENCY, "runtime dependency B");
        JobListener job2Listener = new PollingJobListener();
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
}
