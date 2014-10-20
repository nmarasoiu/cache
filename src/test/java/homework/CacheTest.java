package homework;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by dnmaras on 10/13/14.
 */
public abstract class CacheTest<K, V> {
    //I would use a IoC container to inject the cache
    private CacheFactory<K, V> cacheFactory = new CacheFactoryImpl<>();
    private Cache<K, V> cache;

    @Before
    public void arrange() {
        cache = cacheFactory.create();
    }

    @Test
    public void checkGetAfterPutReturnsValue() {
        checkPutAndGet(testKey(), testValue());
    }

    @Test
    public void checkGetAfterPutReturnsValueForNull() {
        checkPutAndGet(null, testValue());
    }

    @Test
    public void checkGetAfterPutReturnsNullValueForNull() {
        checkPutAndGet(null, null);
    }

    @Test
    public void checkGetAfterPutReturnsNull() {
        checkPutAndGet(testKey(), null);
    }

    private void checkPutAndGet(K key, V value) {
        cache.put(key, value);
        V actual = cache.get(key);
        assertEquals(value, actual);
    }

    abstract protected K testKey();

    abstract protected V testValue();
}
