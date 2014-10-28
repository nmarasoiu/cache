package homework.filesystem;

import homework.FunctionalCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static homework.filesystem.Utils.*;
import static homework.utils.ExceptionWrappingUtils.uncheckIOException;
import static homework.utils.StreamUtils.reify;

/**
 * Created by dnmaras on 10/21/14.
 */
public class ExtCacheOnFilesystem<K, V> extends FileSystemHashCache<K, V> implements FunctionalCache<K, V> {

    public ExtCacheOnFilesystem(Path basePath) {
        super(basePath);
    }
/*
    @Override
    public Stream<Map.Entry<K, V>> keyStream() {
        return uncheckIOException(() -> {
                    try (Stream<Path> pathsStream = Files.walk(basePath)) {
                        return reify(pathsStream
                                .filter(Files::isDirectory)
                                .filter(path -> path.getParent() != null)
                                .filter(path -> basePath.equals(path.getParent().getParent()))
                                .filter(path -> Files.exists(keyPathForEntry(path)))
                                .filter(path -> Files.exists(valuePathForEntry(path)))
                                .map(entryPath ->
                                        uncheckIOException(() -> {
                                            K k = (K) fromBytes(readKeyBytes(entryPath));
                                            return new AbstractMap.SimpleEntry<K, V>(k, get(k).get()) {
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
    }*/
}
