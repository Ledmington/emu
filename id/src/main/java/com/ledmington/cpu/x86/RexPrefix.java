package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

public final class RexPrefix {

    private static final byte REX_PREFIX_MASK = (byte) 0b11110000;
    private static final byte REX_PREFIX = (byte) 0b01000000;

    private final boolean wBit;
    private final boolean rBit;
    private final boolean xBit;
    private final boolean bBit;

    public static boolean isREXPrefix(final byte b) {
        return BitUtils.and(b, REX_PREFIX_MASK) == REX_PREFIX;
    }

    public RexPrefix(final byte b) {
        if (!isREXPrefix(b)) {
            throw new IllegalArgumentException(String.format("Input byte 0x%02x is not a valid REX prefix", b));
        }

        final byte REX_w_mask = (byte) 0b00001000;
        final byte REX_r_mask = (byte) 0b00000100;
        final byte REX_x_mask = (byte) 0b00000010;
        final byte REX_b_mask = (byte) 0b00000001;

        this.wBit = BitUtils.and(b, REX_w_mask) != 0;
        this.rBit = BitUtils.and(b, REX_r_mask) != 0;
        this.xBit = BitUtils.and(b, REX_x_mask) != 0;
        this.bBit = BitUtils.and(b, REX_b_mask) != 0;
    }

    public boolean w() {
        return wBit;
    }

    public boolean isOperand64Bit() {
        return wBit;
    }

    public boolean r() {
        return rBit;
    }

    public boolean ModRMRegExtension() {
        return rBit;
    }

    public boolean x() {
        return xBit;
    }

    public boolean SIBIndexExtension() {
        return xBit;
    }

    public boolean b() {
        return bBit;
    }

    public boolean SIBBaseExtension() {
        return bBit;
    }

    public boolean ModRMRMExtension() {
        return bBit;
    }

    public boolean opcodeRegExtension() {
        return bBit;
    }

    @Override
    public String toString() {
        return (wBit ? ".W" : "") + (rBit ? ".R" : "") + (xBit ? ".X" : "") + (bBit ? ".B" : "");
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 + h + (wBit ? 1 : 0);
        h = 31 + h + (rBit ? 1 : 0);
        h = 31 + h + (xBit ? 1 : 0);
        h = 31 + h + (bBit ? 1 : 0);
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
        final RexPrefix rex = (RexPrefix) other;
        return this.wBit == rex.wBit && this.rBit == rex.rBit && this.xBit == rex.xBit && this.bBit == rex.bBit;
    }
}
