package homework.layered;

import homework.FunctionalCache;
import homework.StatAwareFuncCache;
import homework.dto.Statistic;
import homework.markers.ThreadSafe;
import homework.option.Option;
import homework.utils.LazyValue;
import homework.utils.Pair;

import java.util.function.Consumer;
import java.util.stream.Stream;

@ThreadSafe
public class LayeredCache<K, V> implements FunctionalCache<K, V> {
    protected final StatAwareFuncCache<K, V> memCache;
    protected final StatAwareFuncCache<K, V> fsCache;

    public LayeredCache(StatAwareFuncCache<K, V> memCache, StatAwareFuncCache<K, V> fsCache) {
        this.memCache = memCache;
        this.fsCache = fsCache;
    }

    @Override
    public synchronized Option<V> get(K key) {
        Option<CacheAndCallback<K, V>> cacheHitAndCallbackIfAny = Option.from(
                Stream.of(
                        new CacheAndCallback<>(memCache, statistic -> {
                        }),
                        new CacheAndCallback<>(fsCache, statistic -> memCache.put(key, statistic))
                )
                        .filter(pair -> pair.getFirst().get(key).isPresent())
                        .findFirst());

        //execute callback if any
        cacheHitAndCallbackIfAny.ifPresent(pair -> pair.getSecond().accept(pair.getFirst().get(key).get()));

        //select just the cache hit if any (discard callback, unwrap)
        return cacheHitAndCallbackIfAny.map(pair -> pair.getFirst().get(key).get()).map(stat -> stat.getValue());
    }


    @Override
    public synchronized void put(K key, V value) {
        memCache.toSimpleCache().put(key, value);
        fsCache.toSimpleCache().put(key, value);
    }

    @Override
    public synchronized boolean remove(K k) {
        memCache.remove(k);
        return fsCache.remove(k);
    }

    @Override
    public Stream<Stream<K>> lazyKeyStream() {
        return fsCache.lazyKeyStream();
    }
}

class CacheAndCallback<K, V> extends Pair<StatAwareFuncCache<K, V>, Consumer<Statistic<V>>> {
    CacheAndCallback(StatAwareFuncCache<K, V> cache, Consumer<Statistic<V>> callback) {
        super(cache, callback);
    }
}