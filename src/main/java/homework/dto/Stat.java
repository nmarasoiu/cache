package homework.dto;

import java.util.Collections;
import java.util.Set;

/**
 * Converting a reference to a Set with 0 or 1 elements.
 * If the reference is null, the Set is empty.
 * If the reference is not null, the Set is a singleton.
 * Inspired by Guava Optional and Scala Option.
 * Also has some statistics helping eviction by access time.
 */
public final class Stat<V> {
    public static final Stat NONE = new Stat<Void>(null);

    private final Set<V> valueIfAny;
    private long readTimestampMillis, writeTimestampMillis;

    private Stat(V value) {
        if (value == null) {
            this.valueIfAny = Collections.<V>emptySet();
        } else {
            this.valueIfAny = Collections.singleton(value);
        }
    }

    public static <T> Stat<T> none() {
        return (Stat<T>) NONE;
    }

    public static <T> Stat<T> fromNullable(T key) {
        return new Stat<T>(key);
    }

    public static <T> Stat<T> nullToAbsent(Stat<T> valueStat) {
        return valueStat != null ? valueStat : Stat.<T>none();
    }

    public V toNullable() {
        return isEmpty() ? null : get();
    }


    public V get() {
        return asSet().iterator().next();
    }

    public Set<V> asSet() {
        return valueIfAny;
    }

    public boolean isDefined() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return asSet().isEmpty();
    }

    public void markAsRead(long refTimestampMillis) {
        readTimestampMillis = refTimestampMillis;
    }

    public void markAsWritten(long refTimestampMillis) {
        writeTimestampMillis = refTimestampMillis;
    }

    public boolean wasReadSince(long refTimestampMillis) {
        return readTimestampMillis >= refTimestampMillis;
    }

    public boolean wasWrittenToSince(long refTimestampMillis) {
        return writeTimestampMillis >= refTimestampMillis;
    }
}
