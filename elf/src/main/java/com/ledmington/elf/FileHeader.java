package com.ledmington.elf;

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

    public short nProgramHeaderTableEntries() {
        return nProgramHeaderTableEntries;
    }

    public short nSectionHeaderTableEntries() {
        return nSectionHeaderTableEntries;
    }

    public long programHeaderTableOffset() {
        return programHeaderTableOffset;
    }

    public short programHeaderTableEntrySize() {
        return programHeaderTableEntrySize;
    }

    public long sectionHeaderTableOffset() {
        return sectionHeaderTableOffset;
    }

    public short sectionHeaderTableEntrySize() {
        return sectionHeaderTableEntrySize;
    }

    public FileType getType() {
        return fileType;
    }

    public long entryPointVirtualAddress() {
        return entryPointVirtualAddress;
    }

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
                + fileType.fileTypeName()
                + '\n'
                + "ISA                  : "
                + isa.ISAName()
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
}
