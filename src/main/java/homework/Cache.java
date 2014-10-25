package homework;

/**
 * Created by dnmaras on 10/25/14.
 */
public interface Cache<K, V> {
    V get(K key);

    void put(K key, V value);
}