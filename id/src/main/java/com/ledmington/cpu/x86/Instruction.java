package com.ledmington.cpu.x86;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * High-level representation of an x86 instruction.
 */
public final class Instruction {

    public enum Prefix {
        LOCK,
        REP,
        REPNZ
    }

    private final Optional<Prefix> pref;
    private final Opcode opcode;
    private final Operand[] operands;

    public Instruction(final Prefix pref, final Opcode opcode, final Operand... ops) {
        this.pref = (pref == null) ? Optional.empty() : Optional.of(pref);
        this.opcode = Objects.requireNonNull(opcode);
        this.operands = new Operand[Objects.requireNonNull(ops, "Cannot have null array of operands").length];
        for (int i = 0; i < ops.length; i++) {
            final int finalI = i;
            this.operands[i] =
                    Objects.requireNonNull(ops[i], () -> String.format("The %,d-th operand was null", finalI));
        }
    }

    public Instruction(final Opcode opcode, final Operand... ops) {
        this(null, opcode, ops);
    }

    public Opcode opcode() {
        return opcode;
    }

    public Operand op(final int i) {
        return this.operands[i];
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
        // here it is assumed that all "first-class" registers involved have the same size
        for (final Operand op : operands) {
            if (op instanceof Register r) {
                return r.bits();
            }
            if (op instanceof Immediate imm) {
                return imm.bits();
            }
        }

        if (operands.length > 0 && operands[0] instanceof IndirectOperand io && io.hasExplicitPtrSize()) {
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
        if (this.pref.isPresent()) {
            sb.append(this.pref.orElseThrow().name().toLowerCase()).append(' ');
        }
        sb.append(opcode.mnemonic());

        if (operands.length > 0) {
            sb.append(' ');
            sb.append(operandString(operands[0]));

            for (int i = 1; i < operands.length; i++) {
                sb.append(',');
                sb.append(operandString(operands[i]));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        if (operands.length == 0) {
            return "Instruction(opcode=" + opcode.toString() + ")";
        }
        return "Instruction(opcode=" + opcode.toString() + ";operands="
                + Arrays.stream(operands).toList() + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + opcode.hashCode();
        for (final Operand op : this.operands) {
            h = 31 * h + op.hashCode();
        }
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
        return this.opcode.equals(o.opcode) && Arrays.equals(this.operands, o.operands);
    }
}
