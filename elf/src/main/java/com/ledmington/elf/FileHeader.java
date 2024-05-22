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
package com.ledmington.elf;

import com.ledmington.utils.HashUtils;

/** This class is just a data holder. No check is performed in the constructor on the given data. */
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

    public FileHeader(
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
            short nProgramHeaderTableEntries,
            short sectionHeaderTableEntrySize,
            short nSectionHeaderTableEntries,
            short shstrtab_index) {
        this.bits = is32Bit;
        this.endianness = isLittleEndian;
        this.version = version;
        this.osabi = osabi;
        this.ABIVersion = ABIVersion;
        this.fileType = fileType;
        this.isa = isa;
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

    public boolean is32Bit() {
        return bits;
    }

    public boolean isLittleEndian() {
        return endianness;
    }

    public OSABI getOSABI() {
        return osabi;
    }

    public byte getABIVersion() {
        return ABIVersion;
    }

    public ISA getISA() {
        return isa;
    }

    public int getVersion() {
        return 1;
    }

    public int getFlags() {
        return flags;
    }

    /** Returns the number of PHT entries in the file. */
    public short getNumProgramHeaderTableEntries() {
        return nProgramHeaderTableEntries;
    }

    /** Returns the number of SHT entries in the file. */
    public short getNumSectionHeaderTableEntries() {
        return nSectionHeaderTableEntries;
    }

    /** Returns the offset in the file where the Program Header Table starts. */
    public long getProgramHeaderTableOffset() {
        return programHeaderTableOffset;
    }

    /** Returns the size of each PHT entry. */
    public short getProgramHeaderTableEntrySize() {
        return programHeaderTableEntrySize;
    }

    /** Returns the offset in the file where the Section Header Table starts. */
    public long getSectionHeaderTableOffset() {
        return sectionHeaderTableOffset;
    }

    /** Returns the size of each SHT entry. */
    public short getSectionHeaderTableEntrySize() {
        return sectionHeaderTableEntrySize;
    }

    public FileType getFileType() {
        return fileType;
    }

    public long getEntryPointVirtualAddress() {
        return entryPointVirtualAddress;
    }

    public int getHeaderSize() {
        return headerSize;
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

    public int getSectionHeaderStringTableIndex() {
        return shstrtab_index;
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
