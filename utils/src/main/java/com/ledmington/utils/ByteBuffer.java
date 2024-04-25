package com.ledmington.utils;

import java.util.Objects;

/**
 * A buffer which allows reading with endianness.
 */
public final class ByteBuffer {

    private final byte[] b;
    private boolean isLittleEndian;
    private int i;

    public ByteBuffer(final byte[] b) {
        this(b, false);
    }

    public ByteBuffer(final byte[] b, final boolean isLittleEndian) {
        this.b = Objects.requireNonNull(b);
        this.i = 0;
        this.isLittleEndian = isLittleEndian;
    }

    /**
     * Returns the current endianness: {@code true} for
     * little-endian, {@code false} for big-endian.
     */
    public boolean isLittleEndian() {
        return isLittleEndian;
    }

    /**
     * Changes the current endianness: {@code true} for
     * little-endian, {@code false} for big-endian.
     */
    public void setEndianness(final boolean isLittleEndian) {
        this.isLittleEndian = isLittleEndian;
    }

    /**
     * Returns the current position in the buffer.
     */
    public int position() {
        return i;
    }

    /**
     * Changes the position in the buffer.
     */
    public void setPosition(final int pos) {
        i = pos;
    }

    /**
     * Returns the amount of bytes in the buffer.
     */
    public int length() {
        return b.length;
    }

    /**
     * Moves the cursor back by the given amunt of bytes.
     *
     * @param n
     *      The amount of byte to move back.
     */
    public void goBack(final int n) {
        move(-n);
    }

    private void move(final int amount) {
        i += amount;
    }

    /**
     * Reads 1 byte.
     */
    public byte read1() {
        final byte x = b[i];
        move(1);
        return x;
    }

    /**
     * Reads 2 bytes with the current endianness.
     */
    public short read2() {
        return isLittleEndian ? read2LittleEndian() : read2BigEndian();
    }

    /**
     * Reads 2 bytes in little-endian without modifying the endianness.
     */
    public short read2LittleEndian() {
        final short s = (short) ((BitUtils.asShort(b[i + 1]) << 8) | BitUtils.asShort(b[i]));
        move(2);
        return s;
    }

    /**
     * Reads 2 bytes in big-endian without modifying the endianness.
     */
    public short read2BigEndian() {
        final short s = (short) ((BitUtils.asShort(b[i]) << 8) | BitUtils.asShort(b[i + 1]));
        move(2);
        return s;
    }

    /**
     * Reads 4 bytes with the current endianness.
     */
    public int read4() {
        return isLittleEndian ? read4LittleEndian() : read4BigEndian();
    }

    /**
     * Reads 4 bytes in little-endian without modifying the endianness.
     */
    public int read4LittleEndian() {
        final int x = (BitUtils.asInt(b[i + 3]) << 24)
                | (BitUtils.asInt(b[i + 2]) << 16)
                | (BitUtils.asInt(b[i + 1]) << 8)
                | BitUtils.asInt(b[i]);
        move(4);
        return x;
    }

    /**
     * Reads 4 bytes in big-endian without modifying the endianness.
     */
    public int read4BigEndian() {
        final int x = (BitUtils.asInt(b[i]) << 24)
                | (BitUtils.asInt(b[i + 1]) << 16)
                | (BitUtils.asInt(b[i + 2]) << 8)
                | BitUtils.asInt(b[i + 3]);
        move(4);
        return x;
    }

    /**
     * Reads 8 bytes with the current endianness.
     */
    public long read8() {
        return isLittleEndian ? read8LittleEndian() : read8BigEndian();
    }

    /**
     * Reads 8 bytes in little-endian without modifying the endianness.
     */
    public long read8LittleEndian() {
        final long x = (BitUtils.asLong(b[i + 7]) << 56)
                | (BitUtils.asLong(b[i + 6]) << 48)
                | (BitUtils.asLong(b[i + 5]) << 40)
                | (BitUtils.asLong(b[i + 4]) << 32)
                | (BitUtils.asLong(b[i + 3]) << 24)
                | (BitUtils.asLong(b[i + 2]) << 16)
                | (BitUtils.asLong(b[i + 1]) << 8)
                | BitUtils.asLong(b[i]);
        move(8);
        return x;
    }

    /**
     * Reads 8 bytes in big-endian without modifying the endianness.
     */
    public long read8BigEndian() {
        final long x = (BitUtils.asLong(b[i]) << 56)
                | (BitUtils.asLong(b[i + 1]) << 48)
                | (BitUtils.asLong(b[i + 2]) << 40)
                | (BitUtils.asLong(b[i + 3]) << 32)
                | (BitUtils.asLong(b[i + 4]) << 24)
                | (BitUtils.asLong(b[i + 5]) << 16)
                | (BitUtils.asLong(b[i + 6]) << 8)
                | BitUtils.asLong(b[i + 7]);
        move(8);
        return x;
    }

    public String toString() {
        return "ByteBuffer[b=" + b.toString() + ";i=" + i + ";isLittleEndian=" + isLittleEndian + "]";
    }

    public int hashCode() {
        int h = 17;
        for (final byte x : this.b) {
            h = 31 * h + BitUtils.asInt(x);
        }
        h = 31 * h + i;
        h = 31 * h + (isLittleEndian ? 1 : 0);
        return h;
    }

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
        final ByteBuffer bb = (ByteBuffer) other;
        if (this.b.length != bb.b.length) {
            return false;
        }
        for (int i = 0; i < this.b.length; i++) {
            if (this.b[i] != bb.b[i]) {
                return false;
            }
        }
        return this.i == bb.i && this.isLittleEndian == bb.isLittleEndian;
    }
}
