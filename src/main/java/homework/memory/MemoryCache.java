package homework.memory;

import homework.Cache;
import homework.dto.CacheConfig;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by dnmaras on 10/22/14.
 */
public class MemoryCache<K, V> implements Cache<K, V> {
    protected final Map<K, V> dataMap;
    protected final Map<K, Instant> readAccessOrderedMap;
    protected final Map<K, Instant> writeAccessOrderedMap;
    protected final CacheConfig cacheConfig;

    public MemoryCache(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        readAccessOrderedMap = lruMap(cacheConfig.getMaxObjects());
        writeAccessOrderedMap = new LinkedHashMap<>();
        dataMap = new HashMap<>();
    }

    @Override
    public V get(K key) {
        return cacheOp(key,
                () -> dataMap.get(key),
                readAccessOrderedMap,
                Instant.now());
    }

    @Override
    public void put(K key, V value, Instant lastModifiedTime) {
        cacheOp(key,
                () -> dataMap.put(key, value),
                writeAccessOrderedMap,
                lastModifiedTime);
    }

    @Override
    public Optional<Instant> getLastModifiedMillis(K key) {
        Instant value = writeAccessOrderedMap.get(key);
        return value != null ? Optional.of(value) : Optional.empty();
    }

    interface Callable<V> extends java.util.concurrent.Callable<V> {
        V call();
    }

    private V cacheOp(K key,
                      Callable<V> callable,
                      Map<K, Instant> opAccessMap,
                      Instant lastModTime) {
        opAccessMap.put(key, lastModTime);
        deleteStaleEntries();
        return callable.call();
    }

    private void deleteStaleEntries() {
        writeAccessOrderedMap.entrySet().stream().findFirst()
                .ifPresent(entry -> {
                    Instant lastModifiedTime = entry.getValue();
                    Instant expiryTimeForEntry = lastModifiedTime.plus(
                            cacheConfig.getMaxStalePeriod());
                    if (expiryTimeForEntry.compareTo(Instant.now()) <= 0) {
                        remove(entry.getKey());
                        deleteStaleEntries();
                    }
                });
    }

    private void remove(K key) {
        dataMap.remove(key);
        readAccessOrderedMap.remove(key);
        writeAccessOrderedMap.remove(key);
    }

    <A extends K, B> Map<A, B> lruMap(Number maxObjects) {
        return new LinkedHashMap<A, B>(256, .75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<A, B> eldest) {
                while (size() >= maxObjects.longValue()) {
                    MemoryCache.this.remove(eldest.getKey());
                }
                return false;
            }
        };
    }

}