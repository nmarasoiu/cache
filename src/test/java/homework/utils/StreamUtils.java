package homework.utils;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/21/14.
 */
public final class StreamUtils {
    private StreamUtils(){}

    public static <K, V> Stream<Map.Entry<K, V>> reify(Stream<Map.Entry<K, V>> entryStream) {
        return Stream.of(entryStream.toArray(Map.Entry[]::new));
    }

}
