package com.ledmington.elf.section;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;
import com.ledmington.utils.WriteOnlyByteBuffer;

public final class DynamicSection extends LoadableSection {

    private final boolean is32Bit;
    private final DynamicTableEntry[] dynamicTable;

    public DynamicSection(
            final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        super(name, sectionHeader);

        this.is32Bit = is32Bit;
        b.setPosition((int) sectionHeader.fileOffset());
        final int entrySize = is32Bit ? 8 : 16;
        final int nEntries = (int) sectionHeader.sectionSize() / entrySize;

        final DynamicTableEntry[] tmp = new DynamicTableEntry[nEntries];
        int i = 0;
        while (i < nEntries) {
            long tag = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            long content = is32Bit ? BitUtils.asLong(b.read4()) : b.read8();
            tmp[i] = new DynamicTableEntry(tag, content);
            if (tmp[i].tag().equals(DynamicTableEntryTag.DT_NULL)) {
                break;
            }
            i++;
        }

        // resize dynamic table
        dynamicTable = new DynamicTableEntry[i];
        System.arraycopy(tmp, 0, dynamicTable, 0, i);
    }

    @Override
    public byte[] content() {
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBuffer(dynamicTable.length * (is32Bit ? 8 : 16));
        for (int i = 0; i < dynamicTable.length; i++) {
            if (is32Bit) {
                bb.write(BitUtils.asInt(dynamicTable[i].tag().code()));
                bb.write(BitUtils.asInt(dynamicTable[i].content()));
            } else {
                bb.write(dynamicTable[i].tag().code());
                bb.write(dynamicTable[i].content());
            }
        }
        return bb.array();
    }
}
