package homework;

import homework.dto.CacheConfigBuilder;
import homework.layered.RawSegmentedCache;
import homework.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

/**
 * Created by dnmaras on 10/13/14.
 */
public abstract class CacheTest<K, V> {
    private ExtendedCache<K, V> cache;

    @Before
    public void arrange() {
        cache = new RawSegmentedCache<>(
                new CacheConfigBuilder()
                        .setBasePath(TestUtils.createRoot())
                .setMaxObjects(9090)
                .setMaxStalePeriod(Duration.ofSeconds(5))
                .createCacheConfig());
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
