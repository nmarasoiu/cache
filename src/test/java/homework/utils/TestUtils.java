package homework.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static homework.utils.ExceptionWrappingUtils.uncheckIOException;

/**
 * Created by nmarasoiu on 10/20/14.
 */
public class TestUtils {
    //externalize into configuration
    public static Path createRoot() {
        return createRoot(Paths.get("/tmp/tema/" + UUID.randomUUID()));
    }

    private static Path createRoot(Path home) {
        return uncheckIOException(() -> {
            if(Files.exists(home)) throw new IllegalStateException();
            return Files.createDirectories(home);
        });
    }

}
