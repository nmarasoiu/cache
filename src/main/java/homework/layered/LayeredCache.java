package homework.layered;

import homework.FCache;
import homework.StatFCache;
import homework.dto.Statistic;
import homework.markers.ThreadSafe;
import homework.option.Option;
import homework.utils.LazyValue;
import homework.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static homework.utils.StreamUtils.rangeStream;
import static homework.utils.StreamUtils.toList;

@ThreadSafe
public class LayeredCache<K, V> implements StatFCache<K, V> {
    protected final List<StatFCache<K, V>> caches;
    private final List<Pair<StatFCache<K, V>, List<StatFCache<K, V>>>> cacheWithUpperCaches;

    public LayeredCache(List<StatFCache<K, V>> caches) {
        this.caches = new ArrayList<>(caches);
        cacheWithUpperCaches = createCacheWithUpperCaches();
    }

    @Override
    public synchronized Option<Statistic<V>> get(K key) {
        class CacheAndCallback extends Pair<StatFCache<K, V>, Consumer<Statistic<V>>> {
            LazyValue<Statistic<V>> cachedValueIfAny = new LazyValue<>(() -> getFirst().get(key));

            CacheAndCallback(StatFCache<K, V> cache, Consumer<Statistic<V>> callback) {
                super(cache, callback);
            }

            Option<Statistic<V>> getCachedValueWithStatisticIfAny() {
                return cachedValueIfAny.getOption();
            }

            Statistic<V> getCachedValueWithStatistic() {
                return getCachedValueWithStatisticIfAny().get();
            }
        }
        Stream<CacheAndCallback> cachesContainingTheKey = cacheWithUpperCaches.stream()
                .map(cacheWithUpperCaches ->
                        new CacheAndCallback(cacheWithUpperCaches.getFirst(),
                                statistic ->
                                        cacheWithUpperCaches.getSecond().stream()
                                                .forEach(cache -> cache.put(key, statistic))))
                .filter(pair -> pair.getCachedValueWithStatisticIfAny().isPresent());

        Option<CacheAndCallback> cacheHitAndCallbackIfAny =
                Option.from(cachesContainingTheKey.findFirst());

        //execute callback if any
        cacheHitAndCallbackIfAny.ifPresent(pair -> pair.getSecond().accept(pair.getCachedValueWithStatistic()));

        //select just the cache hit if any (discard callback, unwrap)
        return cacheHitAndCallbackIfAny.map(pair -> pair.getCachedValueWithStatistic());
    }

    @Override
    public synchronized void put(K key, Statistic<V> value) {
        caches.forEach(cache -> cache.put(key, value));
    }


    @Override
    public synchronized boolean remove(K key) {
        return caches.stream()
                .map(cache -> cache.remove(key))
                .reduce(false, (removed1, removed2) -> removed1 || removed2);
    }

    @Override
    public synchronized Stream<Stream<K>> lazyKeyStream() {
        return caches.stream()
                .flatMap(cache -> cache.lazyKeyStream())
                .flatMap(keyStream -> keyStream)//flatten:Stream<Stream<K>>->Stream<K>
                .distinct()//deduplicate keys from the caches
                .map(key -> Stream.of(key));//wrap back to Stream<Stream>
    }

    private List<Pair<StatFCache<K, V>, List<StatFCache<K, V>>>> createCacheWithUpperCaches() {
        return toList(rangeStream(0, caches.size())
                .map(cacheIdx -> new Pair<>(caches.get(cacheIdx), caches.subList(0, cacheIdx))));
    }
}
