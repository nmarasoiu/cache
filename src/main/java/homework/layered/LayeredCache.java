package homework.layered;

import homework.Cache;
import homework.markers.ThreadSafe;

import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ThreadSafe
public class LayeredCache<K, V, CacheType extends Cache<K, V>> implements Cache<K, V> {
    protected final CacheType memCache;
    protected final CacheType fsCache;

    public LayeredCache(CacheType memCache, CacheType fsCache) {
        this.memCache = memCache;
        this.fsCache = fsCache;
    }

    @Override
    //we use synchronized, as the simplest; I would still consider locks for ability to let interrupt
    //read-write locks are not options because the "read" actually is also a potential write op
    public synchronized V get(K key) {
        V value = memCache.get(key);
        //if a null value exists in the map, it will still go to the filesystem; not a bug, but not intuitive and maybe a performance concern
        //to introduce a special wrapper Optional value or special containsKey method just for this case is not worth; maybe if it will be useful for time stamp strategies
        if (value == null) {
            value = fsCache.get(key);
            memCache.put(key, value,
                    getLastModTime(key));
        }
        return value;
    }

    private Instant getLastModTime(K key) {
        return fsCache.getLastModifiedMillis(key)
                .orElse(Instant.now());
    }

    @Override
    public synchronized void put(K key, V value) {
        memCache.put(key, value);
        fsCache.put(key, value);
    }

}
