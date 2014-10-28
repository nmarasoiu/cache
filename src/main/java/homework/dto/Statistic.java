package homework.dto;


import homework.option.Option;
import homework.option.OptionFactory;

import java.time.Instant;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/25/14.
 */
public final class Statistic<V>{
    private final V value;
    private final Stream<Instant> lastModifiedDate;

    public Statistic(V value, Instant lastModifiedDate) {
        this(value, Stream.of(lastModifiedDate));
    }

    public Statistic(V value) {
        this(value, Stream.empty());
    }

    private Statistic(V value, Stream<Instant> lazyInstant) {
        this.value = value;
        this.lastModifiedDate = lazyInstant;
    }

    public Option<Instant> getLastModifiedDate() {
        return OptionFactory.from(lastModifiedDate.findFirst());
    }

    public V getValue() {
        return value;
    }
}
