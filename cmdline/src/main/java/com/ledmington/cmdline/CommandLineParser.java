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
package com.ledmington.cmdline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CommandLineParser {

	private final List<CommandLineArgument> arguments;

	public static CommandLineParserBuilder builder() {
		return new CommandLineParserBuilder();
	}

	/* default */ CommandLineParser(final List<CommandLineArgument> arguments) {
		this.arguments = Objects.requireNonNull(arguments);
	}

	@SuppressWarnings({
		"PMD.UseConcurrentHashMap",
		"PMD.AvoidInstantiatingObjectsInLoops",
		"PMD.AssignmentInOperand",
		"PMD.AvoidReassigningLoopVariables"
	})
	public Map<String, ParsingResult> parse(final String... commandLine) {
		final Map<String, ParsingResult> result = new HashMap<>();

		// Load default values
		for (final CommandLineArgument cla : arguments) {
			switch (cla) {
				case BooleanArgument ba -> {
					if (ba.hasShortName()) {
						result.put(ba.shortName(), new BooleanResult(ba.defaultValue()));
					}
					if (ba.hasLongName()) {
						result.put(ba.longName(), new BooleanResult(ba.defaultValue()));
					}
				}
				case StringArgument sa -> {
					if (sa.hasShortName()) {
						result.put(sa.shortName(), new StringResult(sa.defaultValue()));
					}
					if (sa.hasLongName()) {
						result.put(sa.longName(), new StringResult(sa.defaultValue()));
					}
				}
				default ->
					throw new IllegalArgumentException(String.format("Unknown command line argument: '%s'", cla));
			}
		}

		for (int i = 0; i < commandLine.length; i++) {
			final String arg = commandLine[i];
			for (final CommandLineArgument cla : arguments) {
				switch (cla) {
					case BooleanArgument ba -> {
						if (ba.hasShortName() && arg.equals("-" + ba.shortName())) {
							result.put(ba.shortName(), new BooleanResult(!ba.defaultValue()));
						}
						if (ba.hasLongName() && arg.equals("--" + ba.longName())) {
							result.put(ba.longName(), new BooleanResult(!ba.defaultValue()));
						}
					}
					case StringArgument sa -> {
						final String argumentName;
						final String argumentValue;
						if (arg.contains("=")) {
							final int idx = arg.indexOf('=');
							argumentName = arg.substring(0, idx);
							argumentValue = arg.substring(idx + 1);
						} else {
							argumentName = arg;
							argumentValue = commandLine[++i];
						}
						if (sa.hasShortName() && argumentName.equals("-" + sa.shortName())) {
							result.put(sa.shortName(), new StringResult(argumentValue));
						}
						if (sa.hasLongName() && argumentName.equals("--" + sa.longName())) {
							result.put(sa.longName(), new StringResult(argumentValue));
						}
					}
					default ->
						throw new IllegalArgumentException(String.format("Unknown command line argument: '%s'", cla));
				}
			}
		}

		return result;
	}
}
