package homework.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static homework.utils.ExceptionWrappingUtils.uncheckIOException;

/**
 * Created by nmarasoiu on 10/20/14.
 */
public class TestUtils {
    //externalize into configuration
    public static Path createRoot() {
        return uncheckIOException(() ->
                createRoot(Files.createTempDirectory("tema_" + UUID.randomUUID())));
    }

    private static Path createRoot(Path home) {
        return uncheckIOException(() -> {
            if (Files.exists(home) && Files.list(home).findAny().isPresent()) throw new IllegalStateException();
            return Files.createDirectories(home);
        });
    }

}
