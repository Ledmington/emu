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
package com.ledmington.cpu;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.Instructions;
import com.ledmington.utils.BitUtils;

final class TestDecoding extends X64Encodings {

	private static String asString(final byte[] v) {
		return IntStream.range(0, v.length)
				.mapToObj(i -> String.format("0x%02x", v[i]))
				.collect(Collectors.joining(" "));
	}

	private static Stream<Arguments> onlyInstructions() {
		return X64_ENCODINGS.stream().map(x -> Arguments.of(x.instruction()));
	}

	@ParameterizedTest
	@MethodSource("onlyInstructions")
	void checkReference(final Instruction inst) {
		assertDoesNotThrow(
				() -> InstructionChecker.check(inst),
				() -> String.format(
						"Expected reference instruction '%s' to be correct but it wasn't.",
						InstructionEncoder.toIntelSyntax(inst)));
	}

	private static Stream<Arguments> instructionAndAllowedEncodings() {
		return X64_ENCODINGS.stream().map(x -> Arguments.of(x.instruction(), x.allowedEncodings()));
	}

	/**
	 * This test checks that the given instruction maps to any of the given encodings. This is less strict than decoding
	 * from binary ({@link TestDecoding#fromHex(Instruction, byte[])}) since we do not care about exact binary encoding.
	 *
	 * @param inst The instruction to encode
	 * @param allowedEncodings The allowed encodings for the given instruction.
	 */
	@ParameterizedTest
	@MethodSource("instructionAndAllowedEncodings")
	void toHex(final Instruction inst, final Set<byte[]> allowedEncodings) {
		final byte[] actual = InstructionEncoder.toHex(inst, true);
		final boolean matchFound = allowedEncodings.stream().anyMatch(allowed -> Arrays.equals(allowed, actual));
		assertTrue(matchFound, () -> {
			final String allowedStr =
					allowedEncodings.stream().map(TestDecoding::asString).collect(Collectors.joining(", "));
			String s = String.format(
					"Expected '%s' to be encoded as one of [%s] but was '%s'.",
					inst.toString(), allowedStr, asString(actual));
			final byte[] closestMatch = allowedEncodings.stream()
					.filter(allowed -> allowed.length == actual.length)
					.findFirst()
					.orElse(null);
			if (closestMatch != null) {
				int i = 0;
				for (; i < closestMatch.length; i++) {
					if (closestMatch[i] != actual[i]) {
						break;
					}
				}
				s += String.format(
						" First different byte (vs. same-length encoding) is at index %,d: expected 0b%s but was 0b%s.",
						i, BitUtils.toBinaryString(closestMatch[i]), BitUtils.toBinaryString(actual[i]));
			} else {
				final String allowedLengths = allowedEncodings.stream()
						.map(allowed -> String.valueOf(allowed.length))
						.collect(Collectors.joining(", "));
				s += String.format(
						" Wrong length: allowed lengths are [%s] bytes but were %,d.", allowedLengths, actual.length);
			}
			return s;
		});
	}

	private static Stream<Arguments> instructionAndHex() {
		return X64_ENCODINGS.stream()
				.flatMap(x -> x.allowedEncodings().stream().map(hex -> Arguments.of(x.instruction(), hex)));
	}

	@ParameterizedTest
	@MethodSource("instructionAndHex")
	void fromHex(final Instruction expected, final byte[] hex) {
		final List<Instruction> inst = InstructionDecoder.fromHex(hex, hex.length, true);
		assertEquals(
				1,
				inst.size(),
				() -> String.format(
						"Expected only one instruction to be decoded but there were %,d: %s.", inst.size(), inst));
		assertEquals(
				expected,
				inst.getFirst(),
				() -> String.format(
						"Expected '%s' to be decoded into '%s' but was '%s'.",
						asString(hex), expected, inst.getFirst()));
	}

	private static Stream<Arguments> onlyHex() {
		return X64_ENCODINGS.stream().flatMap(x -> x.allowedEncodings().stream().map(Arguments::of));
	}

	@ParameterizedTest
	@MethodSource("onlyHex")
	void checkFromHex(final byte[] hex) {
		final List<Instruction> inst = InstructionDecoder.fromHex(hex, hex.length, true);
		assertEquals(
				1,
				inst.size(),
				() -> String.format(
						"Expected only one instruction to be decoded but there were %,d: %s.", inst.size(), inst));
		assertDoesNotThrow(
				() -> InstructionChecker.check(inst.getFirst()),
				() -> String.format(
						"Expected instruction '%s' to be valid but it wasn't.",
						inst.getFirst().toString()));
	}

	private static Stream<Arguments> instAndIntelSyntax() {
		return X64_ENCODINGS.stream().map(x -> Arguments.of(x.instruction(), x.intelSyntax()));
	}

	@ParameterizedTest
	@MethodSource("instAndIntelSyntax")
	void toIntelSyntax(final Instruction inst, final String expected) {
		final String actual = InstructionEncoder.toIntelSyntax(inst);
		assertEquals(
				expected,
				actual,
				() -> String.format(
						"Expected '%s' to be encoded as '%s' but was '%s'.", inst.toString(), expected, actual));
	}

	@ParameterizedTest
	@MethodSource("instAndIntelSyntax")
	void fromIntelSyntax(final Instruction expected, final String intelSyntax) {
		final Instruction actual = InstructionDecoder.fromIntelSyntax(intelSyntax);
		assertTrue(
				Instructions.equals(expected, actual),
				() -> String.format(
						"Expected '%s' to be decoded into '%s' but was '%s'.", intelSyntax, expected, actual));
	}

	private static Stream<Arguments> onlyIntelSyntax() {
		return X64_ENCODINGS.stream().map(x -> Arguments.of(x.intelSyntax()));
	}

	@ParameterizedTest
	@MethodSource("onlyIntelSyntax")
	void checkFromIntelSyntax(final String intelSyntax) {
		final Instruction inst = InstructionDecoder.fromIntelSyntax(intelSyntax);
		assertDoesNotThrow(
				() -> InstructionChecker.check(inst),
				() -> String.format("Expected '%s' to be valid but it wasn't.", intelSyntax));
	}
}
