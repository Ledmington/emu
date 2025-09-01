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

	CommandLineParser(final List<CommandLineArgument> arguments) {
		this.arguments = Objects.requireNonNull(arguments);
	}

	public Map<String, ParsingResult> parse(final String... commandLine) {
		final Map<String, ParsingResult> result = new HashMap<>();
		for (int i = 0; i < commandLine.length; i++) {
			final String arg = commandLine[i];

			for (int j = 0; j < arguments.size(); j++) {
				if (arguments.get(i) instanceof final BooleanArgument ba) {
					if (arg.equals("--" + ba.longName())) {
						result.put(ba.longName(), new BooleanResult(true));
					} else if (arg.equals("--no-" + ba.longName())) {
						result.put(ba.longName(), new BooleanResult(false));
					}
				}
			}
		}
		return result;
	}
}
