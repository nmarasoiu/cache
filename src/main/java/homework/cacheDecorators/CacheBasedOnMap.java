package homework.cacheDecorators;

import homework.markers.NonThreadSafe;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/15/14.
 */
@NonThreadSafe(comment = "Just as thread safe or not as the underlying map")
public class CacheBasedOnMap<K, V> implements ExtendedCache<K, V> {
    private Map<K, V> underlyingMap;

    public CacheBasedOnMap(Map<K, V> persistentMap) {
        this.underlyingMap = persistentMap;
    }

    @Override
    public V get(K key) {
        return underlyingMap.get(key);
    }

    @Override
    public void put(K key, V value) {
        underlyingMap.put(key, value);
    }

    @Override
    public Stream<Map.Entry<K, V>> entryStream() {
        return reify(StreamSupport.stream(underlyingMap.entrySet().spliterator(), false));
    }

    @Override
    public boolean remove(K k) {
        boolean contains = underlyingMap.containsKey(k);
        underlyingMap.remove(k);
        return contains;
    }
}
