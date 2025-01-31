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
 * The .gnu.version_r ELF section.
 *
 * <p>Reference <a href= "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.4.
 */
public final class GnuVersionRequirementsSection implements LoadableSection {

	private static final String standardName = ".gnu.version_r";

	private final SectionHeader header;
	private final GnuVersionRequirementEntry[] entries;

	/**
	 * Returns the standard name of this special section.
	 *
	 * @return The string ".gnu.version_r".
	 */
	public static String getStandardName() {
		return standardName;
	}

	/**
	 * Creates the GNU version requirements section with the given header.
	 *
	 * @param sectionHeader The header for this section.
	 * @param b The {@link ReadOnlyByteBuffer} to read data from.
	 * @param dynamicSection The Dynamic section of the ELF file to retrieve the value of DT_VERNEEDNUM from.
	 */
	public GnuVersionRequirementsSection(
			final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final DynamicSection dynamicSection) {
		this.header = Objects.requireNonNull(sectionHeader);

		int versionRequirementsEntryNum = 0;
		{
			Objects.requireNonNull(dynamicSection);
			for (int i = 0; i < dynamicSection.getTableLength(); i++) {
				if (dynamicSection.getEntry(i).getTag() == DynamicTableEntryTag.DT_VERNEEDNUM) {
					versionRequirementsEntryNum =
							BitUtils.asInt(dynamicSection.getEntry(i).getContent());
					break;
				}
			}
		}

		final long oldAlignment = b.getAlignment();
		b.setAlignment(1L);
		b.setPosition(sectionHeader.getFileOffset());

		this.entries = new GnuVersionRequirementEntry[versionRequirementsEntryNum];
		for (int i = 0; i < this.entries.length; i++) {
			final long entryStart = b.getPosition();
			this.entries[i] = new GnuVersionRequirementEntry(b);

			b.setPosition(entryStart + BitUtils.asLong(entries[i].getNextOffset()));
		}

		b.setAlignment(oldAlignment);
	}

	/**
	 * Returns the number of version requirements in this section.
	 *
	 * @return The number of version requirements.
	 */
	public int getRequirementsLength() {
		return entries.length;
	}

	/**
	 * Returns the i-th version requirement.
	 *
	 * @param idx The index of the version requirement to retrieve.
	 * @return The i-th version requirement.
	 */
	public GnuVersionRequirementEntry getEntry(final int idx) {
		return entries[idx];
	}

	/**
	 * Returns the name offset of the auxiliary entry with the given version.
	 *
	 * @param version The version to look for.
	 * @return The name offset of the entry.
	 */
	public int getVersionNameOffset(final short version) {
		for (final GnuVersionRequirementEntry gvre : entries) {
			for (int i = 0; i < gvre.getAuxiliaryLength(); i++) {
				if (gvre.getAuxiliary(i).other() == version) {
					return gvre.getAuxiliary(i).nameOffset();
				}
			}
		}
		return -1;
	}

	/**
	 * Returns the 'other' field of the auxiliary entry with the given version.
	 *
	 * @param version The version to look for.
	 * @return The 'other' field of the entry.
	 */
	public short getVersion(final short version) {
		for (final GnuVersionRequirementEntry gvre : entries) {
			for (int i = 0; i < gvre.getAuxiliaryLength(); i++) {
				if (gvre.getAuxiliary(i).other() == version) {
					return gvre.getAuxiliary(i).other();
				}
			}
		}
		return -1;
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
		for (final GnuVersionRequirementEntry gvre : entries) {
			bytesNeeded += (4 + 2 + 2 + 4 + 4) * gvre.getAuxiliaryLength();
		}
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(bytesNeeded);
		for (final GnuVersionRequirementEntry gvre : entries) {
			wb.write(gvre.getVersion());
			wb.write(gvre.getCount());
			wb.write(gvre.getFileOffset());
			wb.write(gvre.getAuxOffset());
			wb.write(gvre.getNextOffset());

			for (int i = 0; i < gvre.getAuxiliaryLength(); i++) {
				final GnuVersionRequirementAuxiliaryEntry aux = gvre.getAuxiliary(i);
				wb.write(aux.hash());
				wb.write(aux.flags());
				wb.write(aux.other());
				wb.write(aux.nameOffset());
				wb.write(aux.nextOffset());
			}
		}
		return wb.array();
	}

	@Override
	public String toString() {
		return "GnuVersionRequirementsSection(header=" + header + ";entries=" + Arrays.toString(entries) + ")";
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
		final GnuVersionRequirementsSection gvrs = (GnuVersionRequirementsSection) other;
		return this.header.equals(gvrs.header) && Arrays.equals(this.entries, gvrs.entries);
	}
}
