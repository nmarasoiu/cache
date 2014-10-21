package homework;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;

/**
 * Created by nmarasoiu on 10/20/14.
 */
public class TestUtils {
    //externalize into configuration
    public static Path createRoot() {
        return createRoot(Paths.get("/tmp/tema/" + UUID.randomUUID()));
    }

    private static Path createRoot(Path home) {
        return rethrowIOExAsIoErr(() -> {
            if(Files.exists(home)) throw new IllegalStateException();
            return Files.createDirectories(home);
        });
    }

}
