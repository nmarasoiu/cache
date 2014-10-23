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
        chain(
                () -> memCache.put(key, value),
                () -> fsCache.put(key, value)
        );
    }

    protected <T> T underLock(Callable<T> callable) {
        lock.lock();
        try {
            return callable.call();
        } catch (Exception e) {
            throw maybeWrap(e);
        } finally {
            lock.unlock();
        }
    }

    private void chain(Runnable firstAttempt, Runnable secondAttempt) {

        Exception capturedEx = null;
        try {
            firstAttempt.run();
        } catch (Exception e) {
            capturedEx = e;
        } finally {
            //assume value != null for existing entries! The value will be a wrapper and will include statistics for access times
            try {
                secondAttempt.run();
            } catch (Exception e) {
                if (capturedEx != null) {
                    e.addSuppressed(capturedEx);
                }
                throw maybeWrap(e);
            }
        }
        if (capturedEx != null) {
            throw maybeWrap(capturedEx);
        }
    }

    private RuntimeException maybeWrap(Exception e) {
        return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
    }
}
