package homework.utils;

import homework.filesystem.FileSystemHashCache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by dnmaras on 10/25/14.
 */
public class BytesUtils {

    public static byte[] readKeyBytes(Path entryDir) throws IOException {
        return Files.readAllBytes(entryDir.resolve(FileSystemHashCache.KEY_FILENAME));
    }

}
