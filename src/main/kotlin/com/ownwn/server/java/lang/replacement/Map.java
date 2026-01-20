package com.ownwn.server.java.lang.replacement;


public interface Map<K, V> extends java.util.Map<K, V> {

    static <K, V> Map<K, V> of() {
        return new HashMap<>() {
            @Override
            public void clear() {
                throw new UnsupportedOperationException("Immutable map");
            }

            @Override
            public V put(K key, V value) {
                throw new UnsupportedOperationException("Immutable map");
            }

            @Override
            public V remove(Object key) {
                throw new UnsupportedOperationException("Immutable map");
            }

            @Override
            public void putAll(java.util.Map<? extends K, ? extends V> m) {
                throw new UnsupportedOperationException("Immutable map");
            }
        };
    }

    @Override
    Set<Entry<K, V>> entrySet();
}
