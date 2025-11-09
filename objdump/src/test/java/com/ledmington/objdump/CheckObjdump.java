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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.ledmington.utils.ProcessUtils;
import com.ledmington.utils.TerminalUtils;
import com.ledmington.utils.os.OSUtils;

@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public final class CheckObjdump {

	private static final PrintWriter out = System.console() != null
			? System.console().writer()
			: new PrintWriter(System.out, false, StandardCharsets.UTF_8);
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
		} catch (final IOException e) {
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

	private static String runSystemObjdump(final Path p) {
		final String systemObjdump = "/usr/bin/objdump";
		final String[] cmd = {systemObjdump, "-d", "-Mintel", p.toString()};
		return ProcessUtils.run(cmd);
	}

	private static String runCustomObjdump(final Path p) {
		final String[] cmd = {"java", "-jar", fatJarPath, "-d", p.toString()};
		return ProcessUtils.run(cmd);
	}

	private static void checkDiff(final String expected, final String actual) {
		if (expected.equals(actual)) {
			out.println(TerminalUtils.ANSI_GREEN + "OK" + TerminalUtils.ANSI_RESET);
			return;
		}

		out.println(TerminalUtils.ANSI_RED + "ERROR" + TerminalUtils.ANSI_RESET);

		final Path fileExpected;
		final Path fileActual;
		try {
			fileExpected = Files.createTempFile("output-expected-", ".txt");
			fileActual = Files.createTempFile("output-actual-", ".txt");
			Files.writeString(fileExpected, expected);
			Files.writeString(fileActual, actual);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		out.println(ProcessUtils.run(
				"diff", "--unified=3", "--color=always", fileExpected.toString(), fileActual.toString()));
		out.println();

		try {
			Files.deleteIfExists(fileExpected);
			Files.deleteIfExists(fileActual);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void test(final Path p) {
		out.print(p.toString() + " ... ");
		final String outputSystemObjdump = runSystemObjdump(p);
		final String outputCustomObjdump = runCustomObjdump(p);
		checkDiff(outputSystemObjdump, outputCustomObjdump);
	}

	public static void main(final String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(out::flush));

		if (OSUtils.IS_WINDOWS) {
			out.println("It seems that you are running on a windows machine. This test will be disabled.");
			System.exit(0);
		}

		if (args.length == 0) {
			out.println("No arguments provided.");
			out.flush();
			out.close();
			System.exit(-1);
			return;
		}

		final List<Path> elfFiles = new ArrayList<>();
		for (final String arg : args) {
			final Path p = Path.of(arg).normalize().toAbsolutePath();
			if (!Files.exists(p)) {
				out.printf("File '%s' does not exist, skipping it.%n", p);
				continue;
			}
			if (!isELF(p)) {
				out.printf("File '%s' is not an ELF, skipping it.%n", p);
				continue;
			}
			elfFiles.add(p);
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
