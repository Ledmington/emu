package com.ledmington.elf;

public final class SymbolTableSection extends Section {

    private final SymbolTableEntry[] symbolTable;

    public SymbolTableSection(final String name, final SectionHeader entry, final ByteBuffer b, final boolean is32Bit) {
        super(name, entry);

        final int start = (int) entry.fileOffset();
        final int size = (int) entry.size();
        b.setPosition(start);
        b.setAlignment((int) entry.alignment());
        final int symtabEntrySize = is32Bit ? 16 : 24;

        this.symbolTable = new SymbolTableEntry[size / symtabEntrySize];
        for (int i = 0; i < size; i++) {
            symbolTable[i] = new SymbolTableEntry(b, is32Bit);
        }
    }
}
