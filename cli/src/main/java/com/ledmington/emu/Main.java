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
package com.ledmington.emu;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ledmington.mem.MemoryInitializer;
import com.ledmington.utils.MiniLogger;

public final class Main {

	private static final MiniLogger logger = MiniLogger.getLogger("emu");
	private static final PrintWriter out = System.console() == null
			? new PrintWriter(System.out, false, StandardCharsets.UTF_8)
			: System.console().writer();

	private static long parseLongHex(final String s) {
		if (s.isEmpty() || s.length() > 16) {
			throw new IllegalArgumentException(String.format("'%s' is an invalid 64-bit hex value.", s));
		}
		long x = 0L;
		for (int i = s.length() - 1; i >= 0; i--) {
			final char c = s.charAt(i);
			final long y =
					switch (c) {
						case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (c - '0');
						case 'a', 'b', 'c', 'd', 'e', 'f' -> (c - 'a');
						case 'A', 'B', 'C', 'D', 'E', 'F' -> (c - 'A');
						default ->
							throw new IllegalArgumentException(
									String.format("'%c' is not a hexadecimal character.", c));
					};
			x = (x << 4) | y;
		}
		return x;
	}

	@SuppressWarnings("PMD.AvoidCatchingThrowable")
	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.WARNING);

		String filename = null;
		String[] innerArgs = null;

		final String shortHelpFlag = "-h";
		final String longHelpFlag = "--help";
		final String shortQuietFlag = "-q";
		final String longQuietFlag = "--quiet";
		final String verboseFlag = "-v";
		final String veryVerboseFlag = "-vv";
		final String memoryInitializerRandomFlag = "--mem-init-random";
		final String memoryInitializerZeroFlag = "--mem-init-zero";
		final String stackSizeFlag = "--stack-size";
		final String baseAddressFlag = "--base-address";
		final String shortVersionFlag = "-V";
		final String longVersionFlag = "--version";

		label:
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];

			switch (arg) {
				case shortHelpFlag, longHelpFlag -> {
					out.println(String.join(
							"\n",
							"",
							" emu - CPU emulator",
							"",
							" Usage: emu [OPTIONS] FILE",
							"",
							" Command line options:",
							"",
							" -h, --help   Shows this help message and exits.",
							" -q, --quiet  Only errors are reported.",
							" -v           Errors, warnings and info messages are reported.",
							" -vv          All messages are reported.",
							"",
							" --mem-init-random  Uninitialized memory has random values (default).",
							" --mem-init-zero    Uninitialized memory contains binary zero.",
							" --stack-size N     Number of bytes to allocate for the stack. Accepts only integers.",
							"                    Can accept different forms like '1KB', '2MiB', '3Gb', '4Tib'.",
							" --base-address X   Memory location where to load the executable file (hexadecimal 64-bits).",
							" FILE               The ELF executable file to emulate.",
							""));
					out.flush();
					System.exit(0);
				}
				case shortQuietFlag, longQuietFlag -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
				case verboseFlag -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.INFO);
				case veryVerboseFlag -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.DEBUG);
				case memoryInitializerRandomFlag -> EmulatorConstants.setMemoryInitializer(MemoryInitializer::random);
				case memoryInitializerZeroFlag -> EmulatorConstants.setMemoryInitializer(MemoryInitializer::zero);
				case stackSizeFlag -> {
					i++;
					if (i >= args.length) {
						throw new IllegalArgumentException(
								String.format("Expected an argument after '%s'", stackSizeFlag));
					}

					String s = args[i];
					if (s.chars().allMatch(Character::isDigit)) {
						EmulatorConstants.setStackSize(Long.parseLong(s));
						continue;
					}

					int k = 0;
					long bytes = 0L;
					while (k < s.length() && Character.isDigit(s.charAt(k))) {
						final long idx = (s.charAt(k) - '0');
						bytes = bytes * 10L + idx;
						k++;
					}

					s = s.substring(k);

					bytes = switch (s) {
						case "B" -> bytes;
						case "KB" -> bytes * 1_000L;
						case "MB" -> bytes * 1_000_000L;
						case "GB" -> bytes * 1_000_000_000L;
						case "TB" -> bytes * 1_000_000_000_000L;
						case "KiB" -> bytes * 1_024L;
						case "MiB" -> bytes * 1_024L * 1_024L;
						case "GiB" -> bytes * 1_024L * 1_024L * 1_024L;
						case "TiB" -> bytes * 1_024L * 1_024L * 1_024L * 1_024L;
						case "b" -> bytes / 8L;
						case "Kb" -> bytes * 1_000L / 8L;
						case "Mb" -> bytes * 1_000_000L / 8L;
						case "Gb" -> bytes * 1_000_000_000L / 8L;
						case "Tb" -> bytes * 1_000_000_000_000L / 8L;
						case "Kib" -> bytes * 1_024L / 8L;
						case "Mib" -> bytes * 1_024L * 1_024L / 8L;
						case "Gib" -> bytes * 1_024L * 1_024L * 1_024L / 8L;
						case "Tib" -> bytes * 1_024L * 1_024L * 1_024L * 1_024L / 8L;
						default ->
							throw new IllegalArgumentException(String.format("Invalid stack size '%s'", args[i]));
					};

					EmulatorConstants.setStackSize(bytes);
				}
				case baseAddressFlag -> {
					i++;
					if (i >= args.length) {
						throw new IllegalArgumentException(
								String.format("Expected an argument after '%s'", baseAddressFlag));
					}

					EmulatorConstants.setBaseAddress(
							args[i].startsWith("0x") ? parseLongHex(args[i].substring(2)) : parseLongHex(args[i]));
				}
				case shortVersionFlag, longVersionFlag -> {
					out.print(String.join("\n", "", " emu - CPU emulator", " v0.1.0", ""));
					out.flush();
					System.exit(0);
				}
				default -> {
					filename = arg;

					// all the next command-line arguments are considered to be related to the
					// executable to be emulated
					innerArgs = Arrays.copyOfRange(args, i + 1, args.length);
					break label;
				}
			}
		}

		if (filename == null) {
			out.println("Expected the name of the file to run.");
			out.flush();
			System.exit(-1);
		}

		logger.info(
				"Executing %s",
				Stream.concat(Stream.of(filename), Arrays.stream(innerArgs))
						.map(s -> "'" + s + "'")
						.collect(Collectors.joining(" ")));

		try {
			Emu.run(filename, innerArgs);
		} catch (final Throwable t) {
			logger.error(t);
			out.flush();
			System.exit(-1);
		}
		out.flush();
	}
}
