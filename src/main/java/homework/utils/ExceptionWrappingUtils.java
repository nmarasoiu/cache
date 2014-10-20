package homework.utils;

import java.io.IOError;
import java.io.IOException;

/**
 * In languages with Scala, this would be a single method, and T could be bound to Unit, meaning void.
 */
public class ExceptionWrappingUtils {
    public static <T> T rethrowIOExAsIoErr(IOCallable<T> callable) {
        try {
            return callable.call();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public static void rethrowIOExAsIoErr(IORunnable callable) {
        try {
            callable.call();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }


}
