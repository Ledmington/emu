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
package com.ledmington.elf.section;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import com.ledmington.utils.HashUtils;

/** This class is just a data holder. No check is performed in the constructor on the given data. */
public final class SectionHeader {

	private final int nameOffset;
	private final SectionHeaderType type;
	private final Set<SectionHeaderFlags> flags;
	private final long virtualAddress;
	private final long fileOffset;
	private final long sectionSize;
	private final int linkedSectionIndex;
	private final int info;
	private final long alignment;
	private final long entrySize;

	/**
	 * Creates a new section header with the given data. No check is performed except the non-null ones.
	 *
	 * @param nameOffset The offset of the name of this section in the string table.
	 * @param type The type of this section.
	 * @param flags Miscellaneous flags.
	 * @param virtualAddress The virtual memory address where to load this section.
	 * @param fileOffset The offset in the file where this section starts.
	 * @param sectionSize The size in bytes of the section in the file.
	 * @param linkedSectionIndex The index in the section table of the linked section.
	 * @param sh_info Miscellaneous info.
	 * @param alignment Byte-alignment for parsing.
	 * @param entrySize The size in bytes of each entry of this section.
	 */
	public SectionHeader(
			int nameOffset,
			SectionHeaderType type,
			long flags,
			long virtualAddress,
			long fileOffset,
			long sectionSize,
			int linkedSectionIndex,
			int sh_info,
			long alignment,
			long entrySize) {
		this.nameOffset = nameOffset;
		this.type = type;
		this.flags = Objects.requireNonNull(SectionHeaderFlags.fromLong(flags));
		this.virtualAddress = virtualAddress;
		this.fileOffset = fileOffset;
		this.sectionSize = sectionSize;
		this.linkedSectionIndex = linkedSectionIndex;
		this.info = sh_info;
		this.alignment = alignment;
		this.entrySize = entrySize;
	}

	/**
	 * Returns the offset of the name of the section in the string table.
	 *
	 * @return The offset where the name of this section starts in the string table.
	 */
	public int getNameOffset() {
		return nameOffset;
	}

	/**
	 * The offset in the file where the section starts.
	 *
	 * @return The 64-bit offset of the section in the file.
	 */
	public long getFileOffset() {
		return fileOffset;
	}

	/**
	 * Size in bytes of the section. This is the amount of space occupied in the file, except for SHT_NO_BITS sections.
	 *
	 * @return Number of bytes occupied by this section in the file.
	 */
	public long getSectionSize() {
		return sectionSize;
	}

	/**
	 * Returns the type of this section.
	 *
	 * @return The type of this section.
	 */
	public SectionHeaderType getType() {
		return type;
	}

	/**
	 * Returns the set of flags of this section.
	 *
	 * @return The set of flags of this section.
	 */
	public Set<SectionHeaderFlags> getFlags() {
		return new TreeSet<>(flags);
	}

	/**
	 * Returns the size in bytes of each entry. Returns 0 if the section does not hold a table of fixed-size entries.
	 *
	 * @return The size in bytes in the file of each entry of this section.
	 */
	public long getEntrySize() {
		return entrySize;
	}

	/**
	 * Returns the virtual address of the beginning of the section in memory. If the section is not allocated to the
	 * memory image of the program, this field should be zero.
	 *
	 * @return The virtual address where to load this section in memory.
	 */
	public long getVirtualAddress() {
		return virtualAddress;
	}

	/**
	 * Alignment in bytes of this section.
	 *
	 * @return Alignment in bytes of this section.
	 */
	public long getAlignment() {
		return alignment;
	}

	/**
	 * Returns the index of the linked section in the section table.
	 *
	 * @return The index of the linked section.
	 */
	public int getLinkedSectionIndex() {
		return linkedSectionIndex;
	}

	/**
	 * Returns the 32-bit info of this section.
	 *
	 * @return The miscellaneous info of this section.
	 */
	public int getInfo() {
		return info;
	}

	@Override
	public String toString() {
		return "SectionHeader(nameOffset=" + nameOffset + ";type=" + type + ";flags=" + flags
				+ ";virtualAddress="
				+ virtualAddress + ";fileOffset=" + fileOffset + ";size="
				+ sectionSize + ";linkedSectionIndex=" + linkedSectionIndex + ";info=" + info + ";alignment="
				+ alignment + ";entrySize=" + entrySize + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + nameOffset;
		h = 31 * h + type.hashCode();
		h = 31 * h + flags.hashCode();
		h = 31 * h + HashUtils.hash(virtualAddress);
		h = 31 * h + HashUtils.hash(fileOffset);
		h = 31 * h + HashUtils.hash(sectionSize);
		h = 31 * h + linkedSectionIndex;
		h = 31 * h + info;
		h = 31 * h + HashUtils.hash(alignment);
		h = 31 * h + HashUtils.hash(entrySize);
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
		final SectionHeader sh = (SectionHeader) other;
		return this.nameOffset == sh.nameOffset
				&& this.type.equals(sh.type)
				&& this.flags.equals(sh.flags)
				&& this.virtualAddress == sh.virtualAddress
				&& this.fileOffset == sh.fileOffset
				&& this.sectionSize == sh.sectionSize
				&& this.linkedSectionIndex == sh.linkedSectionIndex
				&& this.info == sh.info
				&& this.alignment == sh.alignment
				&& this.entrySize == sh.entrySize;
	}
}
