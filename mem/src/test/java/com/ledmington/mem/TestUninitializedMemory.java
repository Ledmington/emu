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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ledmington.mem.exc.AccessToUninitializedMemoryException;

final class TestUninitializedMemory {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);

	private MemoryController mem;

	@BeforeEach
	public void setup() {
		// Creating a memory controller with all permissions on the whole range, but without initialising anything
		mem = new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		mem.setPermissions(Long.MIN_VALUE, Long.MAX_VALUE, true, true, true);
	}

	@Test
	void cannotRead() {
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

		for (final long address : positions) {
			assertThrows(AccessToUninitializedMemoryException.class, () -> mem.read(address));
		}
	}

	@Test
	void canReadAfterInitialization() {
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

		for (final long address : positions) {
			mem.write(address, (byte) 0);
		}

		for (final long address : positions) {
			assertDoesNotThrow(() -> mem.read(address));
		}
	}

	@Test
	void cannotExecute() {
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

		for (final long address : positions) {
			assertThrows(AccessToUninitializedMemoryException.class, () -> mem.readCode(address));
		}
	}

	@Test
	void canExecuteAfterInitialization() {
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

		for (final long address : positions) {
			mem.write(address, (byte) 0);
		}

		for (final long address : positions) {
			assertDoesNotThrow(() -> mem.readCode(address));
		}
	}

	@Test
	void canWrite() {
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

		for (final long address : positions) {
			assertDoesNotThrow(() -> mem.write(address, (byte) 0));
		}
	}
}
