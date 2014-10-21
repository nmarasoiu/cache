package homework;

import homework.cacheDecorators.CacheBasedOnMap;
import homework.cacheDecorators.ExtendedCache;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ExtendedSegmentedCache<K, V> extends SegmentedCache<K, V, ExtendedCache<K, V>> implements ExtendedCache<K, V> {
    public ExtendedSegmentedCache(Path basePath, double maxObjects) {
        super(basePath, maxObjects);
    }

    @Override
    protected List<ExtendedCache<K, V>> createShardMaps() {
        List<ExtendedCache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            ExtendedCache<K, V> memCache = new CacheBasedOnMap<>(lruMap());
            ExtendedCache<K, V> fsCache = new FileSystemHashCache<>(basePath.resolve(String.valueOf(i)));
            shards.add(new ExtendedLayeredCache<>(memCache, fsCache));
        }
        return Collections.unmodifiableList(shards);
    }

    private Map<K, V> lruMap() {
        return new LinkedHashMap<K, V>(256, .75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() >= maxObjects;
            }
        };
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
