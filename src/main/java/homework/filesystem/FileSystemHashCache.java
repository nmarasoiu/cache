package homework.filesystem;

import homework.ExtendedCache;
import homework.option.Option;
import homework.dto.Statistic;
import homework.markers.NonThreadSafe;
import homework.option.OptionFactory;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static homework.filesystem.Utils.keyPathForEntry;
import static homework.filesystem.Utils.valuePathForEntry;
import static homework.utils.ExceptionWrappingUtils.uncheckIOException;
import static java.nio.file.Files.*;

/**
 * Makes a HashMap in the filesystem.
 * If the filesystem has not-nice limitations in directory entries, pls use ZipFileSystem (hope that is zip64)
 */
@NonThreadSafe
public class FileSystemHashCache<K, V> implements ExtendedCache<K, V> {
    private static final String LAST_ENTRY_NO_FILENAME = "last.txt";
    private static final String PREV_RW = "prevRW", PREV_W = "prevW";
    private static final String NEXT_RW = "prevRW", NEXT_W = "prevW";

    protected final Path basePath;
    protected final FileSystem fs;
    protected final Map<IndexType, Optional<Path>> tails = initialTails();

    private HashMap<IndexType, Optional<Path>> initialTails() {
        HashMap<IndexType, Optional<Path>> m = new HashMap<>();
        m.put(IndexType.ReadWrite, Optional.<Path>empty());
        m.put(IndexType.WriteOnly, Optional.<Path>empty());
        return m;
    }

    public FileSystemHashCache(Path basePath) {
        this.basePath = uncheckIOException(() -> createDirectories(basePath.normalize()));
        this.fs = this.basePath.getFileSystem();
    }

    @Override
    public V get(K key) {
        return getvOptional(key)
                .orElse(null);
    }

    @Override
    public Option<Statistic<V>> getWrapped(K key) {
        Key<K> keyRelated = new Key<K>(basePath, key);
        return getvOptional(key)
                .map(value -> OptionFactory.some(
                        new Statistic<V>(value, getLastModifiedDate(keyRelated))))
                .orElse(OptionFactory.missing());
    }

    private Optional<V> getvOptional(K key) {
        Key<K> keyRelated = new Key<K>(basePath, key);
        Optional<Path> entryDirOptional = keyRelated.findOptionalEntryDir();
        reindex(entryDirOptional);

        return entryDirOptional
                .map(Utils::valuePathForEntry)
                .map(this::readObjectFromFile);
    }

    private Instant getLastModifiedDate(Key<K> keyRelated) {
        return keyRelated.findOptionalEntryDir()
                .map(entry -> uncheckIOException(() ->
                        getLastModifiedTime(entry).toInstant()))
                .orElse(Instant.now());
    }

    private void reindex(Optional<Path> entryDirOptional) {
        if (entryDirOptional.isPresent()) {
            Path entryDir = entryDirOptional.get();
//            rearrangeLinkedList(entryDir);
        } else {

        }
    }

    private void rearrangeLinkedList(Path entryDir) {
        //put the entry at the end of r/w access queue: link the prev to the next, and replace the endPointer
        rearrangeLinkedList(entryDir, IndexType.ReadWrite);
    }

    private void rearrangeLinkedList(Path entryDir, IndexType indexType) {
        removeFromLinkedList(entryDir, indexType);
        tails.get(indexType).ifPresent(tail -> {

        });
//            tails.put(indexType, Optional.of())
    }

    @Override
    public void put(K key, V value) {
        Key<K> keyRelated = new Key<>(basePath, key);
        Optional<Path> maybeEntryDir = keyRelated.findOptionalEntryDir();
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

    private void removeFromLinkedList(Path entryDir, IndexType indexType) {
        Path previousDir = getSibling(entryDir, SiblingType.LEFT, indexType);
        Path nextDir = getSibling(entryDir, SiblingType.RIGHT, indexType);
        writeSibling(previousDir, SiblingType.RIGHT, indexType, nextDir);
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

    private void writeSibling(Path entryDir, SiblingType siblingType, IndexType indexType, Path siblingPath) {
        Path linkPath = pathForSiblingLink(entryDir, siblingType, indexType);
        uncheckIOException(() ->
                        write(linkPath, Collections.singleton(siblingPath.toString()))
        );
    }

    private Path getSibling(Path entryDir, SiblingType siblingType, IndexType indexType) {
        Path linkPath = pathForSiblingLink(entryDir, siblingType, indexType);
        return uncheckIOException(() ->
                        fs.getPath(readAllLines(linkPath).get(0))
        );
    }

    private Path pathForSiblingLink(Path entryDir, SiblingType siblingType, IndexType indexType) {
        return entryDir.resolve(filenameForSiblingKind(siblingType, indexType));
    }

    private String filenameForSiblingKind(SiblingType siblingType, IndexType indexType) {
        final String filename;
        if (siblingType == SiblingType.LEFT) {
            if (indexType == IndexType.ReadWrite) {
                filename = PREV_RW;
            } else if (indexType == IndexType.WriteOnly) {
                filename = PREV_W;
            } else {
                throw new IllegalArgumentException("indexType must be read/write or write only");
            }
        } else if (siblingType == SiblingType.RIGHT) {
            if (indexType == IndexType.ReadWrite) {
                filename = NEXT_RW;
            } else if (indexType == IndexType.WriteOnly) {
                filename = NEXT_W;
            } else {
                throw new IllegalArgumentException("indexType must be read/write or write only");
            }
        } else {
            throw new IllegalArgumentException("sibling must be left/prev or right/next");
        }
        return filename;
    }

}
