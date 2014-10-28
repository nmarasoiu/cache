package homework.layered;

import homework.FunctionalCache;
import homework.StatAwareFuncCache;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ExtLayeredCache<K, V> extends LayeredCache<K, V> implements FunctionalCache<K, V> {

    public ExtLayeredCache(StatAwareFuncCache<K, V> memCache, StatAwareFuncCache<K, V> fsCache) {

        super(memCache, fsCache);
    }

    private FunctionalCache getFsCache() {
        return ((FunctionalCache)fsCache);
    }

}
