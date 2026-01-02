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
package com.ledmington.emu;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

final class TestRFlags {

	@ParameterizedTest
	@EnumSource(RFlags.class)
	void validBits(final RFlags f) {
		assertTrue(
				f.bit() >= 0 && f.bit() < 32,
				() -> String.format("RFlag %s has an invalid bit index of %,d.", f, f.bit()));
	}

	@Test
	void uniqueBits() {
		for (final RFlags a : RFlags.values()) {
			for (final RFlags b : RFlags.values()) {
				if (a == b) {
					continue;
				}
				assertNotEquals(
						a.bit(),
						b.bit(),
						() -> String.format(
								"RFlag %s has the same bit index of RFlag %s, which is %,d.", a, b, a.bit()));
			}
		}
	}

	@Test
	void uniqueSymbol() {
		for (final RFlags a : RFlags.values()) {
			for (final RFlags b : RFlags.values()) {
				if (a == b) {
					continue;
				}
				assertNotEquals(
						a.getSymbol(),
						b.getSymbol(),
						() -> String.format(
								"RFlag %s has the same symbol of RFlag %s, which is '%s'.", a, b, a.getSymbol()));
			}
		}
	}
}
