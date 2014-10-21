package homework;

import homework.cacheDecorators.ExtendedCache;
import homework.markers.NonThreadSafe;
import homework.utils.IORunnable;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;
import static homework.utils.StreamUtils.reify;

/**
 * Makes a HashMap in the filesystem.
 * If the filesystem has not-nice limitations in directory entries, pls use ZipFileSystem (hope that is zip64)
 */
@NonThreadSafe
public class FileSystemHashCache<K, V> implements ExtendedCache<K, V> {
    public static final String valFilename = "value.bin";
    public static final String keyFilename = "key.bin";
    public static final String lastEntryNumberFilename = "last.txt";
    private final Path basePath;

    public FileSystemHashCache(FileSystem fs, String path) {
        this(fs.getPath(path));
    }

    public FileSystemHashCache(Path basePath) {
        this.basePath = rethrowIOExAsIoErr(() -> Files.createDirectories(basePath));
    }

    @Override
    public V get(K key) {
        byte[] keyBytes = bytes(key);
        Optional<Path> maybeValueFile = getExistingValueFile(hashDir(keyBytes), keyBytes);
        return maybeValueFile.map((Path valuePath) -> rethrowIOExAsIoErr(() -> {
            byte[] bytes = Files.readAllBytes(valuePath);
            return (V) fromBytes(bytes);
        })).orElse(null);
    }

    @Override
    public void put(K key, V value) {
        byte[] keyBytes = bytes(key);
        byte[] valueBytes = bytes(value);
        Path hashDir = (hashDir(keyBytes));
        Optional<Path> maybeValueFile = getExistingValueFile(hashDir, keyBytes);
        rethrowIOExAsIoErr(() -> {
            if (maybeValueFile.isPresent()) {
                Files.delete(maybeValueFile.get());
                write(valueBytes, maybeValueFile.get());
            } else {
                Files.createDirectories(hashDir);
                Path entryDir = Files.createDirectories(nextDir(hashDir));
                write(keyBytes, entryDir.resolve(keyFilename));
                write(valueBytes, entryDir.resolve(valFilename));
            }
        });
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

    private Path nextDir(Path hashDir) {
        return rethrowIOExAsIoErr(() -> {
            Path file = hashDir.resolve(lastEntryNumberFilename);
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

    private Optional<Path> getExistingValueFile(Path hashDir, byte[] key) {
        return getEntryFor(hashDir, key)
                .map(entryDir -> entryDir.resolve(valFilename));
    }

    private Optional<Path> getEntryFor(Path hashDir, byte[] key) {
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

    private byte[] keyBytes(Path entryDir) throws IOException {
        return Files.readAllBytes(entryDir.resolve(keyFilename));
    }

    private Path hashDir(byte[] key) {
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

    private Object fromBytes(byte[] bytes) {
        return rethrowIOExAsIoErr(() -> {
            if (bytes.length == 0) return null;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInput in = new ObjectInputStream(bis)) {
                return in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T> byte[] bytes(T object) {
        if (object == null) return new byte[]{};
        if (object instanceof byte[]) {
            return (byte[]) object;
        }
        if (!(object instanceof Serializable)) {
            throw new UncheckedIOException(new NotSerializableException(object.getClass().getName()));
        }
        return rethrowIOExAsIoErr(() -> {
            Serializable ser = (Serializable) object;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutput out = new ObjectOutputStream(bos)) {
                out.writeObject(ser);
                return bos.toByteArray();
            }
        });
    }

}
