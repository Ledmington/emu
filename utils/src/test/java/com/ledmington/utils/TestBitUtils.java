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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("PMD.UnnecessaryCast")
final class TestBitUtils {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);

	@Test
	void shortToByte() {
		final short s = (short) rng.nextInt();
		final byte expected = (byte) (s & 0x00ff);
		final byte actual = BitUtils.asByte(s);
		assertEquals(expected, actual, () -> String.format("Expected 0x%02x but was 0x%02x.", expected, actual));
	}

	@Test
	void intToByte() {
		final int x = rng.nextInt();
		final byte expected = (byte) (x & 0x000000ff);
		final byte actual = BitUtils.asByte(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%02x but was 0x%02x.", expected, actual));
	}

	@Test
	void longToByte() {
		final long x = rng.nextLong();
		final byte expected = (byte) (x & 0x00000000000000ffL);
		final byte actual = BitUtils.asByte(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%02x but was 0x%02x.", expected, actual));
	}

	@Test
	void byteToShort() {
		final byte x = BitUtils.asByte(rng.nextInt());
		final short expected = (short) (((short) x) & 0x00ff);
		final short actual = BitUtils.asShort(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%04x but was 0x%04x.", expected, actual));
	}

	@Test
	void intToShort() {
		final int x = rng.nextInt();
		final short expected = (short) x;
		final short actual = BitUtils.asShort(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%04x but was 0x%04x.", expected, actual));
	}

	@Test
	void longToShort() {
		final long x = rng.nextLong();
		final short expected = (short) x;
		final short actual = BitUtils.asShort(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%04x but was 0x%04x.", expected, actual));
	}

	@Test
	void byteToInt() {
		final byte x = BitUtils.asByte(rng.nextInt());
		final int expected = ((int) x) & 0x000000ff;
		final int actual = BitUtils.asInt(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%08x but was 0x%08x.", expected, actual));
	}

	@Test
	void shortToInt() {
		final short x = BitUtils.asShort(rng.nextInt());
		final int expected = ((int) x) & 0x0000ffff;
		final int actual = BitUtils.asInt(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%08x but was 0x%08x.", expected, actual));
	}

	@Test
	void longToInt() {
		final long x = rng.nextLong();
		final int expected = (int) x;
		final int actual = BitUtils.asInt(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%08x but was 0x%08x.", expected, actual));
	}

	@Test
	void byteToLong() {
		final byte x = BitUtils.asByte(rng.nextInt());
		final long expected = ((long) x) & 0x00000000000000ffL;
		final long actual = BitUtils.asLong(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%016x but was 0x%016x.", expected, actual));
	}

	@Test
	void shortToLong() {
		final short x = BitUtils.asShort(rng.nextInt());
		final long expected = ((long) x) & 0x000000000000ffffL;
		final long actual = BitUtils.asLong(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%016x but was 0x%016x.", expected, actual));
	}

	@Test
	void intToLong() {
		final int x = rng.nextInt();
		final long expected = ((long) x) & 0x00000000ffffffffL;
		final long actual = BitUtils.asLong(x);
		assertEquals(expected, actual, () -> String.format("Expected 0x%016x but was 0x%016x.", expected, actual));
	}

	private static Stream<Arguments> SHRbytes() {
		return Stream.of(
				Arguments.of((byte) 0xff, (byte) 0xff, 0),
				Arguments.of((byte) 0x7f, (byte) 0xff, 1),
				Arguments.of((byte) 0x3f, (byte) 0xff, 2),
				Arguments.of((byte) 0x1f, (byte) 0xff, 3),
				Arguments.of((byte) 0x0f, (byte) 0xff, 4),
				Arguments.of((byte) 0x07, (byte) 0xff, 5),
				Arguments.of((byte) 0x03, (byte) 0xff, 6),
				Arguments.of((byte) 0x01, (byte) 0xff, 7),
				Arguments.of((byte) 0x00, (byte) 0xff, 8),
				//
				Arguments.of((byte) 0x80, (byte) 0x80, 0),
				Arguments.of((byte) 0x40, (byte) 0x80, 1),
				Arguments.of((byte) 0x20, (byte) 0x80, 2),
				Arguments.of((byte) 0x10, (byte) 0x80, 3),
				Arguments.of((byte) 0x08, (byte) 0x80, 4),
				Arguments.of((byte) 0x04, (byte) 0x80, 5),
				Arguments.of((byte) 0x02, (byte) 0x80, 6),
				Arguments.of((byte) 0x01, (byte) 0x80, 7),
				Arguments.of((byte) 0x00, (byte) 0x80, 8));
	}

	@ParameterizedTest
	@MethodSource("SHRbytes")
	void shiftRight(final byte expected, final byte input, final int shift) {
		final byte result = BitUtils.shr(input, shift);
		assertEquals(
				expected,
				result,
				() -> String.format(
						"Expected 0x%02x >>> %,d to be 0x%02x but was 0x%02x", input, shift, expected, result));
	}

	private static Stream<Arguments> ANDbytes() {
		return Stream.of(
				Arguments.of((byte) 0xff, (byte) 0xff, (byte) 0xff),
				Arguments.of((byte) 0x7f, (byte) 0x7f, (byte) 0xff),
				Arguments.of((byte) 0x3f, (byte) 0x3f, (byte) 0xff),
				Arguments.of((byte) 0x1f, (byte) 0x1f, (byte) 0xff),
				Arguments.of((byte) 0x0f, (byte) 0x0f, (byte) 0xff),
				Arguments.of((byte) 0x07, (byte) 0x07, (byte) 0xff),
				Arguments.of((byte) 0x03, (byte) 0x03, (byte) 0xff),
				Arguments.of((byte) 0x01, (byte) 0x01, (byte) 0xff),
				Arguments.of((byte) 0x00, (byte) 0x00, (byte) 0xff));
	}

	@ParameterizedTest
	@MethodSource("ANDbytes")
	void bitwiseAnd(final byte expected, final byte input1, final byte input2) {
		final byte result = BitUtils.and(input1, input2);
		assertEquals(
				expected,
				result,
				() -> String.format(
						"Expected 0x%02x AND 0x%02x to be 0x%02x but was 0x%02x", input1, input2, expected, result));
	}

	private static Stream<Arguments> ORbytes() {
		return Stream.of(
				Arguments.of((byte) 0xff, (byte) 0x00, (byte) 0xff),
				Arguments.of((byte) 0x81, (byte) 0x80, (byte) 0x01),
				Arguments.of((byte) 0x82, (byte) 0x80, (byte) 0x02),
				Arguments.of((byte) 0x84, (byte) 0x80, (byte) 0x04),
				Arguments.of((byte) 0x88, (byte) 0x80, (byte) 0x08),
				Arguments.of((byte) 0x90, (byte) 0x80, (byte) 0x10),
				Arguments.of((byte) 0xa0, (byte) 0x80, (byte) 0x20),
				Arguments.of((byte) 0xc0, (byte) 0x80, (byte) 0x40),
				Arguments.of((byte) 0x80, (byte) 0x80, (byte) 0x80));
	}

	@ParameterizedTest
	@MethodSource("ORbytes")
	void bitwiseOr(final byte expected, final byte input1, final byte input2) {
		final byte result = BitUtils.or(input1, input2);
		assertEquals(
				expected,
				result,
				() -> String.format(
						"Expected 0x%02x OR 0x%02x to be 0x%02x but was 0x%02x", input1, input2, expected, result));
	}

	private static Stream<Arguments> XORbytes() {
		return Stream.of(
				Arguments.of((byte) 0xff, (byte) 0x00, (byte) 0xff),
				Arguments.of((byte) 0xff, (byte) 0xff, (byte) 0x00),
				Arguments.of((byte) 0x00, (byte) 0x00, (byte) 0x00),
				Arguments.of((byte) 0x00, (byte) 0xff, (byte) 0xff),
				Arguments.of((byte) 0x81, (byte) 0x80, (byte) 0x01),
				Arguments.of((byte) 0x01, (byte) 0x80, (byte) 0x81),
				Arguments.of((byte) 0x00, (byte) 0x81, (byte) 0x81));
	}

	@ParameterizedTest
	@MethodSource("XORbytes")
	void bitwiseXor(final byte expected, final byte input1, final byte input2) {
		final byte result = BitUtils.xor(input1, input2);
		assertEquals(
				expected,
				result,
				() -> String.format(
						"Expected 0x%02x XOR 0x%02x to be 0x%02x but was 0x%02x", input1, input2, expected, result));
	}

	@Test
	void binaryStrings() {
		final int bits = 8;
		for (int i = 0; i < 256; i++) {
			final byte x = BitUtils.asByte(i);
			String tmp = Integer.toBinaryString(x);
			if (tmp.length() > bits) {
				tmp = tmp.substring(tmp.length() - bits);
			}
			final String expected = "0".repeat(bits - tmp.length()) + tmp;
			final String actual = BitUtils.toBinaryString(x);
			assertEquals(expected, actual, () -> String.format("Expected '%s' but was '%s'.", expected, actual));
		}
	}

	@Test
	void twoBytesBigEndian() {
		assertArrayEquals(new byte[] {(byte) 0x12, (byte) 0x34}, BitUtils.asBEBytes((short) 0x1234));
	}

	@Test
	void fourBytesBigEndian() {
		assertArrayEquals(
				new byte[] {(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78}, BitUtils.asBEBytes(0x12345678));
	}

	@Test
	void eightBytesBigEndian() {
		assertArrayEquals(
				new byte[] {
					(byte) 0x01,
					(byte) 0x02,
					(byte) 0x03,
					(byte) 0x04,
					(byte) 0x05,
					(byte) 0x06,
					(byte) 0x07,
					(byte) 0x08,
				},
				BitUtils.asBEBytes(0x0102030405060708L));
	}

	@Test
	void twoBytesLittleEndian() {
		assertArrayEquals(new byte[] {(byte) 0x34, (byte) 0x12}, BitUtils.asLEBytes((short) 0x1234));
	}

	@Test
	void fourBytesLittleEndian() {
		assertArrayEquals(
				new byte[] {
					(byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12,
				},
				BitUtils.asLEBytes(0x12345678));
	}

	@Test
	void eightBytesLittleEndian() {
		final byte[] expected = new byte[] {
			(byte) 0x08, (byte) 0x07, (byte) 0x06, (byte) 0x05, (byte) 0x04, (byte) 0x03, (byte) 0x02, (byte) 0x01,
		};
		final long value = 0x0102030405060708L;
		final byte[] actual = BitUtils.asLEBytes(0x0102030405060708L);
		assertArrayEquals(
				expected,
				actual,
				() -> String.format(
						"Expected '0x%016x' to be encoded as %s but was %s.",
						value,
						"["
								+ IntStream.range(0, expected.length)
										.mapToObj(i -> String.format("%02x", expected[i]))
										.collect(Collectors.joining(", "))
								+ "]",
						"["
								+ IntStream.range(0, actual.length)
										.mapToObj(i -> String.format("%02x", actual[i]))
										.collect(Collectors.joining(", "))
								+ "]"));
	}
}
