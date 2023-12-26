package com.ledmington.elf;

public final class RelocationAddendSection extends Section {

    private final RelocationAddendEntry[] relocationAddendTable;

    public RelocationAddendSection(
            final String name, final SectionHeader sectionHeader, final ByteBuffer b, final boolean is32Bit) {
        super(name, sectionHeader);

        b.setPosition((int) sectionHeader.fileOffset());
        final int nEntries = (int) (sectionHeader.size() / sectionHeader.entrySize());
        this.relocationAddendTable = new RelocationAddendEntry[nEntries];
        for (int i = 0; i < nEntries; i++) {
            final long offset = is32Bit ? b.read4AsLong() : b.read8();
            final long info = is32Bit ? b.read4AsLong() : b.read8();
            final long addend = is32Bit ? b.read4AsLong() : b.read8();
            this.relocationAddendTable[i] = new RelocationAddendEntry(offset, info, addend);
        }
    }
}
