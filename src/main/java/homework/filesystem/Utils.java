package homework.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by dnmaras on 10/25/14.
 */
public final class Utils {
    private static final String VALUE_FILENAME = "value.bin";
    private static final String KEY_FILENAME = "key.bin";
    private Utils(){}

    public static  Path keyPathForEntry(Path entryDir) {
        return entryDir.resolve(KEY_FILENAME);
    }

    public static  Path valuePathForEntry(Path entryDir) {
        return entryDir.resolve(VALUE_FILENAME);
    }

    public static byte[] readKeyBytes(Path entryDir) throws IOException {
        return Files.readAllBytes(entryDir.resolve(KEY_FILENAME));
    }

}
