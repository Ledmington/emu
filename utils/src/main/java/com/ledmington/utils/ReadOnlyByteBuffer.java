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
 * An abstract class for a ByteBuffer which allows only read operations. Most methods are already implemented which call
 * on the {@link #read()}, {@link #getPosition()} and {@link #setPosition(long)} methods.
 */
public abstract class ReadOnlyByteBuffer {

    /** Default endianness: true for little-endian, false for big-endian. */
    protected boolean isLE;

    /**
     * Creates a ReadOnlyByteBuffer with the given endianness.
     *
     * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
     */
    protected ReadOnlyByteBuffer(final boolean isLittleEndian) {
        this.isLE = isLittleEndian;
    }

    /**
     * Returns the current endianness.
     *
     * @return True for little-endian, false for big-endian.
     */
    public final boolean isLittleEndian() {
        return isLE;
    }

    /**
     * Changes the current endianness.
     *
     * @param isLittleEndian The new endianness: true for little-endian, false for big-endian.
     */
    public final void setEndianness(final boolean isLittleEndian) {
        this.isLE = isLittleEndian;
    }

    /**
     * Changes the position in the buffer.
     *
     * @param newPosition The new position in the buffer.
     */
    public abstract void setPosition(final long newPosition);

    /**
     * Returns the current position in the buffer.
     *
     * @return The current position in the buffer.
     */
    public abstract long getPosition();

    /**
     * Reads 1 byte from the buffer.
     *
     * @return The byte read.
     */
    protected abstract byte read();

    /**
     * Reads 1 byte and moves the cursor.
     *
     * @return The byte read.
     */
    public final byte read1() {
        final byte x = read();
        setPosition(getPosition() + 1L);
        return x;
    }

    /**
     * Reads 2 bytes with the current endianness.
     *
     * @return The 2 bytes read as a short.
     */
    public final short read2() {
        return isLE ? read2LE() : read2BE();
    }

    /**
     * Reads 2 bytes in little-endian without modifying the endianness.
     *
     * @return The 2 bytes read as a short.
     */
    public final short read2LE() {
        short x = (short) 0x0000;
        x |= BitUtils.asShort(read1());
        x |= BitUtils.asShort(read1() << 8);
        return x;
    }

    /**
     * Reads 2 bytes in big-endian without modifying the endianness.
     *
     * @return The 2 bytes read as a short.
     */
    public final short read2BE() {
        short x = (short) 0x0000;
        x |= BitUtils.asShort(read1() << 8);
        x |= BitUtils.asShort(read1());
        return x;
    }

    /**
     * Reads 4 bytes with the current endianness.
     *
     * @return The 4 bytes read as an int.
     */
    public final int read4() {
        return isLE ? read4LE() : read4BE();
    }

    /**
     * Reads 4 bytes in little-endian without modifying the endianness.
     *
     * @return The 4 bytes read as an int.
     */
    public final int read4LE() {
        int x = 0x00000000;
        x |= BitUtils.asInt(read1());
        x |= (BitUtils.asInt(read1()) << 8);
        x |= (BitUtils.asInt(read1()) << 16);
        x |= (BitUtils.asInt(read1()) << 24);
        return x;
    }

    /**
     * Reads 4 bytes in big-endian without modifying the endianness.
     *
     * @return The 4 bytes read as an int.
     */
    public final int read4BE() {
        int x = 0x00000000;
        x |= (BitUtils.asInt(read1()) << 24);
        x |= (BitUtils.asInt(read1()) << 16);
        x |= (BitUtils.asInt(read1()) << 8);
        x |= BitUtils.asInt(read1());
        return x;
    }

    /**
     * Reads 8 bytes with the current endianness.
     *
     * @return The 8 bytes read as a long.
     */
    public final long read8() {
        return isLE ? read8LE() : read8BE();
    }

    /**
     * Reads 8 bytes in little-endian without modifying the endianness.
     *
     * @return The 8 bytes read as a long.
     */
    public final long read8LE() {
        long x = 0x0000000000000000L;
        x |= BitUtils.asLong(read1());
        x |= (BitUtils.asLong(read1()) << 8);
        x |= (BitUtils.asLong(read1()) << 16);
        x |= (BitUtils.asLong(read1()) << 24);
        x |= (BitUtils.asLong(read1()) << 32);
        x |= (BitUtils.asLong(read1()) << 40);
        x |= (BitUtils.asLong(read1()) << 48);
        x |= (BitUtils.asLong(read1()) << 56);
        return x;
    }

    /**
     * Reads 8 bytes in big-endian without modifying the endianness.
     *
     * @return The 8 bytes read as a long.
     */
    public final long read8BE() {
        long x = 0x0000000000000000L;
        x |= (BitUtils.asLong(read1()) << 56);
        x |= (BitUtils.asLong(read1()) << 48);
        x |= (BitUtils.asLong(read1()) << 40);
        x |= (BitUtils.asLong(read1()) << 32);
        x |= (BitUtils.asLong(read1()) << 24);
        x |= (BitUtils.asLong(read1()) << 16);
        x |= (BitUtils.asLong(read1()) << 8);
        x |= BitUtils.asLong(read1());
        return x;
    }
}
