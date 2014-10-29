package homework.adaptors;

import homework.FCache;
import homework.dto.Stat;
import homework.option.Option;

import java.util.stream.Stream;

/**
 * Created by nmarasoiu on 10/28/2014.
 */
public class FCacheOverStatAware<K,V> implements FCache<K,V> {
    private final FCache<K, Stat<V>> underlying;

    public FCacheOverStatAware(FCache<K, Stat<V>> underlying) {
        this.underlying = underlying;
    }

    @Override
    public void put(K key, V value) {
        underlying.put(key, new Stat<V>(value));
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
