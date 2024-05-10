package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

public final class SIB {

    private final byte scaleByte;
    private final byte indexByte;
    private final byte baseByte;

    public static byte extractScale(final byte sib) {
        final byte SIB_SCALE_MASK = (byte) 0b11000000;
        return BitUtils.shr(BitUtils.and(sib, SIB_SCALE_MASK), 6);
    }

    public static byte extractIndex(final byte sib) {
        final byte SIB_INDEX_MASK = (byte) 0b00111000;
        return BitUtils.shr(BitUtils.and(sib, SIB_INDEX_MASK), 3);
    }

    public static byte extractBase(final byte sib) {
        final byte SIB_BASE_MASK = (byte) 0b00000111;
        return BitUtils.and(sib, SIB_BASE_MASK);
    }

    public SIB(final byte s) {
        this.scaleByte = extractScale(s);
        this.indexByte = extractIndex(s);
        this.baseByte = extractBase(s);
    }

    public byte scale() {
        return scaleByte;
    }

    public byte index() {
        return indexByte;
    }

    public byte base() {
        return baseByte;
    }

    @Override
    public String toString() {
        return "scale:" + BitUtils.toBinaryString(scaleByte).substring(6, 8) + " index:"
                + BitUtils.toBinaryString(indexByte).substring(5, 8) + " base:"
                + BitUtils.toBinaryString(baseByte).substring(5, 8);
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + BitUtils.asInt(scaleByte);
        h = 31 * h + BitUtils.asInt(indexByte);
        h = 31 * h + BitUtils.asInt(baseByte);
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
        final SIB s = (SIB) other;
        return this.scaleByte == s.scaleByte && this.indexByte == s.indexByte && this.baseByte == s.baseByte;
    }
}
