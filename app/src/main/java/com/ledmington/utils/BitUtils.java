package com.ledmington.utils;

public final class BitUtils {
    private BitUtils() {}

    public static byte asByte(final int x) {
        return (byte) (x & 0x000000ff);
    }

    public static short asShort(final byte b) {
        return (short) (((short) b) & (short) 0x00ff);
    }

    public static int asInt(final byte b) {
        return ((int) b) & 0x000000ff;
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
}
