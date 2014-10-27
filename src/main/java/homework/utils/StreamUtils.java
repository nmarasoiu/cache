package homework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/21/14.
 */
public final class StreamUtils {
    private StreamUtils(){}

    public static <T> Stream<T> reify(Stream<T> in) {
        List<T> list = new ArrayList<>();
        in.forEach(list::add);
        return list.stream();
    }

}
