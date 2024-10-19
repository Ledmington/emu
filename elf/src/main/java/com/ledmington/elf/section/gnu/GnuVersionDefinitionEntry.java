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
package com.ledmington.elf.section.gnu;

import java.util.Arrays;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;

/** An entry of the .gnu.version_d section. Usually this structure is called Elfxx_Verdef in the ELF documentation. */
public final class GnuVersionDefinitionEntry {

	private final short version;
	private final short flags;
	private final short versionIndex;
	private final int hash;
	private final int auxOffset;
	private final int nextOffset;
	private final GnuVersionDefinitionAuxiliaryEntry[] aux;

	/**
	 * Creates a new Verdef class with the given data.
	 *
	 * @param b The ReadOnlyByteBuffer to read data from.
	 */
	public GnuVersionDefinitionEntry(final ReadOnlyByteBuffer b) {
		final long start = b.getPosition();

		this.version = b.read2();

		final short expectedVersion = 1;
		if (version != expectedVersion) {
			throw new IllegalArgumentException(String.format(
					"Invalid Verdef version: expected %d (0x%04) but was %d (0x%04x)",
					expectedVersion, expectedVersion, version, version));
		}

		this.flags = b.read2();
		this.versionIndex = b.read2();
		final short auxCount = b.read2();
		this.hash = b.read4();
		this.auxOffset = b.read4();
		this.nextOffset = b.read4();

		this.aux = new GnuVersionDefinitionAuxiliaryEntry[BitUtils.asInt(auxCount)];

		final long oldAlignment = b.getAlignment();
		b.setAlignment(1L);
		b.setPosition(start + BitUtils.asLong(auxOffset));

		for (int i = 0; i < auxCount; i++) {
			final long entryStart = b.getPosition();
			final int vda_name = b.read4();
			final int vda_next = b.read4();
			this.aux[i] = new GnuVersionDefinitionAuxiliaryEntry(vda_name, vda_next);

			b.setPosition(entryStart + BitUtils.asLong(vda_next));
		}
		b.setAlignment(oldAlignment);
	}

	/**
	 * Version revision. This field shall be set to 1.
	 *
	 * @return The version of this structure.
	 */
	public short getVersion() {
		return version;
	}

	/**
	 * Version information flag bitmask.
	 *
	 * @return The flag bitmask of this structure.
	 */
	public short getFlags() {
		return flags;
	}

	/**
	 * Version index numeric value referencing the SHT_GNU_versym section.
	 *
	 * @return Index of the GNU_versym section.
	 */
	public short getVersionIndex() {
		return versionIndex;
	}

	/**
	 * Version name hash value (ELF hash function {@link com.ledmington.elf.section.HashTableSection#hash(byte[])}).
	 *
	 * @return The has value of this structure.
	 */
	public int getHash() {
		return hash;
	}

	/**
	 * Offset to the next verdef entry, in bytes.
	 *
	 * @return Offset the next verdef entry.
	 */
	public int getNextOffset() {
		return nextOffset;
	}

	/**
	 * Offset in bytes to a corresponding entry in an array of Elfxx_Verdaux structures.
	 *
	 * @return Offset in bytes to the first {@link GnuVersionDefinitionAuxiliaryEntry} structure.
	 */
	public int getAuxOffset() {
		return auxOffset;
	}

	/**
	 * Number of associated verdaux array entries.
	 *
	 * @return Number of auxiliary entries.
	 */
	public int getAuxiliaryLength() {
		return aux.length;
	}

	/**
	 * Returns the i-th auxliary entry.
	 *
	 * @param idx The index of the entry.
	 * @return The i-th entry.
	 */
	public GnuVersionDefinitionAuxiliaryEntry getAuxiliary(final int idx) {
		return aux[idx];
	}

	@Override
	public String toString() {
		return "GnuVersionDefinitionEntry(version=" + version + ";flags=" + flags + ";versionIndex=" + versionIndex
				+ ";count="
				+ aux.length
				+ ";hash=" + hash
				+ ";auxOffset="
				+ auxOffset + ";nextOffset="
				+ nextOffset + ";aux="
				+ Arrays.toString(aux) + ')';
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + HashUtils.hash(version);
		h = 31 * h + HashUtils.hash(flags);
		h = 31 * h + HashUtils.hash(versionIndex);
		h = 31 * h + aux.length;
		h = 31 + h + hash;
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
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		final GnuVersionDefinitionEntry gvde = (GnuVersionDefinitionEntry) other;
		return this.version == gvde.version
				&& this.flags == gvde.flags
				&& this.versionIndex == gvde.versionIndex
				&& this.hash == gvde.hash
				&& this.auxOffset == gvde.auxOffset
				&& this.nextOffset == gvde.nextOffset
				&& Arrays.equals(this.aux, gvde.aux);
	}
}
