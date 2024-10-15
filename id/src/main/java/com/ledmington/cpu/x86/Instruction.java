/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ledmington.cpu.x86;

import java.util.Locale;
import java.util.Objects;

/** High-level representation of an x86 instruction. */
public final class Instruction {

    private final InstructionPrefix prefix;
    private final Opcode code;
    private final Operand op1;
    private final Operand op2;
    private final Operand op3;

    private Instruction(
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

    /**
     * Creates an instruction with a prefix and two operands (like REP MOVS BYTE PTR ES:[EDI],BYTE PTR DS:[ESI]).
     *
     * @param prefix The prefix of the Instruction.
     * @param opcode The opcode of the Instruction.
     * @param firstOperand The first operand of the Instruction.
     * @param secondOperand The second operand of the Instruction.
     */
    public Instruction(
            final InstructionPrefix prefix,
            final Opcode opcode,
            final Operand firstOperand,
            final Operand secondOperand) {
        this(prefix, opcode, firstOperand, secondOperand, null);
    }

    /**
     * Creates an Instruction with 3 operands (like PSHUFD XMM0, XMM1, 0x12).
     *
     * @param opcode The opcode of the Instruction.
     * @param firstOperand The first operand of the Instruction.
     * @param secondOperand The second operand of the Instruction.
     * @param thirdOperand The third of the Instruction.
     */
    public Instruction(
            final Opcode opcode, final Operand firstOperand, final Operand secondOperand, final Operand thirdOperand) {
        this(null, opcode, firstOperand, secondOperand, thirdOperand);
    }

    /**
     * Creates an Instruction with two operands (like XOR EAX, EAX).
     *
     * @param opcode The opcode of the Instruction.
     * @param firstOperand The first operand of the Instruction.
     * @param secondOperand The second operand of the Instruction.
     */
    public Instruction(final Opcode opcode, final Operand firstOperand, final Operand secondOperand) {
        this(null, opcode, firstOperand, secondOperand, null);
    }

    /**
     * Creates an Instruction with one operand (like PUSH R9).
     *
     * @param opcode The opcode of the instruction.
     * @param firstOperand The only operand of this instruction.
     */
    public Instruction(final Opcode opcode, final Operand firstOperand) {
        this(null, opcode, firstOperand, null, null);
    }

    /**
     * Creates an instruction with just the opcode (like NOP or ENDBR64).
     *
     * @param opcode The opcode of the instruction.
     */
    public Instruction(final Opcode opcode) {
        this(null, opcode, null, null, null);
    }

    /**
     * Returns the opcode of this Instruction.
     *
     * @return The opcode of this Instruction.
     */
    public Opcode opcode() {
        return code;
    }

    /**
     * Returns the first operand of this Instruction.
     *
     * @return The first operand.
     * @throws IllegalArgumentException If this instruction has no operands.
     */
    public Operand firstOperand() {
        if (op1 == null) {
            throw new IllegalArgumentException("No first operand");
        }
        return op1;
    }

    /**
     * Returns the second operand of this Instruction.
     *
     * @return The second operand.
     * @throws IllegalArgumentException If this instruction has less than two operands.
     */
    public Operand secondOperand() {
        if (op2 == null) {
            throw new IllegalArgumentException("No second operand");
        }
        return op2;
    }

    /**
     * The number of bits "used" by this instruction, which not necessarily corresponds to the size of the operands.
     *
     * <p>For example: {@code lea eax,[rbx]} "uses" 32 bits {@code vaddsd xmm9, xmm10, xmm9} "uses" 64 bits
     *
     * <p>Instructions which do not "use" anything like NOP, RET, LEAVE etc. return 0.
     *
     * @return The bits "used" in this instruction.
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

    /**
     * Reference obtainable through 'objdump -Mintel-mnemonic ...'
     *
     * @return A String representation of this instruction with Intel syntax.
     */
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
        return "Instruction(prefix=" + prefix + ";opcode=" + code.toString()
                + ";operands=[" + op1 + "," + op2 + "," + op3 + "]"
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
