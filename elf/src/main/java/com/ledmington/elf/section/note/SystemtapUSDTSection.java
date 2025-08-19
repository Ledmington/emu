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
package com.ledmington.elf.section.note;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.elf.section.SectionHeader;
import com.ledmington.utils.ReadOnlyByteBuffer;

/** A .note.stapsdt ELF section. */
public final class SystemtapUSDTSection implements NoteSection {

	private final SectionHeader header;
	private final NoteSectionEntry[] entries;

	/**
	 * Creates a .note.stapsdt section with the given data.
	 *
	 * @param sectionHeader The header of this section.
	 * @param b The ReadOnlyByteBuffer to read data from.
	 */
	public SystemtapUSDTSection(final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
		this.header = Objects.requireNonNull(sectionHeader);

		b.setPosition(sectionHeader.getFileOffset());
		b.setAlignment(sectionHeader.getAlignment());
		this.entries = NoteSection.loadNoteSectionEntries(b, sectionHeader.getSectionSize());

		for (int i = 0; i < entries.length; i++) {
			if (!"stapsdt".equals(entries[i].getName())) {
				throw new IllegalArgumentException(String.format(
						"Invalid owner for entry n.%d in .note.stapsdt section: expected 'stapsdt' but was '%s'",
						i, entries[i].getName()));
			}
		}
	}

	@Override
	public String getName() {
		return ".note.stapsdt";
	}

	@Override
	public SectionHeader header() {
		return header;
	}

	@Override
	public int getNumEntries() {
		return entries.length;
	}

	@Override
	public NoteSectionEntry getEntry(final int idx) {
		return entries[idx];
	}

	@Override
	public String toString() {
		return "SystemtapUSDT(header=" + header + ";entries=" + Arrays.toString(entries) + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + header.hashCode();
		h = 31 * h + Arrays.hashCode(entries);
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
		if (!(other instanceof final SystemtapUSDTSection ss)) {
			return false;
		}
		return this.header.equals(ss.header) && Arrays.equals(this.entries, ss.entries);
	}
}
