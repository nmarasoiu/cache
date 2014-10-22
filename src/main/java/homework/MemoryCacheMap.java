package homework;

import java.util.*;

/**
 * Created by dnmaras on 10/22/14.
 */
public class MemoryCacheMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    private Map<K, V> dataMap;
    private Map<Object, Long> readAccessOrderedMap;
    private Map<Object, Long> writeAccessOrderedMap;
    private long acceptableStalenessMillis;

    public MemoryCacheMap(long acceptableStalenessMillis, long maxObjects) {
        this.acceptableStalenessMillis = acceptableStalenessMillis;
        readAccessOrderedMap = lruMap(maxObjects);
        writeAccessOrderedMap = new LinkedHashMap<>();
        dataMap = new HashMap<>();
    }

    <A, B> Map<A, B> lruMap(long maxObjects) {
        return new LinkedHashMap<A, B>(256, .75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<A, B> eldest) {
                while (size() >= maxObjects) {
                    MemoryCacheMap.this.remove(eldest.getKey());
                }
                return false;
            }
        };
    }

    interface Callable<V> extends java.util.concurrent.Callable<V> {
        V call();
    }

    @Override
    public V get(Object key) {
        return cacheOp(key, () -> dataMap.get(key), readAccessOrderedMap);
    }

    @Override
    public V put(K key, V value) {
        return cacheOp(key, () -> dataMap.put(key, value), writeAccessOrderedMap);
    }

    private V cacheOp(Object key, Callable<V> callable, Map<Object, Long> opAccessMap) {
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

    @Override
    public V remove(Object key) {
        V removedVal = dataMap.remove(key);
        readAccessOrderedMap.remove(key);
        writeAccessOrderedMap.remove(key);
        return removedVal;
    }

    @Override
    public void clear() {
        dataMap.clear();
        readAccessOrderedMap.clear();
        writeAccessOrderedMap.clear();
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    @Override
    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return dataMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return dataMap.containsValue(value);
    }

    @Override
    public Set<K> keySet() {
        return dataMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return dataMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return dataMap.entrySet();
    }
}
