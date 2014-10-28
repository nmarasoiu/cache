package homework;

import homework.dto.Statistic;
import homework.option.Option;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/11/14.
 */
public interface FunctionalCache<K, V> {

    void put(K key, V value);

    Option<V> get(K key);


    boolean remove(K k);
//todo: convert to Stream<K> ? the only reason it is stream of stream is because filesystem has overhead in deserializing and this could help ops like size() w/o accessing key value
    Stream<Stream<K>> lazyKeyStream();

}
