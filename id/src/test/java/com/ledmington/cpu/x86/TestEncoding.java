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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.utils.BitUtils;

public final class TestEncoding extends X64Test {
	@ParameterizedTest
	@MethodSource("instructions")
	void parsing(final String expected, final String hexCode) {
		final String[] parsed = hexCode.split(" ");
		final byte[] code = new byte[parsed.length];
		for (int i = 0; i < parsed.length; i++) {
			code[i] = BitUtils.asByte(Integer.parseInt(parsed[i], 16));
		}

		final InstructionDecoder id = new InstructionDecoderV1(code);
		final List<Instruction> instructions = id.decodeAll(code.length);
		assertNotNull(instructions, "InstructionDecoder returned a null List.");
		final int codeLen = instructions.size();
		assertEquals(1, codeLen, () -> String.format("Expected 1 instruction but %,d were found.", codeLen));
		final Instruction inst = instructions.getFirst();
		final byte[] encoded = InstructionEncoder.encode(inst);
		assertArrayEquals(
				code,
				encoded,
				() -> String.format(
						"Expected '%s' but was '%s'.",
						IntStream.range(0, code.length)
								.mapToObj(i -> String.format("%02x", code[i]))
								.collect(Collectors.joining(" ")),
						IntStream.range(0, encoded.length)
								.mapToObj(i -> String.format("%02x", encoded[i]))
								.collect(Collectors.joining(" "))));
	}
}
