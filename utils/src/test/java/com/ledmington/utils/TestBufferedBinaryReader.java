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
package com.ledmington.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class TestBufferedBinaryReader {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);

	@TempDir
	private Path tmp;

	private byte[] arr;
	private Path file;

	@BeforeEach
	void setup() throws IOException {
		this.arr = new byte[1000];
		for (int i = 0; i < arr.length; i++) {
			this.arr[i] = BitUtils.asByte(rng.nextInt());
		}
		this.file = tmp.resolve("data.bin");
		Files.write(file, arr);
	}

	@ParameterizedTest
	@ValueSource(ints = {-99, -1, 0, 3, 99})
	void invalidAlignment(final int alignment) {
		assertThrows(IllegalArgumentException.class, () -> new BufferedBinaryReader(file, false, alignment, 16));
	}

	@ParameterizedTest
	@ValueSource(ints = {-99, -1, 0})
	void invalidBufferSize(final int bufferSize) {
		assertThrows(IllegalArgumentException.class, () -> new BufferedBinaryReader(file, false, 1L, bufferSize));
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 3, 7, 16, 999, 1000, 4096})
	void sameAsInMemory(final int bufferSize) {
		for (final boolean endianness : new boolean[] {false, true}) {
			for (final long alignment : new long[] {1L, 2L, 4L, 8L, 16L}) {
				try (BufferedBinaryReader actual = new BufferedBinaryReader(file, endianness, alignment, bufferSize)) {
					final BinaryReader expected = new InMemoryArrayReader(arr, endianness, alignment);
					while (expected.getPosition() + 4L * alignment + 15L <= arr.length) {
						assertEquals(expected.read1(), actual.read1(), "read1 mismatch");
						assertEquals(expected.read2(), actual.read2(), "read2 mismatch");
						assertEquals(expected.read4(), actual.read4(), "read4 mismatch");
						assertEquals(expected.read8(), actual.read8(), "read8 mismatch");
						assertEquals(expected.getPosition(), actual.getPosition(), "Position mismatch");
					}
				}
			}
		}
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 7, 64})
	void randomAccess(final int bufferSize) {
		try (BufferedBinaryReader br = new BufferedBinaryReader(file, false, 1L, bufferSize)) {
			for (int i = 0; i < 10_000; i++) {
				final int pos = rng.nextInt(arr.length);
				br.setPosition(pos);
				assertEquals(arr[pos], br.read(), () -> String.format("Wrong byte at position %,d", pos));
			}
		}
	}

	@Test
	void outOfBounds() {
		try (BufferedBinaryReader br = new BufferedBinaryReader(file)) {
			assertEquals(arr.length, br.size(), "Wrong file size");
			br.setPosition(arr.length);
			assertThrows(IndexOutOfBoundsException.class, br::read);
			br.setPosition(-1L);
			assertThrows(IndexOutOfBoundsException.class, br::read);
		}
	}

	@Test
	void readPastEnd() {
		try (BufferedBinaryReader br = new BufferedBinaryReader(file, false, 1L, 16)) {
			br.setPosition(arr.length - 4L);
			assertThrows(IndexOutOfBoundsException.class, br::read8);
		}
	}

	@Test
	void equalsAndHashCode() {
		try (BufferedBinaryReader a = new BufferedBinaryReader(file, true, 4L, 16);
				BufferedBinaryReader b = new BufferedBinaryReader(file, true, 4L, 16)) {
			assertEquals(a, b, "Readers on the same file with the same state should be equal");
			assertEquals(a.hashCode(), b.hashCode(), "Equal readers should have the same hash code");
			a.read4();
			assertNotEquals(a, b, "Readers at different positions should not be equal");
			b.read4();
			assertEquals(a, b, "Readers at the same position should be equal");
			assertEquals(a.hashCode(), b.hashCode(), "Equal readers should have the same hash code");
		}
	}
}
