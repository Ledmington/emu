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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public final class TestMathUtils {
	@Test
	void byteCarry() {
		assertTrue(MathUtils.willCarry((byte) 200, (byte) 100));
	}

	@Test
	void byteNoCarry() {
		assertFalse(MathUtils.willCarry((byte) 1, (byte) 2));
	}

	@Test
	void shortCarry() {
		assertTrue(MathUtils.willCarry((short) 60_000, (short) 50_000));
	}

	@Test
	void shortNoCarry() {
		assertFalse(MathUtils.willCarry((short) 1, (short) 2));
	}

	@Test
	void intCarry() {
		assertTrue(MathUtils.willCarry((int) 3_000_000_000L, (int) 3_000_000_000L));
	}

	@Test
	void intNoCarry() {
		assertFalse(MathUtils.willCarry(1, 2));
	}

	@Test
	void longCarry() {
		assertTrue(MathUtils.willCarry(
				Long.parseUnsignedLong("10000000000000000000"), Long.parseUnsignedLong("10000000000000000000")));
	}

	@Test
	void longNoCarry() {
		assertFalse(MathUtils.willCarry(1L, 2L));
	}
}
