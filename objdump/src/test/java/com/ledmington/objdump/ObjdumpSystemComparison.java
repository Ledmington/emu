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
package com.ledmington.objdump;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Stream;

import com.ledmington.utils.ProcessUtils;

/** Runs both this project's objdump and the system's objdump on a given ELF file, to compare their output. */
@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
final class ObjdumpSystemComparison {

	private static final String fatJarPath;

	static {
		try (Stream<Path> s = Files.find(
						Path.of(".", "build").normalize().toAbsolutePath(), 999, (p, bfa) -> bfa.isRegularFile())
				.filter(p -> p.getFileName().toString().startsWith("emu-objdump")
						&& p.getFileName().toString().endsWith(".jar"))) {
			fatJarPath = s.max(Comparator.comparingLong(a -> a.toFile().length()))
					.orElseThrow()
					.normalize()
					.toAbsolutePath()
					.toString();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ObjdumpSystemComparison() {}

	/* default */ static boolean isELF(final Path p) {
		try (InputStream is = Files.newInputStream(p, StandardOpenOption.READ)) {
			final int expectedBytes = 4;
			final byte[] buffer = new byte[expectedBytes];
			final int bytesRead = is.read(buffer);
			return bytesRead == expectedBytes
					&& buffer[0] == (byte) 0x7f
					&& buffer[1] == (byte) 0x45
					&& buffer[2] == (byte) 0x4c
					&& buffer[3] == (byte) 0x46;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/* default */ static String runSystemObjdump(final Path p) {
		final String systemObjdump = "/usr/bin/objdump";
		final String[] cmd = {systemObjdump, "-d", "-Mintel", p.toString()};
		return ProcessUtils.run(cmd);
	}

	/* default */ static String runCustomObjdump(final Path p) {
		final String[] cmd = {"java", "-jar", fatJarPath, "-d", p.toString()};
		return ProcessUtils.run(cmd);
	}
}
