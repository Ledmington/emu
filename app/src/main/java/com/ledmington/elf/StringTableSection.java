package com.ledmington.elf;

public final class StringTableSection extends Section {

    private final char[] table;

    public StringTableSection(final String name, final SHTEntry entry, final ByteBuffer b) {
        super(name, entry);

        final int start = (int) entry.fileOffset();
        final int size = (int) entry.size();
        b.setPosition(start);
        b.setAlignment((int) entry.alignment());
        this.table = new char[size];
        for (int i = 0; i < size; i++) {
            table[i] = (char) b.read1();
        }
    }
}
