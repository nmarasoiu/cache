package homework.utils;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * In languages with Scala, this would be a single method, and T could be bound to Unit, meaning void.
 */
public class ExceptionWrappingUtils {
    public static <T> T rethrowIOExAsIoErr(IOCallable<T> callable) {
        try {
            return callable.call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void rethrowIOExAsIoErr(IORunnable runnable) {
        try {
            runnable.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


}
