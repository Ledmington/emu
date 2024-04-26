package com.ledmington.elf.section;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ReadOnlyByteBuffer;

public final class DynamicSection extends Section {

    private final DynamicTableEntry[] dynamicTable;

    public DynamicSection(
            final String name, final SectionHeader sectionHeader, final ReadOnlyByteBuffer b, final boolean is32Bit) {
        super(name, sectionHeader);

        b.setPosition((int) sectionHeader.fileOffset());
        final int entrySize = is32Bit ? 8 : 16;
        final int nEntries = (int) sectionHeader.size() / entrySize;

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
}
