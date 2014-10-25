package homework.memory;

import homework.ExtendedCache;
import homework.dto.CacheConfig;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Entries are evicted from dataMap based on:
 * 1. The last-write-time should not be older than cacheConfig.getMaxStalePeriod.
 * 2. The number of entries should not exceed cacheConfig.getMaxObjects.
 * <p/>
 * The elements to evict for condition 2 are the ones with oldest read/write-time.
 */
public class MemoryCache<K, V> implements ExtendedCache<K, V> {
    protected final CacheConfig cacheConfig;
    protected final Map<K, V> dataMap;
    protected final Map<K, Instant> writeAccessOrderedMap;

    public MemoryCache(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        dataMap = lruMap(cacheConfig.getMaxObjects());
        writeAccessOrderedMap = new LinkedHashMap<>();
    }

    @Override
    public V get(K key) {
        //the current supplied key could be stale; removing in this case, so that the cache client can get a fresh version
        deleteStaleEntries();
        return dataMap.get(key);
    }

    @Override
    public void put(K key, V value, Instant lastModifiedTime) {
        dataMap.put(key, value);
        writeAccessOrderedMap.put(key, lastModifiedTime);
        deleteStaleEntries();
    }

    @Override
    public Optional<Instant> getLastModifiedMillis(K key) {
        Instant value = writeAccessOrderedMap.get(key);
        return value != null ? Optional.of(value) : Optional.empty();
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

    @Override
    public boolean remove(K key) {
        return dataMap.remove(key) != null
                | writeAccessOrderedMap.remove(key) != null;
    }

    <B> Map<K, B> lruMap(Number maxObjects) {
        long maxNoOfObjects = maxObjects.longValue();
        return new LinkedHashMap<K, B>(16, .75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, B> eldest) {
                while (size() >= maxNoOfObjects) {
                    MemoryCache.this.remove(eldest.getKey());
                }
                return false;
            }
        };
    }

}