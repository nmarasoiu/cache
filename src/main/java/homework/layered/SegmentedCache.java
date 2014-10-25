package homework.layered;


import homework.ExtendedCache;
import homework.dto.CacheConfig;
import homework.utils.CacheConfigBuilder;
import homework.markers.ThreadSafe;

import java.util.Collections;
import java.util.List;

/**
 * Created by dnmaras on 10/17/14.
 */
@ThreadSafe(comment = "Just as thread safe as the underlying shard' caches")
public abstract class SegmentedCache<K, V, CacheType extends ExtendedCache<K, V>> implements ExtendedCache<K, V> {
    protected static final int concurrencyFactor = 16;
    protected CacheConfig cacheConfig;
    protected List<CacheType> shards;

    public SegmentedCache(CacheConfig cacheConfig) {
        //todo: create a copy-constructor like functionality with the builder that we cannot forget copy-ing a property here
        this.cacheConfig = new CacheConfigBuilder()
                .setConcurrencyFactor(concurrencyFactor)
                .setMaxObjects(cacheConfig.getMaxObjects())
                .setBasePath(cacheConfig.getBasePath())
                .setMaxStalePeriod(cacheConfig.getMaxStalePeriod())
                .createCacheConfig();
        shards = Collections.unmodifiableList(createShardMaps());
    }

    @Override
    public V get(K key) {
        return shard(key).get(key);
    }

    @Override
    public void put(K key, V value) {
        shard(key).put((key), (value));
    }

    protected int modulo(K key) {
        return Math.abs(hash(key)) % concurrencyFactor;
    }

    protected int hash(K key) {
        return key == null ? 0 : (key).hashCode();
    }

    protected CacheType shard(K key) {
        return getShards().get(modulo(key));
    }

    abstract protected List<CacheType> createShardMaps();

    abstract protected CacheType theMemCache();

    public List<CacheType> getShards() {
        return shards;
    }
}
