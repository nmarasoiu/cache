package homework.layered;

import homework.ExtendedCache;
import homework.FunctionalCache;
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
public class ExtSegmentedCache<K, V> extends SegmentedCache<K, V, FunctionalCache<K, V>> implements ExtendedCache<K, V> {
    public ExtSegmentedCache(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    protected List<FunctionalCache<K, V>> createShardMaps() {
        List<FunctionalCache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            FunctionalCache<K, V> memCache = theMemCache();
            ExtCacheOnFilesystem<K, V> fsCache = new ExtCacheOnFilesystem<K, V>(cacheConfig.getBasePath().resolve(String.valueOf(i)));
            shards.add(new ExtLayeredCache<>(memCache, fsCache));
        }
        return Collections.unmodifiableList(shards);
    }

    @Override
    protected FunctionalCache<K, V> theMemCache() {
        return new ExtMemoryCache<K,V>(cacheConfig);
    }

    @Override
    public Stream<Map.Entry<K, V>> entryStream() {
        Stream<Map.Entry<K, V>> entryStream = getShards().stream()
                .flatMap(a -> ((ExtendedCache)a).entryStream());//todo
        return reify(entryStream);
    }

    @Override
    public boolean remove(K k) {
        return shard(k).remove(k);
    }

}