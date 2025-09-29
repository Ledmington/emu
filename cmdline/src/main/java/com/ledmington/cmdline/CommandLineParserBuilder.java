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

import java.util.ArrayList;
import java.util.List;

public final class CommandLineParserBuilder {

	private final List<CommandLineArgument> arguments = new ArrayList<>();

	public CommandLineParserBuilder() {}

	public CommandLineParserBuilder addBoolean(
			final String shortName, final String longName, final String description, final boolean defaultValue) {
		arguments.add(new BooleanArgument(shortName, longName, description, defaultValue));
		return this;
	}

	public CommandLineParserBuilder addString(
			final String shortName, final String longName, final String description, final String defaultValue) {
		arguments.add(new StringArgument(shortName, longName, description, defaultValue));
		return this;
	}

	public CommandLineParser build() {
		return new CommandLineParser(arguments);
	}
}
