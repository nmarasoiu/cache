package homework.adaptors;

import homework.FunctionalCache;
import homework.StatAwareFuncCache;
import homework.dto.Statistic;
import homework.option.Option;

import java.util.stream.Stream;

/**
 * Created by nmarasoiu on 10/28/2014.
 */
public class StatAwareOverSimpleCache<K,V> implements StatAwareFuncCache<K,V> {
    private final FunctionalCache<K,V> underlying;

    public StatAwareOverSimpleCache(FunctionalCache<K, V> underlying) {
        this.underlying = underlying;
    }

    @Override
    public void put(K key, Statistic<V> value) {
        underlying.put(key, value.getValue());
    }

    @Override
    public Option<Statistic<V>> get(K key) {
        return underlying.get(key).map(v -> new Statistic<>(v));
    }

    @Override
    public boolean remove(K k) {
        return underlying.remove(k);
    }

    @Override
    public Stream<Stream<K>> lazyKeyStream() {
        return underlying.lazyKeyStream();
    }
}
