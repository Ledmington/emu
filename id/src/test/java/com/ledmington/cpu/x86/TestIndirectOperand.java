/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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
import java.util.Arrays;
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
	private static final Register64[] r64WithoutRIP = Arrays.stream(Register64.values())
			.filter(x -> x != Register64.RIP)
			.toList()
			.toArray(new Register64[0]);
	private static final Register64[] r64WithoutRSP = Arrays.stream(Register64.values())
			.filter(x -> x != Register64.RSP)
			.toList()
			.toArray(new Register64[0]);
	private static final Register64[] r64WithoutRSPAndRIP = Arrays.stream(Register64.values())
			.filter(x -> x != Register64.RSP && x != Register64.RIP)
			.toList()
			.toArray(new Register64[0]);
	private static final Register32[] r32 = Arrays.stream(Register32.values())
			// EIP is never valid
			.filter(x -> x != Register32.EIP)
			.toList()
			.toArray(new Register32[0]);
	private static final Register32[] r32WithoutESP = Arrays.stream(Register32.values())
			// EIP is never valid
			.filter(x -> x != Register32.EIP && x != Register32.ESP)
			.toList()
			.toArray(new Register32[0]);

	@SafeVarargs
	private static <X> X getRandomFrom(final X... v) {
		return v[rng.nextInt(0, v.length)];
	}

	private static Register32 getRandomRegister32() {
		return getRandomFrom(r32);
	}

	private static Register64 getRandomRegister64() {
		return getRandomFrom(r64WithoutRIP);
	}

	private static Register64 getRandomBaseOnlyRegister64() {
		// RIP can be used only in [base] and [base+displacement] cases
		return getRandomFrom(r64WithoutRSP);
	}

	private static Register32 getRandomIndexRegister32() {
		return getRandomFrom(r32WithoutESP);
	}

	private static Register64 getRandomIndexRegister64() {
		return getRandomFrom(r64WithoutRSPAndRIP);
	}

	private static void addDisplacement(final Number disp, final IndirectOperandBuilder iob, final StringBuilder sb) {
		if (disp instanceof final Byte b) {
			iob.displacement(b);
			if (b < (byte) 0) {
				sb.append(String.format("-0x%02x", -b));
			} else {
				sb.append(String.format("+0x%02x", b));
			}
		} else {
			iob.displacement(disp.intValue());
			if (disp.intValue() < 0) {
				sb.append(String.format("-0x%08x", -disp.intValue()));
			} else {
				sb.append(String.format("+0x%08x", disp.intValue()));
			}
		}
	}

	// This code generates all relevant possible combinations of indirect operands, for testing.
	private static Stream<Arguments> correctIndirectOperands() {
		final PointerSize[] ptr = PointerSize.values();
		final int[] constant = {1, 2, 4, 8};
		final Number[] displacements = {
			0, (byte) 0, (byte) 0x02, (byte) 0x12, (byte) 0x82, 0x123, 0x12345678, 0x82345678
		};

		final List<Arguments> args = new ArrayList<>();

		// [base]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomRegister32(), getRandomBaseOnlyRegister64()}) {
				args.add(Arguments.of(
						IndirectOperand.builder().pointer(ps).base(r).build(),
						ps.name().replace('_', ' ') + " [" + r.toIntelSyntax() + "]"));
			}
		}

		// [base + displacement]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomRegister32(), getRandomBaseOnlyRegister64()}) {
				for (final Number disp : displacements) {
					final IndirectOperandBuilder iob =
							IndirectOperand.builder().pointer(ps).base(r);
					final StringBuilder sb = new StringBuilder();
					sb.append(ps.name().replace('_', ' ')).append(" [").append(r.toIntelSyntax());
					addDisplacement(disp, iob, sb);
					sb.append(']');
					args.add(Arguments.of(iob.build(), sb.toString()));
				}
			}
		}

		// [index*scale + displacement]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomIndexRegister32(), getRandomIndexRegister64()}) {
				for (final int c : constant) {
					for (final Number disp : displacements) {
						final IndirectOperandBuilder iob =
								IndirectOperand.builder().pointer(ps).index(r).scale(c);
						final StringBuilder sb = new StringBuilder();
						sb.append(ps.name().replace('_', ' '))
								.append(" [")
								.append(r.toIntelSyntax())
								.append('*')
								.append(c);
						addDisplacement(disp, iob, sb);
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
				addDisplacement(disp, iob, sb);
				sb.append(']');
				args.add(Arguments.of(iob.build(), sb.toString()));
			}
		}

		// [base + index*scale]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomIndexRegister32(), getRandomIndexRegister64()}) {
				for (final int c : constant) {
					for (final Register base : new Register[] {getRandomRegister32(), getRandomRegister64()}) {
						if (r.bits() != base.bits()) {
							continue;
						}

						final IndirectOperandBuilder iob = IndirectOperand.builder()
								.pointer(ps)
								.base(base)
								.index(r)
								.scale(c);

						final String s = ps.name().replace('_', ' ') + " ["
								+ base.toIntelSyntax()
								+ '+'
								+ r.toIntelSyntax()
								+ '*'
								+ c
								+ ']';

						args.add(Arguments.of(iob.build(), s));
					}
				}
			}
		}

		// [base + index*scale + displacement]
		for (final PointerSize ps : ptr) {
			for (final Register r : new Register[] {getRandomIndexRegister32(), getRandomIndexRegister64()}) {
				for (final int c : constant) {
					for (final Register base : new Register[] {getRandomRegister32(), getRandomRegister64()}) {
						if (r.bits() != base.bits()) {
							continue;
						}

						for (final Number disp : displacements) {
							final IndirectOperandBuilder iob = IndirectOperand.builder()
									.pointer(ps)
									.base(base)
									.index(r)
									.scale(c);

							final StringBuilder sb = new StringBuilder();

							sb.append(ps.name().replace('_', ' '))
									.append(" [")
									.append(base.toIntelSyntax())
									.append('+')
									.append(r.toIntelSyntax())
									.append('*')
									.append(c);

							addDisplacement(disp, iob, sb);

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
						() -> IndirectOperand.builder().scale(-1),
						() -> IndirectOperand.builder().scale(0),
						() -> IndirectOperand.builder().scale(3),
						() -> IndirectOperand.builder().scale(5),
						() -> IndirectOperand.builder().scale(6),
						() -> IndirectOperand.builder().scale(7),
						() -> IndirectOperand.builder().scale(9),
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
