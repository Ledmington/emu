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
 * The header of an ELF file. This class is just a data holder. No check is performed in the constructor on the given
 * data.
 */
public final class FileHeader {

	private final boolean bits;
	private final boolean endianness;
	private final byte version;
	private final OSABI osabi;
	private final byte ABIVersion;
	private final FileType fileType;
	private final ISA isa;
	private final long entryPointVirtualAddress;
	private final long programHeaderTableOffset;
	private final long sectionHeaderTableOffset;
	private final int flags;
	private final short headerSize;
	private final short programHeaderTableEntrySize;
	private final short nProgramHeaderTableEntries;
	private final short sectionHeaderTableEntrySize;
	private final short nSectionHeaderTableEntries;
	private final short shstrtab_index;

	/**
	 * Creates a FileHeader object with the given data.
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
	 * @param nProgramHeaderTableEntries The number of PHT entries.
	 * @param sectionHeaderTableEntrySize The size in bytes of each SHT entry.
	 * @param nSectionHeaderTableEntries The number of SHT entries.
	 * @param shstrtab_index The index of the String Table in the SHT.
	 */
	public FileHeader(
			final boolean is32Bit,
			final boolean isLittleEndian,
			final byte version,
			final OSABI osabi,
			final byte ABIVersion,
			final FileType fileType,
			final ISA isa,
			final long entryPointVirtualAddress,
			final long programHeaderTableOffset,
			final long sectionHeaderTableOffset,
			final int flags,
			final short headerSize,
			final short programHeaderTableEntrySize,
			final short nProgramHeaderTableEntries,
			final short sectionHeaderTableEntrySize,
			final short nSectionHeaderTableEntries,
			final short shstrtab_index) {
		this.bits = is32Bit;
		this.endianness = isLittleEndian;
		this.version = version;
		this.osabi = Objects.requireNonNull(osabi);
		this.ABIVersion = ABIVersion;
		this.fileType = Objects.requireNonNull(fileType);
		this.isa = Objects.requireNonNull(isa);
		this.entryPointVirtualAddress = entryPointVirtualAddress;
		this.programHeaderTableOffset = programHeaderTableOffset;
		this.sectionHeaderTableOffset = sectionHeaderTableOffset;
		this.flags = flags;
		this.headerSize = headerSize;
		this.programHeaderTableEntrySize = programHeaderTableEntrySize;
		this.nProgramHeaderTableEntries = nProgramHeaderTableEntries;
		this.sectionHeaderTableEntrySize = sectionHeaderTableEntrySize;
		this.nSectionHeaderTableEntries = nSectionHeaderTableEntries;
		this.shstrtab_index = shstrtab_index;
	}

	/**
	 * Returns the mode this file header was encoded with.
	 *
	 * @return True for 32-bit, false for 64-bit.
	 */
	public boolean is32Bit() {
		return bits;
	}

	/**
	 * Returns the endianness this file header was encoded with.
	 *
	 * @return True for little-endian, false for big-endian.
	 */
	public boolean isLittleEndian() {
		return endianness;
	}

	/**
	 * Returns the operating system ABI used to generate this ELF file header.
	 *
	 * @return The operating system ABI.
	 */
	public OSABI getOSABI() {
		return osabi;
	}

	/**
	 * Returns the (minimum?) supported version of the ABI.
	 *
	 * @return The version of the ABI.
	 */
	public byte getABIVersion() {
		return ABIVersion;
	}

	/**
	 * Returns the ISA used to generate this ELF file.
	 *
	 * @return The ISA of this file.
	 */
	public ISA getISA() {
		return isa;
	}

	/**
	 * Returns the version of the ELF format used.
	 *
	 * @return The ELF version.
	 */
	public int getVersion() {
		return 1;
	}

	/**
	 * Returns the miscellaneous flags of the file header.
	 *
	 * @return The file header flags.
	 */
	public int getFlags() {
		return flags;
	}

	/**
	 * Returns the number of PHT entries in the file.
	 *
	 * @return The number of PHT entries.
	 */
	public short getNumProgramHeaderTableEntries() {
		return nProgramHeaderTableEntries;
	}

