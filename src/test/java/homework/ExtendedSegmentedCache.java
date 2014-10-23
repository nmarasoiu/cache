package homework;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ExtendedSegmentedCache<K, V> extends SegmentedCache<K, V, ExtendedCache<K, V>> implements ExtendedCache<K, V> {
    public ExtendedSegmentedCache(Path basePath, double maxObjects, long stalenessMillis) {
        super(basePath, maxObjects, stalenessMillis);
    }

    @Override
    protected List<ExtendedCache<K, V>> createShardMaps() {
        List<ExtendedCache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            ExtendedCache<K, V> memCache = new ExtCacheOnMap<>(new MapBasedOnCache<>(theMemCache()));
            ExtendedCache<K, V> fsCache = new ExtendedCacheOnFilesystem<K, V>(basePath.resolve(String.valueOf(i)));
            shards.add(new ExtendedLayeredCache<>(memCache, fsCache));
        }
        return Collections.unmodifiableList(shards);
    }

    @Override
    protected ExtendedCache<K, V> theMemCache() {
        return new ExtendedMemoryCache(maxObjects, stalenessMillis);
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
