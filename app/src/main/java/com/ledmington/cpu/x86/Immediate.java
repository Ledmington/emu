package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

public final class Immediate implements Operand {

    private final long value;
    private final int bits;

    private Immediate(final long value, final int bits) {
        if (bits != 8 && bits != 16 && bits != 32 && bits != 64) {
            throw new IllegalArgumentException(
                    String.format("Invalid value of bits: expected 8, 16, 32 or 64 but was %,d", bits));
        }

        this.value = value;
        this.bits = bits;
    }

    public Immediate(final byte b) {
        this(BitUtils.asLong(b), 8);
    }

    public Immediate(final short s) {
        this(BitUtils.asLong(s), 16);
    }

    public Immediate(final int x) {
        this(BitUtils.asLong(x), 32);
    }

    public Immediate(final long x) {
        this(x, 64);
    }

    @Override
    public String toIntelSyntax() {
        return switch (bits) {
            case 8 -> String.format("0x%02x", BitUtils.asByte(value));
            case 16 -> String.format("0x%04x", BitUtils.asShort(value));
            case 32 -> String.format("0x%08x", BitUtils.asInt(value));
            case 64 -> String.format("0x%016x", value);
            default -> throw new IllegalStateException(
                    String.format("Invalid value of bits: expected 8, 16, 32 or 64 but was %,d", bits));
        };
    }
}
