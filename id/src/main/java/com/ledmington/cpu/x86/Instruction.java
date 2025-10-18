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

	Opcode opcode();

	boolean hasFirstOperand();

	boolean hasSecondOperand();

	boolean hasThirdOperand();

	boolean hasFourthOperand();

	default boolean hasOperand(final int operandIndex) {
		return switch (operandIndex) {
			case 0 -> hasFirstOperand();
			case 1 -> hasSecondOperand();
			case 2 -> hasThirdOperand();
			case 3 -> hasFourthOperand();
			default -> throw new IllegalArgumentException(String.format("Invalid operand index: %,d.", operandIndex));
		};
	}

	Operand firstOperand();

	Operand secondOperand();

	Operand thirdOperand();

	Operand fourthOperand();

	default Operand operand(final int operandIndex) {
		return switch (operandIndex) {
			case 0 -> firstOperand();
			case 1 -> secondOperand();
			case 2 -> thirdOperand();
			case 3 -> fourthOperand();
			default -> throw new IllegalArgumentException(String.format("Invalid operand index: %,d.", operandIndex));
		};
	}

	boolean hasPrefix();

	InstructionPrefix getPrefix();

	default boolean hasLockPrefix() {
		return hasPrefix() && getPrefix() == InstructionPrefix.LOCK;
	}

	default boolean hasRepPrefix() {
		return hasPrefix() && getPrefix() == InstructionPrefix.REP;
	}

	default boolean hasRepnzPrefix() {
		return hasPrefix() && getPrefix() == InstructionPrefix.REPNZ;
	}

	boolean hasDestinationMask();

	boolean hasZeroDestinationMask();

	MaskRegister getDestinationMask();

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
}
