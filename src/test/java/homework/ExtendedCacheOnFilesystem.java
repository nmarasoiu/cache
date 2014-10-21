package homework;

import homework.utils.IORunnable;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;
import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/21/14.
 */
public class ExtendedCacheOnFilesystem<K,V> extends FileSystemHashCache<K,V> implements ExtendedCache<K,V> {
    public ExtendedCacheOnFilesystem(FileSystem fs, String path) {
        super(fs, path);
    }

    public ExtendedCacheOnFilesystem(Path basePath) {
        super(basePath);
    }
    @Override
    public Stream<Map.Entry<K, V>> entryStream() {
        return rethrowIOExAsIoErr(() -> {
                    try (Stream<Path> pathsStream = Files.walk(basePath)) {
                        return reify(
                                pathsStream.filter(Files::isDirectory)
                                        .filter(path -> path.getParent() != null)
                                        .filter(path -> basePath.equals(path.getParent().getParent()))
                                        .filter(path -> Files.exists(path.resolve(keyFilename)))
                                        .filter(path -> Files.exists(path.resolve(valFilename)))
                                        .map(entryPath -> rethrowIOExAsIoErr(() -> {
                                            K k = (K) fromBytes(keyBytes(entryPath));
                                            V v = get(k);
                                            return new AbstractMap.SimpleEntry<K, V>(k, v) {
                                                @Override
                                                public V setValue(V value) {
                                                    V old = get(k);
                                                    put(k, value);
                                                    return old;
                                                }
                                            };
                                        })));
                    }
                }
        );
    }

    @Override
    public boolean remove(K key) {
        byte[] b = bytes(key);
        Path hashDir = hashDir(b);
        Optional<Path> entryDirOpt = getEntryFor(hashDir, b);
        boolean exists = entryDirOpt.isPresent();
        if (exists)
            rethrowIOExAsIoErr((IORunnable) () -> {
                Path entryDir = entryDirOpt.get();
                Files.delete(entryDir.resolve(keyFilename));
                Files.delete(entryDir.resolve(valFilename));
                Files.delete(entryDir);
            });
        return exists;
    }

}
