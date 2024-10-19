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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.Test;

final class TestHashUtils {
	@Test
	void hashBooleans() {
		final int f = HashUtils.hash(false);
		final int t = HashUtils.hash(true);
		assertNotEquals(
				f,
				t,
				() -> String.format("Expected hashes of booleans to be different but were 0x%08x and 0x%08x", f, t));
	}

	@Test
	void hashBytes() {
		final Set<Integer> v = new HashSet<>();
		for (int i = 0; i < 256; i++) {
			v.add(HashUtils.hash(BitUtils.asByte(i)));
		}
		assertEquals(256, v.size(), () -> String.format("Expected to have 256 unique values but were %,d", v.size()));
	}

	@Test
	void hashShorts() {
		final RandomGenerator rng = RandomGeneratorFactory.getDefault().create(System.nanoTime());
		final Set<Integer> v = new HashSet<>();
		final short x = BitUtils.asShort(rng.nextInt());
		final int limit = 100;
		for (int i = 0; i < limit; i++) {
			v.add(HashUtils.hash(BitUtils.asShort(x + BitUtils.asShort(i))));
		}
		assertEquals(
				limit,
				v.size(),
				() -> String.format("Expected to have %,d unique values but were %,d", limit, v.size()));
	}

	@Test
	void hashLongs() {
		final RandomGenerator rng = RandomGeneratorFactory.getDefault().create(System.nanoTime());
		final Set<Integer> v = new HashSet<>();
		final long x = rng.nextLong();
		final int limit = 100;
		for (int i = 0; i < limit; i++) {
			v.add(HashUtils.hash(x + BitUtils.asLong(i)));
		}
		assertEquals(
				limit,
				v.size(),
				() -> String.format("Expected to have %,d unique values but were %,d", limit, v.size()));
	}
}
