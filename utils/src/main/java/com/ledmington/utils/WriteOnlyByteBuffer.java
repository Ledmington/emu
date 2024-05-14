package com.ledmington.utils;

/**
 * A ByteBuffer which allows only write operations.
 */
public final class WriteOnlyByteBuffer {

    private final byte[] v;
    private final boolean isLittleEndian;
    private int i;

    /**
     * Creates an empty WriteOnlyByteBuffer with the given length and the given endianness.
     *
     * @param length
     *      The length of the underlying array.
     * @param isLittleEndian
     *      The endianness: true for little-endian, false for big-endian.
     */
    public WriteOnlyByteBuffer(final int length, final boolean isLittleEndian) {
        this.v = new byte[length];
        this.isLittleEndian = isLittleEndian;
    }

    /**
     * Creates an little-endian WriteOnlyByteBuffer with the given length. It is equivalent to calling {@code new WriteOnlyByteBuffer(length, false)}.
     *
     * @param length
     *      The length of the underlying array.
     */
    public WriteOnlyByteBuffer(final int length) {
        this(length, false);
    }

    /**
     * Writes the given byte at the current position.
     *
     * @param x
     *      The byte to be written.
     */
    public void write(final byte x) {
        v[i++] = x;
    }

    /**
     * Writes the given short with the current endianness.
     *
     * @param x
     *      The short to be written.
     */
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

    /**
     * Writes the given int with the current endianness.
     *
     * @param x
     *      The int to be written.
     */
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

    /**
     * Writes the given long with the current endianness.
     *
     * @param x
     *      The long to be written.
     */
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

    /**
     * Writes the given array of bytes. It is equivalent to doing
     * <code>
     *     for (int i=0; i&lt;arr.length; i++) {
     *         write(arr[i]);
     *     }
     * </code>
     *
     * @param arr
     *      The array of bytes to be written.
     */
    public void write(final byte[] arr) {
        System.arraycopy(arr, 0, v, i, arr.length);
        i += arr.length;
    }

    /**
     * Writes the given array of ints witht he current endianness. It is equivalent to doing
     * <code>
     *     for (int i=0; i&lt;arr.length; i++) {
     *         write(arr[i]);
     *     }
     * </code>
     *
     * @param arr
     *      The array of ints to be written.
     */
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

    /**
     * Returns the underlying array.
     *
     * @return
     *      The underlying array.
     */
    public byte[] array() {
        return v;
    }
}
