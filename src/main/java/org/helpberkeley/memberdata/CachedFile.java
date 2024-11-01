package org.helpberkeley.memberdata;

import java.sql.Timestamp;

public class CachedFile implements CacheEntry {
    private final String data;
    private final Timestamp timestamp;
    private final JobDependency key;

    public CachedFile(JobDependency key, String data, Timestamp timestamp) {
        this.key = key;
        this.data = data;
        this.timestamp = timestamp;
    }

    public JobDependency getKey() { return key; }

    public String getData() {
        return data;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}

