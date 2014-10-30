package homework.filesystem;

import homework.FCache;
import homework.dto.Stat;
import homework.markers.NonThreadSafe;
import homework.option.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

import static homework.adaptors.IOUncheckingFiles.*;
import static homework.filesystem.Utils.*;
import static homework.utils.StreamUtils.systemClock;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

/**
 * Makes a HashMap in the filesystem.
 * If the filesystem has not-nice limitations in directory entries, pls use ZipFileSystem (hope that is zip64)
 */
@NonThreadSafe
public class FileSystemHashCache<K, V> implements FCache<K, Stat<V>> {
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
        Path normalizedBase = basePath.normalize();
        LOGGER.debug("Storing fs cache at {}", normalizedBase);
        this.basePath = createDirectories(normalizedBase);
    }

    @Override
    public Option<Stat<V>> get(K key) {
        Key<K> keyRelated = new Key<K>(basePath, key);
        return keyRelated.findOptionalEntryDir()
                .ifPresent((entryDir) -> readIndexer.touch(entryDir))
                .map((entryDir) -> Utils.valuePathForEntry(entryDir))
                .map((valuePath) -> new Stat<V>(
                                (V) readObjectFromFile(valuePath),
                                () -> getLastModifiedTime(valuePath).toInstant())
                );
    }

    @Override
    public void put(K key, Stat<V> value) {
        Key<K> keyRelated = new Key<>(basePath, key);
        Path entryDir = keyRelated.findOptionalEntryDir()
                .orElse(() -> {
                    createDirectories(keyRelated.hashDir());
                    Path newEntryDir = createDirectories(nextDir(keyRelated.hashDir()));
                    write(keyPathForEntry(newEntryDir), keyRelated.keyBytes());
                    return newEntryDir;
                });

        writeIndexer.touch(entryDir);
        writeObjectToFile(value.getValue(), valuePathForEntry(entryDir));
    }

    @Override
    public boolean remove(K key) {
        return new Key<>(basePath, key)
                .findOptionalEntryDir()
                .ifPresent((entryDir) ->
                        recursiveDelete(entryDir))
                .isPresent();
    }

    @Override
    public Stream<K> keyStream() {
        Stream<Path> hashDirs = listDirs(basePath);
        Stream<Path> entryDirs = hashDirs.flatMap(hashDir -> listDirs(hashDir));
        return entryDirs.map(
                entryDir -> (K) fromBytes(readKeyBytes(entryDir)));
    }

    private Path nextDir(Path hashDir) {
        Path file = hashDir.resolve(LAST_ENTRY_NO_FILENAME);
        final String nextInt = String.valueOf
                (exists(file) ? 1 + Long.parseLong(lines(file).findFirst().get()) : 1);
        write(file, Collections.singleton(nextInt));
        return hashDir.resolve(nextInt);
    }


    private Instant now() {
        return nowSource.next();
    }

    private void recursiveDelete(Path directory) {
        walkFileTree(directory, new SimpleFileVisitor<Path>() {
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
        });
    }


    private Stream<Path> listDirs(Path parent) {
        return list(parent).filter(f -> isDirectory(f));
    }

}
