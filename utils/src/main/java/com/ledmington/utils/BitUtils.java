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

/**
 * A collection of low-level bitwise operations.
 *
 * <p>Note: the existence of this class justifies a porting of the code to a more "bit-aware" language like C, C++ or
 * Rust.
 */
public final class BitUtils {

	private BitUtils() {}

	/**
	 * Converts the given short to a byte by truncating it.
	 *
	 * @param s The short to be converted.
	 * @return The rightmost byte of the given short.
	 */
	public static byte asByte(final short s) {
		return (byte) (s & 0x000000ff);
	}

	/**
	 * Converts the given int to a byte by truncating it.
	 *
	 * @param x The int to be converted.
	 * @return The rightmost byte of the given int.
	 */
	public static byte asByte(final int x) {
		return (byte) (x & 0x000000ff);
	}

	/**
	 * Converts the given long to a byte by truncating it.
	 *
	 * @param x The long to be converted.
	 * @return The rightmost byte of the given long.
	 */
	public static byte asByte(final long x) {
		return (byte) (x & 0x00000000000000ffL);
	}

	/**
	 * Converts the given byte to a short by zero-extending it.
	 *
	 * @param b The byte to be converted.
	 * @return A short with the given byte as the rightmost one.
	 */
	public static short asShort(final byte b) {
		return (short) (b & (short) 0x00ff);
	}

	/**
	 * Converts the given int to a short by truncating it.
	 *
	 * @param x The int to be converted.
	 * @return The 2 rightmost bytes of the given int.
	 */
	public static short asShort(final int x) {
		return (short) (x & 0x0000ffff);
	}

	/**
	 * Converts the given long to a short by truncating it.
	 *
	 * @param l The long to be converted.
	 * @return The 2 rightmost bytes of the given long.
	 */
	public static short asShort(final long l) {
		return (short) (l & 0x000000000000ffffL);
	}

	/**
	 * Converts the given byte to an int by zero-extending it.
	 *
	 * @param b The byte to be converted.
	 * @return An int with the given byte as the rightmost one.
	 */
	public static int asInt(final byte b) {
		return b & 0x000000ff;
	}

	/**
	 * Converts the given short to an int by zero-extending it.
	 *
	 * @param s The short to be converted.
	 * @return An int with the given short as the 2 rightmost bytes.
	 */
	public static int asInt(final short s) {
		return s & 0x0000ffff;
	}

	/**
	 * Converts the given long to an int by truncating it.
	 *
	 * @param x The long to be converted.
	 * @return The 4 rightmost bytes of the given long.
	 */
	public static int asInt(final long x) {
		return (int) (x & 0x00000000ffffffffL);
	}

	/**
	 * Converts the given byte to a long by zero-extending it.
	 *
	 * @param b The byte to be converted.
	 * @return A long with the given byte as the rightmost one.
	 */
	public static long asLong(final byte b) {
		return b & 0x00000000000000ffL;
	}

	/**
	 * Converts the given short to a long by zero-extending it.
	 *
	 * @param s The short to be converted.
	 * @return A long with the given short as the 2 rightmost bytes.
	 */
	public static long asLong(final short s) {
		return s & 0x000000000000ffffL;
	}

	/**
	 * Converts the given int to a long by zero-extending it.
	 *
	 * @param x The int to be converted.
	 * @return A long with the given int as the 4 rightmost bytes.
	 */
	public static long asLong(final int x) {
		return x & 0x00000000ffffffffL;
	}

	/**
	 * Returns an 8-character long string containing the bits of the given byte.
	 *
	 * @param b The byte to be converted.
	 * @return A String with the bits of b.
	 */
	public static String toBinaryString(final byte b) {
		final String s = Integer.toBinaryString(asInt(b));
		final char[] v = new char[8];
		int i = 7;
		int j = s.length() - 1;
		while (i >= 0 && j >= 0) {
			v[i] = s.charAt(j);
			i--;
			j--;
		}
		for (; i >= 0; i--) {
			v[i] = '0';
		}
		return new String(v);
	}

