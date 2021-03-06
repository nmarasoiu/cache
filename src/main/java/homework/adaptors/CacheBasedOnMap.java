package homework.adaptors;

import homework.FCache;
import homework.markers.NonThreadSafe;
import homework.option.Option;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/15/14.
 */
@NonThreadSafe(comment = "Just as thread safe or not as the underlying map")
public class CacheBasedOnMap<K, V> implements FCache<K, V> {
    protected Map<K, V> underlyingMap;

    public CacheBasedOnMap(Map<K, V> map) {
        this.underlyingMap = map;
    }

    @Override
    public Option<V> get(K key) {
        V val = underlyingMap.get(key);
        return(val==null && !underlyingMap.containsKey(key))
                ? Option.none():Option.some(val);
    }
    @Override
    public Stream<K> keyStream() {
        return underlyingMap.keySet().stream();
    }

    @Override
    public void put(K key, V value) {
        underlyingMap.put(key, value);
    }

    @Override
    public boolean remove(K key) {
        boolean contains = underlyingMap.containsKey(key);
        underlyingMap.remove(key);
        return contains;
    }

    public long size() {
        return underlyingMap.size();
    }
}
