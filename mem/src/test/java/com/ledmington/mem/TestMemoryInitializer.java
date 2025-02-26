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
package com.ledmington.mem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

final class TestMemoryInitializer {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);

	@Test
	void zero() {
		final Memory mem = new RandomAccessMemory(MemoryInitializer.zero());
		for (int i = 0; i < 100; i++) {
			final long address = rng.nextLong();
			assertEquals(
					(byte) 0x00,
					mem.read(address),
					() -> String.format(
							"Expected read at 0x%016x to return 0 but was 0x%02x.", address, mem.read(address)));
		}
	}

	@Test
	void random() {
		final Memory mem = new RandomAccessMemory(MemoryInitializer.random());
		assertTrue(
				Stream.generate(rng::nextLong)
								.map(mem::read)
								.limit(100)
								.collect(Collectors.toSet())
								.size()
						> 1,
				"mem.read() returned always the same value.");
	}

	@Test
	void randomReturnsDifferentValueAtSamePlace() {
		final Memory mem = new RandomAccessMemory(MemoryInitializer.random());
		final long address = rng.nextLong();
		assertNotEquals(mem.read(address), mem.read(address));
	}
}
