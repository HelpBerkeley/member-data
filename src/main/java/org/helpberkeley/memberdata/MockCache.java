package org.helpberkeley.memberdata;

import java.util.*;
import java.sql.Timestamp;

public class MockCache implements Cache {
    private int maxKeyDepth;
    private Map<JobDependency, LinkedList<CacheEntry>> cache;

    public MockCache(int maxKeyDepth) {
        this.cache = new HashMap<>();
        this.maxKeyDepth = maxKeyDepth;
        System.out.println("Cache created with maxKeyDepth: " + maxKeyDepth);
    }

    @Override
    public Timestamp storeEntry(JobDependency key, String data) {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        MockCacheEntry newEntry = new MockCacheEntry(key, data, ts);

        if (cache.containsKey(key)) {
            cache.get(key).add(newEntry);
        } else {
            cache.put(key, new LinkedList<>(List.of(newEntry)));
        }

        System.out.println("Stored entry for key: " + key + " with data: '" + data + "' at " + ts);
        return ts;
    }

    @Override
    public boolean containsKey(JobDependency key) {
        return cache.containsKey(key);
    }

    @Override
    public List<CacheEntry> getAllVersions(JobDependency key) throws MemberDataException {
        System.out.println("Getting all versions for key: " + key);
        return cache.get(key);
    }

    @Override
    public CacheEntry getLatestVersion(JobDependency key) throws MemberDataException {
        System.out.println("Getting latest version for key: " + key);
        return cache.get(key).getLast();
    }

    @Override
    public CacheEntry getVersion(JobDependency key, int depth) throws MemberDataException {
        System.out.println("Getting version for key: " + key + " at depth: " + depth);
        return cache.get(key).get(depth);
    }

    @Override
    public CacheEntry getVersion(JobDependency key, Timestamp timestamp) throws MemberDataException {
        System.out.println("Getting version for key: " + key + " at timestamp: " + timestamp);
        for (CacheEntry entry: cache.get(key)) {
            if (entry.getTimestamp() == timestamp) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public int getNumVersions(JobDependency key) {
        System.out.println("Getting number of versions for key: " + key);
        return cache.get(key).size();
    }

    @Override
    public boolean hasChanged(JobDependency key) throws MemberDataException {
        System.out.println("Checking if entry has changed for key: " + key);
        return cache.get(key).size() > 1;
    }

    @Override
    public void removeEntries(JobDependency key) {
        System.out.println("Removing entries for key: " + key);
    }

    @Override
    public void destroy() {
        System.out.println("Destroying all cache entries");
    }
}