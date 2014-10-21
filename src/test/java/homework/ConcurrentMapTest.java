package homework;

import org.mapdb.ConcurrentMapInterfaceTest;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Created by dnmaras on 10/19/14.
 */
public class ConcurrentMapTest extends ConcurrentMapInterfaceTest<String, String> {
    public ConcurrentMapTest() {
        super(
                true //allowsNullKeys ***
                , true// , allowsNullValues ***
                , true// , supportsPut,****
                , true// supportsRemove,*
                , true// supportsClear,
                , true// supportsIteratorRemove,
                , true// supportsEntrySetValue  ***
        );
    }

    @Override
    protected ConcurrentMap<String, String> makeEmptyMap() throws UnsupportedOperationException {
        ExtendedCache<String, String> cache = new CacheFactoryImpl<String, String>().create();
        return new ConcurrentMapBasedOnCache<String, String>(cache);
    }

    @Override
    protected ConcurrentMap<String,String> makePopulatedMap() throws UnsupportedOperationException {
        ConcurrentMap<String, String> m = makeEmptyMap();
        Arrays.asList(UUID.randomUUID().toString().split("-")).stream().forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                m.put(s, s);
            }
        });
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
