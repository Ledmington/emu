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
		// A 32-bit register (without EIP)
		R32,
		// A general-purpose 64-bit register (without RIP)
		R64,
		// An MMX 64-bit register
		RMM,
		// An XMM 128-bit register
		RXMM,
		// An YMM 256-bit register
		RYMM,
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
		// An indirect operand with YMMWORD PTR size
		M256,
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

	// Various operand combinations
	private static final Case NOTHING = new Case();
	private static final Case R8 = new Case(OperandType.R8);
	private static final Case R16 = new Case(OperandType.R16);
	private static final Case R32 = new Case(OperandType.R32);
	private static final Case R64 = new Case(OperandType.R64);
	private static final Case M8 = new Case(OperandType.M8);
	private static final Case M16 = new Case(OperandType.M16);
	private static final Case M32 = new Case(OperandType.M32);
	private static final Case M64 = new Case(OperandType.M64);
	private static final Case I8 = new Case(OperandType.I8);
	private static final Case I32 = new Case(OperandType.I32);
	private static final Case R8_R8 = new Case(OperandType.R8, OperandType.R8);
	private static final Case R16_R8 = new Case(OperandType.R16, OperandType.R8);
	private static final Case R16_R16 = new Case(OperandType.R16, OperandType.R16);
	private static final Case R32_R8 = new Case(OperandType.R32, OperandType.R8);
	private static final Case R32_R16 = new Case(OperandType.R32, OperandType.R16);
	private static final Case R32_R32 = new Case(OperandType.R32, OperandType.R32);
	private static final Case R32_RXMM = new Case(OperandType.R32, OperandType.RXMM);
	private static final Case R32_RYMM = new Case(OperandType.R32, OperandType.RYMM);
	private static final Case R64_R8 = new Case(OperandType.R64, OperandType.R8);
	private static final Case R64_R16 = new Case(OperandType.R64, OperandType.R16);
	private static final Case R64_R32 = new Case(OperandType.R64, OperandType.R32);
	private static final Case R64_R64 = new Case(OperandType.R64, OperandType.R64);
	private static final Case RMM_R32 = new Case(OperandType.RMM, OperandType.R32);
	private static final Case RMM_R64 = new Case(OperandType.RMM, OperandType.R64);
	private static final Case RMM_RMM = new Case(OperandType.RMM, OperandType.RMM);
	private static final Case RXMM_R32 = new Case(OperandType.RXMM, OperandType.R32);
	private static final Case RXMM_R64 = new Case(OperandType.RXMM, OperandType.R64);
	private static final Case RXMM_RXMM = new Case(OperandType.RXMM, OperandType.RXMM);
	private static final Case R8_I8 = new Case(OperandType.R8, OperandType.I8);
	private static final Case R16_I8 = new Case(OperandType.R16, OperandType.I8);
	private static final Case R16_I16 = new Case(OperandType.R16, OperandType.I16);
	private static final Case R32_I8 = new Case(OperandType.R32, OperandType.I8);
	private static final Case R32_I32 = new Case(OperandType.R32, OperandType.I32);
	private static final Case R64_I8 = new Case(OperandType.R64, OperandType.I8);
	private static final Case R64_I32 = new Case(OperandType.R64, OperandType.I32);
	private static final Case R64_I64 = new Case(OperandType.R64, OperandType.I64);
	private static final Case RXMM_I8 = new Case(OperandType.RXMM, OperandType.I8);
	private static final Case M8_I8 = new Case(OperandType.M8, OperandType.I8);
	private static final Case M16_I8 = new Case(OperandType.M16, OperandType.I8);
	private static final Case M16_I16 = new Case(OperandType.M16, OperandType.I16);
	private static final Case M32_I32 = new Case(OperandType.M32, OperandType.I32);
	private static final Case M64_I8 = new Case(OperandType.M64, OperandType.I8);
	private static final Case M64_I32 = new Case(OperandType.M64, OperandType.I32);
	private static final Case M8_M8 = new Case(OperandType.M8, OperandType.M8);
	private static final Case M16_M16 = new Case(OperandType.M16, OperandType.M16);
	private static final Case M32_M32 = new Case(OperandType.M32, OperandType.M32);
	private static final Case M8_R8 = new Case(OperandType.M8, OperandType.R8);
	private static final Case M16_R16 = new Case(OperandType.M16, OperandType.R16);
	private static final Case M32_R32 = new Case(OperandType.M32, OperandType.R32);
	private static final Case M64_R64 = new Case(OperandType.M64, OperandType.R64);
	private static final Case M64_RXMM = new Case(OperandType.M64, OperandType.RXMM);
	private static final Case M128_RXMM = new Case(OperandType.M128, OperandType.RXMM);
	private static final Case R8_M8 = new Case(OperandType.R8, OperandType.M8);
	private static final Case R16_M16 = new Case(OperandType.R16, OperandType.M16);
	private static final Case R32_M8 = new Case(OperandType.R32, OperandType.M8);
	private static final Case R32_M16 = new Case(OperandType.R32, OperandType.M16);
	private static final Case R32_M32 = new Case(OperandType.R32, OperandType.M32);
	private static final Case R64_M8 = new Case(OperandType.R64, OperandType.M8);
	private static final Case R64_M16 = new Case(OperandType.R64, OperandType.M16);
	private static final Case R64_M32 = new Case(OperandType.R64, OperandType.M32);
	private static final Case R64_M64 = new Case(OperandType.R64, OperandType.M64);
	private static final Case RMM_M64 = new Case(OperandType.RMM, OperandType.M64);
	private static final Case RXMM_M32 = new Case(OperandType.RXMM, OperandType.M32);
	private static final Case RXMM_M64 = new Case(OperandType.RXMM, OperandType.M64);
	private static final Case RXMM_M128 = new Case(OperandType.RXMM, OperandType.M128);
	private static final Case RYMM_M256 = new Case(OperandType.RYMM, OperandType.M256);
	private static final Case R32_R32_I8 = new Case(OperandType.R32, OperandType.R32, OperandType.I8);
	private static final Case R32_R32_I32 = new Case(OperandType.R32, OperandType.R32, OperandType.I32);
	private static final Case R64_R64_I32 = new Case(OperandType.R64, OperandType.R64, OperandType.I32);
	private static final Case R32_RMM_I8 = new Case(OperandType.R32, OperandType.RMM, OperandType.I8);
	private static final Case R64_R64_I8 = new Case(OperandType.R64, OperandType.R64, OperandType.I8);
	private static final Case RMM_RMM_I8 = new Case(OperandType.RMM, OperandType.RMM, OperandType.I8);
	private static final Case RXMM_RXMM_I8 = new Case(OperandType.RXMM, OperandType.RXMM, OperandType.I8);
	private static final Case R32_M32_I32 = new Case(OperandType.R32, OperandType.M32, OperandType.I32);
	private static final Case R64_M64_I32 = new Case(OperandType.R64, OperandType.M64, OperandType.I32);
	private static final Case RXMM_RXMM_RXMM = new Case(OperandType.RXMM, OperandType.RXMM, OperandType.RXMM);
	private static final Case RYMM_RYMM_RYMM = new Case(OperandType.RYMM, OperandType.RYMM, OperandType.RYMM);
	private static final Case RYMM_RYMM_M256 = new Case(OperandType.RYMM, OperandType.RYMM, OperandType.M256);

	private static final Map<Opcode, List<Case>> CASES = Map.<Opcode, List<Case>>ofEntries(
			Map.entry(Opcode.NOP, List.of(NOTHING, R16, R32, R64, M16, M32, M64)),
			Map.entry(
					Opcode.MOV,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I16, R32_I32, R64_I32, M8_I8, M16_I16, M32_I32,
							M64_I32, M8_R8, M16_R16, M32_R32, M64_R64, R8_M8, R16_M16, R32_M32, R64_M64)),
			Map.entry(Opcode.MOVSXD, List.of(R64_R32, R64_M32)),
			Map.entry(
					Opcode.CMP,
					List.of(
							R8_M8, R16_M16, R32_M32, R64_M64, R8_I8, R16_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32,
							R8_R8, R16_R16, R32_R32, R64_R64, M8_R8, M32_R32, M64_R64, M8_I8, M16_I8, M16_I16, M32_I32,
							M64_I8, M64_I32)),
			Map.entry(Opcode.CALL, List.of(I32, R64, M16, M32, M64)),
			Map.entry(Opcode.JA, List.of(I8, I32)),
			Map.entry(Opcode.JAE, List.of(I8, I32)),
			Map.entry(Opcode.JB, List.of(I8, I32)),
			Map.entry(Opcode.JBE, List.of(I8, I32)),
			Map.entry(Opcode.JG, List.of(I8, I32)),
			Map.entry(Opcode.JGE, List.of(I8, I32)),
			Map.entry(Opcode.JL, List.of(I8, I32)),
			Map.entry(Opcode.JLE, List.of(I8, I32)),
			Map.entry(Opcode.JS, List.of(I8, I32)),
			Map.entry(Opcode.JNS, List.of(I8, I32)),
			Map.entry(Opcode.JE, List.of(I8, I32)),
			Map.entry(Opcode.JNE, List.of(I8, I32)),
			Map.entry(Opcode.JP, List.of(I8, I32)),
			Map.entry(Opcode.JMP, List.of(I8, I32, R16, R64, M16, M32, M64)),
			Map.entry(Opcode.CMOVE, List.of(R32_M32, R64_M64, R32_R32, R64_R64)),
			Map.entry(Opcode.CMOVNE, List.of(R32_M32, R64_M64, R32_R32, R64_R64)),
			Map.entry(Opcode.CMOVS, List.of(R32_M32, R64_M64, R64_R64, R32_R32)),
			Map.entry(Opcode.CMOVNS, List.of(R32_M32, R64_R64, R32_R32)),
			Map.entry(Opcode.CMOVA, List.of(R32_M32, R64_M64, R64_R64, R32_R32)),
			Map.entry(Opcode.CMOVAE, List.of(R32_M32, R32_R32, R64_R64)),
			Map.entry(Opcode.CMOVB, List.of(R32_M32, R64_M64, R32_R32, R64_R64)),
			Map.entry(Opcode.CMOVBE, List.of(R32_M32, R32_R32, R64_R64)),
			Map.entry(Opcode.CMOVL, List.of(R32_M32, R32_R32, R64_R64)),
			Map.entry(Opcode.CMOVLE, List.of(R32_M32, R32_R32, R64_R64)),
			Map.entry(Opcode.CMOVG, List.of(R32_M32, R64_M64, R32_R32, R64_R64)),
			Map.entry(Opcode.CMOVGE, List.of(R32_M32, R32_R32, R64_R64)),
			Map.entry(Opcode.LEA, List.of(R16_M16, R32_M32, R64_M64)),
			Map.entry(Opcode.MOVZX, List.of(R32_R8, R32_R16, R64_R8, R64_R16, R32_M8, R32_M16)),
			Map.entry(Opcode.MOVSX, List.of(R32_R8, R64_R8, R64_R16, R32_M8, R32_M16, R64_M8, R64_M16)),
			Map.entry(Opcode.PUSH, List.of(I8, I32, R16, R64, M64)),
			Map.entry(Opcode.POP, List.of(R16, R64)),
			Map.entry(Opcode.CDQ, List.of(NOTHING)),
			Map.entry(Opcode.CDQE, List.of(NOTHING)),
			Map.entry(Opcode.CWDE, List.of(NOTHING)),
			Map.entry(Opcode.LEAVE, List.of(NOTHING)),
			Map.entry(Opcode.INT3, List.of(NOTHING)),
			Map.entry(Opcode.RET, List.of(NOTHING)),
			Map.entry(Opcode.CPUID, List.of(NOTHING)),
			Map.entry(Opcode.HLT, List.of(NOTHING)),
			Map.entry(
					Opcode.ADD,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32,
							R32_M32, R64_M64, M8_I8, M16_R16, M32_R32, M64_R64, M64_I8, M64_I32)),
			Map.entry(Opcode.ADC, List.of(R16_I16, R32_I8, R64_I8, M8_R8, M32_R32)),
			Map.entry(
					Opcode.AND,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32,
							R8_M8, R16_M16, R32_M32, R64_M64, M8_I8, M32_I32, M64_I8, M64_I32, M32_R32)),
			Map.entry(
					Opcode.SUB,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R16_I16, R32_I8, R32_I32, R8_I8, R64_I8, R64_I32, R32_M32,
							R64_M64, M16_R16, M32_R32, M64_R64, M8_I8, M16_I8, M64_I8, M64_I32)),
			Map.entry(Opcode.SBB, List.of(R32_R32, R64_R64, R8_I8, R16_I16, R32_I8, R64_I8)),
			Map.entry(Opcode.SHR, List.of(R8_R8, R16_R8, R32_R8, R64_R8, R8_I8, R16_I8, R32_I8, R64_I8)),
			Map.entry(Opcode.SAR, List.of(R8_R8, R16_R8, R32_R8, R64_R8, R8_I8, R16_I8, R32_I8, R64_I8)),
			Map.entry(Opcode.SHL, List.of(R8_R8, R16_R8, R32_R8, R64_R8, R8_I8, R16_I8, R32_I8, R64_I8)),
			Map.entry(
					Opcode.IMUL,
					List.of(
							R32_R32_I8,
							R32_R32_I32,
							R64_R64_I32,
							R32_M32_I32,
							R64_R64_I8,
							R64_M64_I32,
							R16_R16,
							R32_R32,
							R64_R64,
							R32_M32,
							R64_M64)),
			Map.entry(Opcode.IDIV, List.of(R32, R64)),
			Map.entry(Opcode.DIV, List.of(R8, R16, R32, R64, M8, M16, M32, M64)),
			Map.entry(Opcode.MUL, List.of(R8, R16, R32, R64)),
			Map.entry(
					Opcode.OR,
					List.of(
							R8_R8, R32_R32, R64_R64, R8_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32, R8_M8, R32_M32,
							R64_M64, M8_I8, M16_I16, M32_I32, M64_I8, M64_I32, M8_R8, M32_R32, M64_R64)),
			Map.entry(
					Opcode.XOR,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32, M8_R8,
							M32_R32, R8_M8, R32_M32, R64_M64)),
			Map.entry(Opcode.NOT, List.of(R32, R64)),
			Map.entry(Opcode.NEG, List.of(R32, R64, M32, M64)),
			Map.entry(
					Opcode.TEST,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I16, R32_I32, R64_I32, M8_I8, M8_R8, M16_I16,
							M16_R16, M32_I32, M32_R32, M64_R64)),
			Map.entry(Opcode.UD2, List.of(NOTHING)),
			Map.entry(Opcode.MOVS, List.of(M8_M8, M16_M16, M32_M32)),
			Map.entry(Opcode.STOS, List.of(M8_R8, M32_R32, M64_R64)),
			Map.entry(Opcode.MOVDQA, List.of(RXMM_RXMM, RXMM_M128)),
			Map.entry(Opcode.MOVDQU, List.of(RXMM_M128)),
			Map.entry(Opcode.MOVAPS, List.of(RXMM_RXMM, M128_RXMM, RXMM_M128)),
			Map.entry(Opcode.MOVAPD, List.of(RXMM_RXMM, M128_RXMM)),
			Map.entry(Opcode.MOVQ, List.of(RMM_R64, RXMM_R64, M64_RXMM, RMM_M64, RXMM_M64)),
			Map.entry(Opcode.MOVD, List.of(RMM_R32, RXMM_R32, RXMM_M32, RMM_M64)),
			Map.entry(Opcode.MOVHPS, List.of(RXMM_M64, M64_RXMM)),
			Map.entry(Opcode.MOVHPD, List.of(M64_RXMM)),
			Map.entry(Opcode.MOVHLPS, List.of(RXMM_RXMM)),
			Map.entry(Opcode.PUNPCKLQDQ, List.of(RXMM_RXMM)),
			Map.entry(Opcode.PUNPCKLDQ, List.of(RXMM_RXMM)),
			Map.entry(Opcode.PUNPCKHQDQ, List.of(RXMM_RXMM)),
			Map.entry(Opcode.SETA, List.of(R8, M8)),
			Map.entry(Opcode.SETAE, List.of(R8, M8)),
			Map.entry(Opcode.SETE, List.of(R8, M8)),
			Map.entry(Opcode.SETNE, List.of(R8, M8)),
			Map.entry(Opcode.SETB, List.of(R8, M8)),
			Map.entry(Opcode.SETBE, List.of(R8, M8)),
			Map.entry(Opcode.SETL, List.of(R8, M8)),
			Map.entry(Opcode.SETLE, List.of(R8, M8)),
			Map.entry(Opcode.SETG, List.of(R8, M8)),
			Map.entry(Opcode.SETGE, List.of(R8, M8)),
			Map.entry(Opcode.SETO, List.of(R8, M8)),
			Map.entry(Opcode.SETNO, List.of(R8, M8)),
			Map.entry(Opcode.MOVABS, List.of(R64_I64)),
			Map.entry(Opcode.MOVUPS, List.of(RXMM_M128, M128_RXMM)),
			Map.entry(Opcode.MOVSD, List.of(RXMM_M64)),
			Map.entry(Opcode.ENDBR64, List.of(NOTHING)),
			Map.entry(Opcode.INC, List.of(R8, R32, R64, M8, M16, M32, M64)),
			Map.entry(Opcode.DEC, List.of(R8, R32, R64, M8, M16, M32, M64)),
			Map.entry(Opcode.PSHUFD, List.of(RXMM_RXMM_I8)),
			Map.entry(Opcode.PSHUFW, List.of(RMM_RMM_I8)),
			Map.entry(Opcode.SHUFPD, List.of(RXMM_RXMM_I8)),
			Map.entry(Opcode.SHUFPS, List.of(RXMM_RXMM_I8)),
			Map.entry(Opcode.PXOR, List.of(RMM_RMM, RXMM_RXMM, RXMM_M128)),
			Map.entry(Opcode.POR, List.of(RXMM_RXMM, RXMM_M128)),
			Map.entry(Opcode.PAND, List.of(RXMM_RXMM, RXMM_M128)),
			Map.entry(Opcode.PADDQ, List.of(RXMM_RXMM, RXMM_M128)),
			Map.entry(Opcode.PSUBQ, List.of(RXMM_RXMM, RXMM_M128)),
			Map.entry(Opcode.PSUBB, List.of(RXMM_RXMM)),
			Map.entry(Opcode.PSUBW, List.of(RXMM_RXMM)),
			Map.entry(Opcode.PSUBD, List.of(RXMM_RXMM)),
			Map.entry(Opcode.PSLLDQ, List.of(RXMM_I8)),
			Map.entry(Opcode.PSRLDQ, List.of(RXMM_I8)),
			Map.entry(Opcode.CVTSI2SD, List.of(RXMM_R64, RXMM_R32)),
			Map.entry(Opcode.DIVSD, List.of(RXMM_RXMM)),
			Map.entry(Opcode.ADDSD, List.of(RXMM_RXMM)),
			Map.entry(Opcode.XORPS, List.of(RXMM_RXMM)),
			Map.entry(Opcode.UCOMISD, List.of(RXMM_M64)),
			Map.entry(Opcode.UCOMISS, List.of(RXMM_M32)),
			Map.entry(Opcode.BT, List.of(R32_I8, R32_R32, R64_I8, R64_R64)),
			Map.entry(Opcode.BTC, List.of(R32_I8, R32_R32, R64_I8, R64_R64)),
			Map.entry(Opcode.BTR, List.of(R32_I8, R32_R32, R64_I8, R64_R64)),
			Map.entry(Opcode.BTS, List.of(R32_I8, R32_R32, R64_I8, R64_R64)),
			Map.entry(Opcode.XGETBV, List.of(NOTHING)),
			Map.entry(Opcode.XCHG, List.of(R8_R8, R16_R16, R32_R32, R64_R64, M8_R8, M16_R16, M32_R32, M64_R64)),
			Map.entry(Opcode.BSWAP, List.of(R32, R64)),
			Map.entry(Opcode.PREFETCHNTA, List.of(M8)),
			Map.entry(Opcode.PREFETCHT0, List.of(M8)),
			Map.entry(Opcode.PREFETCHT1, List.of(M8)),
			Map.entry(Opcode.PREFETCHT2, List.of(M8)),
			Map.entry(Opcode.CMPXCHG, List.of(M8_R8, M16_R16, M32_R32, M64_R64)),
			Map.entry(Opcode.XADD, List.of(M8_R8, M16_R16, M32_R32, M64_R64)),
			Map.entry(Opcode.PCMPEQB, List.of(RMM_RMM, RXMM_RXMM)),
			Map.entry(Opcode.PCMPEQW, List.of(RMM_RMM, RXMM_RXMM)),
			Map.entry(Opcode.PCMPEQD, List.of(RMM_RMM, RXMM_RXMM)),
			Map.entry(Opcode.RDRAND, List.of(R16, R32, R64)),
			Map.entry(Opcode.RDSEED, List.of(R16, R32, R64)),
			Map.entry(Opcode.RDSSPQ, List.of(R64)),
			Map.entry(Opcode.INCSSPQ, List.of(R64)),
			Map.entry(Opcode.LAHF, List.of(NOTHING)),
			Map.entry(Opcode.SAHF, List.of(NOTHING)),
			Map.entry(Opcode.SYSCALL, List.of(NOTHING)),
			Map.entry(Opcode.BSR, List.of(R32_M32)),
			Map.entry(Opcode.BSF, List.of(R32_R32, R64_R64)),
			Map.entry(Opcode.ROR, List.of(R32_I8, R64_I8)),
			Map.entry(Opcode.ROL, List.of(R32_I8, R64_I8)),
			Map.entry(Opcode.RCR, List.of(R32_I8)),
			Map.entry(Opcode.RCL, List.of(R32_I8)),
			Map.entry(Opcode.PMOVMSKB, List.of(R32_RXMM)),
			Map.entry(Opcode.PMINUB, List.of(RXMM_RXMM)),
			Map.entry(Opcode.PALIGNR, List.of(RXMM_RXMM_I8)),
			Map.entry(Opcode.VPXOR, List.of(RXMM_RXMM_RXMM)),
			Map.entry(Opcode.PEXTRW, List.of(R32_RMM_I8)),
			Map.entry(Opcode.VMOVDQU, List.of(RYMM_M256)),
			Map.entry(Opcode.VPMINUB, List.of(RYMM_RYMM_RYMM)),
			Map.entry(Opcode.VPMOVMSKB, List.of(R32_RYMM)),
			Map.entry(Opcode.VPCMPEQB, List.of(RYMM_RYMM_M256)),
			Map.entry(Opcode.VZEROALL, List.of(NOTHING)),
			Map.entry(Opcode.VMOVQ, List.of(RXMM_M64)),
			Map.entry(Opcode.VMOVD, List.of(RXMM_M32)),
			Map.entry(Opcode.PCMPISTRI, List.of(RXMM_RXMM_I8)));

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
			// filter out the cases with different number of operands
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
			case R32 -> op instanceof final Register32 r && !r.equals(Register32.EIP);
			case R64 -> op instanceof final Register64 r && !r.equals(Register64.RIP);
			case RMM -> op instanceof RegisterMMX;
			case RXMM -> op instanceof RegisterXMM;
			case RYMM -> op instanceof RegisterYMM;
			case M8 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.BYTE_PTR;
			case M16 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.WORD_PTR;
			case M32 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.DWORD_PTR;
			case M64 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.QWORD_PTR;
			case M128 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.XMMWORD_PTR;
			case M256 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.YMMWORD_PTR;
			case I8 -> op instanceof final Immediate imm && imm.bits() == 8;
			case I16 -> op instanceof final Immediate imm && imm.bits() == 16;
			case I32 -> op instanceof final Immediate imm && imm.bits() == 32;
			case I64 -> op instanceof final Immediate imm && imm.bits() == 64;
		};
	}
}
