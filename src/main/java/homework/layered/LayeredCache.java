package homework.layered;

import homework.FunctionalCache;
import homework.StatAwareFuncCache;
import homework.dto.Statistic;
import homework.markers.ThreadSafe;
import homework.option.Option;
import homework.option.Option;
import homework.utils.Pair;

import java.time.Instant;
import java.util.Optional;
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
        class CacheAndCallback<K, V> extends Pair<StatAwareFuncCache<K, V>, Consumer<Statistic<V>>> {

            CacheAndCallback(StatAwareFuncCache<K, V> cache, Consumer<Statistic<V>> callback) {
                super(cache, callback);
            }
        }
        Stream<CacheAndCallback<K, V>> cachesWithCallbackPairs =
                Stream.of(
                        new CacheAndCallback<K, V>(memCache, statistic -> {
                                }),
                        new CacheAndCallback<K, V>(fsCache, statistic -> {
                            V value = statistic.getValue();
                            Instant lastModifiedDate = statistic.getLastModifiedDate().get();
                            memCache.put(key, new Statistic<V>(value, lastModifiedDate));
                        }));

        //todo: bundle the filter on the pair's cache hit with extraction of option
        Stream<Pair<Option<Statistic<V>>, Consumer<Statistic<V>>>>
                cacheHitOptionalWithCallbackPairs
                = cachesWithCallbackPairs
                .map(pair -> new Pair<>(pair.getFirst().get(key), pair.getSecond()));

        Stream<Pair<Option<Statistic<V>>, Consumer<Statistic<V>>>>
                cacheHitAndCallbackPairs = cacheHitOptionalWithCallbackPairs.filter(pair -> pair.getFirst().isPresent());

        Optional<Pair<Option<Statistic<V>>, Consumer<Statistic<V>>>>
                cacheHitAndCallbackIfAny = cacheHitAndCallbackPairs.findFirst();

        //execute callback if any
        cacheHitAndCallbackIfAny.ifPresent(pair -> pair.getSecond().accept(pair.getFirst().get()));

        //select just the cache hit if any (discard callback, unwrap)
        Optional<Statistic<V>> optionalStat = cacheHitAndCallbackIfAny.map(pair -> pair.getFirst().get());
        Option<Statistic<V>> optionValueWithStat = Option.from(optionalStat);
        return optionValueWithStat.map(stat -> stat.getValue());
    }

    @Override
    public synchronized void put(K key, V value) {
        //todo: optimize this, it creates new instance every time
        memCache.toSimpleCache().put(key, value);
        fsCache.toSimpleCache().put(key, value);
    }

    @Override
    public synchronized boolean remove(K k) {
        return /*memCache.remove(k) |*/ fsCache.remove(k);
    }

    @Override
    public Stream<Stream<K>> lazyKeyStream() {
        return fsCache.lazyKeyStream();
    }
}
