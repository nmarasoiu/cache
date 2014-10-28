package homework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static homework.utils.ExceptionWrappingUtils.uncheckIOException;

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
    public static  <V> Stream<V> streamFrom(IOCallable<V> callable) {
        return Stream.of(1).map(any -> uncheckIOException(callable));
    }


}
