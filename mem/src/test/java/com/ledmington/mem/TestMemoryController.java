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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.ledmington.mem.exc.IllegalReadException;
import com.ledmington.mem.exc.IllegalWriteException;

final class TestMemoryController {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);

	private static Stream<Arguments> randomMemoryLocations() {
		return Stream.generate(rng::nextLong).distinct().limit(100).map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("randomMemoryLocations")
	void granularPermissions(final long address) {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, false);

		// Setting permissions only to the first byte
		mem.setPermissions(address, address, true, true, false);

		assertDoesNotThrow(() -> mem.read(address));
		assertThrows(IllegalReadException.class, () -> mem.read4(address));
		assertThrows(IllegalReadException.class, () -> mem.read8(address));
	}

	@ParameterizedTest
	@MethodSource("randomMemoryLocations")
	void multiByteRead(final long address) {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		mem.setPermissions(address, address + 7L, true, true, false);
		mem.write(address, (byte) 0x00);
		mem.write(address + 1L, (byte) 0x01);
		mem.write(address + 2L, (byte) 0x02);
		mem.write(address + 3L, (byte) 0x03);
		mem.write(address + 4L, (byte) 0x04);
		mem.write(address + 5L, (byte) 0x05);
		mem.write(address + 6L, (byte) 0x06);
		mem.write(address + 7L, (byte) 0x07);
		assertEquals(
				0x0706050403020100L,
				mem.read8(address),
				() -> String.format("Expected 0x%016x but was 0x%016x.", 0x0706050403020100L, mem.read8(address)));
	}

	@ParameterizedTest
	@MethodSource("randomMemoryLocations")
	void multiByteWrite(final long address) {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		mem.setPermissions(address, address + 7L, true, true, false);
		mem.write(address, 0x0706050403020100L);
		assertEquals(
				0x0706050403020100L,
				mem.read8(address),
				() -> String.format("Expected 0x%016x but was 0x%016x.", 0x0706050403020100L, mem.read8(address)));
	}

	@ParameterizedTest
	@ValueSource(longs = {0L, 1L, 2L, 3L, 4L, 5L, 6L})
	void unalignedMultiByteRead(final long offset) {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		final long address = rng.nextLong();
		mem.setPermissions(address, address + offset, true, false, false);
		mem.initialize(address, 8, (byte) 0x00);
		assertThrows(IllegalReadException.class, () -> mem.read8(address));
	}

	@ParameterizedTest
	@ValueSource(longs = {0L, 1L, 2L, 3L, 4L, 5L, 6L})
	void unalignedMultiByteWrite(final long offset) {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		final long address = rng.nextLong();
		mem.setPermissions(address, address + offset, false, true, false);
		mem.initialize(address, 8, (byte) 0x00);
		assertThrows(IllegalWriteException.class, () -> mem.write(address, 0L));
	}
}
