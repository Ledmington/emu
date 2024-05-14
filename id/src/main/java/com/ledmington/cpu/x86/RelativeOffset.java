package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

/**
 * An offset relative to the current (at run-time) value of
 * the Instruction Pointer register.
 * Equivalent to an IndirectOperand with only the instruction pointer as the
 * base.
 */
public final class RelativeOffset implements Operand {

    private final long value;

    public static RelativeOffset of8(final byte x) {
        return new RelativeOffset(x);
    }

    public static RelativeOffset of32(final int x) {
        return new RelativeOffset(x);
    }

    private RelativeOffset(final long value) {
        this.value = value;
    }

    @Override
    public String toIntelSyntax() {
        return String.format("0x%x", value);
    }

    public long amount() {
        return value;
    }

    @Override
    public String toString() {
        return "RelativeOffset(" + value + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + (BitUtils.asInt(value >>> 32) ^ BitUtils.asInt(value));
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
        return this.value == ((RelativeOffset) other).value;
    }
}
