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
package com.ledmington.elf.section.gnu;

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.elf.section.DynamicSection;
import com.ledmington.elf.section.DynamicTableEntryTag;
import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/**
 * The .gnu.version_d ELF section.
 *
 * <p>Reference <a href= "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.3.
 */
public final class GnuVersionDefinitionSection implements LoadableSection {

	private static final String standardName = ".gnu.version_d";

	/**
	 * Returns the standard name of this special section.
	 *
	 * @return The string ".gnu.version_d".
	 */
	public static String getStandardName() {
		return standardName;
	}

	private final SectionHeader header;
	private final GnuVersionDefinitionEntry[] entries;

	/**
	 * Creates the GNU version definition section with the given data.
	 *
	 * @param sectionHeader The header of the section.
	 * @param b The buffer to read data from.
	 * @param dynamicSection The Dynamic section of the ELF file to retrieve the value of DT_VERDEFNUM from.
	 */
	public GnuVersionDefinitionSection(
			final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final DynamicSection dynamicSection) {
		this.header = Objects.requireNonNull(sectionHeader);

		int versionDefinitionEntryNum = 0;
		{
			Objects.requireNonNull(dynamicSection);
			for (int i = 0; i < dynamicSection.getTableLength(); i++) {
				if (dynamicSection.getEntry(i).getTag() == DynamicTableEntryTag.DT_VERDEFNUM) {
					versionDefinitionEntryNum =
							BitUtils.asInt(dynamicSection.getEntry(i).getContent());
					break;
				}
			}
		}

		final long oldAlignment = b.getAlignment();
		b.setAlignment(1L);
		b.setPosition(sectionHeader.getFileOffset());

		this.entries = new GnuVersionDefinitionEntry[versionDefinitionEntryNum];
		for (int i = 0; i < this.entries.length; i++) {
			final long entryStart = b.getPosition();
			this.entries[i] = new GnuVersionDefinitionEntry(b);

			b.setPosition(entryStart + BitUtils.asLong(entries[i].getNextOffset()));
		}

		b.setAlignment(oldAlignment);
	}

	@Override
	public String getName() {
		return standardName;
	}

	@Override
	public SectionHeader getHeader() {
		return header;
	}

	@Override
	public byte[] getLoadableContent() {
		int bytesNeeded = (2 + 2 + 4 + 4 + 4) * entries.length;
		for (final GnuVersionDefinitionEntry gvre : entries) {
			bytesNeeded += (4 + 2 + 2 + 4 + 4) * gvre.getAuxiliaryLength();
		}
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(bytesNeeded);
		for (final GnuVersionDefinitionEntry gvre : entries) {
			wb.write(gvre.getVersion());
			wb.write(gvre.getFlags());
			wb.write(gvre.getVersionIndex());
			wb.write(gvre.getAuxiliaryLength());
			wb.write(gvre.getHash());
			wb.write(gvre.getAuxOffset());
			wb.write(gvre.getNextOffset());

			for (int i = 0; i < gvre.getAuxiliaryLength(); i++) {
				final GnuVersionDefinitionAuxiliaryEntry aux = gvre.getAuxiliary(i);
				wb.write(aux.nameOffset());
				wb.write(aux.nextOffset());
			}
		}
		return wb.array();
	}

	@Override
	public String toString() {
		return "GnuVersionDefinitionSection(header=" + header + ";entries=" + Arrays.toString(entries) + ")";
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
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		final GnuVersionDefinitionSection gvds = (GnuVersionDefinitionSection) other;
		return this.header.equals(gvds.header) && Arrays.equals(this.entries, gvds.entries);
	}
}
