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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class TestMathUtils {
	@Test
	void byteCarry() {
		final byte a = (byte) 200;
		final byte b = (byte) 100;
		assertTrue(
				MathUtils.willCarryAdd(a, b),
				() -> String.format("Expected 0x%02x + 0x%02x to carry but id did not.", a, b));
	}

	@Test
	void byteNoCarry() {
		final byte a = (byte) 1;
		final byte b = (byte) 2;
		assertFalse(
				MathUtils.willCarryAdd(a, b),
				() -> String.format("Expected 0x%02x + 0x%02x to not carry but id did.", a, b));
	}

	@Test
	void shortCarry() {
		final short a = (short) 50_000;
		final short b = (short) 60_000;
		assertTrue(
				MathUtils.willCarryAdd(a, b),
				() -> String.format("Expected 0x%04x + 0x%04x to carry but id did not.", a, b));
	}

	@Test
	void shortNoCarry() {
		final short a = (short) 1;
		final short b = (short) 2;
		assertFalse(
				MathUtils.willCarryAdd(a, b),
				() -> String.format("Expected 0x%04x + 0x%04x to not carry but id did.", a, b));
	}

	@Test
	void intCarry() {
		final int a = (int) 3_000_000_000L;
		final int b = (int) 3_000_000_000L;
		assertTrue(
				MathUtils.willCarryAdd(a, b),
				() -> String.format("Expected 0x%08x + 0x%08x to carry but id did not.", a, b));
	}

	@Test
	void intNoCarry() {
		final int a = 1;
		final int b = 2;
		assertFalse(
				MathUtils.willCarryAdd(a, b),
				() -> String.format("Expected 0x%08x + 0x%08x to not carry but id did.", a, b));
	}

	@Test
	void longCarry() {
		final long a = Long.parseUnsignedLong("10000000000000000000");
		final long b = Long.parseUnsignedLong("10000000000000000000");
		assertTrue(
				MathUtils.willCarryAdd(a, b),
				() -> String.format("Expected 0x%016x + 0x%016x to not carry but id did.", a, b));
	}

	@Test
	void longNoCarry() {
		final long a = 1L;
		final long b = 2L;
		assertFalse(
				MathUtils.willCarryAdd(a, b),
				() -> String.format("Expected 0x%016x + 0x%016x to not carry but id did.", a, b));
	}

	@Test
	void byteBorrow() {
		final byte a = (byte) 50;
		final byte b = (byte) 100;
		assertTrue(
				MathUtils.willCarrySub(a, b),
				() -> String.format("Expected 0x%02x - 0x%02x to carry but id did not.", a, b));
	}

	@Test
	void byteNoBorrow() {
		final byte a = (byte) 100;
		final byte b = (byte) 50;
		assertFalse(
				MathUtils.willCarrySub(a, b),
				() -> String.format("Expected 0x%02x - 0x%02x to not carry but id did.", a, b));
	}

	@Test
	void shortBorrow() {
		final short a = (short) 10_000;
		final short b = (short) 50_000;
		assertTrue(
				MathUtils.willCarrySub(a, b),
				() -> String.format("Expected 0x%04x - 0x%04x to carry but id did not.", a, b));
	}

	@Test
	void shortNoBorrow() {
		final short a = (short) 50_000;
		final short b = (short) 10_000;
		assertFalse(
				MathUtils.willCarrySub(a, b),
				() -> String.format("Expected 0x%04x - 0x%04x to not carry but id did.", a, b));
	}

	@Test
	void intBorrow() {
		final int a = (int) 1_000_000_000L;
		final int b = (int) 3_000_000_000L;
		assertTrue(
				MathUtils.willCarrySub(a, b),
				() -> String.format("Expected 0x%08x - 0x%08x to carry but id did not.", a, b));
	}

	@Test
	void intNoBorrow() {
		final int a = (int) 3_000_000_000L;
		final int b = (int) 1_000_000_000L;
		assertFalse(
				MathUtils.willCarrySub(a, b),
				() -> String.format("Expected 0x%08x - 0x%08x to not carry but id did.", a, b));
	}

	@Test
	void longBorrow() {
		final long a = Long.parseUnsignedLong("10000000000000000000");
		final long b = Long.parseUnsignedLong("F000000000000000", 16);
		assertTrue(
				MathUtils.willCarrySub(a, b),
				() -> String.format("Expected 0x%016x - 0x%016x to carry but id did not.", a, b));
	}

	@Test
	void longNoBorrow() {
		final long a = Long.parseUnsignedLong("F000000000000000", 16);
		final long b = Long.parseUnsignedLong("10000000000000000000");
		assertFalse(
				MathUtils.willCarrySub(a, b),
				() -> String.format("Expected 0x%016x - 0x%016x to not carry but id did.", a, b));
	}

	@Test
	void byteOverflowSub() {
		final byte a = (byte) -128;
		final byte b = (byte) 1;
		assertTrue(
				MathUtils.willOverflowSub(a, b),
				() -> String.format("Expected 0x%02x - 0x%02x to overflow but it did not.", a, b));
	}

	@Test
	void byteNoOverflowSub() {
		final byte a = (byte) 50;
		final byte b = (byte) 20;
		assertFalse(
				MathUtils.willOverflowSub(a, b),
				() -> String.format("Expected 0x%02x - 0x%02x to not overflow but it did.", a, b));
	}

	@Test
	void shortOverflowSub() {
		final short a = (short) -32_768;
		final short b = (short) 1;
		assertTrue(
				MathUtils.willOverflowSub(a, b),
				() -> String.format("Expected 0x%04x - 0x%04x to overflow but it did not.", a, b));
	}

	@Test
	void shortNoOverflowSub() {
		final short a = (short) 5000;
		final short b = (short) 2000;
		assertFalse(
				MathUtils.willOverflowSub(a, b),
				() -> String.format("Expected 0x%04x - 0x%04x to not overflow but it did.", a, b));
	}

	@Test
	void intOverflowSub() {
		final int a = Integer.MIN_VALUE;
		final int b = 1;
		assertTrue(
				MathUtils.willOverflowSub(a, b),
				() -> String.format("Expected 0x%08x - 0x%08x to overflow but it did not.", a, b));
	}

	@Test
	void intNoOverflowSub() {
		final int a = 10_000;
		final int b = 2_000;
		assertFalse(
				MathUtils.willOverflowSub(a, b),
				() -> String.format("Expected 0x%08x - 0x%08x to not overflow but it did.", a, b));
	}

	@Test
	void longOverflowSub() {
		final long a = Long.MIN_VALUE;
		final long b = 1L;
		assertTrue(
				MathUtils.willOverflowSub(a, b),
				() -> String.format("Expected 0x%016x - 0x%016x to overflow but it did not.", a, b));
	}

	@Test
	void longNoOverflowSub() {
		final long a = 1_000L;
		final long b = 500L;
		assertFalse(
				MathUtils.willOverflowSub(a, b),
				() -> String.format("Expected 0x%016x - 0x%016x to not overflow but it did.", a, b));
	}
}
