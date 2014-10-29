package homework.adaptors;

import homework.Cache;
import homework.FCache;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/18/14.
 * Incomplete, incorrect!
 * The sole purpose, to plug this Map into a test machinery.
 * Both google guava and mapdb have comprehensive Map tests.
 * This way we can test the Cache functionality pretty deep.
 */
public class MapBasedOnCache<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    //todo: do sth with this too? these are 2 views over the same cache
    private final FCache<K, V> fCache;
    private final Cache<K, V> simpleCache;

    public MapBasedOnCache(FCache<K, V> extCache) {
        this.fCache = (extCache);
        this.simpleCache = new CacheOverFunctionalCache<>(extCache);
    }

    @Override
    public V get(Object key) {
        return simpleCache.get((K) key);
    }

    @Override
    public V put(K key, V value) {
        V old = get(key);
        fCache.put(key, value);
        return old;
    }

    @Override
    public V remove(Object key) {
        V old = simpleCache.get((K) key);
        fCache.remove((K) key);
        return old;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {

            @Override
            public boolean add(Entry<K, V> kvEntry) {
                boolean contains = contains(kvEntry.getKey());
                put(kvEntry.getKey(), kvEntry.getValue());
                return contains;
            }

            @Override
            public boolean remove(Object o) {
                //todo - make this functional
                if (!(o instanceof Entry)) return false;
                Entry<K, V> kvEntry = (Entry) o;
                K key = kvEntry.getKey();
                V v = simpleCache.get(key);
                if (v == kvEntry.getValue() || (v != null && v.equals(kvEntry.getValue()))) {
                    return fCache.remove(key);
                }
                return false;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {
                    private final Iterator<Entry<K, V>> iterator = getEntryStream().iterator();

                    private Stream<Entry<K, V>> getEntryStream() {
                        return fCache.lazyKeyStream()
                                .flatMap(a->a)
                                .flatMap(key ->
                                        Collections.singletonMap(key, get(key))
                                                .entrySet().stream());
                    }

                    private Entry<K, V> current;

                    @Override
                    public void remove() {
                        if (!fCache.remove(current.getKey())) {
                            throw new IllegalStateException("Called remove twice ? or concurrent modification?");
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        current = iterator.next();
                        return current;
                    }
                };
            }

            @Override
            public int size() {
                return (int) fCache.lazyKeyStream().count();
            }
        };
    }
}
