package homework.adaptors;

import homework.FCache;
import homework.dto.Stat;
import homework.option.Option;

import java.util.stream.Stream;

/**
 * Created by nmarasoiu on 10/28/2014.
 */
public class StatAwareOverSimpleCache<K,V> implements FCache<K,Stat<V>> {
    private final FCache<K,V> underlying;

    public StatAwareOverSimpleCache(FCache<K, V> underlying) {
        this.underlying = underlying;
    }

    @Override
    public void put(K key, Stat<V> value) {
        underlying.put(key, value.getValue());
    }

    @Override
    public Option<Stat<V>> get(K key) {
        return underlying.get(key).map(v -> new Stat<>(v));
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
