package homework.layered;

import homework.FunctionalCache;
import homework.StatAwareFuncCache;
import homework.dto.Statistic;
import homework.markers.ThreadSafe;
import homework.option.Option;
import homework.utils.LazyValue;
import homework.utils.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ThreadSafe
public class LayeredCache<K, V> implements FunctionalCache<K, V> {
    protected final StatAwareFuncCache<K, V> memCache;
    protected final StatAwareFuncCache<K, V> fsCache;
    protected final List<StatAwareFuncCache<K, V>> caches;

    public LayeredCache(StatAwareFuncCache<K, V> memCache, StatAwareFuncCache<K, V> fsCache) {
        this.memCache = memCache;
        this.fsCache = fsCache;
        caches = Arrays.asList(memCache, fsCache);
    }

    @Override
    public synchronized Option<V> get(K key) {
        class CacheAndCallback extends Pair<StatAwareFuncCache<K, V>, Consumer<Statistic<V>>> {
            LazyValue<Statistic<V>> cachedValueIfAny = new LazyValue<>(() -> getFirst().get(key));

            CacheAndCallback(StatAwareFuncCache<K, V> cache, Consumer<Statistic<V>> callback) {
                super(cache, callback);
            }

            Option<Statistic<V>> getCachedValueWithStatisticIfAny() {
                return cachedValueIfAny.getValue();
            }

            Statistic<V> getCachedValueWithStatistic() {
                return getCachedValueWithStatisticIfAny().get();
            }
        }
        Option<CacheAndCallback> cacheHitAndCallbackIfAny =
                Option.from(Stream.of(
                        new CacheAndCallback(memCache, statistic -> {
                        }), new CacheAndCallback(fsCache, statistic -> memCache.put(key, statistic)))
                        .filter(pair -> pair.getCachedValueWithStatisticIfAny().isPresent())
                        .findFirst());

        //execute callback if any
        cacheHitAndCallbackIfAny.ifPresent(pair -> pair.getSecond().accept(pair.getCachedValueWithStatistic()));

        //select just the cache hit if any (discard callback, unwrap)
        return cacheHitAndCallbackIfAny.map(pair -> pair.getCachedValueWithStatistic().getValue());
    }


    @Override
    public synchronized void put(K key, V value) {
        caches.forEach(cache -> cache.toSimpleCache().put(key, value));
    }

    @Override
    public synchronized boolean remove(K key) {
        return caches.stream()
                .map(cache -> cache.remove(key))
                .reduce(true, (removed1, removed2) -> /*removed1||*/removed2);
    }

    @Override
    public Stream<Stream<K>> lazyKeyStream() {
        return caches.stream()
                .flatMap(cache->cache.lazyKeyStream())
                .flatMap(keyStream -> keyStream)//flatten:Stream<Stream<K>>->Stream<K>
                .distinct()//deduplicate keys from the caches
                .map(key -> Stream.of(key));//wrap back to Stream<Stream>
    }
}
