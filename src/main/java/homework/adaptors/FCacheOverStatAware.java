package homework.adaptors;

import homework.FCache;
import homework.StatFCache;
import homework.dto.Statistic;
import homework.option.Option;

import java.util.stream.Stream;

/**
 * Created by nmarasoiu on 10/28/2014.
 */
public class FCacheOverStatAware<K,V> implements FCache<K,V> {
    private final StatFCache<K,V> underlying;

    public FCacheOverStatAware(StatFCache<K, V> underlying) {
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
