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
package com.ledmington.cmdline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class TestParser {
	@Test
	void booleanOptionLongName() {
		final CommandLineParser p = CommandLineParser.builder()
				.addBoolean(null, "test", "This is a testing option.", true)
				.build();
		assertTrue(p.parse().get("test").asBoolean(), "Expected to parse '' as true but didn't.");
		assertFalse(p.parse("--test").get("test").asBoolean(), "Expected to parse '--test' as false but didn't.");
	}

	@Test
	void booleanOptionShortName() {
		final CommandLineParser p = CommandLineParser.builder()
				.addBoolean("test", null, "This is a testing option.", true)
				.build();
		assertTrue(p.parse().get("test").asBoolean(), "Expected to parse '' as true but didn't.");
		assertFalse(p.parse("-test").get("test").asBoolean(), "Expected to parse '-test' as false but didn't.");
	}

	@Test
	void stringOption() {
		final CommandLineParser p = CommandLineParser.builder()
				.addString("test", null, "This is a testing option.", "default")
				.build();
		assertEquals("default", p.parse().get("test").asString(), "Expected to parse '' as 'default' but didn't.");
		assertEquals(
				"hello",
				p.parse("-test", "hello").get("test").asString(),
				"Expected to parse '-test hello' as 'hello' but didn't.");
		assertEquals(
				"hello",
				p.parse("-test=hello").get("test").asString(),
				"Expected to parse '-test=hello' as 'hello' but didn't.");
		assertThrows(
				IllegalArgumentException.class,
				() -> p.parse("-test"),
				"Expected to not be able to parse '-test' but it did.");
	}
}
