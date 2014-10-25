package homework.option;

import java.util.NoSuchElementException;

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
        public Object value() {
            throw new NoSuchElementException();
        }
    };

    public static <V> Option<V> missing() {
        return MISSING;
    }

    public static <V> Option<V> some(V val) {
        return new OptionWithValue<V>(val);
    }
}
