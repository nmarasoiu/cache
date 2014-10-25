package homework.utils;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * In languages with Scala, this would be a single method, and T could be bound to Unit, meaning void.
 */
public class ExceptionWrappingUtils {
    // execute a lamda and convert the IOEXception to its unchecked equivalent
    public static <T> T uncheckIOException(IOCallable<T> callable) {
        try {
            return callable.call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void uncheckIOException(IORunnable runnable) {
        try {
            runnable.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
