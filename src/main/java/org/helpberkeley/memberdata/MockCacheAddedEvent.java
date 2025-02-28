package org.helpberkeley.memberdata;

import java.util.Random;

public class MockCacheAddedEvent implements Event {
    private final JobDependency dependencyKey;
    private final Cache cache;
    private int eventId;

    public MockCacheAddedEvent(Cache cache, JobDependency dependencyKey) {
        Random random = new Random();
        this.eventId = random.nextInt(10000);
        this.cache = cache;
        this.dependencyKey = dependencyKey;
    }

    public Integer getId() {
        return eventId;
    }

    public boolean hasOccurred() {
        return cache.containsKey(dependencyKey);
    }
}
