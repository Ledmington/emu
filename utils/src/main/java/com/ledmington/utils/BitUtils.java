package com.ledmington.utils;

/**
 * The existence of this class justifies a porting of the code
 * to a more "bit-aware" language like C, C++ or Rust.
 */
public final class BitUtils {

    private BitUtils() {}

    public static byte asByte(final int x) {
        return (byte) (x & 0x000000ff);
    }

    public static byte asByte(final long x) {
        return (byte) (x & 0x00000000000000ffL);
    }

    public static short asShort(final byte b) {
        return (short) (((short) b) & (short) 0x00ff);
    }

    public static short asShort(final long l) {
        return (short) (l & 0x000000000000ffffL);
    }

    public static int asInt(final byte b) {
        return ((int) b) & 0x000000ff;
    }

    public static int asInt(final long x) {
        return (int) (x & 0x00000000ffffffffL);
    }

    public static long asLong(final byte b) {
        return ((long) b) & 0x00000000000000ffL;
    }

    public static long asLong(final short s) {
        return ((long) s) & 0x000000000000ffffL;
    }

    public static long asLong(final int x) {
        return ((long) x) & 0x00000000ffffffffL;
    }

    public static byte parseByte(final String s) {
        if (s.length() != 2) {
            throw new IllegalArgumentException(String.format("Expected length to be 2, but was %,d", s.length()));
        }
        return asByte(Integer.parseInt(s, 16));
    }

    public static String toBinaryString(final byte b) {
        String s = Integer.toBinaryString(asInt(b));
        if (s.length() < 8) {
            s = "0".repeat(8 - s.length()) + s;
        }
        return s;
    }

    public static byte and(final byte a, final byte b) {
        return asByte(a & b);
    }

    public static byte or(final byte a, final byte b) {
        return asByte(a | b);
    }

    public static byte or(final byte a, final byte b, final byte c, final byte d, final byte e) {
        return asByte(a | b | c | d | e);
    }

    public static byte xor(final byte a, final byte b) {
        return BitUtils.asByte(a ^ b);
    }

    public static byte shr(final byte b, final int x) {
        return asByte((b >>> x) & (0x000000ff >>> x));
    }
}
