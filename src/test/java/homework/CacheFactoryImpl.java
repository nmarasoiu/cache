package homework;

import homework.Cache;
import homework.CacheFactory;
import homework.SegmentedCache;
import homework.cacheDecorators.ExtendedCache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;

/**
 * Created by dnmaras on 10/13/14.
 */
public class CacheFactoryImpl<K, V> implements CacheFactory<K, V> {
    private Path root = createRoot(Paths.get("/tmp/tema"));

    private Path createRoot(Path home) {
        return rethrowIOExAsIoErr(() -> {
            try {
                Runtime.getRuntime().exec("rm -rf " + home).waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(Files.exists(home)) throw new IllegalStateException();
            return Files.createDirectories(home);
        });
    }

    @Override
    public ExtendedCache<K, V> create() {
        return new ExtendedSegmentedCache<>(root, 9090);
    }
}
