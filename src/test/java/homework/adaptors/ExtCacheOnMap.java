package homework.adaptors;

import homework.ExtendedCache;

import java.util.Map;
import java.util.stream.Stream;
import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/21/14.
 */
public class ExtCacheOnMap<K,V> extends CacheBasedOnMap<K, V> implements ExtendedCache<K, V> {

    public ExtCacheOnMap(Map<K, V> persistentMap) {
        super(persistentMap);
    }
    @Override
    public Stream<Map.Entry<K, V>> entryStream() {
        return reify(underlyingMap.entrySet().stream());
    }

    @Override
    public boolean remove(K k) {
        boolean contains = underlyingMap.containsKey(k);
        underlyingMap.remove(k);
        return contains;
    }
}
