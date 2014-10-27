package homework;

import homework.option.Option;
import homework.dto.Statistic;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/11/14.
 */
public interface ExtendedCache<K, V> extends Cache<K, V> {

    /**
     * Gets the value bundled with its last refresh timestamp.
     * The refresh timestamp == the time when it was last put.
     *
     * But the big advantage of this method, is that we are sure that if the returned value is != null,
     * then the entry exists in the Map (the entry mapping the key to null value).
     * So we circumvent the need for a containsKey method, with the efficiency advantage at least potential.
     * This method aims toward Scala's "Map.get" which is of type K -> Option[V].
     */
    default Option<Statistic<V>> getWrapped(K key) {
        throw new UnsupportedOperationException();
    }

    default  Option<V> getAsInScala(K key) {
        throw new UnsupportedOperationException();
    }

    default boolean remove(K k) {
        throw new UnsupportedOperationException();
    }

    default void put(K key, V value, Instant lastModTime) {
        throw new UnsupportedOperationException();
    }

    default Stream<Map.Entry<K, V>> entryStream() {
        throw new UnsupportedOperationException();
    }

}
