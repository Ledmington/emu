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
package com.ledmington.elf.section;

import java.util.Objects;

/**
 * An ELF Null section.
 *
 * @param header The section header entry corresponding to this section.
 */
public record NullSection(SectionHeader header) implements Section {

	/**
	 * Creates a Null section with the given section header entry.
	 *
	 * @param header The section header entry corresponding to this section.
	 */
	public NullSection {
		Objects.requireNonNull(header);
	}

	@Override
	public String getName() {
		return "";
	}
}
