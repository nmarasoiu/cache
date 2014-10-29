package homework.layered;


import homework.FCache;
import homework.adaptors.FCacheOverStatAware;
import homework.dto.CacheConfig;
import homework.dto.Stat;
import homework.filesystem.FileSystemHashCache;
import homework.markers.ThreadSafe;
import homework.memory.MemoryCache;
import homework.option.Option;
import homework.utils.CacheConfigBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/17/14.
 */
@ThreadSafe(comment = "Just as thread safe as the underlying shard' caches")
public class SegmentedCache<K, V> implements FCache<K, V> {
    protected static final int concurrencyFactor = 16;
    protected CacheConfig cacheConfig;
    protected List<FCache<K, V>> shards;

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
    public Option<V> get(K key) {
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

    protected FCache<K, V> shard(K key) {
        return getShards().get(modulo(key));
    }

    protected List<FCache<K, V>> createShardMaps() {
        List<FCache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            FCache<K, Stat<V>> memCache = new MemoryCache<K, V>(cacheConfig);
            FCache<K, Stat<V>> fsCache = new FileSystemHashCache<>(
                    cacheConfig.getBasePath().resolve(String.valueOf(i)));
            shards.add(new FCacheOverStatAware<K, V>(new LayeredCache<>(
                    new ArrayList<>(Arrays.asList(memCache, fsCache)))));
        }
        return Collections.unmodifiableList(shards);
    }

    public List<FCache<K, V>> getShards() {
        return shards;
    }

    @Override
    public boolean remove(K k) {
        return shard(k).remove(k);
    }

    @Override
    public Stream<Stream<K>> lazyKeyStream() {
        return reify(shards.stream()
                .flatMap(shard -> shard.lazyKeyStream()));
    }
}
