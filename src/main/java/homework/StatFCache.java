package homework;

import homework.adaptors.FCacheOverStatAware;
import homework.dto.Statistic;
import homework.option.Option;

/**
 * Created by dnmaras on 10/27/14.
 */
public interface StatFCache<K, V> extends FCache<K, Statistic<V>> {
    /**
     * Gets the value bundled with its last refresh timestamp.
     * The refresh timestamp == the time when it was last put.
     * <p/>
     * But the big advantage of this method, is that we are sure that if the returned value is != null,
     * then the entry exists in the Map (the entry mapping the key to null value).
     * So we circumvent the need for a containsKey method, with the efficiency advantage at least potential.
     * This method aims toward Scala's "Map.get" which is of type K -> Option[V].
     *
     * The declaration of this method would not be necessary but its better for documentation.
     */
    Option<Statistic<V>> get(K key);
}
