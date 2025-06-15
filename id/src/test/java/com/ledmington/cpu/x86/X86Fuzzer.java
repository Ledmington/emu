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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ledmington.cpu.x86.exc.DecodingException;
import com.ledmington.cpu.x86.exc.InvalidInstruction;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/** A very simple fuzzer for x86_64 instructions. */
public final class X86Fuzzer {

	private X86Fuzzer() {}

	private static String toHex(final WriteOnlyByteBuffer wb) {
		final byte[] v = wb.array();
		return IntStream.range(0, v.length)
				.mapToObj(i -> String.format("%02x", v[i]))
				.collect(Collectors.joining(" "));
	}

	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.WARNING);

		if (args.length > 0) {
			System.out.println("Ignoring command-line arguments.");
		}

		final long seed =
				RandomGeneratorFactory.getDefault().create(System.nanoTime()).nextLong();
		final RandomGenerator rng = RandomGeneratorFactory.getDefault().create(seed);
		System.out.printf("Seed: %d%n", seed);

		final int attempts = 1000;
		System.out.printf("Generating %,d random instructions.%n", attempts);
		final Set<String> visited = new HashSet<>();

		final long start = System.nanoTime();
		while (visited.size() < attempts) {
			Instruction inst = null;
			int instructionLength = 0;
			final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1();
			boolean valid;
			do {
				valid = true;
				wb.write(BitUtils.asByte(rng.nextInt()));
				instructionLength++;
				try {
					final List<Instruction> tmp = InstructionDecoder.fromHex(wb.array(), instructionLength);
					if (tmp.size() != 1) {
						throw new AssertionError(String.format(
								"%s was decoded into %,d instructions instead of one: %s.",
								toHex(wb), tmp.size(), tmp));
					}
					inst = tmp.getFirst();
				} catch (final InvalidInstruction ii) {
					System.out.printf(" %5d | %s : %s%n", visited.size(), toHex(wb), ii.getMessage());
					System.exit(-1);
					return;
				} catch (final ArrayIndexOutOfBoundsException aioobe) {
					// If we receive an AIOOBE, it means the instruction is not yet complete and we need to add more
					// bytes
					valid = false;
				} catch (final DecodingException ignored) {
				}
			} while (!valid);

			final String hex = toHex(wb);
			if (visited.contains(hex)) {
				// this instruction was already generated some time in the past, skip it
				continue;
			}
			visited.add(hex);

			System.out.printf(
					" %5d | %-11s : %s%n",
					visited.size(),
					hex,
					(inst == null)
							? "unknown"
							: InstructionEncoder.toIntelSyntax(inst, !containsNullRegister(inst), 0, false));
		}
		final long end = System.nanoTime();

		System.out.printf("Checked %,d random unique x86_64 instructions in %,d ns.%n", attempts, end - start);
		System.out.printf(
				"Approximate throughput: %.6f inst/sec%n",
				(double) attempts / ((double) (end - start) / 1_000_000_000.0));
		System.out.println();

		System.exit(0);
	}

	private static boolean containsNullRegister(final Instruction inst) {
		return (inst.hasFirstOperand() && inst.firstOperand() instanceof NullRegister)
				|| (inst.hasSecondOperand() && inst.secondOperand() instanceof NullRegister)
				|| (inst.hasThirdOperand() && inst.thirdOperand() instanceof NullRegister)
				|| (inst.hasFourthOperand() && inst.fourthOperand() instanceof NullRegister);
	}
}
