package homework.dto;


import homework.option.Option;
import homework.utils.LazyValue;

import java.time.Instant;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/25/14.
 */
public final class Statistic<V> {
    private final V value;
    private final LazyValue<Instant> lastModifiedDate;

    public Statistic(V value) {
        this(value, new LazyValue<>());
    }

    public Statistic(V value, Instant lastModifiedDate) {
        this(value, () -> lastModifiedDate);
    }

    private Statistic(V value, Supplier<Instant> lastModSupplier) {
        this.value = value;
        this.lastModifiedDate = new LazyValue<>(lastModSupplier);
    }

    public Statistic(V value, LazyValue<Instant> lastModifiedDate) {
        this.value = value;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Option<Instant> getLastModifiedDate() {
        return lastModifiedDate.getValue();
    }

    public V getValue() {
        return value;
    }
}
