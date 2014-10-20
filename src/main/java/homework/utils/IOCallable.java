package homework.utils;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Restriction of Callable to just IOExceptions.
 */
public interface IOCallable<V> extends Callable<V> {
    V call() throws IOException;
}