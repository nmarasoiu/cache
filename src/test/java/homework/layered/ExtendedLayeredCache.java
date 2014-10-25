package homework.layered;

import homework.ExtendedCache;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ExtendedLayeredCache<K, V> extends LayeredCache<K, V, ExtendedCache<K, V>> implements ExtendedCache<K, V> {

    public ExtendedLayeredCache(ExtendedCache<K, V> memCache, ExtendedCache<K, V> fsCache) {
        super(memCache, fsCache);
    }

    @Override
    public synchronized Stream<Map.Entry<K, V>> entryStream() {
        return fsCache.entryStream();
    }

    @Override
    public synchronized boolean remove(K k) {
        memCache.remove(k);
        return fsCache.remove(k);
    }
}