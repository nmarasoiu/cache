package homework.dto;


import homework.option.Option;
import homework.utils.LazyValue;

import java.time.Instant;
import java.util.function.Supplier;

import static homework.option.Option.some;

/**
 * Created by dnmaras on 10/25/14.
 */
public final class Stat<V> {
    private final V value;
    private final LazyValue<Instant> lastModifiedDate;

    public Stat(V value) {
        this(value, new LazyValue<>());
    }

    public Stat(V value, Supplier<Instant> lastModSupplier) {
        this.value = value;
        this.lastModifiedDate = new LazyValue<>(()->some(lastModSupplier.get()));
    }

    public Stat(V value, LazyValue<Instant> lastModifiedDate) {
        this.value = value;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Option<Instant> getLastModifiedDate() {
        return lastModifiedDate.getOption();
    }

    public V getValue() {
        return value;
    }
}
