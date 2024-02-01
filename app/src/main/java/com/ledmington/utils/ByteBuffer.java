package com.ledmington.utils;

import java.util.Objects;

public final class ByteBuffer {

    private static final MiniLogger logger = MiniLogger.getLogger("byte-buffer");

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

    public void setEndianness(final boolean isLittleEndian) {
        this.isLittleEndian = isLittleEndian;
    }

    public int position() {
        return i;
    }

    public void setPosition(final int pos) {
        this.i = pos;
    }

    public void goBack(final int n) {
        // logger.debug("Goind back %,d bytes", n);
        this.i -= n;
    }

    private void move(final int amount) {
        i += amount;
    }

    public byte read1() {
        final byte x = b[i];
        move(1);
        // logger.debug("Read 0x%02x", x);
        return x;
    }

    public short read2() {
        final short s;
        if (isLittleEndian) {
            s = (short) ((BitUtils.asShort(b[i + 1]) << 8) | BitUtils.asShort(b[i]));
        } else {
            s = (short) ((BitUtils.asShort(b[i]) << 8) | BitUtils.asShort(b[i + 1]));
        }
        move(2);
        // logger.debug("Read 0x%04x", s);
        return s;
    }

    public int read4() {
        final int x;
        if (isLittleEndian) {
            x = (BitUtils.asInt(b[i + 3]) << 24)
                    | (BitUtils.asInt(b[i + 2]) << 16)
                    | (BitUtils.asInt(b[i + 1]) << 8)
                    | BitUtils.asInt(b[i]);
        } else {
            x = (BitUtils.asInt(b[i]) << 24)
                    | (BitUtils.asInt(b[i + 1]) << 16)
                    | (BitUtils.asInt(b[i + 2]) << 8)
                    | BitUtils.asInt(b[i + 3]);
        }
        move(4);
        // logger.debug("Read 0x%08x", x);
        return x;
    }

    public long read8() {
        final long x;
        if (isLittleEndian) {
            x = (BitUtils.asLong(b[i + 7]) << 56)
                    | (BitUtils.asLong(b[i + 6]) << 48)
                    | (BitUtils.asLong(b[i + 5]) << 40)
                    | (BitUtils.asLong(b[i + 4]) << 32)
                    | (BitUtils.asLong(b[i + 3]) << 24)
                    | (BitUtils.asLong(b[i + 2]) << 16)
                    | (BitUtils.asLong(b[i + 1]) << 8)
                    | BitUtils.asLong(b[i]);
        } else {
            x = (BitUtils.asLong(b[i]) << 56)
                    | (BitUtils.asLong(b[i + 1]) << 48)
                    | (BitUtils.asLong(b[i + 2]) << 40)
                    | (BitUtils.asLong(b[i + 3]) << 32)
                    | (BitUtils.asLong(b[i + 4]) << 24)
                    | (BitUtils.asLong(b[i + 5]) << 16)
                    | (BitUtils.asLong(b[i + 6]) << 8)
                    | BitUtils.asLong(b[i + 7]);
        }
        move(8);
        // logger.debug("Read 0x%016x", x);
        return x;
    }
}
