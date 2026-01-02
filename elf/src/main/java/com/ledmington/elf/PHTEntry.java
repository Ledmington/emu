/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.elf;

import java.util.Objects;

/**
 * An entry of an ELF Program Header Table.
 *
 * @param type The type of this entry.
 * @param flags Miscellaneous flags.
 * @param segmentFileOffset The segment's offset in file.
 * @param segmentVirtualAddress The virtual address where to load this segment in memory.
 * @param segmentPhysicalAddress The physical address where to load this segment in memory.
 * @param segmentFileSize The size in bytes of this segment on the file.
 * @param segmentMemorySize The size in bytes of this segment in memory.
 * @param alignment Byte alignment.
 */
public record PHTEntry(
		PHTEntryType type,
		int flags,
		long segmentFileOffset,
		long segmentVirtualAddress,
		long segmentPhysicalAddress,
		long segmentFileSize,
		long segmentMemorySize,
		long alignment) {

	/**
	 * Default constructor.
	 *
	 * @param type The type of this entry.
	 * @param flags Miscellaneous flags.
	 * @param segmentFileOffset The segment's offset in file.
	 * @param segmentVirtualAddress The virtual address where to load this segment in memory.
	 * @param segmentPhysicalAddress The physical address where to load this segment in memory.
	 * @param segmentFileSize The size in bytes of this segment on the file.
	 * @param segmentMemorySize The size in bytes of this segment in memory.
	 * @param alignment Byte alignment.
	 */
	public PHTEntry {
		Objects.requireNonNull(type);

		if ((flags & ~(PHTEntryFlags.PF_R.getCode() | PHTEntryFlags.PF_W.getCode() | PHTEntryFlags.PF_X.getCode()))
				!= 0) {
			throw new IllegalArgumentException(String.format("Invalid PHT Entry flags 0x%08x", flags));
		}
	}

	/**
	 * Checks whether this entry contains the PF_R flag.
	 *
	 * @return True if this entry contains the PF_R flag, false otherwise.
	 */
	public boolean isReadable() {
		return (flags & PHTEntryFlags.PF_R.getCode()) != 0;
	}

	/**
	 * Checks whether this entry contains the PF_W flag.
	 *
	 * @return True if this entry contains the PF_W flag, false otherwise.
	 */
	public boolean isWriteable() {
		return (flags & PHTEntryFlags.PF_W.getCode()) != 0;
	}

	/**
	 * Checks whether this entry contains the PF_X flag.
	 *
	 * @return True if this entry contains the PF_X flag, false otherwise.
	 */
	public boolean isExecutable() {
		return (flags & PHTEntryFlags.PF_X.getCode()) != 0;
	}

	@Override
	public String toString() {
		return "PHTEntry(type=" + type + ";readable="
				+ isReadable() + ";writeable="
				+ isWriteable() + ";executable="
				+ isExecutable() + ";segmentFileOffset="
				+ segmentFileOffset + ";segmentVirtualAddress="
				+ segmentVirtualAddress + ";segmentPhysicalAddress="
				+ segmentPhysicalAddress + ";segmentFileSize="
				+ segmentFileSize + ";segmentMemorySize="
				+ segmentMemorySize + ";alignment="
				+ alignment + ')';
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + type.hashCode();
		h = 31 * h + flags;
		h = 31 * h + Long.hashCode(segmentFileOffset);
		h = 31 * h + Long.hashCode(segmentVirtualAddress);
		h = 31 * h + Long.hashCode(segmentPhysicalAddress);
		h = 31 * h + Long.hashCode(segmentFileSize);
		h = 31 * h + Long.hashCode(segmentMemorySize);
		h = 31 * h + Long.hashCode(alignment);
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
		if (!(other
				instanceof
				PHTEntry(
						final PHTEntryType type1,
						final int flags1,
						final long fileOffset,
						final long virtualAddress,
						final long physicalAddress,
						final long fileSize,
						final long memorySize,
						final long alignment1))) {
			return false;
		}
		return this.type == type1
				&& this.flags == flags1
				&& this.segmentFileOffset == fileOffset
				&& this.segmentVirtualAddress == virtualAddress
				&& this.segmentPhysicalAddress == physicalAddress
				&& this.segmentFileSize == fileSize
				&& this.segmentMemorySize == memorySize
				&& this.alignment == alignment1;
	}
}
