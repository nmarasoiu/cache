package homework.adaptors;

import homework.FunctionalCache;

import java.util.Map;
import java.util.stream.Stream;

import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/21/14.
 */
public class ExtCacheOnMap<K,V> extends CacheBasedOnMap<K, V> implements FunctionalCache<K, V> {

    public ExtCacheOnMap(Map<K, V> persistentMap) {
        super(persistentMap);
    }
}
