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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/** An ELF .dynamic section. */
public final class DynamicSection implements LoadableSection {

	private final String name;
	private final SectionHeader header;
	private final boolean is32Bit;
	private final DynamicTableEntry[] dynamicTable;

	/**
	 * Creates a DynamicSection with the given name and header by parsing bytes read from the ReadOnlyByteBuffer.
	 *
	 * @param name The name of this section.
	 * @param sectionHeader The header of this section.
	 * @param b The buffer to read bytes from.
	 * @param is32Bit Used for alignment.
	 */
	public DynamicSection(
			final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
		this.name = Objects.requireNonNull(name);
		this.header = Objects.requireNonNull(sectionHeader);
		this.is32Bit = is32Bit;
		b.setPosition(sectionHeader.getFileOffset());
		final int entrySize = is32Bit ? 8 : 16;
		final int nEntries = (int) sectionHeader.getSectionSize() / entrySize;

		final List<DynamicTableEntry> tmp = new ArrayList<>(nEntries);
		int i = 0;
		while (i < nEntries) {
			long tag = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
			long content = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
			tmp.add(new DynamicTableEntry(tag, content));
			if (tmp.getLast().getTag().equals(DynamicTableEntryTag.DT_NULL)) {
				break;
			}
			i++;
		}

		dynamicTable = tmp.toArray(new DynamicTableEntry[0]);
	}

	/**
	 * Returns the number of entries in the dynamic section.
	 *
	 * @return The number of entries in the dynamic section.
	 */
	public int getTableLength() {
		return dynamicTable.length;
	}

	/**
	 * Returns the i-th entry in the dynamic section.
	 *
	 * @param idx The index of the entry to retrieve.
	 * @return The i-th entry of the dynamic section.
	 */
	public DynamicTableEntry getEntry(final int idx) {
		return dynamicTable[idx];
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
	public byte[] getLoadableContent() {
		final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(dynamicTable.length * (is32Bit ? 8 : 16));
		for (final DynamicTableEntry dynamicTableEntry : dynamicTable) {
			if (is32Bit) {
				bb.write(BitUtils.asInt(dynamicTableEntry.getTag().getCode()));
				bb.write(BitUtils.asInt(dynamicTableEntry.getContent()));
			} else {
				bb.write(dynamicTableEntry.getTag().getCode());
				bb.write(dynamicTableEntry.getContent());
			}
		}
		return bb.array();
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + name.hashCode();
		h = 31 * h + header.hashCode();
		h = 31 * h + HashUtils.hash(is32Bit);
		h = 31 * h + Arrays.hashCode(dynamicTable);
		return h;
	}

	@Override
	public String toString() {
		return "DynamicSection(name=" + name + ";header=" + header + ";is32Bit=" + is32Bit + ";dynamicTable="
				+ Arrays.toString(dynamicTable) + ")";
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
		final DynamicSection ds = (DynamicSection) other;
		return this.name.equals(ds.name)
				&& this.header.equals(ds.header)
				&& this.is32Bit == ds.is32Bit
				&& Arrays.equals(this.dynamicTable, ds.dynamicTable);
	}
}
