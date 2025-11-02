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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.SystemPrintln")
public final class GenerateE2ETestFiles {

	private enum OS {
		LINUX,
		MACOS,
		WINDOWS
	}

	private static final OS os =
			System.getProperty("os.name").toLowerCase(Locale.US).contains("win")
					? OS.WINDOWS
					: System.getProperty("os.name").toLowerCase(Locale.US).contains("mac") ? OS.MACOS : OS.LINUX;
	private static final List<String> SEARCH_DIRECTORIES =
			switch (os) {
				case WINDOWS ->
					List.of(
							"C:\\MinGW\\bin",
							"C:\\TDM-GCC-64\\bin",
							"C:\\Program Files\\LLVM\\bin",
							"C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Community\\VC\\Tools\\MSVC",
							"C:\\Program Files\\Microsoft Visual Studio\\2022\\Community\\VC\\Tools\\MSVC");
				case MACOS -> List.of("/usr/bin", "/usr/local/bin", "/opt/homebrew/bin", "/opt/local/bin");
				case LINUX -> List.of("/usr/bin", "/usr/local/bin", "/bin", "/opt/bin");
			};
	private static final List<String> POSSIBLE_COMPILERS = List.of("gcc", "clang", "cc");
	private static final List<String> POSSIBLE_ASSEMBLERS = List.of("nasm");
	private static final List<String> POSSIBLE_LINKERS = List.of("ld", "lld");

	private GenerateE2ETestFiles() {}

	@SuppressWarnings("PMD.OnlyOneReturn")
	private static String findExecutable(final List<String> searchDirs, final List<String> executableNames) {
		for (final String dir : searchDirs) {
			for (final String exec : executableNames) {
				final File candidate = new File(dir, exec + (os == OS.WINDOWS ? ".exe" : ""));
				if (candidate.exists() && candidate.canExecute()) {
					return candidate.getAbsolutePath();
				}
			}
		}
		return null;
	}

	@SuppressWarnings("PMD.OnlyOneReturn")
	private static String findCCompiler() {
		final String compilerPath = findExecutable(SEARCH_DIRECTORIES, POSSIBLE_COMPILERS);
		if (compilerPath != null) {
			return compilerPath;
		}

		// Fallback: try system PATH
		final String pathEnv = System.getenv("PATH");
		if (pathEnv != null) {
			final List<String> pathDirs = List.of(pathEnv.split(File.pathSeparator));
			return findExecutable(pathDirs, POSSIBLE_COMPILERS);
		}

		return null;
	}

	@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
	private static void run(final String... cmd) {
		System.out.println(Arrays.stream(cmd).map(x -> "'" + x + "'").collect(Collectors.joining(" ")));
		try {
			final Process proc =
					new ProcessBuilder().command(cmd).redirectErrorStream(true).start();
			final StringBuilder output = new StringBuilder();
			try (BufferedReader reader =
					new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
				String line = reader.readLine();
				for (; line != null; line = reader.readLine()) {
					output.append(line).append('\n');
				}
			}
			proc.waitFor();
			if (!output.isEmpty()) {
				System.out.println(output);
			}
		} catch (final IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static String findAssembler() {
		return findExecutable(SEARCH_DIRECTORIES, POSSIBLE_ASSEMBLERS);
	}

	private static String findLinker() {
		return findExecutable(SEARCH_DIRECTORIES, POSSIBLE_LINKERS);
	}

	@SuppressWarnings("PMD.OnlyOneReturn")
	public static void main(final String[] args) {
		if (args.length != 0) {
			System.err.println("This program does not need any command-line argument.");
			System.exit(-1);
			return;
		}

		final String cCompiler = findCCompiler();
		if (cCompiler == null) {
			System.err.printf("Could not find a C compiler. Tried: %s.", String.join(", ", POSSIBLE_COMPILERS));
			System.exit(-1);
			return;
		}
		System.out.printf("C compiler: '%s'%n", cCompiler);

		final String assembler = findAssembler();
		if (assembler == null) {
			System.err.printf("Could not find an assembler. Tried: %s.", String.join(", ", POSSIBLE_ASSEMBLERS));
			System.exit(-1);
			return;
		}
		System.out.printf("Assembler : '%s'%n", assembler);

		final String linker = findLinker();
		if (linker == null) {
			System.err.printf("Could not find a linker. Tried: %s.", String.join(", ", POSSIBLE_LINKERS));
			System.exit(-1);
			return;
		}
		System.out.printf("Linker    : '%s'%n", linker);

		System.out.println();

		final String dir =
				Path.of("src", "test", "resources").normalize().toAbsolutePath().toString();
		final String outputDir = Path.of(dir, "generated").toString();
		final String doNothing = Path.of(dir, "do_nothing.c").toString();
		final String small = Path.of(dir, "small.asm").toString();
		final String smallObj = Path.of(outputDir, "small.o").toString();

		run(
				cCompiler,
				"-static",
				doNothing,
				"-o",
				Path.of(outputDir, "do_nothing.static").toString());
		run(cCompiler, doNothing, "-o", Path.of(outputDir, "do_nothing.dynamic").toString());
		run(assembler, "-felf64", small, "-o", smallObj);
		run(linker, smallObj, "-o", Path.of(outputDir, "small.x").toString());
	}
}
