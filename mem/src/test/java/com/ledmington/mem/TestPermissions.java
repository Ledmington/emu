/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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

import com.ledmington.mem.exc.IllegalExecutionException;
import com.ledmington.mem.exc.IllegalReadException;
import com.ledmington.mem.exc.IllegalWriteException;
import com.ledmington.utils.BitUtils;

final class TestPermissions {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);

	private MemoryController mem;

	@BeforeEach
	void setup() {
		mem = new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, false);
	}

	@Test
	void cantReadByDefault() {
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

		for (final long address : positions) {
			assertThrows(IllegalReadException.class, () -> mem.read(address));
		}
	}

	@Test
	void cantExecuteByDefault() {
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

		for (final long address : positions) {
			assertThrows(IllegalExecutionException.class, () -> mem.readCode(address));
		}
	}

	@Test
	void cantWriteByDefault() {
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());

		for (final long address : positions) {
			assertThrows(IllegalWriteException.class, () -> mem.write(address, BitUtils.asByte(rng.nextInt())));
		}
	}

	@Test
	void canRead() {
		final long start = rng.nextLong();
		final long end = start + 100L;
		mem.setPermissions(start, end, true, false, false);

		for (long i = start; i <= end; i++) {
			final long finalI = i;
			assertDoesNotThrow(() -> mem.read(finalI));
			assertThrows(IllegalExecutionException.class, () -> mem.readCode(finalI));
			assertThrows(IllegalWriteException.class, () -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
		}
	}

	@Test
	void canReadSingleAddress() {
		final long address = rng.nextLong();
		mem.setPermissions(address, 1L, true, false, false);

		assertThrows(IllegalReadException.class, () -> mem.read(address - 1L));
		assertThrows(IllegalExecutionException.class, () -> mem.readCode(address - 1L));
		assertThrows(IllegalWriteException.class, () -> mem.write(address - 1L, (byte) rng.nextInt()));

		assertDoesNotThrow(() -> mem.read(address));
		assertThrows(IllegalExecutionException.class, () -> mem.readCode(address));
		assertThrows(IllegalWriteException.class, () -> mem.write(address, (byte) rng.nextInt()));

		assertThrows(IllegalReadException.class, () -> mem.read(address + 1L));
		assertThrows(IllegalExecutionException.class, () -> mem.readCode(address + 1L));
		assertThrows(IllegalWriteException.class, () -> mem.write(address + 1L, (byte) rng.nextInt()));
	}

	@Test
	void canWrite() {
		final long start = rng.nextLong();
		final long numBytes = 100L;
		mem.setPermissions(start, numBytes, false, true, false);

		for (long i = 0L; i < numBytes; i++) {
			final long address = start + i;
			assertDoesNotThrow(() -> mem.write(address, BitUtils.asByte(rng.nextInt())));
			assertThrows(IllegalReadException.class, () -> mem.read(address));
			assertThrows(IllegalExecutionException.class, () -> mem.readCode(address));
		}
	}

	@Test
	void canWriteSingleAddress() {
		final long address = rng.nextLong();
		mem.setPermissions(address, 1L, false, true, false);

		assertThrows(IllegalReadException.class, () -> mem.read(address - 1L));
		assertThrows(IllegalExecutionException.class, () -> mem.readCode(address - 1L));
		assertThrows(IllegalWriteException.class, () -> mem.write(address - 1L, (byte) rng.nextInt()));

		assertThrows(IllegalReadException.class, () -> mem.read(address));
		assertThrows(IllegalExecutionException.class, () -> mem.readCode(address));
		assertDoesNotThrow(() -> mem.write(address, (byte) rng.nextInt()));

		assertThrows(IllegalReadException.class, () -> mem.read(address + 1L));
		assertThrows(IllegalExecutionException.class, () -> mem.readCode(address + 1L));
		assertThrows(IllegalWriteException.class, () -> mem.write(address + 1L, (byte) rng.nextInt()));
	}

	@Test
	void canReadAndWrite() {
		final long start = rng.nextLong();
		final long numBytes = 100L;
		mem.setPermissions(start, numBytes, true, true, false);

		for (long i = 0L; i < numBytes; i++) {
			final long address = start + i;
			assertDoesNotThrow(() -> mem.read(address));
			assertDoesNotThrow(() -> mem.write(address, BitUtils.asByte(rng.nextInt())));
			assertThrows(IllegalExecutionException.class, () -> mem.readCode(address));
		}
	}

	@Test
	void canExecute() {
		final long start = rng.nextLong();
		final long numBytes = 100L;
		mem.setPermissions(start, numBytes, false, false, true);

		for (long i = 0L; i < numBytes; i++) {
			final long address = start + i;
			assertDoesNotThrow(() -> mem.readCode(address));
			assertThrows(IllegalReadException.class, () -> mem.read(address));
			assertThrows(IllegalWriteException.class, () -> mem.write(address, BitUtils.asByte(rng.nextInt())));
		}
	}

	@Test
	void canExecuteSingleAddress() {
		final long address = rng.nextLong();
		mem.setPermissions(address, 1L, false, false, true);

		assertThrows(IllegalReadException.class, () -> mem.read(address - 1L));
		assertThrows(IllegalExecutionException.class, () -> mem.readCode(address - 1L));
		assertThrows(IllegalWriteException.class, () -> mem.write(address - 1L, (byte) rng.nextInt()));

		assertThrows(IllegalReadException.class, () -> mem.read(address));
		assertDoesNotThrow(() -> mem.readCode(address));
		assertThrows(IllegalWriteException.class, () -> mem.write(address, (byte) rng.nextInt()));

		assertThrows(IllegalReadException.class, () -> mem.read(address + 1L));
		assertThrows(IllegalExecutionException.class, () -> mem.readCode(address + 1L));
		assertThrows(IllegalWriteException.class, () -> mem.write(address + 1L, (byte) rng.nextInt()));
	}

	@Test
	void canReadAndExecute() {
		final long start = rng.nextLong();
		final long end = start + 100L;
		mem.setPermissions(start, end, true, false, true);

		for (long i = start; i <= end; i++) {
			final long finalI = i;
			assertDoesNotThrow(() -> mem.read(finalI));
			assertDoesNotThrow(() -> mem.readCode(finalI));
			assertThrows(IllegalWriteException.class, () -> mem.write(finalI, BitUtils.asByte(rng.nextInt())));
		}
	}

	@Test
	void canWriteAndExecute() {
		final long start = rng.nextLong();
		final long numBytes = 100L;
		mem.setPermissions(start, numBytes, false, true, true);

		for (long i = 0L; i < numBytes; i++) {
			final long address = start + i;
			assertDoesNotThrow(() -> mem.readCode(address));
			assertDoesNotThrow(() -> mem.write(address, BitUtils.asByte(rng.nextInt())));
			assertThrows(IllegalReadException.class, () -> mem.read(address));
		}
	}

	@Test
	void canReadWriteAndExecute() {
		final long start = rng.nextLong();
		final long numBytes = 100L;
		mem.setPermissions(start, numBytes, true, true, true);

		for (long i = 0L; i < numBytes; i++) {
			final long address = start + i;
			assertDoesNotThrow(() -> mem.readCode(address));
			assertDoesNotThrow(() -> mem.read(address));
			assertDoesNotThrow(() -> mem.write(address, BitUtils.asByte(rng.nextInt())));
		}
	}
}
