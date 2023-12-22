package com.ledmington.elf;

/**
 * This class is just a data holder.
 * No check is performed in the constructor on the given data.
 */
public final class FileHeader {

    private final int magicNumber;
    private final boolean is32Bit;
    private final boolean isLittleEndian;
    private final byte version;
    private final OSABI osabi;
    private final byte ABIVersion;
    private final FileType fileType;
    private final ISA isa;
    private final long entryAddress;
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
            int magic,
            boolean is32Bit,
            boolean isLittleEndian,
            byte version,
            OSABI osabi,
            byte ABIVersion,
            FileType fileType,
            ISA isa,
            long entryAddress,
            long programHeaderTableOffset,
            long sectionHeaderTableOffset,
            int flags,
            short headerSize,
            short programHeaderTableEntrySize,
            short nProgramHeaderTableEntries,
            short sectionHeaderTableEntrySize,
            short nSectionHeaderTableEntries,
            short shstrtab_index) {
        this.magicNumber = magic;
        this.is32Bit = is32Bit;
        this.isLittleEndian = isLittleEndian;
        this.version = version;
        this.osabi = osabi;
        this.ABIVersion = ABIVersion;
        this.fileType = fileType;
        this.isa = isa;
        this.entryAddress = entryAddress;
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

    public boolean isLittleEndian() {
        return isLittleEndian;
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

    public short shstrtab_index() {
        return shstrtab_index;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Magic number         : ");
        sb.append(String.format("0x%08x\n", magicNumber));
        sb.append("Format               : ");
        sb.append(is32Bit ? "32 bit\n" : "64 bit\n");
        sb.append("Endianness           : ");
        sb.append(isLittleEndian ? "2's complement, little endian\n" : "2's complement, big endian\n");
        sb.append("Version              : ");
        sb.append(version);
        sb.append('\n');
        sb.append("OS ABI               : ");
        sb.append(osabi.OSName());
        sb.append('\n');
        sb.append("ABI version          : ");
        sb.append(ABIVersion);
        sb.append('\n');
        sb.append("File type            : ");
        sb.append(fileType.fileTypeName());
        sb.append('\n');
        sb.append("ISA                  : ");
        sb.append(isa.ISAName());
        sb.append('\n');
        sb.append("Entry point address  : ");
        sb.append(String.format("0x%016x", entryAddress));
        if (entryAddress == 0x0L) {
            sb.append(" (none)");
        }
        sb.append('\n');
        sb.append("PHT offset           : ");
        sb.append(String.format("%,d (bytes into file)\n", programHeaderTableOffset));
        sb.append("SHT offset           : ");
        sb.append(String.format("%,d (bytes into file)\n", sectionHeaderTableOffset));
        sb.append("Flags                : ");
        sb.append(String.format("0x%08x\n", flags));
        sb.append("Size of this header  : ");
        sb.append(String.format("%,d (bytes)\n", headerSize));
        sb.append("PHTE size            : ");
        sb.append(String.format("%,d (bytes)\n", programHeaderTableEntrySize));
        sb.append("PHT entries          : ");
        sb.append(nProgramHeaderTableEntries);
        sb.append('\n');
        sb.append("SHTE size            : ");
        sb.append(String.format("%,d (bytes)\n", sectionHeaderTableEntrySize));
        sb.append("SHT entries          : ");
        sb.append(nSectionHeaderTableEntries);
        sb.append('\n');
        sb.append("SHTE names idx       : ");
        sb.append(shstrtab_index);
        sb.append('\n');
        return sb.toString();
    }
}
