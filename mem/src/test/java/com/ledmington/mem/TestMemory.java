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

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Base class for all implementors of {@link Memory}. */
abstract sealed class TestMemory permits TestMemoryController, TestRandomAccessMemory, TestPagedMemory {

	protected static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);

	// Classes extending TestMemory must implement this method
	protected abstract Memory getMemory();

	protected static Stream<Arguments> randomMemoryLocations() {
		return Stream.generate(rng::nextLong).distinct().limit(100).map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("randomMemoryLocations")
	void singleByte(final long address) {
		final Memory mem = getMemory();
		final byte value = 0x12;
		mem.write(address, value);
		final byte actual = mem.read(address);
		assertEquals(value, actual, () -> String.format("Expected 0x%02x but was 0x%02x.", value, actual));
	}

	@ParameterizedTest
	@MethodSource("randomMemoryLocations")
	void twoBytes(final long address) {
		final Memory mem = getMemory();
		final short value = 0x1234;
		mem.write(address, value);
		final short actual = mem.read2(address);
		assertEquals(value, actual, () -> String.format("Expected 0x%04x but was 0x%04x.", value, actual));
	}

	@ParameterizedTest
	@MethodSource("randomMemoryLocations")
	void fourBytes(final long address) {
		final Memory mem = getMemory();
		final int value = 0x12345678;
		mem.write(address, value);
		final int actual = mem.read4(address);
		assertEquals(value, actual, () -> String.format("Expected 0x%08x but was 0x%08x.", value, actual));
	}

	@ParameterizedTest
	@MethodSource("randomMemoryLocations")
	void eightBytes(final long address) {
		final Memory mem = getMemory();
		final long value = 0x0102030405060708L;
		mem.write(address, value);
		final long actual = mem.read8(address);
		assertEquals(value, actual, () -> String.format("Expected 0x%08x but was 0x%08x.", value, actual));
	}
}
