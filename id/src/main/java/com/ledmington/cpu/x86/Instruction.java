package com.ledmington.cpu.x86;

import java.util.Arrays;
import java.util.Objects;

/**
 * High-level representation of an x86 instruction.
 */
public final class Instruction {

    private final Opcode opcode;
    private final Operand[] operands;

    public Instruction(final Opcode opcode, final Operand... ops) {
        this.opcode = Objects.requireNonNull(opcode);
        this.operands = new Operand[Objects.requireNonNull(ops).length];
        for (int i = 0; i < ops.length; i++) {
            this.operands[i] = Objects.requireNonNull(ops[i]);
        }
    }

    public int nOperands() {
        return operands.length;
    }

    /**
     * The number of bits "used" by this instruction, which not necessarily
     * corresponds to the size of the operands.
     *
     * For example:
     * lea eax,[rbx] "uses" 32 bits
     * vaddsd xmm9, xmm10, xmm9 "uses" 64 bits
     *
     * Instructions which do not "use" anything like NOP, RET, LEAVE etc.
     * return 0.
     */
    public int bits() {
        // here it is assumed that not all operands can be IndirectOperands
        // and that all "first-class" registers involved have the same size
        for (final Operand op : operands) {
            if (op instanceof Register r) {
                return r.bits();
            }
        }

        return 0;
    }

    private String operandString(final Operand op) {
        if (op instanceof IndirectOperand io && opcode != Opcode.LEA) {
            return switch (this.bits()) {
                        case 8 -> "BYTE";
                        case 16 -> "WORD";
                        case 32 -> "DWORD";
                        case 64 -> "QWORD";
                        case 128 -> "XMMWORD";
                        case 256 -> "YMMWORD";
                        case 512 -> "ZMMWORD";
                        default -> throw new IllegalStateException(String.format(
                                "Instruction '%s' invalid value of bits: '%,d'",
                                io.reg2(), op, io.reg2().bits()));
                    } + " PTR " + op.toIntelSyntax();
        }
        return op.toIntelSyntax();
    }

    public String toIntelSyntax() {
        final StringBuilder sb = new StringBuilder();
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

    public String toString() {
        if (operands.length == 0) {
            return "Instruction(opcode=" + opcode.toString() + ")";
        }
        return "Instruction(opcode=" + opcode.toString() + ";operands="
                + Arrays.stream(operands).toList() + ")";
    }
}
