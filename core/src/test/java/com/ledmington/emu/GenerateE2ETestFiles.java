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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class GenerateE2ETestFiles {

	private enum OS {
		LINUX,
		MACOS,
		WINDOWS
	}

	private static final OS os = System.getProperty("os.name").toLowerCase().contains("win")
			? OS.WINDOWS
			: (System.getProperty("os.name").toLowerCase().contains("mac") ? OS.MACOS : OS.LINUX);
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
	private static String C_COMPILER = null;
	private static String ASSEMBLER = null;
	private static String LINKER = null;

	private GenerateE2ETestFiles() {}

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

	private static String findCCompiler() {
		final List<String> compilers = List.of("gcc", "clang", "cc");

		final String compilerPath = findExecutable(SEARCH_DIRECTORIES, compilers);
		if (compilerPath != null) {
			return compilerPath;
		}

		// Fallback: try system PATH
		final String pathEnv = System.getenv("PATH");
		if (pathEnv != null) {
			final List<String> pathDirs = List.of(pathEnv.split(File.pathSeparator));
			return findExecutable(pathDirs, compilers);
		}

		return null;
	}

	private static void run(final String... cmd) {
		System.out.println(Arrays.stream(cmd).map(x -> "'" + x + "'").collect(Collectors.joining(" ")));
		try {
			final Process proc =
					new ProcessBuilder().command(cmd).redirectErrorStream(true).start();
			final StringBuilder output = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
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
		return findExecutable(SEARCH_DIRECTORIES, List.of("nasm"));
	}

	private static String findLinker() {
		return findExecutable(SEARCH_DIRECTORIES, List.of("ld"));
	}

	public static void main(final String[] args) {
		if (args.length != 0) {
			System.err.println("This program does not need any command-line argument.");
			System.exit(-1);
			return;
		}

		C_COMPILER = findCCompiler();
		if (C_COMPILER == null) {
			System.err.println("Could not find a C compiler.");
			System.exit(-1);
			return;
		}
		System.out.printf("C compiler: '%s'%n", C_COMPILER);

		ASSEMBLER = findAssembler();
		if (ASSEMBLER == null) {
			System.err.println("Could not find an assembler.");
			System.exit(-1);
			return;
		}
		System.out.printf("Assembler: '%s'%n", ASSEMBLER);

		LINKER = findLinker();
		if (LINKER == null) {
			System.err.println("Could not find a linker.");
			System.exit(-1);
			return;
		}
		System.out.printf("Linker: '%s'%n", LINKER);

		System.out.println();

		final String dir =
				Path.of("src", "test", "resources").normalize().toAbsolutePath().toString();
		final String outputDir = Path.of(dir, "generated").toString();
		final String doNothing = Path.of(dir, "do_nothing.c").toString();
		final String small = Path.of(dir, "small.asm").toString();
		final String smallObj = Path.of(outputDir, "small.o").toString();

		run(
				C_COMPILER,
				"-static",
				doNothing,
				"-o",
				Path.of(outputDir, "do_nothing.static").toString());
		run(
				C_COMPILER,
				doNothing,
				"-o",
				Path.of(outputDir, "do_nothing.dynamic").toString());
		run(ASSEMBLER, "-felf64", small, "-o", smallObj);
		run(LINKER, smallObj, "-o", Path.of(outputDir, "small.x").toString());
	}
}
