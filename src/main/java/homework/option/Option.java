package homework.option;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by dnmaras on 10/25/14.
 * Scala Option, but without all the machinery for monads composition like flatMap.
 * In my opinion, Java 8 Optional got it wrong, and was copied after Guava.
 * One case clearly not covered is when the value is null, but still the value "exists".
 * This is exactly what happens with a null value in a map.
 * And Scala Map has get method returning Option[V] which is simpler and more efficient than always checking for containsKey.
 * TODO: see if it is easy to add the monad methods: map, flatMap, forEach, asSet, etc.
 * TODO: or replace with existent Optional jdk8 if we are not using this Optional in any situation where we could wrapp null.
 */
public interface Option<V> {
    boolean isEmpty();
    V get();

    <U> Option<U> map(Function<? super V, ? extends U> mapper);
}
