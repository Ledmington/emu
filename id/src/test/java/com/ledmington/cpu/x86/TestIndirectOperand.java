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
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestIndirectOperand {

	private static final RandomGenerator rng =
			RandomGeneratorFactory.getDefault().create(42);
	private static final Register64[] r64 = Register64.values();
	private static final Register32[] r32 = Register32.values();

	private static Register32 getRandomRegister32() {
		return r32[rng.nextInt(0, r32.length)];
	}

	private static Register64 getRandomRegister64() {
		return r64[rng.nextInt(0, r64.length)];
	}

	// This code generates all relevant possible combinations of indirect operands, for testing.
	private static Stream<Arguments> correctIndirectOperands() {
		final PointerSize[] ptr = PointerSize.values();
		final int[] constant = new int[] {1, 2, 4, 8};
		final Number[] displacements =
				new Number[] {0, (byte) 0x02, (byte) 0x12, (byte) 0x82, 0x123, 0x12345678, 0x82345678};

		final List<Arguments> args = new ArrayList<>();

		// [index]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomRegister32(), getRandomRegister64()}) {
				args.add(Arguments.of(
						IndirectOperand.builder().index(r).pointer(ps).build(),
						ps.name().replace('_', ' ') + " [" + r.toIntelSyntax() + "]"));
			}
		}

		// [index + displacement]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomRegister32(), getRandomRegister64()}) {
				for (final Number disp : displacements) {
					final IndirectOperandBuilder iob =
							IndirectOperand.builder().index(r).pointer(ps);
					final StringBuilder sb = new StringBuilder();
					sb.append(ps.name().replace('_', ' ')).append(" [").append(r.toIntelSyntax());

					if (disp.intValue() != 0) {
						if (disp instanceof Byte b) {
							iob.displacement(b);
							if (b < (byte) 0) {
								sb.append(String.format("-0x%x", -b));
							} else {
								sb.append(String.format("+0x%x", b));
							}
						} else {
							iob.displacement(disp.intValue());
							if (disp.intValue() < 0) {
								sb.append(String.format("-0x%x", -disp.intValue()));
							} else {
								sb.append(String.format("+0x%x", disp.intValue()));
							}
						}
					}
					sb.append(']');
					args.add(Arguments.of(iob.build(), sb.toString()));
				}
			}
		}

		// [index*scale]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomRegister32(), getRandomRegister64()}) {
				for (final int c : constant) {
					final StringBuilder sb = new StringBuilder();
					sb.append(ps.name().replace('_', ' ')).append(" [").append(r.toIntelSyntax());
					if (c != 1) {
						sb.append('*').append(c).append("+0x0");
					}
					sb.append(']');
					args.add(Arguments.of(
							IndirectOperand.builder()
									.index(r)
									.constant(c)
									.pointer(ps)
									.build(),
							sb.toString()));
				}
			}
		}

		// [index*scale + displacement]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomRegister32(), getRandomRegister64()}) {
				for (final int c : constant) {
					for (final Number disp : displacements) {
						final IndirectOperandBuilder iob =
								IndirectOperand.builder().index(r).pointer(ps).constant(c);
						final StringBuilder sb = new StringBuilder();
						sb.append(ps.name().replace('_', ' ')).append(" [").append(r.toIntelSyntax());

						if (c != 1) {
							sb.append('*').append(c);
						}

						if (disp.intValue() != 0 || c != 1) {
							if (disp instanceof Byte b) {
								iob.displacement(b);
								if (b < (byte) 0) {
									sb.append(String.format("-0x%x", -b));
								} else {
									sb.append(String.format("+0x%x", b));
								}
							} else {
								iob.displacement(disp.intValue());
								if (disp.intValue() < 0) {
									sb.append(String.format("-0x%x", -disp.intValue()));
								} else {
									sb.append(String.format("+0x%x", disp.intValue()));
								}
							}
						}
						sb.append(']');
						args.add(Arguments.of(iob.build(), sb.toString()));
					}
				}
			}
		}

		// [displacement]
		for (final PointerSize ps : ptr) {
			for (final Number disp : displacements) {
				final IndirectOperandBuilder iob = IndirectOperand.builder().pointer(ps);
				final StringBuilder sb = new StringBuilder();
				sb.append(ps.name().replace('_', ' ')).append(" [");
				if (disp instanceof Byte b) {
					iob.displacement(b);
					if (b < (byte) 0) {
						sb.append(String.format("0x%x", -b));
					} else {
						sb.append(String.format("0x%x", b));
					}
				} else {
					iob.displacement(disp.intValue());
					if (disp.intValue() < 0) {
						sb.append(String.format("0x%x", -disp.intValue()));
					} else {
						sb.append(String.format("0x%x", disp.intValue()));
					}
				}
				sb.append(']');
				args.add(Arguments.of(iob.build(), sb.toString()));
			}
		}

		// [base + index*scale + displacement]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomRegister32(), getRandomRegister64()}) {
				for (final int c : constant) {
					for (final Register base : new Register[] {getRandomRegister32(), getRandomRegister64()}) {
						if (r.bits() != base.bits()) {
							continue;
						}

						for (final Number disp : displacements) {
							final IndirectOperandBuilder iob = IndirectOperand.builder()
									.base(base)
									.index(r)
									.constant(c)
									.pointer(ps);

							final StringBuilder sb = new StringBuilder();

							sb.append(ps.name().replace('_', ' '))
									.append(" [")
									.append(base.toIntelSyntax())
									.append('+')
									.append(r.toIntelSyntax())
									.append('*')
									.append(c);

							if (disp instanceof Byte b) {
								iob.displacement(b);
								if (b < (byte) 0) {
									sb.append(String.format("-0x%x", -b));
								} else {
									sb.append(String.format("+0x%x", b));
								}
							} else {
								iob.displacement(disp.intValue());
								if (disp.intValue() < 0) {
									sb.append(String.format("-0x%x", -disp.intValue()));
								} else {
									sb.append(String.format("+0x%x", disp.intValue()));
								}
							}

							sb.append(']');

							args.add(Arguments.of(iob.build(), sb.toString()));
						}
					}
				}
			}
		}
		return args.stream();
	}

	@ParameterizedTest
	@MethodSource("correctIndirectOperands")
	void correct(final IndirectOperand io, final String expected) {
		final String actual = io.toIntelSyntax();
		assertEquals(expected, actual, () -> String.format("Expected '%s' but was '%s'.", expected, actual));
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
