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
package com.ledmington.emu;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import com.ledmington.utils.BitUtils;

/** Provides utility methods for detecting unsigned carries, unsigned borrows, and signed overflow conditions. */
public final class MathUtils {

	private static final int UINT8_MAX = 0x000000ff;
	private static final int UINT16_MAX = 0x0000ffff;
	private static final long UINT32_MAX = 0x0000_0000_ffff_ffffL;
	private static final BigInteger UINT64_MAX = new BigInteger("ffffffffffffffff", 16);

	private MathUtils() {}

	// Unsigned Addition Carry

	/**
	 * Determines whether adding two bytes as unsigned values produces a carry.
	 *
	 * @param a The first byte operand.
	 * @param b The second byte operand.
	 * @return True if the addition produces an unsigned carry.
	 */
	public static boolean willCarryAdd(final byte a, final byte b) {
		// FIXME: can't we do it without actually adding?
		return (BitUtils.asInt(a) + BitUtils.asInt(b)) > UINT8_MAX;
	}

	/**
	 * Determines whether adding two shorts as unsigned values produces a carry.
	 *
	 * @param a The first short operand.
	 * @param b The second short operand.
	 * @return True if the addition produces an unsigned carry.
	 */
	public static boolean willCarryAdd(final short a, final short b) {
		// FIXME: can't we do it without actually adding?
		return (BitUtils.asInt(a) + BitUtils.asInt(b)) > UINT16_MAX;
	}

	/**
	 * Determines whether adding two ints as unsigned values produces a carry.
	 *
	 * @param a The first int operand.
	 * @param b The second int operand.
	 * @return True if the addition produces an unsigned carry.
	 */
	public static boolean willCarryAdd(final int a, final int b) {
		// FIXME: can't we do it without actually adding?
		return (BitUtils.asLong(a) + BitUtils.asLong(b)) > UINT32_MAX;
	}

	private static BigInteger toUnsignedBigInteger(final long value) {
		return new BigInteger(1, ByteBuffer.allocate(8).putLong(value).array());
	}

	/**
	 * Determines whether adding two longs as unsigned values produces a carry.
	 *
	 * @param a The first long operand.
	 * @param b The second long operand.
	 * @return True if the addition produces an unsigned carry.
	 */
	public static boolean willCarryAdd(final long a, final long b) {
		// FIXME: can't we do it without actually adding (and without using BigInteger)?
		return toUnsignedBigInteger(a).add(toUnsignedBigInteger(b)).compareTo(UINT64_MAX) > 0;
	}

	// Unsigned Subtraction Borrow

	/**
	 * Determines whether subtracting two bytes as unsigned values produces a borrow.
	 *
	 * @param a The first byte operand.
	 * @param b The second byte operand.
	 * @return True if the subtraction produces an unsigned borrow.
	 */
	public static boolean willCarrySub(final byte a, final byte b) {
		return BitUtils.asInt(a) < BitUtils.asInt(b);
	}

	/**
	 * Determines whether subtracting two shorts as unsigned values produces a borrow.
	 *
	 * @param a The first short operand.
	 * @param b The second short operand.
	 * @return True if the subtraction produces an unsigned borrow.
	 */
	public static boolean willCarrySub(final short a, final short b) {
		return BitUtils.asInt(a) < BitUtils.asInt(b);
	}

	/**
	 * Determines whether subtracting two ints as unsigned values produces a borrow.
	 *
	 * @param a The first int operand.
	 * @param b The second int operand.
	 * @return True if the subtraction produces an unsigned borrow.
	 */
	public static boolean willCarrySub(final int a, final int b) {
		return BitUtils.asLong(a) < BitUtils.asLong(b);
	}

	/**
	 * Determines whether subtracting two longs as unsigned values produces a borrow.
	 *
	 * @param a The first long operand.
	 * @param b The second long operand.
	 * @return True if the subtraction produces an unsigned borrow.
	 */
	public static boolean willCarrySub(final long a, final long b) {
		return toUnsignedBigInteger(a).compareTo(toUnsignedBigInteger(b)) < 0;
	}

	// Signed Addition Overflow

	/**
	 * Determines whether adding two bytes as signed values produces an overflow.
	 *
	 * @param a The first byte operand.
	 * @param b The second byte operand.
	 * @return True if the addition produces a signed overflow.
	 */
	public static boolean willOverflowAdd(final byte a, final byte b) {
		final int r = (byte) (a + b);
		return ((a ^ r) & (b ^ r)) < 0;
	}

	/**
	 * Determines whether adding two shorts as signed values produces an overflow.
	 *
	 * @param a The first short operand.
	 * @param b The second short operand.
	 * @return True if the addition produces a signed overflow.
	 */
	public static boolean willOverflowAdd(final short a, final short b) {
		final int r = (short) (a + b);
		return ((a ^ r) & (b ^ r)) < 0;
	}

	/**
	 * Determines whether adding two ints as signed values produces an overflow.
	 *
	 * @param a The first int operand.
	 * @param b The second int operand.
	 * @return True if the addition produces a signed overflow.
	 */
	public static boolean willOverflowAdd(final int a, final int b) {
		final int r = a + b;
		return ((a ^ r) & (b ^ r)) < 0;
	}

	/**
	 * Determines whether adding two longs as signed values produces an overflow.
	 *
	 * @param a The first long operand.
	 * @param b The second long operand.
	 * @return True if the addition produces a signed overflow.
	 */
	public static boolean willOverflowAdd(final long a, final long b) {
		final long r = a + b;
		return ((a ^ r) & (b ^ r)) < 0L;
	}

	// Signed Subtraction Overflow

	/**
	 * Determines whether subtracting two bytes as signed values produces an overflow.
	 *
	 * @param a The first byte operand.
	 * @param b The second byte operand.
	 * @return True if the subtraction produces a signed overflow.
	 */
	public static boolean willOverflowSub(final byte a, final byte b) {
		final int r = (byte) (a - b);
		return ((a ^ b) & (a ^ r)) < 0;
	}

	/**
	 * Determines whether subtracting two shorts as signed values produces an overflow.
	 *
	 * @param a The first short operand.
	 * @param b The second short operand.
	 * @return True if the subtraction produces a signed overflow.
	 */
	public static boolean willOverflowSub(final short a, final short b) {
		final int r = (short) (a - b);
		return ((a ^ b) & (a ^ r)) < 0;
	}

	/**
	 * Determines whether subtracting two ints as signed values produces an overflow.
	 *
	 * @param a The first int operand.
	 * @param b The second int operand.
	 * @return True if the subtraction produces a signed overflow.
	 */
	public static boolean willOverflowSub(final int a, final int b) {
		final int r = a - b;
		return ((a ^ b) & (a ^ r)) < 0;
	}

	/**
	 * Determines whether subtracting two longs as signed values produces an overflow.
	 *
	 * @param a The first long operand.
	 * @param b The second long operand.
	 * @return True if the subtraction produces a signed overflow.
	 */
	public static boolean willOverflowSub(final long a, final long b) {
		final long r = a - b;
		return ((a ^ b) & (a ^ r)) < 0L;
	}
}
