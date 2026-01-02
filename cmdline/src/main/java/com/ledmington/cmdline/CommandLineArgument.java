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

/** Interface representing a command-line argument with optional short and long names. */
public interface CommandLineArgument {

	/**
	 * Checks if the argument has a short name.
	 *
	 * @return {@code true} if the argument has a short name, {@code false} otherwise.
	 */
	boolean hasShortName();

	/**
	 * Returns the short name of the argument.
	 *
	 * @return The short name of the argument, or {@code null} if none exists.
	 */
	String shortName();

	/**
	 * Checks if the argument has a long name.
	 *
	 * @return {@code true} if the argument has a long name, {@code false} otherwise.
	 */
	boolean hasLongName();

	/**
	 * Returns the long name of the argument.
	 *
	 * @return The long name of the argument, or {@code null} if none exists.
	 */
	String longName();

	/**
	 * Returns a brief description of the argument’s purpose.
	 *
	 * @return The description of the argument.
	 */
	String description();
}
