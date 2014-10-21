package homework;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by dnmaras on 10/19/14.
 */
public class RawSegmentedCache<K,V> extends SegmentedCache<K,V,Cache<K,V>> {
    public RawSegmentedCache(Path basePath, double maxObjects) {
        super(basePath, maxObjects);
    }

    @Override
    protected List<Cache<K, V>> createShardMaps() {
        List<Cache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            Cache<K, V> memCache = new CacheBasedOnMap<>(lruMap());
            Cache<K, V> fsCache = new FileSystemHashCache<>(basePath.resolve(String.valueOf(i)));
            shards.add(new LayeredCache<>(memCache, fsCache));
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
}
