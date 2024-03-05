package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

public final class RexPrefix {

    private static final byte REX_PREFIX_MASK = (byte) 0xf0;
    private static final byte REX_PREFIX = (byte) 0x40;

    private final boolean w;
    private final boolean r;
    private final boolean x;
    private final boolean b;

    public static boolean isREXPrefix(final byte b) {
        return BitUtils.and(b, REX_PREFIX_MASK) == REX_PREFIX;
    }

    public RexPrefix(final byte b) {
        if (!isREXPrefix(b)) {
            throw new IllegalArgumentException(String.format("Input byte 0x%02x is not a valid REX prefix", b));
        }

        final byte REX_w_mask = (byte) 0x08;
        final byte REX_r_mask = (byte) 0x04;
        final byte REX_x_mask = (byte) 0x02;
        final byte REX_b_mask = (byte) 0x01;

        this.w = BitUtils.and(b, REX_w_mask) != 0;
        this.r = BitUtils.and(b, REX_r_mask) != 0;
        this.x = BitUtils.and(b, REX_x_mask) != 0;
        this.b = BitUtils.and(b, REX_b_mask) != 0;
    }

    public boolean w() {
        return w;
    }

    public boolean isOperand64Bit() {
        return w;
    }

    public boolean r() {
        return r;
    }

    public boolean ModRMRegExtension() {
        return r;
    }

    public boolean x() {
        return x;
    }

    public boolean SIBIndexExtension() {
        return x;
    }

    public boolean b() {
        return b;
    }

    public boolean extension() {
        return b;
    }

    public boolean SIBBaseExtension() {
        return b;
    }

    public boolean ModRMRMExtension() {
        return b;
    }

    public boolean opcodeRegExtension() {
        return b;
    }

    public int hashCode() {
        int h = 17;
        h = 31 + h + (w ? 1 : 0);
        h = 31 + h + (r ? 1 : 0);
        h = 31 + h + (x ? 1 : 0);
        h = 31 + h + (b ? 1 : 0);
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
        final RexPrefix rex = (RexPrefix) other;
        return this.w == rex.w && this.r == rex.r && this.x == rex.x && this.b == rex.b;
    }

    public String toString() {
        return (w ? ".W" : "") + (r ? ".R" : "") + (x ? ".X" : "") + (b ? ".B" : "");
    }
}
