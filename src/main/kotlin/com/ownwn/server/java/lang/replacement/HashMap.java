package com.ownwn.server.java.lang.replacement;

import java.util.Collection;
import java.util.Set;

public class HashMap<K, V> implements Map<K, V> { // todo store size field for faster computation
    // todo handle mutable keys, will be slow but need to deal with them
    private static final double loadFactor = 0.75;
    private static final int nullHash = 29283873;
    private List<List<Entry<K, V>>> buckets;
    private int numItems = 0;

    public HashMap() {
        this(32);
    }

    public HashMap(int initialCapacity) {
        buckets = new ArrayList<>(initialCapacity);
        for (int i = 0; i < initialCapacity; i++) {
            buckets.add(new ArrayList<>());
        }
    }

    private int hash(Entry<K, V> e) {
        return hash(e.key());
    }

    private int hash(Object o) {
        return o == null ? nullHash : o.hashCode();
    }

    @Override
    public int size() {
        return numItems;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        for (var bucket : buckets) {
            for (var entry : bucket) {
                if (Objects.equals(entry.value, value)) return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        int hash = hash(key);
        var bucket = buckets.get(hash % buckets.size());
        for (var entry : bucket) {
            if (Objects.equals(entry.key, key)) return entry.value;
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        V old = get(key); // might be null, meaning no previous key/entry
        int hash = hash(key);
        var bucket = buckets.get(hash % buckets.size());
        if (old != null) {
            bucket.remove(new Entry<>(key, old));
            numItems--;
        }
        bucket.add(new Entry<>(key, value));
        numItems++;

        checkLoad();

        return old;
    }

    private void checkLoad() {
        double currentLoad = ((double) numItems) / buckets.size();
        if (currentLoad >= loadFactor) {
            resize();
        }
    }

    private void resize() {
        numItems = 0;
        var oldBuckets = buckets;
        buckets = new ArrayList<>();

        for (int i = 0; i < oldBuckets.size() * 2; i++) {
            buckets.add(new ArrayList<>());
        }

        for (var bucket : oldBuckets) {
            for (var entry : bucket) {
                put(entry.key, entry.value);
            }
        }
    }

    @Override
    public V remove(Object key) {
        int hash = hash(key);
        var bucket = buckets.get(hash % buckets.size());
        for (int i = 0; i < bucket.size(); i++) {
            var oldEntry = bucket.get(i);
            if (oldEntry.key.equals(key)) {
                bucket.remove(i);
                numItems--;
                return oldEntry.value;
            }
        }
        return null;
    }

    @Override
    public void putAll(java.util.Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {
        for (var bucket : buckets) {
            bucket.clear();
        }
        numItems = 0;
    }

    @Override
    public Set<K> keySet() {
        return Set.of();
    }

    @Override
    public Collection<V> values() {
        return List.of();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return Set.of(); // todo
    }


    public record Entry<K, V>(K key, V value) {
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("{");
        for (var bucket : buckets) {
            for (var item : bucket) {
                res.append(item.key).append("=").append(item.value);
                res.append(", ");
            }
        }
        if (res.length() > 1) res.delete(res.length()-2, res.length());
        return res.append("}").toString();
    }
}
