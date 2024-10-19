/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ledmington.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An immutable wrapper for the {@link java.util.Map} interface. This means that operations like put, putAll, clear,
 * remove throw UnsupportedOperationException.
 *
 * @param <K> The type of the key objects.
 * @param <V> The type of the value objects.
 */
public final class ImmutableMap<K, V> implements Map<K, V> {

	// Cached UnsupportedOperationException
	private static final UnsupportedOperationException uoe =
			new UnsupportedOperationException("This is an ImmutableMap.");

	private final Map<K, V> m;

	/**
	 * Returns a new MapBuilder. Sometimes, the type inference system needs a hint about the type of this map like this:
	 * {@code ImmutableMap.<String,Integer>builder()}
	 *
	 * @return A new MapBuilder.
	 * @param <X> The type of key objects.
	 * @param <Y> The type of element objects.
	 */
	public static <X, Y> MapBuilder<X, Y> builder() {
		return new MapBuilder<>();
	}

	/**
	 * Wraps the given Map with an Immutable interface.
	 *
	 * @param m The Map to be wrapped and used as Immutable.
	 */
	public ImmutableMap(final Map<K, V> m) {
		this.m = new HashMap<>(m);
	}

	@Override
	public void clear() {
		throw uoe;
	}

	@Override
	public boolean containsKey(final Object key) {
		return m.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return m.containsValue(value);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return m.entrySet();
	}

	@Override
	public V get(final Object key) {
		return m.get(key);
	}

	@Override
	public boolean isEmpty() {
		return m.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return m.keySet();
	}

	@Override
	public V put(K key, V value) {
		throw uoe;
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		throw uoe;
	}

	@Override
	public V remove(final Object key) {
		throw uoe;
	}

	@Override
	public int size() {
		return m.size();
	}

	@Override
	public Collection<V> values() {
		return m.values();
	}

	@Override
	public String toString() {
		return m.toString();
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + m.hashCode();
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		return this.m.equals(((ImmutableMap<?, ?>) other).m);
	}
}
