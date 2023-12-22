package com.ledmington.elf;

import com.ledmington.utils.MiniLogger;

public final class ELFParser {

    private static final MiniLogger logger = MiniLogger.getLogger("elf-parser");

    // A collection of standard section names
    private static final String SECTION_NAMES_TABLE_NAME = ".shstrtab";
    private static final String STRING_TABLE_NAME = ".strtab";
    private static final String SYMBOL_TABLE_NAME = ".symtab";
    private static final String INTERP_SECTION_NAME = ".interp";
    private static final String DYNAMIC_SYMBOL_TABLE_NAME = ".dynsym";

    private ByteBuffer b;

    public ELFParser() {}

    private FileHeader parseFileHeader() {
        final int magicNumber = b.read4();
        if (magicNumber != 0x7f454c46) {
            throw new IllegalArgumentException(
                    String.format("Wrong magic number, expected 0x7f454c46 but was 0x%08x", magicNumber));
        }

        final byte format = b.read1();
        final boolean is32Bit = format == 1;
        if (format != 1 && format != 2) {
            throw new IllegalArgumentException(
                    String.format("Wrong bit format, expected 1 or 2 but was %d (0x%02x)", format, format));
        }

        final byte endianness = b.read1();
        b.setEndianness(endianness == 1);
        if (endianness != 1 && endianness != 2) {
            throw new IllegalArgumentException(
                    String.format("Wrong endianness, expected 1 or 2 but was %d (0x%02x)", endianness, endianness));
        }

        final byte ELFVersion = b.read1();
        if (ELFVersion != (byte) 0x01) {
            throw new IllegalArgumentException(
                    String.format("Wrong ELF version, expected 1 but was %d (0x%02x)", ELFVersion, ELFVersion));
        }

        final byte osabi = b.read1();
        if (!OSABI.isValid(osabi)) {
            throw new IllegalArgumentException(String.format("Wrong OS ABI value, was %d (0x%02x)", osabi, osabi));
        }

        final byte ABIVersion = b.read1();

        for (int idx = 0; idx < 7; idx++) {
            final byte x = b.read1();
            if (x != (byte) 0x00) {
                logger.warning("Byte %d (0x%08x) of EI_PAD was not 0x00 but 0x%02x", idx, b.position(), x);
            }
        }

        final short fileType = b.read2();
        if (!FileType.isValid(fileType)) {
            throw new IllegalArgumentException(
                    String.format("Wrong file type value, was %d (0x%04x)", fileType, fileType));
        }

        final short isa = b.read2();
        if (!ISA.isValid(isa)) {
            throw new IllegalArgumentException(String.format("Wrong ISA value, was %d (0x%04x)", isa, isa));
        }

        final int ELFVersion_2 = b.read4();
        if (ELFVersion_2 != 0x00000001) {
            throw new IllegalArgumentException(
                    String.format("Wrong ELF version, expected 1 but was %d (0x%08x)", ELFVersion_2, ELFVersion_2));
        }
        if (ELFVersion != ELFVersion_2) {
            throw new IllegalArgumentException(String.format(
                    "ERROR: wrong ELF version, expected to be equal to EI_VERSION byte but was %d (0x%08x)",
                    ELFVersion_2, ELFVersion_2));
        }

        final long entryPoint = is32Bit ? b.read4AsLong() : b.read8();

        final long PHTOffset = is32Bit ? b.read4AsLong() : b.read8();

        final long SHTOffset = is32Bit ? b.read4AsLong() : b.read8();

        final int flags = b.read4();

        final short headerSize = b.read2();
        if ((is32Bit && headerSize != 52) || (!is32Bit && headerSize != 64)) {
            throw new IllegalArgumentException(String.format(
                    "Wrong header size value, expected %d but was %d (0x%04x)",
                    is32Bit ? 52 : 64, headerSize, headerSize));
        }

        final short programHeaderTableEntrySize = b.read2();

        final short nProgramHeaderTableEntries = b.read2();

        final short sectionHeaderTableEntrySize = b.read2();

        final short nSectionHeaderTableEntries = b.read2();

        final short namesSectionHeaderTableEntryIndex = b.read2();

        return new FileHeader(
                magicNumber,
                format == 1,
                endianness == 1,
                ELFVersion,
                OSABI.fromCode(osabi),
                ABIVersion,
                FileType.fromCode(fileType),
                ISA.fromCode(isa),
                entryPoint,
                PHTOffset,
                SHTOffset,
                flags,
                headerSize,
                programHeaderTableEntrySize,
                nProgramHeaderTableEntries,
                sectionHeaderTableEntrySize,
                nSectionHeaderTableEntries,
                namesSectionHeaderTableEntryIndex);
    }

