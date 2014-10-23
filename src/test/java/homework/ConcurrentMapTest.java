package homework;

import homework.adaptors.MapBasedOnCache;
import homework.layered.ExtendedSegmentedCache;
import org.mapdb.ConcurrentMapInterfaceTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import static homework.utils.TestUtils.createRoot;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ConcurrentMapTest extends ConcurrentMapInterfaceTest<String, String> {

    public static final List<String> STRINGS = Arrays.asList(UUID.randomUUID().toString().split("-"));

    @Override
    protected ConcurrentMap<String, String> makeEmptyMap() throws UnsupportedOperationException {
        return new ConcurrentMapBasedOnCache<>(new ExtendedSegmentedCache<>(createRoot(), 9090, 4000));
    }

    @Override
    protected ConcurrentMap<String,String> makePopulatedMap() throws UnsupportedOperationException {
        ConcurrentMap<String, String> m = makeEmptyMap();
        STRINGS.stream().forEach(s -> m.put(s, s));
        return m;
    }

    @Override
    protected String getKeyNotInPopulatedMap() throws UnsupportedOperationException {
        return "k";
    }

    @Override
    protected String getValueNotInPopulatedMap() throws UnsupportedOperationException {
        return "x";
    }

    @Override
    protected String getSecondValueNotInPopulatedMap() throws UnsupportedOperationException {
        return "y";
    }

    private static class ConcurrentMapBasedOnCache<K, V> extends MapBasedOnCache<K, V> implements ConcurrentMap<K,V>{
        public ConcurrentMapBasedOnCache(ExtendedCache<K, V> cache) {
            super(cache);
        }

        @Override
        public V replace(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V putIfAbsent(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            throw new UnsupportedOperationException();
        }
    }
}
