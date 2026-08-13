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

import java.util.Objects;

/**
 * Documents a positional command-line argument for {@link CommandLineParser#helpMessage()}. This is purely descriptive:
 * {@link CommandLineParser#parse} always returns every leftover token through
 * {@link ParsedArguments#positionalArguments()} regardless of how many (if any) {@code PositionalArgument}s were
 * registered on the builder.
 *
 * @param name The name shown for this argument in the usage line and the help listing (e.g. {@code "FILE"}).
 * @param description A brief description of the argument's purpose.
 */
public record PositionalArgument(String name, String description) {

	/** Creates a new {@code PositionalArgument} record. */
	public PositionalArgument {
		Objects.requireNonNull(name, "Null name.");
		Objects.requireNonNull(description, "Null description.");
	}
}
