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
package com.ledmington.cmdline;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A builder for {@link CommandLineParser} to ease adding arguments to be parsed. */
public final class CommandLineParserBuilder {

	/** The default group name for options added before the first {@link #group(String)} call. */
	private static final String DEFAULT_GROUP = "Options";

	private final List<CommandLineArgument> arguments = new ArrayList<>();
	private final List<String> argumentGroups = new ArrayList<>();
	private final List<PositionalArgument> positionalArguments = new ArrayList<>();
	private String currentGroup = DEFAULT_GROUP;
	private String programName = "";
	private String programDescription = "";

	/** Creates a new {@link CommandLineParserBuilder}. */
	public CommandLineParserBuilder() {}

	/**
	 * Sets the name of the program, used in the usage line and title of {@link CommandLineParser#helpMessage()}.
	 *
	 * @param name The name of the program.
	 * @return This instance of {@link CommandLineParserBuilder}.
	 */
	public CommandLineParserBuilder programName(final String name) {
		this.programName = Objects.requireNonNull(name, "Null name.");
		return this;
	}

	/**
	 * Sets a one-line description of the program, shown next to its name in {@link CommandLineParser#helpMessage()}.
	 *
	 * @param description The description of the program.
	 * @return This instance of {@link CommandLineParserBuilder}.
	 */
	public CommandLineParserBuilder description(final String description) {
		this.programDescription = Objects.requireNonNull(description, "Null description.");
		return this;
	}

	/**
	 * Starts a new named group: every option added after this call (until the next {@link #group(String)} call, if any)
	 * is listed under this heading in {@link CommandLineParser#helpMessage()}.
	 *
	 * @param name The name of the group (e.g. {@code "Memory options"}).
	 * @return This instance of {@link CommandLineParserBuilder}.
	 */
	public CommandLineParserBuilder group(final String name) {
		this.currentGroup = Objects.requireNonNull(name, "Null name.");
		return this;
	}

	/**
	 * Adds a new boolean command-line argument, under the group started by the last {@link #group(String)} call.
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
		argumentGroups.add(currentGroup);
		return this;
	}

	/**
	 * Adds a new String command-line argument, under the group started by the last {@link #group(String)} call.
	 *
	 * @param shortName The short name of the argument.
	 * @param longName The long name of the argument.
	 * @param description The description of the argument.
	 * @param defaultValue The default value of the argument.
	 * @return This instance of {@link CommandLineParserBuilder}.
	 */
	public CommandLineParserBuilder addString(
			final String shortName, final String longName, final String description, final String defaultValue) {
		arguments.add(new StringArgument(shortName, longName, description, defaultValue));
		argumentGroups.add(currentGroup);
		return this;
	}

	/**
	 * Documents a positional argument for {@link CommandLineParser#helpMessage()}. Purely descriptive: parsing itself
	 * always returns every leftover token, whether or not it was documented here.
	 *
	 * @param name The name shown for this argument (e.g. {@code "FILE"}).
	 * @param description The description of the argument.
	 * @return This instance of {@link CommandLineParserBuilder}.
	 */
	public CommandLineParserBuilder addPositional(final String name, final String description) {
		positionalArguments.add(new PositionalArgument(name, description));
		return this;
	}

	/**
	 * Creates the {@link CommandLineParser} with the given arguments.
	 *
	 * @return A new {@link CommandLineParser}.
	 */
	public CommandLineParser build() {
		return new CommandLineParser(arguments, argumentGroups, positionalArguments, programName, programDescription);
	}
}
