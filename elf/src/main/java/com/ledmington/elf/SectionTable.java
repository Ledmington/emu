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
package com.ledmington.elf;

import com.ledmington.elf.section.Section;

/** An interface for ELF files/objects which behave like a section table. */
public interface SectionTable {

	/**
	 * Returns the number of sections.
	 *
	 * @return The number of sections.
	 */
	int getSectionTableLength();

	/**
	 * Returns the i-th section.
	 *
	 * @param idx The index of the section to return.
	 * @return The i-th section.
	 */
	Section getSection(final int idx);
}
