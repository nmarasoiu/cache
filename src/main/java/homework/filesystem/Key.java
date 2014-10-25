package homework.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static homework.filesystem.Utils.readKeyBytes;
import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;

/**
 * Created by dnmaras on 10/25/14.
 */
public class Key<K> {
    //input
    private final Path basePath;
    private final K key;
    //lazy computable values
    private byte[] keyBytes;
    private String persistentHash;
    private Path hashDir;

    public Key(Path basePath, K key) {
        this.key = key;
        this.basePath = basePath;
    }

    public byte[] keyBytes() {
        if (keyBytes == null) {
            keyBytes = bytes(key);
        }
        return keyBytes;
    }

    private String persistentHash() {
        if (persistentHash == null) {
            persistentHash = toString(newDigester().digest(keyBytes()));
        }
        return persistentHash;
    }

    public Path hashDir() {
        if (hashDir == null) {
            hashDir = basePath.resolve(persistentHash());
        }
        return hashDir;
    }

    public Optional<Path> findOptionalEntryDir() {
        if (!Files.exists(hashDir())) {
            return Optional.empty();
        }
        return rethrowIOExAsIoErr(() -> {
            try (Stream<Path> list = Files.list(hashDir())) {
                return list.filter(Files::isDirectory)
                        .filter(this::isThisMyKey)
                        .findFirst();
            }
        });
    }

    private Boolean isThisMyKey(Path entryDir) {
        return rethrowIOExAsIoErr(() -> (Arrays.equals(readKeyBytes(entryDir), keyBytes())));
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
        return rethrowIOExAsIoErr(() -> {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutput out = new ObjectOutputStream(bos)) {
                out.writeObject(object);
                return bos.toByteArray();
            }
        });
    }
}