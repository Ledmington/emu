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

import com.ledmington.utils.BitUtils;

/**
 * This class represents an x86 SIB (Scale-Index-Base) byte, used for representing indirect operands inside x86
 * instructions.
 */
public final class SIB {

    private final byte scaleByte;
    private final byte indexByte;
    private final byte baseByte;

    /**
     * Extracts the Scale portion of the given byte interpreted as a SIB byte.
     *
     * @param sib The SIB byte.
     * @return The Scale of the given byte.
     */
    public static byte extractScale(final byte sib) {
        final byte SIB_SCALE_MASK = (byte) 0b11000000;
        return BitUtils.shr(BitUtils.and(sib, SIB_SCALE_MASK), 6);
    }

    /**
     * Extracts the Index portion of the given byte interpreted as a SIB byte.
     *
     * @param sib The SIB byte.
     * @return The Index of the given byte.
     */
    public static byte extractIndex(final byte sib) {
        final byte SIB_INDEX_MASK = (byte) 0b00111000;
        return BitUtils.shr(BitUtils.and(sib, SIB_INDEX_MASK), 3);
    }

    /**
     * Extracts the Base portion of the given byte interpreted as a SIB byte.
     *
     * @param sib The SIB byte.
     * @return The Base of the given byte.
     */
    public static byte extractBase(final byte sib) {
        final byte SIB_BASE_MASK = (byte) 0b00000111;
        return BitUtils.and(sib, SIB_BASE_MASK);
    }

    /**
     * Creates a SIB object by parsing the given SIB byte.
     *
     * @param sib The SIB byte to be parsed.
     */
    public SIB(final byte sib) {
        this.scaleByte = extractScale(sib);
        this.indexByte = extractIndex(sib);
        this.baseByte = extractBase(sib);
    }

    /**
     * Returns the scale of this SIB.
     *
     * @return The scale of this SIB.
     */
    public byte scale() {
        return scaleByte;
    }

    /**
     * Returns the index of this SIB.
     *
     * @return The index of this SIB.
     */
    public byte index() {
        return indexByte;
    }

    /**
     * Returns the base of this SIB.
     *
     * @return The base of this SIB.
     */
    public byte base() {
        return baseByte;
    }

    @Override
    public String toString() {
        return "scale:" + BitUtils.toBinaryString(scaleByte).substring(6, 8) + " index:"
                + BitUtils.toBinaryString(indexByte).substring(5, 8) + " base:"
                + BitUtils.toBinaryString(baseByte).substring(5, 8);
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + BitUtils.asInt(scaleByte);
        h = 31 * h + BitUtils.asInt(indexByte);
        h = 31 * h + BitUtils.asInt(baseByte);
        return h;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final SIB s = (SIB) other;
        return this.scaleByte == s.scaleByte && this.indexByte == s.indexByte && this.baseByte == s.baseByte;
    }
}
