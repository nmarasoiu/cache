package homework.layered;

import homework.Cache;
import homework.markers.ThreadSafe;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ThreadSafe
public class LayeredCache<K, V, CacheType extends Cache<K, V>> implements Cache<K, V> {
    protected Lock lock = new ReentrantLock();
    protected CacheType memCache;
    protected CacheType fsCache;

    public LayeredCache(CacheType memCache, CacheType fsCache) {
        this.memCache = memCache;
        this.fsCache = fsCache;
    }

    @Override
    public V get(K key) {
        return underLock(() -> {
            V value = memCache.get(key);
            if (value == null) {
                value = fsCache.get(key);
                memCache.put(key, value);
            }
            return value;
        });
    }

    @Override
    public void put(K key, V value) {
        underLock(() -> {
            memCache.put(key, value);
            fsCache.put(key, value);
            return null;
        });
    }

    protected <T> T underLock(Callable<T> callable) {
        lock.lock();
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

}
