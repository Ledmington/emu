package com.ledmington.cpu.x86;

import java.util.Objects;

import com.ledmington.utils.BitUtils;

/**
 * This class maps the following cases:
 * [reg2]
 * [reg2 + displacement]
 * [reg2 * constant]
 * [reg2 * constant + displacement]
 * [displacement]
 * [reg1 + reg2 * constant]
 * [reg1 + reg2 * constant + displacement]
 */
public final class IndirectOperand implements Operand {

    private final Register reg1;
    private final int constant;
    private final Register reg2;
    private final Long displacement;
    private final DisplacementType displacementType;
    private final int ptrSize;

    public static IndirectOperandBuilder builder() {
        return new IndirectOperandBuilder();
    }

    public IndirectOperand(
            final Register reg1,
            final Register reg2,
            final int constant,
            final Long displacement,
            final DisplacementType displacementType,
            final int ptrSize) {
        this.reg1 = reg1;
        this.constant = constant;
        this.reg2 = reg2;
        this.displacement = displacement;
        this.displacementType = Objects.requireNonNull(displacementType);
        this.ptrSize = ptrSize;

        if (constant != 0 && Integer.bitCount(constant) != 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid 'constant' value: expected 0 or a power of two but was %,d", constant));
        }
    }

    public boolean hasExplicitPtrSize() {
        return this.ptrSize != 0;
    }

    public int explicitPtrSize() {
        return this.ptrSize;
    }

    @Override
    public String toIntelSyntax() {
        final StringBuilder sb = new StringBuilder();
        boolean shouldAddSign = false;
        if (reg2 != null && reg2 instanceof SegmentRegister sr) {
            sb.append(sr.segment().toIntelSyntax()).append(':');
        }
        sb.append('[');
        if (reg1 != null) {
            sb.append(reg1.toIntelSyntax());
            if (reg2 != null) {
                sb.append('+');
            }
            shouldAddSign = true;
        }
        if (reg2 != null) {
            sb.append(reg2.toIntelSyntax());
            shouldAddSign = true;
        }
        if (constant != 0 && constant != 1) {
            sb.append('*').append(constant);
            shouldAddSign = true;
        }
        if (displacement != null) {
            long d = displacement;
            if (displacement < 0) {
                d = switch (displacementType) {
                    case BYTE -> (~BitUtils.asByte(d)) + 1;
                    case SHORT -> (~BitUtils.asShort(d)) + 1;
                    case INT -> (~BitUtils.asInt(d)) + 1;
                    case LONG -> (~d) + 1;};
            }
            if (shouldAddSign) {
                sb.append((displacement < 0) ? '-' : '+');
            }
            sb.append(String.format("0x%x", d));
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public String toString() {
        return "IndirectOperand(reg1="
                + ((reg1 == null) ? "null" : reg1.toString())
                + ";reg2="
                + reg2.toString() + ";constant="
                + constant + ";displacement="
                + displacement.toString()
                + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + reg1.hashCode();
        h = 31 * h + constant;
        h = 31 * h + reg2.hashCode();
        h = 31 * h + displacement.hashCode();
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
        final IndirectOperand io = (IndirectOperand) other;
        return this.reg1.equals(io.reg1)
                && this.constant == io.constant
                && this.reg2.equals(io.reg2)
                && this.displacement.equals(io.displacement);
    }
}
