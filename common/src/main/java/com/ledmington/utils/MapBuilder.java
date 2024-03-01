package com.ledmington.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * A useful class to build an {@link ImmutableMap<K,V>}.
 *
 * @param <K>
 *            The type of key objects.
 * @param <V>
 *            The type of element objects.
 */
public final class MapBuilder<K, V> {

    private final Map<K, V> m = new HashMap<>();
    private boolean alreadyBuilt = false;

    /**
     * Creates and empty Map.
     */
    public MapBuilder() {}

    private void assertNotBuilt() {
        if (alreadyBuilt) {
            throw new IllegalStateException("Cannot build twice the same Map.");
        }
    }

    /**
     * Adds the given key and value to the map that is being built.
     *
     * @param key
     *              The key object.
     * @param value
     *              The value object.
     * @return
     *         The reference to this MapBuilder to allow fluent code.
     */
    public MapBuilder<K, V> put(final K key, final V value) {
        assertNotBuilt();
        m.put(key, value);
        return this;
    }

    /**
     * Constructs an {@code ImmutableMap} with the given data and returns it.
     *
     * @return
     *         A new ImmutableMap.
     */
    public ImmutableMap<K, V> build() {
        assertNotBuilt();
        alreadyBuilt = true;
        return new ImmutableMap<>(m);
    }
}
