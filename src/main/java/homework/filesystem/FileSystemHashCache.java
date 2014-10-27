package homework.filesystem;

import homework.FunctionalCache;
import homework.NowSource;
import homework.dto.Statistic;
import homework.markers.NonThreadSafe;
import homework.option.Option;
import homework.option.OptionFactory;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static homework.filesystem.Utils.keyPathForEntry;
import static homework.filesystem.Utils.valuePathForEntry;
import static homework.utils.ExceptionWrappingUtils.uncheckIOException;
import static java.nio.file.Files.*;

/**
 * Makes a HashMap in the filesystem.
 * If the filesystem has not-nice limitations in directory entries, pls use ZipFileSystem (hope that is zip64)
 */
@NonThreadSafe
public class FileSystemHashCache<K, V> implements FunctionalCache<K, V> {
    private static final String LAST_ENTRY_NO_FILENAME = "last.txt";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemHashCache.class);

    protected final Path basePath;
    private Indexer writeIndexer;
    private Indexer readIndexer;

    private NowSource nowSource;

    public FileSystemHashCache(Path basePath){
        this(basePath, Instant::now);
    }
    public FileSystemHashCache(Path basePath, NowSource nowSource) {
        this.nowSource = nowSource;
        this.basePath = uncheckIOException(() -> {
            readIndexer = new Indexer(IndexType.READ, basePath);
            writeIndexer = new Indexer(IndexType.WRITE, basePath);
            Path normalizedBase = basePath.normalize();
            LOGGER.debug("Storing fs cache at {}", normalizedBase);
            return createDirectories(normalizedBase);
        });
    }

    @Override
    public Option<Statistic<V>> getWrapped(K key) {
        Key<K> keyRelated = new Key<K>(basePath, key);
        return get(key)
                .map(value -> new Statistic<V>(value,
                        keyRelated.findOptionalEntryDir()
                                .map(entryPathToLastModifiedMapper())
                                .orElse(now())));
    }

    @Override
    public Option<V> get(K key) {
        Key<K> keyRelated = new Key<K>(basePath, key);
        Optional<Path> entryDirOptional = keyRelated.findOptionalEntryDir();
        if (entryDirOptional.isPresent()) {
            System.out.println(entryDirOptional.get().toAbsolutePath());
//            uncheckIOException(() -> readIndexer.touch(entryDirOptional.get()));
        }

        return entryDirOptional
                .map(Utils::valuePathForEntry)
                        //map into Option because reading value from file can give null value
                .map((valuePath) -> OptionFactory.some(readObjectFromFile(valuePath)))
                .orElse(OptionFactory.missing());
    }

    private Function<Path, Instant> entryPathToLastModifiedMapper() {
        return entry -> uncheckIOException(() ->
                getLastModifiedTime(entry).toInstant());
    }

    @Override
    public void put(K key, V value) {
        Key<K> keyRelated = new Key<>(basePath, key);
        Optional<Path> maybeEntryDir = keyRelated.findOptionalEntryDir();
//        writeIndexer.reindex(maybeEntryDir);
        uncheckIOException(() -> {
            final Path valuePath;
            if (maybeEntryDir.isPresent()) {
                Path entryDir = maybeEntryDir.get();
                valuePath = valuePathForEntry(entryDir);
                delete(valuePath);
            } else {
                createDirectories(keyRelated.hashDir());
                Path entryDir = createDirectories(nextDir(keyRelated.hashDir()));
                write(keyPathForEntry(entryDir), keyRelated.keyBytes());
                valuePath = valuePathForEntry(entryDir);
            }
            writeObjectToFile(value, valuePath);
        });
    }

    private Path nextDir(Path hashDir) {
        return uncheckIOException(() -> {
            Path file = hashDir.resolve(LAST_ENTRY_NO_FILENAME);
            final String nextInt = String.valueOf
                    (exists(file) ? 1 + Long.parseLong(lines(file).findFirst().get()) : 1);
            write(file, Collections.singleton(nextInt));
            return hashDir.resolve(nextInt);
        });
    }

    private V readObjectFromFile(Path path) {
        return uncheckIOException(() -> {
            try (InputStream fileInStream = newInputStream(path);
                 ObjectInput objectInStream = new ObjectInputStream(fileInStream)) {
                return (V) objectInStream.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void writeObjectToFile(Object value, Path path) {
        uncheckIOException(() -> {
            try (OutputStream fileOutStream = newOutputStream(path);
                 ObjectOutput objOutStream = new ObjectOutputStream(fileOutStream)) {
                objOutStream.writeObject(value);
            }
        });

    }

    private Instant now() {
        return nowSource.now();
    }
}
