package homework.option;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/25/14.
 */
public final class OptionFactory {
    private OptionFactory() {
    }

    public static final Option MISSING = new Option() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Object get() {
            throw new NoSuchElementException();
        }

        @Override
        public Option map(Function mapper) {
            return MISSING;
        }
    };

    public static <V> Option<V> none() {
        return MISSING;
    }

    public static <V> Option<V> some(V val) {
        return new OptionWithValue<V>(val);
    }

    public static <T> Option<T> from(Stream<T> stream) {
        return from(stream.findFirst());
    }
    public static <V> Option<V> from(Optional<V> optional) {
        return optional.isPresent() ? some(optional.get()) : MISSING;
    }
}
