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

/** Common utilities for {@code hashCode} implementation. */
public final class HashUtils {

    private HashUtils() {}

    /**
     * Hashes the given boolean value as a 32-bit int.
     *
     * @param b The boolean to be hashed.
     * @return A 32-bit hash.
     */
    public static int hash(final boolean b) {
        return b ? 1 : 0;
    }

    /**
     * Hashes the given byte value as a 32-bit int.
     *
     * @param b The byte to be hashed.
     * @return A 32-bit hash.
     */
    public static int hash(final byte b) {
        return BitUtils.asInt(b);
    }

    /**
     * Hashes the given short value as a 32-bit int.
     *
     * @param s The short to be hashed.
     * @return A 32-bit hash.
     */
    public static int hash(final short s) {
        return BitUtils.asInt(s);
    }

    /**
     * Hashes the given long value as a 32-bit int.
     *
     * @param l The long to be hashed.
     * @return A 32-bit hash.
     */
    public static int hash(final long l) {
        return BitUtils.asInt(l >>> 32) ^ BitUtils.asInt(l);
    }
}
