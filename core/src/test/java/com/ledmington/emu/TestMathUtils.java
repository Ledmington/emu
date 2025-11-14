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
		assertTrue(MathUtils.willCarryAdd((byte) 200, (byte) 100));
	}

	@Test
	void byteNoCarry() {
		assertFalse(MathUtils.willCarryAdd((byte) 1, (byte) 2));
	}

	@Test
	void shortCarry() {
		assertTrue(MathUtils.willCarryAdd((short) 60_000, (short) 50_000));
	}

	@Test
	void shortNoCarry() {
		assertFalse(MathUtils.willCarryAdd((short) 1, (short) 2));
	}

	@Test
	void intCarry() {
		assertTrue(MathUtils.willCarryAdd((int) 3_000_000_000L, (int) 3_000_000_000L));
	}

	@Test
	void intNoCarry() {
		assertFalse(MathUtils.willCarryAdd(1, 2));
	}

	@Test
	void longCarry() {
		assertTrue(MathUtils.willCarryAdd(
				Long.parseUnsignedLong("10000000000000000000"), Long.parseUnsignedLong("10000000000000000000")));
	}

	@Test
	void longNoCarry() {
		assertFalse(MathUtils.willCarryAdd(1L, 2L));
	}

	@Test
	void byteBorrow() {
		assertTrue(MathUtils.willCarrySub((byte) 50, (byte) 100));
	}

	@Test
	void byteNoBorrow() {
		assertFalse(MathUtils.willCarrySub((byte) 100, (byte) 50));
	}

	@Test
	void shortBorrow() {
		assertTrue(MathUtils.willCarrySub((short) 10_000, (short) 50_000));
	}

	@Test
	void shortNoBorrow() {
		assertFalse(MathUtils.willCarrySub((short) 50_000, (short) 10_000));
	}

	@Test
	void intBorrow() {
		assertTrue(MathUtils.willCarrySub((int) 1_000_000_000L, (int) 3_000_000_000L));
	}

	@Test
	void intNoBorrow() {
		assertFalse(MathUtils.willCarrySub(3_000_000_000L, 1_000_000_000L));
	}

	@Test
	void longBorrow() {
		long a = Long.parseUnsignedLong("10000000000000000000");
		long b = Long.parseUnsignedLong("F000000000000000", 16); // much larger
		assertTrue(MathUtils.willCarrySub(a, b));
	}

	@Test
	void longNoBorrow() {
		long a = Long.parseUnsignedLong("F000000000000000", 16);
		long b = Long.parseUnsignedLong("10000000000000000000");
		assertFalse(MathUtils.willCarrySub(a, b));
	}
}
