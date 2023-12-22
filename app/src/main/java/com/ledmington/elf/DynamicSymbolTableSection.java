package com.ledmington.elf;

public final class DynamicSymbolTableSection extends Section {

    private final SymbolTableEntry[] symbolTable;

    public DynamicSymbolTableSection(
            final String name, final SectionHeader entry, final ByteBuffer b, final boolean is32Bit) {
        super(name, entry);

        final int start = (int) entry.fileOffset();
        final int size = (int) entry.size();
        b.setPosition(start);
        b.setAlignment((int) entry.alignment());
        final int symtabEntrySize = is32Bit ? 16 : 24;

        System.out.printf("DynSym table size : %,d\n", size);
        System.out.printf("DynSym table entry size : %,d bytes\n", symtabEntrySize);
        System.out.printf("Alignment: %,d bytes\n", entry.alignment());
        this.symbolTable = new SymbolTableEntry[size / symtabEntrySize];
        for (int i = 0; i < size; i++) {
            System.out.printf("Parsing symbol n.%,d -> position: %,d\n", i, b.position());
            symbolTable[i] = new SymbolTableEntry(b, is32Bit);
            System.out.printf("%s\n", symbolTable[i]);
        }
    }
}
