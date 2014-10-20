package homework.utils;

import java.io.IOException;

/**
 * A Runnable that can throw an IOException
 */
public interface IORunnable {
    void call() throws IOException;
}
