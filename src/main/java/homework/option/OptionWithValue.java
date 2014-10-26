package homework.option;

import java.util.function.Function;

/**
 * Created by dnmaras on 10/25/14.
 */
public class OptionWithValue<V> implements Option<V> {
    private final V value;

    public OptionWithValue(V value) {
        this.value = value;
    }


    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public V get() {
        return value;
    }

    @Override
    public <U> Option<U> map(Function<? super V, ? extends U> mapper) {
        return new OptionWithValue<>(mapper.apply(value));
    }
}
