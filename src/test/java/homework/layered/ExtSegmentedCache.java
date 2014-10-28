package homework.layered;

import homework.ExtendedFuncCache;
import homework.FunctionalCache;
import homework.StatAwareFuncCache;
import homework.dto.CacheConfig;
import homework.filesystem.ExtCacheOnFilesystem;
import homework.memory.ExtMemoryCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ExtSegmentedCache<K, V> extends SegmentedCache<K, V, FunctionalCache<K, V>>
        implements ExtendedFuncCache<K, V> {
    public ExtSegmentedCache(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    protected List<FunctionalCache<K, V>> createShardMaps() {
        List<FunctionalCache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            StatAwareFuncCache<K, V> memCache = theMemCache();
            ExtCacheOnFilesystem<K, V> fsCache = new ExtCacheOnFilesystem<K, V>(cacheConfig.getBasePath().resolve(String.valueOf(i)));
            shards.add(new ExtLayeredCache<>(memCache, fsCache));
        }
        return Collections.unmodifiableList(shards);
    }

    @Override
    protected StatAwareFuncCache<K, V> theMemCache() {
        return new ExtMemoryCache<K,V>(cacheConfig);
    }

    @Override
    public Stream<Map.Entry<K, V>> keyStream() {
        Stream<Map.Entry<K, V>> entryStream = getShards().stream()
                .flatMap(a -> ((ExtendedFuncCache) a).keyStream());//todo
        return reify(entryStream);
    }

    @Override
    public boolean remove(K k) {
        FunctionalCache<K, V> shard = shard(k);
        //todo
        return ((ExtendedFuncCache
                )shard).remove(k);
    }

}
