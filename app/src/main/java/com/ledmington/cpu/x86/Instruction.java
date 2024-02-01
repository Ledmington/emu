package com.ledmington.cpu.x86;

import java.util.Objects;
import java.util.Optional;

/**
 * High-level representation of an x86 instruction.
 */
public final class Instruction {

    private final Opcode opcode;
    private final Optional<Operand> operand1;
    private final Optional<Operand> operand2;

    public Instruction(final Opcode opcode, final Optional<Operand> op1, final Optional<Operand> op2) {
        this.opcode = Objects.requireNonNull(opcode);
        this.operand1 = Objects.requireNonNull(op1);
        this.operand2 = Objects.requireNonNull(op2);

        if (op2.isPresent() && op1.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot have an instruction with a second operand without a first operand");
        }
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
        if (this.operand1.isPresent()) {
            sb.append(' ');

            if (operand2.isPresent()) {
                sb.append(operandString(this.operand2.orElseThrow()));
            }

            sb.append(',');
            sb.append(operandString(this.operand1.orElseThrow()));
        }
        return sb.toString();
    }
}
