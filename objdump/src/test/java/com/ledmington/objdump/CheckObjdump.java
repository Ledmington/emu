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
package com.ledmington.objdump;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.ledmington.utils.ProcessUtils;
import com.ledmington.utils.TerminalUtils;

@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public final class CheckObjdump {

	private static final PrintWriter out = System.console() != null
			? System.console().writer()
			: new PrintWriter(System.out, false, StandardCharsets.UTF_8);
	private static final boolean isWindows =
			System.getProperty("os.name").toLowerCase(Locale.US).contains("windows");
	private static final String fatJarPath;

	static {
		try (Stream<Path> s = Files.find(
						Path.of(".", "build").normalize().toAbsolutePath(), 999, (p, bfa) -> bfa.isRegularFile())
				.filter(p -> p.getFileName().toString().startsWith("emu-objdump")
						&& p.getFileName().toString().endsWith(".jar"))) {
			fatJarPath = s.max(Comparator.comparingLong(a -> a.toFile().length()))
					.orElseThrow()
					.normalize()
					.toAbsolutePath()
					.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private CheckObjdump() {}

	private static boolean isELF(final Path p) {
		try (InputStream is = Files.newInputStream(p, StandardOpenOption.READ)) {
			final int expectedBytes = 4;
			final byte[] buffer = new byte[expectedBytes];
			final int bytesRead = is.read(buffer);
			return bytesRead == expectedBytes
					&& buffer[0] == (byte) 0x7f
					&& buffer[1] == (byte) 0x45
					&& buffer[2] == (byte) 0x4c
					&& buffer[3] == (byte) 0x46;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<String> runSystemObjdump(final Path p) {
		final String systemObjdump = "/usr/bin/objdump";
		final String[] cmd = {systemObjdump, "-d", "-Mintel", p.toString()};
		return List.of(ProcessUtils.run(cmd).split("\n"));
	}

	private static List<String> runCustomObjdump(final Path p) {
		final String[] cmd = {"java", "-jar", fatJarPath, "-d", p.toString()};
		return List.of(ProcessUtils.run(cmd).split("\n"));
	}

	private static void checkDiff(final List<String> expected, final List<String> actual) {
		if (expected.equals(actual)) {
			out.println(TerminalUtils.ANSI_GREEN + "OK" + TerminalUtils.ANSI_RESET);
			return;
		}

		out.println(TerminalUtils.ANSI_RED + "OK" + TerminalUtils.ANSI_RESET);

		final int minSize = Math.min(expected.size(), actual.size());
		final int maxSize = Math.max(expected.size(), actual.size());
		// 'r' stands for "Reference" and 'a' stands for "Actual"
		final String fmtExpected = " %" + (1 + (int) Math.ceil(Math.log10(maxSize))) + "d |r| %s\u001b[47m \u001b[0m%n";
		final String fmtActual = " %" + (1 + (int) Math.ceil(Math.log10(maxSize))) + "d |a| %s\u001b[47m \u001b[0m%n";

		// start of the different block
		int start = -1;
		for (int i = 0; i < minSize; i++) {
			final String e = expected.get(i);
			final String a = actual.get(i);
			if (e.equals(a)) {
				if (start != -1) {
					out.println("----------");
					for (int j = start; j < i; j++) {
						final String a2 = actual.get(j);
						out.printf(fmtActual, j, a2);
					}
					start = -1;
					out.println("==========");
				}
			} else {
				if (start == -1) {
					start = i;
					out.println("==========");
				}
				out.printf(fmtExpected, i, e);
			}
		}

		if (expected.size() > minSize) {
			for (int i = minSize; i < expected.size(); i++) {
				final String e = expected.get(i);
				out.printf(fmtExpected, i, e);
			}
		} else if (actual.size() > minSize) {
			for (int i = minSize; i < actual.size(); i++) {
				final String a = actual.get(i);
				out.printf(fmtActual, i, a);
			}
		}

		out.println();
	}

	private static void test(final Path p) {
		out.print(p.toString() + " ... ");
		final List<String> outputSystemObjdump = runSystemObjdump(p);
		final List<String> outputCustomObjdump = runCustomObjdump(p);
		checkDiff(outputSystemObjdump, outputCustomObjdump);
	}

	public static void main(final String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			out.println();
			out.flush();
		}));

		if (isWindows) {
			out.println("It seems that you are running on a windows machine. This test will be disabled.");
			System.exit(0);
		}

		final List<Path> elfFiles;
		if (args.length > 0) {
			elfFiles = Arrays.stream(args)
					.map(s -> Path.of(s).normalize().toAbsolutePath())
					.filter(p -> {
						if (!Files.exists(p)) {
							out.printf("File '%s' does not exist, skipping it.%n", p);
							return false;
						}
						return true;
					})
					.filter(p -> {
						if (!isELF(p)) {
							out.printf("File '%s' is not an ELF, skipping it.%n", p);
							return false;
						}
						return true;
					})
					.distinct()
					.sorted()
					.toList();
		} else {
			out.println("No arguments provided.");
			out.flush();
			out.close();
			System.exit(-1);
			return;
		}

		for (int i = 0; i < elfFiles.size(); i++) {
			final Path p = elfFiles.get(i);
			out.printf(" [%d / %d] ", i, elfFiles.size());
			test(p);
		}

		out.flush();
		out.close();
		System.exit(0);
	}
}
