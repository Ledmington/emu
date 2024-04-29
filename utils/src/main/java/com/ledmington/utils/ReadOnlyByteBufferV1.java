package com.ledmington.utils;

import java.util.Objects;

/**
 * A buffer which allows reading with endianness.
 */
public final class ReadOnlyByteBufferV1 extends ReadOnlyByteBuffer {

    private final byte[] b;
    private long position;

    public ReadOnlyByteBufferV1(final byte[] b) {
        this(b, false);
    }

    public ReadOnlyByteBufferV1(final byte[] b, final boolean isLittleEndian) {
        super(isLittleEndian);
        this.b = Objects.requireNonNull(b);
        this.position = 0L;
    }

    @Override
    public long position() {
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
        return "ByteBuffer[b=" + b.toString() + ";i=" + position + ";isLittleEndian=" + isLittleEndian + "]";
    }

    @Override
    public int hashCode() {
        int h = 17;
        for (final byte x : this.b) {
            h = 31 * h + BitUtils.asInt(x);
        }
        h = 31 * h + (BitUtils.asInt(position) ^ BitUtils.asInt(position >>> 32));
        h = 31 * h + (isLittleEndian ? 1 : 0);
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
        return this.position == bb.position && this.isLittleEndian == bb.isLittleEndian;
    }
}
