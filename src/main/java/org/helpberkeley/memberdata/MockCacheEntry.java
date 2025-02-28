package org.helpberkeley.memberdata;

import java.sql.Timestamp;

public class MockCacheEntry implements CacheEntry {
    private JobDependency key;
    private String data;
    private Timestamp timestamp;

    public MockCacheEntry(JobDependency key, String data, Timestamp timestamp) {
        this.key = key;
        this.data = data;
        this.timestamp = timestamp;
    }

    @Override
    public JobDependency getKey() {
        return key;
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }
}
