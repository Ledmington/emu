package com.ledmington.cpu.x86;

public final class RelativeOffset implements Operand {

    private final long value;

    public static RelativeOffset of32(final int x) {
        final long l = ((long) x) & 0x00000000ffffffffL;
        return new RelativeOffset(l);
    }

    private RelativeOffset(final long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("0x%x", value);
    }
}
