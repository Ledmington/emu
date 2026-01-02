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
package com.ledmington.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/** A collection of common utilities to handle processes. */
public final class ProcessUtils {

	private ProcessUtils() {}

	/**
	 * Runs the given command in a process, without stdin, capturing its stdout and stderr and checking if the exit code
	 * is 0.
	 *
	 * @param cmd The command to be executed.
	 * @return The mixed stdout and stderr of the process.
	 */
	@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
	public static String run(final String... cmd) {
		Objects.requireNonNull(cmd, "Null command.");
		if (cmd.length == 0) {
			throw new IllegalArgumentException("Empty command.");
		}

		final Process process;
		final StringBuilder output = new StringBuilder();
		try {
			process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
			try (BufferedReader reader =
					new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
				String line = reader.readLine();
				while (line != null) {
					if (!output.isEmpty()) {
						output.append('\n');
					}
					output.append(line);
					line = reader.readLine();
				}
			}
			final int exitCode = process.waitFor();
			if (exitCode != 0) {
				throw new RuntimeException(String.format(
						"Process with command %s failed with exit code %,d and output:%n%s%n",
						Arrays.stream(cmd).map(s -> "'" + s + "'").collect(Collectors.joining(" ")), exitCode, output));
			}
		} catch (final IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		return output.toString();
	}
}
