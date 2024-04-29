package com.ledmington.elf.section;

import com.ledmington.utils.ReadOnlyByteBuffer;

public final class StringTableSection extends LoadableSection {

    private final char[] table;

    public StringTableSection(final String name, final SectionHeader entry, final ReadOnlyByteBuffer b) {
        super(name, entry);

        final int start = (int) entry.fileOffset();
        final int size = (int) entry.sectionSize();
        b.setPosition(start);
        this.table = new char[size];
        for (int i = 0; i < size; i++) {
            table[i] = (char) b.read1();
        }
    }

    @Override
    public byte[] content() {
        final byte[] v = new byte[table.length];
        for (int i = 0; i < table.length; i++) {
            v[i] = (byte) table[i];
        }
        return v;
    }
}
