package com.ledmington.elf;

import com.ledmington.utils.HashUtils;

/**
 * This class is just a data holder.
 * No check is performed in the constructor on the given data.
 */
public final class FileHeader {

    private final boolean is32Bit;
    private final boolean isLittleEndian;
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
        this.is32Bit = is32Bit;
        this.isLittleEndian = isLittleEndian;
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
        return is32Bit;
    }

    public short getNumProgramHeaderTableEntries() {
        return nProgramHeaderTableEntries;
    }

    public short getNumSectionHeaderTableEntries() {
        return nSectionHeaderTableEntries;
    }

    public long getProgramHeaderTableOffset() {
        return programHeaderTableOffset;
    }

    public short getProgramHeaderTableEntrySize() {
        return programHeaderTableEntrySize;
    }

    public long getSectionHeaderTableOffset() {
        return sectionHeaderTableOffset;
    }

    public short getSectionHeaderTableEntrySize() {
        return sectionHeaderTableEntrySize;
    }

    public FileType getFileType() {
        return fileType;
    }

    public long getEntryPointVirtualAddress() {
        return entryPointVirtualAddress;
    }

    @Override
    public String toString() {
        return "Format               : " + (is32Bit ? "32 bit\n" : "64 bit\n")
                + "Endianness           : "
                + (isLittleEndian ? "2's complement, little endian\n" : "2's complement, big endian\n")
                + "Version              : "
                + version
                + '\n'
                + "OS ABI               : "
                + osabi.OSName()
                + '\n'
                + "ABI version          : "
                + ABIVersion
                + '\n'
                + "File type            : "
                + fileType.getName()
                + '\n'
                + "ISA                  : "
                + isa.getName()
                + '\n'
                + "Entry point address  : "
                + String.format("0x%016x\n", entryPointVirtualAddress)
                + "PHT offset           : "
                + String.format("%,d (bytes into file)\n", programHeaderTableOffset)
                + "SHT offset           : "
                + String.format("%,d (bytes into file)\n", sectionHeaderTableOffset)
                + "Flags                : "
                + String.format("0x%08x\n", flags)
                + "Size of this header  : "
                + String.format("%,d (bytes)\n", headerSize)
                + "PHTE size            : "
                + String.format("%,d (bytes)\n", programHeaderTableEntrySize)
                + "PHT entries          : "
                + nProgramHeaderTableEntries
                + '\n'
                + "SHTE size            : "
                + String.format("%,d (bytes)\n", sectionHeaderTableEntrySize)
                + "SHT entries          : "
                + nSectionHeaderTableEntries
                + '\n'
                + "SHTE names idx       : "
                + shstrtab_index
                + '\n';
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + HashUtils.hash(is32Bit ? 1 : 0);
        h = 31 * h + HashUtils.hash(isLittleEndian ? 1 : 0);
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
                && this.nProgramHeaderTableEntries == fh.nProgramHeaderTableEntries
                && this.sectionHeaderTableEntrySize == fh.sectionHeaderTableEntrySize
                && this.nSectionHeaderTableEntries == fh.nSectionHeaderTableEntries
                && this.shstrtab_index == fh.shstrtab_index;
    }
}
