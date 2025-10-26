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
package com.ledmington.emudb;

import java.util.Arrays;

import com.ledmington.utils.MiniLogger;

public final class Main {

	private Main() {}

	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.WARNING);

		int i = 0;
		loop:
		for (; i < args.length; i++) {
			final String arg = args[i];
			switch (arg) {
				case "-q", "--quiet" -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
				case "-v", "--verbose" -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.INFO);
				case "-vv", "--very-verbose" -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.DEBUG);
				default -> {
					break loop;
				}
			}
		}

		final String[] arguments = (i > 0) ? Arrays.copyOfRange(args, i, args.length) : args;

		final EmuDB debugger = new EmuDB();
		debugger.run(arguments);
	}
}
