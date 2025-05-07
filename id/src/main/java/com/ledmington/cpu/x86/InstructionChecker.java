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

@SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass")
public final class InstructionChecker {

	/** Different categories of operands. Check {@link #matches} to know exactly what each one represents. */
	private enum OperandType {
		// TODO: since some instruction can only target AL, AX, EAX or RAX, maybe make a specific category only for
		// those?
		/** An 8-bit register. */
		R8,
		/** A 16-bit register. */
		R16,
		/** A 32-bit register (without EIP). */
		R32,
		/** A general-purpose 64-bit register (without RIP). */
		R64,
		/** An MMX 64-bit register. */
		RMM,
		/** An XMM 128-bit register. */
		RX,
		/** A YMM 256-bit register. */
		RY,
		/** A ZMM 512-bit register. */
		RZ,
		/** A mask register. */
		RK,
		/** A segment register. */
		RS,
		// TODO: encode also indirect operands with and without a segment register
		/** An indirect operand with BYTE PTR size. */
		M8,
		/** An indirect operand with WORD PTR size. */
		M16,
		/** An indirect operand with DWORD PTR size. */
		M32,
		/** An indirect operand with QWORD PTR size. */
		M64,
		/** An indirect operand with XMMWORD PTR size. */
		M128,
		/** An indirect operand with YMMWORD PTR size. */
		M256,
		/** An indirect operand with ZMMWORD PTR size. */
		M512,
		/** An immediate value of 8 bits. */
		I8,
		/** An immediate value of 16 bits. */
		I16,
		/** An immediate value of 32 bits. */
		I32,
		/** An immediate value of 64 bits. */
		I64,
		/** A segmented address with a 64-bit immediate. */
		S64
	}

	private static final class Case {
		private final OperandType op1;
		private final OperandType op2;
		private final OperandType op3;
		private final OperandType op4;

		/* default */ Case(final OperandType... ot) {
			Objects.requireNonNull(ot);
			this.op1 = ot.length > 0 ? Objects.requireNonNull(ot[0]) : null;
			this.op2 = ot.length > 1 ? Objects.requireNonNull(ot[1]) : null;
			this.op3 = ot.length > 2 ? Objects.requireNonNull(ot[2]) : null;
			this.op4 = ot.length > 3 ? Objects.requireNonNull(ot[3]) : null;
			if (ot.length > 4) {
				throw new IllegalArgumentException("Too many operand types.");
			}
		}

