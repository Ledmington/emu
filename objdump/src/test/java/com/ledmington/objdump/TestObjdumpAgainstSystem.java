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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ledmington.utils.SuppressFBWarnings;

/** Checks that this project's objdump produces the exact same output as the system's objdump. */
@DisabledOnOs(OS.WINDOWS)
final class TestObjdumpAgainstSystem {

	private static final String SYSTEM_OBJDUMP_PATH = "/usr/bin/objdump";

	private static Path e2eTestFile(final String executableName) {
		final String e2eTestFilesDir = Objects.requireNonNull(
				System.getProperty("e2eTestFilesDir"), "System property 'e2eTestFilesDir' was not set.");
		final Path p = Path.of(e2eTestFilesDir, executableName);
		if (!Files.exists(p)) {
			throw new IllegalStateException(String.format(
					"File '%s' not found: did you forget to run './gradlew :core:generateEndToEndTestFiles'?", p));
		}
		return p;
	}

	@SuppressFBWarnings(
			value = "DMI_HARDCODED_ABSOLUTE_FILENAME",
			justification = "This is where the system's objdump is expected to be found on Linux.")
	private static void checkSystemObjdumpIsAvailable() {
		Assumptions.assumeTrue(Files.exists(Path.of(SYSTEM_OBJDUMP_PATH)), "system objdump was not found, skipping.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"do_nothing.static", "do_nothing.dynamic", "small.x"})
	void disassembly(final String executableName) {
		checkSystemObjdumpIsAvailable();
		final Path p = e2eTestFile(executableName);
		assertEquals(
				ObjdumpSystemComparison.runSystemObjdump(p),
				ObjdumpSystemComparison.runCustomObjdump(p),
				() -> "objdump output for '" + p + "' did not match the system's objdump output.");
	}
}
