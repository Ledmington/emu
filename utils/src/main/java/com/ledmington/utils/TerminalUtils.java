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
package com.ledmington.utils;

/** A collection of common utilities regarding terminal interaction. */
public final class TerminalUtils {

	private TerminalUtils() {}

	/** The character sequence to reset terminal colors and decorations to the default state. */
	public static final String ANSI_RESET = "\u001b[0m";

	/** The character sequence to set/reset boldness. */
	public static final String ANSI_BOLD = "\u001b[1m";

	/** The character sequence to set/reset foreground color red. */
	public static final String ANSI_RED = "\u001b[31m";

	/** The character sequence to set/reset foreground color green. */
	public static final String ANSI_GREEN = "\u001b[32m";

	/** The character sequence to set/reset foreground color yellow. */
	public static final String ANSI_YELLOW = "\u001b[33m";

	/** The character sequence to set/reset foreground color blue. */
	public static final String ANSI_BLUE = "\u001b[34m";

	/** The character sequence to set/reset foreground color magenta. */
	public static final String ANSI_MAGENTA = "\u001b[35m";

	/** The character sequence to set/reset foreground color cyan. */
	public static final String ANSI_CYAN = "\u001b[36m";

	/** The character sequence to set/reset foreground color white. */
	public static final String ANSI_WHITE = "\u001b[37m";

	/** The character sequence to set/reset foreground color gray. */
	public static final String ANSI_GRAY = "\u001b[90m";

	/** The character sequence to set/reset foreground color purple. */
	public static final String ANSI_PURPLE = "\u001b[95m";
}
