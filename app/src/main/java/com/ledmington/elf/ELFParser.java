package com.ledmington.elf;

import java.util.Objects;

import com.ledmington.utils.MiniLogger;

public final class ELFParser {

    private static final MiniLogger logger = MiniLogger.getLogger("elf-parser");

    private byte[] b;
    private long i = 0;

    public ELFParser() {}

    private short asShort(final byte b) {
        return (short) (((short) b) & (short) 0x00ff);
    }

    private int asInt(final byte b) {
        return (int) b & 0x000000ff;
    }

    private long asLong(final byte b) {
        return (long) b & 0x00000000000000ffL;
    }

    private byte read1() {
        final byte x = b[(int) i];
        i++;
        return x;
    }

    private short read2(final boolean isLittleEndian) {
        final short s;
        if (isLittleEndian) {
            s = (short) ((asShort(b[(int) (i + 1)]) << 8) | asShort(b[(int) i]));
        } else {
            s = (short) ((asShort(b[(int) i]) << 8) | asShort(b[(int) (i + 1)]));
        }
        i += 2;
        return s;
    }

    private int read4(final boolean isLittleEndian) {
        final int x;
        if (isLittleEndian) {
            x = (asInt(b[(int) (i + 3)]) << 24)
                    | (asInt(b[(int) (i + 2)]) << 16)
                    | (asInt(b[(int) (i + 1)]) << 8)
                    | asInt(b[(int) i]);
        } else {
            x = (asInt(b[(int) i]) << 24)
                    | (asInt(b[(int) (i + 1)]) << 16)
                    | (asInt(b[(int) (i + 2)]) << 8)
                    | asInt(b[(int) (i + 3)]);
        }
        i += 4;
        return x;
    }

    private long read8(final boolean isLittleEndian) {
        final long x;
        if (isLittleEndian) {
            x = (asLong(b[(int) (i + 7)]) << 56)
                    | (asLong(b[(int) (i + 6)]) << 48)
                    | (asLong(b[(int) (i + 5)]) << 40)
                    | (asLong(b[(int) (i + 4)]) << 32)
                    | (asLong(b[(int) (i + 3)]) << 24)
                    | (asLong(b[(int) (i + 2)]) << 16)
                    | (asLong(b[(int) (i + 1)]) << 8)
                    | asLong(b[(int) i]);
        } else {
            x = (asLong(b[(int) i]) << 56)
                    | (asLong(b[(int) (i + 1)]) << 48)
                    | (asLong(b[(int) (i + 2)]) << 40)
                    | (asLong(b[(int) (i + 3)]) << 32)
                    | (asLong(b[(int) (i + 4)]) << 24)
                    | (asLong(b[(int) (i + 5)]) << 16)
                    | (asLong(b[(int) (i + 6)]) << 8)
                    | asLong(b[(int) (i + 7)]);
        }
        i += 8;
        return x;
    }

