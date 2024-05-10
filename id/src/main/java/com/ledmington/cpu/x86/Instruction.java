package com.ledmington.cpu.x86;

import java.util.Locale;
import java.util.Objects;

/**
 * High-level representation of an x86 instruction.
 */
public final class Instruction {

    private final InstructionPrefix prefix;
    private final Opcode opcode;
    private final Operand firstOperand;
    private final Operand secondOperand;
    private final Operand thirdOperand;

    public Instruction(
            final InstructionPrefix prefix,
            final Opcode opcode,
            final Operand firstOperand,
            final Operand secondOperand,
            final Operand thirdOperand) {
        this.prefix = prefix;
        this.opcode = Objects.requireNonNull(opcode);
        this.firstOperand = firstOperand;
        if (firstOperand == null && secondOperand != null) {
            throw new IllegalArgumentException(String.format(
                    "Cannot have an x86 instruction with a second operand (%s) but not a first", secondOperand));
        }
        this.secondOperand = secondOperand;
        if (thirdOperand != null && (firstOperand == null || secondOperand == null)) {
            throw new IllegalArgumentException(String.format(
                    "Cannot have an x86 instruction with a third operand (%s) but not a first or a second",
                    thirdOperand));
        }
        this.thirdOperand = thirdOperand;
    }

    public Instruction(
            final InstructionPrefix prefix,
            final Opcode opcode,
            final Operand firstOperand,
            final Operand secondOperand) {
        this(prefix, opcode, firstOperand, secondOperand, null);
    }

    public Instruction(
            final Opcode opcode, final Operand firstOperand, final Operand secondOperand, final Operand thirdOperand) {
        this(null, opcode, firstOperand, secondOperand, thirdOperand);
    }

    public Instruction(final Opcode opcode, final Operand firstOperand, final Operand secondOperand) {
        this(null, opcode, firstOperand, secondOperand, null);
    }

    public Instruction(final Opcode opcode, final Operand firstOperand) {
        this(null, opcode, firstOperand, null, null);
    }

    public Instruction(final Opcode opcode) {
        this(null, opcode, null, null, null);
    }

    public Opcode opcode() {
        return opcode;
    }

    public Operand firstOperand() {
        if (firstOperand == null) {
            throw new IllegalArgumentException("No first operand");
        }
        return firstOperand;
    }

    public Operand secondOperand() {
        if (secondOperand == null) {
            throw new IllegalArgumentException("No second operand");
        }
        return secondOperand;
    }

    /**
     * The number of bits "used" by this instruction, which not necessarily
     * corresponds to the size of the operands.
     * <p>
     * For example:
     * {@code lea eax,[rbx]} "uses" 32 bits
     * {@code vaddsd xmm9, xmm10, xmm9} "uses" 64 bits
     * <p>
     * Instructions which do not "use" anything like NOP, RET, LEAVE etc.
     * return 0.
     */
    public int bits() {
        // here it is assumed that all "first-class" registers involved have the same
        // size
        if (firstOperand instanceof Register r) {
            return r.bits();
        }
        if (firstOperand instanceof Immediate imm) {
            return imm.bits();
        }
        if (secondOperand instanceof Register r) {
            return r.bits();
        }
        if (secondOperand instanceof Immediate imm) {
            return imm.bits();
        }
        if (thirdOperand instanceof Register r) {
            return r.bits();
        }
        if (thirdOperand instanceof Immediate imm) {
            return imm.bits();
        }

        if (firstOperand != null && firstOperand instanceof IndirectOperand io && io.hasExplicitPtrSize()) {
            return io.explicitPtrSize();
        }

        return 0;
    }

    private String sizeToPointerType(final int size) {
        return switch (size) {
            case 8 -> "BYTE";
            case 16 -> "WORD";
            case 32 -> "DWORD";
            case 64 -> "QWORD";
            case 128 -> "XMMWORD";
            case 256 -> "YMMWORD";
            case 512 -> "ZMMWORD";
            default -> throw new IllegalStateException(String.format("Invalid value of bits: '%,d'", size));
        };
    }

    private String operandString(final Operand op) {
        if (op instanceof IndirectOperand io && opcode != Opcode.LEA) {
            if (io.hasExplicitPtrSize()) {
                return sizeToPointerType(io.explicitPtrSize()) + " PTR " + op.toIntelSyntax();
            }

            return sizeToPointerType(this.bits()) + " PTR " + op.toIntelSyntax();
        }
        return op.toIntelSyntax();
    }

    public String toIntelSyntax() {
        final StringBuilder sb = new StringBuilder();
        if (this.prefix != null) {
            sb.append(this.prefix.name().toLowerCase(Locale.US)).append(' ');
        }
        sb.append(opcode.mnemonic());

        if (firstOperand != null) {
            sb.append(' ').append(operandString(firstOperand));
            if (secondOperand != null) {
                sb.append(',').append(operandString(secondOperand));
                if (thirdOperand != null) {
                    sb.append(',').append(operandString(thirdOperand));
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Instruction(prefix=" + prefix.toString() + ";opcode=" + opcode.toString()
                + (firstOperand == null
                        ? ""
                        : ";operands=[" + firstOperand
                                + (secondOperand == null
                                        ? ""
                                        : "," + secondOperand + (thirdOperand == null ? "" : "," + thirdOperand))
                                + "]")
                + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + prefix.hashCode();
        h = 31 * h + opcode.hashCode();
        h = 31 * h + firstOperand.hashCode();
        h = 31 * h + secondOperand.hashCode();
        h = 31 * h + thirdOperand.hashCode();
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
        final Instruction o = (Instruction) other;
        return this.prefix.equals(o.prefix)
                && this.opcode.equals(o.opcode)
                && this.firstOperand.equals(o.firstOperand)
                && this.secondOperand.equals(o.secondOperand)
                && this.thirdOperand.equals(o.thirdOperand);
    }
}
