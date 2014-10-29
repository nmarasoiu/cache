package homework;

import homework.option.Option;

import java.util.stream.Stream;

/**
 * A "functional programming" version of a cache interface, inspired from Scala Map.
 */
public interface FCache<K, V> extends CommonCache<K, V> {
    /**
     * Gets an Option indicating the existence of key - value mapping (entry) in the cache or its non existence.
     * In case the mapping is present, the value is wrapped in the Option and can be get.
     * The advantage of this method's signature returning Option,
     * is that we are sure that if the returned value is != null,
     * then the entry exists in the Map (the entry mapping the key to null value).
     * So we circumvent the need for a containsKey method, with the efficiency advantage at least potential.
     * This method aims toward Scala's "Map.get" which is of type K -> Option[V].
     *
     * @param key The key to look for in the cache.
     * @return The Option populated with the value, or the empty() Option when mapping not found.
     */
    Option<V> get(K key);

    //todo, hai sa despartim put/stat de get/stat; sunt get si put cu stat corect impl in mem si fs si wrappers?


    //ops below are just used in tests, so they dont need to be efficient

    /**
     * Similar to Map.remove.
     * This is for testing only and needs extraction somewhere else, but since the client only access Cache and not FCache it is ok for now.
     * @param key The key to look for in the cache.
     * @return true if the entry existed
     */
    boolean remove(K key);

    /**
     * Gets a stream of keys from the map. Stream is better if many large keys exist.
     * This is for testing only and needs extraction somewhere else, but since the client only access Cache and not FCache it is ok for now.
     * @return
     */
    Stream<K> keyStream();
}
