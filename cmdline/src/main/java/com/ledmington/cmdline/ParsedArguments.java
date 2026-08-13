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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The result of parsing a full command line: the recognized options plus any leftover positional arguments, i.e. the
 * tokens, in order, from the first one that did not match any registered option onward. Parsing never looks for options
 * past that point, so a filename followed by arguments meant for another program is returned intact.
 *
 * @param options A {@link Map} from the short/long name of each registered argument to its parsed (or default) value.
 * @param positionalArguments The tokens following the last recognized option, in their original order.
 */
public record ParsedArguments(Map<String, ParsingResult> options, List<String> positionalArguments) {

	/** Creates a new {@code ParsedArguments} record, defensively copying both collections. */
	public ParsedArguments {
		options = Map.copyOf(Objects.requireNonNull(options, "Null options."));
		positionalArguments = List.copyOf(Objects.requireNonNull(positionalArguments, "Null positionalArguments."));
	}

	/**
	 * Convenience accessor equivalent to {@code options().get(name)}.
	 *
	 * @param name The short or long name of a registered argument.
	 * @return The parsed (or default) value of the given argument.
	 */
	public ParsingResult get(final String name) {
		if (!options.containsKey(name)) {
			throw new IllegalArgumentException(String.format("Unknown command-line argument '%s'.", name));
		}
		return options.get(name);
	}
}
