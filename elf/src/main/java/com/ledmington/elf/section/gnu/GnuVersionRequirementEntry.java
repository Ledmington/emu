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

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;

/**
 * An entry of the .gnu.version_r section. Usually this structure is called Elfxx_Verneed in the ELF documentation.
 *
 * <p>Useful reference <a href=
 * "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 */
public final class GnuVersionRequirementEntry {

	private final short version;
	private final int fileOffset;
	private final int auxOffset;
	private final int nextOffset;
	private final GnuVersionRequirementAuxiliaryEntry[] aux;

	/**
	 * Creates a new Verneed class with the given data.
	 *
	 * @param b The ReadOnlyByteBuffer to read data from.
	 */
	public GnuVersionRequirementEntry(final ReadOnlyByteBuffer b) {
		final long start = b.getPosition();

		this.version = b.read2();
		final short count = b.read2();
		this.fileOffset = b.read4();
		this.auxOffset = b.read4();
		this.nextOffset = b.read4();

		if (version == 0) {
			throw new IllegalArgumentException("Invalid Vernaux version: expected >=1 but was 0");
		}

		final short minimumExpectedAuxEntries = 1;
		if (count < minimumExpectedAuxEntries) {
			throw new IllegalArgumentException(String.format(
					"Invalid number of Vernaux entries: expected at least 1 but was %,d (0x%04x)", count, count));
		}

		this.aux = new GnuVersionRequirementAuxiliaryEntry[count];

		final long oldAlignment = b.getAlignment();
		b.setAlignment(1L);
		b.setPosition(start + BitUtils.asLong(auxOffset));

		for (int i = 0; i < count; i++) {
			final long entryStart = b.getPosition();
			final int vna_hash = b.read4();
			final short vna_flags = b.read2();
			final short vna_other = b.read2();
			final int vna_name = b.read4();
			final int vna_next = b.read4();
			this.aux[i] = new GnuVersionRequirementAuxiliaryEntry(vna_hash, vna_flags, vna_other, vna_name, vna_next);

			b.setPosition(entryStart + BitUtils.asLong(vna_next));
		}
		b.setAlignment(oldAlignment);
	}

	/**
	 * Version of structure. This value is currently set to 1, and will be reset if the versioning implementation is
	 * incompatibly altered.
	 *
	 * @return The version of this structure.
	 */
	public short getVersion() {
		return version;
	}

	/**
	 * Returns the offset to the file name string in the section header, in bytes.
	 *
	 * @return The offset to the file name string in the section header, in bytes.
	 */
	public int getFileOffset() {
		return fileOffset;
	}

	/**
	 * Returns the offset to a corresponding entry in the vernaux array, in bytes.
	 *
	 * @return The offset to a corresponding entry in the vernaux array, in bytes.
	 */
	public int getAuxOffset() {
		return auxOffset;
	}

	/**
	 * Returns the offset to the next verneed entry, in bytes.
	 *
	 * @return The offset to the next verneed entry, in bytes.
	 */
	public int getNextOffset() {
		return nextOffset;
	}

	/**
	 * Returns the number of associated verneed array entries.
	 *
	 * @return The number of associated verneed array entries.
	 */
	public short getCount() {
		return BitUtils.asShort(aux.length);
	}

	/**
	 * Returns the number of associated verneed array entries.
	 *
	 * @return The number of associated verneed array entries.
	 */
	public int getAuxiliaryLength() {
		return aux.length;
	}

	/**
	 * Returns the i-th aux entry.
	 *
	 * @param idx The index of the entry to return.
	 * @return The i-th aux entry.
	 */
	public GnuVersionRequirementAuxiliaryEntry getAuxiliary(final int idx) {
		return aux[idx];
	}

	@Override
	public String toString() {
		return "GnuVersionRequirementEntry(version=" + version + ";count="
				+ aux.length + ";fileOffset="
				+ fileOffset + ";auxOffset="
				+ auxOffset + ";nextOffset="
				+ nextOffset + ";aux="
				+ Arrays.toString(aux) + ')';
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + HashUtils.hash(version);
		h = 31 * h + fileOffset;
		h = 31 * h + auxOffset;
		h = 31 * h + nextOffset;
		h = 31 * h + Arrays.hashCode(aux);
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
		if (!(other instanceof GnuVersionRequirementEntry gvre)) {
			return false;
		}
		return this.version == gvre.version
				&& this.fileOffset == gvre.fileOffset
				&& this.auxOffset == gvre.auxOffset
				&& this.nextOffset == gvre.nextOffset
				&& Arrays.equals(this.aux, gvre.aux);
	}
}
