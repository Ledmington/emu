/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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

import java.util.HashMap;
import java.util.Map;

/**
 * A useful class to build an {@link ImmutableMap}.
 *
 * @param <K> The type of key objects.
 * @param <V> The type of element objects.
 */
public final class MapBuilder<K, V> {

	private final Map<K, V> m = new HashMap<>();
	private boolean alreadyBuilt;

	/** Creates and empty Map. */
	public MapBuilder() {}

	private void assertNotBuilt() {
		if (alreadyBuilt) {
			throw new IllegalStateException("Cannot build twice the same Map.");
		}
	}

	/**
	 * Adds the given key and value to the map that is being built.
	 *
	 * @param key The key object.
	 * @param value The value object.
	 * @return The reference to this MapBuilder to allow fluent code.
	 */
	public MapBuilder<K, V> put(final K key, final V value) {
		assertNotBuilt();
		m.put(key, value);
		return this;
	}

	/**
	 * Constructs an {@code ImmutableMap} with the given data and returns it.
	 *
	 * @return A new ImmutableMap.
	 */
	public Map<K, V> build() {
		assertNotBuilt();
		alreadyBuilt = true;
		return new ImmutableMap<>(m);
	}
}