	/**
	 * Returns the number of SHT entries in the file.
	 *
	 * @return The number of SHT entries.
	 */
	public short getNumSectionHeaderTableEntries() {
		return nSectionHeaderTableEntries;
	}

	/**
	 * Returns the offset in the file where the Program Header Table starts.
	 *
	 * @return The PHT file offset.
	 */
	public long getProgramHeaderTableOffset() {
		return programHeaderTableOffset;
	}

	/**
	 * Returns the size of each PHT entry.
	 *
	 * @return The PHT entry size.
	 */
	public short getProgramHeaderTableEntrySize() {
		return programHeaderTableEntrySize;
	}

	/**
	 * Returns the offset in the file where the Section Header Table starts.
	 *
	 * @return The SHT file offset.
	 */
	public long getSectionHeaderTableOffset() {
		return sectionHeaderTableOffset;
	}

	/**
	 * Returns the size of each SHT entry.
	 *
	 * @return The SHT entry size.
	 */
	public short getSectionHeaderTableEntrySize() {
		return sectionHeaderTableEntrySize;
	}

	/**
	 * Returns the type of this ELF file object.
	 *
	 * @return The ELF file type.
	 */
	public FileType getFileType() {
		return fileType;
	}

	/**
	 * Returns the virtual address of the entry point of this file.
	 *
	 * @return The entry point virtual address.
	 */
	public long getEntryPointVirtualAddress() {
		return entryPointVirtualAddress;
	}

	/**
	 * Returns the size in bytes of this file header in the file.
	 *
	 * @return The size of this file header.
	 */
	public short getHeaderSize() {
		return headerSize;
	}

	/**
	 * Returns the index of the String Table in the SHT.
	 *
	 * @return The index of the String Table in the SHT.
	 */
	public int getSectionHeaderStringTableIndex() {
		return shstrtab_index;
	}

	@Override
	public String toString() {
		return "FileHeader(bits=" + bits + ";endianness="
				+ endianness + ";version="
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
				+ programHeaderTableEntrySize + ";nProgramHeaderTableEntries="
				+ nProgramHeaderTableEntries + ";sectionHeaderTableEntrySize="
				+ sectionHeaderTableEntrySize + ";nSectionHeaderTableEntries="
				+ nSectionHeaderTableEntries + ";shstrtab_index="
				+ shstrtab_index + ')';
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + HashUtils.hash(bits);
		h = 31 * h + HashUtils.hash(endianness);
		h = 31 * h + HashUtils.hash(version);
		h = 31 * h + osabi.hashCode();
		h = 31 * h + HashUtils.hash(ABIVersion);
		h = 31 * h + fileType.hashCode();
		h = 31 * h + isa.hashCode();
		h = 31 * h + HashUtils.hash(entryPointVirtualAddress);
		h = 31 * h + HashUtils.hash(programHeaderTableOffset);
		h = 31 * h + HashUtils.hash(sectionHeaderTableOffset);
		h = 31 * h + flags;
		h = 31 * h + HashUtils.hash(headerSize);
		h = 31 * h + HashUtils.hash(programHeaderTableEntrySize);
		h = 31 * h + HashUtils.hash(nProgramHeaderTableEntries);
		h = 31 * h + HashUtils.hash(sectionHeaderTableEntrySize);
		h = 31 * h + HashUtils.hash(nSectionHeaderTableEntries);
		h = 31 * h + HashUtils.hash(shstrtab_index);
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
		final FileHeader fh = (FileHeader) other;
		return this.bits == fh.bits
				&& this.endianness == fh.endianness
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
				&& this.nProgramHeaderTableEntries == fh.nProgramHeaderTableEntries
				&& this.sectionHeaderTableEntrySize == fh.sectionHeaderTableEntrySize
				&& this.nSectionHeaderTableEntries == fh.nSectionHeaderTableEntries
				&& this.shstrtab_index == fh.shstrtab_index;
	}
}
