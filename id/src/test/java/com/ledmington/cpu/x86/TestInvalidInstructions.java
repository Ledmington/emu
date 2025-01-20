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

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.cpu.x86.exc.InvalidInstruction;

final class TestInvalidInstructions {

	private static Stream<Arguments> invalidInstructions() {
		return Stream.<Supplier<Instruction>>of(
						// MOV with 2 indirect operands
						() -> new Instruction(
								Opcode.MOV,
								IndirectOperand.builder().reg2(Register64.RAX).build(),
								IndirectOperand.builder().reg2(Register64.RBX).build()),

						// MOV with immediate as destination operand,
						() -> new Instruction(Opcode.MOV, new Immediate(0L), Register64.RAX),

						// MOV with 2 immediates
						() -> new Instruction(Opcode.MOV, new Immediate(0L), new Immediate(1L)),

						// MOV with three operands
						() -> new Instruction(Opcode.MOV, Register64.RAX, Register64.RBX, Register64.RCX),

						// MOV with registers of different size
						() -> new Instruction(Opcode.MOV, Register8.AL, Register64.RBX),
						() -> new Instruction(Opcode.MOV, Register16.AX, Register64.RBX),
						() -> new Instruction(Opcode.MOV, Register32.EAX, Register64.RBX),
						() -> new Instruction(Opcode.MOV, Register64.RAX, Register32.EAX),
						() -> new Instruction(Opcode.MOV, Register64.RAX, Register16.AX),
						() -> new Instruction(Opcode.MOV, Register64.RAX, Register8.AL),

						// MOV with register and indirect operand of different size
						() -> new Instruction(
								Opcode.MOV,
								Register64.RAX,
								IndirectOperand.builder()
										.reg2(Register64.RBX)
										.pointer(PointerSize.DWORD_PTR)
										.build()),
						() -> new Instruction(
								Opcode.MOV,
								Register64.RAX,
								IndirectOperand.builder()
										.reg2(Register64.RBX)
										.pointer(PointerSize.WORD_PTR)
										.build()),
						() -> new Instruction(
								Opcode.MOV,
								Register64.RAX,
								IndirectOperand.builder()
										.reg2(Register64.RBX)
										.pointer(PointerSize.BYTE_PTR)
										.build()),
						() -> new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.reg2(Register64.RBX)
										.pointer(PointerSize.DWORD_PTR)
										.build(),
								Register64.RAX),
						() -> new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.reg2(Register64.RBX)
										.pointer(PointerSize.WORD_PTR)
										.build(),
								Register64.RAX),
						() -> new Instruction(
								Opcode.MOV,
								IndirectOperand.builder()
										.reg2(Register64.RBX)
										.pointer(PointerSize.BYTE_PTR)
										.build(),
								Register64.RAX),

						// MOV with register and immediate of different size
						() -> new Instruction(Opcode.MOV, Register64.RAX, new Immediate(0)),
						() -> new Instruction(Opcode.MOV, Register64.RAX, new Immediate((short) 0)),
						() -> new Instruction(Opcode.MOV, Register64.RAX, new Immediate((byte) 0)),
						() -> new Instruction(Opcode.MOV, new Immediate(0), Register64.RAX),
						() -> new Instruction(Opcode.MOV, new Immediate((short) 0), Register64.RAX),
						() -> new Instruction(Opcode.MOV, new Immediate((byte) 0), Register64.RAX))
				.map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("invalidInstructions")
	void invalid(final Supplier<Instruction> sup) {
		assertThrows(InvalidInstruction.class, sup::get);
	}
}
