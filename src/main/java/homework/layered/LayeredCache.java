package homework.layered;

import homework.FunctionalCache;
import homework.dto.Statistic;
import homework.markers.ThreadSafe;
import homework.option.Option;
import homework.option.OptionFactory;
import homework.utils.Pair;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ThreadSafe
public class LayeredCache<K, V> implements FunctionalCache<K, V> {
    protected final FunctionalCache<K, V> memCache;
    protected final FunctionalCache<K, V> fsCache;

    public LayeredCache(FunctionalCache<K, V> memCache, FunctionalCache<K, V> fsCache) {
        this.memCache = memCache;
        this.fsCache = fsCache;
    }

    @Override
    //we use synchronized, as the simplest; I would still consider locks for ability to let interrupt
    //read-write locks are not options because the "read" actually is also a potential write op
    public synchronized Option<V> get(K key) {
        class CacheAndCallback<K, V> extends Pair<FunctionalCache<K, V>, Consumer<Statistic<V>>> {

            CacheAndCallback(FunctionalCache<K, V> cache, Consumer<Statistic<V>> callback) {
                super(cache, callback);
            }

            public CacheAndCallback(FunctionalCache<K, V> cache) {
                this(cache, statistic -> {
                });
            }

        }
        Stream<CacheAndCallback<K, V>> cachesWithCallbackPairs =
                Stream.of(new CacheAndCallback<K, V>(memCache),
                        new CacheAndCallback<K, V>(fsCache, statistic -> {
                            V value = statistic.getValue();
                            Instant lastModifiedDate = statistic.getLastModifiedDate();
                            memCache.put(key, value, lastModifiedDate);
                        }));

        Stream<Pair<Option<Statistic<V>>, Consumer<Statistic<V>>>>
                cacheHitOptionalWithCallbackPairs
                = cachesWithCallbackPairs
                .map(pair -> new Pair<>(pair.getFirst().getWrapped(key), pair.getSecond()));

        Stream<Pair<Option<Statistic<V>>, Consumer<Statistic<V>>>>
                cacheHitAndCallbackPairs = cacheHitOptionalWithCallbackPairs.filter(pair -> pair.getFirst().isPresent());

        Optional<Pair<Option<Statistic<V>>, Consumer<Statistic<V>>>>
                cacheHitAndCallbackIfAny = cacheHitAndCallbackPairs.findFirst();

        //execute callback if any
        cacheHitAndCallbackIfAny.ifPresent(pair -> pair.getSecond().accept(pair.getFirst().get()));

        //select just the cache hit if any (discard callback, unwrap)
        Optional<Statistic<V>> optionalStat = cacheHitAndCallbackIfAny.map(pair -> pair.getFirst().get());
        Option<Statistic<V>> optionValueWithStat = OptionFactory.from(optionalStat);
        return optionValueWithStat.map(stat -> stat.getValue());
    }

    @Override
    public synchronized void put(K key, V value) {
        memCache.put(key, value);
        fsCache.put(key, value);
    }

}
