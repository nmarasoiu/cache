package homework.cacheDecorators;

import homework.Cache;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/18/14.
 */
public interface ExtendedCache<K, V> extends Cache<K,V> {
    Stream<Map.Entry<K,V>> entryStream();
    boolean remove(K k);
}
