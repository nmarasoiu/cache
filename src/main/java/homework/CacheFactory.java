package homework;

/**
 * Created by dnmaras on 10/13/14.
 */
public interface CacheFactory<K, V> {
    Cache<K,V> create();
}
