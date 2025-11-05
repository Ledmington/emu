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
package com.ledmington.cpu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ledmington.cpu.x86.Instruction;
import com.ledmington.cpu.x86.NullRegister;
import com.ledmington.cpu.x86.exc.DecodingException;
import com.ledmington.cpu.x86.exc.InvalidInstruction;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/** A very simple fuzzer for x86_64 instructions. */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.UnnecessaryCast", "PMD.AvoidLiteralsInIfCondition"})
public final class X86Fuzzer {

	private static final String GRAY = "\u001b[90m";
	private static final String YELLOW = "\u001b[33m";
	private static final String PURPLE = "\u001b[95m";
	private static final String RESET = "\u001b[0m";

	private static final long SEED =
			RandomGeneratorFactory.getDefault().create(System.nanoTime()).nextLong();
	private static final RandomGenerator RNG =
			RandomGeneratorFactory.getDefault().create(SEED);

	private X86Fuzzer() {}

	private static String toHex(final byte[] b) {
		return IntStream.range(0, b.length)
				.mapToObj(i -> String.format("%02x", b[i]))
				.collect(Collectors.joining(" "));
	}

	private static void print(final int id, final String hex, final String message) {
		System.out.printf(" %s%5d%s | %s%-17s%s : %s%n", GRAY, id, RESET, PURPLE, hex, RESET, message);
	}

	private static boolean containsNullRegister(final Instruction inst) {
		return (inst.hasFirstOperand() && inst.firstOperand() instanceof NullRegister)
				|| (inst.hasSecondOperand() && inst.secondOperand() instanceof NullRegister)
				|| (inst.hasThirdOperand() && inst.thirdOperand() instanceof NullRegister)
				|| (inst.hasFourthOperand() && inst.fourthOperand() instanceof NullRegister);
	}

	private static Instruction generateRandomInstruction() {
		Instruction inst = null;
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1();
		boolean valid;
		do {
			valid = true;
			wb.write(BitUtils.asByte(RNG.nextInt()));
			try {
				final List<Instruction> tmp = InstructionDecoder.fromHex(wb.array(), wb.array().length, false);
				if (tmp.size() != 1) {
					throw new AssertionError(String.format(
							"%s was decoded into %,d instructions instead of one: %s.",
							toHex(wb.array()), tmp.size(), tmp));
				}
				inst = tmp.getFirst();

				try {
					InstructionChecker.check(inst);
				} catch (final InvalidInstruction e) {
					if (!containsNullRegister(inst)) {
						print(-1, toHex(wb.array()), "InvalidInstruction");
						throw e;
					}
				}
			} catch (final ArrayIndexOutOfBoundsException aioobe) {
				// If we receive an AIOOBE, it means the instruction is not yet complete and we need to add more bytes
				valid = false;
			}
		} while (!valid);
		return inst;
	}

	@SuppressWarnings("PMD.EmptyCatchBlock")
	private static Instruction generateRandomCorrectInstruction() {
		while (true) {
			try {
				return generateRandomInstruction();
			} catch (final DecodingException e) {
				// a DecodingException means that the generated instruction is complete but not correct,
				// so we start over
			}
		}
	}

	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.WARNING);

		if (args.length > 0) {
			System.out.println("Ignoring command-line arguments.");
		}

		System.out.printf("Seed: %dL (0x%016xL)%n", SEED, SEED);

		final int attempts = 1000;
		System.out.printf("Generating %,d random instructions.%n", attempts);
		final Set<Instruction> visited = new HashSet<>();

		final long start = System.nanoTime();
		while (visited.size() < attempts) {
			final Instruction inst = generateRandomCorrectInstruction();
			if (visited.contains(inst)) {
				// this instruction was already generated some time in the past, skip it
				continue;
			}
			visited.add(inst);

			final String hex;
			try {
				hex = toHex(InstructionEncoder.toHex(inst, false));
			} catch (final IllegalArgumentException e) {
				print(-1, "??", InstructionEncoder.toIntelSyntax(inst) + " (IllegalArgument)");
				throw e;
			}

			print(
					visited.size(),
					hex,
					(inst == null)
							? (YELLOW + "unknown" + RESET)
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
}
