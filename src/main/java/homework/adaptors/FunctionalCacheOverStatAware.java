package homework.adaptors;

import homework.FunctionalCache;
import homework.StatAwareFuncCache;
import homework.dto.Statistic;
import homework.option.Option;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by nmarasoiu on 10/28/2014.
 */
public class FunctionalCacheOverStatAware<K,V> implements FunctionalCache<K,V> {
    private final StatAwareFuncCache<K,V> underlying;

    public FunctionalCacheOverStatAware(StatAwareFuncCache<K, V> underlying) {
        this.underlying = underlying;
    }

    @Override
    public void put(K key, V value) {
        underlying.put(key, new Statistic<V>(value));
    }

    @Override
    public Option<V> get(K key) {
        return underlying.get(key).map(stat -> stat.getValue());
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
