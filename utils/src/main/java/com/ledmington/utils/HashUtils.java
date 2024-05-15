package com.ledmington.utils;

public final class HashUtils {

    private HashUtils() {}

    public static int hash(final boolean b) {
        return b ? 1 : 0;
    }

    public static int hash(final byte b) {
        return BitUtils.asInt(b);
    }

    public static int hash(final short s) {
        return BitUtils.asInt(s);
    }

    public static int hash(final long l) {
        return BitUtils.asInt(l >>> 32) ^ BitUtils.asInt(l);
    }
}
