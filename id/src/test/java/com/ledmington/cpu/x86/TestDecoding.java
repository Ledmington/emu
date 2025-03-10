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

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestDecoding extends X64Encodings {

	private static Stream<Arguments> instAndHex() {
		return X64_ENCODINGS.stream().map(x -> Arguments.of(x.instruction(), x.hex()));
	}

	@ParameterizedTest
	@MethodSource("instAndHex")
	void toHex(final Instruction inst, final byte[] expected) {
		assertArrayEquals(expected, InstructionEncoder.toHex(inst));
	}

	@ParameterizedTest
	@MethodSource("instAndHex")
	void fromHex(final Instruction expected, final byte[] hex) {
		final List<Instruction> inst = InstructionDecoder.fromHex(hex, hex.length);
		assertEquals(1, inst.size());
		assertEquals(expected, inst.getFirst());
	}

	private static Stream<Arguments> instAndIntelSyntax() {
		return X64_ENCODINGS.stream().map(x -> Arguments.of(x.instruction(), x.intelSyntax()));
	}

	@ParameterizedTest
	@MethodSource("instAndIntelSyntax")
	void toIntelSyntax(final Instruction inst, final String expected) {
		assertEquals(expected, InstructionEncoder.toIntelSyntax(inst));
	}

	@ParameterizedTest
	@MethodSource("instAndIntelSyntax")
	void fromIntelSyntax(final Instruction expected, final String intelSyntax) {
		assertEquals(expected, InstructionDecoder.fromIntelSyntax(intelSyntax));
	}
}
