/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.emu;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javafx.application.Application;
import javafx.stage.Stage;

import com.ledmington.utils.MiniLogger;

public final class Main extends Application {

	private static final PrintWriter out = System.console() == null
			? new PrintWriter(System.out, false, StandardCharsets.UTF_8)
			: System.console().writer();

	@Override
	public void start(final Stage stage) {
		new Emu(stage);
	}

	public static void main(final String[] args) {
		MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.WARNING);

		final String shortHelpFlag = "-h";
		final String longHelpFlag = "--help";
		final String shortQuietFlag = "-q";
		final String longQuietFlag = "--quiet";
		final String verboseFlag = "-v";
		final String veryVerboseFlag = "-vv";

		for (final String arg : args) {
			switch (arg) {
				case shortHelpFlag, longHelpFlag -> {
					out.print(String.join(
							"\n",
							"",
							" emu - CPU emulator GUI",
							"",
							" Usage: emu [OPTIONS]",
							"",
							" Command line options:",
							"",
							" -h, --help   Shows this help message and exits.",
							" -q, --quiet  Only errors are reported.",
							" -v           Errors, warnings and info messages are reported.",
							" -vv          All messages are reported.",
							""));
					out.flush();
					System.exit(0);
				}
				case shortQuietFlag, longQuietFlag -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.ERROR);
				case verboseFlag -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.INFO);
				case veryVerboseFlag -> MiniLogger.setMinimumLevel(MiniLogger.LoggingLevel.DEBUG);
				default -> {
					out.printf("Unknown flag '%s'\n", arg);
					out.flush();
					System.exit(-1);
				}
			}
		}

		Application.launch(args);
		out.flush();
	}
}
