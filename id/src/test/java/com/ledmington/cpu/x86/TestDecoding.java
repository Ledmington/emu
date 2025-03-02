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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
final class TestDecoding extends X64Test {

	private static byte[] toByteArray(final List<Byte> b) {
		final byte[] v = new byte[b.size()];
		for (int i = 0; i < b.size(); i++) {
			v[i] = b.get(i);
		}
		return v;
	}

	@ParameterizedTest
	@MethodSource("instructions")
	void parsing(final String expected, final List<Byte> code) {
		final List<Instruction> instructions = InstructionDecoder.fromHex(toByteArray(code), code.size());
		assertNotNull(instructions, "InstructionDecoder returned a null List.");
		final int codeLen = instructions.size();
		assertEquals(1, codeLen, () -> String.format("Expected 1 instruction but %,d were found.", codeLen));
		final Instruction inst = instructions.getFirst();
		final String decoded = InstructionEncoder.toIntelSyntax(inst);
		assertEquals(expected, decoded, () -> String.format("Expected '%s' but '%s' was decoded.", expected, decoded));
		assertFalse(
				inst.isLegacy(),
				() -> String.format(
						"%s ('%s') is a valid instruction but it is for legacy/compatibility 32-bit mode, not for 64-bit mode.",
						inst, decoded));
	}
}
