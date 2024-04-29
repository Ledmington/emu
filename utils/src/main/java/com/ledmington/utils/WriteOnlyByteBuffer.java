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

    public void write(final byte x) {
        v[i++] = x;
    }

    public void write(final short x) {
        if (isLittleEndian) {
            writeLE(x);
        } else {
            writeBE(x);
        }
    }

    private void writeLE(final short x) {
        v[i++] = (byte) (x & ((short) 0x00ff));
        v[i++] = (byte) (x >>> 8);
    }

    private void writeBE(final short x) {
        v[i++] = (byte) (x >>> 8);
        v[i++] = (byte) (x & ((short) 0x00ff));
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

    public void write(final long x) {
        if (isLittleEndian) {
            writeLE(x);
        } else {
            writeBE(x);
        }
    }

    private void writeLE(final long x) {
        v[i++] = (byte) (x & 0x00000000000000ffL);
        v[i++] = (byte) ((x & 0x000000000000ff00L) >>> 8);
        v[i++] = (byte) ((x & 0x0000000000ff0000L) >>> 16);
        v[i++] = (byte) ((x & 0x00000000ff000000L) >>> 24);
        v[i++] = (byte) ((x & 0x000000ff00000000L) >>> 32);
        v[i++] = (byte) ((x & 0x0000ff0000000000L) >>> 40);
        v[i++] = (byte) ((x & 0x00ff000000000000L) >>> 48);
        v[i++] = (byte) (x >>> 56);
    }

    private void writeBE(final long x) {
        v[i++] = (byte) (x >>> 56);
        v[i++] = (byte) ((x & 0x00ff000000000000L) >>> 48);
        v[i++] = (byte) ((x & 0x0000ff0000000000L) >>> 40);
        v[i++] = (byte) ((x & 0x000000ff00000000L) >>> 32);
        v[i++] = (byte) ((x & 0x00000000ff000000L) >>> 24);
        v[i++] = (byte) ((x & 0x0000000000ff0000L) >>> 16);
        v[i++] = (byte) ((x & 0x000000000000ff00L) >>> 8);
        v[i++] = (byte) (x & 0x00000000000000ffL);
    }

    public void write(final byte[] arr) {
        System.arraycopy(arr, 0, v, i, arr.length);
        i += arr.length;
    }

    public void write(final int[] arr) {
        if (isLittleEndian) {
            for (final int x : arr) {
                writeLE(x);
            }
        } else {
            for (final int x : arr) {
                writeBE(x);
            }
        }
    }

    public byte[] array() {
        return v;
    }
}
