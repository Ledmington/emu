package com.ledmington.elf.section;

import com.ledmington.elf.Section;
import com.ledmington.elf.SectionHeader;
import com.ledmington.utils.ByteBuffer;

public final class SymbolTableSection extends Section {

    private final SymbolTableEntry[] symbolTable;

    public SymbolTableSection(final String name, final SectionHeader entry, final ByteBuffer b, final boolean is32Bit) {
        super(name, entry);

        final int start = (int) entry.fileOffset();
        final int size = (int) entry.size();
        b.setPosition(start);
        final int symtabEntrySize = is32Bit ? 16 : 24;

        final int nEntries = size / symtabEntrySize;
        this.symbolTable = new SymbolTableEntry[nEntries];
        for (int i = 0; i < nEntries; i++) {
            symbolTable[i] = new SymbolTableEntry(b, is32Bit);
        }
    }
}