    private PHTEntry parseProgramHeaderEntry(final boolean is32Bit) {
        final int segmentType = b.read4();
        if (!PHTEntryType.isValid(segmentType)) {
            throw new IllegalArgumentException(
                    String.format("Wrong segment type value, was %d (0x%08x)", segmentType, segmentType));
        }

        int flags = 0x00000000;
        if (!is32Bit) {
            flags = b.read4();
        }

        final long segmentOffset = is32Bit ? b.read4AsLong() : b.read8();

        final long segmentVirtualAddress = is32Bit ? b.read4AsLong() : b.read8();

        final long segmentPhysicalAddress = is32Bit ? b.read4AsLong() : b.read8();

        final long segmentSizeOnFile = is32Bit ? b.read4AsLong() : b.read8();

        final long segmentSizeInMemory = is32Bit ? b.read4AsLong() : b.read8();

        if (is32Bit) {
            flags = b.read4();
        }

        final long alignment = b.read8();
        if (alignment != 0
                && Long.bitCount(alignment) != 1
                && segmentVirtualAddress % alignment != segmentOffset % alignment) {
            if (alignment != 0 && Long.bitCount(alignment) != 1) {
                throw new IllegalArgumentException(String.format(
                        "Wrong value for alignment: expected 0 or a power of two but was %,d (0x%016x)",
                        alignment, alignment));
            } else {
                throw new IllegalArgumentException(String.format(
                        "Wrong value for alignment: expected 0x%016x %% %,d to be equal to 0x%016x %% %,d but wasn't",
                        segmentVirtualAddress, alignment, segmentOffset, alignment));
            }
        }

        return new PHTEntry(
                PHTEntryType.fromCode(segmentType),
                flags,
                segmentOffset,
                segmentVirtualAddress,
                segmentPhysicalAddress,
                segmentSizeOnFile,
                segmentSizeInMemory,
                alignment);
    }

    private SectionHeader parseSectionHeaderEntry(final boolean is32Bit) {
        final int nameOffset = b.read4();

        final int type = b.read4();
        if (!SectionHeaderType.isValid(type)) {
            throw new IllegalArgumentException(String.format("Wrong section header type: was %d (0x%08x)", type, type));
        }

        final long flags = is32Bit ? b.read4AsLong() : b.read8();
        if (!SectionHeaderFlags.isValid(flags)) {
            final StringBuilder sb = new StringBuilder();
            for (final SectionHeaderFlags f : SectionHeaderFlags.values()) {
                if ((flags & f.code()) != 0L) {
                    sb.append(f.id());
                }
            }
            throw new IllegalArgumentException(
                    String.format("Invalid flags value: was 0x%016x (%s)", flags, sb.toString()));
        }

        final long virtualAddress = is32Bit ? b.read4AsLong() : b.read8();

        final long fileOffset = is32Bit ? b.read4AsLong() : b.read8();

        final long size = is32Bit ? b.read4AsLong() : b.read8();

        final int sh_link = b.read4();

        final int sh_info = b.read4();

        final long alignment = is32Bit ? b.read4AsLong() : b.read8();
        if (alignment != 0 && Long.bitCount(alignment) != 1) {
            throw new IllegalArgumentException(String.format(
                    "Wrong value for alignment: expected a power of two but was %,d (0x%016x)", alignment, alignment));
        }

        final long entrySize = is32Bit ? b.read4AsLong() : b.read8();

        return new SectionHeader(
                nameOffset,
                SectionHeaderType.fromCode(type),
                flags,
                virtualAddress,
                fileOffset,
                size,
                sh_link,
                sh_info,
                alignment,
                entrySize);
    }

