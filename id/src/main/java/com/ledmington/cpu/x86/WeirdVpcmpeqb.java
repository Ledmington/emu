/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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

/**
 * This specific class represents the specific case of: 'vpcmpeqb k0,ymm16,ymm17' when encoded as '62 b1 7d 20 74 c1'.
 *
 * @param firstOperand The first operand of the instruction, usually a mask register.
 * @param secondOperand The second operand of the instruction, usually an YMM register.
 * @param thirdOperand The third operand of the instruction, usually an YMM register.
 */
public record WeirdVpcmpeqb(Operand firstOperand, Operand secondOperand, Operand thirdOperand) implements Instruction {

	@Override
	public Opcode opcode() {
		return Opcode.VPCMPEQB;
	}

	@Override
	public boolean hasFirstOperand() {
		return true;
	}

	@Override
	public boolean hasSecondOperand() {
		return true;
	}

	@Override
	public boolean hasThirdOperand() {
		return true;
	}

	@Override
	public boolean hasFourthOperand() {
		return false;
	}

	@Override
	public Operand firstOperand() {
		return firstOperand;
	}

	@Override
	public Operand secondOperand() {
		return secondOperand;
	}

	@Override
	public Operand thirdOperand() {
		return thirdOperand;
	}

	@Override
	public Operand fourthOperand() {
		throw new IllegalArgumentException("No fourth operand.");
	}

	@Override
	public boolean hasPrefix() {
		return false;
	}

	@Override
	public InstructionPrefix getPrefix() {
		throw new IllegalArgumentException("No prefix.");
	}

	@Override
	public boolean hasDestinationMask() {
		return false;
	}

	@Override
	public boolean hasZeroDestinationMask() {
		return false;
	}

	@Override
	public MaskRegister getDestinationMask() {
		throw new IllegalArgumentException("No destination mask.");
	}
}
