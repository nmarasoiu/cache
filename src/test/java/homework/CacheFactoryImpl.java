package homework;

import java.nio.file.Path;

import static homework.TestUtils.createRoot;

/**
 * Created by dnmaras on 10/13/14.
 */
public class CacheFactoryImpl<K, V> implements CacheFactory<K, V> {
    private Path root = createRoot();

    @Override
    public ExtendedCache<K, V> create() {
        return new ExtendedSegmentedCache<>(root, 9090);
    }
}
