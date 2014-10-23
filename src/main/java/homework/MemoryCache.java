package homework;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by dnmaras on 10/22/14.
 */
public class MemoryCache<K, V> implements Cache<K, V> {
    protected Map<K, V> dataMap;
    private Map<K, Long> readAccessOrderedMap;
    private Map<K, Long> writeAccessOrderedMap;
    private long acceptableStalenessMillis;

    public MemoryCache(long acceptableStalenessMillis, long maxObjects) {
        this.acceptableStalenessMillis = acceptableStalenessMillis;
        readAccessOrderedMap = lruMap(maxObjects);
        writeAccessOrderedMap = new LinkedHashMap<>();
        dataMap = new HashMap<>();
    }

    @Override
    public V get(K key) {
        return cacheOp(key, () -> dataMap.get(key), readAccessOrderedMap);
    }

    @Override
    public void put(K key, V value) {
        cacheOp(key, () -> dataMap.put(key, value), writeAccessOrderedMap);
    }

    interface Callable<V> extends java.util.concurrent.Callable<V> {
        V call();
    }

    private V cacheOp(K key, Callable<V> callable, Map<K, Long> opAccessMap) {
        long currentTimestamp = System.currentTimeMillis();
        opAccessMap.put(key, currentTimestamp);
        deleteStaleEntries(currentTimestamp);
        return callable.call();
    }

    private void deleteStaleEntries(long currentTimestamp) {
        writeAccessOrderedMap.entrySet().stream().findFirst()
                .ifPresent(entry -> {
                    if (currentTimestamp - entry.getValue() > acceptableStalenessMillis) {
                        remove(entry.getKey());
                        deleteStaleEntries(currentTimestamp);
                    }
                });
    }

    private void remove(K key) {
        dataMap.remove(key);
        readAccessOrderedMap.remove(key);
        writeAccessOrderedMap.remove(key);
    }

    <A extends K, B> Map<A, B> lruMap(long maxObjects) {
        return new LinkedHashMap<A, B>(256, .75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<A, B> eldest) {
                while (size() >= maxObjects) {
                    MemoryCache.this.remove(eldest.getKey());
                }
                return false;
            }
        };
    }

}