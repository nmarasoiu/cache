package homework;

import java.time.Instant;

/**
 * Created by nmarasoiu on 10/27/14.
 */
public interface NowSource {
    Instant now();
}
