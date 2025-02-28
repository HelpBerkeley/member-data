package org.helpberkeley.memberdata;

import java.util.Random;

public class MockCacheChangeEvent implements Event {
    private final JobDependency dependencyKey;
    private final Cache cache;
    private int eventId;

    public MockCacheChangeEvent(Cache cache, JobDependency dependencyKey) {
        Random random = new Random();
        this.eventId = random.nextInt(10000);
        this.cache = cache;
        this.dependencyKey = dependencyKey;
    }

    public Integer getId() {
        return eventId;
    }

    public boolean hasOccurred() {
        try {
            return cache.hasChanged(dependencyKey);
        } catch (MemberDataException e) {
            System.err.println("No versions available to check for changes.");
            return false;
        }
    }
}
