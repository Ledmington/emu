package com.ledmington.elf;

import java.util.Objects;

public final class ByteBuffer {

    private final byte[] b;
    private boolean isLittleEndian;
    private int i;
    private int alignment;

    public ByteBuffer(final byte[] b) {
        this.b = Objects.requireNonNull(b);
        this.i = 0;
        this.isLittleEndian = false;
        this.alignment = 1;
    }

    public void setEndianness(final boolean isLittleEndian) {
        this.isLittleEndian = isLittleEndian;
    }

    public int position() {
        return i;
    }

    public void setAlignment(final int alignment) {
        this.alignment = alignment == 0 ? 1 : alignment;
    }

    public void setPosition(final int pos) {
        this.i = pos;
    }

    private short asShort(final byte b) {
        return (short) (((short) b) & (short) 0x00ff);
    }

    private int asInt(final byte b) {
        return ((int) b) & 0x000000ff;
    }

    private long asLong(final byte b) {
        return ((long) b) & 0x00000000000000ffL;
    }

    private void move(final int amount) {
        i += Math.max(amount, alignment);
    }

    public byte read1() {
        final byte x = b[i];
        move(1);
        return x;
    }

    public short read2() {
        final short s;
        if (isLittleEndian) {
            s = (short) ((asShort(b[i + 1]) << 8) | asShort(b[i]));
        } else {
            s = (short) ((asShort(b[i]) << 8) | asShort(b[i + 1]));
        }
        move(2);
        return s;
    }

    public int read4() {
        final int x;
        if (isLittleEndian) {
            x = (asInt(b[i + 3]) << 24) | (asInt(b[i + 2]) << 16) | (asInt(b[i + 1]) << 8) | asInt(b[i]);
        } else {
            x = (asInt(b[i]) << 24) | (asInt(b[i + 1]) << 16) | (asInt(b[i + 2]) << 8) | asInt(b[i + 3]);
        }
        move(4);
        return x;
    }

    public long read4AsLong() {
        return ((long) read4()) & 0x00000000ffffffffL;
    }

    public long read8() {
        final long x;
        if (isLittleEndian) {
            x = (asLong(b[i + 7]) << 56)
                    | (asLong(b[i + 6]) << 48)
                    | (asLong(b[i + 5]) << 40)
                    | (asLong(b[i + 4]) << 32)
                    | (asLong(b[i + 3]) << 24)
                    | (asLong(b[i + 2]) << 16)
                    | (asLong(b[i + 1]) << 8)
                    | asLong(b[i]);
        } else {
            x = (asLong(b[i]) << 56)
                    | (asLong(b[i + 1]) << 48)
                    | (asLong(b[i + 2]) << 40)
                    | (asLong(b[i + 3]) << 32)
                    | (asLong(b[i + 4]) << 24)
                    | (asLong(b[i + 5]) << 16)
                    | (asLong(b[i + 6]) << 8)
                    | asLong(b[i + 7]);
        }
        move(8);
        return x;
    }
}
