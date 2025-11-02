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

/** A builder for {@link CommandLineParser} to ease adding arguments to be parsed. */
public final class CommandLineParserBuilder {

	private final List<CommandLineArgument> arguments = new ArrayList<>();

	/** Creates a new {@link CommandLineParserBuilder}. */
	public CommandLineParserBuilder() {}

	/**
	 * Adds a new boolean command-line argument.
	 *
	 * @param shortName The short name of the argument.
	 * @param longName The long name of the argument.
	 * @param description The description of the argument.
	 * @param defaultValue The default value of the argument.
	 * @return This instance of {@link CommandLineParserBuilder}.
	 */
	public CommandLineParserBuilder addBoolean(
			final String shortName, final String longName, final String description, final boolean defaultValue) {
		arguments.add(new BooleanArgument(shortName, longName, description, defaultValue));
		return this;
	}

	/**
	 * Adds a new String command-line argument.
	 *
	 * @param shortName The short name of the argument.
	 * @param longName The long name of the argument.
	 * @param description The description of the argument.
	 * @param defaultValue The default value of the argument.
	 * @return This instance of {@link CommandLineParserBuilder}.
	 */
	@SuppressWarnings("PMD.UseObjectForClearerAPI")
	public CommandLineParserBuilder addString(
			final String shortName, final String longName, final String description, final String defaultValue) {
		arguments.add(new StringArgument(shortName, longName, description, defaultValue));
		return this;
	}

	/**
	 * Creates the {@link CommandLineParser} with the given arguments.
	 *
	 * @return A new {@link CommandLineParser}.
	 */
	public CommandLineParser build() {
		return new CommandLineParser(arguments);
	}
}
