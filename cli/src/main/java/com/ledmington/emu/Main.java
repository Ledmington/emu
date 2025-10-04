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
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.AvoidLiteralsInIfCondition"})
public final class Main {

	private static final MiniLogger logger = MiniLogger.getLogger("emu");
	private static final PrintWriter out = System.console() == null
			? new PrintWriter(System.out, false, StandardCharsets.UTF_8)
			: System.console().writer();

	private Main() {}

	@SuppressWarnings({"PMD.AvoidCatchingThrowable", "PMD.NcssCount", "PMD.CyclomaticComplexity"})
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
		final String memoryInitializerFlag = "--mem-init";
		final String stackSizeFlag = "--stack-size";
		final String baseAddressFlag = "--base-address";
		final String baseStackAddressFlag = "--base-stack-address";
		final String baseStackValueFlag = "--base-stack-value";
		final String shortVersionFlag = "-V";
		final String longVersionFlag = "--version";
		final String checkMemPermissionsFlag = "--check-mem-perm";
		final String noCheckMemPermissionsFlag = "--no-check-mem-perm";
		final String checkMemInitFlag = "--check-mem-init";
		final String noCheckMemInitFlag = "--no-check-mem-init";
		final String checkInstructionsFlags = "--check-instructions";
		final String noCheckInstructionsFlags = "--no-check-instructions";

		int i = 0;
		while (i < args.length) {
			boolean shouldExit = false;
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
							" General options:",
							" -h, --help     Shows this help message and exits.",
							" -q, --quiet    Only errors are reported.",
							" -v             Errors, warnings and info messages are reported.",
							" -vv            All messages are reported.",
							" -V, --version  Prints the version of the emulator and exits.",
							"",
							" Memory options:",
							" --mem-init MI           What value to initialize memory with: random (default), zero",
							"                           or any hexadecimal 1-byte value (example: 0xfa).",
							" --stack-size N          Number of bytes to allocate for the stack. Accepts only integers.",
							"                           Can accept different forms like '1KB', '2MiB', '3Gb', '4Tib'.",
							"                           Default: " + EmulatorConstants.getStackSize() + " bytes ("
									+ EmulatorConstants.getStackSize() / 1_048_576L + " MiB).",
							" --base-address X        Memory location where to load the executable file as hexadecimal 64-bits.",
							"                           Default: "
									+ String.format("0x%x", EmulatorConstants.getBaseAddress()) + ".",
							" --base-stack-address X  The address of the base of the stack (default: "
									+ String.format("0x%x", EmulatorConstants.getBaseStackAddress()) + ").",
							" --base-stack-value X    The value to put at the base of the stack (default: "
									+ String.format("0x%x", EmulatorConstants.getBaseStackValue()) + ").",
							" --check-mem-perm        Breaks when the program tries to access a memory location",
							"                           with the wrong permissions (default).",
							" --no-check-mem-perm     Disables the above.",
							" --check-mem-init        Breaks when reading uninitialized memory locations (default).",
							" --no-check-mem-init     Disables the above.",
							"",
							" CPU options:",
							" --check-instructions      Checks that disassembled instructions are correct (default).",
							" --no-check-instructions   Disables the above.",
							"",
							" FILE                    The ELF executable file to emulate.",
							""));
					out.flush();
					System.exit(0);
				}
				case shortVersionFlag, longVersionFlag -> {
					out.print(String.join("\n", "", " emu - CPU emulator", " v0.1.0", ""));
					out.flush();
					System.exit(0);
				}
				case shortQuietFlag, longQuietFlag -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
				case verboseFlag -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.INFO);
				case veryVerboseFlag -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.DEBUG);
				case checkMemPermissionsFlag -> EmulatorConstants.shouldBreakOnWrongPermissions(true);
				case noCheckMemPermissionsFlag -> EmulatorConstants.shouldBreakOnWrongPermissions(false);
				case checkMemInitFlag -> EmulatorConstants.shouldBreakWhenReadingUninitializedMemory(true);
				case noCheckMemInitFlag -> EmulatorConstants.shouldBreakWhenReadingUninitializedMemory(false);
				case checkInstructionsFlags -> EmulatorConstants.shouldCheckInstructions(true);
				case noCheckInstructionsFlags -> EmulatorConstants.shouldCheckInstructions(false);
				case memoryInitializerFlag -> {
					i++;
					if (i >= args.length) {
						throw new IllegalArgumentException(
								String.format("Expected an argument after '%s'", memoryInitializerFlag));
					}

					String x = args[i];
					if ("zero".equals(x)) {
						EmulatorConstants.setMemoryInitializer(MemoryInitializer.zero());
					} else if ("random".equals(x)) {
						EmulatorConstants.setMemoryInitializer(MemoryInitializer.random());
					} else {
						if (x.startsWith("0x")) {
							x = x.substring(2);
						}
						final byte v = BitUtils.asByte(Integer.parseInt(x, 16));
						EmulatorConstants.setMemoryInitializer(MemoryInitializer.of(v));
					}
				}
				case stackSizeFlag -> {
					i++;
					if (i >= args.length) {
						throw new IllegalArgumentException(
								String.format("Expected an argument after '%s'", stackSizeFlag));
					}

					final String s = args[i];
					if (s.chars().allMatch(Character::isDigit)) {
						EmulatorConstants.setStackSize(Long.parseLong(s));
						continue;
					}

					final long bytes = parseBytes(s);

					EmulatorConstants.setStackSize(bytes);
				}
				case baseAddressFlag -> {
					i++;
					if (i >= args.length) {
						throw new IllegalArgumentException(
								String.format("Expected an argument after '%s'", baseAddressFlag));
					}

					final long val =
							Long.parseUnsignedLong(args[i].startsWith("0x") ? args[i].substring(2) : args[i], 16);
					EmulatorConstants.setBaseAddress(val);
				}
				case baseStackAddressFlag -> {
					i++;
					if (i >= args.length) {
						throw new IllegalArgumentException(
								String.format("Expected an argument after '%s'", baseStackAddressFlag));
					}

					final long val =
							Long.parseUnsignedLong(args[i].startsWith("0x") ? args[i].substring(2) : args[i], 16);
					EmulatorConstants.setBaseStackAddress(val);
				}
				case baseStackValueFlag -> {
					i++;
					if (i >= args.length) {
						throw new IllegalArgumentException(
								String.format("Expected an argument after '%s'", baseStackValueFlag));
					}

					final long val =
							Long.parseUnsignedLong(args[i].startsWith("0x") ? args[i].substring(2) : args[i], 16);
					EmulatorConstants.setBaseStackValue(val);
				}
				default -> {
					filename = arg;

					// all the next command-line arguments are considered to be related to the
					// executable to be emulated
					innerArgs = Arrays.copyOfRange(args, i + 1, args.length);
					shouldExit = true;
				}
			}
			if (shouldExit) {
				break;
			}
			i++;
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
			final Emu emu = new Emu(Emu.getDefaultExecutionContext());
			emu.loadRunAndUnload(filename, innerArgs);
		} catch (final Throwable t) {
			logger.error(t);
			out.flush();
			System.exit(-1);
		}
		out.flush();
	}

	private static long parseBytes(final String arg) {
		long bytes = 0L;
		int i = 0;
		for (; i < arg.length() && Character.isDigit(arg.charAt(i)); i++) {
			final long idx = arg.charAt(i) - '0';
			bytes = bytes * 10L + idx;
		}

		final String s = arg.substring(i);

		return switch (s) {
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
			default -> throw new IllegalArgumentException(String.format("Invalid stack size '%s'", arg));
		};
	}
}
