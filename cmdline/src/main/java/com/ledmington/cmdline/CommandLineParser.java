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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** The parser of command-line arguments. */
public final class CommandLineParser {

	private final List<CommandLineArgument> arguments;
	private final List<String> argumentGroups;
	private final List<PositionalArgument> positionalArguments;
	private final String programName;
	private final String programDescription;

	/**
	 * Returns a new {@link CommandLineParserBuilder} to ease adding the different types of arguments.
	 *
	 * @return A new {@link CommandLineParserBuilder}.
	 */
	public static CommandLineParserBuilder builder() {
		return new CommandLineParserBuilder();
	}

	/* default */ CommandLineParser(
			final List<CommandLineArgument> arguments,
			final List<String> argumentGroups,
			final List<PositionalArgument> positionalArguments,
			final String programName,
			final String programDescription) {
		this.arguments = Objects.requireNonNull(arguments);
		this.argumentGroups = Objects.requireNonNull(argumentGroups);
		this.positionalArguments = Objects.requireNonNull(positionalArguments);
		this.programName = Objects.requireNonNull(programName);
		this.programDescription = Objects.requireNonNull(programDescription);
	}

	/**
	 * Parses the given command-line arguments. Recognized options (and their default values, for the ones not found)
	 * are returned alongside any positional arguments, i.e. the tokens starting from the first one that does not match
	 * any registered option: parsing stops there and everything from that point on is returned as-is, without being
	 * matched against the registered options.
	 *
	 * @param commandLine The list of input arguments.
	 * @return The parsed options and any leftover positional arguments.
	 */
	@SuppressWarnings({"PMD.UseConcurrentHashMap", "PMD.AvoidInstantiatingObjectsInLoops"})
	public ParsedArguments parse(final String... commandLine) {
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

		final List<String> positional = new ArrayList<>();
		int i = 0;
		while (i < commandLine.length) {
			final int consumed = tryMatch(commandLine, i, result);
			if (consumed == 0) {
				// The current token (and everything after it) is not a registered option: treat it as the
				// start of the positional arguments and stop looking for options.
				positional.addAll(Arrays.asList(commandLine).subList(i, commandLine.length));
				break;
			}
			i += consumed;
		}

		return new ParsedArguments(result, positional);
	}

	/**
	 * Tries to match the token at {@code index} against every registered option. On a match, updates {@code result}
	 * under both the short and long name of the matched option (so a lookup gives the same answer regardless of which
	 * alias the user typed) and returns how many tokens were consumed. Returns {@code 0} if no option matched.
	 */
	private int tryMatch(final String[] commandLine, final int index, final Map<String, ParsingResult> result) {
		final String arg = commandLine[index];
		final int equalsIndex = arg.indexOf('=');
		final String argumentName = equalsIndex == -1 ? arg : arg.substring(0, equalsIndex);

		final Optional<CommandLineArgument> match = arguments.stream()
				.filter(cla -> (cla.hasShortName() && argumentName.equals("-" + cla.shortName()))
						|| (cla.hasLongName() && argumentName.equals("--" + cla.longName())))
				.findFirst();

		if (match.isEmpty()) {
			return 0;
		}

		final CommandLineArgument cla = match.get();
		final MatchOutcome outcome =
				switch (cla) {
					case BooleanArgument ba -> new MatchOutcome(new BooleanResult(!ba.defaultValue()), 1);
					case StringArgument _ -> {
						final String value =
								equalsIndex == -1 ? commandLine[index + 1] : arg.substring(equalsIndex + 1);
						yield new MatchOutcome(new StringResult(value), equalsIndex == -1 ? 2 : 1);
					}
					default ->
						throw new IllegalArgumentException(String.format("Unknown command line argument: '%s'", cla));
				};

		if (cla.hasShortName()) {
			result.put(cla.shortName(), outcome.value());
		}
		if (cla.hasLongName()) {
			result.put(cla.longName(), outcome.value());
		}
		return outcome.tokensConsumed();
	}

	/**
	 * Renders the full help message: program name/description, a usage line, every registered option grouped under the
	 * headings set up via {@link CommandLineParserBuilder#group(String)}, and any documented positional arguments, all
	 * aligned into a single block of text.
	 *
	 * @return The formatted help message.
	 */
	public String helpMessage() {
		final List<String> optionColumns =
				arguments.stream().map(this::formatFlag).toList();
		final List<String> positionalColumns =
				positionalArguments.stream().map(p -> " " + p.name()).toList();
		final int width = Stream.concat(optionColumns.stream(), positionalColumns.stream())
				.mapToInt(String::length)
				.max()
				.orElse(0);

		final List<String> lines = new ArrayList<>();
		lines.add("");
		addTitleAndUsage(lines);
		addGroupedOptions(lines, optionColumns, width);
		addPositionalArguments(lines, positionalColumns, width);

		if (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
			lines.remove(lines.size() - 1);
		}

		return String.join("\n", lines);
	}

	private void addTitleAndUsage(final List<String> lines) {
		if (!programName.isEmpty()) {
			lines.add(" " + programName + (programDescription.isEmpty() ? "" : " - " + programDescription));
			lines.add("");
		}
		lines.add(" Usage: " + programName + " [OPTIONS]"
				+ positionalArguments.stream().map(p -> " " + p.name()).collect(Collectors.joining()));
		lines.add("");
	}

	private void addGroupedOptions(final List<String> lines, final List<String> optionColumns, final int width) {
		for (final String group : argumentGroups.stream().distinct().toList()) {
			lines.add(" " + group + ":");
			lines.add("");
			for (int i = 0; i < arguments.size(); i++) {
				if (!group.equals(argumentGroups.get(i))) {
					continue;
				}
				final String flag = optionColumns.get(i);
				lines.add(flag
						+ " ".repeat(width - flag.length() + 3)
						+ arguments.get(i).description());
			}
			lines.add("");
		}
	}

	private void addPositionalArguments(
			final List<String> lines, final List<String> positionalColumns, final int width) {
		if (positionalArguments.isEmpty()) {
			return;
		}
		lines.add(" Positional arguments:");
		lines.add("");
		for (int i = 0; i < positionalArguments.size(); i++) {
			final String col = positionalColumns.get(i);
			lines.add(col
					+ " ".repeat(width - col.length() + 3)
					+ positionalArguments.get(i).description());
		}
		lines.add("");
	}

	private String formatFlag(final CommandLineArgument cla) {
		final StringBuilder sb = new StringBuilder(" ");
		if (cla.hasShortName()) {
			sb.append('-').append(cla.shortName());
		}
		if (cla.hasShortName() && cla.hasLongName()) {
			sb.append(", ");
		}
		if (cla.hasLongName()) {
			sb.append("--").append(cla.longName());
		}
		final String valuePlaceholder =
				switch (cla) {
					case BooleanArgument _ -> "";
					case StringArgument _ -> " <value>";
					default ->
						throw new IllegalArgumentException(String.format("Unknown command line argument: '%s'", cla));
				};
		return sb.append(valuePlaceholder).toString();
	}

	private record MatchOutcome(ParsingResult value, int tokensConsumed) {}
}
