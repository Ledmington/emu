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
package com.ledmington.emu;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.util.Objects;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** End-to-end testing emulating entire executables. */
final class TestEmulation {

	@ParameterizedTest
	@ValueSource(strings = {"do_nothing.static", "do_nothing.dynamic", "small.x"})
	void endToEndEmulation(final String executableName) {
		final String path = Objects.requireNonNull(
						Thread.currentThread()
								.getContextClassLoader()
								.getResource(String.join(File.separator, "generated", executableName)),
						() -> String.format(
								"File '%s' not found: did you forget to run './gradlew :core:generateE2ETestFiles'?",
								executableName))
				.getPath();
		final Emu emu = new Emu();
		assertDoesNotThrow(() -> emu.loadRunAndUnload(path));
	}
}
