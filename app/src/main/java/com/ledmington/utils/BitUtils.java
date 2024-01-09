package com.ledmington.utils;

public final class BitUtils {
    private BitUtils() {}

    public static byte asByte(final int x) {
        return (byte) (x & 0x000000ff);
    }

    public static byte parseByte(final String s) {
        if (s.length() != 2) {
            throw new IllegalArgumentException(String.format("Expected length to be 2, but was %,d", s.length()));
        }
        return asByte(Integer.parseInt(s, 16));
    }
}
