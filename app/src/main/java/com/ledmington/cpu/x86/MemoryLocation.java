package com.ledmington.cpu.x86;

public final class MemoryLocation implements Operand {

    private final long value;

    public static MemoryLocation of32(final int x) {
        final long l = ((long) x) & 0x00000000ffffffffL;
        return new MemoryLocation(l);
    }

    private MemoryLocation(final long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("0x%x", value);
    }
}
