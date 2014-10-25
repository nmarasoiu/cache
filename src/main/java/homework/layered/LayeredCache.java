package homework.layered;

import homework.ExtendedCache;
import homework.dto.Statistic;
import homework.markers.ThreadSafe;

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
        Statistic<V> statVal = memCache.getWrapped(key);
        if (statVal == null) {
            statVal = fsCache.getWrapped(key);
            if (statVal == null) {
                return null;
            }
            memCache.put(key, statVal.getValue(), statVal.getLastModifiedDate());
        }
        return statVal.getValue();
    }

    @Override
    public synchronized void put(K key, V value) {
        memCache.put(key, value);
        fsCache.put(key, value);
    }

}
