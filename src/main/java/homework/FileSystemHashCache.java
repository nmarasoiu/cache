package homework;

import homework.markers.NonThreadSafe;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;
import static homework.utils.StreamUtils.reify;

/**
 * Makes a HashMap in the filesystem.
 * If the filesystem has not-nice limitations in directory entries, pls use ZipFileSystem (hope that is zip64)
 */
@NonThreadSafe
public class FileSystemHashCache<K, V> implements Cache<K, V> {
    public static final String VALUE_FILENAME = "value.bin";
    public static final String KEY_FILENAME = "key.bin";
    public static final String LAST_ENTRY_NO_FILENAME = "last.txt";
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[]{};
    protected final Path basePath;

    public FileSystemHashCache(FileSystem fs, String path) {
        this(fs.getPath(path));
    }

    public FileSystemHashCache(Path basePath) {
        this.basePath = rethrowIOExAsIoErr(() -> Files.createDirectories(basePath));
    }

    @Override
    public V get(K key) {
        byte[] keyBytes = bytes(key);
        return getExistingValueFile(hashDir(keyBytes), keyBytes)
                .map((Path valuePath) -> readObjectFromFile(valuePath))
                .orElse(null);
    }

    @Override
    public void put(K key, V value) {
        byte[] keyBytes = bytes(key);
        Path hashDir = (hashDir(keyBytes));
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
            if (Files.size(path) == 0) {
                return null;
            }
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
            if (value == null) {
                Files.createFile(path);
            }
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

    Optional<Path> getEntryFor(Path hashDir, byte[] key) {
        return Files.exists(hashDir) ?
                rethrowIOExAsIoErr(() -> {
                    try (Stream<Path> list = Files.list(hashDir)) {
                        return reify(list.filter(Files::isDirectory)
                                .filter(entryDir -> isThisMyKey(key, entryDir))
                                .findFirst());
                    }
                })
                : Optional.empty();
    }

    private Boolean isThisMyKey(byte[] bytes, Path entryDir) {
        return rethrowIOExAsIoErr(() -> (Arrays.equals(keyBytes(entryDir), bytes)));
    }

    byte[] keyBytes(Path entryDir) throws IOException {
        return Files.readAllBytes(entryDir.resolve(KEY_FILENAME));
    }

    Path hashDir(byte[] key) {
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

    <T> byte[] bytes(T object) {
        if (object == null) {
            return EMPTY_BYTE_ARRAY;
        } else if (object instanceof byte[]) {
            return (byte[]) object;
        } else
            return rethrowIOExAsIoErr(() -> {
                if (!(object instanceof Serializable)) {
                    throw new NotSerializableException(object.getClass().getName());
                }
                Serializable ser = (Serializable) object;
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     ObjectOutput out = new ObjectOutputStream(bos)) {
                    out.writeObject(ser);
                    return bos.toByteArray();
                }
            });
    }

}