	/**
	 * Computes the bitwise NOT and returns it as a byte.
	 *
	 * @param x The byte to be inverted.
	 * @return The inverted byte.
	 */
	public static byte not(final byte x) {
		return asByte(~x);
	}

	/**
	 * Computes the bitwise AND and returns it as a byte.
	 *
	 * @param a The left-hand side operand.
	 * @param b The right-hand side operand.
	 * @return The bitwise AND as a byte.
	 */
	public static byte and(final byte a, final byte b) {
		return asByte(a & b);
	}

	/**
	 * Computes the bitwise AND and returns it as a short.
	 *
	 * @param a The left-hand side operand.
	 * @param b The right-hand side operand.
	 * @return The bitwise AND as a short.
	 */
	public static short and(final short a, final short b) {
		return asShort(a & b);
	}

	/**
	 * Computes the bitwise OR and returns it as a byte.
	 *
	 * @param a The first operand.
	 * @param b The second operand.
	 * @param others The other operands.
	 * @return The bitwise OR as a byte.
	 */
	public static byte or(final byte a, final byte b, final byte... others) {
		byte x = asByte(a | b);
		for (final byte y : others) {
			x = asByte(x | y);
		}
		return x;
	}

	/**
	 * Computes the bitwise XOR and returns it as a byte.
	 *
	 * @param a The left-hand side operand.
	 * @param b The right-hand side operand.
	 * @return The bitwise XOR as a byte.
	 */
	public static byte xor(final byte a, final byte b) {
		return asByte(a ^ b);
	}

	/**
	 * Computes the bitwise XOR and returns it as a short.
	 *
	 * @param a The left-hand side operand.
	 * @param b The right-hand side operand.
	 * @return The bitwise XOR as a short.
	 */
	public static short xor(final short a, final short b) {
		return asShort(a ^ b);
	}

	/**
	 * Shift (logical) to the right and return as a byte.
	 *
	 * @param b The byte to be shifted.
	 * @param x The number of bits to shift.
	 * @return The shifted value, as a byte.
	 */
	public static byte shr(final byte b, final int x) {
		return asByte((b >>> x) & (0x000000ff >>> x));
	}

	/**
	 * Shift (logical) to the right and return as a long.
	 *
	 * @param l The long to be shifted.
	 * @param x The number of bits to shift.
	 * @return The shifted value, as a long.
	 */
	public static long shr(final long l, final int x) {
		return l >>> x;
	}

	/**
	 * Shift (logical) to the left and return as a byte.
	 *
	 * @param b The byte to be shifted.
	 * @param x The number of bits to shift.
	 * @return The shifted value, as a byte.
	 */
	public static byte shl(final byte b, final int x) {
		return asByte(b << x);
	}

	/**
	 * Shift (logical) to the left and return as a long.
	 *
	 * @param l The long to be shifted.
	 * @param x The number of bits to shift.
	 * @return The shifted value, as a long.
	 */
	public static long shl(final long l, final int x) {
		return l << x;
	}

	/**
	 * Returns the given 32-bit integer as an array of 4 big-endian bytes.
	 *
	 * @param x The 32-bit integer to be split.
	 * @return An array of 4 big endian bytes.
	 */
	public static byte[] asBEBytes(final int x) {
		return new byte[] {asByte(x), asByte(x >>> 8), asByte(x >>> 16), asByte(x >>> 24)};
	}

	/**
	 * Returns the given 64-bit long as an array of 8 big-endian bytes.
	 *
	 * @param x The 64-bit long to be split.
	 * @return An array of 8 big endian bytes.
	 */
	public static byte[] asBEBytes(final long x) {
		return new byte[] {
			asByte(x),
			asByte(x >>> 8),
			asByte(x >>> 16),
			asByte(x >>> 24),
			asByte(x >>> 32),
			asByte(x >>> 40),
			asByte(x >>> 48),
			asByte(x >>> 56)
		};
	}
}
