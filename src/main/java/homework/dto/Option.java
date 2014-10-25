package homework.dto;

import java.util.Set;

/**
 * Created by dnmaras on 10/25/14.
 * Scala Option, but without all the machinery for monads composition like flatMap.
 * In my opinion, Java 8 Optional got it wrong, and was copied after Guava.
 * One case clearly not covered is when the value is null, but still the value exists.
 * This is exactly what happens with a null value in a map.
 * And Scala Map has get method returning Option[V] which is simpler and more efficient than always checking for containsKey.
 */
public interface Option<V> {
    boolean isEmpty();
    V value();
}
