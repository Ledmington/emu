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
package com.ledmington.readelf;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.ledmington.utils.ProcessUtils;
import com.ledmington.utils.TerminalUtils;
import com.ledmington.utils.os.OSUtils;

@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public final class CheckReadelf {

	private static final PrintWriter out = System.console() != null
			? System.console().writer()
			: new PrintWriter(System.out, false, StandardCharsets.UTF_8);

	private CheckReadelf() {}

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

	private static void test(final Path p, final boolean wide) {
		out.print(p.toString() + (wide ? " (wide)" : "") + " ... ");
		final String outputSystemReadelf = ReadelfSystemComparison.runSystemReadelf(p, wide);
		final String outputCustomReadelf = ReadelfSystemComparison.runCustomReadelf(p, wide);
		checkDiff(outputSystemReadelf, outputCustomReadelf);
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
			if (!ReadelfSystemComparison.isELF(p)) {
				out.printf("File '%s' is not an ELF, skipping it.%n", p);
				continue;
			}
			elfFiles.add(p);
		}

		final int totalTasks = elfFiles.size() * 2;
		for (int i = 0; i < elfFiles.size(); i++) {
			final Path p = elfFiles.get(i);
			out.printf(" [%d / %d] ", i * 2 + 1, totalTasks);
			test(p, false);
			out.printf(" [%d / %d] ", i * 2 + 2, totalTasks);
			test(p, true);
		}

		out.flush();
		out.close();
		System.exit(0);
	}
}
