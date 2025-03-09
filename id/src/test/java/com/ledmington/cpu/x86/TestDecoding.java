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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.utils.BitUtils;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
final class TestDecoding extends X64Test {

	private static byte[] toByteArray(final List<Byte> b) {
		final byte[] v = new byte[b.size()];
		for (int i = 0; i < b.size(); i++) {
			v[i] = b.get(i);
		}
		return v;
	}

	private static String toString(final byte[] v) {
		final StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (v.length > 0) {
			sb.append(String.format("0x%02x", v[0]));
			for (int i = 1; i < v.length; i++) {
				sb.append(String.format(", 0x%02x", v[i]));
			}
		}
		return sb.append(']').toString();
	}

	private static Stream<Arguments> onlyHex() {
		return instructions().map(arg -> Arguments.of(arg.get()[1]));
	}

	@ParameterizedTest
	@MethodSource("onlyHex")
	void hexToHex(final List<Byte> code) {
		final byte[] expected = toByteArray(code);
		final List<Instruction> instructions = InstructionDecoder.fromHex(expected, expected.length);
		assertEquals(
				1,
				instructions.size(),
				() -> String.format("Expected 1 instruction but %,d were found.", instructions.size()));
		final Instruction inst = instructions.getFirst();
		final byte[] actual = InstructionEncoder.toHex(inst);
		assertArrayEquals(
				expected,
				actual,
				() -> String.format("Expected '%s' but was '%s'", toString(expected), toString(actual)));
		assertFalse(
				inst.isLegacy(),
				() -> String.format(
						"%s is a valid instruction but it is for legacy/compatibility 32-bit mode, not for 64-bit mode.",
						InstructionEncoder.toIntelSyntax(inst)));
	}

	private static Stream<Arguments> onlyIntelSyntax() {
		return instructions().map(arg -> Arguments.of(arg.get()[0]));
	}

	@ParameterizedTest
	@MethodSource("onlyIntelSyntax")
	void stringToString(final String expected) {
		final String actual = InstructionEncoder.toIntelSyntax(InstructionDecoder.fromIntelSyntax(expected));
		assertEquals(expected, actual, () -> String.format("Expected '%s' but was '%s'.", expected, actual));
	}

	@ParameterizedTest
	@MethodSource("instructions")
	void hexToString(final String expected, final List<Byte> code) {
		final List<Instruction> decoded = InstructionDecoder.fromHex(toByteArray(code), code.size());
		assertEquals(
				1, decoded.size(), () -> String.format("Expected 1 instruction but %,d were found.", decoded.size()));
		final String actual = InstructionEncoder.toIntelSyntax(decoded.getFirst());
		assertEquals(expected, actual, () -> String.format("Expected '%s' but was '%s'.", expected, actual));
	}

	@ParameterizedTest
	@MethodSource("instructions")
	void stringToHex(final String asm, final List<Byte> code) {
		final Instruction inst = InstructionDecoder.fromIntelSyntax(asm);
		final byte[] expected = toByteArray(code);
		final byte[] actual = InstructionEncoder.toHex(inst);
		assertArrayEquals(expected, actual, () -> {
			String s = String.format("Expected '%s' but was '%s'.", toString(expected), toString(actual));
			if (expected.length == actual.length) {
				for (int i = 0; i < expected.length; i++) {
					if (expected[i] != actual[i]) {
						s = s
								+ String.format(
										" Elements at index %,d were 0b%s and 0b%s, respectively.",
										i, BitUtils.toBinaryString(expected[i]), BitUtils.toBinaryString(actual[i]));
						break;
					}
				}
			}
			return s;
		});
	}
}
