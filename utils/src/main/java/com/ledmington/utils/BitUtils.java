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
        return (short) (((short) b) & (short) 0x00ff);
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
        return ((int) b) & 0x000000ff;
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
        return ((long) b) & 0x00000000000000ffL;
    }

    /**
     * Converts the given short to a long by zero-extending it.
     *
     * @param s The short to be converted.
     * @return A long with the given short as the 2 rightmost bytes.
     */
    public static long asLong(final short s) {
        return ((long) s) & 0x000000000000ffffL;
    }

    /**
     * Converts the given int to a long by zero-extending it.
     *
     * @param x The int to be converted.
     * @return A long with the given int as the 4 rightmost bytes.
     */
    public static long asLong(final int x) {
        return ((long) x) & 0x00000000ffffffffL;
    }

    /**
     * Returns an 8 character long string containing the bits of the given byte.
     *
     * @param b The byte to be converted.
     * @return A String with the bits of b.
     */
    public static String toBinaryString(final byte b) {
        String s = Integer.toBinaryString(asInt(b));
        final int byteLength = 8;
        if (s.length() < byteLength) {
            s = "0".repeat(byteLength - s.length()) + s;
        }
        return s;
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
        return BitUtils.asByte(a ^ b);
    }

    /**
     * Shift (logical) to the right and return as a byte.
     *
     * @param b The byte to be shifted.
     * @param x The number of bits to shift.
     * @return The shifted byte, as a byte.
     */
    public static byte shr(final byte b, final int x) {
        return asByte((b >>> x) & (0x000000ff >>> x));
    }

    /**
     * Shift (logical) to the left and return as a byte.
     *
     * @param b The byte to be shifted.
     * @param x The number of bits to shift.
     * @return The shifted byte, as a byte.
     */
    public static byte shl(final byte b, final int x) {
        return asByte(b << x);
    }
}
