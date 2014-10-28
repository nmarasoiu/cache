package homework;

import homework.option.Option;

import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/11/14.
 */
public interface FunctionalCache<K, V> {
    void put(K key, V value);
    Option<V> get(K key);
    boolean remove(K k);
    Stream<Stream<K>> lazyKeyStream();
}
