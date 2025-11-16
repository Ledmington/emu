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
package com.ledmington.emu;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.ledmington.utils.BitUtils;

public final class MathUtils {

	private static final int UINT8_MAX = 0x000000ff;
	private static final int UINT16_MAX = 0x0000ffff;
	private static final long UINT32_MAX = 0x0000_0000_ffff_ffffL;
	private static final BigInteger UINT64_MAX = new BigInteger("ffffffffffffffff", 16);

	private MathUtils() {}

	// Unsigned Addition Carry

	public static boolean willCarryAdd(final byte a, final byte b) {
		// FIXME: can't we do it without actually adding?
		return (BitUtils.asInt(a) + BitUtils.asInt(b)) > UINT8_MAX;
	}

	public static boolean willCarryAdd(final short a, final short b) {
		// FIXME: can't we do it without actually adding?
		return (BitUtils.asInt(a) + BitUtils.asInt(b)) > UINT16_MAX;
	}

	public static boolean willCarryAdd(final int a, final int b) {
		// FIXME: can't we do it without actually adding?
		return (BitUtils.asLong(a) + BitUtils.asLong(b)) > UINT32_MAX;
	}

	private static BigInteger toUnsignedBigInteger(final long value) {
		return new BigInteger(1, ByteBuffer.allocate(8).putLong(value).array());
	}

	public static boolean willCarryAdd(final long a, final long b) {
		// FIXME: can't we do it without actually adding (and without using BigInteger)?
		return toUnsignedBigInteger(a).add(toUnsignedBigInteger(b)).compareTo(UINT64_MAX) > 0;
	}

	// Unsigned Subtraction Borrow

	public static boolean willCarrySub(final byte a, final byte b) {
		return BitUtils.asInt(a) < BitUtils.asInt(b);
	}

	public static boolean willCarrySub(final short a, final short b) {
		return BitUtils.asInt(a) < BitUtils.asInt(b);
	}

	public static boolean willCarrySub(final int a, final int b) {
		return BitUtils.asLong(a) < BitUtils.asLong(b);
	}

	public static boolean willCarrySub(final long a, final long b) {
		// Unsigned 64-bit compare using BigInteger, consistent with your willCarrySum(long,...)
		return toUnsignedBigInteger(a).compareTo(toUnsignedBigInteger(b)) < 0;
	}

	// Signed Addition Overflow

	public static boolean willOverflowAdd(final byte a, final byte b) {
		final int r = (byte) ((int) a + (int) b);
		return (((int) a ^ r) & ((int) b ^ r)) < 0;
	}

	public static boolean willOverflowAdd(final short a, final short b) {
		final int r = (short) ((int) a + (int) b);
		return (((int) a ^ r) & ((int) b ^ r)) < 0;
	}

	public static boolean willOverflowAdd(final int a, final int b) {
		final int r = a + b;
		return ((a ^ r) & (b ^ r)) < 0;
	}

	public static boolean willOverflowAdd(final long a, final long b) {
		final long r = a + b;
		return ((a ^ r) & (b ^ r)) < 0L;
	}

	// Signed Subtraction Overflow

	public static boolean willOverflowSub(final byte a, final byte b) {
		final int r = (byte) ((int) a - (int) b);
		return (((int) a ^ (int) b) & ((int) a ^ r)) < 0;
	}

	public static boolean willOverflowSub(final short a, final short b) {
		final int r = (short) ((int) a - (int) b);
		return (((int) a ^ (int) b) & ((int) a ^ r)) < 0;
	}

	public static boolean willOverflowSub(final int a, final int b) {
		final int r = a - b;
		return ((a ^ b) & (a ^ r)) < 0;
	}

	public static boolean willOverflowSub(final long a, final long b) {
		final long r = a - b;
		return ((a ^ b) & (a ^ r)) < 0L;
	}
}
