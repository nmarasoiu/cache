package homework.layered;

import homework.ExtendedCache;
import homework.FunctionalCache;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ExtLayeredCache<K, V> extends LayeredCache<K, V> implements ExtendedCache<K, V> {

    public ExtLayeredCache(FunctionalCache<K, V> memCache, ExtendedCache<K, V> fsCache) {
        super(memCache, fsCache);
    }

    @Override
    public synchronized Stream<Map.Entry<K, V>> entryStream() {
        //todo: fix this
        return ((ExtendedCache)fsCache).entryStream();
    }

    @Override
    public synchronized boolean remove(K k) {
        memCache.remove(k);
        return fsCache.remove(k);
    }
}
