package com.ledmington.elf.section;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;

public final class SymbolTableEntry {

    private final int nameOffset; // relative to the start of the symbol string table
    private final short sectionTableIndex;
    private final long value;
    private final long size;
    private final SymbolTableEntryInfo info;
    private final SymbolTableEntryVisibility visibility;

    public SymbolTableEntry(final ReadOnlyByteBuffer b, final boolean is32Bit) {
        if (is32Bit) {
            nameOffset = b.read4();
            value = BitUtils.asLong(b.read4());
            size = BitUtils.asLong(b.read4());
            info = SymbolTableEntryInfo.fromByte(b.read1());
            visibility = SymbolTableEntryVisibility.fromByte(b.read1());
            sectionTableIndex = b.read2();
        } else {
            nameOffset = b.read4();
            info = SymbolTableEntryInfo.fromByte(b.read1());
            visibility = SymbolTableEntryVisibility.fromByte(b.read1());
            sectionTableIndex = b.read2();
            value = b.read8();
            size = b.read8();
        }
    }

    public long value() {
        return value;
    }

    public long size() {
        return size;
    }

    public SymbolTableEntryInfo info() {
        return info;
    }

    public SymbolTableEntryVisibility visibility() {
        return visibility;
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
                + String.format("0x%02x (%s)\n", info.toByte(), info)
                + "Other               : " + visibility + "\n";
    }
}
