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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.utils.BitUtils;

final class TestIncompleteInstruction extends X86Test {

	static Stream<Arguments> incompleteInstructions() {
		final Set<String> validInstructions =
				instructions().map(arg -> ((String) arg.get()[1]).strip()).collect(Collectors.toSet());
		return validInstructions.stream()
				.flatMap(hexCode -> {
					final String[] splitted = hexCode.split(" ");
					if (splitted.length == 1) { // NOPMD
						return Stream.of();
					}
					// for each instruction, we generate all prefixes that do not represent other
					// valid instructions
					final List<String> ll = new ArrayList<>();
					for (int i = 0; i < splitted.length; i++) {
						ll.add(String.join(" ", Arrays.copyOfRange(splitted, 0, i + 1)));
					}
					return ll.stream();
				})
				.distinct()
				// avoid testing valid instructions assuming they're wrong
				.filter(s -> !validInstructions.contains(s))
				.sorted()
				.map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("incompleteInstructions")
	void incorrectDecoding(final String hexCode) {
		final String[] parsed = hexCode.split(" ");
		final byte[] code = new byte[parsed.length];
		for (int i = 0; i < parsed.length; i++) {
			code[i] = BitUtils.asByte(Integer.parseInt(parsed[i], 16));
		}

		final InstructionDecoder id = new InstructionDecoderV1(code);

		// Here we expect an ArrayIndexOutOfBoundsException to be thrown because,
		// like CPUs which break when requesting a new byte and not finding it,
		// the InstructionDecoder will ask for more bytes than are available and
		// the ReadOnlyByteBufferV1 will throw this exception.
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> id.decodeAll(code.length));
	}
}
