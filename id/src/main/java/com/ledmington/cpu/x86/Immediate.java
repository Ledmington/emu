package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

/**
 * This class represents an immediate value in a x86 instruction.
 */
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

    /**
     * Creates an immediate value of 1 byte.
     * @param b
     *      The 1-byte immediate.
     */
    public Immediate(final byte b) {
        this(BitUtils.asLong(b), 8);
    }

    /**
     * Creates an immediate value of 2 bytes.
     * @param s
     *      The 2-bytes immediate.
     */
    public Immediate(final short s) {
        this(BitUtils.asLong(s), 16);
    }

    /**
     * Creates an immediate value of 4 bytes.
     * @param x
     *      The 4-bytes immediate.
     */
    public Immediate(final int x) {
        this(BitUtils.asLong(x), 32);
    }

    /**
     * Creates an immediate value of 8 bytes.
     * @param x
     *      The 8-bytes immediate.
     */
    public Immediate(final long x) {
        this(x, 64);
    }

    public int bits() {
        return bits;
    }

    @Override
    public String toIntelSyntax() {
        return switch (bits) {
            case 8 -> String.format("0x%x", BitUtils.asByte(value));
            case 16 -> String.format("0x%x", BitUtils.asShort(value));
            case 32 -> String.format("0x%x", BitUtils.asInt(value));
            case 64 -> String.format("0x%x", value);
            default -> throw new IllegalStateException(
                    String.format("Invalid value of bits: expected 8, 16, 32 or 64 but was %,d", bits));
        };
    }

    @Override
    public String toString() {
        return "Immediate(" + this.toIntelSyntax() + ")";
    }

    @Override
    public int hashCode() {
        return (int) (value >>> 32) * 31 + BitUtils.asInt(value);
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
        final Immediate imm = (Immediate) other;
        return this.value == imm.value && this.bits == imm.bits;
    }
}
