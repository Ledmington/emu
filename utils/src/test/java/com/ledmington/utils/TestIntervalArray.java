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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("PMD.TooManyStaticImports")
final class TestIntervalArray {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void defaultValues(final boolean defaultValue) {
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());
		final IntervalArray ia = new IntervalArray(defaultValue);
		for (final long x : positions) {
			assertEquals(
					defaultValue,
					ia.get(x),
					() -> String.format(
							"Expected all values to be initially false but value at address 0x%016x was true.", x));
		}
	}

	@Test
	void settingZeroBytesIsNotAnError() {
		final IntervalArray ia = new IntervalArray(false);
		assertDoesNotThrow(() -> ia.set(0L, 0L));
		assertDoesNotThrow(() -> ia.set(0L, 0L, true));
	}

	@Test
	void resettingZeroBytesIsNotAnError() {
		final IntervalArray ia = new IntervalArray(true);
		assertDoesNotThrow(() -> ia.reset(0L, 0L));
		assertDoesNotThrow(() -> ia.set(0L, 0L, false));
	}

	@Test
	void settingNegativeBytesIsAnError() {
		final IntervalArray ia = new IntervalArray(false);
		assertThrows(IllegalArgumentException.class, () -> ia.set(0L, -1L));
		assertThrows(IllegalArgumentException.class, () -> ia.set(0L, -1L, true));
	}

	@Test
	void resettingNegativeBytesIsAnError() {
		final IntervalArray ia = new IntervalArray(true);
		assertThrows(IllegalArgumentException.class, () -> ia.reset(0L, -1L));
		assertThrows(IllegalArgumentException.class, () -> ia.set(0L, -1L, false));
	}

	private static Stream<Arguments> pairsOfMemoryRegions() {
		return Stream.of(Arguments.of(0L, 1L), Arguments.of(-1L, 1L), Arguments.of(0x12345678L, 1000L));
	}

	@ParameterizedTest
	@MethodSource("pairsOfMemoryRegions")
	void setAndGet(final long start, final long numBytes) {
		final IntervalArray ia = new IntervalArray(false);
		ia.set(start, numBytes);
		for (long i = 0L; i < numBytes; i++) {
			final long address = start + i;
			assertTrue(
					ia.get(address),
					() -> String.format(
							"Expected value at address 0x%016x to be true after one set but it was false.", address));
		}
	}

	@Test
	void setTwiceAndGet() {
		final IntervalArray ia = new IntervalArray(false);
		final Set<Long> positions =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());
		for (final long x : positions) {
			ia.set(x, 1L);
			ia.set(x, 1L);
			assertTrue(
					ia.get(x),
					() -> String.format(
							"Expected value at address 0x%016x to be true after two sets but it was false.", x));
		}
	}

	@ParameterizedTest
	@MethodSource("pairsOfMemoryRegions")
	void resetAndGet(final long start, final long numBytes) {
		final IntervalArray ia = new IntervalArray(true);
		ia.reset(start, numBytes);
		for (long i = 0L; i < numBytes; i++) {
			final long address = start + i;
			assertFalse(
					ia.get(address),
					() -> String.format(
							"Expected value at address 0x%016x to be false after one reset but it was true.", address));
		}
	}

	@Test
	void resetTwiceAndGet() {
		final IntervalArray ia = new IntervalArray(true);
		final Set<Long> addresses =
				Stream.generate(rng::nextLong).distinct().limit(100).collect(Collectors.toSet());
		for (final long x : addresses) {
			ia.reset(x, 1L);
			ia.reset(x, 1L);
			assertFalse(
					ia.get(x),
					() -> String.format(
							"Expected value at address 0x%016x to be false after two resets but it was true.", x));
		}
	}

	@Test
	void setOverlappingRegions() {
		final IntervalArray ia = new IntervalArray(false);
		ia.set(0L, 10L);
		ia.set(5L, 15L);
		for (long x = 0L; x <= 15L; x++) {
			final long finalX = x;
			assertTrue(
					ia.get(x),
					() -> String.format("Expected value at address 0x%016x to be true but it was false.", finalX));
		}
	}

	@Test
	void resetOverlappingRegions() {
		final IntervalArray ia = new IntervalArray(false);
		ia.set(0L, 10L);
		ia.reset(5L, 15L);
		for (long x = 0L; x < 5L; x++) {
			final long finalX = x;
			assertTrue(
					ia.get(x),
					() -> String.format("Expected value at address 0x%016x to be true but it was false.", finalX));
		}
		for (long x = 5L; x <= 15L; x++) {
			final long finalX = x;
			assertFalse(
					ia.get(x),
					() -> String.format(
							"Expected value at address 0x%016x to be reset to false but it was true.", finalX));
		}
	}

	@Test
	void setAndResetSameBlock() {
		final IntervalArray ia = new IntervalArray(false);
		ia.set(0L, 10L);
		ia.reset(0L, 10L);
		for (long x = 0L; x <= 10L; x++) {
			final long finalX = x;
			assertFalse(
					ia.get(x),
					() -> String.format(
							"Expected value at address 0x%016x to be reset to false but it was true.", finalX));
		}
	}

	@Test
	void blockFullyContained() {
		final IntervalArray ia = new IntervalArray(false);
		ia.set(0L, 15L);
		ia.reset(5L, 5L);
		for (long x = 0L; x < 5L; x++) {
			final long finalX = x;
			assertTrue(
					ia.get(x),
					() -> String.format("Expected value at address 0x%016x to be true but it was false.", finalX));
		}
		for (long x = 5L; x < 10L; x++) {
			final long finalX = x;
			assertFalse(
					ia.get(x),
					() -> String.format(
							"Expected value at address 0x%016x to be reset to false but it was true.", finalX));
		}
		for (long x = 10L; x < 15L; x++) {
			final long finalX = x;
			assertTrue(
					ia.get(x),
					() -> String.format("Expected value at address 0x%016x to be true but it was false.", finalX));
		}
	}
}
