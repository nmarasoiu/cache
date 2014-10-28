package homework.adaptors;

import homework.FunctionalCache;
import homework.markers.NonThreadSafe;
import homework.option.Option;
import homework.option.OptionFactory;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/15/14.
 */
@NonThreadSafe(comment = "Just as thread safe or not as the underlying map")
public class CacheBasedOnMap<K, V> implements FunctionalCache<K, V> {
    protected Map<K, V> underlyingMap;

    public CacheBasedOnMap(Map<K, V> map) {
        this.underlyingMap = map;
    }

    @Override
    public Option<V> get(K key) {
        V val = underlyingMap.get(key);
        return(val==null && !underlyingMap.containsKey(key))
                ? OptionFactory.missing():OptionFactory.some(val);
    }
    @Override
    public Stream<Stream<K>> lazyKeyStream() {
        Stream<K> keyStream = underlyingMap.keySet().stream();
        return keyStream.map(key->Stream.of(key));
    }

    @Override
    public void put(K key, V value) {
        underlyingMap.put(key, value);
    }

    @Override
    public boolean remove(K k) {
        boolean contains = underlyingMap.containsKey(k);
        underlyingMap.remove(k);
        return contains;
    }

    public long size() {
        return underlyingMap.size();
    }
}
