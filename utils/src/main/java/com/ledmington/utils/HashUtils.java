package com.ledmington.utils;

/**
 * Common utilities for {@code hashCode} implementation.
 */
public final class HashUtils {

    private HashUtils() {}

    /**
     * Hashes the given boolean value as a 32-bit int.
     *
     * @param b
     *          The boolean to be hashed.
     * @return
     *         A 32-bit hash.
     */
    public static int hash(final boolean b) {
        return b ? 1 : 0;
    }

    /**
     * Hashes the given byte value as a 32-bit int.
     *
     * @param b
     *          The byte to be hashed.
     * @return
     *         A 32-bit hash.
     */
    public static int hash(final byte b) {
        return BitUtils.asInt(b);
    }

    /**
     * Hashes the given short value as a 32-bit int.
     *
     * @param s
     *          The short to be hashed.
     * @return
     *         A 32-bit hash.
     */
    public static int hash(final short s) {
        return BitUtils.asInt(s);
    }

    /**
     * Hashes the given long value as a 32-bit int.
     *
     * @param l
     *          The long to be hashed.
     * @return
     *         A 32-bit hash.
     */
    public static int hash(final long l) {
        return BitUtils.asInt(l >>> 32) ^ BitUtils.asInt(l);
    }
}