    private FileHeader parseFileHeader() {
        final int magicNumber = read4(false);
        if (magicNumber != 0x7f454c46) {
            throw new IllegalArgumentException(
                    String.format("Wrong magic number, expected 0x7f454c46 but was 0x%08x", magicNumber));
        }

        final byte format = read1();
        final boolean is32Bit = format == 1;
        if (format != 1 && format != 2) {
            throw new IllegalArgumentException(
                    String.format("Wrong bit format, expected 1 or 2 but was %d (0x%02x)", format, format));
        }

        final byte endianness = read1();
        final boolean isLittleEndian = endianness == 1;
        if (endianness != 1 && endianness != 2) {
            throw new IllegalArgumentException(
                    String.format("Wrong endianness, expected 1 or 2 but was %d (0x%02x)", endianness, endianness));
        }

        final byte ELFVersion = read1();
        if (ELFVersion != (byte) 0x01) {
            throw new IllegalArgumentException(
                    String.format("Wrong ELF version, expected 1 but was %d (0x%02x)", ELFVersion, ELFVersion));
        }

        final byte osabi = read1();
        if (!OSABI.isValid(osabi)) {
            throw new IllegalArgumentException(String.format("Wrong OS ABI value, was %d (0x%02x)", osabi, osabi));
        }

        final byte ABIVersion = read1();

        for (int idx = 0; idx < 7; idx++) {
            final byte b = read1();
            if (b != (byte) 0x00) {
                logger.warning("Byte %d (0x%08x) of EI_PAD was not 0x00 but 0x%02x", idx, i, b);
            }
        }

        final short fileType = read2(isLittleEndian);
        if (!FileType.isValid(fileType)) {
            throw new IllegalArgumentException(
                    String.format("Wrong file type value, was %d (0x%04x)", fileType, fileType));
        }

        final short isa = read2(isLittleEndian);
        if (!ISA.isValid(isa)) {
            throw new IllegalArgumentException(String.format("Wrong ISA value, was %d (0x%04x)", isa, isa));
        }

        final int ELFVersion_2 = read4(isLittleEndian);
        if (ELFVersion_2 != 0x00000001) {
            throw new IllegalArgumentException(
                    String.format("Wrong ELF version, expected 1 but was %d (0x%08x)", ELFVersion_2, ELFVersion_2));
        }
        if (ELFVersion != ELFVersion_2) {
            throw new IllegalArgumentException(String.format(
                    "ERROR: wrong ELF version, expected to be equal to EI_VERSION byte but was %d (0x%08x)",
                    ELFVersion_2, ELFVersion_2));
        }

        final long entryPoint =
                is32Bit ? (((long) read4(isLittleEndian)) & 0x00000000ffffffffL) : read8(isLittleEndian);

        final long programHeaderStart =
                is32Bit ? (((long) read4(isLittleEndian)) & 0x00000000ffffffffL) : read8(isLittleEndian);

        final long sectionHeaderStart =
                is32Bit ? (((long) read4(isLittleEndian)) & 0x00000000ffffffffL) : read8(isLittleEndian);

        final int flags = read4(isLittleEndian);

        final short headerSize = read2(isLittleEndian);
        if ((is32Bit && headerSize != 52) || (!is32Bit && headerSize != 64)) {
            throw new IllegalArgumentException(String.format(
                    "Wrong header size value, expected %d but was %d (0x%04x)",
                    is32Bit ? 52 : 64, headerSize, headerSize));
        }

        final short programHeaderTableEntrySize = read2(isLittleEndian);

        final short nProgramHeaderTableEntries = read2(isLittleEndian);

        final short sectionHeaderTableEntrySize = read2(isLittleEndian);

        final short nSectionHeaderTableEntries = read2(isLittleEndian);

        final short namesSectionHeaderTableEntryIndex = read2(isLittleEndian);

        return new FileHeader(
                magicNumber,
                format == 1,
                isLittleEndian,
                ELFVersion,
                OSABI.fromCode(osabi),
                ABIVersion,
                FileType.fromCode(fileType),
                ISA.fromCode(isa),
                entryPoint,
                programHeaderStart,
                sectionHeaderStart,
                flags,
                programHeaderTableEntrySize,
                nProgramHeaderTableEntries,
                sectionHeaderTableEntrySize,
                nSectionHeaderTableEntries,
                namesSectionHeaderTableEntryIndex);
    }

    private ProgramHeaderEntry parseProgramHeaderEntry(final boolean is32Bit, final boolean isLittleEndian) {
        final int segmentType = read4(isLittleEndian);
        if (!ProgramHeaderEntryType.isValid(segmentType)) {
            throw new IllegalArgumentException(
                    String.format("Wrong segment type value, was %d (0x%08x)", segmentType, segmentType));
        }

        int flags = 0x00000000;
        if (!is32Bit) {
            flags = read4(isLittleEndian);
        }

        final long segmentOffset =
                is32Bit ? (((long) read4(isLittleEndian)) & 0x00000000ffffffffL) : read8(isLittleEndian);

        final long segmentVirtualAddress =
                is32Bit ? (((long) read4(isLittleEndian)) & 0x00000000ffffffffL) : read8(isLittleEndian);

        final long segmentPhysicalAddress =
                is32Bit ? (((long) read4(isLittleEndian)) & 0x00000000ffffffffL) : read8(isLittleEndian);

        final long segmentSizeOnFile =
                is32Bit ? (((long) read4(isLittleEndian)) & 0x00000000ffffffffL) : read8(isLittleEndian);

        final long segmentSizeInMemory =
                is32Bit ? (((long) read4(isLittleEndian)) & 0x00000000ffffffffL) : read8(isLittleEndian);

        if (is32Bit) {
            flags = read4(isLittleEndian);
        }

        final long alignment = read8(isLittleEndian);
        if (alignment != 0 && Long.bitCount(alignment) != 1) {
            throw new IllegalArgumentException(String.format(
                    "Wrong value for alignment: expected 0 or a power of two but was %,d (0x%016x)",
                    alignment, alignment));
        }

        return new ProgramHeaderEntry(
                ProgramHeaderEntryType.fromCode(segmentType),
                flags,
                segmentOffset,
                segmentVirtualAddress,
                segmentPhysicalAddress,
                segmentSizeOnFile,
                segmentSizeInMemory,
                alignment);
    }

