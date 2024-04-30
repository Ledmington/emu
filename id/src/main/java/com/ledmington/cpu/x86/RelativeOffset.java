package com.ledmington.cpu.x86;

/**
 * An offset relative to the current (at run-time) value of
 * the Instruction Pointer register.
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
}
