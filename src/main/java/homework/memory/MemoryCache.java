package homework.memory;

import homework.ExtendedCache;
import homework.dto.CacheConfig;
import homework.dto.Statistic;
import homework.filesystem.IndexType;
import homework.option.Option;
import homework.option.OptionFactory;

import java.time.Instant;
import java.util.HashMap;
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
    protected final Map<IndexType, Map<K, Instant>> accessOrderedMap;

    public MemoryCache(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        dataMap = new HashMap<>();
        accessOrderedMap = new LinkedHashMap<>();
        //order read then write is important, as we are iterating in this order when evicting
        accessOrderedMap.put(IndexType.READ, new LinkedHashMap<>());
        accessOrderedMap.put(IndexType.WRITE, new LinkedHashMap<>());
    }

    @Override
    public V get(K key) {
        readAccessOrderedMap().put(key, Instant.now());
        //the current supplied key could be stale; removing in this case, so that the cache client can get a fresh version
        maybeDoSomeEviction();
        return dataMap.get(key);
    }

    @Override
    public void put(K key, V value, Instant lastModifiedTime) {
        writeAccessOrderedMap().put(key, lastModifiedTime);
        dataMap.put(key, value);
        maybeDoSomeEviction();
    }

    @Override
    public Option<Statistic<V>> getWrapped(K key) {
        if (dataMap.containsKey(key))
            return OptionFactory.some(
                    new Statistic<V>(get(key),
                            writeAccessOrderedMap().get(key)));
        else
            return OptionFactory.missing();
    }

    private void maybeDoSomeEviction() {
        //by stale we mean entries not "put" recently; we first eagerly evict too stale entries so that the cache client can go fetch them fresh
        deleteStaleEntries();
        //now delete entries not recently "read"; if by any chance we evict all entries ever read and still have too many entries only "put", evict from those as well by access order
        long numberEntriesToEvict = dataMap.size() -
                cacheConfig.getMaxObjects();
        if (numberEntriesToEvict > 0) {
                    accessOrderedMap.values().stream()
                    .flatMap(accessMap -> accessMap.entrySet().stream())
                    .limit(numberEntriesToEvict)
                    .forEach(entry -> remove(entry.getKey()));
        }
    }

    private void deleteStaleEntries() {
        writeAccessOrderedMap().entrySet().stream().findFirst()
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
        writeAccessOrderedMap().remove(key);
        return contains;
    }

    private Map<K, Instant> readAccessOrderedMap() {
        return accessOrderedMap.get(IndexType.WRITE);
    }

    private Map<K, Instant> writeAccessOrderedMap() {
        return accessOrderedMap.get(IndexType.READ);
    }
}