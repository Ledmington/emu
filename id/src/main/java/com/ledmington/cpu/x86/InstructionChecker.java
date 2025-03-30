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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ledmington.cpu.x86.exc.InvalidInstruction;

public final class InstructionChecker {

	private enum OperandType {
		// An 8-bit register
		R8,
		// A 16-bit register
		R16,
		// A 32-bit register
		R32,
		// A general-purpose 64-bit register
		R64,
		// An MMX 64-bit register
		RMM,
		// An XMM 128-bit register
		RXMM,
		// An indirect operand with BYTE PTR size
		M8,
		// An indirect operand with WORD PTR size
		M16,
		// An indirect operand with DWORD PTR size
		M32,
		// An indirect operand with QWORD PTR size
		M64,
		// An indirect operand with XMMWORD PTR size
		M128,
		// An immediate value of 8 bits
		I8,
		// An immediate value of 16 bits
		I16,
		// An immediate value of 32 bits
		I32,
		// An immediate value of 64 bits
		I64
	}

	private static final class Case {
		private final OperandType op1;
		private final OperandType op2;
		private final OperandType op3;

		Case(final OperandType... ot) {
			Objects.requireNonNull(ot);
			this.op1 = ot.length > 0 ? Objects.requireNonNull(ot[0]) : null;
			this.op2 = ot.length > 1 ? Objects.requireNonNull(ot[1]) : null;
			this.op3 = ot.length > 2 ? Objects.requireNonNull(ot[2]) : null;
			if (ot.length > 3) {
				throw new IllegalArgumentException("Too many operand types.");
			}
		}

		public int numOperands() {
			return op3 != null ? 3 : (op2 != null ? 2 : (op1 != null ? 1 : 0));
		}

		public OperandType firstOperandType() {
			return op1;
		}

		public OperandType secondOperandType() {
			return op2;
		}

		public OperandType thirdOperandType() {
			return op3;
		}
	}

	private static Case op(final OperandType... ot) {
		return new Case(ot);
	}

