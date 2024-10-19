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
package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestSIB {

	static Stream<Arguments> scales() {
		return Stream.of(
				Arguments.of((byte) 0x00, (byte) 0x00),
				Arguments.of((byte) 0x40, (byte) 0x01),
				Arguments.of((byte) 0x80, (byte) 0x02),
				Arguments.of((byte) 0xc0, (byte) 0x03));
	}

	@ParameterizedTest
	@MethodSource("scales")
	void parseScale(final byte m, final byte expected) {
		final byte actual = new SIB(m).scale();
		assertEquals(
				expected,
				actual,
				() -> String.format(
						"Expected scale of SIB byte 0x%02x to be 0x%02x but was 0x%02x", m, expected, actual));
	}

	static Stream<Arguments> indexes() {
		return Stream.of(
				Arguments.of((byte) 0x00, (byte) 0x00),
				Arguments.of((byte) 0x08, (byte) 0x01),
				Arguments.of((byte) 0x10, (byte) 0x02),
				Arguments.of((byte) 0x18, (byte) 0x03),
				Arguments.of((byte) 0x20, (byte) 0x04),
				Arguments.of((byte) 0x28, (byte) 0x05),
				Arguments.of((byte) 0x30, (byte) 0x06),
				Arguments.of((byte) 0x38, (byte) 0x07));
	}

	@ParameterizedTest
	@MethodSource("indexes")
	void parseIndex(final byte m, final byte expected) {
		final byte actual = new SIB(m).index();
		assertEquals(
				expected,
				actual,
				() -> String.format(
						"Expected index of SIB byte 0x%02x to be 0x%02x but was 0x%02x", m, expected, actual));
	}

	static Stream<Arguments> bases() {
		return Stream.of(
				Arguments.of((byte) 0x00, (byte) 0x00),
				Arguments.of((byte) 0x01, (byte) 0x01),
				Arguments.of((byte) 0x02, (byte) 0x02),
				Arguments.of((byte) 0x03, (byte) 0x03),
				Arguments.of((byte) 0x04, (byte) 0x04),
				Arguments.of((byte) 0x05, (byte) 0x05),
				Arguments.of((byte) 0x06, (byte) 0x06),
				Arguments.of((byte) 0x07, (byte) 0x07));
	}

	@ParameterizedTest
	@MethodSource("bases")
	void parseBase(final byte m, final byte expected) {
		final byte actual = new SIB(m).base();
		assertEquals(
				expected,
				actual,
				() -> String.format(
						"Expected base of SIB byte 0x%02x to be 0x%02x but was 0x%02x", m, expected, actual));
	}
}
