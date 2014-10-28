package homework.memory;

import homework.FunctionalCache;
import homework.dto.CacheConfig;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/23/14.
 */
public class ExtMemoryCache<K, V> extends MemoryCache<K, V> implements FunctionalCache<K, V> {

    public ExtMemoryCache(CacheConfig cacheConfig) {
        super(cacheConfig);
    }


}
