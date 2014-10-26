package homework.layered;

import homework.ExtendedCache;
import homework.dto.Statistic;
import homework.markers.ThreadSafe;
import homework.option.Option;

import java.time.Instant;

@ThreadSafe
public class LayeredCache<K, V> implements ExtendedCache<K, V> {
    protected final ExtendedCache<K, V> memCache;
    protected final ExtendedCache<K, V> fsCache;

    public LayeredCache(ExtendedCache<K, V> memCache, ExtendedCache<K, V> fsCache) {
        this.memCache = memCache;
        this.fsCache = fsCache;
    }

    @Override
    //we use synchronized, as the simplest; I would still consider locks for ability to let interrupt
    //read-write locks are not options because the "read" actually is also a potential write op
    public synchronized V get(K key) {
        Option<Statistic<V>> maybeValueWithStats = getStatisticOption(key);
        if (maybeValueWithStats.isEmpty()) {
            return null;
        }
        Statistic<V> stat = maybeValueWithStats.get();
        V value = stat.getValue();
        Instant lastModifiedDate = stat.getLastModifiedDate();
        memCache.put(key, value, lastModifiedDate);
        return value;
    }

    private Option<Statistic<V>> getStatisticOption(K key) {
        Option<Statistic<V>> maybeValueWithStats;
        maybeValueWithStats = memCache.getWrapped(key);
        if (maybeValueWithStats.isEmpty()) {
            maybeValueWithStats = fsCache.getWrapped(key);
        }
        return maybeValueWithStats;
    }

    @Override
    public synchronized void put(K key, V value) {
        memCache.put(key, value);
        fsCache.put(key, value);
    }

}
