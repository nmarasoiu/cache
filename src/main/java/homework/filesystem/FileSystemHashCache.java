package homework.filesystem;

import homework.StatAwareFuncCache;
import homework.dto.Statistic;
import homework.markers.NonThreadSafe;
import homework.option.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import static homework.filesystem.Utils.keyPathForEntry;
import static homework.filesystem.Utils.readKeyBytes;
import static homework.filesystem.Utils.valuePathForEntry;
import static homework.utils.ExceptionWrappingUtils.uncheckIOException;
import static homework.utils.StreamUtils.streamFrom;
import static homework.utils.StreamUtils.systemClock;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.lines;
import static java.nio.file.Files.list;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.write;

/**
 * Makes a HashMap in the filesystem.
 * If the filesystem has not-nice limitations in directory entries, pls use ZipFileSystem (hope that is zip64)
 */
@NonThreadSafe
public class FileSystemHashCache<K, V> implements StatAwareFuncCache<K, V> {
    private static final String LAST_ENTRY_NO_FILENAME = "last.txt";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemHashCache.class);

    protected final Path basePath;
    private final Indexer writeIndexer;
    private final Indexer readIndexer;

    private final Iterator<Instant> nowSource;

    public FileSystemHashCache(Path basePath) {
        this(basePath, systemClock());
    }

    public FileSystemHashCache(Path basePath, Stream<Instant> nowSource) {
        this.nowSource = nowSource.iterator();
        readIndexer = new Indexer(IndexType.READ, basePath);
        writeIndexer = new Indexer(IndexType.WRITE, basePath);
        this.basePath = uncheckIOException(() -> {
            Path normalizedBase = basePath.normalize();
            LOGGER.debug("Storing fs cache at {}", normalizedBase);
            return createDirectories(normalizedBase);
        });
    }

    @Override
    public Option<Statistic<V>> get(K key) {
        Key<K> keyRelated = new Key<K>(basePath, key);
        Option<Path> entryDirOption = keyRelated.findOptionalEntryDir();
        if (entryDirOption.isPresent()) {
            System.out.println(entryDirOption.get().toAbsolutePath());
//            uncheckIOException(() -> readIndexer.touch(entryDirOption.get()));
        }
        return entryDirOption
                .map((entryDir) -> Utils.valuePathForEntry(entryDir))
                .map((valuePath) -> readObjectFromFile(valuePath))
                .map(value ->
                        new Statistic<V>(value,
                                keyRelated.findOptionalEntryDir()
                                        .map(entryPathToLastModifiedMapper())
                                        .get()));
    }

    @Override
    public Stream<Stream<K>> lazyKeyStream() {
        return uncheckIOException(() -> {
            Stream<Path> hashDirs = listDirs(basePath);
            Stream<Path> entryDirs = hashDirs.flatMap(
                    hashDir -> uncheckIOException(() -> listDirs(hashDir)));
            return entryDirs.map(
                    entryDir -> streamFrom(() ->
                            (K) fromBytes(readKeyBytes(entryDir))));
        });
    }

    private Stream<Path> listDirs(Path parent) {
        return uncheckIOException(() -> list(parent).filter(f -> isDirectory(f)));
    }

    @Override
    public boolean remove(K key) {
        Key<K> keyRelated = new Key<>(basePath, key);
        Option<Path> entryDirOpt = keyRelated.findOptionalEntryDir();
        boolean exists = entryDirOpt.isPresent();
        if (exists) {
            uncheckIOException(() -> recursiveDelete(entryDirOpt.get()));
        }
        return exists;
    }

    private Function<Path, Instant> entryPathToLastModifiedMapper() {
        return entry -> uncheckIOException(() ->
                getLastModifiedTime(entry).toInstant());
    }

    @Override
    public void put(K key, Statistic<V> value) {
        Key<K> keyRelated = new Key<>(basePath, key);
        Path valuePath = keyRelated.findOptionalEntryDir()
                .ifPresent(entryDir -> {
//                    uncheckIOException(() -> recursiveDelete(entryDir));
                })
                .orElse(() -> uncheckIOException(() -> {
                    createDirectories(keyRelated.hashDir());
                    Path entryDir = createDirectories(nextDir(keyRelated.hashDir()));
                    write(keyPathForEntry(entryDir), keyRelated.keyBytes());
                    return valuePathForEntry(entryDir);
                }));
//        writeIndexer.reindex(maybeEntryDir);
        //todo, hai sa despartim put/stat de get/stat
        writeObjectToFile(value.getValue(), valuePath);
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
        return nowSource.next();
    }

    private void recursiveDelete(Path directory) {
        uncheckIOException(() ->
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                }));
    }

    private Object fromBytes(byte[] bytes) {
        return uncheckIOException(() -> {
            try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                return in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
