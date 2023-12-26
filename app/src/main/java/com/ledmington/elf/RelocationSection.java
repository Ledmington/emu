package com.ledmington.elf;

public final class RelocationSection extends Section {

    private final RelocationEntry[] relocationTable;

    public RelocationSection(
            final String name, final SectionHeader sectionHeader, final ByteBuffer b, final boolean is32Bit) {
        super(name, sectionHeader);

        b.setPosition((int) sectionHeader.fileOffset());
        final int nEntries = (int) (sectionHeader.size() / sectionHeader.entrySize());
        this.relocationTable = new RelocationEntry[nEntries];
        for (int i = 0; i < nEntries; i++) {
            final long offset = is32Bit ? b.read4AsLong() : b.read8();
            final long info = is32Bit ? b.read4AsLong() : b.read8();
            this.relocationTable[i] = new RelocationEntry(offset, info);
        }
    }
}
