package homework.layered;

import homework.FunctionalCache;
import homework.dto.CacheConfig;
import homework.filesystem.FileSystemHashCache;
import homework.memory.MemoryCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dnmaras on 10/19/14.
 */
public class RawSegmentedCache<K,V> extends SegmentedCache<K,V, FunctionalCache<K,V>> {
    public RawSegmentedCache(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    protected List<FunctionalCache<K, V>> createShardMaps() {
        List<FunctionalCache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            FunctionalCache<K, V> memCache = theMemCache();
            FunctionalCache<K, V> fsCache = new FileSystemHashCache<>(cacheConfig.getBasePath().resolve(String.valueOf(i)));
            shards.add(new LayeredCache<>(memCache, fsCache));
        }
        return Collections.unmodifiableList(shards);
    }


    @Override
    protected FunctionalCache<K,V> theMemCache() {
        return new MemoryCache<K, V>(cacheConfig);
    }
}
