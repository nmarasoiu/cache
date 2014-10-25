package homework.memory;

import homework.ExtendedCache;
import homework.dto.CacheConfig;
import homework.dto.Statistic;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public Statistic<V> getWrapped(K key) {
        if (dataMap.containsKey(key))
            return new Statistic<V>(get(key), writeAccessOrderedMap.get(key));
        else
            return null;
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
        boolean contains = dataMap.containsKey(key);
        dataMap.remove(key);
        writeAccessOrderedMap.remove(key);
        return contains;
    }

    <B> Map<K, B> lruMap(Number maxObjects) {
        long maxNoOfObjects = maxObjects.longValue();
        //todo: make this a read-access time order (not read-write)
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