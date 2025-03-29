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
package com.ledmington.elf;

import java.util.Objects;

import com.ledmington.utils.HashUtils;

/**
 * An entry of an ELF Program Header Table.
 *
 * @param type The type of this entry.
 * @param flags Miscellaneous flags.
 * @param segmentOffset The segment's offset in file.
 * @param segmentVirtualAddress The virtual address where to load this segment in memory.
 * @param segmentPhysicalAddress The physical address where to load this segment in memory.
 * @param segmentFileSize The size in bytes of this segment on the file.
 * @param segmentMemorySize The size in bytes of this segment in memory.
 * @param alignment Byte alignment.
 */
public record PHTEntry(
		PHTEntryType type,
		int flags,
		long segmentOffset,
		long segmentVirtualAddress,
		long segmentPhysicalAddress,
		long segmentFileSize,
		long segmentMemorySize,
		long alignment) {

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
				+ isExecutable() + ";segmentOffset="
				+ segmentOffset + ";segmentVirtualAddress="
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
		h = 31 * h + HashUtils.hash(segmentOffset);
		h = 31 * h + HashUtils.hash(segmentVirtualAddress);
		h = 31 * h + HashUtils.hash(segmentPhysicalAddress);
		h = 31 * h + HashUtils.hash(segmentFileSize);
		h = 31 * h + HashUtils.hash(segmentMemorySize);
		h = 31 * h + HashUtils.hash(alignment);
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
		if (!(other instanceof final PHTEntry phte)) {
			return false;
		}
		return this.type.equals(phte.type)
				&& this.flags == phte.flags
				&& this.segmentOffset == phte.segmentOffset
				&& this.segmentVirtualAddress == phte.segmentVirtualAddress
				&& this.segmentPhysicalAddress == phte.segmentPhysicalAddress
				&& this.segmentFileSize == phte.segmentFileSize
				&& this.segmentMemorySize == phte.segmentMemorySize
				&& this.alignment == phte.alignment;
	}
}
