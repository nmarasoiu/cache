package homework;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dnmaras on 10/19/14.
 */
public class RawSegmentedCache<K,V> extends SegmentedCache<K,V,Cache<K,V>> {
    public RawSegmentedCache(Path basePath, double maxObjects, long stalenessMillis) {
        super(basePath, maxObjects, stalenessMillis);
    }

    @Override
    protected List<Cache<K, V>> createShardMaps() {
        List<Cache<K, V>> shards = new ArrayList<>(concurrencyFactor);
        for (int i = 0; i < concurrencyFactor; i++) {
            Cache<K, V> memCache = theMemCache();
            Cache<K, V> fsCache = new FileSystemHashCache<>(basePath.resolve(String.valueOf(i)));
            shards.add(new LayeredCache<>(memCache, fsCache));
        }
        return Collections.unmodifiableList(shards);
    }


    @Override
    protected Cache<K,V> theMemCache() {
        return new MemoryCache<K, V>(stalenessMillis, maxObjects);
    }
}