	private static final Map<Opcode, List<Case>> CASES = Map.ofEntries(
			Map.entry(
					Opcode.NOP,
					List.of(
							op(),
							op(OperandType.R16),
							op(OperandType.R32),
							op(OperandType.R64),
							op(OperandType.M16),
							op(OperandType.M32),
							op(OperandType.M64))),
			Map.entry(
					Opcode.MOV,
					List.of(
							op(OperandType.R32, OperandType.R32),
							op(OperandType.R64, OperandType.R64),
							op(OperandType.R8, OperandType.I8),
							op(OperandType.R16, OperandType.I16),
							op(OperandType.R32, OperandType.I32),
							op(OperandType.R64, OperandType.I32),
							op(OperandType.M8, OperandType.I8),
							op(OperandType.M16, OperandType.I16),
							op(OperandType.M32, OperandType.I32),
							op(OperandType.M64, OperandType.I32),
							op(OperandType.M8, OperandType.R8),
							op(OperandType.M64, OperandType.R64),
							op(OperandType.R8, OperandType.M8),
							op(OperandType.R64, OperandType.M64))),
			Map.entry(
					Opcode.MOVSXD, List.of(op(OperandType.R64, OperandType.R32), op(OperandType.R64, OperandType.M32))),
			Map.entry(
					Opcode.CMP,
					List.of(
							op(OperandType.R8, OperandType.M8),
							op(OperandType.R16, OperandType.M16),
							op(OperandType.R32, OperandType.M32),
							op(OperandType.R64, OperandType.M64),
							op(OperandType.R8, OperandType.I8),
							op(OperandType.R16, OperandType.I16),
							op(OperandType.R32, OperandType.I32),
							op(OperandType.R64, OperandType.I32),
							op(OperandType.R8, OperandType.R8),
							op(OperandType.R16, OperandType.R16),
							op(OperandType.R32, OperandType.R32),
							op(OperandType.R64, OperandType.R64),
							op(OperandType.M8, OperandType.R8),
							op(OperandType.M32, OperandType.R32),
							op(OperandType.M8, OperandType.I8),
							op(OperandType.M16, OperandType.I8),
							op(OperandType.M16, OperandType.I16),
							op(OperandType.M32, OperandType.I32),
							op(OperandType.M64, OperandType.I32))),
			Map.entry(
					Opcode.CALL,
					List.of(
							op(OperandType.I32),
							op(OperandType.R64),
							op(OperandType.M16),
							op(OperandType.M32),
							op(OperandType.M64))),
			Map.entry(Opcode.JA, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JAE, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JB, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JBE, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JG, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JGE, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JL, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JLE, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JS, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JNS, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JE, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JNE, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(Opcode.JP, List.of(op(OperandType.I8), op(OperandType.I32))),
			Map.entry(
					Opcode.JMP,
					List.of(
							op(OperandType.I8),
							op(OperandType.I32),
							op(OperandType.R16),
							op(OperandType.R64),
							op(OperandType.M16),
							op(OperandType.M32),
							op(OperandType.M64))),
			Map.entry(
					Opcode.CMOVE, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.CMOVNE, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.CMOVS,
					List.of(
							op(OperandType.R32, OperandType.M32),
							op(OperandType.R64, OperandType.R64),
							op(OperandType.R32, OperandType.R32))),
			Map.entry(
					Opcode.CMOVNS, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.CMOVA,
					List.of(
							op(OperandType.R32, OperandType.M32),
							op(OperandType.R64, OperandType.R64),
							op(OperandType.R32, OperandType.R32))),
			Map.entry(
					Opcode.CMOVAE, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.CMOVB, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.CMOVBE, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.CMOVL, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.CMOVLE, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.CMOVG, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.CMOVGE, List.of(op(OperandType.R32, OperandType.M32), op(OperandType.R64, OperandType.R64))),
			Map.entry(
					Opcode.LEA,
					List.of(
							op(OperandType.R16, OperandType.M16),
							op(OperandType.R32, OperandType.M32),
							op(OperandType.R64, OperandType.M64))),
			Map.entry(
					Opcode.MOVZX,
					List.of(
							op(OperandType.R32, OperandType.R8),
							op(OperandType.R64, OperandType.R8),
							op(OperandType.R64, OperandType.R16),
							op(OperandType.R32, OperandType.M8),
							op(OperandType.R32, OperandType.M16))),
			Map.entry(
					Opcode.MOVSX,
					List.of(
							op(OperandType.R32, OperandType.R8),
							op(OperandType.R64, OperandType.R8),
							op(OperandType.R64, OperandType.R16),
							op(OperandType.R32, OperandType.M8),
							op(OperandType.R32, OperandType.M16),
							op(OperandType.R64, OperandType.M8),
							op(OperandType.R64, OperandType.M16))),
			Map.entry(
					Opcode.PUSH,
					List.of(
							op(OperandType.I8),
							op(OperandType.I32),
							op(OperandType.R16),
							op(OperandType.R64),
							op(OperandType.M64))),
			Map.entry(Opcode.POP, List.of(op(OperandType.R16), op(OperandType.R64))),
			Map.entry(Opcode.CDQ, List.of(op())),
			Map.entry(Opcode.CDQE, List.of(op())),
			Map.entry(Opcode.CWDE, List.of(op())),
			Map.entry(Opcode.LEAVE, List.of(op())),
			Map.entry(Opcode.RET, List.of(op())),
			Map.entry(Opcode.CPUID, List.of(op())),
			Map.entry(Opcode.HLT, List.of(op())),
			Map.entry(
					Opcode.ADD,
					List.of(
							op(OperandType.R64, OperandType.R64),
							op(OperandType.R8, OperandType.I8),
							op(OperandType.R16, OperandType.I8),
							op(OperandType.R16, OperandType.I16),
							op(OperandType.R32, OperandType.I8),
							op(OperandType.R32, OperandType.I32),
							op(OperandType.R64, OperandType.I8),
							op(OperandType.R64, OperandType.I32),
							op(OperandType.R32, OperandType.M32),
							op(OperandType.R64, OperandType.M64),
							op(OperandType.M8, OperandType.I8),
							op(OperandType.M16, OperandType.R16),
							op(OperandType.M32, OperandType.R32),
							op(OperandType.M64, OperandType.R64),
							op(OperandType.M64, OperandType.I8),
							op(OperandType.M64, OperandType.I32))),
			Map.entry(Opcode.ADC, List.of(op(OperandType.R16, OperandType.I16))),
			Map.entry(
					Opcode.AND,
					List.of(
							op(OperandType.R64, OperandType.R64),
							op(OperandType.R8, OperandType.I8),
							op(OperandType.R16, OperandType.I8),
							op(OperandType.R16, OperandType.I16),
							op(OperandType.R32, OperandType.I8),
							op(OperandType.R32, OperandType.I32),
							op(OperandType.R64, OperandType.I8),
							op(OperandType.R64, OperandType.I32),
							op(OperandType.R8, OperandType.M8),
							op(OperandType.R16, OperandType.M16),
							op(OperandType.R32, OperandType.M32),
							op(OperandType.R64, OperandType.M64))));

	private InstructionChecker() {}

	private static void error(final String fmt, final Object... args) {
		throw new InvalidInstruction(String.format(fmt, args));
	}

	public static void check(final Instruction inst) {
		final boolean hasFirstOperand = inst.hasFirstOperand();
		final boolean hasSecondOperand = inst.hasSecondOperand();
		final boolean hasThirdOperand = inst.hasThirdOperand();

		final int numOperands = hasThirdOperand ? 3 : (hasSecondOperand ? 2 : (hasFirstOperand ? 1 : 0));

		if (numOperands >= 2) {
			checkNoMoreThanOneImmediate(inst);
			checkNoMoreThanOneIndirect(inst);
		}

		if (!CASES.containsKey(inst.opcode())) {
			error("Unknown opcode '%s'.", inst.opcode());
		}

		final List<Case> cases = CASES.get(inst.opcode());
		for (final Case c : cases) {
			if (c.numOperands() != numOperands) {
				continue;
			}

			if (numOperands >= 1 && !matches(c.firstOperandType(), inst.firstOperand())) {
				continue;
			}

			if (numOperands >= 2 && !matches(c.secondOperandType(), inst.secondOperand())) {
				continue;
			}

			if (numOperands == 3 && !matches(c.thirdOperandType(), inst.thirdOperand())) {
				continue;
			}

			// we return (meaning that the instruction is correct) as soon as we find a case that matches the current
			// instruction
			return;
		}

		error("'%s' is not a valid instruction.", inst.toString());
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

	private static boolean matches(final OperandType opt, final Operand op) {
		return switch (opt) {
			case R8 -> op instanceof Register8;
			case R16 -> op instanceof Register16;
			case R32 -> op instanceof Register32;
			case R64 -> op instanceof Register64;
			case RMM -> op instanceof RegisterMMX;
			case RXMM -> op instanceof RegisterXMM;
			case M8 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.BYTE_PTR;
			case M16 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.WORD_PTR;
			case M32 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.DWORD_PTR;
			case M64 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.QWORD_PTR;
			case M128 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.XMMWORD_PTR;
			case I8 -> op instanceof final Immediate imm && imm.bits() == 8;
			case I16 -> op instanceof final Immediate imm && imm.bits() == 16;
			case I32 -> op instanceof final Immediate imm && imm.bits() == 32;
			case I64 -> op instanceof final Immediate imm && imm.bits() == 64;
		};
	}
}
