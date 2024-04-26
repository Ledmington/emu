package com.ledmington.elf.section;

import com.ledmington.utils.ReadOnlyByteBuffer;

public final class StringTableSection extends Section {

    private final char[] table;

    public StringTableSection(final String name, final SectionHeader entry, final ReadOnlyByteBuffer b) {
        super(name, entry);

        final int start = (int) entry.fileOffset();
        final int size = (int) entry.size();
        b.setPosition(start);
        this.table = new char[size];
        for (int i = 0; i < size; i++) {
            table[i] = (char) b.read1();
        }
    }
}
