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

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/** End-to-end testing emulating entire executables. */
final class TestEmulation {

	@Test
	void doNothing() {
		final Path p;
		try {
			p = Paths.get(Objects.requireNonNull(
							Thread.currentThread().getContextClassLoader().getResource("do_nothing.elf"))
					.toURI());
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
		assertDoesNotThrow(() -> Emu.run(p.toString()));
	}

	@Test
	void doNothing2() {
		final Path p;
		try {
			p = Paths.get(Objects.requireNonNull(
							Thread.currentThread().getContextClassLoader().getResource("do_nothing.small"))
					.toURI());
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
		assertDoesNotThrow(() -> Emu.run(p.toString()));
	}

	@Test
	void small() {
		final Path p;
		try {
			p = Paths.get(Objects.requireNonNull(
							Thread.currentThread().getContextClassLoader().getResource("small.x"))
					.toURI());
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
		assertDoesNotThrow(() -> Emu.run(p.toString()));
	}
}
