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
    private final long programHeaderStart;
    private final long sectionHeaderStart;
    private final int flags;
    private final short programHeaderTableEntrySize;
    private final short nProgramHeaderTableEntries;
    private final short sectionHeaderTableEntrySize;
    private final short nSectionHeaderTableEntries;
    private final short namesSectionHeaderTableEntryIndex;

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
            long programHeaderStart,
            long sectionHeaderStart,
            int flags,
            short programHeaderTableEntrySize,
            short nProgramHeaderTableEntries,
            short sectionHeaderTableEntrySize,
            short nSectionHeaderTableEntries,
            short namesSectionHeaderTableEntryIndex) {
        this.magicNumber = magic;
        this.is32Bit = is32Bit;
        this.isLittleEndian = isLittleEndian;
        this.version = version;
        this.osabi = osabi;
        this.ABIVersion = ABIVersion;
        this.fileType = fileType;
        this.isa = isa;
        this.entryAddress = entryAddress;
        this.programHeaderStart = programHeaderStart;
        this.sectionHeaderStart = sectionHeaderStart;
        this.flags = flags;
        this.programHeaderTableEntrySize = programHeaderTableEntrySize;
        this.nProgramHeaderTableEntries = nProgramHeaderTableEntries;
        this.sectionHeaderTableEntrySize = sectionHeaderTableEntrySize;
        this.nSectionHeaderTableEntries = nSectionHeaderTableEntries;
        this.namesSectionHeaderTableEntryIndex = namesSectionHeaderTableEntryIndex;
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

    public long programHeaderOffset() {
        return programHeaderStart;
    }

    public long sectionHeaderOffset() {
        return sectionHeaderStart;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Magic number   : ");
        sb.append(String.format("0x%08x\n", magicNumber));
        sb.append("Format         : ");
        sb.append(is32Bit ? "32 bit\n" : "64 bit\n");
        sb.append("Endianness     : ");
        sb.append(isLittleEndian ? "little endian\n" : "big endian\n");
        sb.append("Version        : ");
        sb.append(version);
        sb.append('\n');
        sb.append("OS ABI         : ");
        sb.append(osabi.OSName());
        sb.append('\n');
        sb.append("ABI version    : ");
        sb.append(ABIVersion);
        sb.append('\n');
        sb.append("File type      : ");
        sb.append(fileType.fileTypeName());
        sb.append('\n');
        sb.append("ISA            : ");
        sb.append(isa.ISAName());
        sb.append('\n');
        sb.append("Entry address  : ");
        sb.append(String.format("0x%016x\n", entryAddress));
        sb.append("PH start       : ");
        sb.append(String.format("0x%016x\n", programHeaderStart));
        sb.append("SH start       : ");
        sb.append(String.format("0x%016x\n", sectionHeaderStart));
        sb.append("Flags          : ");
        sb.append(String.format("0x%08x\n", flags));
        sb.append("PHTE size      : ");
        sb.append(programHeaderTableEntrySize);
        sb.append('\n');
        sb.append("PHT entries    : ");
        sb.append(nProgramHeaderTableEntries);
        sb.append('\n');
        sb.append("SHTE size      : ");
        sb.append(sectionHeaderTableEntrySize);
        sb.append('\n');
        sb.append("SHT entries    : ");
        sb.append(nSectionHeaderTableEntries);
        sb.append('\n');
        sb.append("SHTE names idx : ");
        sb.append(namesSectionHeaderTableEntryIndex);
        sb.append('\n');
        return sb.toString();
    }
}
