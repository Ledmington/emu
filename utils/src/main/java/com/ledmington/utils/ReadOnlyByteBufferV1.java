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

import java.util.Arrays;
import java.util.Objects;

/** A buffer which allows reading with endianness. This implementation uses a byte array. */
public final class ReadOnlyByteBufferV1 extends ReadOnlyByteBuffer {

    private final byte[] b;
    private long position;

    /**
     * Creates a big-endian ReadOnlyByteBufferV1 with the given array. It is equivalent to calling {@code new
     * ReadOnlyByteBufferV1(b, false, 1)}.
     *
     * @param b The byte array ot be used.
     */
    public ReadOnlyByteBufferV1(final byte[] b) {
        this(b, false, 1L);
    }

    /**
     * Creates a ReadOnlyByteBufferV1 with the given array and the given endianness. Equivalent to calling {@code new
     * ReadOnlyByteBufferV1(b, e, 1)}.
     *
     * @param b The byte array ot be used.
     * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
     */
    public ReadOnlyByteBufferV1(final byte[] b, final boolean isLittleEndian) {
        this(b, isLittleEndian, 1L);
    }

    /**
     * Creates a ReadOnlyByteBufferV1 with the given array, the given endianness and the given alignment.
     *
     * @param b The byte array ot be used.
     * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
     * @param alignment The byte alignment to be used while reading.
     */
    public ReadOnlyByteBufferV1(final byte[] b, final boolean isLittleEndian, final long alignment) {
        super(isLittleEndian, alignment);
        this.b = Objects.requireNonNull(b);
        this.position = 0L;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public void setPosition(final long newPosition) {
        position = newPosition;
    }

    @Override
    protected byte read() {
        return b[BitUtils.asInt(position)];
    }

    @Override
    public String toString() {
        return "ReadOnlyByteBufferV1(b=" + Arrays.toString(b) + ";i=" + position + ";isLittleEndian=" + isLE + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        for (final byte x : this.b) {
            h = 31 * h + BitUtils.asInt(x);
        }
        h = 31 * h + HashUtils.hash(position);
        h = 31 * h + HashUtils.hash(isLE);
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
        final ReadOnlyByteBufferV1 bb = (ReadOnlyByteBufferV1) other;
        if (this.b.length != bb.b.length) {
            return false;
        }
        for (int i = 0; i < this.b.length; i++) {
            if (this.b[i] != bb.b[i]) {
                return false;
            }
        }
        return this.position == bb.position && this.isLE == bb.isLE;
    }
}
