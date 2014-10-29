package homework.option;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Scala Option wannabe.
 * In my opinion, Java 8 Optional and Streams got it wrong in some ways, and Optional was copied after Guava.
 * One case clearly not covered is when the value is null, but still the value "exists".
 * This is exactly what happens with a null value in a map.
 * And Scala Map has get method returning Option[V] which is simpler and more efficient than always checking for containsKey.
 * TODO: facade filesystem work so that:
 * 1. IOException is wrpped into unchecked
 * 2. the Optional returning methods return Option
 */
public abstract class Option<V> {
    public abstract V get();

    public abstract boolean isEmpty();

    public boolean isPresent() {
        return !isEmpty();
    }

    public abstract <U> Option<U> flatMap(Function<? super V, Option<U>> mapper);

    public abstract <U> Option<U> map(Function<? super V, ? extends U> mapper);

    Stream<V> asStream() {
        return (isPresent()) ? Stream.of(get()) : Stream.empty();
    }

    public static <V> Option<V> empty() {
        return MISSING;
    }

    public static <V> Option<V> none() {
        return MISSING;
    }

    public static <V> Option<V> some(V val) {
        return new OptionWithValue<V>(val);
    }

    public static <V> Option<V> of(V val) {
        return some(val);
    }

    public static <V> Option<V> from(Optional<V> optional) {
        return optional.isPresent() ? some(optional.get()) : MISSING;
    }

    public Option<V> ifPresent(Consumer<V> consumer) {
        if (isPresent())
            consumer.accept(get());
        return this;
    }

    public Option<V> orElse(Runnable task) {
        if (isEmpty())
            task.run();
        return this;
    }

    public V orElse(Supplier<V> valSupplier) {
        return Stream.concat(asStream(), Stream.generate(valSupplier))
                .findFirst().get();
    }

    private static final class OptionWithValue<V> extends Option<V> {

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

        @Override
        public <U> Option<U> flatMap(Function<? super V, Option<U>> mapper) {
            return mapper.apply(value);
        }

    }

    private static final Option MISSING = new Option() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Option flatMap(Function mapper) {
            return MISSING;
        }

        @Override
        public Option map(Function mapper) {
            return MISSING;
        }

        @Override
        public Object get() {
            throw new NoSuchElementException();
        }
    };
}
