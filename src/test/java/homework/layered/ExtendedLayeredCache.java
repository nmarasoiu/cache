package homework.layered;

import homework.ExtendedCache;
import homework.layered.LayeredCache;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ExtendedLayeredCache<K,V> extends LayeredCache<K,V, ExtendedCache<K,V>> implements ExtendedCache<K, V> {

    public ExtendedLayeredCache(ExtendedCache<K, V> memCache, ExtendedCache<K, V> fsCache) {
        super(memCache, fsCache);
    }

    @Override
    public Stream<Map.Entry<K, V>> entryStream() {
        return underLock(fsCache::entryStream);
    }

    @Override
    public boolean remove(K k) {
        return underLock(() -> fsCache.remove(k));
    }
}
