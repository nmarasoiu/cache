package homework.filesystem;

import homework.ExtendedCache;
import homework.markers.NonThreadSafe;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;

/**
 * Makes a HashMap in the filesystem.
 * If the filesystem has not-nice limitations in directory entries, pls use ZipFileSystem (hope that is zip64)
 */
@NonThreadSafe
public class FileSystemHashCache<K, V> implements ExtendedCache<K, V> {
    public static final String VALUE_FILENAME = "value.bin";
    public static final String KEY_FILENAME = "key.bin";
    public static final String LAST_ENTRY_NO_FILENAME = "last.txt";
    protected final Path basePath;

    public FileSystemHashCache(Path basePath) {
        this.basePath = rethrowIOExAsIoErr(() -> Files.createDirectories(basePath.normalize()));
    }

    @Override
    public V get(K key) {
        byte[] keyBytes = bytes(key);
        return getExistingValueFile(entryDir(keyBytes), keyBytes)
                .map(this::readObjectFromFile)
                .orElse(null);
    }

    @Override
    public void put(K key, V value) {
        byte[] keyBytes = bytes(key);
        Path hashDir = (entryDir(keyBytes));
        Optional<Path> maybeValueFile = getExistingValueFile(hashDir, keyBytes);
        rethrowIOExAsIoErr(() -> {
            final Path valuePath;
            if (maybeValueFile.isPresent()) {
                valuePath = maybeValueFile.get();
                Files.delete(valuePath);
            } else {
                Files.createDirectories(hashDir);
                Path entryDir = Files.createDirectories(nextDir(hashDir));
                write(keyBytes, entryDir.resolve(KEY_FILENAME));
                valuePath = entryDir.resolve(VALUE_FILENAME);
            }
            writeObjectToFile(value, valuePath);
        });
    }

    @Override
    public Optional<Instant> getLastModifiedMillis(K key) {
            Path entryDir = entryDir(bytes(key));
            if (Files.exists(entryDir))
                return rethrowIOExAsIoErr(()->
                        Optional.of(Files.getLastModifiedTime(entryDir).toInstant()));
            else
                return Optional.<Instant>empty();
    }

    protected byte[] keyBytes(Path entryDir) throws IOException {
        return Files.readAllBytes(entryDir.resolve(KEY_FILENAME));
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

    private Optional<Path> getExistingValueFile(Path hashDir, byte[] key) {
        return getEntryFor(hashDir, key)
                .map(entryDir -> entryDir.resolve(VALUE_FILENAME));
    }

    //todo: reorder member functions (methods), by their access level (public/protected/private in this order)
    protected Optional<Path> getEntryFor(Path hashDir, byte[] key) {
        return Files.exists(hashDir) ?
                rethrowIOExAsIoErr(() -> {
                    try (Stream<Path> list = Files.list(hashDir)) {
                        return list.filter(Files::isDirectory)
                                .filter(entryDir -> isThisMyKey(key, entryDir))
                                .findFirst();
                    }
                })
                : Optional.empty();
    }

    private Boolean isThisMyKey(byte[] bytes, Path entryDir) {
        return rethrowIOExAsIoErr(() -> (Arrays.equals(keyBytes(entryDir), bytes)));
    }

    protected Path entryDir(byte[] key) {
        return basePath.resolve(hash(key));
    }

    private String hash(byte[] key) {
        return toString(newDigester().digest((key)));
    }

    private String toString(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            //todo: cache the formatter and the digester factory below, etc
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private MessageDigest newDigester() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> byte[] bytes(T object) {
        return rethrowIOExAsIoErr(() -> {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutput out = new ObjectOutputStream(bos)) {
                out.writeObject(object);
                return bos.toByteArray();
            }
        });
    }
}
