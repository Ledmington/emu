package com.ledmington.utils;

public final class WriteOnlyByteBuffer {

    private final byte[] v;
    private final boolean isLittleEndian;
    private int i = 0;

    public WriteOnlyByteBuffer(final int length, final boolean isLittleEndian) {
        this.v = new byte[length];
        this.isLittleEndian = isLittleEndian;
    }

    public WriteOnlyByteBuffer(final int length) {
        this(length, false);
    }

    public void write(final int x) {
        if (isLittleEndian) {
            writeLE(x);
        } else {
            writeBE(x);
        }
    }

    private void writeLE(final int x) {
        v[i++] = (byte) (x & 0x000000ff);
        v[i++] = (byte) ((x & 0x0000ff00) >>> 8);
        v[i++] = (byte) ((x & 0x00ff0000) >>> 16);
        v[i++] = (byte) (x >>> 24);
    }

    private void writeBE(final int x) {
        v[i++] = (byte) (x >>> 24);
        v[i++] = (byte) ((x & 0x00ff0000) >>> 16);
        v[i++] = (byte) ((x & 0x0000ff00) >>> 8);
        v[i++] = (byte) (x & 0x000000ff);
    }

    public void write(final byte[] arr) {
        System.arraycopy(arr, 0, v, i, arr.length);
        i += arr.length;
    }

    public byte[] array() {
        return v;
    }
}
