package com.ledmington.elf;

public final class SymbolTableEntry {

    private final int nameOffset; // relative to the start of the symbol string table
    private final short sectionTableIndex;
    private final long value;
    private final long size;
    private final byte info;
    private final byte other;

    public SymbolTableEntry(final ByteBuffer b, final boolean is32Bit) {
        if (is32Bit) {
            nameOffset = b.read4();
            value = b.read4AsLong();
            size = b.read4AsLong();
            info = b.read1();
            other = b.read1();
            sectionTableIndex = b.read2();
        } else {
            nameOffset = b.read4();
            info = b.read1();
            other = b.read1();
            sectionTableIndex = b.read2();
            value = b.read8();
            size = b.read8();
        }
    }
}
