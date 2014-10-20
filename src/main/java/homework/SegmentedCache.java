package homework;


import homework.markers.ThreadSafe;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dnmaras on 10/17/14.
 */
@ThreadSafe(comment = "Just as thread safe as the underlying shard' caches")
public abstract class SegmentedCache<K, V, CacheType extends Cache<K, V>> implements Cache<K, V> {
    protected static final int concurrencyFactor = 16;
    protected List<CacheType> shards;
    protected double maxObjects;
    protected Path basePath;

    public SegmentedCache(Path basePath, double maxObjects) {
        this.basePath = basePath;
        this.maxObjects = .99 * maxObjects / concurrencyFactor;
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

    protected CacheType shard(K key) {
        return getShards().get(modulo(key));
    }

    protected int modulo(K key) {
        return hash(key) % concurrencyFactor;
    }

    protected int hash(K key) {
        return key == null ? 0 : (key).hashCode();
    }

    abstract protected List<CacheType> createShardMaps();

    public List<CacheType> getShards() {
        return shards;
    }
}
