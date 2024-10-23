package org.helpberkeley.memberdata;

public class cachedFile {
    private final String data;
    private final String timestamp;
    private final Cache.cacheKey cacheKey;

    public cachedFile(Cache.cacheKey cacheKey, String data, String timestamp) {
        this.cacheKey = cacheKey;
        this.data = data;
        this.timestamp = timestamp;
    }

    public Cache.cacheKey getCacheKey() {
        return cacheKey;
    }

    public String getData() {
        return data;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

