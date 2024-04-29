package com.ledmington.elf.section;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;

public final class DynamicSymbolTableSection extends LoadableSection {

    private final boolean is32Bit;
    private final SymbolTableEntry[] symbolTable;

    public DynamicSymbolTableSection(
            final String name, final SectionHeader entry, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        super(name, entry);
        this.is32Bit = is32Bit;

        final int start = (int) entry.fileOffset();
        final int size = (int) entry.sectionSize();
        b.setPosition(start);
        final int symtabEntrySize = (int) entry.entrySize(); // 16 bytes for 32-bits, 24 bytes for 64-bits

        final int nEntries = size / symtabEntrySize;
        this.symbolTable = new SymbolTableEntry[nEntries];
        for (int i = 0; i < nEntries; i++) {
            symbolTable[i] = new SymbolTableEntry(b, is32Bit);
        }
    }

    @Override
    public byte[] content() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(symbolTable.length * (is32Bit ? 16 : 24));
        for (final SymbolTableEntry ste : symbolTable) {
            if (is32Bit) {
                bb.write(ste.nameOffset());
                bb.write(BitUtils.asInt(ste.value()));
                bb.write(BitUtils.asInt(ste.size()));
                bb.write(ste.info().toByte());
                bb.write(ste.visibility().code());
                bb.write(ste.sectionTableIndex());
            } else {
                bb.write(ste.nameOffset());
                bb.write(ste.info().toByte());
                bb.write(ste.visibility().code());
                bb.write(ste.sectionTableIndex());
                bb.write(ste.value());
                bb.write(ste.size());
            }
        }
        return bb.array();
    }
}
