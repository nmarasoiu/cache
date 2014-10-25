package homework;

import java.time.Instant;
import java.util.Optional;

/**
 * Created by dnmaras on 10/11/14.
 */
public interface Cache<K, V> {
    V get(K key);

    default void put(K key, V value) {
        put(key, value, Instant.now());
    }

    default void put(K key, V value, Instant lastModTime){
        throw new UnsupportedOperationException();
    }
    default Optional<Instant> getLastModifiedMillis(K key) {
        throw new UnsupportedOperationException();
    }
}
