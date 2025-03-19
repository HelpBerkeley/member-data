package org.helpberkeley.memberdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HBCacheSingleton implements Cache {
    private static final HBCacheSingleton INSTANCE = new HBCacheSingleton(2);
    private static final Logger LOGGER = LoggerFactory.getLogger(HBCacheSingleton.class);
    private int maxKeyDepth;
    private Map<JobDependency, LinkedList<CacheEntry>> cache;

    public HBCacheSingleton(int maxKeyDepth) {
        this.cache = new HashMap<>();
        this.maxKeyDepth = maxKeyDepth;
//        LOGGER.info("Cache created with maxKeyDepth: {}", maxKeyDepth);
    }

    public static Cache getInstance() {
        return INSTANCE;
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

        LOGGER.info("Stored entry for key: {} with data: '{}' at {}", key, data, ts);
        return ts;
    }

    @Override
    public boolean containsKey(JobDependency key) {
        return cache.containsKey(key);
    }

    /**
     * Remove old files from cache.
     */
    @Override
    public void clean() {
        LOGGER.info("Removing old files from cache.");
    }

    /**
     * Fetch latest files from Discourse.
     */
    @Override
    public void fetch() {
        this.storeEntry(JobDependency.CONSUMER_REQUESTS, "consumer request A");
        this.storeEntry(JobDependency.VOLUNTEER_REQUESTS, "volunteer request A");
        this.storeEntry(JobDependency.MEMBERDATA_ERRORS, "memberdata errors A");
        this.storeEntry(JobDependency.MEMBERDATA_REPORT, "memberdata report A");
        this.storeEntry(JobDependency.MEMBERDATA_RAW, "memberdata raw A");
        this.storeEntry(JobDependency.DRIVERS, "drivers A");
        this.storeEntry(JobDependency.DISPATCHERS, "dispatchers A");
        this.storeEntry(JobDependency.DAILY_WORKFLOW_REQUEST, "daily workflow request A");
        this.storeEntry(JobDependency.ONEKITCHEN_WORKFLOW_REQUEST, "onekitchen workflow request A");
        LOGGER.info("Fetching latest files from Discourse.");
    }

    @Override
    public List<CacheEntry> getAllVersions(JobDependency key) throws MemberDataException {
        LOGGER.info("Getting all versions for key: {}", key);
        return cache.get(key);
    }

    @Override
    public CacheEntry getLatestVersion(JobDependency key) throws MemberDataException {
        LOGGER.info("Getting latest version for key: {}", key);
        return cache.get(key).getLast();
    }

    @Override
    public CacheEntry getVersion(JobDependency key, int depth) throws MemberDataException {
        LOGGER.info("Getting version for key: {} at depth: {}", key, depth);
        return cache.get(key).get(depth);
    }

    @Override
    public CacheEntry getVersion(JobDependency key, Timestamp timestamp) throws MemberDataException {
        LOGGER.info("Getting version for key: {} at timestamp: {}", key, timestamp);
        for (CacheEntry entry: cache.get(key)) {
            if (entry.getTimestamp() == timestamp) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public int getNumVersions(JobDependency key) {
        LOGGER.info("Getting number of versions for key: {}", key);
        return cache.get(key).size();
    }

    @Override
    public boolean hasChanged(JobDependency key) throws MemberDataException {
        LOGGER.info("Checking if entry has changed for key: {}", key);
        return !cache.get(key).isEmpty();
    }

    @Override
    public void removeEntries(JobDependency key) {
        LOGGER.info("Removing entries for key: {}", key);
    }

    @Override
    public void destroy() {
        LOGGER.info("Destroying all cache entries");
    }

}
