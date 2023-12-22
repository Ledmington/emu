package com.ledmington.elf;

public final class SymbolTableEntry {

    private final int nameOffset; // relative to the start of the symbol string table
    private final short sectionTableIndex;
    private final long value;
    private final long size;
    private final SymbolTableEntryInfo info;
    private final byte other;

    public SymbolTableEntry(final ByteBuffer b, final boolean is32Bit) {
        if (is32Bit) {
            nameOffset = b.read4();
            value = b.read4AsLong();
            size = b.read4AsLong();
            info = SymbolTableEntryInfo.fromByte(b.read1());
            other = b.read1();
            sectionTableIndex = b.read2();
        } else {
            nameOffset = b.read4();
            info = SymbolTableEntryInfo.fromByte(b.read1());
            other = b.read1();
            sectionTableIndex = b.read2();
            value = b.read8();
            size = b.read8();
        }
    }

    public String toString() {
        return "Name offset         : " + String.format("0x%08x\n", nameOffset)
                + "Section table index : "
                + String.format("%,d (0x%04x)\n", sectionTableIndex, sectionTableIndex)
                + "Value               : "
                + String.format("%,d (0x%016x)\n", value, value)
                + "Size                : "
                + String.format("%,d bytes (0x%016x)\n", size, size)
                + "Info                : "
                + String.format("0x%02x\n", info.toByte())
                + "Other               : "
                + String.format("0x%02x\n", other);
    }
}
