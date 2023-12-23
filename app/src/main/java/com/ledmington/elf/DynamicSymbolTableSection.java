package com.ledmington.elf;

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
        System.out.printf("Dynamic symbol table has %,d entries\n", nEntries);
        System.out.printf("Alignment : %,d bytes\n", entry.alignment());
        for (int i = 0; i < size; i++) {
            symbolTable[i] = new SymbolTableEntry(b, is32Bit);
            System.out.printf(
                    "%,3d: 0x%016x %5d %7s %7s %7s\n",
                    i,
                    symbolTable[i].value(),
                    symbolTable[i].size(),
                    symbolTable[i].info().type(),
                    symbolTable[i].info().bind(),
                    symbolTable[i].visibility().name());
        }
        System.out.println("Finished parsing dynsym");
    }
}
