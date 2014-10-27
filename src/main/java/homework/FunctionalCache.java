package homework;

import homework.dto.Statistic;
import homework.option.Option;

import java.time.Instant;

/**
 * Created by dnmaras on 10/11/14.
 */
public interface FunctionalCache<K, V> {

    void put(K key, V value);

    /**
     * Gets the value bundled with its last refresh timestamp.
     * The refresh timestamp == the time when it was last put.
     * <p/>
     * But the big advantage of this method, is that we are sure that if the returned value is != null,
     * then the entry exists in the Map (the entry mapping the key to null value).
     * So we circumvent the need for a containsKey method, with the efficiency advantage at least potential.
     * This method aims toward Scala's "Map.get" which is of type K -> Option[V].
     */
    default Option<Statistic<V>> getWrapped(K key) {
        throw new UnsupportedOperationException();
    }

    default Option<V> get(K key) {
        throw new UnsupportedOperationException();
    }

    default boolean remove(K k) {
        throw new UnsupportedOperationException();
    }

    default void put(K key, V value, Instant lastModTime) {
        throw new UnsupportedOperationException();
    }

}
