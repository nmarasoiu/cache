package homework;

import homework.option.Option;

import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/11/14.
 */
public interface FCache<K, V> {
    Option<V> get(K key);
    //todo docu;
    //todo, hai sa despartim put/stat de get/stat; sunt get si put cu stat corect impl in mem si fs si wrappers?
    void put(K key, V value);
    //ops below are just used in tests, so they dont need to be efficient
    boolean remove(K k);
    Stream<Stream<K>> lazyKeyStream();
}
