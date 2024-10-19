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
package com.ledmington.elf.section.rel;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.elf.section.Section;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;

/** An ELF Relocation table without explicit addends. */
public final class RelocationSection implements Section {

	private final String name;
	private final SectionHeader header;
	private final RelocationEntry[] relocationTable;

	/**
	 * Creates an ELF relocation section with the given data.
	 *
	 * @param name The name of the section.
	 * @param sectionHeader The header of the section.
	 * @param b The ReadOnlyByteBuffer to read data from.
	 * @param is32Bit Used for byte alignment.
	 */
	public RelocationSection(
			final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
		this.name = Objects.requireNonNull(name);
		this.header = Objects.requireNonNull(sectionHeader);

		b.setPosition(sectionHeader.getFileOffset());
		final int nEntries = (int) (sectionHeader.getSectionSize() / sectionHeader.getEntrySize());
		this.relocationTable = new RelocationEntry[nEntries];
		for (int i = 0; i < nEntries; i++) {
			final long offset = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
			final long info = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
			this.relocationTable[i] = new RelocationEntry(offset, info);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SectionHeader getHeader() {
		return header;
	}

	@Override
	public String toString() {
		return "RelocationSection(name=" + name + ";header=" + header + ";relocationTable="
				+ Arrays.toString(relocationTable) + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + name.hashCode();
		h = 31 * h + header.hashCode();
		h = 31 * h + Arrays.hashCode(relocationTable);
		return h;
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		final RelocationSection rs = (RelocationSection) other;
		return this.name.equals(rs.name)
				&& this.header.equals(rs.header)
				&& Arrays.equals(this.relocationTable, rs.relocationTable);
	}
}
