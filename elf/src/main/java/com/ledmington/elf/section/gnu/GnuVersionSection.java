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

import com.ledmington.elf.section.LoadableSection;
import com.ledmington.elf.section.SectionHeader;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/**
 * The .gnu.version ELF section.
 *
 * <p>Reference <a href= "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 * Paragraph 2.7.2.
 */
public final class GnuVersionSection implements LoadableSection {

	private static final String standardName = ".gnu.version";

	/**
	 * Returns the standard name of this section.
	 *
	 * @return The string ".gnu.version".
	 */
	public static String getStandardName() {
		return standardName;
	}

	private final SectionHeader header;
	private final short[] versions;

	/**
	 * Creates a GNU Version section with the given header and parses the content from the ReadOnlyByteBuffer b.
	 *
	 * @param sectionHeader The header of this section.
	 * @param b The ReadOnlyByteBuffer to read the contents from.
	 */
	public GnuVersionSection(final SectionHeader sectionHeader, final ReadOnlyByteBuffer b) {
		this.header = Objects.requireNonNull(sectionHeader);

		b.setPosition(sectionHeader.getFileOffset());
		final int nEntries = BitUtils.asInt(sectionHeader.getSectionSize() / 2L);
		this.versions = new short[nEntries];
		for (int i = 0; i < nEntries; i++) {
			versions[i] = b.read2();
		}
	}

	/**
	 * Returns the number of versions contained in this section.
	 *
	 * <p>Note: This number is the same as the number of entries in the '.dynsym' section.
	 *
	 * @return The number of versions contained in this section.
	 */
	public int getVersionsLength() {
		return versions.length;
	}

	/**
	 * Returns the i-th version in this section.
	 *
	 * @param idx The index of the version to find.
	 * @return The i-th version in this section.
	 */
	public short getVersion(final int idx) {
		return versions[idx];
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
		final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(versions.length * 2);
		for (final short version : versions) {
			bb.write(version);
		}
		return bb.array();
	}

	@Override
	public String toString() {
		return "GnuVersionSection(header=" + header + ";versions=" + Arrays.toString(versions) + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + header.hashCode();
		h = 31 * h + Arrays.hashCode(versions);
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
		final GnuVersionSection gvs = (GnuVersionSection) other;
		return this.header.equals(gvs.header) && Arrays.equals(this.versions, gvs.versions);
	}
}
