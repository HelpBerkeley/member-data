package org.helpberkeley.memberdata;

import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

public class SchedulerStubsTest {

    @Test
    public void stubsTest() {
        Cache cache = new MockCache(4);
        Scheduler scheduler = new MockScheduler(true, cache);

        JobDependency monitoredDependency = JobDependency.CONSUMER_REQUESTS;
        cache.storeEntry(monitoredDependency, "old request");

        JobDependency awaitedDependency = JobDependency.ONEKITCHEN_WORKFLOW_REQUEST;

        Job jobOne = new MockJob();
        Job jobTwo = new MockJob();
        Job jobOneKitchenWorkflow = new MockOneKitchenWorkflowJob();

        Event consumerRequestChange = new MockCacheChangeEvent(cache, monitoredDependency);
        Event oneKitchenWorkflowRequest = new MockCacheAddedEvent(cache, awaitedDependency);
        System.out.println("consumerRequestChange has occurred: " + consumerRequestChange.hasOccurred());
        System.out.println("oneKitchenWorkflowRequest has occurred: " + oneKitchenWorkflowRequest.hasOccurred());

        scheduler.scheduleJob(jobOne, "0 17 * * *");
        scheduler.scheduleJobWithEventTrigger(jobTwo, consumerRequestChange);
        scheduler.scheduleJobWithEventTrigger(jobOneKitchenWorkflow, oneKitchenWorkflowRequest);
        scheduler.start();
        System.out.println("Current Schedule: " + scheduler.getSchedule());

        cache.storeEntry(monitoredDependency, "new request");
        cache.storeEntry(awaitedDependency, "onekitchen workflow request");
        System.out.println("consumerRequestChange has occurred: " + consumerRequestChange.hasOccurred());
        System.out.println("oneKitchenWorkflowRequest has occurred: " + oneKitchenWorkflowRequest.hasOccurred());
        scheduler.stop();
        scheduler.destroy();

        JobDependency key = JobDependency.CONSUMER_REQUESTS;
        cache.storeEntry(key, "Sample data");

        List<CacheEntry> versions = cache.getAllVersions(key);
        System.out.println("Number of versions: " + versions.size());

        scheduler.stop();
        scheduler.destroy();
        cache.destroy();
    }

}
