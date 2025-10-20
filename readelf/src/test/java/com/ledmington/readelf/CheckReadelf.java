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
package com.ledmington.readelf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public final class CheckReadelf {

	private static final PrintWriter out = System.console() != null
			? System.console().writer()
			: new PrintWriter(System.out, false, StandardCharsets.UTF_8);
	private static final boolean isWindows =
			System.getProperty("os.name").toLowerCase(Locale.US).contains("windows");
	private static final String fatJarPath;

	static {
		try (Stream<Path> s = Files.find(
						Path.of(".", "build").normalize().toAbsolutePath(), 999, (p, bfa) -> bfa.isRegularFile())
				.filter(p -> p.getFileName().toString().startsWith("emu-readelf")
						&& p.getFileName().toString().endsWith(".jar"))) {
			fatJarPath = s.max(Comparator.comparingLong(a -> a.toFile().length()))
					.orElseThrow()
					.normalize()
					.toAbsolutePath()
					.toString();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private CheckReadelf() {}

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

	private static List<String> run(final String... cmd) {
		final Process process;
		final List<String> lines = new ArrayList<>();
		try {
			process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			try (BufferedReader reader =
					new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line = reader.readLine();
				while (line != null) {
					lines.add(line);
					line = reader.readLine();
				}
			}
			final int exitCode = process.waitFor();
			if (exitCode != 0) {
				out.printf(" \u001b[31mERROR\u001b[0m: exit code = %d%n", exitCode);
				out.println(String.join("\n", lines));
				out.println();
			}
		} catch (final IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		return lines;
	}

	private static List<String> runSystemReadelf(final Path p, final boolean wide) {
		final String systemReadelf = "/usr/bin/readelf";
		final String[] cmd = wide
				? new String[] {systemReadelf, "-a", "-W", p.toString()}
				: new String[] {systemReadelf, "-a", p.toString()};
		return run(cmd);
	}

	private static List<String> runCustomReadelf(final Path p, final boolean wide) {
		final String[] cmd = wide
				? new String[] {"java", "-jar", fatJarPath, "-a", "-W", p.toString()}
				: new String[] {"java", "-jar", fatJarPath, "-a", p.toString()};
		return run(cmd);
	}

	private static void checkDiff(final List<String> expected, final List<String> actual) {
		if (expected.equals(actual)) {
			out.println("\u001b[32mOK\u001b[0m");
			return;
		}

		out.println("\u001b[31mERROR\u001b[0m");

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

	private static void test(final Path p, final boolean wide) {
		out.print(p.toString() + (wide ? " (wide)" : "") + " ... ");
		final List<String> outputSystemReadelf = runSystemReadelf(p, wide);
		final List<String> outputCustomReadelf = runCustomReadelf(p, wide);
		checkDiff(outputSystemReadelf, outputCustomReadelf);
	}

	public static void main(final String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(out::flush));

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
							out.printf("File '%s' does not exist, skipping it.", p);
							return false;
						}
						return true;
					})
					.filter(p -> {
						if (!isELF(p)) {
							out.printf("File '%s' is not an ELF, skipping it.", p);
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
			out.printf(" [%d / %d] ", i * 2 + 1, elfFiles.size() * 2);
			test(p, false);
			out.printf(" [%d / %d] ", i * 2 + 2, elfFiles.size() * 2);
			test(p, true);
		}

		out.flush();
		out.close();
		System.exit(0);
	}
}
