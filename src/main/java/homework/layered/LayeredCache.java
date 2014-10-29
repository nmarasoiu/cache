package homework.layered;

import homework.FCache;
import homework.dto.Stat;
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
public class LayeredCache<K, V> implements FCache<K, Stat<V>> {
    protected final List<FCache<K, Stat<V>>> caches;
    private final List<Pair<FCache<K, Stat<V>>, List<FCache<K, Stat<V>>>>> cacheWithUpperCaches;

    public LayeredCache(List<FCache<K, Stat<V>>> caches) {
        this.caches = new ArrayList<>(caches);
        cacheWithUpperCaches = createCacheWithUpperCaches();
    }

    /**
     * Gets the value bundled with its last refresh timestamp.
     * The refresh timestamp == the time when it was last put.
     * <p/>
     * But the big advantage of this method's signature returning Option,
     * is that we are sure that if the returned value is != null,
     * then the entry exists in the Map (the entry mapping the key to null value).
     * So we circumvent the need for a containsKey method, with the efficiency advantage at least potential.
     * This method aims toward Scala's "Map.get" which is of type K -> Option[V].
     *
     * Iterates the caches from upper to lower and for the first cache hit if any, gets the value and its stats like last mod time,
     * and before returning that result, it updates all the upper caches with that value with statistic.
     */
    @Override
    public synchronized Option<Stat<V>> get(K key) {
        class CacheAndCallback extends Pair<FCache<K, Stat<V>>, Consumer<Stat<V>>> {
            LazyValue<Stat<V>> cachedValueIfAny = new LazyValue<>(() -> getFirst().get(key));

            CacheAndCallback(FCache<K, Stat<V>> cache, Consumer<Stat<V>> callback) {
                super(cache, callback);
            }

            Option<Stat<V>> getCachedValueWithStatisticIfAny() {
                return cachedValueIfAny.getOption();
            }

            Stat<V> getCachedValueWithStatistic() {
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
    public synchronized void put(K key, Stat<V> value) {
        caches.forEach(cache -> cache.put(key, value));
    }


    @Override
    public synchronized boolean remove(K key) {
        return caches.stream()
                .map(cache -> cache.remove(key))
                .reduce(false, (removed1, removed2) -> removed1 || removed2);
    }

    @Override
    public synchronized Stream<K> keyStream() {
        return caches.stream()
                .flatMap(cache -> cache.keyStream())
                .distinct();//deduplicate keys from the caches
    }

    private List<Pair<FCache<K, Stat<V>>, List<FCache<K, Stat<V>>>>> createCacheWithUpperCaches() {
        return toList(rangeStream(0, caches.size())
                .map(cacheIdx -> new Pair<>(caches.get(cacheIdx), caches.subList(0, cacheIdx))));
    }
}
