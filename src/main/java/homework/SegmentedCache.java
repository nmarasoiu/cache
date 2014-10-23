package homework;


import homework.markers.ThreadSafe;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by dnmaras on 10/17/14.
 */
@ThreadSafe(comment = "Just as thread safe as the underlying shard' caches")
public abstract class SegmentedCache<K, V, CacheType extends Cache<K, V>> implements Cache<K, V> {
    protected static final int concurrencyFactor = 16;
    protected List<CacheType> shards;
    protected long maxObjects;
    protected Path basePath;
    protected long stalenessMillis;

    public SegmentedCache(Path basePath, double maxObjects, long stalenessMillis) {
        this.basePath = basePath;
        this.stalenessMillis = stalenessMillis;
        this.maxObjects = (long) (.99 * maxObjects / concurrencyFactor);
        shards = createShardMaps();
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

    abstract protected CacheType theMemCache() ;

    public List<CacheType> getShards() {
        return shards;
    }
}
