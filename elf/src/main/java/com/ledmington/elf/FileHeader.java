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

/**
 * The header of an ELF file. This class is just a data holder. No check is performed in the constructor on the given
 * data.
 *
 * @param is32Bit Used to differentiate between ELF32 and ELF64 versions.
 * @param isLittleEndian The endianness for the binary representation of this file header.
 * @param version The version of the ELF file.
 * @param osabi The operating system ABI.
 * @param ABIVersion The (minimum?) version of the ABI supported.
 * @param fileType The type of this file.
 * @param isa The ISA of the code contained in ths file.
 * @param entryPointVirtualAddress The virtual memory address where to start execution.
 * @param programHeaderTableOffset The offset in the file where the PH table is stored.
 * @param sectionHeaderTableOffset The offset in the file where the SH table is stored.
 * @param flags Miscellaneous flags.
 * @param headerSize The size in bytes of this header on file.
 * @param programHeaderTableEntrySize The size in bytes of each PHT entry.
 * @param numProgramHeaderTableEntries The number of PHT entries.
 * @param sectionHeaderTableEntrySize The size in bytes of each SHT entry.
 * @param numSectionHeaderTableEntries The number of SHT entries.
 * @param sectionHeaderStringTableIndex The index of the String Table in the SHT.
 */
public record FileHeader(
		boolean is32Bit,
		boolean isLittleEndian,
		byte version,
		OSABI osabi,
		byte ABIVersion,
		FileType fileType,
		ISA isa,
		long entryPointVirtualAddress,
		long programHeaderTableOffset,
		long sectionHeaderTableOffset,
		int flags,
		short headerSize,
		short programHeaderTableEntrySize,
		short numProgramHeaderTableEntries,
		short sectionHeaderTableEntrySize,
		short numSectionHeaderTableEntries,
		short sectionHeaderStringTableIndex) {

	/**
	 * Default constructor.
	 *
	 * @param is32Bit Used to differentiate between ELF32 and ELF64 versions.
	 * @param isLittleEndian The endianness for the binary representation of this file header.
	 * @param version The version of the ELF file.
	 * @param osabi The operating system ABI.
	 * @param ABIVersion The (minimum?) version of the ABI supported.
	 * @param fileType The type of this file.
	 * @param isa The ISA of the code contained in ths file.
	 * @param entryPointVirtualAddress The virtual memory address where to start execution.
	 * @param programHeaderTableOffset The offset in the file where the PH table is stored.
	 * @param sectionHeaderTableOffset The offset in the file where the SH table is stored.
	 * @param flags Miscellaneous flags.
	 * @param headerSize The size in bytes of this header on file.
	 * @param programHeaderTableEntrySize The size in bytes of each PHT entry.
	 * @param numProgramHeaderTableEntries The number of PHT entries.
	 * @param sectionHeaderTableEntrySize The size in bytes of each SHT entry.
	 * @param numSectionHeaderTableEntries The number of SHT entries.
	 * @param sectionHeaderStringTableIndex The index of the String Table in the SHT.
	 */
	public FileHeader {
		Objects.requireNonNull(osabi);
		Objects.requireNonNull(fileType);
		Objects.requireNonNull(isa);
	}

	@Override
	public String toString() {
		return "FileHeader(is32Bit=" + is32Bit + ";isLittleEndian="
				+ isLittleEndian + ";version="
				+ version + ";OSABI="
				+ osabi + ";ABIVersion="
				+ ABIVersion + ";fileType="
				+ fileType + ";isa="
				+ isa + ";entryPointVirtualAddress="
				+ entryPointVirtualAddress + ";programHeaderTableOffset="
				+ programHeaderTableOffset + ";sectionHeaderTableOffset="
				+ sectionHeaderTableOffset + ";flags="
				+ flags + ";headerSize="
				+ headerSize + ";programHeaderTableEntrySize="
				+ programHeaderTableEntrySize + ";numProgramHeaderTableEntries="
				+ numProgramHeaderTableEntries + ";sectionHeaderTableEntrySize="
				+ sectionHeaderTableEntrySize + ";numSectionHeaderTableEntries="
				+ numSectionHeaderTableEntries + ";sectionHeaderStringTableIndex="
				+ sectionHeaderStringTableIndex + ')';
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + Boolean.hashCode(is32Bit);
		h = 31 * h + Boolean.hashCode(isLittleEndian);
		h = 31 * h + Byte.hashCode(version);
		h = 31 * h + osabi.hashCode();
		h = 31 * h + Byte.hashCode(ABIVersion);
		h = 31 * h + fileType.hashCode();
		h = 31 * h + isa.hashCode();
		h = 31 * h + Long.hashCode(entryPointVirtualAddress);
		h = 31 * h + Long.hashCode(programHeaderTableOffset);
		h = 31 * h + Long.hashCode(sectionHeaderTableOffset);
		h = 31 * h + flags;
		h = 31 * h + Short.hashCode(headerSize);
		h = 31 * h + Short.hashCode(programHeaderTableEntrySize);
		h = 31 * h + Short.hashCode(numProgramHeaderTableEntries);
		h = 31 * h + Short.hashCode(sectionHeaderTableEntrySize);
		h = 31 * h + Short.hashCode(numSectionHeaderTableEntries);
		h = 31 * h + Short.hashCode(sectionHeaderStringTableIndex);
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
		if (!(other instanceof final FileHeader fh)) {
			return false;
		}
		return this.is32Bit == fh.is32Bit
				&& this.isLittleEndian == fh.isLittleEndian
				&& this.version == fh.version
				&& this.osabi.equals(fh.osabi)
				&& this.ABIVersion == fh.ABIVersion
				&& this.fileType.equals(fh.fileType)
				&& this.isa.equals(fh.isa)
				&& this.entryPointVirtualAddress == fh.entryPointVirtualAddress
				&& this.programHeaderTableOffset == fh.programHeaderTableOffset
				&& this.sectionHeaderTableOffset == fh.sectionHeaderTableOffset
				&& this.flags == fh.flags
				&& this.headerSize == fh.headerSize
				&& this.programHeaderTableEntrySize == fh.programHeaderTableEntrySize
				&& this.numProgramHeaderTableEntries == fh.numProgramHeaderTableEntries
				&& this.sectionHeaderTableEntrySize == fh.sectionHeaderTableEntrySize
				&& this.numSectionHeaderTableEntries == fh.numSectionHeaderTableEntries
				&& this.sectionHeaderStringTableIndex == fh.sectionHeaderStringTableIndex;
	}
}
