package homework.filesystem;

import homework.ExtendedCache;
import homework.markers.NonThreadSafe;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static homework.filesystem.Utils.keyPathForEntry;
import static homework.filesystem.Utils.valuePathForEntry;
import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.readAllLines;

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

    public FileSystemHashCache(Path basePath) {
        this.basePath = rethrowIOExAsIoErr(() -> Files.createDirectories(basePath.normalize()));
        this.fs = this.basePath.getFileSystem();
    }

    @Override
    public V get(K key) {
        Key<K> keyRelated = new Key<K>(basePath, key);
        Optional<Path> entryDirOptional = keyRelated.findOptionalEntryDir();
        if (entryDirOptional.isPresent()) {
            Path entryDir = entryDirOptional.get();
            //put the entry at the end of r/w access queue: link the prev to the next, and replace the endPointer
//            removeFromLinkedList(entryDir, IndexType.ReadWrite);

        } else {

        }
        return entryDirOptional
                .map(entryDir -> valuePathForEntry(entryDir))
                .map(this::readObjectFromFile)
                .orElse(null);
    }

    @Override
    public void put(K key, V value) {
        Key<K> keyRelated = new Key<>(basePath, key);
        Optional<Path> maybeEntryDir = keyRelated.findOptionalEntryDir();
        rethrowIOExAsIoErr(() -> {
            final Path valuePath;
            if (maybeEntryDir.isPresent()) {
                Path entryDir = maybeEntryDir.get();
                valuePath = valuePathForEntry(entryDir);
                Files.delete(valuePath);
            } else {
                Files.createDirectories(keyRelated.hashDir());
                Path entryDir = Files.createDirectories(nextDir(keyRelated.hashDir()));
                write(keyRelated.keyBytes(), keyPathForEntry(entryDir));
                valuePath = valuePathForEntry(entryDir);
            }
            writeObjectToFile(value, valuePath);
        });
    }

    @Override
    public Optional<Instant> getLastModifiedMillis(K key) {
        Key<K> keyRelated = new Key<>(basePath, key);
        return keyRelated.findOptionalEntryDir()
                .map(entry -> rethrowIOExAsIoErr(() ->
                        getLastModifiedTime(entry).toInstant()));
    }

    private void removeFromLinkedList(Path entryDir, IndexType indexType) {
        Path previousDir = getSibling(entryDir, SiblingType.LEFT, indexType);
        Path nextDir = getSibling(entryDir, SiblingType.RIGHT, indexType);
        writeSibling(previousDir, SiblingType.RIGHT, indexType, nextDir);
    }

    private Path nextDir(Path hashDir) {
        return rethrowIOExAsIoErr(() -> {
            Path file = hashDir.resolve(LAST_ENTRY_NO_FILENAME);
            final String nextInt = String.valueOf
                    (Files.exists(file) ? 1 + Long.parseLong(Files.lines(file).findFirst().get()) : 1);
            Files.write(file, Collections.singleton(nextInt));
            return hashDir.resolve(nextInt);
        });
    }

    private void write(byte[] source, Path path) throws IOException {
        try (InputStream keyStream = new ByteArrayInputStream((source))) {
            Files.copy(keyStream, path);
        }
    }

    private V readObjectFromFile(Path path) {
        return rethrowIOExAsIoErr(() -> {
            try (InputStream fileInStream = Files.newInputStream(path);
                 ObjectInput objectInStream = new ObjectInputStream(fileInStream)) {
                return (V) objectInStream.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void writeObjectToFile(Object value, Path path) {
        rethrowIOExAsIoErr(() -> {
            try (OutputStream fileOutStream = Files.newOutputStream(path);
                 ObjectOutput objOutStream = new ObjectOutputStream(fileOutStream)) {
                objOutStream.writeObject(value);
            }
        });

    }

    private void writeSibling(Path entryDir, SiblingType siblingType, IndexType indexType, Path siblingPath) {
        Path linkPath = pathForSiblingLink(entryDir, siblingType, indexType);
        rethrowIOExAsIoErr(() ->
                        Files.write(linkPath, Collections.singleton(siblingPath.toString()))
        );
    }

    private Path getSibling(Path entryDir, SiblingType siblingType, IndexType indexType) {
        Path linkPath = pathForSiblingLink(entryDir, siblingType, indexType);
        return rethrowIOExAsIoErr(() ->
                        fs.getPath(readAllLines(linkPath).get(0))
        );
    }

    private Path pathForSiblingLink(Path entryDir, SiblingType siblingType, IndexType indexType) {
        return entryDir.resolve(filenameForSiblingKind(siblingType, indexType));
    }

    private String filenameForSiblingKind(SiblingType sublingType, IndexType indexType) {
        final String filename;
        if (sublingType == SiblingType.LEFT) {
            if (indexType == IndexType.ReadWrite) {
                filename = PREV_RW;
            } else if (indexType == IndexType.WriteOnly) {
                filename = PREV_W;
            } else {
                throw new IllegalArgumentException("indexType must be read/write or write only");
            }
        } else if (sublingType == SiblingType.RIGHT) {
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
