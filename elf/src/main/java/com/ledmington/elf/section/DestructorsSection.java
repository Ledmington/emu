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

import java.util.Arrays;
import java.util.Objects;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBufferV1;

/** An ELF .fini_array section. */
public final class DestructorsSection implements LoadableSection {

	private final String name;
	private final SectionHeader header;
	private final long[] destructors; // function pointers
	private final boolean is32Bit;

	/**
	 * Creates a DestructorsSection with the given name and the given header.
	 *
	 * @param name The name of this section.
	 * @param sectionHeader The header of this section.
	 * @param b The {@link ReadOnlyByteBuffer} to read data from.
	 * @param dynamicSection The Dynamic section of the ELF file to retrieve the value of DT_FINI_ARRAYSZ from.
	 * @param is32Bit Used for alignment.
	 */
	public DestructorsSection(
			final String name,
			final SectionHeader sectionHeader,
			final ReadOnlyByteBuffer b,
			final DynamicSection dynamicSection,
			final boolean is32Bit) {
		this.name = Objects.requireNonNull(name);
		this.header = Objects.requireNonNull(sectionHeader);
		this.is32Bit = is32Bit;

		if (dynamicSection == null) {
			this.destructors = new long[0];
			return;
		}

		int destructorsSizeInBytes = 0; // bytes
		{
			for (int i = 0; i < dynamicSection.getTableLength(); i++) {
				if (dynamicSection.getEntry(i).getTag() == DynamicTableEntryTag.DT_FINI_ARRAYSZ) {
					destructorsSizeInBytes =
							BitUtils.asInt(dynamicSection.getEntry(i).getContent());
					break;
				}
			}
		}

		final int wordSize = is32Bit ? 4 : 8;
		if (destructorsSizeInBytes % wordSize != 0) {
			throw new IllegalArgumentException(String.format(
					"Expected size of .fini_array section to be a multiple of %d bytes but was %d (0x%x)",
					wordSize, destructorsSizeInBytes, destructorsSizeInBytes));
		}

		b.setPosition(sectionHeader.getFileOffset());
		b.setAlignment(sectionHeader.getAlignment());

		this.destructors = new long[destructorsSizeInBytes / wordSize];
		for (int i = 0; i < destructors.length; i++) {
			this.destructors[i] = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
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

	/**
	 * Returns the number of destructors.
	 *
	 * @return The number of destructors.
	 */
	public int getNumDestructors() {
		return destructors.length;
	}

	/**
	 * Returns the i-th destructor.
	 *
	 * @param idx The index of the destructor to return.
	 * @return The i-th destructor.
	 */
	public long getDestructor(final int idx) {
		return destructors[idx];
	}

	@Override
	public byte[] getLoadableContent() {
		final int wordSize = is32Bit ? 4 : 8;
		final WriteOnlyByteBuffer wb = new WriteOnlyByteBufferV1(destructors.length * wordSize);
		for (final long d : destructors) {
			if (is32Bit) {
				wb.write(BitUtils.asInt(d));
			} else {
				wb.write(d);
			}
		}
		return wb.array();
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + name.hashCode();
		h = 31 * h + header.hashCode();
		h = 31 * h + Arrays.hashCode(destructors);
		return h;
	}

	@Override
	public String toString() {
		return "DestructorsSection(name=" + name + ";header=" + header + ";destructors=" + Arrays.toString(destructors)
				+ ")";
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (this == other) {
			return true;
		}
		if (!(other instanceof final DestructorsSection ds)) {
			return false;
		}
		return this.name.equals(ds.name)
				&& this.header.equals(ds.header)
				&& Arrays.equals(this.destructors, ds.destructors);
	}
}
