package homework.memory;

import homework.ExtendedCache;
import homework.dto.CacheConfig;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/23/14.
 */
public class ExtendedMemoryCache<K, V> extends MemoryCache<K, V> implements ExtendedCache<K, V> {

    public ExtendedMemoryCache(CacheConfig cacheConfig) {
        super(cacheConfig);
    }

    @Override
    public Stream<Map.Entry<K, V>> entryStream() {
        return dataMap.entrySet().stream();
    }

    @Override
    public boolean remove(K k) {
        return dataMap.remove(k) != null;
    }
}
