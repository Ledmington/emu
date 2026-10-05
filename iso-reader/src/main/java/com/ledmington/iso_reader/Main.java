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
package com.ledmington.iso_reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ledmington.iso.Iso;
import com.ledmington.iso.Sector;
import com.ledmington.utils.BufferedBinaryReader;

public final class Main {
	public static void main(final String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: iso-reader <iso_file>");
			System.exit(1);
			return;
		}

		if ("-h".equals(args[0]) || "--help".equals(args[0])) {
			System.out.println("Usage: iso-reader <iso_file>");
			System.exit(0);
			return;
		}

		final Path input = Path.of(args[0]).normalize().toAbsolutePath();
		try {
			System.out.printf("The file '%s' is %,d bytes long.%n", input, Files.size(input));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		try (BufferedBinaryReader reader = new BufferedBinaryReader(input)) {
			// System area
			final Sector[] systemArea = new Sector[16];
			for (int i = 0; i < 16; i++) {
				systemArea[i] = Sector.read(reader);
			}

			// Data area

			final Iso iso = new Iso(systemArea);
		}
	}
}
