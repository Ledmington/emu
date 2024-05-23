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

    /** Byte alignment */
    protected long alignment;

    /**
     * Creates a ReadOnlyByteBuffer with the given endianness and the given alignment.
     *
     * @param isLittleEndian The endianness: true for little-endian, false for big-endian.
     */
    protected ReadOnlyByteBuffer(final boolean isLittleEndian, final long alignment) {
        this.isLE = isLittleEndian;

        checkAlignment(alignment);
        this.alignment = alignment;
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

    private void checkAlignment(final long alignment) {
        if (alignment <= 0 || Long.bitCount(alignment) != 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid alignment: expected a power of two >0 but was %,d", alignment));
        }
    }

    public void setAlignment(final long newAlignment) {
        checkAlignment(newAlignment);
        this.alignment = newAlignment;
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

    private void move() {
        setPosition(getPosition() + 1L);
    }

    private void moveAndAlign() {
        move();
        if (getPosition() % alignment != 0) {
            setPosition((getPosition() / alignment + 1L) * alignment);
        }
    }

    /**
     * Reads 1 byte and moves the cursor.
     *
     * @return The byte read.
     */
    public final byte read1() {
        final byte x = read();
        moveAndAlign();
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
        x |= BitUtils.asShort(read());
        move();
        x |= BitUtils.asShort(read() << 8);
        moveAndAlign();
        return x;
    }

    /**
     * Reads 2 bytes in big-endian without modifying the endianness.
     *
     * @return The 2 bytes read as a short.
     */
    public final short read2BE() {
        short x = (short) 0x0000;
        x |= BitUtils.asShort(read() << 8);
        move();
        x |= BitUtils.asShort(read());
        moveAndAlign();
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
        x |= BitUtils.asInt(read());
        move();
        x |= (BitUtils.asInt(read()) << 8);
        move();
        x |= (BitUtils.asInt(read()) << 16);
        move();
        x |= (BitUtils.asInt(read()) << 24);
        moveAndAlign();
        return x;
    }

    /**
     * Reads 4 bytes in big-endian without modifying the endianness.
     *
     * @return The 4 bytes read as an int.
     */
    public final int read4BE() {
        int x = 0x00000000;
        x |= (BitUtils.asInt(read()) << 24);
        move();
        x |= (BitUtils.asInt(read()) << 16);
        move();
        x |= (BitUtils.asInt(read()) << 8);
        move();
        x |= BitUtils.asInt(read());
        moveAndAlign();
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
        x |= BitUtils.asLong(read());
        move();
        x |= (BitUtils.asLong(read()) << 8);
        move();
        x |= (BitUtils.asLong(read()) << 16);
        move();
        x |= (BitUtils.asLong(read()) << 24);
        move();
        x |= (BitUtils.asLong(read()) << 32);
        move();
        x |= (BitUtils.asLong(read()) << 40);
        move();
        x |= (BitUtils.asLong(read()) << 48);
        move();
        x |= (BitUtils.asLong(read()) << 56);
        moveAndAlign();
        return x;
    }

    /**
     * Reads 8 bytes in big-endian without modifying the endianness.
     *
     * @return The 8 bytes read as a long.
     */
    public final long read8BE() {
        long x = 0x0000000000000000L;
        x |= (BitUtils.asLong(read()) << 56);
        move();
        x |= (BitUtils.asLong(read()) << 48);
        move();
        x |= (BitUtils.asLong(read()) << 40);
        move();
        x |= (BitUtils.asLong(read()) << 32);
        move();
        x |= (BitUtils.asLong(read()) << 24);
        move();
        x |= (BitUtils.asLong(read()) << 16);
        move();
        x |= (BitUtils.asLong(read()) << 8);
        move();
        x |= BitUtils.asLong(read());
        moveAndAlign();
        return x;
    }
}
