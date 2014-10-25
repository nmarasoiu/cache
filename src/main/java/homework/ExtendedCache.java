package homework;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/11/14.
 */
public interface ExtendedCache<K, V> extends Cache<K, V> {

    default void put(K key, V value) {
        put(key, value, Instant.now());
    }

    default boolean remove(K k) {
        throw new UnsupportedOperationException();
    }

    default void put(K key, V value, Instant lastModTime) {
        throw new UnsupportedOperationException();
    }

    default Optional<Instant> getLastModifiedMillis(K key) {
        throw new UnsupportedOperationException();
    }

    default Stream<Map.Entry<K, V>> entryStream() {
        throw new UnsupportedOperationException();
    }
}
