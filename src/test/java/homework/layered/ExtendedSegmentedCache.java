package homework.layered;

import homework.ExtendedCache;
import homework.dto.CacheConfig;
import homework.filesystem.ExtendedCacheOnFilesystem;
import homework.memory.ExtendedMemoryCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ExtendedSegmentedCache<K, V> extends SegmentedCache<K, V, ExtendedCache<K, V>> implements ExtendedCache<K, V> {
    public ExtendedSegmentedCache(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    protected List<ExtendedCache<K, V>> createShardMaps() {
        List<ExtendedCache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            ExtendedCache<K, V> memCache = theMemCache();
            ExtendedCache<K, V> fsCache = new ExtendedCacheOnFilesystem<K, V>(cacheConfig.getBasePath().resolve(String.valueOf(i)));
            shards.add(new ExtendedLayeredCache<>(memCache, fsCache));
        }
        return Collections.unmodifiableList(shards);
    }

    @Override
    protected ExtendedCache<K, V> theMemCache() {
        return new ExtendedMemoryCache<K,V>(cacheConfig);
    }

    @Override
    public Stream<Map.Entry<K, V>> entryStream() {
        Stream<Map.Entry<K, V>> entryStream = getShards().stream().flatMap(ExtendedCache::entryStream);
        return reify(entryStream);
    }

    @Override
    public boolean remove(K k) {
        return shard(k).remove(k);
    }

}
