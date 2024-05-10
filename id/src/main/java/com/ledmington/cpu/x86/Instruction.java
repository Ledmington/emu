package com.ledmington.cpu.x86;

import java.util.Locale;
import java.util.Objects;

/**
 * High-level representation of an x86 instruction.
 */
public final class Instruction {

    private final InstructionPrefix prefix;
    private final Opcode code;
    private final Operand op1;
    private final Operand op2;
    private final Operand op3;

    public Instruction(
            final InstructionPrefix prefix,
            final Opcode opcode,
            final Operand firstOperand,
            final Operand secondOperand,
            final Operand thirdOperand) {
        this.prefix = prefix;
        this.code = Objects.requireNonNull(opcode);
        this.op1 = firstOperand;
        if (firstOperand == null && secondOperand != null) {
            throw new IllegalArgumentException(String.format(
                    "Cannot have an x86 instruction with a second operand (%s) but not a first", secondOperand));
        }
        this.op2 = secondOperand;
        if (thirdOperand != null && (firstOperand == null || secondOperand == null)) {
            throw new IllegalArgumentException(String.format(
                    "Cannot have an x86 instruction with a third operand (%s) but not a first or a second",
                    thirdOperand));
        }
        this.op3 = thirdOperand;
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
        return code;
    }

    public Operand firstOperand() {
        if (op1 == null) {
            throw new IllegalArgumentException("No first operand");
        }
        return op1;
    }

    public Operand secondOperand() {
        if (op2 == null) {
            throw new IllegalArgumentException("No second operand");
        }
        return op2;
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
        if (op1 instanceof Register r) {
            return r.bits();
        }
        if (op1 instanceof Immediate imm) {
            return imm.bits();
        }
        if (op2 instanceof Register r) {
            return r.bits();
        }
        if (op2 instanceof Immediate imm) {
            return imm.bits();
        }
        if (op3 instanceof Register r) {
            return r.bits();
        }
        if (op3 instanceof Immediate imm) {
            return imm.bits();
        }

        if (op1 != null && op1 instanceof IndirectOperand io && io.hasExplicitPtrSize()) {
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
        if (op instanceof IndirectOperand io && code != Opcode.LEA) {
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
        sb.append(code.mnemonic());

        if (op1 != null) {
            sb.append(' ').append(operandString(op1));
            if (op2 != null) {
                sb.append(',').append(operandString(op2));
                if (op3 != null) {
                    sb.append(',').append(operandString(op3));
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Instruction(prefix=" + prefix.toString() + ";opcode=" + code.toString()
                + (op1 == null
                        ? ""
                        : ";operands=[" + op1 + (op2 == null ? "" : "," + op2 + (op3 == null ? "" : "," + op3)) + "]")
                + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + prefix.hashCode();
        h = 31 * h + code.hashCode();
        h = 31 * h + op1.hashCode();
        h = 31 * h + op2.hashCode();
        h = 31 * h + op3.hashCode();
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
                && this.code.equals(o.code)
                && this.op1.equals(o.op1)
                && this.op2.equals(o.op2)
                && this.op3.equals(o.op3);
    }
}
