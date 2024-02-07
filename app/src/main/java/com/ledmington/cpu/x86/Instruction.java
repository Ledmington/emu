package com.ledmington.cpu.x86;

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

    private String operandString(final Operand op) {
        if (!(op instanceof IndirectOperand)) {
            return op.toIntelSyntax();
        }
        if (opcode == Opcode.LEA) {
            return op.toIntelSyntax();
        } else {
            return "QWORD PTR " + op.toIntelSyntax();
        }
    }

    public String toString() {
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
}
