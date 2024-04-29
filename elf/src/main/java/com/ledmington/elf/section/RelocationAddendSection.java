package com.ledmington.elf.section;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;

public final class RelocationAddendSection extends LoadableSection {

    private final boolean is32Bit;
    private final RelocationAddendEntry[] relocationAddendTable;

    public RelocationAddendSection(
            final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        super(name, sectionHeader);

        this.is32Bit = is32Bit;
        b.setPosition((int) sectionHeader.fileOffset());
        final int nEntries = (int) (sectionHeader.sectionSize() / sectionHeader.entrySize());
        this.relocationAddendTable = new RelocationAddendEntry[nEntries];
        for (int i = 0; i < nEntries; i++) {
            final long offset = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            final long info = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            final long addend = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            this.relocationAddendTable[i] = new RelocationAddendEntry(offset, info, addend);
        }
    }

    @Override
    public byte[] content() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(relocationAddendTable.length * (is32Bit ? 12 : 24));
        for (int i = 0; i < relocationAddendTable.length; i++) {
            if (is32Bit) {
                bb.write(BitUtils.asInt(relocationAddendTable[i].offset()));
                bb.write(BitUtils.asInt(relocationAddendTable[i].info()));
                bb.write(BitUtils.asInt(relocationAddendTable[i].addend()));
            } else {
                bb.write(relocationAddendTable[i].offset());
                bb.write(relocationAddendTable[i].info());
                bb.write(relocationAddendTable[i].addend());
            }
        }
        return bb.array();
    }
}
