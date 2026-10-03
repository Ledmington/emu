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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

		try (final DataInputStream in =
				new DataInputStream(new BufferedInputStream(new FileInputStream(input.toFile())))) {
			final byte[] buffer = new byte[32_768];
			final int bytesRead = in.read(buffer, 0, buffer.length);
			System.out.printf("Read %,d bytes%n", bytesRead);

			for (int i = 0; i < Math.min(bytesRead, buffer.length); i++) {
				if (i % 16 == 0) {
					System.out.printf("0x%06x : ", i);
				}
				System.out.printf(" %02x", buffer[i]);
				if (i % 16 == 15) {
					System.out.println();
				}
			}
			System.out.println();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