    private PHTEntry[] parseProgramHeaderTable(final FileHeader fileHeader) {
        final int nPHTEntries = fileHeader.nProgramHeaderTableEntries();
        final PHTEntry[] programHeaderTable = new PHTEntry[nPHTEntries];
        final int PHTOffset = (int) fileHeader.programHeaderTableOffset();
        final int PHTEntrySize = fileHeader.programHeaderTableEntrySize();

        for (int k = 0; k < nPHTEntries; k++) {
            b.setPosition(PHTOffset + k * PHTEntrySize);
            final PHTEntry programHeaderEntry = parseProgramHeaderEntry(fileHeader.is32Bit());
            programHeaderTable[k] = programHeaderEntry;
        }

        return programHeaderTable;
    }

    private SectionHeader[] parseSectionHeaderTable(final FileHeader fileHeader) {
        final int nSHTEntries = fileHeader.nSectionHeaderTableEntries();
        final SectionHeader[] sectionHeaderTable = new SectionHeader[nSHTEntries];
        final int SHTOffset = (int) fileHeader.sectionHeaderTableOffset();
        final int SHTEntrySize = fileHeader.sectionHeaderTableEntrySize();

        for (int k = 0; k < nSHTEntries; k++) {
            b.setPosition(SHTOffset + k * SHTEntrySize);
            final SectionHeader sectionHeaderEntry = parseSectionHeaderEntry(fileHeader.is32Bit());
            sectionHeaderTable[k] = sectionHeaderEntry;
        }

        return sectionHeaderTable;
    }

    private Section[] parseSectionTable(final FileHeader fileHeader, final SectionHeader[] sectionHeaderTable) {
        final Section[] sectionTable = new Section[fileHeader.nSectionHeaderTableEntries()];

        final int shstr_offset = (int) sectionHeaderTable[sectionHeaderTable.length - 1].fileOffset();
        for (int k = 0; k < sectionHeaderTable.length; k++) {
            final SectionHeader entry = sectionHeaderTable[k];
            b.setPosition(shstr_offset + entry.nameOffset());

            final StringBuilder sb = new StringBuilder();
            char c = (char) b.read1();
            while (c != '\0') {
                sb.append(c);
                c = (char) b.read1();
            }
            final String name = sb.toString();

            final String typeName = entry.type().name();

            if (typeName.equals(SectionHeaderType.SHT_NULL.name())) {
                sectionTable[k] = new NullSection(entry);
            } else if (name.equals(SYMBOL_TABLE_NAME) || typeName.equals(SectionHeaderType.SHT_SYMTAB.name())) {
                sectionTable[k] = new SymbolTableSection(name, entry, b, fileHeader.is32Bit());
            } else if (name.equals(SECTION_NAMES_TABLE_NAME)
                    || name.equals(STRING_TABLE_NAME)
                    || typeName.equals(SectionHeaderType.SHT_STRTAB.name())) {
                sectionTable[k] = new StringTableSection(name, entry, b);
            } else if (name.equals(INTERP_SECTION_NAME)) {
                sectionTable[k] = new InterpreterPathSection(name, entry, b);
            } else if (name.equals(DYNAMIC_SYMBOL_TABLE_NAME) || typeName.equals(SectionHeaderType.SHT_DYNSYM.name())) {
                sectionTable[k] = new DynamicSymbolTableSection(name, entry, b, fileHeader.is32Bit());
            } else {
                logger.warning(String.format(
                        "Don't know how to parse section n.%,d with type %s and name '%s'", k, typeName, name));
                sectionTable[k] = null;
            }
        }

        return sectionTable;
    }

    public ELF parse(final byte[] bytes) {
        this.b = new ByteBuffer(bytes);

        final FileHeader fileHeader = parseFileHeader();

        final PHTEntry[] programHeaderTable = parseProgramHeaderTable(fileHeader);

        final SectionHeader[] sectionHeaderTable = parseSectionHeaderTable(fileHeader);

        final Section[] sectionTable = parseSectionTable(fileHeader, sectionHeaderTable);

        return new ELF(fileHeader, programHeaderTable, sectionTable);
    }
}
