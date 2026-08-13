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
package com.ledmington.emu;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import com.ledmington.cmdline.CommandLineParser;
import com.ledmington.cmdline.ParsedArguments;
import com.ledmington.mem.MemoryInitializer;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.MiniLogger;

public final class Main {

	private static final MiniLogger logger = MiniLogger.getLogger("emu");
	private static final PrintWriter out = System.console() == null
			? new PrintWriter(System.out, false, StandardCharsets.UTF_8)
			: System.console().writer();

	/** Sentinel default for the numeric/address options below: means "not provided on the command line". */
	private static final String UNSET = "";

	private static final CommandLineParser PARSER = CommandLineParser.builder()
			.programName("emu")
			.description("CPU emulator")
			.group("General options")
			.addBoolean("h", "help", "Shows this help message and exits.", false)
			.addBoolean("q", "quiet", "Only errors are reported.", false)
			.addBoolean("v", null, "Errors, warnings and info messages are reported.", false)
			.addBoolean("vv", null, "All messages are reported.", false)
			.addBoolean("V", "version", "Prints the version of the emulator and exits.", false)
			.group("Memory options")
			.addString(
					null,
					"mem-init",
					"What value to initialize memory with: random (default), zero or any hexadecimal 1-byte value"
							+ " (example: 0xfa).",
					"random")
			.addString(
					null,
					"stack-size",
					"Number of bytes to allocate for the stack. Accepts only integers, or forms like '1KB', '2MiB',"
							+ " '3Gb', '4Tib'. Default: " + EmulatorConstants.getStackSize() + " bytes ("
							+ EmulatorConstants.getStackSize() / 1_048_576L + " MiB).",
					UNSET)
			.addString(
					null,
					"base-address",
					"Memory location where to load the executable file as hexadecimal 64-bits. Default: "
							+ String.format("0x%x", EmulatorConstants.getBaseAddress()) + ".",
					UNSET)
			.addString(
					null,
					"base-stack-address",
					"The address of the base of the stack. Default: "
							+ String.format("0x%x", EmulatorConstants.getBaseStackAddress()) + ".",
					UNSET)
			.addString(
					null,
					"base-stack-value",
					"The value to put at the base of the stack. Default: "
							+ String.format("0x%x", EmulatorConstants.getBaseStackValue()) + ".",
					UNSET)
			.addBoolean(
					null,
					"check-mem-perm",
					"Breaks when the program tries to access a memory location with the wrong permissions"
							+ " (default).",
					false)
			.addBoolean(null, "no-check-mem-perm", "Disables the above.", false)
			.addBoolean(null, "check-mem-init", "Breaks when reading uninitialized memory locations (default).", false)
			.addBoolean(null, "no-check-mem-init", "Disables the above.", false)
			.group("CPU options")
			.addBoolean(
					null, "check-instructions", "Checks that disassembled instructions are correct (default).", false)
			.addBoolean(null, "no-check-instructions", "Disables the above.", false)
			.addPositional("FILE", "The ELF executable file to emulate.")
			.build();

	private Main() {}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.WARNING);

		final ParsedArguments parsed = PARSER.parse(args);

		if (parsed.get("help").asBoolean()) {
			printHelp();
			System.exit(0);
			return;
		}
		if (parsed.get("version").asBoolean()) {
			out.print(String.join("\n", "", " emu - CPU emulator", " v0.1.0", ""));
			out.flush();
			System.exit(0);
			return;
		}

		applyLoggingLevel(parsed);
		applyMemoryChecks(parsed);
		applyMemoryInitializer(parsed.get("mem-init").asString());
		applyNumericOptions(parsed);

		final List<String> positional = parsed.positionalArguments();
		if (positional.isEmpty()) {
			out.println("Expected the name of the file to run.");
			out.flush();
			System.exit(-1);
			return;
		}
		final String filename = positional.get(0);
		final String[] innerArgs = positional.subList(1, positional.size()).toArray(new String[0]);

		logger.info("Executing %s", positional.stream().map(s -> "'" + s + "'").collect(Collectors.joining(" ")));

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

	private static void applyLoggingLevel(final ParsedArguments parsed) {
		// Most-verbose-wins: with independent boolean flags we cannot recover which one was passed last on the
		// command line, so precedence is fixed instead of following command-line order.
		if (parsed.get("vv").asBoolean()) {
			MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.DEBUG);
		} else if (parsed.get("v").asBoolean()) {
			MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.INFO);
		} else if (parsed.get("quiet").asBoolean()) {
			MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
		}
	}

	private static void applyMemoryChecks(final ParsedArguments parsed) {
		// Disabling wins over enabling when both are passed, for the same reason as applyLoggingLevel above.
		if (parsed.get("check-mem-perm").asBoolean()) {
			EmulatorConstants.shouldBreakOnWrongPermissions(true);
		}
		if (parsed.get("no-check-mem-perm").asBoolean()) {
			EmulatorConstants.shouldBreakOnWrongPermissions(false);
		}
		if (parsed.get("check-mem-init").asBoolean()) {
			EmulatorConstants.shouldBreakWhenReadingUninitializedMemory(true);
		}
		if (parsed.get("no-check-mem-init").asBoolean()) {
			EmulatorConstants.shouldBreakWhenReadingUninitializedMemory(false);
		}
		if (parsed.get("check-instructions").asBoolean()) {
			EmulatorConstants.shouldCheckInstructions(true);
		}
		if (parsed.get("no-check-instructions").asBoolean()) {
			EmulatorConstants.shouldCheckInstructions(false);
		}
	}

	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	private static void applyMemoryInitializer(final String memInit) {
		if ("zero".equals(memInit)) {
			EmulatorConstants.setMemoryInitializer(MemoryInitializer.zero());
		} else if ("random".equals(memInit)) {
			EmulatorConstants.setMemoryInitializer(MemoryInitializer.random());
		} else {
			EmulatorConstants.setMemoryInitializer(MemoryInitializer.of(parseHexByte(memInit)));
		}
	}

	private static void applyNumericOptions(final ParsedArguments parsed) {
		final String stackSize = parsed.get("stack-size").asString();
		if (!UNSET.equals(stackSize)) {
			EmulatorConstants.setStackSize(
					stackSize.chars().allMatch(Character::isDigit) ? Long.parseLong(stackSize) : parseBytes(stackSize));
		}

		final String baseAddress = parsed.get("base-address").asString();
		if (!UNSET.equals(baseAddress)) {
			EmulatorConstants.setBaseAddress(parseHexLong(baseAddress));
		}

		final String baseStackAddress = parsed.get("base-stack-address").asString();
		if (!UNSET.equals(baseStackAddress)) {
			EmulatorConstants.setBaseStackAddress(parseHexLong(baseStackAddress));
		}

		final String baseStackValue = parsed.get("base-stack-value").asString();
		if (!UNSET.equals(baseStackValue)) {
			EmulatorConstants.setBaseStackValue(parseHexLong(baseStackValue));
		}
	}

	private static long parseHexLong(final String s) {
		return Long.parseUnsignedLong(s.startsWith("0x") ? s.substring(2) : s, 16);
	}

	private static byte parseHexByte(final String s) {
		return BitUtils.asByte(Integer.parseInt(s.startsWith("0x") ? s.substring(2) : s, 16));
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

	private static void printHelp() {
		out.println(PARSER.helpMessage());
		out.flush();
	}
}
