package homework.layered;

import homework.ExtendedFuncCache;
import homework.StatAwareFuncCache;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ExtLayeredCache<K, V> extends LayeredCache<K, V> implements ExtendedFuncCache<K, V> {

    public ExtLayeredCache(StatAwareFuncCache<K, V> memCache, StatAwareFuncCache<K, V> fsCache) {

        super(memCache, fsCache);
    }

    @Override
    public synchronized Stream<Map.Entry<K, V>> entryStream() {
        //todo: fix this
        return getFsCache().entryStream();
    }

    private ExtendedFuncCache getFsCache() {
        return ((ExtendedFuncCache)fsCache);
    }

    @Override
    public synchronized boolean remove(K k) {
//        memCache.remove(k);
        return getFsCache().remove(k);
    }
}
