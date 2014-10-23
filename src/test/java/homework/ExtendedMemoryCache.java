package homework;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/23/14.
 */
public class ExtendedMemoryCache<K, V> extends MemoryCache<K, V> implements ExtendedCache<K, V> {
    public ExtendedMemoryCache(long acceptableStalenessMillis, long maxObjects) {
        super(acceptableStalenessMillis, maxObjects);
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
