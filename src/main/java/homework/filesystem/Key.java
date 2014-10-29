package homework.filesystem;

import homework.option.Option;
import homework.utils.LazyValue;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Stream;

import static homework.filesystem.Utils.readKeyBytes;
import static homework.option.Option.some;
import static homework.utils.ExceptionWrappingUtils.uncheckIOException;

/**
 * Created by dnmaras on 10/25/14.
 */
//todo extract Entry wrapping Key
public class Key<K> {
    //input
    private final Path basePath;
    private final K key;
    //lazy computable values
    private LazyValue<byte[]> keyBytes;
    private LazyValue<String> persistentHash;
    private LazyValue<Path> hashDir;

    public Key(Path basePath, K key) {
        this.key = key;
        this.basePath = basePath;
        keyBytes = new LazyValue<>(() -> some(bytes(key)));
        persistentHash = new LazyValue<>(() -> some(toString(newDigester().digest(keyBytes()))));
        hashDir = new LazyValue<>(() -> some(basePath.resolve(persistentHash())));
    }

    public byte[] keyBytes() {
        return keyBytes.get();
    }

    private String persistentHash() {
        return persistentHash.get();
    }

    public Path hashDir() {
        return hashDir.get();
    }

    public Option<Path> findOptionalEntryDir() {
        return Files.exists(hashDir()) ?
                uncheckIOException(() -> {
                    try (Stream<Path> list = Files.list(hashDir())) {
                        return Option.from(
                                list.filter((path) -> Files.isDirectory(path))
                                        .filter(this::isThisMyKey)
                                        .findFirst());
                    }
                }) : Option.empty();
    }

    private Boolean isThisMyKey(Path entryDir) {
        return uncheckIOException(() -> (Arrays.equals(readKeyBytes(entryDir), keyBytes())));
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

    private <T> byte[] bytes(T object) {
        return uncheckIOException(() -> {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutput out = new ObjectOutputStream(bos)) {
                out.writeObject(object);
                return bos.toByteArray();
            }
        });
    }
}