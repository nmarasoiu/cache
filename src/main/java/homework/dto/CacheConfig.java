package homework.dto;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Created by dnmaras on 10/25/14.
 */
public class CacheConfig {
    public Path getBasePath() {
        return basePath;
    }

    private Path basePath;
    private Number maxObjects;

    public Duration getMaxStalePeriod() {
        return maxStalePeriod;
    }

    public Number getMaxObjects() {
        return maxObjects;
    }

    //how much to stay in the cache without being (re)written/refreshed, before being evicted?
    private Duration maxStalePeriod;
    public CacheConfig(Number maxObjects, Path basePath, Duration maxStalePeriod) {
        this.basePath = basePath;
        this.maxObjects =  maxObjects;
        this.maxStalePeriod = maxStalePeriod;
    }

}
