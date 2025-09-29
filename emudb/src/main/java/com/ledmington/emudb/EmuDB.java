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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import com.ledmington.elf.ELF;
import com.ledmington.emu.Emu;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public final class EmuDB {

	private ELF currentFile = null;

	public EmuDB() {}

	public void run() {
		final Terminal terminal;
		try {
			terminal = TerminalBuilder.builder().system(true).build();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		final LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
		while (true) {
			final String line;
			try {
				line = reader.readLine("(emudb) ").strip().toLowerCase(Locale.US);
			} catch (final UserInterruptException e) {
				break;
			}
			if (line.isBlank()) {
				continue;
			}

			if (line.equals("quit")) {
				System.exit(0);
				return;
			}
			if (line.equals("help")) {
				System.out.println(" help -- Prints this message");
				System.out.println(" quit -- Terminates the debugger");
				System.out.println(" run FILENAME -- Executes FILENAME");
				continue;
			}

			final String[] splitted = line.split(" +");
			if (splitted[0].equals("run")) {
				Emu.run(String.valueOf(Path.of(splitted[1]).normalize().toAbsolutePath()));
			}
		}
	}
}
