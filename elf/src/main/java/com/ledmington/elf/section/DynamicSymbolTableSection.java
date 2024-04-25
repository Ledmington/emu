package com.ledmington.elf.section;

import com.ledmington.utils.ByteBuffer;

public final class DynamicSymbolTableSection extends Section {

    private final SymbolTableEntry[] symbolTable;

    public DynamicSymbolTableSection(
            final String name, final SectionHeader entry, final ByteBuffer b, final boolean is32Bit) {
        super(name, entry);

        final int start = (int) entry.fileOffset();
        final int size = (int) entry.size();
        b.setPosition(start);
        final int symtabEntrySize = (int) entry.entrySize(); // 16 bytes for 32-bits, 24 bytes for 64-bits

        final int nEntries = size / symtabEntrySize;
        this.symbolTable = new SymbolTableEntry[nEntries];
        for (int i = 0; i < nEntries; i++) {
            symbolTable[i] = new SymbolTableEntry(b, is32Bit);
        }
    }
}
