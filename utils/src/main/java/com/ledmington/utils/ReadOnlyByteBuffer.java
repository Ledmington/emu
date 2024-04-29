package com.ledmington.utils;

public abstract class ReadOnlyByteBuffer {

    protected boolean isLittleEndian = false;

    protected ReadOnlyByteBuffer(final boolean isLittleEndian) {
        this.isLittleEndian = isLittleEndian;
    }

    /**
     * Returns the current endianness: {@code true} for
     * little-endian, {@code false} for big-endian.
     */
    public final boolean isLittleEndian() {
        return isLittleEndian;
    }

    /**
     * Changes the current endianness: {@code true} for
     * little-endian, {@code false} for big-endian.
     */
    public final void setEndianness(final boolean isLittleEndian) {
        this.isLittleEndian = isLittleEndian;
    }

    /**
     * Changes the position in the buffer.
     */
    public abstract void setPosition(final long newPosition);

    /**
     * Returns the current position in the buffer.
     */
    public abstract long position();

    protected abstract byte read();

    /**
     * Reads 1 byte.
     */
    public final byte read1() {
        final byte x = read();
        setPosition(position() + 1L);
        return x;
    }

    /**
     * Reads 2 bytes with the current endianness.
     */
    public final short read2() {
        return isLittleEndian ? read2LE() : read2BE();
    }

    /**
     * Reads 2 bytes in little-endian without modifying the endianness.
     */
    public final short read2LE() {
        short x = (short) 0x0000;
        x |= BitUtils.asShort(read1());
        x |= BitUtils.asShort(read1() << 8);
        return x;
    }

    /**
     * Reads 2 bytes in big-endian without modifying the endianness.
     */
    public final short read2BE() {
        short x = (short) 0x0000;
        x |= BitUtils.asShort(read1() << 8);
        x |= BitUtils.asShort(read1());
        return x;
    }

    /**
     * Reads 4 bytes with the current endianness.
     */
    public final int read4() {
        return isLittleEndian ? read4LE() : read4BE();
    }

    /**
     * Reads 4 bytes in little-endian without modifying the endianness.
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
     */
    public final long read8() {
        return isLittleEndian ? read8LE() : read8BE();
    }

    /**
     * Reads 8 bytes in little-endian without modifying the endianness.
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
