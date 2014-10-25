package homework.filesystem;

import homework.ExtendedCache;
import homework.utils.IORunnable;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static homework.utils.BytesUtils.readKeyBytes;
import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;
import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/21/14.
 */
public class ExtendedCacheOnFilesystem<K, V> extends FileSystemHashCache<K, V> implements ExtendedCache<K, V> {

    public ExtendedCacheOnFilesystem(Path basePath) {
        super(basePath);
    }

    @Override
    public Stream<Map.Entry<K, V>> entryStream() {
        return rethrowIOExAsIoErr(() -> {
                    try (Stream<Path> pathsStream = Files.walk(basePath)) {
                        return reify(pathsStream
                                .filter(Files::isDirectory)
                                .filter(path -> path.getParent() != null)
                                .filter(path -> basePath.equals(path.getParent().getParent()))
                                .filter(path -> Files.exists(path.resolve(KEY_FILENAME)))
                                .filter(path -> Files.exists(path.resolve(VALUE_FILENAME)))
                                .map(entryPath -> rethrowIOExAsIoErr(() -> {
                                    K k = (K) fromBytes(readKeyBytes(entryPath));
                                    return new AbstractMap.SimpleEntry<K, V>(k, get(k)) {
                                        @Override
                                        public V setValue(V value) {
                                            put(k, value);
                                            return super.setValue(value);
                                        }
                                    };
                                })));
                    }
                }
        );
    }

    @Override
    public boolean remove(K key) {
        KeyDerivates<K> keyRelated = new KeyDerivates<>(basePath, key);
        Optional<Path> entryDirOpt = keyRelated.findOptionalEntryDir();
        boolean exists = entryDirOpt.isPresent();
        if (exists)
            rethrowIOExAsIoErr((IORunnable) () -> {
                Path entryDir = entryDirOpt.get();
                Files.delete(entryDir.resolve(KEY_FILENAME));
                Files.delete(entryDir.resolve(VALUE_FILENAME));
                Files.delete(entryDir);
            });
        return exists;
    }

    private Object fromBytes(byte[] bytes) {
        return rethrowIOExAsIoErr(() -> {
            try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                return in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
