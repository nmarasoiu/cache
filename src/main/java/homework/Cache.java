package homework;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/25/14.
 */
public interface Cache<K, V> {
    V get(K key);

    boolean containsKey(K key);

    void put(K key, V value);

    boolean remove(K key);
}