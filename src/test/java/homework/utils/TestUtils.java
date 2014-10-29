package homework.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static homework.adaptors.IOUncheckingFiles.*;


/**
 * Created by nmarasoiu on 10/20/14.
 */
public class TestUtils {
    //externalize into configuration
    public static Path createRoot() {
        return createRoot(createTempDirectory(("tema_" + UUID.randomUUID())));
    }

    private static Path createRoot(Path home) {
            if (Files.exists(home) && list(home).findAny().isPresent()) throw new IllegalStateException();
            return createDirectories(home);
    }

}
