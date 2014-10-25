package homework.dto;


import homework.option.Option;

import java.time.Instant;

/**
 * Created by dnmaras on 10/25/14.
 */
public final class Statistic<V> implements Option<V> {
    private V value;
    private Instant lastModifiedDate;

    public Statistic(V value, Instant lastModifiedDate) {
        this.value = value;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public V value() {
        return value;
    }

}
