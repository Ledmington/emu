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

import com.ledmington.cpu.x86.exc.InvalidInstruction;

public final class InstructionChecker {

	private InstructionChecker() {}

	private static void error(final String fmt, final Object... args) {
		throw new InvalidInstruction(String.format(fmt, args));
	}

	public static void check(final Instruction inst) {
		final Opcode code = inst.opcode();

		final boolean hasFirstOperand = inst.hasFirstOperand();
		final boolean hasSecondOperand = inst.hasSecondOperand();
		final boolean hasThirdOperand = inst.hasThirdOperand();

		final int numOperands = hasThirdOperand ? 3 : (hasSecondOperand ? 2 : (hasFirstOperand ? 1 : 0));

		if (numOperands >= 2) {
			checkNoMoreThanOneImmediate(inst);
			checkNoMoreThanOneIndirect(inst);
		}

		final boolean isFirstRegister = hasFirstOperand && inst.firstOperand() instanceof Register;
		final boolean isFirstImmediate = hasFirstOperand && inst.firstOperand() instanceof Immediate;
		final boolean isFirstIndirect = hasFirstOperand && inst.firstOperand() instanceof IndirectOperand;
		final boolean isSecondIndirect = hasSecondOperand && inst.secondOperand() instanceof IndirectOperand;

		switch (code) {
			case Opcode.MOV -> {
				if (numOperands != 2) {
					error("%s requires 2 operands.", code.name());
				}
				if (isFirstImmediate) {
					error("Destination operand of %s cannot be an immediate value.", code.name());
				}
				if (inst.firstOperand().bits() != inst.secondOperand().bits()
						&&
						// Some instructions allow an implicit sign-extension when the first operand is 64 bits and the
						// second operand is a 32-bit immediate
						!(inst.secondOperand() instanceof final Immediate imm
								&& imm.bits() == 32
								&& inst.firstOperand().bits() == 64)) {
					error(
							"%s cannot have two operands of different sizes: were %,d (%s) and %,d (%s) bits, respectively.",
							code.name(),
							inst.firstOperand().bits(),
							inst.firstOperand(),
							inst.secondOperand().bits(),
							inst.secondOperand());
				}
			}
			case Opcode.MOVSXD -> {
				if (numOperands != 2) {
					error("%s requires 2 operands.", code.name());
				}
				if (!(inst.firstOperand() instanceof Register64)) {
					error("%s requires destination operand to be a 64-bit register.", code.name());
				}
				if (!(inst.secondOperand() instanceof Register32)
						&& (!(inst.secondOperand() instanceof IndirectOperand io) || io.bits() != 32)) {
					error(
							"%s requires source operand to be a 32-bit register or a 32-bit indirect operand.",
							code.name());
				}
			}
			case Opcode.NOP -> {
				if (numOperands > 1) {
					error("%s cannot have 2 or 3 operands.", code.name());
				}
				if (hasFirstOperand && (inst.firstOperand().bits() == 8 || isFirstImmediate)) {
					error("%s cannot have an 8-bit operand (%s).", code.name(), inst.firstOperand());
				}
			}
			case Opcode.LEA -> {
				if (numOperands != 2) {
					error("%s requires 2 operands.", code.name());
				}
				if (!isFirstRegister || inst.firstOperand().bits() == 8) {
					error("%s requires a not 8-bit destination register (%s).", code.name(), inst.firstOperand());
				}
				if (!isSecondIndirect) {
					error("%s requires an indirect operand (%s).", code.name(), inst.secondOperand());
				}
			}
			case Opcode.CMP -> {
				if (numOperands != 2) {
					error("%s requires 2 operands.", code.name());
				}
			}
			default -> {}
		}
	}

	/**
	 * This is one of the few general rules of x86 which applies to all instructions. No instruction is allowed to have
	 * more than one immediate value.
	 */
	private static void checkNoMoreThanOneImmediate(final Instruction inst) {
		int count = 0;
		if (inst.hasFirstOperand() && inst.firstOperand() instanceof Immediate) {
			count++;
		}
		if (inst.hasSecondOperand() && inst.secondOperand() instanceof Immediate) {
			count++;
		}
		if (inst.hasThirdOperand() && inst.thirdOperand() instanceof Immediate) {
			count++;
		}
		if (count > 1) {
			error("No instruction can have more than 1 immediate value.");
		}
	}

	/**
	 * This is one of the few general rules of x86 which applies to all instructions. No instruction is allowed to have
	 * more than one indirect operand (except MOVS).
	 */
	private static void checkNoMoreThanOneIndirect(final Instruction inst) {
		if (inst.opcode() == Opcode.MOVS) {
			return;
		}

		int count = 0;
		if (inst.hasFirstOperand() && inst.firstOperand() instanceof IndirectOperand) {
			count++;
		}
		if (inst.hasSecondOperand() && inst.secondOperand() instanceof IndirectOperand) {
			count++;
		}
		if (inst.hasThirdOperand() && inst.thirdOperand() instanceof IndirectOperand) {
			count++;
		}
		if (count > 1) {
			error("No instruction can have more than 1 indirect operand.");
		}
	}
}
