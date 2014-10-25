package homework.layered;

import homework.ExtendedCache;
import homework.dto.CacheConfig;
import homework.filesystem.FileSystemHashCache;
import homework.memory.MemoryCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dnmaras on 10/19/14.
 */
public class RawSegmentedCache<K,V> extends SegmentedCache<K,V, ExtendedCache<K,V>> {
    public RawSegmentedCache(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    protected List<ExtendedCache<K, V>> createShardMaps() {
        List<ExtendedCache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            ExtendedCache<K, V> memCache = theMemCache();
            ExtendedCache<K, V> fsCache = new FileSystemHashCache<>(cacheConfig.getBasePath().resolve(String.valueOf(i)));
            shards.add(new LayeredCache<>(memCache, fsCache));
        }
        return Collections.unmodifiableList(shards);
    }


    @Override
    protected ExtendedCache<K,V> theMemCache() {
        return new MemoryCache<K, V>(cacheConfig);
    }
}
