/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.elf.section;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class TestSectionHeaderType {
	@Test
	void allSectionHeaderTypesAreValid() {
		for (final SectionHeaderType sht : SectionHeaderType.values()) {
			assertTrue(
					SectionHeaderType.isValid(sht.getCode()),
					() -> String.format(
							"Expected SectionHeaderType object %s with code 0x%08x to be valid but wasn't",
							sht, sht.getCode()));
		}
	}
}
