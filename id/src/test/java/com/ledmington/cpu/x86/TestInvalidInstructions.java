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

import static com.ledmington.cpu.x86.PointerSize.BYTE_PTR;
import static com.ledmington.cpu.x86.PointerSize.DWORD_PTR;
import static com.ledmington.cpu.x86.PointerSize.QWORD_PTR;
import static com.ledmington.cpu.x86.PointerSize.WORD_PTR;
import static com.ledmington.cpu.x86.Register16.AX;
import static com.ledmington.cpu.x86.Register32.EAX;
import static com.ledmington.cpu.x86.Register64.RAX;
import static com.ledmington.cpu.x86.Register64.RBX;
import static com.ledmington.cpu.x86.Register64.RCX;
import static com.ledmington.cpu.x86.Register8.AH;
import static com.ledmington.cpu.x86.Register8.AL;
import static com.ledmington.cpu.x86.RegisterXMM.XMM0;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.cpu.x86.exc.InvalidInstruction;

final class TestInvalidInstructions {

	private static final Immediate bimm = new Immediate((byte) 0);
	private static final Immediate simm = new Immediate((short) 0);
	private static final Immediate iimm = new Immediate(0);
	private static final Immediate limm = new Immediate(0L);

	private static Stream<Arguments> invalidInstructions() {
		return Stream.of(
				// MOV with 2 indirect operands
				Arguments.of(new Instruction(
						Opcode.MOV,
						IndirectOperand.builder().base(RAX).pointer(QWORD_PTR).build(),
						IndirectOperand.builder().base(RBX).pointer(QWORD_PTR).build())),

				// MOV with immediate as destination operand,
				Arguments.of(new Instruction(Opcode.MOV, limm, RAX)),

				// MOV with 2 immediates
				Arguments.of(new Instruction(Opcode.MOV, limm, new Immediate(1L))),

				// MOV with 0, 1 or 3 operands
				Arguments.of(new Instruction(Opcode.MOV)),
				Arguments.of(new Instruction(Opcode.MOV, RAX)),
				Arguments.of(new Instruction(Opcode.MOV, RAX, RBX, RCX)),

				// MOV with registers of different size
				Arguments.of(new Instruction(Opcode.MOV, AL, RBX)),
				Arguments.of(new Instruction(Opcode.MOV, AX, RBX)),
				Arguments.of(new Instruction(Opcode.MOV, EAX, RBX)),
				Arguments.of(new Instruction(Opcode.MOV, RAX, EAX)),
				Arguments.of(new Instruction(Opcode.MOV, RAX, AX)),
				Arguments.of(new Instruction(Opcode.MOV, RAX, AL)),

				// MOV with register and indirect operand of different size
				Arguments.of(new Instruction(
						Opcode.MOV,
						RAX,
						IndirectOperand.builder().base(RBX).pointer(DWORD_PTR).build())),
				Arguments.of(new Instruction(
						Opcode.MOV,
						RAX,
						IndirectOperand.builder().base(RBX).pointer(WORD_PTR).build())),
				Arguments.of(new Instruction(
						Opcode.MOV,
						RAX,
						IndirectOperand.builder().base(RBX).pointer(BYTE_PTR).build())),
				Arguments.of(new Instruction(
						Opcode.MOV,
						IndirectOperand.builder().base(RBX).pointer(DWORD_PTR).build(),
						RAX)),
				Arguments.of(new Instruction(
						Opcode.MOV,
						IndirectOperand.builder().base(RBX).pointer(WORD_PTR).build(),
						RAX)),
				Arguments.of(new Instruction(
						Opcode.MOV,
						IndirectOperand.builder().base(RBX).pointer(BYTE_PTR).build(),
						RAX)),

				// MOV with register and immediate of different size
				Arguments.of(new Instruction(Opcode.MOV, RAX, simm)),
				Arguments.of(new Instruction(Opcode.MOV, RAX, bimm)),
				Arguments.of(new Instruction(Opcode.MOV, iimm, RAX)),
				Arguments.of(new Instruction(Opcode.MOV, simm, RAX)),
				Arguments.of(new Instruction(Opcode.MOV, bimm, RAX)),

				// MOVSXD with 0, 1 or 3 operands
				Arguments.of(new Instruction(Opcode.MOVSXD)),
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX, RAX, RAX)),
				// MOVSXD with a non-64-bit destination operand
				Arguments.of(new Instruction(Opcode.MOVSXD, AH, EAX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, AX, EAX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, EAX, EAX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, XMM0, EAX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, bimm, EAX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, simm, EAX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, iimm, EAX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, limm, EAX)),
				Arguments.of(new Instruction(
						Opcode.MOVSXD,
						IndirectOperand.builder().base(RAX).pointer(QWORD_PTR).build(),
						EAX)),
				// MOVSXD with a non-32-bit source operand
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX, AH)),
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX, AX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX, RAX)),
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX, XMM0)),
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX, bimm)),
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX, simm)),
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX, iimm)),
				Arguments.of(new Instruction(Opcode.MOVSXD, RAX, limm)),
				Arguments.of(new Instruction(
						Opcode.MOVSXD,
						RAX,
						IndirectOperand.builder().base(RAX).pointer(QWORD_PTR).build())),

				// NOP with 8-bit operands and immediates
				Arguments.of(new Instruction(Opcode.NOP, AH)),
				Arguments.of(new Instruction(
						Opcode.NOP,
						IndirectOperand.builder().base(RAX).pointer(BYTE_PTR).build())),
				Arguments.of(new Instruction(Opcode.NOP, bimm)),
				Arguments.of(new Instruction(Opcode.NOP, simm)),
				Arguments.of(new Instruction(Opcode.NOP, iimm)),
				Arguments.of(new Instruction(Opcode.NOP, limm)),
				Arguments.of(new Instruction(Opcode.NOP, RAX, RAX)),
				Arguments.of(new Instruction(Opcode.NOP, RAX, RAX, RAX)),

				// LEA with 0, 1 or 3 operands
				Arguments.of(new Instruction(Opcode.LEA)),
				Arguments.of(new Instruction(Opcode.LEA, RAX)),
				Arguments.of(new Instruction(Opcode.LEA, RAX, RAX, RAX)),

				// LEA with 8 bit registers
				Arguments.of(new Instruction(
						Opcode.LEA,
						AH,
						IndirectOperand.builder().base(RAX).pointer(QWORD_PTR).build())));
	}

	@ParameterizedTest
	@MethodSource("invalidInstructions")
	void invalid(final Instruction inst) {
		assertThrows(
				InvalidInstruction.class,
				() -> InstructionChecker.check(inst),
				() -> String.format("Expected instruction '%s' to be invalid but it wasn't.", inst.toString()));
	}
}
