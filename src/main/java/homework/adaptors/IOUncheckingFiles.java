package homework.adaptors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * Adaptor over nio.Files (well, not per se adaptor, because these are static functions not instance that can wrap one another).
 * 1. Removes IOException from signatures (converts them to the runtime jdk UncheckedIOException.
 * 2. The Optional return values and/or arguments are made to be Option typed values.
 * This Option is a class i have made in this project inspired by Scala Option, which serves better our purpose with Maps than JDK8 Optional which is taken from Guava.
 * And that is because Optional assimilates "having a value" with "having a not null value", while in Option, "holding a value" and "if that value is null or not" are independent.
 * That allows us to work with Option in a monad composition style without worrying that a map to null value (for instance by de-serializing a null value) would make the Optional appear as not having any value instead of having a value, which happens to be the null ref.
 * This is helpful also because we work with many FunctionalCache implementations treating different aspects, adapting, decorating, etc.
 * And all can rely on get(key) returning an Option<V> meaning the answer to the 2 questions:
 * 1. does a mapping exists for the key? and
 * 2. what is the value of the mapping? (null being a valid value)
 */
public final class IOUncheckingFiles {
    private IOUncheckingFiles() {
    }

    public static Path createDirectories(Path dir, FileAttribute<?>... attrs) {
        return uncheckIOException(() -> Files.createDirectories(dir, attrs));
    }

    public static byte[] readAllBytes(Path path) {
        return uncheckIOException(() -> Files.readAllBytes(path));
    }

    public static FileTime getLastModifiedTime(Path path, LinkOption... options) {
        return uncheckIOException(() -> Files.getLastModifiedTime(path, options));
    }

    public static Path write(Path path, byte[] bytes, OpenOption... options) {
        return uncheckIOException(() -> Files.write(path, bytes, options));

    }

    public static Path write(Path path, Iterable<? extends CharSequence> lines,
                             OpenOption... options) {
        return uncheckIOException(() -> Files.write(path, lines, options
        ));
    }

    public static InputStream newInputStream(Path path, OpenOption... options) {
        return uncheckIOException(() -> Files.newInputStream(path, options));

    }

    public static OutputStream newOutputStream(Path path, OpenOption... options) {
        return uncheckIOException(() -> Files.newOutputStream(path, options));

    }

    public static Stream<String> lines(Path path) {
        return uncheckIOException(() -> Files.lines(path));

    }

    public static Stream<Path> list(Path dir) {
        return uncheckIOException(() -> Files.list(dir));
    }

    public static List<String> readAllLines(Path path) {
        return uncheckIOException(() -> Files.readAllLines(path));
    }

    public static boolean deleteIfExists(Path path) {
        return uncheckIOException(() -> Files.deleteIfExists(path));
    }

    public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor) {
        return uncheckIOException(() -> Files.walkFileTree(start, visitor));
    }

    interface IOCallable<V> {
        V call() throws IOException;
    }

    public static Path createTempDirectory(Path dir,
                                           String prefix,
                                           FileAttribute<?>... attrs) {
        return uncheckIOException(() -> Files.createTempDirectory(dir, prefix, attrs));
    }
    public static Path createTempDirectory(String prefix,
                                           FileAttribute<?>... attrs)
    {
        return uncheckIOException(() -> Files.createTempDirectory(prefix,attrs));
    }

    public static <T> byte[] bytes(T object) {
        return uncheckIOException(() -> {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutput out = new ObjectOutputStream(bos)) {
                out.writeObject(object);
                return bos.toByteArray();
            }
        });
    }

    public static Object fromBytes(byte[] bytes) {
        return uncheckIOException(() -> {
            try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                return in.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Object readObjectFromFile(Path path) {
        return uncheckIOException(() -> {
                    try (InputStream fileInStream = newInputStream(path);
                         ObjectInput objectInStream = new ObjectInputStream(fileInStream)) {
                        return objectInStream.readObject();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public static void writeObjectToFile(Object value, Path path) {
        uncheckIOException(() -> {
            try (OutputStream fileOutStream = newOutputStream(path);
                 ObjectOutput objOutStream = new ObjectOutputStream(fileOutStream)) {
                objOutStream.writeObject(value);
            }
            return null;
        });

    }

    // execute a lamda and convert the IOEXception to its unchecked equivalent
    public static <T> T uncheckIOException(IOCallable<T> callable) {
        try {
            return callable.call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
