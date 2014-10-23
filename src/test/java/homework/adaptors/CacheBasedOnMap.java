package homework.adaptors;

import homework.Cache;
import homework.markers.NonThreadSafe;

import java.util.Map;

/**
 * Created by dnmaras on 10/15/14.
 */
@NonThreadSafe(comment = "Just as thread safe or not as the underlying map")
public class CacheBasedOnMap<K, V> implements Cache<K, V> {
    protected Map<K, V> underlyingMap;

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

}
