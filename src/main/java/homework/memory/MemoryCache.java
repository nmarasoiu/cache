package homework.memory;

import homework.StatAwareFuncCache;
import homework.adaptors.CacheBasedOnMap;
import homework.dto.CacheConfig;
import homework.dto.Statistic;
import homework.filesystem.IndexType;
import homework.option.Option;
import homework.utils.StreamUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static homework.utils.StreamUtils.systemClock;

/**
 * Entries are evicted from dataMap based on:
 * 1. The last-write-time should not be older than cacheConfig.getMaxStalePeriod.
 * 2. The number of entries should not exceed cacheConfig.getMaxObjects.
 * <p/>
 * The elements to evict for condition 2 are the ones with oldest read/write-time.
 */
public class MemoryCache<K, V> implements StatAwareFuncCache<K, V> {
    protected final CacheConfig cacheConfig;
    protected final Map<K, V> dataMap;
    private final CacheBasedOnMap<K, V> dataCache;
    protected final Map<IndexType, Map<K, Instant>> accessOrderedMap;
    private Iterator<Instant> nowSource;

    public MemoryCache(CacheConfig cacheConfig) {
        this(cacheConfig, systemClock());
    }

    //testing constructor, to simulate time
    MemoryCache(CacheConfig cacheConfig, Stream<Instant> nowSource) {
        this.nowSource = nowSource.iterator();
        this.cacheConfig = cacheConfig;
        //todo: do something with this! just one..
        dataMap =new HashMap<K, V>();
        dataCache = new CacheBasedOnMap<>(dataMap);
        accessOrderedMap = new HashMap<>();
        accessOrderedMap.put(IndexType.READ, new LinkedHashMap<>());
        accessOrderedMap.put(IndexType.WRITE, new LinkedHashMap<>());
    }

    @Override
    public Stream<Stream<K>> lazyKeyStream() {
        return dataMap.keySet().stream().map(key->Stream.of(key));
    }

    @Override
    public void put(K key, Statistic<V> stat) {
        //todo - is this the right place to inject now? i guess so, because unless it comes with a timestamp from filesystem using the explicit Statistic constructor, it means it is a normal value put, and that is a fresh one
        Instant lastModTimestamp = stat.getLastModifiedDate().orElse(nowSource.next());
        writeAccessOrderedMap().put(key, lastModTimestamp);
        dataMap.put(key, stat.getValue());
        maybeDoSomeEviction();
    }

    @Override
    public Option<Statistic<V>> get(K key) {

        return getVal(key).map(value ->
                new Statistic<V>(value,
                        writeAccessOrderedMap().get(key)));
    }

    public Option<V> getVal(K key) {
        readAccessOrderedMap().put(key, now());
        //the current supplied key could be stale; removing in this case, so that the cache client can get a fresh version
        maybeDoSomeEviction();
        return dataCache.get(key);
    }

    @Override
    public boolean remove(K key) {
        boolean contains = dataMap.containsKey(key);
        simpleRemove(key);
        return contains;
    }
    private void maybeDoSomeEviction() {
        //by stale we mean entries not "put" recently; we first eagerly evict too stale entries so that the cache client can go fetch them fresh
        Stream<K> staleEntries =
                writeAccessOrderedMap().entrySet().stream()
                        //takeWhile - no such method in Java8 (is in Scala); so I replace it with filter, but this would need optimization
                        .filter(this::isStale)
                        .map(entry -> entry.getKey());
        //now delete entries not recently "read"; if by any chance we evict all entries ever read and still have too many entries only "put", evict from those as well by access order
        Stream<K> accessOrderedStream =
                Stream.of(IndexType.READ, IndexType.WRITE)
                        .flatMap(indexType -> accessOrderedMap.get(indexType).keySet().stream());
        Stream<K> keysInEvictOrder =
                Stream.concat(staleEntries, accessOrderedStream)
//                .distinct()//todo: is this needed?
                        //to do check that limit(0) bypasses all stream generation at all & serial stream generation
                        .limit(Math.max(0, dataMap.size() - cacheConfig.getMaxObjects()));
        StreamUtils.reify(keysInEvictOrder).forEach(this::simpleRemove);
    }

    private boolean isStale(Map.Entry<K, Instant> entry) {
        Instant lastModifiedTime = entry.getValue();
        Instant expiryTimeForEntry = lastModifiedTime.plus(
                cacheConfig.getMaxStalePeriod());
        return expiryTimeForEntry.isBefore(now());
    }

    protected void simpleRemove(K key) {
        dataMap.remove(key);
        readAccessOrderedMap().remove(key);
        writeAccessOrderedMap().remove(key);
    }

    private Map<K, Instant> readAccessOrderedMap() {
        return accessOrderedMap.get(IndexType.WRITE);
    }

    private Map<K, Instant> writeAccessOrderedMap() {
        return accessOrderedMap.get(IndexType.READ);
    }

    private Instant now() {
        return nowSource.next();
    }
}