    private SectionHeaderEntry parseSectionHeaderEntry(final boolean is32Bit, final boolean isLittleEndian) {
        final int nameOffset = read4(isLittleEndian);

        final int type = read4(isLittleEndian);
        if (!SectionHeaderEntryType.isValid(type)) {
            throw new IllegalArgumentException(String.format("Wrong section header type: was %d (0x%08x)", type, type));
        }

        final long flags = is32Bit ? ((long) read4(isLittleEndian) & 0x00000000ffffffffL) : read8(isLittleEndian);
        if (!SectionHeaderEntryFlags.isValid(flags)) {
            throw new IllegalArgumentException(
                    String.format("Invalid flags value: was 0x%016x (byte 0x%08x)", flags, i));
        }

        final long virtualAddress =
                is32Bit ? ((long) read4(isLittleEndian) & 0x00000000ffffffffL) : read8(isLittleEndian);

        final long size = is32Bit ? ((long) read4(isLittleEndian) & 0x00000000ffffffffL) : read8(isLittleEndian);

        final int sh_link = read4(isLittleEndian);

        final int sh_info = read4(isLittleEndian);

        final long alignment = is32Bit ? ((long) read4(isLittleEndian) & 0x00000000ffffffffL) : read8(isLittleEndian);
        if (Long.bitCount(alignment) != 1) {
            throw new IllegalArgumentException(String.format(
                    "Wrong value for alignment: expected a power of two but was %,d (0x%016x)", alignment, alignment));
        }

        final long entrySize = is32Bit ? ((long) read4(isLittleEndian) & 0x00000000ffffffffL) : read8(isLittleEndian);

        return new SectionHeaderEntry(
                nameOffset,
                SectionHeaderEntryType.fromCode(type),
                flags,
                virtualAddress,
                size,
                sh_link,
                sh_info,
                alignment,
                entrySize);
    }

    public ELF parse(final byte[] bytes) {
        this.b = Objects.requireNonNull(bytes);
        i = 0;

        final FileHeader fileHeader = parseFileHeader();

        final ProgramHeaderEntry[] programHeaderTable = new ProgramHeaderEntry[fileHeader.nProgramHeaderTableEntries()];
        i = fileHeader.programHeaderOffset();
        for (int k = 0; k < fileHeader.nProgramHeaderTableEntries(); k++) {
            final ProgramHeaderEntry programHeaderEntry =
                    parseProgramHeaderEntry(fileHeader.is32Bit(), fileHeader.isLittleEndian());
            programHeaderTable[k] = programHeaderEntry;
        }

        final SectionHeaderEntry[] sectionHeaderTable = new SectionHeaderEntry[fileHeader.nSectionHeaderTableEntries()];
        i = fileHeader.sectionHeaderOffset();
        for (int k = 0; k < fileHeader.nSectionHeaderTableEntries(); k++) {
            logger.debug("Parsing SHT entry n.%,d", k);
            final SectionHeaderEntry sectionHeaderEntry =
                    parseSectionHeaderEntry(fileHeader.is32Bit(), fileHeader.isLittleEndian());
            sectionHeaderTable[k] = sectionHeaderEntry;
        }

        return new ELF(fileHeader, programHeaderTable, sectionHeaderTable);
    }
}
