package homework;

import homework.cacheDecorators.ExtendedCache;

import java.util.*;

/**
 * Created by dnmaras on 10/18/14.
 * Incomplete, incorrect!
 * The sole purpose, to plug this Map into a test machinery.
 * Both google guava and mapdb have comprehensive Map tests.
 * This way we can test the Cache functionality pretty deep.
 */
public class MapBasedOnCache<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    private ExtendedCache<K, V> cache;

    public MapBasedOnCache(ExtendedCache<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public V get(Object key) {
        return cache.get((K) key);
    }

    @Override
    public V put(K key, V value) {
        V old = get(key);
        cache.put(key, value);
        return old;
    }

    @Override
    public V remove(Object key) {
        V old = cache.get((K) key);
        cache.remove((K) key);
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
                if (!(o instanceof Entry)) return false;
                Entry<K, V> kvEntry = (Entry) o;
                K key = kvEntry.getKey();
                V v = cache.get(key);
                if (v == kvEntry.getValue() || (v != null && v.equals(kvEntry.getValue()))) {
                    return cache.remove(key);
                }
                return false;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {
                    private final Iterator<Entry<K, V>> iterator = cache.entryStream().iterator();
                    private Entry<K, V> current;

                    @Override
                    public void remove() {
                        if (!cache.remove(current.getKey())) {
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
                return (int) cache.entryStream().count();
            }
        };
    }
}
