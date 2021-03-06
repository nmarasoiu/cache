package homework.filesystem;

import java.nio.file.Path;

import static homework.adaptors.IOUncheckingFiles.readAllBytes;

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

    public static byte[] readKeyBytes(Path entryDir){
        return readAllBytes(entryDir.resolve(KEY_FILENAME));
    }

}