		public int numOperands() {
			return op4 != null ? 4 : (op3 != null ? 3 : (op2 != null ? 2 : (op1 != null ? 1 : 0)));
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

		public OperandType fourthOperandType() {
			return op4;
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
	private static final Case I16 = new Case(OperandType.I16);
	private static final Case I32 = new Case(OperandType.I32);
	private static final Case I16_I8 = new Case(OperandType.I16, OperandType.I8);
	private static final Case R8_R8 = new Case(OperandType.R8, OperandType.R8);
	private static final Case R8_R16 = new Case(OperandType.R8, OperandType.R16);
	private static final Case R16_R8 = new Case(OperandType.R16, OperandType.R8);
	private static final Case R16_R16 = new Case(OperandType.R16, OperandType.R16);
	private static final Case R16_R32 = new Case(OperandType.R16, OperandType.R32);
	private static final Case R32_R8 = new Case(OperandType.R32, OperandType.R8);
	private static final Case R32_R16 = new Case(OperandType.R32, OperandType.R16);
	private static final Case R32_R32 = new Case(OperandType.R32, OperandType.R32);
	private static final Case R32_RX = new Case(OperandType.R32, OperandType.RX);
	private static final Case R32_RY = new Case(OperandType.R32, OperandType.RY);
	private static final Case R64_R8 = new Case(OperandType.R64, OperandType.R8);
	private static final Case R64_R16 = new Case(OperandType.R64, OperandType.R16);
	private static final Case R64_R32 = new Case(OperandType.R64, OperandType.R32);
	private static final Case R64_R64 = new Case(OperandType.R64, OperandType.R64);
	private static final Case R64_RX = new Case(OperandType.R64, OperandType.RX);
	private static final Case R64_RK = new Case(OperandType.R64, OperandType.RK);
	private static final Case RMM_R32 = new Case(OperandType.RMM, OperandType.R32);
	private static final Case RMM_R64 = new Case(OperandType.RMM, OperandType.R64);
	private static final Case RMM_RMM = new Case(OperandType.RMM, OperandType.RMM);
	private static final Case RX_R32 = new Case(OperandType.RX, OperandType.R32);
	private static final Case RX_R64 = new Case(OperandType.RX, OperandType.R64);
	private static final Case RX_RX = new Case(OperandType.RX, OperandType.RX);
	private static final Case RY_RX = new Case(OperandType.RY, OperandType.RX);
	private static final Case RZ_R32 = new Case(OperandType.RZ, OperandType.R32);
	private static final Case RZ_RX = new Case(OperandType.RZ, OperandType.RX);
	private static final Case R32_RK = new Case(OperandType.R32, OperandType.RK);
	private static final Case RK_R32 = new Case(OperandType.RK, OperandType.R32);
	private static final Case RK_R64 = new Case(OperandType.RK, OperandType.R64);
	private static final Case RK_RK = new Case(OperandType.RK, OperandType.RK);
	private static final Case R8_I8 = new Case(OperandType.R8, OperandType.I8);
	private static final Case R16_I8 = new Case(OperandType.R16, OperandType.I8);
	private static final Case R16_I16 = new Case(OperandType.R16, OperandType.I16);
	private static final Case R32_I8 = new Case(OperandType.R32, OperandType.I8);
	private static final Case R32_I32 = new Case(OperandType.R32, OperandType.I32);
	private static final Case R64_I8 = new Case(OperandType.R64, OperandType.I8);
	private static final Case R64_I32 = new Case(OperandType.R64, OperandType.I32);
	private static final Case R64_I64 = new Case(OperandType.R64, OperandType.I64);
	private static final Case RX_I8 = new Case(OperandType.RX, OperandType.I8);
	private static final Case R8_S64 = new Case(OperandType.R8, OperandType.S64);
	private static final Case R32_S64 = new Case(OperandType.R32, OperandType.S64);
	private static final Case S64_R8 = new Case(OperandType.S64, OperandType.R8);
	private static final Case S64_R32 = new Case(OperandType.S64, OperandType.R32);
	private static final Case M8_I8 = new Case(OperandType.M8, OperandType.I8);
	private static final Case M16_I8 = new Case(OperandType.M16, OperandType.I8);
	private static final Case M16_I16 = new Case(OperandType.M16, OperandType.I16);
	private static final Case M32_I8 = new Case(OperandType.M32, OperandType.I8);
	private static final Case M32_I32 = new Case(OperandType.M32, OperandType.I32);
	private static final Case M64_I8 = new Case(OperandType.M64, OperandType.I8);
	private static final Case M64_I32 = new Case(OperandType.M64, OperandType.I32);
	private static final Case M8_M8 = new Case(OperandType.M8, OperandType.M8);
	private static final Case M16_M16 = new Case(OperandType.M16, OperandType.M16);
	private static final Case M32_M32 = new Case(OperandType.M32, OperandType.M32);
	private static final Case M8_R8 = new Case(OperandType.M8, OperandType.R8);
	private static final Case M8_R16 = new Case(OperandType.M8, OperandType.R16);
	private static final Case M16_R16 = new Case(OperandType.M16, OperandType.R16);
	private static final Case M16_RS = new Case(OperandType.M16, OperandType.RS);
	private static final Case M32_R16 = new Case(OperandType.M32, OperandType.R16);
	private static final Case M32_R32 = new Case(OperandType.M32, OperandType.R32);
	private static final Case M64_R64 = new Case(OperandType.M64, OperandType.R64);
	private static final Case M64_RX = new Case(OperandType.M64, OperandType.RX);
	private static final Case M128_RX = new Case(OperandType.M128, OperandType.RX);
	private static final Case M256_RY = new Case(OperandType.M256, OperandType.RY);
	private static final Case M512_RZ = new Case(OperandType.M512, OperandType.RZ);
	private static final Case R8_M8 = new Case(OperandType.R8, OperandType.M8);
	private static final Case R16_M8 = new Case(OperandType.R16, OperandType.M8);
	private static final Case R16_M16 = new Case(OperandType.R16, OperandType.M16);
	private static final Case R16_M32 = new Case(OperandType.R16, OperandType.M32);
	private static final Case R32_M8 = new Case(OperandType.R32, OperandType.M8);
	private static final Case R32_M16 = new Case(OperandType.R32, OperandType.M16);
	private static final Case R32_M32 = new Case(OperandType.R32, OperandType.M32);
	private static final Case R64_M8 = new Case(OperandType.R64, OperandType.M8);
	private static final Case R64_M16 = new Case(OperandType.R64, OperandType.M16);
	private static final Case R64_M32 = new Case(OperandType.R64, OperandType.M32);
	private static final Case R64_M64 = new Case(OperandType.R64, OperandType.M64);
	private static final Case RMM_M64 = new Case(OperandType.RMM, OperandType.M64);
	private static final Case RX_M32 = new Case(OperandType.RX, OperandType.M32);
	private static final Case RX_M64 = new Case(OperandType.RX, OperandType.M64);
	private static final Case RX_M128 = new Case(OperandType.RX, OperandType.M128);
	private static final Case RY_M256 = new Case(OperandType.RY, OperandType.M256);
	private static final Case RZ_M512 = new Case(OperandType.RZ, OperandType.M512);
	private static final Case RS_M16 = new Case(OperandType.RS, OperandType.M16);
	private static final Case I8_R8 = new Case(OperandType.I8, OperandType.R8);
	private static final Case I8_R32 = new Case(OperandType.I8, OperandType.R32);
	private static final Case R32_R32_I8 = new Case(OperandType.R32, OperandType.R32, OperandType.I8);
	private static final Case R32_R32_I32 = new Case(OperandType.R32, OperandType.R32, OperandType.I32);
	private static final Case R64_R64_I32 = new Case(OperandType.R64, OperandType.R64, OperandType.I32);
	private static final Case R32_RMM_I8 = new Case(OperandType.R32, OperandType.RMM, OperandType.I8);
	private static final Case R64_R64_I8 = new Case(OperandType.R64, OperandType.R64, OperandType.I8);
	private static final Case RMM_RMM_I8 = new Case(OperandType.RMM, OperandType.RMM, OperandType.I8);
	private static final Case RX_RX_I8 = new Case(OperandType.RX, OperandType.RX, OperandType.I8);
	private static final Case RX_M128_I8 = new Case(OperandType.RX, OperandType.M128, OperandType.I8);
	private static final Case R32_M32_I32 = new Case(OperandType.R32, OperandType.M32, OperandType.I32);
	private static final Case R64_M64_I32 = new Case(OperandType.R64, OperandType.M64, OperandType.I32);
	private static final Case R32_R32_R32 = new Case(OperandType.R32, OperandType.R32, OperandType.R32);
	private static final Case R64_R64_R8 = new Case(OperandType.R64, OperandType.R64, OperandType.R8);
	private static final Case R64_R64_R64 = new Case(OperandType.R64, OperandType.R64, OperandType.R64);
	private static final Case RK_RX_M128 = new Case(OperandType.RK, OperandType.RX, OperandType.M128);
	private static final Case RK_RY_M256 = new Case(OperandType.RK, OperandType.RY, OperandType.M256);
	private static final Case RX_RX_RX = new Case(OperandType.RX, OperandType.RX, OperandType.RX);
	private static final Case RX_RX_M128 = new Case(OperandType.RX, OperandType.RX, OperandType.M128);
	private static final Case RY_RY_RY = new Case(OperandType.RY, OperandType.RY, OperandType.RY);
	private static final Case RK_RX_RX = new Case(OperandType.RK, OperandType.RX, OperandType.RX);
	private static final Case RK_RY_RY = new Case(OperandType.RK, OperandType.RY, OperandType.RY);
	private static final Case RK_RK_RK = new Case(OperandType.RK, OperandType.RK, OperandType.RK);
	private static final Case RY_RY_M256 = new Case(OperandType.RY, OperandType.RY, OperandType.M256);
	private static final Case RX_RX_M128_I8 =
			new Case(OperandType.RX, OperandType.RX, OperandType.M128, OperandType.I8);
	private static final Case RY_RY_M256_I8 =
			new Case(OperandType.RY, OperandType.RY, OperandType.M256, OperandType.I8);
	private static final Case RY_RY_RY_I8 = new Case(OperandType.RY, OperandType.RY, OperandType.RY, OperandType.I8);

	private static final Map<Opcode, List<Case>> CASES = Map.<Opcode, List<Case>>ofEntries(
			Map.entry(Opcode.NOP, List.of(NOTHING, R16, R32, R64, M16, M32, M64)),
			Map.entry(
					Opcode.MOV,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I16, R32_I32, R64_I32, M8_I8, M16_I16, M32_I32,
							M64_I32, M8_R8, M16_R16, M16_RS, M32_R32, M64_R64, R8_M8, R16_M16, R32_M32, R64_M64,
							RS_M16)),
			Map.entry(Opcode.MOVSXD, List.of(R32_R32, R64_R32, R32_M32, R64_M32)),
			Map.entry(
					Opcode.CMP,
					List.of(
							R8_M8, R16_M16, R32_M32, R64_M64, R8_I8, R16_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32,
							R8_R8, R16_R16, R32_R32, R64_R64, M8_R8, M32_R32, M64_R64, M8_I8, M16_I8, M16_I16, M32_I8,
							M32_I32, M64_I8, M64_I32)),
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
			Map.entry(Opcode.JNP, List.of(I8, I32)),
			Map.entry(Opcode.JO, List.of(I8, I32)),
			Map.entry(Opcode.JNO, List.of(I8, I32)),
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
			Map.entry(Opcode.LDDQU, List.of(RX_M128)),
			Map.entry(Opcode.MOVZX, List.of(R32_R8, R32_R16, R64_R8, R64_R16, R32_M8, R32_M16)),
			Map.entry(Opcode.MOVSX, List.of(R32_R8, R64_R8, R64_R16, R32_M8, R32_M16, R64_M8, R64_M16)),
			Map.entry(Opcode.PUSH, List.of(I8, I32, R16, R64, M64)),
			Map.entry(Opcode.POP, List.of(R16, R64, M64)),
			Map.entry(Opcode.CDQ, List.of(NOTHING)),
			Map.entry(Opcode.CDQE, List.of(NOTHING)),
			Map.entry(Opcode.CWDE, List.of(NOTHING)),
			Map.entry(Opcode.LEAVE, List.of(NOTHING)),
			Map.entry(Opcode.INT3, List.of(NOTHING)),
			Map.entry(Opcode.INT, List.of(I8)),
			Map.entry(Opcode.IRET, List.of(NOTHING)),
			Map.entry(Opcode.RET, List.of(NOTHING, I16)),
			Map.entry(Opcode.RETF, List.of(NOTHING, I16)),
			Map.entry(Opcode.CPUID, List.of(NOTHING)),
			Map.entry(Opcode.HLT, List.of(NOTHING)),
			Map.entry(Opcode.FWAIT, List.of(NOTHING)),
			Map.entry(Opcode.PUSHF, List.of(NOTHING)),
			Map.entry(Opcode.POPF, List.of(NOTHING)),
			Map.entry(Opcode.CMC, List.of(NOTHING)),
			Map.entry(
					Opcode.ADD,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32,
							R8_M8, R32_M32, R64_M64, M8_I8, M8_R8, M16_R16, M16_I8, M32_R32, M64_R64, M32_I8, M32_I32,
							M64_I8, M64_I32)),
			Map.entry(
					Opcode.ADC,
					List.of(
							R8_R8, R32_R32, R64_R64, R8_I8, R16_I16, R32_I8, R32_I32, R64_I8, R8_M8, R32_M32, M8_R8,
							M32_R32, M32_I8)),
			Map.entry(
					Opcode.AND,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32,
							R8_M8, R16_M16, R32_M32, R64_M64, M8_I8, M16_I16, M32_I8, M32_I32, M64_I8, M64_I32, M8_R8,
							M32_R32)),
			Map.entry(
					Opcode.SUB,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R16_I16, R32_I8, R32_I32, R8_I8, R64_I8, R64_I32, R8_M8,
							R32_M32, R64_M64, M8_R8, M16_R16, M32_R32, M64_R64, M8_I8, M16_I8, M32_I8, M64_I8,
							M64_I32)),
			Map.entry(
					Opcode.SBB,
					List.of(
							R8_R8, R32_R32, R64_R64, R8_I8, R16_I16, R32_I8, R32_I32, R64_I8, R8_M8, R32_M32, M8_R8,
							M32_R32, M8_I8, M32_I8)),
			Map.entry(Opcode.SHR, List.of(R8_R8, R16_R8, R32_R8, R64_R8, R8_I8, R16_I8, R32_I8, R64_I8)),
			Map.entry(Opcode.SAR, List.of(R8_R8, R16_R8, R32_R8, R64_R8, R8_I8, R16_I8, R32_I8, R64_I8)),
			Map.entry(Opcode.SHL, List.of(R8_R8, R16_R8, R32_R8, R64_R8, R8_I8, R16_I8, R32_I8, R64_I8)),
			Map.entry(Opcode.SHLD, List.of(R64_R64_R8)),
			Map.entry(Opcode.SHRD, List.of(R64_R64_R8)),
			Map.entry(
					Opcode.IMUL,
					List.of(
							R64,
							R16_R16,
							R32_R32,
							R64_R64,
							R32_M32,
							R64_M64,
							R32_R32_I8,
							R32_R32_I32,
							R64_R64_I32,
							R32_M32_I32,
							R64_R64_I8,
							R64_M64_I32)),
			Map.entry(Opcode.IDIV, List.of(R32, R64)),
			Map.entry(Opcode.DIV, List.of(R8, R16, R32, R64, M8, M16, M32, M64)),
			Map.entry(Opcode.MUL, List.of(R8, R16, R32, R64, M64)),
			Map.entry(
					Opcode.OR,
					List.of(
							R8_R8, R32_R32, R64_R64, R8_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32, R8_M8, R32_M32,
							R64_M64, M8_I8, M16_I8, M16_I16, M32_I8, M32_I32, M64_I8, M64_I32, M8_R8, M16_R16, M32_R32,
							M64_R64)),
			Map.entry(
					Opcode.XOR,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I16, R32_I8, R32_I32, R64_I8, R64_I32, M8_R8,
							M32_R32, R8_M8, R32_M32, R64_M64)),
			Map.entry(Opcode.NOT, List.of(R16, R32, R64)),
			Map.entry(Opcode.NEG, List.of(R32, R64, M32, M64)),
			Map.entry(
					Opcode.TEST,
					List.of(
							R8_R8, R16_R16, R32_R32, R64_R64, R8_I8, R16_I16, R32_I32, R64_I32, M8_I8, M8_R8, M16_I16,
							M16_R16, M32_I32, M32_R32, M64_R64)),
			Map.entry(Opcode.UD2, List.of(NOTHING)),
			Map.entry(Opcode.MOVS, List.of(M8_M8, M16_M16, M32_M32)),
			Map.entry(Opcode.STOS, List.of(M8_R8, M32_R32, M64_R64)),
			Map.entry(Opcode.CMPS, List.of(M8_M8, M32_M32)),
			Map.entry(Opcode.LODS, List.of(R8_M8, R32_M32)),
			Map.entry(Opcode.SCAS, List.of(R8_M8, R32_M32)),
			Map.entry(Opcode.MOVDQA, List.of(RX_RX, RX_M128, M128_RX)),
			Map.entry(Opcode.MOVDQU, List.of(RX_M128)),
			Map.entry(Opcode.MOVAPS, List.of(RX_RX, M128_RX, RX_M128)),
			Map.entry(Opcode.MOVAPD, List.of(RX_RX, M128_RX)),
			Map.entry(Opcode.MOVQ, List.of(RMM_R64, RX_R64, M64_RX, RMM_M64, RX_M64)),
			Map.entry(Opcode.MOVD, List.of(RMM_R32, RX_R32, RX_M32, RMM_M64)),
			Map.entry(Opcode.MOVHPS, List.of(RX_M64, M64_RX)),
			Map.entry(Opcode.MOVHPD, List.of(M64_RX)),
			Map.entry(Opcode.MOVHLPS, List.of(RX_RX)),
			Map.entry(Opcode.PUNPCKLQDQ, List.of(RX_RX)),
			Map.entry(Opcode.PUNPCKLDQ, List.of(RX_RX)),
			Map.entry(Opcode.PUNPCKHQDQ, List.of(RX_RX)),
			Map.entry(Opcode.PUNPCKLWD, List.of(RX_RX)),
			Map.entry(Opcode.PUNPCKHDQ, List.of(RX_RX)),
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
			Map.entry(Opcode.MOVABS, List.of(R64_I64, R8_S64, R32_S64, S64_R8, S64_R32)),
			Map.entry(Opcode.MOVUPS, List.of(RX_M128, M128_RX)),
			Map.entry(Opcode.MOVSD, List.of(RX_M64)),
			Map.entry(Opcode.ENDBR64, List.of(NOTHING)),
			Map.entry(Opcode.INC, List.of(R8, R16, R32, R64, M8, M16, M32, M64)),
			Map.entry(Opcode.DEC, List.of(R8, R32, R64, M8, M16, M32, M64)),
			Map.entry(Opcode.PSHUFD, List.of(RX_RX_I8)),
			Map.entry(Opcode.PSHUFW, List.of(RMM_RMM_I8)),
			Map.entry(Opcode.PSHUFB, List.of(RX_RX)),
			Map.entry(Opcode.SHUFPD, List.of(RX_RX_I8)),
			Map.entry(Opcode.SHUFPS, List.of(RX_RX_I8)),
			Map.entry(Opcode.PXOR, List.of(RMM_RMM, RX_RX, RX_M128)),
			Map.entry(Opcode.POR, List.of(RX_RX, RX_M128)),
			Map.entry(Opcode.PAND, List.of(RX_RX, RX_M128)),
			Map.entry(Opcode.PADDQ, List.of(RX_RX, RX_M128)),
			Map.entry(Opcode.PADDD, List.of(RX_RX)),
			Map.entry(Opcode.PSUBQ, List.of(RX_RX, RX_M128)),
			Map.entry(Opcode.PSUBB, List.of(RX_RX)),
			Map.entry(Opcode.PSUBW, List.of(RX_RX)),
			Map.entry(Opcode.PSUBD, List.of(RX_RX)),
			Map.entry(Opcode.PSLLDQ, List.of(RX_I8)),
			Map.entry(Opcode.PSRLDQ, List.of(RX_I8)),
			Map.entry(Opcode.CVTSI2SD, List.of(RX_R64, RX_R32)),
			Map.entry(Opcode.DIVSD, List.of(RX_RX)),
			Map.entry(Opcode.DIVPD, List.of(RX_RX)),
			Map.entry(Opcode.DIVPS, List.of(RX_RX)),
			Map.entry(Opcode.DIVSS, List.of(RX_RX, RX_M32)),
			Map.entry(Opcode.ADDSD, List.of(RX_RX)),
			Map.entry(Opcode.XORPS, List.of(RX_RX)),
			Map.entry(Opcode.UCOMISD, List.of(RX_M64)),
			Map.entry(Opcode.UCOMISS, List.of(RX_M32)),
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
			Map.entry(Opcode.PCMPEQB, List.of(RMM_RMM, RX_RX, RX_M128)),
			Map.entry(Opcode.PCMPEQW, List.of(RMM_RMM, RX_RX)),
			Map.entry(Opcode.PCMPEQD, List.of(RMM_RMM, RX_RX, RX_M128)),
			Map.entry(Opcode.RDRAND, List.of(R16, R32, R64)),
			Map.entry(Opcode.RDSEED, List.of(R16, R32, R64)),
			Map.entry(Opcode.RDSSPQ, List.of(R64)),
			Map.entry(Opcode.INCSSPQ, List.of(R64)),
			Map.entry(Opcode.LAHF, List.of(NOTHING)),
			Map.entry(Opcode.SAHF, List.of(NOTHING)),
			Map.entry(Opcode.SYSCALL, List.of(NOTHING)),
			Map.entry(Opcode.BSR, List.of(R32_R32, R64_R64, R32_M32, R64_M64)),
			Map.entry(Opcode.BSF, List.of(R32_R32, R64_R64)),
			Map.entry(Opcode.ROR, List.of(R8_I8, R16_I8, R32_I8, R64_I8)),
			Map.entry(Opcode.ROL, List.of(R8_R8, R32_R8, R8_I8, R16_I8, R32_I8, R64_I8)),
			Map.entry(Opcode.RCR, List.of(R8_R8, R8_I8, R32_I8)),
			Map.entry(Opcode.RCL, List.of(R8_R8, R32_R8, R8_I8, R32_I8)),
			Map.entry(Opcode.PMOVMSKB, List.of(R32_RX)),
			Map.entry(Opcode.PMINUB, List.of(RX_RX, RX_M128)),
			Map.entry(Opcode.PMINUD, List.of(RX_M128)),
			Map.entry(Opcode.PMAXUB, List.of(RX_RX)),
			Map.entry(Opcode.PALIGNR, List.of(RX_RX_I8, RX_M128_I8)),
			Map.entry(Opcode.VPXOR, List.of(RX_RX_RX)),
			Map.entry(Opcode.VPXORQ, List.of(RY_RY_M256)),
			Map.entry(Opcode.VPORQ, List.of(RY_RY_RY)),
			Map.entry(Opcode.PEXTRW, List.of(R32_RMM_I8)),
			Map.entry(Opcode.VMOVDQU, List.of(RY_M256, M256_RY)),
			Map.entry(Opcode.VPMINUB, List.of(RY_RY_RY, RY_RY_M256)),
			Map.entry(Opcode.VPMINUD, List.of(RY_RY_RY, RY_RY_M256)),
			Map.entry(Opcode.VPMOVMSKB, List.of(R32_RX, R32_RY)),
			Map.entry(Opcode.VPCMPEQB, List.of(RK_RX_RX, RK_RY_RY, RY_RY_M256, RK_RX_M128, RK_RY_M256)),
			Map.entry(Opcode.VPCMPEQD, List.of(RK_RY_RY, RY_RY_M256, RK_RY_M256)),
			Map.entry(Opcode.VPCMPEQQ, List.of(RX_RX_M128)),
			Map.entry(Opcode.VPCMPNEQB, List.of(RK_RY_RY, RK_RY_M256)),
			Map.entry(Opcode.VZEROALL, List.of(NOTHING)),
			Map.entry(Opcode.VMOVQ, List.of(R64_RX, RX_M64, M64_RX)),
			Map.entry(Opcode.VMOVD, List.of(RX_M32)),
			Map.entry(Opcode.PCMPISTRI, List.of(RX_RX_I8, RX_M128_I8)),
			Map.entry(Opcode.PUNPCKLBW, List.of(RX_RX)),
			Map.entry(Opcode.VPBROADCASTB, List.of(RY_RX, RZ_R32)),
			Map.entry(Opcode.VPBROADCASTD, List.of(RY_RX, RZ_R32)),
			Map.entry(Opcode.SARX, List.of(R32_R32_R32)),
			Map.entry(Opcode.VPOR, List.of(RY_RY_RY)),
			Map.entry(Opcode.VPAND, List.of(RY_RY_RY)),
			Map.entry(Opcode.VPANDN, List.of(RX_RX_RX)),
			Map.entry(Opcode.BZHI, List.of(R32_R32_R32, R64_R64_R64)),
			Map.entry(Opcode.MOVBE, List.of(R32_M32)),
			Map.entry(Opcode.MOVNTDQ, List.of(M128_RX)),
			Map.entry(Opcode.SFENCE, List.of(NOTHING)),
			Map.entry(Opcode.VMOVUPS, List.of(RZ_M512, M512_RZ)),
			Map.entry(Opcode.VMOVDQU8, List.of(RZ_M512, M512_RZ)),
			Map.entry(Opcode.VMOVDQU64, List.of(RZ_M512, M512_RZ)),
			Map.entry(Opcode.VMOVNTDQ, List.of(M256_RY, M512_RZ)),
			Map.entry(Opcode.PCMPGTB, List.of(RX_RX)),
			Map.entry(Opcode.VPCMPGTB, List.of(RX_RX_RX)),
			Map.entry(Opcode.VPSUBB, List.of(RX_RX_RX)),
			Map.entry(Opcode.VPCMPISTRI, List.of(RX_RX_I8)),
			Map.entry(Opcode.VPSLLDQ, List.of(RX_RX_I8)),
			Map.entry(Opcode.VPSRLDQ, List.of(RX_RX_I8)),
			Map.entry(Opcode.VPALIGNR, List.of(RX_RX_M128_I8)),
			Map.entry(Opcode.CLC, List.of(NOTHING)),
			Map.entry(Opcode.STC, List.of(NOTHING)),
			Map.entry(Opcode.CLI, List.of(NOTHING)),
			Map.entry(Opcode.STI, List.of(NOTHING)),
			Map.entry(Opcode.CLD, List.of(NOTHING)),
			Map.entry(Opcode.STD, List.of(NOTHING)),
			Map.entry(Opcode.VPSHUFB, List.of(RX_RX_RX)),
			Map.entry(Opcode.VBROADCASTSS, List.of(RZ_RX)),
			Map.entry(Opcode.VMOVAPS, List.of(M512_RZ)),
			Map.entry(Opcode.KMOVQ, List.of(R64_RK, RK_R64)),
			Map.entry(Opcode.KMOVD, List.of(R32_RK, RK_R32)),
			Map.entry(Opcode.XTEST, List.of(NOTHING)),
			Map.entry(Opcode.VPCMPNEQUB, List.of(RK_RX_M128, RK_RY_M256)),
			Map.entry(Opcode.SLDT, List.of(M16)),
			Map.entry(Opcode.INS, List.of(M8_R16, M32_R16)),
			Map.entry(Opcode.OUTS, List.of(R16_M8, R16_M32)),
			Map.entry(Opcode.ENTER, List.of(I16_I8)),
			Map.entry(Opcode.XLAT, List.of(M8)),
			Map.entry(Opcode.FADD, List.of(M32, M64)),
			Map.entry(Opcode.FIADD, List.of(M16, M32)),
			Map.entry(Opcode.FLD, List.of(M32, M64)),
			Map.entry(Opcode.FILD, List.of(M16, M32)),
			Map.entry(Opcode.LOOPNE, List.of(I8)),
			Map.entry(Opcode.LOOPE, List.of(I8)),
			Map.entry(Opcode.LOOP, List.of(I8)),
			Map.entry(Opcode.JRCXZ, List.of(I8)),
			Map.entry(Opcode.IN, List.of(R8_I8, R32_I8, R8_R16, R32_R16)),
			Map.entry(Opcode.OUT, List.of(I8_R8, I8_R32, R16_R8, R16_R32)),
			Map.entry(Opcode.VPTERNLOGD, List.of(RY_RY_M256_I8, RY_RY_RY_I8)),
			Map.entry(Opcode.VPTESTMB, List.of(RK_RY_RY)),
			Map.entry(Opcode.KORTESTD, List.of(RK_RK)),
			Map.entry(Opcode.KORD, List.of(RK_RK_RK)),
			Map.entry(Opcode.TZCNT, List.of(R32_R32, R64_R64)),
			Map.entry(Opcode.KUNPCKDQ, List.of(RK_RK_RK)),
			Map.entry(Opcode.KUNPCKBW, List.of(RK_RK_RK)),
			Map.entry(Opcode.FXSAVE, List.of(M64)),
			Map.entry(Opcode.FXRSTOR, List.of(M64)),
			Map.entry(Opcode.XSAVE, List.of(M64)),
			Map.entry(Opcode.XRSTOR, List.of(M64)),
			Map.entry(Opcode.XSAVEC, List.of(M64)),
			Map.entry(Opcode.MOVMSKPS, List.of(R32_RX)),
			Map.entry(Opcode.ANDPD, List.of(RX_M128)),
			Map.entry(Opcode.XBEGIN, List.of(I32)),
			Map.entry(Opcode.XEND, List.of(NOTHING)),
			Map.entry(Opcode.STMXCSR, List.of(M32)));

	private InstructionChecker() {}

	private static void error(final String fmt, final Object... args) {
		throw new InvalidInstruction(String.format(fmt, args));
	}

	public static void check(final Instruction inst) {
		final int numOperands = inst.getNumOperands();

		if (numOperands >= 2) {
			checkNoMoreThanOneImmediate(inst);
			checkNoMoreThanOneIndirect(inst);
		}
		if (inst.hasDestinationMask()) {
			checkRegistersXYZ(inst);
			checkNoDuplicateMask(inst);
		} else {
			checkNoDestinationMaskZero(inst);
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

			if (numOperands == 4 && !matches(c.fourthOperandType(), inst.fourthOperand())) {
				continue;
			}

			// we return (meaning that the instruction is correct) as soon as we find a case that matches the current
			// instruction
			return;
		}

		error("'%s' is not a valid instruction.", inst.toString());
	}

	/** An x86 instruction cannot have the 'zero' modifier ("{z}") without a destination mask. */
	private static void checkNoDestinationMaskZero(final Instruction inst) {
		if (!inst.hasDestinationMask() && inst.hasZeroDestinationMask()) {
			error("Zero destination mask without destination mask.");
		}
	}

	/**
	 * This is one of the few general rules of x86 which applies to all instructions. No instruction is allowed to have
	 * more than one immediate value (except ENTER).
	 */
	private static void checkNoMoreThanOneImmediate(final Instruction inst) {
		if (inst.opcode() == Opcode.ENTER) {
			return;
		}

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
		if (inst.hasFourthOperand() && inst.fourthOperand() instanceof Immediate) {
			count++;
		}
		if (count > 1) {
			error("No instruction can have more than 1 immediate value.");
		}
	}

	/**
	 * This is one of the few general rules of x86 which applies to all instructions. No instruction is allowed to have
	 * more than one indirect operand (except MOVS and CMPS).
	 */
	private static void checkNoMoreThanOneIndirect(final Instruction inst) {
		if (inst.opcode() == Opcode.MOVS || inst.opcode() == Opcode.CMPS) {
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
		if (inst.hasFourthOperand() && inst.fourthOperand() instanceof IndirectOperand) {
			count++;
		}
		if (count > 1) {
			error("No instruction can have more than 1 indirect operand.");
		}
	}

	private static boolean isXYZ(final Operand op) {
		return op instanceof RegisterXMM
				|| op instanceof RegisterYMM
				|| op instanceof RegisterZMM
				|| (op instanceof final IndirectOperand io
						&& (io.getPointerSize() == PointerSize.XMMWORD_PTR
								|| io.getPointerSize() == PointerSize.YMMWORD_PTR
								|| io.getPointerSize() == PointerSize.ZMMWORD_PTR));
	}

	/**
	 * When using a mask register on the destination operand, at least one of the operands must be an XMM, YMM or ZMM
	 * register or an indirect operand with XMMWORD, YMMWORD or ZMMWORD pointer size.
	 */
	private static void checkRegistersXYZ(final Instruction inst) {
		if (!inst.hasDestinationMask()) {
			return;
		}

		if (inst.getNumOperands() < 1) {
			error("An instruction with no operands cannot have a destination mask.");
		}

		if (isXYZ(inst.firstOperand())) {
			return;
		}
		if (inst.hasSecondOperand() && isXYZ(inst.secondOperand())) {
			return;
		}
		if (inst.hasThirdOperand() && isXYZ(inst.thirdOperand())) {
			return;
		}
		if (inst.hasFourthOperand() && isXYZ(inst.fourthOperand())) {
			return;
		}
		error(
				"When using a destination mask, at least one operand must be an XMM, YMM or ZMM register or an indirect operand with XMMWORD, YMMWORD or ZMMWORD pointer size.");
	}

	/** When using a destination mask, the mask register cannot be used as an operand. */
	private static void checkNoDuplicateMask(final Instruction inst) {
		if (!inst.hasDestinationMask()) {
			return;
		}
		if (inst.getNumOperands() < 1) {
			error("An instruction with no operands cannot have a destination mask.");
		}

		final MaskRegister m = inst.getDestinationMask();
		if (inst.firstOperand().equals(m)
				|| (inst.hasSecondOperand() && inst.secondOperand().equals(m))
				|| (inst.hasThirdOperand() && inst.thirdOperand().equals(m))
				|| (inst.hasFourthOperand() && inst.fourthOperand().equals(m))) {
			error("The destination mask register cannot be used as an operand in the same instruction.");
		}
	}

	private static boolean matches(final OperandType opt, final Operand op) {
		return switch (opt) {
			case R8 -> op instanceof Register8;
			case R16 -> op instanceof Register16;
			case R32 -> op instanceof final Register32 r && !r.equals(Register32.EIP);
			case R64 -> op instanceof final Register64 r && !r.equals(Register64.RIP);
			case RMM -> op instanceof RegisterMMX;
			case RX -> op instanceof RegisterXMM;
			case RY -> op instanceof RegisterYMM;
			case RZ -> op instanceof RegisterZMM;
			case RK -> op instanceof MaskRegister;
			case RS -> op instanceof SegmentRegister;
			case M8 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.BYTE_PTR;
			case M16 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.WORD_PTR;
			case M32 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.DWORD_PTR;
			case M64 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.QWORD_PTR;
			case M128 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.XMMWORD_PTR;
			case M256 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.YMMWORD_PTR;
			case M512 -> op instanceof final IndirectOperand io && io.getPointerSize() == PointerSize.ZMMWORD_PTR;
			case I8 -> op instanceof final Immediate imm && imm.bits() == 8;
			case I16 -> op instanceof final Immediate imm && imm.bits() == 16;
			case I32 -> op instanceof final Immediate imm && imm.bits() == 32;
			case I64 -> op instanceof final Immediate imm && imm.bits() == 64;
			case S64 ->
				op instanceof final SegmentedAddress sa && sa.immediate().bits() == 64;
		};
	}
}
