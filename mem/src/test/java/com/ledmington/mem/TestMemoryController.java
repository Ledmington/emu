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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.ledmington.mem.exc.IllegalReadException;
import com.ledmington.mem.exc.IllegalWriteException;

final class TestMemoryController extends TestMemory {

	@Override
	protected Memory getMemory() {
		return new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true, true, true, false);
	}

	@ParameterizedTest
	@MethodSource("randomMemoryLocations")
	void granularPermissions(final long address) {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, false);

		// Setting permissions only to the first byte
		mem.setPermissions(address, 1L, true, true, false);

		assertDoesNotThrow(() -> mem.read(address));
		assertThrows(IllegalReadException.class, () -> mem.read8(address));
	}

	@ParameterizedTest
	@ValueSource(longs = {0L, 1L, 2L, 3L, 4L, 5L, 6L})
	void unalignedMultiByteRead(final long numBytes) {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		final long address = rng.nextLong();
		mem.setPermissions(address, numBytes, true, false, false);
		mem.initialize(address, 8, (byte) 0x00);
		assertThrows(IllegalReadException.class, () -> mem.read8(address));
	}

	@ParameterizedTest
	@ValueSource(longs = {0L, 1L, 2L, 3L, 4L, 5L, 6L})
	void unalignedMultiByteWrite(final long numBytes) {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		final long address = rng.nextLong();
		mem.setPermissions(address, numBytes, false, true, false);
		mem.initialize(address, 8, (byte) 0x00);
		assertThrows(IllegalWriteException.class, () -> mem.write(address, 0L));
	}
}
