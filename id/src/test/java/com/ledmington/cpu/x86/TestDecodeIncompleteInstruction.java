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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestDecodeIncompleteInstruction extends X64Encodings {

	private static Stream<Arguments> incompleteInstructions() {
		final Set<List<Byte>> validInstructions = X64_ENCODINGS.stream()
				.map(x -> IntStream.range(0, x.hex().length)
						.mapToObj(i -> x.hex()[i])
						.toList())
				.collect(Collectors.toSet());
		return validInstructions.stream()
				.flatMap(splitted -> {
					if (splitted.size() == 1) {
						return Stream.of();
					}
					// for each instruction, we generate all prefixes that do not represent other
					// valid instructions
					final List<List<Byte>> ll = new ArrayList<>();
					for (int i = 1; i < splitted.size(); i++) {
						final List<Byte> tmp = new ArrayList<>(i);
						for (int j = 0; j < i; j++) {
							tmp.add(splitted.get(j));
						}
						ll.add(tmp);
					}
					return ll.stream();
				})
				.distinct()
				// avoid testing valid instructions assuming they're wrong
				.filter(s -> !validInstructions.contains(s))
				.sorted((a, b) -> {
					final String sa =
							a.stream().map(x -> String.format("%02x", x)).collect(Collectors.joining(" "));
					final String sb =
							b.stream().map(x -> String.format("%02x", x)).collect(Collectors.joining(" "));
					return sa.compareTo(sb);
				})
				.map(Arguments::of);
	}

	private static byte[] toByteArray(final List<Byte> b) {
		final byte[] v = new byte[b.size()];
		for (int i = 0; i < b.size(); i++) {
			v[i] = b.get(i);
		}
		return v;
	}

	@ParameterizedTest
	@MethodSource("incompleteInstructions")
	void incorrectDecoding(final List<Byte> code) {
		// Here we expect an ArrayIndexOutOfBoundsException to be thrown because,
		// like CPUs which break when requesting a new byte and not finding it,
		// the InstructionDecoder will ask for more bytes than are available and
		// the ReadOnlyByteBufferV1 will throw this exception.
		assertThrows(
				ArrayIndexOutOfBoundsException.class, () -> InstructionDecoder.fromHex(toByteArray(code), code.size()));
	}
}
