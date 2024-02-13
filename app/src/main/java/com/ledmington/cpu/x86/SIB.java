package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

public final class SIB {

    private final byte scale;
    private final byte index;
    private final byte base;

    public static byte extractScale(final byte sib) {
        final byte SIB_SCALE_MASK = (byte) 0xc0; // 11000000
        return BitUtils.shr(BitUtils.and(sib, SIB_SCALE_MASK), 6);
    }

    public static byte extractIndex(final byte sib) {
        final byte SIB_INDEX_MASK = (byte) 0x38; // 00111000
        return BitUtils.shr(BitUtils.and(sib, SIB_INDEX_MASK), 3);
    }

    public static byte extractBase(final byte sib) {
        final byte SIB_BASE_MASK = (byte) 0x07; // 00000111
        return BitUtils.and(sib, SIB_BASE_MASK);
    }

    public SIB(final byte s) {
        this.scale = extractScale(s);
        this.index = extractIndex(s);
        this.base = extractBase(s);
    }

    public byte scale() {
        return scale;
    }

    public byte index() {
        return index;
    }

    public byte base() {
        return base;
    }

    public int hashCode() {
        int h = 17;
        h = 31 * h + BitUtils.asInt(scale);
        h = 31 * h + BitUtils.asInt(index);
        h = 31 * h + BitUtils.asInt(base);
        return h;
    }

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
        final SIB s = (SIB) other;
        return this.scale == s.scale && this.index == s.index && this.base == s.base;
    }

    public String toString() {
        return "scale:" + BitUtils.toBinaryString(scale).substring(6, 8) + " index:"
                + BitUtils.toBinaryString(index).substring(5, 8) + " base:"
                + BitUtils.toBinaryString(base).substring(5, 8);
    }
}
