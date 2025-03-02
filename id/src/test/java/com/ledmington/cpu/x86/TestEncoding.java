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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestEncoding extends X64Test {

	private static Stream<Arguments> instructionEncodings() {
		return instructions().map(arg -> Arguments.of(arg.get()[1]));
	}

	private static byte[] toByteArray(final List<Byte> b) {
		final byte[] v = new byte[b.size()];
		for (int i = 0; i < b.size(); i++) {
			v[i] = b.get(i);
		}
		return v;
	}

	@ParameterizedTest
	@MethodSource("instructionEncodings")
	void parsing(final List<Byte> input) {
		final byte[] code = toByteArray(input);
		final List<Instruction> instructions = InstructionDecoder.fromHex(code, code.length);
		assertNotNull(instructions, "InstructionDecoder returned a null List.");
		final int codeLen = instructions.size();
		assertEquals(1, codeLen, () -> String.format("Expected 1 instruction but %,d were found.", codeLen));
		final Instruction inst = instructions.getFirst();
		final byte[] encoded = InstructionEncoder.toHex(inst);
		assertArrayEquals(
				code,
				encoded,
				() -> String.format(
						"Expected '%s' to be encoded as '%s' but was '%s'.",
						InstructionEncoder.toIntelSyntax(inst),
						IntStream.range(0, code.length)
								.mapToObj(i -> String.format("%02x", code[i]))
								.collect(Collectors.joining(" ")),
						IntStream.range(0, encoded.length)
								.mapToObj(i -> String.format("%02x", encoded[i]))
								.collect(Collectors.joining(" "))));
	}
}
