package homework.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by dnmaras on 10/21/14.
 */
public final class StreamUtils {
    private StreamUtils() {
    }

    public static <T> Stream<T> reify(Stream<T> in) {
        return toList(in).stream();
    }

    public static <T> List<T> toList(Stream<T> in) {
        List<T> list = new ArrayList<>();
        in.forEach(list::add);
        return list;
    }

    public static Stream<Integer> rangeStream(int startInclusive, int endExclusive) {
        return stream(IntStream.range(startInclusive, endExclusive).iterator());
    }
    public static <T> Stream<T> stream(Iterator<T> iterator) {
        Spliterator<T> spliterator
                = Spliterators.spliteratorUnknownSize(iterator, 0);
        return StreamSupport.stream(spliterator, false);
    }

    public static Stream<Instant> systemClock() {
        return Stream.generate(() -> Instant.now());
    }

}
