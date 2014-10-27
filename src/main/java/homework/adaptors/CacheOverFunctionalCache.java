package homework.adaptors;

import homework.Cache;
import homework.FunctionalCache;
import homework.option.Option;

/**
 * Created by dnmaras on 10/27/14.
 */
public class CacheOverFunctionalCache<K, V> implements Cache<K, V> {
    protected FunctionalCache<K, V> underlyingCache;

    public CacheOverFunctionalCache(FunctionalCache<K, V> underlyingCache) {
        this.underlyingCache = underlyingCache;
    }

    @Override
    public V get(K key) {
        Option<V> optionalValue = underlyingCache.get(key);
        return optionalValue.isEmpty() ? null : optionalValue.get();
    }

    @Override
    public boolean containsKey(K key) {
        return underlyingCache.get(key).isPresent();
    }

    @Override
    public void put(K key, V value) {
        underlyingCache.put(key, value);
    }

    @Override
    public boolean remove(K key) {
        return underlyingCache.remove(key);
    }

}
