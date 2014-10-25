package homework.dto;

import java.nio.file.Path;
import java.time.Duration;

public class CacheConfigBuilder {
    private Path basePath;
    //how much to stay in the cache without being (re)written/refreshed, before being evicted?
    private Duration maxStalePeriod = Duration.ofHours(24);
    private Number maxObjects = Integer.MAX_VALUE;

    private int concurrencyFactor = 1;

    public CacheConfigBuilder setConcurrencyFactor(int concurrencyFactor) {
        this.concurrencyFactor = concurrencyFactor;
        return this;
    }

    public CacheConfigBuilder setMaxObjects(Number maxObjects) {
        this.maxObjects = maxObjects;
        return this;
    }

    public CacheConfigBuilder setBasePath(Path basePath) {
        this.basePath = basePath;
        return this;
    }

    public CacheConfigBuilder setMaxStalePeriod(Duration maxStalePeriod) {
        this.maxStalePeriod = maxStalePeriod;
        return this;
    }

    public CacheConfig createCacheConfig() {
        return new CacheConfig(maxObjects.longValue() / concurrencyFactor, basePath, maxStalePeriod);
    }
}