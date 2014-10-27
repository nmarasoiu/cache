package homework;

import homework.dto.Statistic;
import homework.option.Option;

import java.time.Instant;

/**
 * Created by dnmaras on 10/11/14.
 */
public interface FunctionalCache<K, V> {

    void put(K key, V value);

    Option<V> get(K key);

    //todo - check it
    default boolean remove(K key) {
        throw new UnsupportedOperationException();
    }
}
