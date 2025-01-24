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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.cpu.x86.exc.InvalidInstruction;

final class TestInvalidInstructions {

	private static Stream<Arguments> invalidInstructions() {
		return Stream.of(
				// MOV with 2 indirect operands
				Arguments.of(Opcode.MOV, new Operand[] {
					IndirectOperand.builder()
							.index(Register64.RAX)
							.pointer(PointerSize.QWORD_PTR)
							.build(),
					IndirectOperand.builder()
							.index(Register64.RBX)
							.pointer(PointerSize.QWORD_PTR)
							.build()
				}),

				// MOV with immediate as destination operand,
				Arguments.of(Opcode.MOV, new Operand[] {new Immediate(0L), Register64.RAX}),

				// MOV with 2 immediates
				Arguments.of(Opcode.MOV, new Operand[] {new Immediate(0L), new Immediate(1L)}),

				// MOV with three operands
				Arguments.of(Opcode.MOV, new Operand[] {Register64.RAX, Register64.RBX, Register64.RCX}),

				// MOV with registers of different size
				Arguments.of(Opcode.MOV, new Operand[] {Register8.AL, Register64.RBX}),
				Arguments.of(Opcode.MOV, new Operand[] {Register16.AX, Register64.RBX}),
				Arguments.of(Opcode.MOV, new Operand[] {Register32.EAX, Register64.RBX}),
				Arguments.of(Opcode.MOV, new Operand[] {Register64.RAX, Register32.EAX}),
				Arguments.of(Opcode.MOV, new Operand[] {Register64.RAX, Register16.AX}),
				Arguments.of(Opcode.MOV, new Operand[] {Register64.RAX, Register8.AL}),

				// MOV with register and indirect operand of different size
				Arguments.of(Opcode.MOV, new Operand[] {
					Register64.RAX,
					IndirectOperand.builder()
							.index(Register64.RBX)
							.pointer(PointerSize.DWORD_PTR)
							.build()
				}),
				Arguments.of(Opcode.MOV, new Operand[] {
					Register64.RAX,
					IndirectOperand.builder()
							.index(Register64.RBX)
							.pointer(PointerSize.WORD_PTR)
							.build()
				}),
				Arguments.of(Opcode.MOV, new Operand[] {
					Register64.RAX,
					IndirectOperand.builder()
							.index(Register64.RBX)
							.pointer(PointerSize.BYTE_PTR)
							.build()
				}),
				Arguments.of(Opcode.MOV, new Operand[] {
					IndirectOperand.builder()
							.index(Register64.RBX)
							.pointer(PointerSize.DWORD_PTR)
							.build(),
					Register64.RAX
				}),
				Arguments.of(Opcode.MOV, new Operand[] {
					IndirectOperand.builder()
							.index(Register64.RBX)
							.pointer(PointerSize.WORD_PTR)
							.build(),
					Register64.RAX
				}),
				Arguments.of(Opcode.MOV, new Operand[] {
					IndirectOperand.builder()
							.index(Register64.RBX)
							.pointer(PointerSize.BYTE_PTR)
							.build(),
					Register64.RAX
				}),

				// MOV with register and immediate of different size
				Arguments.of(Opcode.MOV, new Operand[] {Register64.RAX, new Immediate((short) 0)}),
				Arguments.of(Opcode.MOV, new Operand[] {Register64.RAX, new Immediate((byte) 0)}),
				Arguments.of(Opcode.MOV, new Operand[] {new Immediate(0), Register64.RAX}),
				Arguments.of(Opcode.MOV, new Operand[] {new Immediate((short) 0), Register64.RAX}),
				Arguments.of(Opcode.MOV, new Operand[] {new Immediate((byte) 0), Register64.RAX}));
	}

	@ParameterizedTest
	@MethodSource("invalidInstructions")
	void invalid(final Opcode opcode, final Operand... operands) {
		assertTrue(operands.length <= 3);

		switch (operands.length) {
			case 0 -> assertThrows(InvalidInstruction.class, () -> new Instruction(opcode));
			case 1 -> assertThrows(InvalidInstruction.class, () -> new Instruction(opcode, operands[0]));
			case 2 -> assertThrows(InvalidInstruction.class, () -> new Instruction(opcode, operands[0], operands[1]));
			case 3 -> assertThrows(
					InvalidInstruction.class, () -> new Instruction(opcode, operands[0], operands[1], operands[2]));
		}
	}
}
