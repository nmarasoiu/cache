package homework.layered;

import homework.FunctionalCache;
import homework.StatAwareFuncCache;
import homework.dto.Statistic;
import homework.markers.ThreadSafe;
import homework.option.Option;
import homework.utils.LazyValue;
import homework.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static homework.utils.StreamUtils.rangeStream;
import static homework.utils.StreamUtils.toList;

@ThreadSafe
public class LayeredCache<K, V, Cache extends StatAwareFuncCache<K, V>> implements FunctionalCache<K, V> {
    protected final List<Cache> caches;
    private final List<Pair<Cache, List<Cache>>> cacheWithUpperCaches;

    public LayeredCache(List<Cache> caches) {
        this.caches = new ArrayList<>(caches);
        cacheWithUpperCaches = createCacheWithUpperCaches();
    }

    @Override
    public synchronized Option<V> get(K key) {
        class CacheAndCallback extends Pair<Cache, Consumer<Statistic<V>>> {
            LazyValue<Statistic<V>> cachedValueIfAny = new LazyValue<>(() -> getFirst().get(key));

            CacheAndCallback(Cache cache, Consumer<Statistic<V>> callback) {
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
                Option.from(
                        cacheWithUpperCaches.stream()
                                .map(cacheWithUpperCaches ->
                                        new CacheAndCallback(cacheWithUpperCaches.getFirst(),
                                                statistic ->
                                                        cacheWithUpperCaches.getSecond().stream()
                                                                .forEach(cache -> cache.put(key, statistic))))
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
                .reduce(false, (removed1, removed2) -> removed1 || removed2);
    }

    @Override
    public Stream<Stream<K>> lazyKeyStream() {
        return caches.stream()
                .flatMap(cache -> cache.lazyKeyStream())
                .flatMap(keyStream -> keyStream)//flatten:Stream<Stream<K>>->Stream<K>
                .distinct()//deduplicate keys from the caches
                .map(key -> Stream.of(key));//wrap back to Stream<Stream>
    }

    private List<Pair<Cache, List<Cache>>> createCacheWithUpperCaches() {
        return toList(rangeStream(0, caches.size())
                .map(cacheIdx -> new Pair<>(caches.get(cacheIdx), caches.subList(0, cacheIdx))));
    }
}
