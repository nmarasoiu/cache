package homework.layered;

import homework.Cache;
import homework.dto.CacheConfig;
import homework.filesystem.FileSystemHashCache;
import homework.memory.MemoryCache;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dnmaras on 10/19/14.
 */
public class RawSegmentedCache<K,V> extends SegmentedCache<K,V, Cache<K,V>> {
    public RawSegmentedCache(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    protected List<Cache<K, V>> createShardMaps() {
        List<Cache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            Cache<K, V> memCache = theMemCache();
            Cache<K, V> fsCache = new FileSystemHashCache<>(cacheConfig.getBasePath().resolve(String.valueOf(i)));
            shards.add(new LayeredCache<>(memCache, fsCache));
        }
        return Collections.unmodifiableList(shards);
    }


    @Override
    protected Cache<K,V> theMemCache() {
        return new MemoryCache<K, V>(cacheConfig);
    }
}
