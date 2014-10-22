package homework;

import java.util.*;

/**
 * Created by dnmaras on 10/22/14.
 */
public class MemoryCacheMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    private Map<K, V> dataMap;
    private Map<Object, Long> readAccessOrderedMap;
    private Map<Object, Long> writeAccessOrderedMap;
    private Long currentTimestamp;
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
        return cacheOp(key, readAccessOrderedMap, () -> dataMap.get(key));
    }

    @Override
    public V put(K key, V value) {
        return cacheOp(key, writeAccessOrderedMap, () -> dataMap.put(key, value));
    }

    private V cacheOp(Object key, Map<Object, Long> accessMap, Callable<V> callable) {
        currentTimestamp = System.currentTimeMillis();
        deleteStaleEntries();
        accessMap.put(key, currentTimestamp);
        return callable.call();
    }

    private void deleteStaleEntries() {
        writeAccessOrderedMap
                .entrySet().stream()
                .filter(entry -> currentTimestamp - entry.getValue() > acceptableStalenessMillis)
                .findFirst()
                .ifPresent(entry -> {
                    remove(entry.getKey());
                    deleteStaleEntries();
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
