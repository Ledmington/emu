/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestIndirectOperand {
	static Stream<Arguments> correctIndirectOperands() {
		return Stream.of(
				Arguments.of(IndirectOperand.builder().reg2(Register32.EAX), "[eax]"),
				Arguments.of(IndirectOperand.builder().reg2(Register32.EAX).disp((byte) 0x12), "[eax+0x12]"),
				Arguments.of(IndirectOperand.builder().reg2(Register32.EAX).disp((short) 0x1234), "[eax+0x1234]"),
				Arguments.of(IndirectOperand.builder().reg2(Register32.EAX).disp(0x12345678), "[eax+0x12345678]"),
				Arguments.of(
						IndirectOperand.builder().reg2(Register32.EAX).disp(0x1123456789abcdefL),
						"[eax+0x1123456789abcdef]"),
				Arguments.of(IndirectOperand.builder().reg2(Register32.EAX).disp((byte) 0x82), "[eax-0x7e]"),
				Arguments.of(IndirectOperand.builder().reg2(Register32.EAX).disp((short) 0x8234), "[eax-0x7dcc]"),
				Arguments.of(IndirectOperand.builder().reg2(Register32.EAX).disp(0x82345678), "[eax-0x7dcba988]"),
				Arguments.of(
						IndirectOperand.builder().reg2(Register32.EAX).disp(0x8123456789abcdefL),
						"[eax-0x7edcba9876543211]"),
				Arguments.of(IndirectOperand.builder().disp((byte) 0x12), "[0x12]"),
				Arguments.of(IndirectOperand.builder().disp((short) 0x1234), "[0x1234]"),
				Arguments.of(IndirectOperand.builder().disp(0x12345678), "[0x12345678]"),
				Arguments.of(IndirectOperand.builder().disp(0x1123456789abcdefL), "[0x1123456789abcdef]"),
				Arguments.of(IndirectOperand.builder().reg2(Register32.EBX).constant(2), "[ebx*2]"),
				Arguments.of(
						IndirectOperand.builder()
								.reg1(Register32.EAX)
								.reg2(Register32.EBX)
								.constant(2),
						"[eax+ebx*2]"),
				Arguments.of(
						IndirectOperand.builder()
								.reg1(Register32.EAX)
								.reg2(Register32.EBX)
								.constant(2)
								.disp((byte) 0x12),
						"[eax+ebx*2+0x12]"),
				Arguments.of(
						IndirectOperand.builder()
								.reg1(Register32.EAX)
								.reg2(Register32.EBX)
								.constant(2)
								.disp((short) 0x1234),
						"[eax+ebx*2+0x1234]"),
				Arguments.of(
						IndirectOperand.builder()
								.reg1(Register32.EAX)
								.reg2(Register32.EBX)
								.constant(2)
								.disp(0x12345678),
						"[eax+ebx*2+0x12345678]"),
				Arguments.of(
						IndirectOperand.builder()
								.reg1(Register32.EAX)
								.reg2(Register32.EBX)
								.constant(2)
								.disp(0x1123456789abcdefL),
						"[eax+ebx*2+0x1123456789abcdef]"));
	}

	@ParameterizedTest
	@MethodSource("correctIndirectOperands")
	void correct(final IndirectOperandBuilder iob, final String expected) {
		final IndirectOperand io = iob.build();
		assertEquals(
				expected,
				io.toIntelSyntax(),
				() -> String.format(
						"Expected indirect operand '%s' to be '%s' but wasn't", io.toIntelSyntax(), expected));
	}

	static Stream<Arguments> wrongIndirectOperands() {
		return Stream.<Supplier<IndirectOperandBuilder>>of(
						() -> IndirectOperand.builder().constant(-1),
						() -> IndirectOperand.builder().constant(0),
						() -> IndirectOperand.builder().constant(3),
						() -> IndirectOperand.builder().constant(5),
						() -> IndirectOperand.builder().constant(6),
						() -> IndirectOperand.builder().constant(7),
						() -> IndirectOperand.builder().constant(9),
						() -> IndirectOperand.builder().reg1(Register8.AL),
						() -> IndirectOperand.builder().reg1(Register16.AX),
						() -> IndirectOperand.builder().reg1(RegisterXMM.XMM0),
						() -> IndirectOperand.builder().reg1(Register32.EAX).reg1(Register32.EAX),
						() -> IndirectOperand.builder().reg2(Register8.AL),
						() -> IndirectOperand.builder().reg2(Register16.AX),
						() -> IndirectOperand.builder().reg2(RegisterXMM.XMM0),
						() -> IndirectOperand.builder().reg2(Register32.EAX).reg2(Register32.EAX),
						() -> IndirectOperand.builder().reg1(Register32.EAX).reg2(Register64.RAX),
						() -> IndirectOperand.builder().reg2(Register32.EAX).reg1(Register64.RAX),
						() -> IndirectOperand.builder().reg1(Register64.RAX).reg2(Register32.EAX),
						() -> IndirectOperand.builder().reg2(Register64.RAX).reg1(Register32.EAX))
				.map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("wrongIndirectOperands")
	void correct(final Supplier<IndirectOperandBuilder> task) {
		assertThrows(IllegalArgumentException.class, task::get);
	}
}
