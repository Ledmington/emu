/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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

/** High-level representation of an x86 instruction. */
public interface Instruction {

    static InstructionBuilder builder() {
        return new InstructionBuilder();
    }

    /**
     * Returns the opcode of this Instruction.
     *
     * @return The opcode of this Instruction.
     */
    Opcode opcode();

    /**
     * Checks whether this instruction has a first operand.
     *
     * @return True if the first operand exists, false otherwise.
     */
    boolean hasFirstOperand();

    /**
     * Checks whether this instruction has a second operand.
     *
     * @return True if the second operand exists, false otherwise.
     */
    boolean hasSecondOperand();

    /**
     * Checks whether this instruction has a third operand.
     *
     * @return True if the third operand exists, false otherwise.
     */
    boolean hasThirdOperand();

    /**
     * Checks whether this instruction has a fourth operand.
     *
     * @return True if the fourth operand exists, false otherwise.
     */
    boolean hasFourthOperand();

    /**
     * Checks whether this instruction has an operand at the given index.
     *
     * @param operandIndex The operand index (0–3).
     * @return True if the operand exists, false otherwise.
     */
    default boolean hasOperand(final int operandIndex) {
        return switch (operandIndex) {
            case 0 -> hasFirstOperand();
            case 1 -> hasSecondOperand();
            case 2 -> hasThirdOperand();
            case 3 -> hasFourthOperand();
            default -> throw new IllegalArgumentException(String.format("Invalid operand index: %,d.", operandIndex));
        };
    }

    /**
     * Returns the first operand of this Instruction.
     *
     * @return The first operand.
     * @throws IllegalArgumentException If this instruction has no operands.
     */
    Operand firstOperand();

    /**
     * Returns the second operand of this Instruction.
     *
     * @return The second operand.
     * @throws IllegalArgumentException If this instruction has less than two operands.
     */
    Operand secondOperand();

    /**
     * Returns the third operand of this instruction.
     *
     * @return The third operand.
     */
    Operand thirdOperand();

    /**
     * Returns the fourth operand of this instruction.
     *
     * @return The fourth operand.
     */
    Operand fourthOperand();

    /**
     * Returns the operand at the specified index.
     *
     * @param operandIndex The operand index (0–3).
     * @return The operand at the specified index.
     */
    default Operand operand(final int operandIndex) {
        return switch (operandIndex) {
            case 0 -> firstOperand();
            case 1 -> secondOperand();
            case 2 -> thirdOperand();
            case 3 -> fourthOperand();
            default -> throw new IllegalArgumentException(String.format("Invalid operand index: %,d.", operandIndex));
        };
    }

    /**
     * Checks whether this instruction has a prefix.
     *
     * @return True if this instruction has a prefix, false otherwise.
     */
    boolean hasPrefix();

    /**
     * Returns the prefix of this instruction.
     *
     * @return The prefix of this instruction.
     */
    InstructionPrefix getPrefix();

    /**
     * Checks whether this instruction has a LOCK prefix.
     *
     * @return True if this instruction has a LOCK prefix, false otherwise.
     */
    default boolean hasLockPrefix() {
        return hasPrefix() && getPrefix() == InstructionPrefix.LOCK;
    }

    /**
     * Checks whether this instruction has a REP prefix.
     *
     * @return True if this instruction has a REP prefix, false otherwise.
     */
    default boolean hasRepPrefix() {
        return hasPrefix() && getPrefix() == InstructionPrefix.REP;
    }

    /**
     * Checks whether this instruction has a REPNZ prefix.
     *
     * @return True if this instruction has a REPNZ prefix, false otherwise.
     */
    default boolean hasRepnzPrefix() {
        return hasPrefix() && getPrefix() == InstructionPrefix.REPNZ;
    }

    /**
     * Checks whether this instruction has a destination mask.
     *
     * @return True if this instruction has a destination mask, false otherwise.
     */
    boolean hasDestinationMask();

    /**
     * Checks whether this instruction has a zero destination mask.
     *
     * @return True if the instruction has a zero destination mask, false otherwise.
     */
    boolean hasZeroDestinationMask();

    /**
     * Returns the destination mask of this instruction.
     *
     * @return The destination mask of this instruction.
     */
    MaskRegister getDestinationMask();

    /**
     * Returns the number of operands in this instruction.
     *
     * @return The number of operands.
     */
    default int getNumOperands() {
        if (hasFourthOperand()) {
            return 4;
        }
        if (hasThirdOperand()) {
            return 3;
        }
        if (hasSecondOperand()) {
            return 2;
        }
        if (hasFirstOperand()) {
            return 1;
        }
        return 0;
    }

    /**
     * Checks whether this instruction is part of the legacy/compatibility x86 set.
     *
     * @return True if the instruction is legacy, false otherwise.
     */
    default boolean isLegacy() {
        return opcode() == Opcode.ENDBR32;
    }
}