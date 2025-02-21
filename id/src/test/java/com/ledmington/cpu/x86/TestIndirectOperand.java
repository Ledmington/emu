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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestIndirectOperand {

	private static Stream<Arguments> correctIndirectOperands() {
		final List<Arguments> args = new ArrayList<>();
		for (final PointerSize ptr : PointerSize.values()) {
			for (final Register indexRegister : new Register[] {Register32.EAX, Register64.RAX}) {
				for (final int constant : new int[] {1, 2, 4, 8}) {
					for (final boolean hasBaseRegister : new boolean[] {false, true}) {
						for (final Number displacement : new Number[] {
							0,
							(byte) 0x12,
							(byte) 0x82,
							(short) 0x1234,
							(short) 0x8234,
							0x12345678,
							0x82345678,
							0x1123456789abcdefL,
							0x8123456789abcdefL
						}) {
							final IndirectOperandBuilder iob = IndirectOperand.builder();
							final StringBuilder sb = new StringBuilder();

							iob.pointer(ptr);
							sb.append(ptr.name().replace('_', ' ')).append(" [");

							if (hasBaseRegister) {
								final Register base = indexRegister.bits() == 32 ? Register32.EBX : Register64.RBX;
								iob.base(base);
								sb.append(base.toIntelSyntax()).append('+');
							}

							iob.index(indexRegister);
							sb.append(indexRegister.toIntelSyntax());

							iob.constant(constant);
							if (constant != 1) {
								sb.append('*').append(constant);
							}

							if (displacement.intValue() != 0 || (hasBaseRegister || constant != 1)) {
								switch (displacement) {
									case Byte b -> {
										iob.displacement(b);
										sb.append(b < 0 ? '-' : '+');
										sb.append(String.format("0x%x", b < 0 ? -b : b));
									}
									case Short s -> {
										iob.displacement(s);
										sb.append(s < 0 ? '-' : '+');
										sb.append(String.format("0x%x", s < 0 ? -s : s));
									}
									case Integer x -> {
										iob.displacement(x);
										sb.append(x < 0 ? '-' : '+');
										sb.append(String.format("0x%x", x < 0 ? -x : x));
									}
									case Long l -> {
										iob.displacement(l);
										sb.append(l < 0L ? '-' : '+');
										sb.append(String.format("0x%x", l < 0L ? -l : l));
									}
									default -> throw new IllegalStateException("Unreachable.");
								}
							}

							sb.append(']');

							args.add(Arguments.of(iob, sb.toString()));
						}
					}
				}
			}
		}
		return args.stream();
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

	private static Stream<Arguments> wrongIndirectOperands() {
		return Stream.<Supplier<IndirectOperandBuilder>>of(
						() -> IndirectOperand.builder().constant(-1),
						() -> IndirectOperand.builder().constant(0),
						() -> IndirectOperand.builder().constant(3),
						() -> IndirectOperand.builder().constant(5),
						() -> IndirectOperand.builder().constant(6),
						() -> IndirectOperand.builder().constant(7),
						() -> IndirectOperand.builder().constant(9),
						() -> IndirectOperand.builder().base(Register8.AL),
						() -> IndirectOperand.builder().base(Register16.AX),
						() -> IndirectOperand.builder().base(RegisterXMM.XMM0),
						() -> IndirectOperand.builder().base(Register32.EAX).base(Register32.EAX),
						() -> IndirectOperand.builder().index(Register8.AL),
						() -> IndirectOperand.builder().index(Register16.AX),
						() -> IndirectOperand.builder().index(RegisterXMM.XMM0),
						() -> IndirectOperand.builder().index(Register32.EAX).index(Register32.EAX),
						() -> IndirectOperand.builder().base(Register32.EAX).index(Register64.RAX),
						() -> IndirectOperand.builder().index(Register32.EAX).base(Register64.RAX),
						() -> IndirectOperand.builder().base(Register64.RAX).index(Register32.EAX),
						() -> IndirectOperand.builder().index(Register64.RAX).base(Register32.EAX))
				.map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("wrongIndirectOperands")
	void correct(final Supplier<IndirectOperandBuilder> task) {
		assertThrows(IllegalArgumentException.class, task::get);
	}
}
